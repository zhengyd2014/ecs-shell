package com.emc.ecs.client.management;

import com.emc.ecs.client.ECSClient;
import com.emc.ecs.client.exceptions.ServiceErrorException;
import com.emc.ecs.client.provision.StoragePoolCreateParam;
import com.emc.ecs.client.provision.StoragePoolNodeListRestRep;
import com.emc.ecs.client.provision.StoragePoolRestRep;
import com.emc.ecs.util.JSONUtil;
import com.emc.ecs.util.UrlUtil;
import com.emc.storageos.data.datasvcmodels.ObjectNamedRelatedResourceRep;
import com.emc.storageos.data.datasvcmodels.RetentionMinMaxGovernor;
import com.emc.storageos.data.datasvcmodels.StringHashMapEntry;
import com.emc.storageos.data.datasvcmodels.VirtualArrayRestRep;
import com.emc.storageos.data.datasvcmodels.bucket.ObjectBucketInfoRestRep;
import com.emc.storageos.data.datasvcmodels.bucket.ObjectBucketParam;
import com.emc.storageos.data.datasvcmodels.datastore.CommodityDataStoreParam;
import com.emc.storageos.data.datasvcmodels.datastore.CommodityParam;
import com.emc.storageos.data.datasvcmodels.namespace.NamespaceCreateParam;
import com.emc.storageos.data.datasvcmodels.namespace.NamespaceList;
import com.emc.storageos.data.datasvcmodels.nodes.DataNodeRestRep;
import com.emc.storageos.data.datasvcmodels.user.BlobUser;
import com.emc.storageos.data.datasvcmodels.user.UserCreateParam;
import com.emc.storageos.data.datasvcmodels.user.UserSecretKeyCreateParam;
import com.emc.storageos.data.datasvcmodels.user.UsersList;
import com.emc.storageos.data.datasvcmodels.vpool.DataServiceVpoolCreateParam;
import com.emc.storageos.data.datasvcmodels.vpool.DataServiceVpoolRestRep;
import com.emc.storageos.data.datasvcmodels.zone.VdcInsertParam;
import com.emc.storageos.data.datasvcmodels.zone.VdcRestRep;
import com.emc.storageos.data.datasvcmodels.zone.VdcSecretKeyRestRep;
import com.emc.storageos.objcontrol.service.impl.resource.dynamicconfig.model.DynamicConfigResp;
import org.apache.tapestry5.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengf1 on 11/8/16.
 */
public class ECSInfo {

    public static String VDCS_INFO = "vdcs";
    public static String NODES_INFO = "nodes";
    public static String STORAGE_POOLS_INFO = "storagePools";
    public static String REPLICATION_GROUPS_INFO = "replicationGroups";
    public static String NAMESPACES_INFO = "namespaces";
    public static String BUCKETS_INFO = "buckets";
    public static String USERS_INFO = "users";

    public static String OBJECT_USER = "OBJECT_USER";
    public static String OBJECT_USER_SECRET = "OBJECT_USER_SECRET";

    private String ecs;
    private String user;
    private String serviceUser = "emcservice";
    private String password;

    ECSClientExt ecsClient;
    ECSClientExt ecsServiceClient;


    /**
     *  components of ECS, getting from ECS the first time accessing them,
     *  further accessing will just return the value stored in the variables.
     */
    private List<VdcRestRep> vdcs;
    private List<DataNodeRestRep> nodes;
    private List<VirtualArrayRestRep> storagePools;
    private List<ObjectNamedRelatedResourceRep> namespaces;
    private List<ECSBucketInfo> buckets;
    private List<DataServiceVpoolRestRep> replicationGroups;
    private List<ECSUserInfo> users;
    private ECSResourceUsage resourceUsage;

    public ECSInfo(String host) {
        this(host, "root", "ChangeMe");
    }

    public ECSInfo(String host, String user, String password) {
        this.ecs = host;
        this.user = user;
        this.password = password;
        ecsClient = (ECSClientExt) new ECSClientExt(host,true, null).withLogin(user, password);
        ecsServiceClient = (ECSClientExt) new ECSClientExt(host, true, "json").withLogin(serviceUser, password);
    }

    /**
     * set all the components to null, so next time it will retrieve the list from ECS, instead from cache.
     */
    public void refresh() {
        vdcs = null;
        nodes = null;
        storagePools = null;
        namespaces = null;
        buckets = null;
        replicationGroups = null;
        users = null;
        resourceUsage = null;
    }

    public ECSClient getEcsClient() {
        return this.ecsClient;
    }

    /**
     * get blob users and their secret key
     *
     * @return
     * @throws Exception
     */
    public List<ECSUserInfo> getUsers() throws Exception {
        if (users == null) {
            users = new ArrayList<ECSUserInfo>();
            UsersList usersList = ecsClient.users().getAll(null);
            for (BlobUser blobUser : usersList.getUsers()) {
                ECSUserInfo user = new ECSUserInfo(blobUser, getObjectUserSecret(blobUser.getUser().toString()));
                users.add(user);
            }
        }
        return users;
    }

    public List<VdcRestRep> getVdcs() {
        if (vdcs == null) {
            // ecsClient.setMediaTypeToXml();
            vdcs = ecsClient.vdcs().list();
        }
        return vdcs;
    }

    public List<VirtualArrayRestRep> getStoragePools() {
        if (storagePools == null) {
            try {
                storagePools = ecsClient.virtualArrays().list();
            } catch (Exception ex) {
                if (ex.getMessage().contains("No Data Store")) {
                    storagePools = new ArrayList<VirtualArrayRestRep>();
                }
            }
        }
        return storagePools;
    }

    public List<VirtualArrayRestRep> getStoragePools(String endPoint) {
        ECSClient client = new ECSClient(endPoint).withLogin(user, password);
        List<VirtualArrayRestRep> varrays = client.virtualArrays().list();
        client.auth().logout();
        return varrays;
    }

    public List<DataServiceVpoolRestRep> getReplicationGroups() {
        if (replicationGroups == null) {
            replicationGroups = ecsClient.vpools().list();
        }
        return replicationGroups;
    }


    public List<ObjectNamedRelatedResourceRep> getNamespaces() {
        if (namespaces == null) {
            NamespaceList namespaceList = ecsClient.namespaces().list(null);
            namespaces = namespaceList.getNamespaces();
        }

        return namespaces;
    }

    /**
     *  list all buckets in ECS
     *
     * @return
     */
    public List<ECSBucketInfo> getBuckets() {
        if (buckets != null) {
            return buckets;
        }

        // else load bucketInfoList from ECS
        List<ObjectNamedRelatedResourceRep> repList = getNamespaces();
        List<ECSBucketInfo> bucketInfos = new ArrayList<ECSBucketInfo>();
        for (ObjectNamedRelatedResourceRep rep : repList) {
            List<ObjectBucketInfoRestRep> bucketInfoRestRepList = ecsClient.namespaces().getBuckets(rep.getName());
            for (ObjectBucketInfoRestRep bucketRep : bucketInfoRestRepList) {
                String url = "/object/billing/buckets/" + rep.getName() + "/" + bucketRep.getName() + "/info";
                Object response =  ((ECSClientExt)ecsClient).config().getJson(url);
                JSONObject bucket = JSONUtil.responseToJSONObject(response);

                ECSBucketInfo bucketInfo = new ECSBucketInfo(
                        bucket.getString("namespace"),
                        bucket.getString("name"),
                        bucket.getString("vpool_id"),
                        bucket.getLong("total_size"),
                        bucket.getLong("total_objects"));

                bucketInfos.add(bucketInfo);
            }
        }

        buckets = bucketInfos;
        return bucketInfos;
    }


    public void addLicense() throws Exception {
        File licenseFile = new File("ecs-test-license.lic");
        System.out.println("upload license file: " + licenseFile.getAbsolutePath());
        String license = UrlUtil.convertStreamToString(new FileInputStream(licenseFile));
        ecsClient.setMediaTypeToJson();
        ecsClient.licensing().set(license);
        ecsClient.setMediaTypeToXml();
    }

    /**
     * create storagePools, and assign all the nodes in the VDC to the pool
     *
     * @param name
     * @throws Exception
     */
    public void createStoragePool(String name) throws Exception {
        if (getStoragePools() != null && getStoragePools().size() > 0) {
            System.out.println("already have one storage pool, skip creating additional storage pools");
            return;
        }

        StoragePoolCreateParam sp = new StoragePoolCreateParam();
        sp.setName(name);
        sp.setIsColdStorageEnabled(false);
        sp.setIsProtected("false");
        sp.setDescription("created by ecs-shell");
        StoragePoolRestRep spr = ecsClient.storagePools().create(sp);
        System.out.println("Storage Pool (Virtual Array): " + name + " created");
        StoragePoolNodeListRestRep nodeList = ecsClient.storagePools().getAllNodes();
        List<DataNodeRestRep> nodes = nodeList.getStoragePoolNodeList();

        CommodityDataStoreParam ds = new CommodityDataStoreParam();
        List<CommodityParam> cplist = new ArrayList<CommodityParam>();
        for (DataNodeRestRep node : nodes) {
            System.out.println(node.getNodeName() + " : " + node.getIp());
            CommodityParam cp = new CommodityParam();
            cp.setName(node.getNodeName());
            cp.setNodeId("node" + node.getIp());
            cp.setVirtualArray(spr.getId().toString());
            cplist.add(cp);
        }

        ds.setCommodityParams(cplist);
        ecsClient.storagePools().assignNode(ds);

        // reset stroagePools to null, so next time it will get reload.
        storagePools = null;
    }

    public void createVdc(String name) {
        if (getVdcs() != null && getVdcs().size() > 0) {
            System.out.println("local vdc already created, skip creating.");
            return;
        }

        VdcInsertParam vdcInsertParam = new VdcInsertParam();
        vdcInsertParam.setVdcName(name);
        vdcInsertParam.setSecretKeys("Czik74rudoKJlMX6IHnT");

        String endPoints = "";
        for (DataNodeRestRep nodeRestRep : getNodes()) {
            endPoints += nodeRestRep.getIp() + ",";
        }
        endPoints = endPoints.substring(0,endPoints.length()-1);

        vdcInsertParam.setInterVdcEndPoints(endPoints);
        vdcInsertParam.setManagementEndPoints(endPoints);
        //System.out.println(endPoints);
        ecsClient.vdcs().updateVdc(name,vdcInsertParam);

        // refresh vdcs
        vdcs = null;
    }

    public void createReplicationGroup(String name) {
        DataServiceVpoolCreateParam createParam = new DataServiceVpoolCreateParam();
        createParam.setAllowAllNamespaces(true);
        createParam.setDescription("created by ecs shell");
        createParam.setEnableRebalancing(true);
        createParam.setFullRep(false);
        createParam.setName(name);

        List<StringHashMapEntry> mappings = new ArrayList<StringHashMapEntry>();
        for (VdcRestRep vdc : getVdcs()) {
            StringHashMapEntry entry = new StringHashMapEntry();
            String endPoint = vdc.getInterVdcEndPoints().split(",")[0];
            entry.setName(vdc.getVdcId());
            entry.setValue(getStoragePools(endPoint).get(0).getId());
            mappings.add(entry);
        }
        createParam.setVarrayMappings(mappings);
        ecsClient.vpools().createVpool(createParam);
        replicationGroups = null;
    }

    public void createNamespace(String name) throws Exception {
        NamespaceCreateParam create = new NamespaceCreateParam();
        create.setNamespace(name);
        create.setDefaultBucketBlockSize(-1L);
        create.setIsComplianceEnabled(false);
        create.setIsEncryptionEnabled(false);
        create.setIsStaleAllowed(false);
        create.setTenantDefaultProject(new URI(name));
        create.setTenantDefaultVpool(getReplicationGroups().get(0).getId());
        ecsClient.namespaces().create(create);

        // refresh namespace
        namespaces = null;
    }

    public void createUser(String name) throws Exception {
        UserCreateParam user = new UserCreateParam();
        user.setNamespace(getNamespaces().get(0).getName());
        user.setUser(name);
        ecsClient.users().add(user);
        UserSecretKeyCreateParam keyParam = new UserSecretKeyCreateParam();
        keyParam.setNamespace(getNamespaces().get(0).getName());
        ecsClient.userSecretKeys().create(new URI(name),keyParam);
        // refresh users
        users = null;
    }


    public void createBucket(String name) throws Exception {
        ObjectBucketParam bucket = new ObjectBucketParam();
        bucket.setNamespace(getNamespaces().get(0).getName());
        bucket.setName(name);
        bucket.setBucketOwner(getUsers().get(0).getUserName());
        bucket.setDefaultGroupDirExecutePermission(false);
        bucket.setDefaultGroupDirReadPermission(false);
        bucket.setDefaultGroupDirWritePermission(false);
        bucket.setDefaultGroupFileExecutePermission(false);
        bucket.setDefaultGroupFileReadPermission(false);
        bucket.setDefaultGroupFileWritePermission(false);
        bucket.setFilesystemEnabled(false);
        bucket.setIsEncryptionEnabled(false);
        bucket.setIsStaleAllowed(false);
        RetentionMinMaxGovernor governor = new RetentionMinMaxGovernor();
        governor.setEnforceRetention(false);
        bucket.setMinMaxGovernor(governor);
        bucket.setTags(null);
        bucket.setVpool(getReplicationGroups().get(0).getId());
        ecsClient.buckets().create(bucket);

        // refresh buckets
        buckets = null;
    }

    /**
     * get all node in local vdc
     *
     * @return
     */
    public List<DataNodeRestRep> getNodes() {
        if (nodes == null) {
            StoragePoolNodeListRestRep nodeList = ecsClient.storagePools().getAllNodes();
            nodes = nodeList.getStoragePoolNodeList();
        }
        return nodes;
    }


    public void linkVdc(String remoteEcsIp, String name) {

        ECSClient remoteEcsClient = new ECSClientExt(remoteEcsIp,true, null).withLogin(user, password);
        try {
            VdcRestRep remotevdc = remoteEcsClient.vdcs().getLocal();
            System.out.println("remote vdc created, no need to link");
            System.out.println(remotevdc.getName());
            System.out.println(remotevdc.getManagementEndPoints());
            System.out.println(remotevdc.getInterVdcEndPoints());
            System.out.println(remotevdc.getInterVdcCmdEndPoints());
            System.out.println(remotevdc.getSecretKeys());
            System.out.println(remotevdc.getSecretKeys());
            return;
        } catch (ServiceErrorException ex) {
            // do nothing
        }

        VdcSecretKeyRestRep key = remoteEcsClient.vdcs().getLocalSecret();
        System.out.println("get vdc secret key: " + key.getKey());

        VdcInsertParam vdcParam = new VdcInsertParam();
        vdcParam.setSecretKeys(key.getKey());

        String endPoints = "";
        for (DataNodeRestRep nodeRestRep : remoteEcsClient.storagePools().getAllNodes().getStoragePoolNodeList()) {
            endPoints += nodeRestRep.getIp() + ",";
        }
        endPoints = endPoints.substring(0,endPoints.length()-1);

        vdcParam.setInterVdcEndPoints(endPoints);
        vdcParam.setManagementEndPoints(endPoints);
        vdcParam.setVdcName(name);
        //System.out.println(endPoints);
        ecsClient.vdcs().updateVdc(name,vdcParam);

        // refresh vdcs
        vdcs = null;
    }


    public DynamicConfigResp listEcsConfig() {
        DynamicConfigResp resp = ((ECSClientExt)ecsServiceClient).config().getConfig();
        return resp;
    }

    public void setEcsConfig(String name, String value) {
        ((ECSClientExt)ecsServiceClient).config().setConfig(name, value);
    }


    public ECSResourceUsage getStatsCluster() {
        if (resourceUsage == null) {
            Object object  = ecsClient.config().getStatCluster(ECSResourceUsage.DEFAUL_HOURS);
            resourceUsage = new ECSResourceUsage(JSONUtil.responseToJSONObject(object));
        }

        return resourceUsage;
    }


    private String getObjectUserSecret(String user) throws Exception{
        return ecsClient.userSecretKeys().get(new URI(user)).getSecretKey1();
    }


    public static void main(String[] args) throws Exception {
        ECSInfo info = new ECSInfo("10.243.85.45");
        info.getStatsCluster();
    }
}
