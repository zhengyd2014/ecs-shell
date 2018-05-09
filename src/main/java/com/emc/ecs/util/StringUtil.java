package com.emc.ecs.util;

import org.apache.commons.lang.time.DurationFormatUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by zhengf1 on 10/24/16.
 */
public class StringUtil {

    public static String readableSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB"};
        //final String[] units = new String[] { "B", "kB", "MB", "GB", "TB", "PB"};
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));

        String format = "#,##0.###";
        if (digitGroups >= units.length) {
            return new DecimalFormat(format).format(size / Math.pow(1024, units.length - 1)) + " " + units[units.length -1];
        } else {
            return new DecimalFormat(format).format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }
    }

    public static String readableTime(long time) {
        return DurationFormatUtils.formatDurationHMS(time);
    }


    public static long toGB(String size) {
        String[] parts = size.split(" ");

        long number = 0;
        if (parts.length > 2) {
            return 0;
        } else if (parts.length == 2) {
            number = Long.parseLong(parts[0]) * unitToNumber(parts[1]);
        }

        return number;
    }

    private static long unitToNumber(String unit) {
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB", "PB"};
        long number = 1;
        boolean bFound = false;
        for (int i=0; i<units.length; i++ ) {
            if (unit.toUpperCase().equals(units[i])) {
                number = number * (long)Math.pow(1024,i);
                bFound = true;
                break;
            }
        }

        if (bFound) {
            return number;
        } else {
            return 0;
        }
    }


    public static String readableSize(String size) {
        String bytes = new BigDecimal(size).toPlainString();
        bytes = bytes.split("\\.")[0];
        return readableSize(Long.parseLong(bytes));
    }

    public static void main(String[] args) {
        String size = "2.1629572147333333E8";
        System.out.println(readableSize(size));
    }
}
