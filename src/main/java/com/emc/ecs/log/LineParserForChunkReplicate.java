package com.emc.ecs.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhengf1 on 1/13/17.
 *
 * parse line like:
 * cm.log:2017-01-09T14:57:47,762 [TaskScheduler-ChunkManager-UNSEAL_GEO_DATA_COMMIT-ParallelExecutor-019]  INFO  GeoSendTrackerTaskScanner.java (line 419) Successfully commit chunk 05518aef-7d08-41ab-8d59-01c51d36703e to remote site urn:storageos:VirtualDataCenterData:0ed5f7ca-745a-48ee-879f-2adc5a03caab with sealed length 134217600
 */
public class LineParserForChunkReplicate implements LineParser {
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

    @Override
    public LineParseResult parseLine(String line) throws Exception {
        if (!line.contains("GeoSendTrackerTaskScanner.java (line 419) Successfully commit chunk")) {
            return null;
        }

        String[] tokens = line.split(" ");
        String chunkId = tokens[11];
        String timestamp = tokens[0].substring(tokens[0].indexOf(":") + 1);

        LineParseResult result= new LineParseResult();
        result.chunkId = chunkId;
        Date date = df.parse(timestamp);
        result.chunkPhase = new ChunkPhase(chunkId, ChunkState.Replicated, date.getTime());
        return result;
    }


    public static void main(String[] args) throws Exception {
        String line = "cm.log:2017-01-09T14:57:47,762 [TaskScheduler-ChunkManager-UNSEAL_GEO_DATA_COMMIT-ParallelExecutor-019]  INFO  GeoSendTrackerTaskScanner.java (line 419) Successfully commit chunk 05518aef-7d08-41ab-8d59-01c51d36703e to remote site urn:storageos:VirtualDataCenterData:0ed5f7ca-745a-48ee-879f-2adc5a03caab with sealed length 134217600\n";

        LineParser lineParser = new LineParserForChunkReplicate();
        LineParseResult lineParseResult = lineParser.parseLine(line);
        System.out.println(lineParseResult.chunkId);
        System.out.println(lineParseResult.chunkPhase);
    }
}
