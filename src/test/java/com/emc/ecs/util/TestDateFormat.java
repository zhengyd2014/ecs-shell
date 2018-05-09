package com.emc.ecs.util;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by zhengf1 on 12/13/16.
 */
public class TestDateFormat {


    public static void  main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH");
        long startHour = 1483752060321L;
        long endHour = 1484012234903L;
        for (long i=startHour; i <= endHour; i += 3600 * 1000) {
            System.out.println(df.format(new Date(i)));
        }
    }

    public static void testDataFormat() {
        String dateStr = "1481662800";
        System.out.println(dateStr);


        long lDate = Long.parseLong(dateStr);
        System.out.println(lDate);
        Date d = new Date( lDate* 1000);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.out.println(sdfDate.format(d));
    }


}
