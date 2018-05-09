package com.emc.ecs;

import asg.cliche.Command;
import asg.cliche.Param;
import com.emc.ecs.util.Ssh2Exec;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

/**
 * Created by zhengf1 on 1/4/17.
 */
public class SharedCommands {

    private Properties env;

    public SharedCommands(Properties env) {
        this.env = env;
    }

    @Command
    public void echo(String... commands) {
        for (String command : commands) {
            System.out.println(command);
        }
    }

    @Command (description = "sleep for specific seconds")
    public void sleep(@Param(name="seconds", description="number of seconds") int number) throws Exception {
        System.out.println("sleeping for " + number + " seconds");
        Thread.sleep(number * 1000);
    }

    @Command
    public void ssh(String server, String username, String password, String shellcommand) throws Exception {
        Ssh2Exec shell = new Ssh2Exec(server, username, password);
        shell.execCmd(shellcommand);
    }

    @Command
    public void ssh(String shellcommand) throws Exception {
        if (StringUtils.isEmpty(env.getProperty("ECS_IP"))) {
            System.out.println("need to connect to ecs first");
            return;
        }


        Ssh2Exec shell = new Ssh2Exec(
                env.getProperty("ECS_IP"),
                env.getProperty("SSH_USER"),
                env.getProperty("PASSWORD"));
        shell.execCmd(shellcommand);
    }

    @Command
    public void uploadFile(String localFile, String remoteFile) throws Exception {
        if (StringUtils.isEmpty(env.getProperty("ECS_IP"))) {
            System.out.println("need to connect to ecs first");
            return;
        }

        Ssh2Exec shell = new Ssh2Exec(
                env.getProperty("ECS_IP"),
                env.getProperty("SSH_USER"),
                env.getProperty("PASSWORD"));
        shell.uploadFile(localFile, remoteFile);
    }
}
