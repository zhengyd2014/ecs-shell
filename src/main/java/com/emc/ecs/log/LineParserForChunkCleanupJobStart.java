package com.emc.ecs.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 1/13/17.
 *
 * parse line like:
 * cm-chunk-reclaim.log:2017-01-09T15:18:43,955 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-282] DEBUG  RepoReclaimer.java (line 334) has collected all references for repo 02ecd383-c2e2-493c-a425-af860f9e6fe4 , isAlignmentChunk true, isGCReady true
 */
public class LineParserForChunkCleanupJobStart implements LineParser {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

    @Override
    public LineParseResult parseLine(String line) throws Exception {
        if (!line.contains("has collected all references for repo")) {
            return null;
        }

        String[] tokens = line.split(" ");
        String chunkId = tokens[13];
        String timestamp = tokens[0].substring(tokens[0].indexOf(":") + 1);

        LineParseResult result= new LineParseResult();
        result.chunkId = chunkId;
        Date date = df.parse(timestamp);
        result.chunkPhase = new ChunkPhase(chunkId, ChunkState.CleanupJobStart, date.getTime());
        return result;
    }


    public static void main(String[] args) throws Exception {
        String line = "cm-chunk-reclaim.log:2017-01-09T15:18:43,955 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-282] DEBUG  RepoReclaimer.java (line 334) has collected all references for repo 02ecd383-c2e2-493c-a425-af860f9e6fe4 , isAlignmentChunk true, isGCReady true";
        LineParser lineParser = new LineParserForChunkCleanupJobStart();
        LineParseResult lineParseResult = lineParser.parseLine(line);
        System.out.println(lineParseResult.chunkId);
        System.out.println(lineParseResult.chunkPhase);
    }
}
