package com.emc.ecs.client.management;

/**
 * Created by zhengf1 on 12/22/16.
 */
public class ECSBucketInfo {
    private String namespace;
    private String bucketName;
    private String replicationGroup;
    private String vdc;
    private long size;
    private long objectNumber;

    public ECSBucketInfo(String namespace, String bucketName, String replicationGroup, long size, long objectNumber) {
        this.namespace = namespace;
        this.bucketName = bucketName;
        this.objectNumber = objectNumber;
        this.replicationGroup = replicationGroup;
        this.size = size;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getBucketName() {
        return bucketName;
    }

    public long getObjectNumber() {
        return objectNumber;
    }

    public String getReplicationGroup() {
        return replicationGroup;
    }

    public long getSize() {
        return size;
    }
}
