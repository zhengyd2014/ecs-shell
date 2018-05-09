package com.emc.ecs.client.management;

import com.emc.storageos.data.datasvcmodels.user.BlobUser;

/**
 * Created by zhengf1 on 12/22/16.
 */
public class ECSUserInfo {

    private BlobUser blobUser;
    private String secretKey;

    public ECSUserInfo(BlobUser blobUser, String secretKey) {
        this.blobUser = blobUser;
        this.secretKey = secretKey;
    }

    public String getUserName() {
        return blobUser.getUser().toString();
    }

    public String getNamespace() {
        return blobUser.getNamespace().toString();
    }

    public String getSecretKey() {
        return secretKey;
    }
}
