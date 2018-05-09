package com.emc.ecs;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import asg.cliche.ShellFactory;
import asg.cliche.util.ArrayHashMultiMap;
import asg.cliche.util.MultiMap;
import com.emc.ecs.client.management.ECSInfo;
import com.emc.ecs.client.management.ECSInfoHandler;
import com.emc.ecs.client.management.ECSUserInfo;
import com.emc.ecs.dtquery.DtQueryHandler;
import com.emc.ecs.log.LogHandler;
import com.emc.ecs.s3.S3Handler;
import com.emc.ecs.stat.StatHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * Created by zhengf1 on 10/31/16.
 */
public class MainShell implements ShellDependent {

    private static final Logger logger = LogManager.getLogger(MainShell.class);
    private Shell shell;

    private Properties env = Settings.getInstance().getProperties();
    private static String ECS_IP = "ECS_IP";

    // to get the shell field set
    public void cliSetShell(Shell shell) {
        this.shell = shell;
    }

    @Command (description="load chunk tables from specified ecs node")
    public void chunktable() throws Exception {
        if (!checkConnected()) {
            return;
        }

        DtQueryHandler dtQueryHandler = new DtQueryHandler();
        dtQueryHandler.chunktable(env.getProperty(ECS_IP));

        ShellFactory.createSubshell("dt_query", shell, "chunk analysis tool", dtQueryHandler)
                .commandLoop();
    }

    @Command (description="load chunk tables from specified ecs node")
    public void loadchunkfromfile(@Param(name="file", description="chunk dump file") String chunk_file) throws Exception {
        DtQueryHandler dtQueryHandler = new DtQueryHandler();
        dtQueryHandler.fromfile(chunk_file);

        ShellFactory.createSubshell("dt_query", shell, "chunk analysis tool", dtQueryHandler)
                .commandLoop();
    }

    @Command (description = "ecs management client")
    public void management() throws Exception {
        if (!checkConnected()) {
            return;
        }

        ECSInfoHandler handler = new ECSInfoHandler(env.getProperty(ECS_IP));
        ShellFactory.createSubshell("ecs_manage", shell, "ecs management", handler)
                .commandLoop();
    }

    @Command (description = "ecs stat framework")
    public void stat() throws Exception {
        if (!checkConnected()) {
            return;
        }

        StatHandler statHandler = new StatHandler(env.getProperty(ECS_IP));
        ShellFactory.createSubshell("ecs_stat", shell, "ecs stat display", statHandler)
                .commandLoop();
    }

    @Command (description = "ecs log file analyse")
    public void logAnalyze() throws Exception {

        LogHandler logHandler = new LogHandler();
        ShellFactory.createSubshell("log_analyzer", shell, "log analyzer", logHandler)
                .commandLoop();
    }


    @Command (description = "s3 client")
    public void s3handler() throws Exception {
        if (!checkConnected()) {
            return;
        }

        String ecs_endpoint = env.getProperty(ECS_IP);
        ECSInfo ecsInfo = new ECSInfo(ecs_endpoint);
        ECSUserInfo user = ecsInfo.getUsers().get(0);
        String secret_key = user.getSecretKey();
        String userName = user.getUserName();

        S3Handler s3Handler = new S3Handler(userName, secret_key, ecs_endpoint);
        ShellFactory.createSubshell("s3_client", shell, "s3 client", s3Handler)
                .commandLoop();
    }


    @Command (description = "disconnect with current ecs")
    public void connect(String ecs_ip) {
        env.setProperty(ECS_IP, ecs_ip);
        shell.getPath().set(0, "ecs_shell_" + ecs_ip);
    }

    @Command (description = "disconnect with current ecs")
    public void disconnect() {
        env.remove(ECS_IP);
        shell.getPath().set(0,"ecs_shell");
    }

    /**
     * check if ECS endpoint is set
     * @return
     */
    private boolean checkConnected() {
        if (StringUtils.isEmpty(env.getProperty(ECS_IP))) {
            System.out.println("not connected, please use 'connect <ecs>' command first.");
            return false;
        }

        return true;
    }


    public static void main(String[] args) throws Exception {
        //BasicConfigurator.configure();
        Settings.configLoggerTest();

        logger.debug("debug: start main");
        logger.info("info: start main");

        MultiMap<String, Object> auxHandlers = new ArrayHashMultiMap<String, Object>();
        auxHandlers.put("#", new SimulatorHandler());
        auxHandlers.put("!", Settings.getInstance());
        auxHandlers.put("!", new SharedCommands(Settings.getInstance().getProperties()));

        // handle input arguments
        String commandFile = null;
        if (args.length != 0) {
            for (String arg : args) {
                if (arg.toLowerCase().equals("-h")) {
                    System.out.println(getHelpMessage());
                } else if (arg.startsWith("-D")) {
                    String[] parameter = arg.substring(2).split("=");
                    System.out.println(parameter[0] + " = " + parameter[1]);
                    Settings.getInstance().setConfig(parameter[0], parameter[1]);
                } else if (commandFile == null){
                    commandFile = arg;
                }

            }
        }

        // run command loop
        MainShell mainShell = new MainShell();
        Shell shell = ShellFactory.createConsoleShell("ecs_shell", "The Ecs Shell at version 1.0.1 \n" +
                "Enter ?l to list available commands.", mainShell, auxHandlers);
        shell.setBatchFile(commandFile);
        shell.commandLoop();
    }


    private static String getHelpMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append("NAME\n");
        sb.append("        ecs_shell - ecs interactive shell\n");
        sb.append("\n");
        sb.append("SYNOPSIS\n");
        sb.append("        ecs_shell [ options ] [ batch_file ]\n");
        sb.append("\n");
        sb.append("OPTIONS\n");
        sb.append("        options format as '-D<name>=<value>.'\n");
        sb.append("\n");
        sb.append("   Standard Options\n");
        sb.append("         ECS_ENDPOINT              ecs node to connect\n");
        sb.append("         ECS_MANAGEMENT_USER       user to authenticate with ecs\n");
        sb.append("                                   default as 'emcservice'\n");
        sb.append("         ECS_MANAGEMENT_PASSWORD   password to authenticate with ecs\n");
        sb.append("                                   default as 'ChangeMe'\n");
        sb.append("         OUTPUT_MODE               default as 'ChangeMe'\n");

        return sb.toString();
    }

}
