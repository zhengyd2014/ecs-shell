package com.emc.ecs.util;

import com.emc.ecs.log.ChunkLifeCycleDistribution;
import org.junit.Test;

import java.util.Date;

/**
 * Created by zhengf1 on 2/3/17.
 */
public class TestTimeRange {

    private static long MINUTE = 60 * 1000;
    private static long HOUR = 60 * MINUTE;

    @Test
    public void testChunkLifeCycleDistribution() {
        long shortest = 24 * HOUR + 34 * MINUTE ;
        long longest = 25 * HOUR + 51 * MINUTE;
        long step = 120 * MINUTE;

        //shortest: 14771856, longest: 174862218
        shortest = 14771856; longest = 174862218;

        long start = TimeUtil.roundToHour(shortest);
        long end = TimeUtil.roundToHour(longest) + HOUR;
        int number = (int) ((end-start) / step) + 1 ;

        ChunkLifeCycleDistribution distribution = new ChunkLifeCycleDistribution(start,step,number);
        System.out.println(distribution);
    }
}
