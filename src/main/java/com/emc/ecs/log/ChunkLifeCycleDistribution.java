package com.emc.ecs.log;

import com.emc.ecs.dtquery.COPY;
import com.emc.ecs.dtquery.Chunk;
import com.emc.ecs.dtquery.ChunkSizeDistributionResult;
import com.emc.ecs.util.Constants;
import com.emc.ecs.util.StringUtil;
import com.emc.ecs.util.TimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengf1 on 10/24/16.
 */
public class ChunkLifeCycleDistribution {

    private static final Logger logger = LogManager.getLogger(ChunkSizeDistributionResult.class);
    private long start;
    private long end;
    int rangeNumber;
    long rangeLen;
    int totalChunkNumber = 0;
    long totalTime = 0;
    List<TimeRange> ranges = new ArrayList<TimeRange>();

    public ChunkLifeCycleDistribution(long start, long end) {
        this(start, end, 5);
    }

//    public ChunkLifeCycleDistribution(long start, long end, int rangeNumber) {
//        this.start = TimeUtil.roundToHour(start);
//        this.end = TimeUtil.roundToHour(end) + 1;
//
//        this.rangeNumber = rangeNumber;
//        this.rangeLen = (end - start) / rangeNumber;
//        for (int i = 1; i<= rangeNumber; i++) {
//            ranges.add(new TimeRange(i*rangeLen, (i+1)*rangeLen));
//        }
//    }

    public ChunkLifeCycleDistribution(long start, long step, int rangeNumber) {
        this.start = start;
        this.rangeNumber = rangeNumber;
        this.rangeLen = step;
        this.end = start + step * rangeNumber;
        for (int i = 0; i< rangeNumber; i++) {
            ranges.add(new TimeRange(start + i*rangeLen, start + (i+1)*rangeLen));
        }
    }

    public class TimeRange {
        long start;
        long end;

        TimeRange(long start, long end) {
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
//            sb.append(StringUtil.readableTime(start)).append(" - ").
//                    append(StringUtil.readableTime(end)).append(" : ");

            sb.append(StringUtil.readableTime(start)).append(" : ");
            sb.append(count);

            return sb.toString();
        }
    }




 //   List<ChunkLifeCycle> chunkList = new ArrayList<ChunkLifeCycle>();


    public List<TimeRange> getRanges() {
        return this.ranges;
    }


    public void addChunkTimeSpan(long timespan) {
        // invalid timespan, skip
        if (timespan == 0 ) {
            return;
        }

        try {
            ranges.get((int) ((timespan - start) / rangeLen)).count += 1;
        } catch (Exception ex) {
            logger.error("start: " + start + ", end: " + end + ", timespan: " + timespan);
            logger.error(ex.getMessage());
        }
        totalChunkNumber++;
        totalTime += timespan;
    }

//    public void addChunk(ChunkLifeCycle chunk) {
//
//        long timespan = chunk.getLifetime();
//
//        // invalid timespan, skip
//        if (timespan == 0 ) {
//            return;
//        }
//
//        ranges.get((int)(timespan/rangeLen)).count += 1;
//        totalChunkNumber++;
//        totalTime += chunk.getLifetime();
//    }

    public int getTotalNumber() {
        return totalChunkNumber;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getAverageTime() {
        if (getTotalNumber() == 0) {
            return 0;
        }
        return getTotalTime() / getTotalNumber();
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("start time: ").append(StringUtil.readableTime(start))
                .append(", step: ").append(StringUtil.readableTime(rangeLen))
                .append(", range numer: ").append(rangeNumber)
                .append(", end time: ").append(StringUtil.readableTime(end)).append("\n");
        sb.append("chunk number: ").append(getTotalNumber())
                .append(", average time: ").append(StringUtil.readableTime(getAverageTime()))
                .append("\n");

        for (TimeRange range : ranges) {
            sb.append(range).append("\n");
        }

        return sb.toString();
    }

    public String[] toStringColumns() {
        List<String> list = new ArrayList<String>();
        list.add(String.valueOf(getTotalNumber()));
        list.add(StringUtil.readableTime(getAverageTime()));
        return (String[])list.toArray();
    }
}
