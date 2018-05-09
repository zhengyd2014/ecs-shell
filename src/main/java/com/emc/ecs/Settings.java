package com.emc.ecs;

import asg.cliche.Command;
import asg.cliche.ConsoleIO;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternSelector;

import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by zhengf1 on 11/9/16.
 */
public class Settings {

    private static Settings instance;

    private Properties conf = new Properties() {{
        put("ECS_MANAGEMENT_USER", "emcservice");
        put("USER", "root");
        put("SSH_USER", "core");
        put("PASSWORD", "ChangeMe");
    }};

    private Settings() {}

    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public Properties getProperties() {
        return conf;
    }

    @Command
    public  void setConfig(String key, String value) {
        conf.put(key, value);
    }

    public  String getConfig(String key) {
        return (String)conf.get(key);
    }

    @Command
    public void listConfig() {
        for (Map.Entry entry : conf.entrySet() ) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    @Command
    public void setLogLevel(String level) {

    }


    public String parseVariables(String input) {

        String temp = input;
        int aStartIndexOld = 0;

        while (input.indexOf("}", aStartIndexOld) != -1) {
            int end = input.indexOf("}", aStartIndexOld);
            int begin = input.substring(0, end).lastIndexOf("${");
            if (begin == -1) {
                //throw new ParseException("bad input for closing ${ }");
                break;
            }

            String value = null;
            String name = input.substring(begin + 2, end);

            // firstly, check if it's local variables
            if (conf.getProperty(name.toUpperCase()) != null) {
                value = conf.getProperty(name.toUpperCase());
            }


            //
            // translate SIZE to long, for example: 1K -> 1024
            //
            if (value == null && name.toUpperCase().endsWith("K")) {
                try {
                    float fValue = Float.parseFloat(name.substring(0, name.length() - 1));
                    fValue = fValue * 1024;
                    long lValue = (long) fValue;
                    value = String.valueOf(lValue);
                } catch (NumberFormatException nfe) { /* do nothing */ }
            }

            if (value == null && name.toUpperCase().endsWith("M")) {
                try {
                    float fValue = Float.parseFloat(name.substring(0, name.length() - 1));
                    fValue = fValue * 1024 * 1024;
                    long iValue = (long) fValue;
                    value = String.valueOf(iValue);
                } catch (NumberFormatException nfe) { /* do nothing */ }
            }

            if (value == null && name.toUpperCase().endsWith("G")) {
                try {
                    float fValue = Float.parseFloat(name.substring(0, name.length() - 1));
                    fValue = fValue * 1024 * 1024 * 1024;
                    long iValue = (long) fValue;
                    value = String.valueOf(iValue);
                } catch (NumberFormatException nfe) { /* do nothing */ }
            }

            /*
            if (value == null || value.trim().length() == 0) {
                value = removeQuotations(Constants.Variables.getProperty(name.toUpperCase(), ""));

                if (value.equals("")) {
                    if (name.toLowerCase().startsWith("random")) {
                        Random rand = new Random();
                        value = String.valueOf(Math.abs(rand.nextInt()));
                        Constants.Variables.put(name.toUpperCase(), value);
                        //                       return value;
                    }

                    if (name.equalsIgnoreCase("REAL_RANDOM")) {
                        Random rand = new Random();
                        value = String.valueOf(Math.abs(rand.nextInt()));
                    }
                }
            }*/

            if (value == null || value.equals("")) {
                //throw new ParseException(name + " don't have a value.");
                printMessage(name + " don't have a value");
                return "";
            }

            temp = input.substring(0, begin) + value + input.substring(end + 1);
            input = temp;
        }

        return input;
    }


    public static String removeQuotations(String input) {
        if (input == null)
            return input;

        if ((input.length() >= 2) &&
                (input.startsWith("\"")) &&
                (input.endsWith("\""))) {
            input = input.substring(1, input.length() - 1);
        }

        return input;

    }

    public static void printMessage(String str) {
        System.out.println("\n" + str);
    }


    public static void configLoggerTest() throws Exception{
        Logger root = Logger.getRootLogger();
        root.addAppender(new org.apache.log4j.FileAppender(
                new org.apache.log4j.PatternLayout(org.apache.log4j.PatternLayout.TTCC_CONVERSION_PATTERN), "ecs-shell.log"));
    }

    public static void configLogger() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        Layout layout = PatternLayout.createDefaultLayout();
        Appender appender = FileAppender.createAppender("target/test.log", "false", "false", "File", "true",
                "false", "false", "4000", layout, null, "false", null, config);
        appender.start();
        config.addAppender(appender);
        AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};
        LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.ALL, "com.emc.ecs.client",
                "true", refs, null, config, null );
        loggerConfig.addAppender(appender, null, null);
        config.addLogger("com.emc.ecs.client", loggerConfig);
        ctx.updateLoggers();
    }
}
