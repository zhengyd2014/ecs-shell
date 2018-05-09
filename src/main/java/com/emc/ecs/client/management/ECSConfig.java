package com.emc.ecs.client.management;

import com.emc.ecs.client.impl.RestClient;
import com.emc.ecs.client.provision.impl.PathConstants;
import com.emc.storageos.data.datasvcmodels.ObjectNamedRelatedResourceRep;
import com.emc.storageos.objcontrol.service.impl.resource.dynamicconfig.model.DynamicConfigEntry;
import com.emc.storageos.objcontrol.service.impl.resource.dynamicconfig.model.DynamicConfigResp;
import com.emc.storageos.objcontrol.service.impl.resource.dynamicconfig.model.DynamicConfigUpdateReq;

import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhengf1 on 11/14/16.
 */
public class ECSConfig {

    private RestClient client;

    public ECSConfig(RestClient client) {
        this.client = client;
        client.getConfig().setMediaType("application/json");
    }

    public DynamicConfigResp getConfig() {
        DynamicConfigResp restRep = this.client.get(DynamicConfigResp.class, "/config");
        return restRep;
    }

    public void setConfig(String name, String value) {
        DynamicConfigUpdateReq req = new DynamicConfigUpdateReq();
        req.setValue(value);
        try {
            client.put(Response.class, req, "/config/{vdcName}", new Object[]{name});
        } catch (Exception ex) {
            // do nothing
        }
    }



    public Object getStatCluster(int hours) {
        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.HOUR, -2);
        long end = cal.getTime().getTime()/1000;
        cal.add(Calendar.HOUR, 0 - hours);
        long start = cal.getTime().getTime()/1000;

        String cluster_url = PathConstants.CLUSTER_STATS_URL;

        String cluster_traffic_url = PathConstants.CLUSTER_STATS_URL
                + "?category=traffic";

        String historical_parameters = "dataType=historical&startTime="
                + start + "&endTime="
                + end +"&interval=3600";
        Object result = null;
        try {
            //result = client.getJsonWithQueryParams(Object.class, cluster_url, historical_parameters);
            result = client.getJson(Object.class, cluster_url);
        } catch (Exception ex) {
            // do nothing
        }

        return result;
    }

    public Object getJson(String url) {
        return client.getJson(Object.class,url);
    }

    public Object getJsonWithQueryParams(String url, String parameters) {
        try {
            return client.getJsonWithQueryParams(Object.class, url, parameters);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return null;
    }

    public Object getObjectNumber(String namespace, String bucket) {
        String url = "";

        return null;
    }
}
