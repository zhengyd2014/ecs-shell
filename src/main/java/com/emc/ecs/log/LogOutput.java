package com.emc.ecs.log;

import com.emc.ecs.util.StringUtil;
import de.vandermeer.asciitable.v2.V2_AsciiTable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.emc.ecs.util.Output.printV2AsciiTable;

/**
 * Created by fred on 2/13/17.
 */
public class LogOutput {

    public static void print(List<ChunkPhase> list, int number) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

        // header
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("Time Stamp", "Chunk ID", "State");
        table.addRule();

        for (int i=0; i<list.size() && i<number; i++){
            ChunkPhase phase = list.get(i);
            table.addRow(df.format(new Date(phase.timestamp)),
                    phase.chunkId,
                    phase.state);
        }
        table.addRule();
        printV2AsciiTable(table);
    }

    public static void print(ChunkLifeCycle lifeCycle) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

        // header
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("Time Stamp", "Chunk ID", "State", "time spent");
        table.addRule();

        List<ChunkPhase> chunkPhaseList = lifeCycle.getChunkPhaseList();
        long startTime = chunkPhaseList.get(0).timestamp;
        for (int i=0; i<chunkPhaseList.size(); i++){
            ChunkPhase phase = chunkPhaseList.get(i);
            table.addRow(df.format(new Date(phase.timestamp)),
                    phase.chunkId,
                    phase.state,
                    StringUtil.readableTime(phase.timestamp - startTime));
        }
        table.addRule();
        printV2AsciiTable(table);
    }
}
