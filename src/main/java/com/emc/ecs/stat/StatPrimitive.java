package com.emc.ecs.stat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 12/2/16.
 */
public class StatPrimitive {

    private String id;
    private String timestamp;
    private long counter;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public String getTimestampStr() {
        String s = new SimpleDateFormat("yyyy-HH-dd HH:mm:ss").format(new Date(Long.parseLong(timestamp)));
        return s;
    }
}
