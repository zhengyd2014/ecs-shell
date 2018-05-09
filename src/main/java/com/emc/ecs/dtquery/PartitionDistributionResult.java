package com.emc.ecs.dtquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengf1 on 10/28/16.
 */
public class PartitionDistributionResult {
    static public int PARTITION_NUMBER = 128;

    public List<Integer> chunkNumberList = new ArrayList<Integer>();


    public PartitionDistributionResult() {
        for (int i=0; i<=PARTITION_NUMBER; i++) {
            chunkNumberList.add(0);
        }
    }


    public void addChunk(Chunk chunk) {
        chunkNumberList.set(chunk.partition, chunkNumberList.get(chunk.partition) + 1);
    }

    public void setChunkNumber(int index, int number) {
        chunkNumberList.set(index, number);
    }

    public int getChunkNumber(int index) {
        return chunkNumberList.get(index);
    }

    public int getTotalNumber() {
        int sum = 0;
        for (int i : chunkNumberList) {
            sum += i;
        }

        return sum;
    }
}
