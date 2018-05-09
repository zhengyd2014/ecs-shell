package com.emc.ecs.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 1/13/17.
 *
 * parse line like:
 * cm-chunk-reclaim.log:2017-01-09T15:18:43,978 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-171] DEBUG  RepoReclaimer.java (line 352) CheckReclaimable for chunk e712a8b2-0e0e-4f0b-9ebd-5d8157b97926: fullChunkReclaimable true, garbageRangeBit -1, repoUsageSize 0
 */
public class LineParserForChunkCleanupJobDone implements LineParser {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

    @Override
    public LineParseResult parseLine(String line) throws Exception {
        if (!line.contains("fullChunkReclaimable true, garbageRangeBit -1, repoUsageSize 0")) {
            return null;
        }

        String[] tokens = line.split(" ");
        String chunkId = tokens[10].substring(0,tokens[10].length()-1);
        String timestamp = tokens[0].substring(tokens[0].indexOf(":") + 1);

        LineParseResult result= new LineParseResult();
        result.chunkId = chunkId;
        Date date = df.parse(timestamp);
        result.chunkPhase = new ChunkPhase(chunkId, ChunkState.CleanupJobDone, date.getTime());
        return result;
    }


    public static void main(String[] args) throws Exception {
        String line = "cm-chunk-reclaim.log:2017-01-09T15:18:43,978 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-171] DEBUG  RepoReclaimer.java (line 352) CheckReclaimable for chunk e712a8b2-0e0e-4f0b-9ebd-5d8157b97926: fullChunkReclaimable true, garbageRangeBit -1, repoUsageSize 0";
        LineParser lineParser = new LineParserForChunkCleanupJobDone();
        LineParseResult lineParseResult = lineParser.parseLine(line);
        System.out.println(lineParseResult.chunkId);
        System.out.println(lineParseResult.chunkPhase);
    }
}
