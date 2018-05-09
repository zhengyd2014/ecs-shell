package com.emc.ecs.util;

/**
 * Created by zhengf1 on 1/13/17.
 */
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ssh2Exec {
    private String host;
    private String user;
    private String password;
    private int bufferSzie = 1024;

    private static Logger logger = LoggerFactory.getLogger(Ssh2Exec.class);

    public Ssh2Exec(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setBufferSzie(int bufferSzie) {
        this.bufferSzie = bufferSzie;
    }

    public void execCmd(String cmd) throws Exception {

        StringBuffer sb = new StringBuffer();

        Connection conn = getConnection();

        Session sess = conn.openSession();
        sess.execCommand(cmd);

        InputStream stdout = new StreamGobbler(sess.getStdout());
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            sb.append(line + "\n");
        }

        InputStream stderr = new StreamGobbler(sess.getStderr());
        br = new BufferedReader(new InputStreamReader(stderr));
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            sb.append(line);
        }

        if (sess != null && sess.getExitStatus() != null) {
            System.out.println("status code: " + sess.getExitStatus());
            sess.close();
        }

        if (conn != null)
            conn.close();

        System.out.println(sb.toString());
    }

    public void uploadFile(String localFile, String remoteFile) throws IOException
    {
        logger.debug("upload local file '" + localFile + "' to remote file '" + remoteFile + "'");
        System.out.println("local file: " + new File(localFile).getAbsolutePath());
        Connection conn = getConnection();
        SCPClient scp = conn.createSCPClient();
        try {
            // split remote file in directory
            if (remoteFile.contains("/")) {
                String dir = remoteFile.substring(0, remoteFile.lastIndexOf("/"));
                String file = remoteFile.substring(remoteFile.lastIndexOf("/") + 1);
                scp.put(localFile, file, dir, "0600");
            } else {
                scp.put(localFile, remoteFile, "", "0600");
            }
        } finally {

        }

        logger.debug("upload file complete");
    }


    private Connection getConnection() throws IOException {
        Connection conn = new Connection(host);
        conn.connect();

        logger.debug("authenticate with User = " + user + ", password = " + password);
        boolean isAuthenticated = conn.authenticateWithPassword(user, password);

        if (isAuthenticated == false)
            throw new IOException("Authentication failed");

        return conn;
    }



    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Ssh2Exec[host:").append(host).append(", user:").append(user)
                .append(", password:").append(password).append("]");
        return sb.toString();
    }

}
