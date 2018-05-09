package com.emc.ecs.util;

/**
 * Created by zhengf1 on 2/3/17.
 */
public class TimeUtil {

    /**
     *
     *
     * @param timestamp
     * @return
     */
    public static long roundToHour(long timestamp) {
        long seconds = timestamp / 1000;
        return (seconds - (seconds % 3600)) * 1000;
    }
}
