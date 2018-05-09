package com.emc.ecs.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 1/13/17.
 *
 * parse line like:
 * cm-chunk-reclaim.log:2017-01-09T15:18:43,953 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-282] DEBUG  RepoReclaimer.java (line 304) try to get progress for chunk:02c752df-8f31-4c2b-8f4b-2de49da21f4d, dt:urn:storageos:OwnershipInfo:251ed5a2-1d89-4a69-9622-7ca52db40182__CT_95_128_1:, rg:urn:storageos:ReplicationGroupInfo:e5bc3371-2b18-4b0d-b870-d8b4ee87f938:global
 */
public class LineParserForChunkGCCandidate implements LineParser {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

    @Override
    public LineParseResult parseLine(String line) throws Exception {
        if (!line.contains("RepoReclaimer.java (line 304) try to get progress for")) {
            return null;
        }

        String[] tokens = line.split(" ");
        String chunkId = tokens[12].substring(tokens[12].indexOf(":") + 1, tokens[12].length()-1);
        String timestamp = tokens[0].substring(tokens[0].indexOf(":") + 1);

        LineParseResult result= new LineParseResult();
        result.chunkId = chunkId;
        Date date = df.parse(timestamp);
        result.chunkPhase = new ChunkPhase(chunkId, ChunkState.GcCandidate, date.getTime());
        return result;
    }


    public static void main(String[] args) throws Exception {
        String line = "cm-chunk-reclaim.log:2017-01-09T15:18:43,953 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-282] DEBUG  RepoReclaimer.java (line 304) try to get progress for chunk:02c752df-8f31-4c2b-8f4b-2de49da21f4d, dt:urn:storageos:OwnershipInfo:251ed5a2-1d89-4a69-9622-7ca52db40182__CT_95_128_1:, rg:urn:storageos:ReplicationGroupInfo:e5bc3371-2b18-4b0d-b870-d8b4ee87f938:global";
        LineParser lineParser = new LineParserForChunkGCCandidate();
        LineParseResult lineParseResult = lineParser.parseLine(line);
        System.out.println(lineParseResult.chunkId);
        System.out.println(lineParseResult.chunkPhase);
    }
}
