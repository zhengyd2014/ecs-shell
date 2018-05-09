package com.emc.ecs.log;

import com.emc.ecs.dtquery.Chunk;
import com.emc.ecs.dtquery.ChunkParser;
import com.emc.ecs.util.Constants;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Created by zhengf1 on 1/12/17.
 */
public class ChunkLifeCycle implements Serializable {

    private static final long serialVersionUID = 1L;

    private String chunkId;

    private TreeMap<ChunkState, ChunkPhase> progress = new TreeMap<ChunkState, ChunkPhase>();

    public ChunkLifeCycle(String id) {
        this.chunkId = id;
    }

    public void addChunkPhase(ChunkPhase chunkPhase) {
        ChunkPhase phase = getChunkPhase(chunkPhase.state);
        if (phase == null || phase.timestamp > chunkPhase.timestamp) {
            progress.put(chunkPhase.state, chunkPhase);
        }
    }

    /**
     *
     * @param state
     * @return
     */
    public ChunkPhase getChunkPhase(ChunkState state) {
        return progress.get(state);
    }


    /**
     *  get the time stayed at the specified phase
     *
     *  if state is null, return the chunk's GClifetime (ChunkFreed.timestamp - CleanupJobStart.timestamp).
     *
     * @param state
     * @return
     */
    public long getPhaseTime(ChunkState state) {
        if (state == null) {
            return getLifetime();
        }

        ChunkPhase chunkPhase = getChunkPhase(state);
        if (chunkPhase == null) {
            return 0;
        }

        long timestamp = chunkPhase.timestamp;
        long ceil = Long.MAX_VALUE;
        for (ChunkPhase phase : progress.values()) {
            if (phase.timestamp > timestamp && phase.timestamp < ceil) {
                ceil = phase.timestamp;
            }
        }

        if (ceil != Long.MAX_VALUE) {
            return ceil - timestamp;
        }

        return 0;
    }


    /**
     *  get chunk's lifetime.
     *
     *  return Freed.timestamp - GcCandidate.timestamp.
     *
     *  if either freed or GcCandidate phase not in the chunk life cycle, return 0.
     */
    private long getLifetime() {
        ChunkPhase created = getChunkPhase(ChunkState.GcCandidate);
        ChunkPhase freed = getChunkPhase(ChunkState.ChunkFreed);

        if (created != null && freed != null) {
            return freed.timestamp - created.timestamp;
        }

        return 0;
    }

    /**
     * get a ChunkPhase by a time str in format "yyyy-MM-ddTHH'.
     *
     * it will return the floor phase comparing the time provided
     * @param timeStr
     * @return
     * @throws Exception
     */
    public ChunkPhase getChunkPhase(String timeStr) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH");
        long time = df.parse(timeStr).getTime();
        return getChunkPhase(time);
    }

    public ChunkPhase getChunkPhase(long time) throws Exception {
        long start = time;
        long end = time + Constants.ONE_HOUR_IN_MILLISECONDS;

        if (progress.firstEntry() != null && end < progress.firstEntry().getValue().timestamp) {
            return null;
        }

        if (end > progress.lastEntry().getValue().timestamp) {
            return new ChunkPhase(progress.lastEntry().getValue().chunkId,
                    progress.lastEntry().getValue().state, time);
        }

        ChunkPhase floorPhase = null;
        for (ChunkPhase phase : progress.values()) {
            if (end > phase.timestamp) {
                if (floorPhase == null) {
                    floorPhase = phase;
                } else if (phase.timestamp > floorPhase.timestamp) {
                    floorPhase = phase;
                }
            }
        }

        return new ChunkPhase(floorPhase.chunkId, floorPhase.state, time);
    }

    public List<ChunkPhase> getChunkPhaseList() {
        return new ArrayList<ChunkPhase>(progress.values());
    }

    @Override
    public String toString() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder sb = new StringBuilder();
        sb.append(this.chunkId + ":\n");
        for (ChunkState state : progress.keySet()) {
            sb.append("    " + state.toString() + " : " + sdfDate.format(new Date(progress.get(state).timestamp)));
        }

        return sb.toString();
    }
}
