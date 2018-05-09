package com.emc.ecs.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 1/13/17.
 *
 * it was used to paring line like below:
 * cm.log:2017-01-09T14:59:36,900 [TaskScheduler-ChunkManager-COMMUNICATOR-ParallelExecutor-435-0af5802c:15971a3f410:de:1] DEBUG  ChunkServer.java (line 790) created chunk c2d27774-b979-4f3f-a171-e16276ff59f8 status: ACTIVE
 */
public class LineParserForChunkCreate implements LineParser {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

    @Override
    public LineParseResult parseLine(String line) throws Exception {
        if (!line.contains("ChunkServer.java (line 790) created chunk")) {
            return null;
        }

        String[] tokens = line.split(" ");
        String chunkId = tokens[9];
        String timestamp = tokens[0].substring(tokens[0].indexOf(":") + 1);

        LineParseResult result= new LineParseResult();
        result.chunkId = chunkId;
        Date date = df.parse(timestamp);
        result.chunkPhase = new ChunkPhase(chunkId, ChunkState.Created, date.getTime());
        return result;
    }


    public static void main(String[] args) throws Exception {
        String line = "cm.log:2017-01-09T14:59:36,900 [TaskScheduler-ChunkManager-COMMUNICATOR-ParallelExecutor-435-0af5802c:15971a3f410:de:1] DEBUG  ChunkServer.java (line 790) created chunk c2d27774-b979-4f3f-a171-e16276ff59f8 status: ACTIVE";

        LineParser lineParser = new LineParserForChunkCreate();
        LineParseResult lineParseResult = lineParser.parseLine(line);
        System.out.println(lineParseResult.chunkId);
        System.out.println(lineParseResult.chunkPhase);
    }
}
