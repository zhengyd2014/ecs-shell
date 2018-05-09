package com.emc.ecs.s3;

import asg.cliche.Command;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by zhengf1 on 11/22/16.
 */
public class S3Handler {

    private String user;
    private String password;
    private String ecs;

    final AmazonS3 s3;

    public S3Handler(String user, String password, String ecs) {
        this.user = user;
        this.password = password;
        this.ecs = ecs;
        AWSCredentials credentials = new BasicAWSCredentials(user, password);
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setProtocol(Protocol.HTTP);
        s3 = new AmazonS3Client(credentials, configuration);
        s3.setEndpoint(ecs + ":9020");
        //s3.setEndpoint("10.32.172.184:9020");
        s3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());
    }

    @Command
    public void listBuckets() {
        List<Bucket> buckets = s3.listBuckets();
        S3Output.printBuckets(buckets);
    }

    @Command
    public void listObjects(String bucketName) {
        ObjectListing objectListing = s3.listObjects(bucketName);
        List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();
        S3Output.printObjectSummaries(s3ObjectSummaries);
    }

    @Command
    public void createObject(String bucketName, String file) {
        File objectFile = new File(file);
        s3.putObject(bucketName, objectFile.getName(), objectFile);
        System.out.println("create object success");
    }

    @Command
    public void getObject(String bucket, String key) throws Exception{
        S3Object s3object = s3.getObject(new GetObjectRequest(
                bucket, key));
        System.out.println("Content-Type: "  +
                s3object.getObjectMetadata().getContentType());
        displayTextInputStream(s3object.getObjectContent());
    }

    private static void displayTextInputStream(InputStream input)
            throws IOException {
        // Read one text line at a time and display.
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }
}
