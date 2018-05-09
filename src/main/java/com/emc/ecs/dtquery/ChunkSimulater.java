package com.emc.ecs.dtquery;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhengf1 on 11/1/16.
 */
public class ChunkSimulater {

    private int total_chunk_number;
    private int partition_number = 128;
    private boolean bUseSecureRandom = false;

    public int paired = 0;
    public int unpaired_in_vdc1 = 0;
    public int unpaired_in_vdc2 = 0;

    private List<Partition> partitionList;

    private Random r = new Random();
    private SecureRandom sr = new SecureRandom();

    private class Chunk {
        int partition;
        int vdc;

        public Chunk(int partition, int vdc) {
            this.partition = partition;
            this.vdc = vdc;
        }
    }

    private class Partition {
        int chunk_from_vdc_1;
        int chunk_from_vdc_2;
    }

    public ChunkSimulater(int total_chunk_number) {
        this(total_chunk_number, false);
    }

    public ChunkSimulater(int total_chunk_number, boolean bUseSecureRandom) {
        this.total_chunk_number = total_chunk_number;
        this.bUseSecureRandom = bUseSecureRandom;
    }

    public ChunkSimulater(int total_chunk_number, int partition_number) {
        this.total_chunk_number = total_chunk_number;
        this.partition_number = partition_number;
    }

    public void simulate() {
        partitionList = new ArrayList<Partition>();
        for (int i=0; i<partition_number; i++) {
            partitionList.add(new Partition());
        }

        for (int j=0; j< total_chunk_number; j++) {
            addChunk(generateChunk());
        }

        calculateSimulateResult();
    }

    private Chunk generateChunk() {
        if (bUseSecureRandom) {
            return (new Chunk(sr.nextInt(partition_number), sr.nextInt(2)));
        } else {
            return (new Chunk(r.nextInt(partition_number), r.nextInt(2)));
        }
    }


    private void addChunk(Chunk chunk) {
        if (chunk.vdc == 0) {
            partitionList.get(chunk.partition).chunk_from_vdc_1++;
        } else if (chunk.vdc == 1) {
            partitionList.get(chunk.partition).chunk_from_vdc_2++;
        } else {
            System.out.println("error, chunk vdc from: " + chunk.vdc);
        }
    }

    public double getUnpiredPercentage() {
        return (double)(unpaired_in_vdc1 + unpaired_in_vdc2) / total_chunk_number * 100;
    }

    public String calculateSimulateResult() {
        paired = 0;
        unpaired_in_vdc1 = 0;
        unpaired_in_vdc2 = 0;
        for (Partition p : partitionList) {
            paired += Math.min(p.chunk_from_vdc_1, p.chunk_from_vdc_2);
            if (p.chunk_from_vdc_1 > p.chunk_from_vdc_2) {
                unpaired_in_vdc1 += p.chunk_from_vdc_1 - p.chunk_from_vdc_2;
                //System.out.print(p.chunk_from_vdc_2 + " " + (p.chunk_from_vdc_1 - p.chunk_from_vdc_2) + "\n");

            } else {
                unpaired_in_vdc2 += p.chunk_from_vdc_2 - p.chunk_from_vdc_1;
                //System.out.print(p.chunk_from_vdc_1 + " " + (p.chunk_from_vdc_2 - p.chunk_from_vdc_1) + "\n");
            }
        }

        return paired + " " + unpaired_in_vdc1 + " " + unpaired_in_vdc2;
    }

}
