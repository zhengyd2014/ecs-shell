package com.emc.ecs.dtquery;

import com.emc.ecs.util.Constants;
import com.emc.ecs.util.StringUtil;

import java.security.Signature;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengf1 on 10/24/16.
 */
public class ChunkSizeDistributionResult {
    public static long SIZE_128M = 128 * 1024 * 1024;

    public class SizeRange {
        long start;
        long end;

        SizeRange(long start, long end) {
            this.start = start;
            this.end = end;
        }
        int count;

        public long getStart() {
            return this.start;
        }

        public long getEnd() {
            return this.end;
        }

        public int getCount() {
            return this.count;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(StringUtil.readableSize(start)).append(" - ").
                    append(StringUtil.readableSize(end)).append(" : ");
            sb.append(count);

            return sb.toString();
        }
    }

    int rangeNumber;
    long rangeLen;

    List<SizeRange> ranges = new ArrayList<SizeRange>();
    List<Chunk> chunkList = new ArrayList<Chunk>();

    public ChunkSizeDistributionResult() {
         this(5);
    }

    public ChunkSizeDistributionResult(int rangeNumber) {
        this.rangeNumber = rangeNumber;
        rangeLen = SIZE_128M / rangeNumber;
        for (int i = 0; i< rangeNumber; i++) {
            ranges.add(new SizeRange(i*rangeLen, (i+1)*rangeLen));
        }
    }

    public List<SizeRange> getRanges() {
        return this.ranges;
    }

    public void addChunk(Chunk chunk) {
        chunkList.add(chunk);
        ranges.get((int)(chunk.sealedLength/rangeLen)).count += 1;
    }

    public int getTotalNumber() {
        return chunkList.size();
    }

    public long getTotalCapacity() {
        long sum = 0;
        for (Chunk chunk : chunkList) {
            sum += chunk.capacity;
        }

        return sum;
    }

    public long getAverageCapacity() {
        if (getTotalNumber() == 0) {
            return 0;
        }
        return getTotalCapacity() / getTotalNumber();
    }

    public long getTotalSealedLength() {
        long sum = 0;
        for (Chunk chunk : chunkList) {
            sum += chunk.sealedLength;
        }

        return sum;
    }

    public long getAverageSealedLength() {
        if (getTotalNumber() == 0) {
            return 0;
        }
        return getTotalSealedLength() / getTotalNumber();
    }

    public int getTotalEcCopies() {
        int sum = 0;
        for (Chunk chunk : chunkList) {
            for (COPY copy : chunk.copyList) {
                if (copy.isEced) {
                    sum++;
                }
            }
        }

        return sum;
    }

    public int getTotalNormalCopies() {
        int sum = 0;
        for (Chunk chunk : chunkList) {
            for (COPY copy : chunk.copyList) {
                if (!copy.isEced) {
                    sum++;
                }
            }
        }

        return sum;
    }

    public long getTotalUsedCapacity() {
        return (long)(getTotalEcCopies() * Constants.EC_CHUNK_SIZE)
                        + getTotalNormalCopies() * Constants.NORMAL_CHUNK_SIZE;
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("number: ").append(getTotalNumber())
                .append(", capacity: ").append(StringUtil.readableSize(getTotalCapacity()))
                .append(", average capacity: ").append(StringUtil.readableSize(getAverageCapacity()))
                .append(", sealed: ").append(StringUtil.readableSize(getTotalSealedLength()))
                .append(", average sealed: ").append(StringUtil.readableSize(getAverageSealedLength()))
                .append(", ec copies: ").append(getTotalEcCopies())
                .append(", normal copies: ").append(getTotalNormalCopies())
                .append(", used disk capacity: ").append(StringUtil.readableSize(getTotalUsedCapacity()))
                .append("\n");

        for (SizeRange range : ranges) {
            sb.append(range).append("\n");
        }

        return sb.toString();
    }

    public String toStringWithoutRange() {
        StringBuffer sb = new StringBuffer();
        sb.append("number: ").append(getTotalNumber())
                .append(", capacity: ").append(StringUtil.readableSize(getTotalCapacity()))
                .append(", average capacity: ").append(StringUtil.readableSize(getAverageCapacity()))
                .append(", sealed: ").append(StringUtil.readableSize(getTotalSealedLength()))
                .append(", average sealed: ").append(StringUtil.readableSize(getAverageSealedLength()))
                .append(", ec copies: ").append(getTotalEcCopies())
                .append(", normal copies: ").append(getTotalNormalCopies())
                .append(", used disk capacity: ").append(StringUtil.readableSize(getTotalUsedCapacity()))
                .append("\n");

        return sb.toString();
    }


    public String[] toStringColumns() {
        List<String> list = new ArrayList<String>();
        list.add(String.valueOf(getTotalNumber()));
        list.add(StringUtil.readableSize(getTotalCapacity()));
        list.add(StringUtil.readableSize(getAverageCapacity()));
        list.add(StringUtil.readableSize(getTotalSealedLength()));
        list.add(StringUtil.readableSize(getAverageSealedLength()));
        list.add(String.valueOf(getTotalEcCopies()));
        list.add(String.valueOf(getTotalNormalCopies()));
        list.add(StringUtil.readableSize(getTotalUsedCapacity()));
        return (String[])list.toArray();
    }

}
