package com.emc.ecs.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 1/13/17.
 *
 * parse line like:
 * cm.log.20170106-051158.gz:2017-01-06T05:11:49,009 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-002]  INFO  ChunkDeletingState.java (line 185) Copies of chunk 123d7657-819d-416b-9184-c073d894c2e8 are freed, last chunk info with copies: status: DELETING
 *
 */
public class LineParserForChunkFreed implements LineParser {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

    @Override
    public LineParseResult parseLine(String line) throws Exception {
        if (!line.contains("are freed, last chunk info with copies: status: DELETING")) {
            return null;
        } else if (line.contains("REMOTE")) {
            return null;
        }

        String[] tokens = line.split(" ");
        String chunkId = tokens[11];
        String timestamp = tokens[0].substring(tokens[0].indexOf(":") + 1);

        LineParseResult result= new LineParseResult();
        result.chunkId = chunkId;
        Date date = df.parse(timestamp);
        result.chunkPhase = new ChunkPhase(chunkId, ChunkState.ChunkFreed, date.getTime());
        return result;
    }


    public static void main(String[] args) throws Exception {
        String line = "cm.log.20170106-051158.gz:2017-01-06T05:11:49,009 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-002]  INFO  ChunkDeletingState.java (line 185) Copies of chunk 123d7657-819d-416b-9184-c073d894c2e8 are freed, last chunk info with copies: status: DELETING";
        LineParser lineParser = new LineParserForChunkFreed();
        LineParseResult lineParseResult = lineParser.parseLine(line);
        System.out.println(lineParseResult.chunkId);
        System.out.println(lineParseResult.chunkPhase);
    }
}
