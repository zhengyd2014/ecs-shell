package com.emc.ecs.log;

import asg.cliche.Command;
import asg.cliche.Param;
import com.emc.ecs.dtquery.Chunk;
import com.emc.ecs.dtquery.ChunkParser;
import com.emc.ecs.util.Output;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhengf1 on 1/12/17.
 */
public class LogHandler {

    LogParser logParser;

    public LogHandler() {
        logParser = new LogParser();
    }

    @Command (description = "parse log entries from specified file")
    public void parseLog(@Param(name="log file", description = "log file contains chunk life cycle event") String file) throws Exception {
        logParser.parseLogFile(file);
    }

    @Command (description = "parse all files under specified directory, which extension name have to be txt")
    public void parseDirctory(@Param(name="directory name", description = "directory where log file resides") String directory) throws Exception {
        logParser.parseDirectory(directory);
    }

    @Command (description = "save chunk life cycle objects to specified file")
    public void saveToFile(@Param(name="file name", description = "file to save life cycle objects") String file) throws Exception {
        logParser.saveToFile(file);
    }

    @Command (description = "read chunk life cycle objects from specified file")
    public void readFromFile(@Param(name = "file name", description = "file to read life cycle objects from") String file) throws Exception {
        logParser.readFromFile(file);
    }

    @Command (description = "list chunk life cycle events hourly")
    public void timeBucket() {
        TimeBucket timeBucket = logParser.getTimeBucket();
        Output.print(timeBucket);
    }

    @Command (description = "list chunk life cycle phase hourly")
    public void timeBucketWithPhase() throws Exception {
        TimeBucket timeBucket = logParser.getTimeBucketWithPhase();
        Output.print(timeBucket);
    }

    @Command (description = "retrieve 10 chunks by time and state")
    public void getChunkByTimeAndState(@Param(name = "time string", description = "format: yyyy-mm-ddThh") String time,
                                       @Param(name = "chunk state", description = "chunk state") String state) throws Exception {
        List<ChunkPhase> list = logParser.getTimeBucket().getChunkPhaseList(time);
        List<ChunkPhase> result = new ArrayList<>();

        for (ChunkPhase phase : list) {
            if (phase.state.equals(ChunkState.valueOf(state))) {
                result.add(phase);
            }
        }
        Output.print(result, 10);
    }

    @Command
    public void getChunkById(@Param(name="chunk id", description = "chunk id") String chunkId) {
        LogOutput.print(logParser.getChunkLifeCycleMap().get(chunkId));
    }

    @Command (description = "present chunk lifetime in a distributed way, interval by minutes")
    public void getLifetimeDistributionByInterval(@Param(name="interval", description = "interval by minutes") int interval) {
        long interval_in_millseconds = interval * 60 * 1000;
        System.out.println(logParser.getChunkLifetimeDistribution(interval_in_millseconds, null));
    }

    @Command (description = "present chunk phase in a distributed way, interval by minutes")
    public void getDistributionByInterval(@Param(name = "interval", description = "interval by minute") int interval, String state) {
        long interval_in_millseconds = interval * 60 * 1000;
        System.out.println(logParser.getChunkLifetimeDistribution(interval_in_millseconds, ChunkState.valueOf(state)));
    }

}
