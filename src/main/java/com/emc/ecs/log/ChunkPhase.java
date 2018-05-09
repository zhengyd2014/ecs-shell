package com.emc.ecs.log;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by zhengf1 on 1/12/17.
 */
public class ChunkPhase implements Serializable {

    private static final long serialVersionUID = 2L;

    public String chunkId;
    public long timestamp;
    public ChunkState state;

    public ChunkPhase(String chunkId, ChunkState state, long timestamp) {
        this.chunkId = chunkId;
        this.state = state;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");
        return chunkId + " : " + df.format(timestamp) + " : " + state;
    }
}
