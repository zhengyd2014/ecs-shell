package com.emc.ecs.util;

import com.emc.ecs.log.ChunkLifeCycle;
import com.emc.ecs.log.ChunkPhase;
import com.emc.ecs.log.ChunkState;
import com.emc.ecs.log.LogParser;
import com.emc.ecs.log.TimeBucket;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Created by zhengf1 on 2/6/17.
 */
public class TestLogParser {

    LogParser logParser = new LogParser();

    @Before
    public void loadData() throws Exception {
        logParser.parseLogFile("life-cycle-test-data.txt");
    }

    @Test
    public void testParseFile() throws Exception {
        System.out.println(logParser);
    }

    @Test
    public void testGetChunkPhaseime() throws Exception {
        for (ChunkState state : ChunkState.values()) {
            for (String key : logParser.getChunkLifeCycleMap().keySet()) {
                System.out.println("Id: " + key + ", " + state + ": "
                        + StringUtil.readableTime(
                        logParser.getChunkLifeCycleMap().get(key).getPhaseTime(state)));
            }
        }
    }

    @Test
    public void testGetChunkLifetime() throws Exception {
        for (String key : logParser.getChunkLifeCycleMap().keySet()) {
            System.out.println("Id: " + key + ", life time: "
                    + StringUtil.readableTime(
                    logParser.getChunkLifeCycleMap().get(key).getPhaseTime(null)));
        }
    }

    @Test
    public void testGetTimeBucket() throws Exception{
        Output.print(logParser.getTimeBucket());
        Output.print(logParser.getTimeBucketWithPhase());
    }



    @Test
    public void testTreeMapOfChunkLifeCycle() {
        String chunkId = "testid";
        long timeStamp = new Date().getTime();

        ChunkLifeCycle chunkLifeCycle = new ChunkLifeCycle(chunkId);
        chunkLifeCycle.addChunkPhase(new ChunkPhase(chunkId, ChunkState.ChunkFreed, timeStamp + Constants.ONE_HOUR_IN_MILLISECONDS));
        chunkLifeCycle.addChunkPhase(new ChunkPhase(chunkId, ChunkState.CleanupJobDone, timeStamp));
        chunkLifeCycle.addChunkPhase(new ChunkPhase(chunkId, ChunkState.CleanupJobStart, timeStamp - Constants.ONE_HOUR_IN_MILLISECONDS));
        System.out.println(chunkLifeCycle);

        System.out.println(ChunkState.ChunkFreed.compareTo(ChunkState.CleanupJobDone));
        System.out.println(ChunkState.VerificationTaskDone.compareTo(ChunkState.CleanupJobDone));
    }

}
