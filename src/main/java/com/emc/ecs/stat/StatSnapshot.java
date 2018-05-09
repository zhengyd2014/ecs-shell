package com.emc.ecs.stat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 12/19/16.
 */
public class StatSnapshot {

    private String value;
    private long timestamp;

    public static long ONE_HOUR = 60 * 60 * 1000; // 1 hour of milliseconds

    public StatSnapshot(String value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getValue() {
        return this.value;
    }

    public String getTimeByFormat(SimpleDateFormat format) {
        Date d = new Date(timestamp);
        return format.format(d);
    }
}
