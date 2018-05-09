package com.emc.ecs.log;

import com.emc.ecs.util.Constants;
import com.emc.ecs.util.TimeUtil;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by zhengf1 on 1/16/17.
 */
public class TimeBucket {
    private TreeMap<Long, HourBucket> timeBuckets = new TreeMap<Long, HourBucket>();
    private long startHour;
    private long endHour;

    public TimeBucket() {
    }

    public TimeBucket(long startTime, long endTime) {
        startHour = TimeUtil.roundToHour(startTime);
        endHour = TimeUtil.roundToHour(endTime);
        for (long i=startHour; i <= endHour; i += Constants.ONE_HOUR_IN_MILLISECONDS) {
            timeBuckets.put(i, new HourBucket());
        }
    }

    public void addChunkLifeCycle(ChunkLifeCycle chunkLifeCycle) throws Exception {
        for (long i=startHour; i <= endHour; i += Constants.ONE_HOUR_IN_MILLISECONDS) {
            ChunkPhase phase = chunkLifeCycle.getChunkPhase(i);
            if (phase != null) {
                timeBuckets.get(i).add(phase);
            }
        }
    }

    public void addChunkPhase(ChunkPhase chunkPhase) {
        Long hour = TimeUtil.roundToHour(chunkPhase.timestamp);

        if (timeBuckets.get(hour) == null) {
            HourBucket hourBucket = new HourBucket();
            hourBucket.add(chunkPhase);
            timeBuckets.put(hour, hourBucket);
        } else {
            timeBuckets.get(hour).add(chunkPhase);
        }
    }


    public Set<Long> getKeys() {
        return timeBuckets.keySet();
    }

    public List<HourBucket> getHourBuckets() {
        List<HourBucket> hourBuckets = new ArrayList<HourBucket>();
        for (Long key : timeBuckets.keySet()) {
            hourBuckets.add(timeBuckets.get(key));
        }

        return hourBuckets;
    }

    public long getChunkNumber(Long hour, ChunkState state) {
        return timeBuckets.get(hour).getNumberByState(state);
    }

    public List<ChunkPhase> getChunkPhaseList(String time) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH");
        return timeBuckets.get(df.parse(time).getTime()).getChunkPhaseList();
    }

    public long getHourlyTotal(Long hour) {
        return timeBuckets.get(hour).getTotal();
    }

    public long getTotalByChunkState(ChunkState state) {
        long sum = 0;
        for (Long key : timeBuckets.keySet()) {
            sum += timeBuckets.get(key).getNumberByState(state);
        }

        return sum;
    }

    class HourBucket {
        List<ChunkPhase> phaseList = new ArrayList<ChunkPhase>();

        public List<ChunkPhase> getChunkPhaseList() {
            return phaseList;
        }

        public void add(ChunkPhase chunkPhase) {
            phaseList.add(chunkPhase);
        }

        public long getNumberByState(ChunkState state) {
            long sum = 0;
            for (ChunkPhase phase : phaseList) {
                if (phase.state.equals(state)) {
                    sum ++;
                }
            }

            return sum;
        }

        public long getTotal() {
            return phaseList.size();
        }
    }
}
