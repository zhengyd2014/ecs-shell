package com.emc.ecs.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 1/13/17.
 *
 * parse line like:
 * cm-chunk-reclaim.log.20170106-143858.gz:2017-01-06T14:31:16,150 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-235] DEBUG  RepoReclaimer.java (line 465) chunk 671abdca-43f4-4b89-9c62-14637bdc233b verification status is status: SCHEDULED ; verified: 0xNULL ; failedRangeBit: 0xNULL, reclaim skipped
 *
 */
public class LineParserForChunkVerificationTaskCreated implements LineParser {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

    @Override
    public LineParseResult parseLine(String line) throws Exception {
        if (!line.contains("verification status is status: SCHEDULED")) {
            return null;
        }

        String[] tokens = line.split(" ");
        String chunkId = tokens[8];
        String timestamp = tokens[0].substring(tokens[0].indexOf(":") + 1);

        LineParseResult result= new LineParseResult();
        result.chunkId = chunkId;
        Date date = df.parse(timestamp);
        result.chunkPhase = new ChunkPhase(chunkId, ChunkState.VerificationTaskCreated, date.getTime());
        return result;
    }


    public static void main(String[] args) throws Exception {
        String line = "cm-chunk-reclaim.log.20170106-143858.gz:2017-01-06T14:31:16,150 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-235] DEBUG  RepoReclaimer.java (line 465) chunk 671abdca-43f4-4b89-9c62-14637bdc233b verification status is status: SCHEDULED ; verified: 0xNULL ; failedRangeBit: 0xNULL, reclaim skipped";
        LineParser lineParser = new LineParserForChunkVerificationTaskCreated();
        LineParseResult lineParseResult = lineParser.parseLine(line);
        System.out.println(lineParseResult.chunkId);
        System.out.println(lineParseResult.chunkPhase);
    }
}
