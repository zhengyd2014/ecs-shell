package com.emc.ecs.client.management;

import asg.cliche.Command;
import com.emc.ecs.util.Output;
import com.emc.ecs.util.Ssh2Exec;
import com.emc.storageos.data.datasvcmodels.nodes.DataNodeRestRep;
import com.emc.storageos.data.datasvcmodels.zone.VdcRestRep;
import com.emc.storageos.objcontrol.object.shared.DataNodeInfo;
import com.emc.storageos.objcontrol.service.impl.resource.dynamicconfig.model.DynamicConfigResp;

import java.util.List;

/**
 * Created by zhengf1 on 12/22/16.
 */
public class ECSInfoHandler {

    private String ecs;
    private ECSInfo ecsInfo;

    public ECSInfoHandler(String ip) {
        this.ecs = ip;
        ecsInfo = new ECSInfo(ip);
    }

    @Command
    public void siteCommand(String cmd) throws Exception{
        List<DataNodeRestRep> nodes = ecsInfo.getNodes();
        for (DataNodeRestRep node : nodes) {
            System.out.println(" ------- " + node.getIp() + " --------");
            Ssh2Exec shell = new Ssh2Exec(node.getIp(), "root", "ChangeMe");
            shell.execCmd(cmd);
        }
    }

    @Command
    public void clusterCommand(String cmd) throws Exception{
        List<VdcRestRep> vdcs = ecsInfo.getVdcs();
        for (VdcRestRep vdc : vdcs) {
            System.out.println(" ======= " + vdc.getVdcName() + " ========");
            ECSInfo ecsVdc = new ECSInfo(vdc.getManagementEndPoints().split(",")[0]);
            List<DataNodeRestRep> nodes = ecsVdc.getNodes();
            for (DataNodeRestRep node : nodes) {
                System.out.println(" ------- " + node.getIp() + " --------");
                Ssh2Exec shell = new Ssh2Exec(node.getIp(), "admin", "ChangeMe");
                shell.execCmd(cmd);
            }
        }
    }

    @Command (description = "list buckets")
    public void listBuckets() {
        Output.printBuckets(ecsInfo.getBuckets());
    }

    @Command (description = "add license to ecs")
    public void addLicense() throws Exception {
        ecsInfo.addLicense();
    }

    @Command (description = "create storage pool with all nodes in current data center")
    public void createStoragePool(String name) throws Exception {
        ecsInfo.createStoragePool(name);
        Output.printStoragePools(ecsInfo.getStoragePools());
    }

    @Command
    public void createVdc(String name) throws Exception {
        ecsInfo.createVdc(name);
        Output.printVdcs(ecsInfo.getVdcs());
    }

    @Command
    public void linkVdc(String remote_ecs_ip, String name) throws Exception {
        ecsInfo.linkVdc(remote_ecs_ip, name);
        Output.printVdcs(ecsInfo.getVdcs());
    }

    @Command
    public void createReplicationGroup(String name) throws Exception {
        ecsInfo.createReplicationGroup(name);
        Output.printReplicationGroups(ecsInfo.getReplicationGroups());
    }

    @Command
    public void createNamespace(String name) throws Exception {
        ecsInfo.createNamespace(name);
        Output.printNamespaces(ecsInfo.getNamespaces());
    }

    @Command (description = "create an object user, which will belongs to first namespace")
    public void createUser(String name) throws Exception {
        ecsInfo.createUser(name);
        Output.printUsers(ecsInfo.getUsers());
    }

    @Command (description = "create buckets, it will use the first Namespace")
    public void createBucket(String name) throws Exception {
        ecsInfo.createBucket(name);
        Output.printBuckets(ecsInfo.getBuckets());
    }

    @Command
    public void getResourceUsage() throws Exception {
        ECSResourceUsage response = ecsInfo.getStatsCluster();
        Output.print(response);
    }

    @Command (description = "refresh ecs info")
    public void refresh() {
        ecsInfo.refresh();
    }

    @Command
    public void info() throws Exception {
        Output.printVdcs(ecsInfo.getVdcs());
        Output.printNodes(ecsInfo.getNodes());
        Output.printStoragePools(ecsInfo.getStoragePools());
        Output.printReplicationGroups(ecsInfo.getReplicationGroups());
        Output.printNamespaces(ecsInfo.getNamespaces());
        Output.printBuckets(ecsInfo.getBuckets());
        Output.printUsers(ecsInfo.getUsers());
    }

    @Command (description = "show info by type, valid types: vdcs, nodes, storagePools, replicationGroups, namespaces, buckets, users.")
    public void show(String info_type) throws Exception {
        if (info_type.equalsIgnoreCase(ECSInfo.VDCS_INFO)) {
            Output.printVdcs(ecsInfo.getVdcs());
        } else if (info_type.equalsIgnoreCase(ECSInfo.NODES_INFO)) {
            Output.printNodes(ecsInfo.getNodes());
        } else if (info_type.equalsIgnoreCase(ECSInfo.STORAGE_POOLS_INFO)) {
            Output.printStoragePools(ecsInfo.getStoragePools());
        } else if (info_type.equalsIgnoreCase(ECSInfo.REPLICATION_GROUPS_INFO)) {
            Output.printReplicationGroups(ecsInfo.getReplicationGroups());
        } else if(info_type.equalsIgnoreCase(ECSInfo.NAMESPACES_INFO)) {
            Output.printNamespaces(ecsInfo.getNamespaces());
        } else if (info_type.equalsIgnoreCase(ECSInfo.BUCKETS_INFO)) {
            Output.printBuckets(ecsInfo.getBuckets());
        } else if(info_type.equalsIgnoreCase(ECSInfo.USERS_INFO)) {
            Output.printUsers(ecsInfo.getUsers());
        }
    }

    @Command
    public void listEcsConfig() {
        DynamicConfigResp resp = ecsInfo.listEcsConfig();
        Output.printConfig(resp);
    }

    @Command
    public void setEcsConfig(String name, String value) {
        ecsInfo.setEcsConfig(name, value);
    }

}
