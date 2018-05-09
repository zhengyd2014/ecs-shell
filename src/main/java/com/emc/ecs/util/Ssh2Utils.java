package com.emc.ecs.util;

/**
 * Created by zhengf1 on 1/13/17.
 */
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3FileAttributes;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

public class Ssh2Utils {
    public Connection connect;
    public SCPClient scpClient;
    public SFTPv3Client sftPv3Client;

    public Ssh2Utils(String host, String user, String passwd) throws IOException{
        connect = new Connection(host);
        connect.connect();

        boolean isAuthenticated = connect.authenticateWithPassword(user, passwd);

        if (isAuthenticated == false){
            throw new IOException("Authentication failed");
        }else {
            scpClient = connect.createSCPClient();
            sftPv3Client = new SFTPv3Client(connect);
        }
    }

    public void getDir(String remoteDirectory, String localDirectory) throws IOException {
        SFTPv3FileAttributes sftPv3FileAttributes = null;
        Vector<SFTPv3DirectoryEntry> fileList = null;
        try{
            fileList = sftPv3Client.ls(remoteDirectory);
        } catch (Exception e){
            System.out.println("Error to handle " + remoteDirectory + " : " + e.getMessage());
            return;
        }
        Iterator<SFTPv3DirectoryEntry> iterator = fileList.iterator();
        SFTPv3DirectoryEntry sftPv3DirectoryEntry;
        String file;
        File tmpDir;

        while (iterator.hasNext()) {
            sftPv3DirectoryEntry = iterator.next();
            file = sftPv3DirectoryEntry.filename;
            if (!file.equalsIgnoreCase(".") && !file.equalsIgnoreCase("..")) {
                final String fullFileName = remoteDirectory + "/" + file;
                try{
                    sftPv3FileAttributes = sftPv3Client.stat(fullFileName);
                    if (sftPv3FileAttributes.isDirectory()) {
                        final String subDir = localDirectory + "/" + file;
                        tmpDir = new File(subDir);
                        if (!tmpDir.exists()){
                            if (!tmpDir.mkdirs()){
                                System.out.println("Create directory failed! " + subDir);
                            }
                        }
                        getDir(fullFileName, subDir);
                    }
                    else {
                        scpClient.get(fullFileName, localDirectory);
                    }
                } catch (Exception e){
                    System.out.println("Error to handle " + fullFileName + " : " + e.getMessage());
                }
            }
        }
    }
}