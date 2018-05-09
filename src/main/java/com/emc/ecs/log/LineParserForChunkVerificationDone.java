package com.emc.ecs.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 1/13/17.
 *
 * parse line like:
 * cm-chunk-reclaim.log.20170106-160149.gz:2017-01-06T15:50:11,998 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-220]INFO  RepoReclaimer.java (line 478) chunk b0e7d1e3-f1c7-4712-9b50-9cc36d5eba48 gc verification is complete, deleting chunk *
 */
public class LineParserForChunkVerificationDone implements LineParser {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

    @Override
    public LineParseResult parseLine(String line) throws Exception {
        if (!line.contains("gc verification is complete, deleting chunk")) {
            return null;
        }

        String[] tokens = line.split(" ");
        String chunkId = tokens[9];
        String timestamp = tokens[0].substring(tokens[0].indexOf(":") + 1);

        LineParseResult result= new LineParseResult();
        result.chunkId = chunkId;
        Date date = df.parse(timestamp);
        result.chunkPhase = new ChunkPhase(chunkId, ChunkState.VerificationTaskDone, date.getTime());
        return result;
    }


    public static void main(String[] args) throws Exception {
        String line = "cm-chunk-reclaim.log.20170106-160149.gz:2017-01-06T15:50:11,998 [TaskScheduler-ChunkManager-DEFAULT_BACKGROUND_OPERATION-ScheduledExecutor-220]INFO  RepoReclaimer.java (line 478) chunk b0e7d1e3-f1c7-4712-9b50-9cc36d5eba48 gc verification is complete, deleting chunk";
        LineParser lineParser = new LineParserForChunkVerificationDone();
        LineParseResult lineParseResult = lineParser.parseLine(line);
        System.out.println(lineParseResult.chunkId);
        System.out.println(lineParseResult.chunkPhase);
    }
}
