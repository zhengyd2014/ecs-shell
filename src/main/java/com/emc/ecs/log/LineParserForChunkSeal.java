package com.emc.ecs.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 1/13/17.
 *
 * parse line like:
 * cm.log:2017-01-09T15:00:57,707 [TaskScheduler-ChunkManager-COMMUNICATOR-ParallelExecutor-012-0af58029:15971a39859:c3:1] DEBUG  ChunkActiveState.java (line 96) seal chu
 nk 8b338c50-e301-4a22-a42f-4b1ba35d9f40 length 134217600
 */
public class LineParserForChunkSeal implements LineParser {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

    @Override
    public LineParseResult parseLine(String line) throws Exception {
        if (!line.contains("ChunkActiveState.java (line 96) seal chunk")) {
            return null;
        }

        String[] tokens = line.split(" ");
        String chunkId = tokens[9];
        String timestamp = tokens[0].substring(tokens[0].indexOf(":") + 1);

        LineParseResult result= new LineParseResult();
        result.chunkId = chunkId;
        Date date = df.parse(timestamp);
        result.chunkPhase = new ChunkPhase(chunkId, ChunkState.Sealed, date.getTime());
        return result;
    }


    public static void main(String[] args) throws Exception {
        String line = "cm.log:2017-01-09T15:00:57,707 [TaskScheduler-ChunkManager-COMMUNICATOR-ParallelExecutor-012-0af58029:15971a39859:c3:1] DEBUG  ChunkActiveState.java (line 96) seal chunk 8b338c50-e301-4a22-a42f-4b1ba35d9f40 length 134217600";

        LineParser lineParser = new LineParserForChunkSeal();
        LineParseResult lineParseResult = lineParser.parseLine(line);
        System.out.println(lineParseResult.chunkId);
        System.out.println(lineParseResult.chunkPhase);
    }
}
