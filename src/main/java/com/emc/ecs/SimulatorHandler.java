package com.emc.ecs;

import asg.cliche.Command;
import asg.cliche.Param;
import com.emc.ecs.dtquery.ChunkSimulater;

/**
 * Created by zhengf1 on 11/7/16.
 */
public class SimulatorHandler {

    @Command(description = "simulate Chunk XOR")
    public String simulate(@Param(name="chunk_num", description = "total chunk number") int chunk_number,
                         @Param(name="partition_num", description = "partition number (ususally set it to 128)") int partition,
                         @Param(name="iterate_num", description = "the number of times to run the simulation") int iterate_number) {

        int paired = 0;
        int unpaired_in_vdc1 = 0;
        int unpaired_in_vdc2 = 0;

        StringBuffer sb = new StringBuffer();
        for (int i=0; i<iterate_number; i++) {
            ChunkSimulater simulater = new ChunkSimulater(chunk_number, partition);
            simulater.simulate();
            System.out.println(simulater.paired + " " + simulater.unpaired_in_vdc1 + " " + simulater.unpaired_in_vdc2);
            paired += simulater.paired;
            unpaired_in_vdc1 += simulater.unpaired_in_vdc1;
            unpaired_in_vdc2 += simulater.unpaired_in_vdc2;
        }

        sb.append("Average: \n");
        float percentage = (float)(unpaired_in_vdc1 + unpaired_in_vdc2) / (chunk_number * iterate_number) * 100;
        sb.append(paired/iterate_number).append(" ")
                .append(unpaired_in_vdc1/iterate_number)
                .append(" ").append(unpaired_in_vdc2/iterate_number)
                .append(",unpaired total: ").append(unpaired_in_vdc1/iterate_number + unpaired_in_vdc2/iterate_number)
                .append(" unpaired%: ").append(percentage);

        return sb.toString();
    }
}
