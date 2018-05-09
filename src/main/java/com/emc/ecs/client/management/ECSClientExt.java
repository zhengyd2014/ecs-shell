package com.emc.ecs.client.management;

import com.emc.ecs.client.ECSClient;
import com.emc.ecs.client.impl.RestClient;

/**
 * Created by zhengf1 on 11/15/16.
 */
public class ECSClientExt extends ECSClient {

    public ECSClientExt(String host) {
        super(host);
    }

    public ECSClientExt(String host, boolean ignore, String mediaType) {
        super(host, ignore);
        if (mediaType == null || mediaType.toLowerCase().equals("xml")) {
            this.client.getConfig().setMediaType("application/xml");
        } else {
            this.client.getConfig().setMediaType("application/json");
        }
    }

    public void setMediaTypeToXml() {
        this.client.getConfig().setMediaType("application/xml");
    }

    public void setMediaTypeToJson() {
        this.client.getConfig().setMediaType("application/json");
    }

    public ECSConfig config() {
        return new ECSConfig(this.client);
    }
}
