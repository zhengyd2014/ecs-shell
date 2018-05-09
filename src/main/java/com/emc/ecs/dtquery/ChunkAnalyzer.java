package com.emc.ecs.dtquery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zhengf1 on 10/25/16.
 */
public class ChunkAnalyzer {

    public static Map<String, ChunkSizeDistributionResult> getChunkCountByType(List<Chunk> chunkList) {
        Map<String, ChunkSizeDistributionResult> result = new HashMap<String, ChunkSizeDistributionResult>();
        for (Chunk chunk : chunkList) {
            if (!result.containsKey(chunk.type)) {
                ChunkSizeDistributionResult cr = new ChunkSizeDistributionResult();
                cr.addChunk(chunk);
                result.put(chunk.type, cr);
            } else {
                result.get(chunk.type).addChunk(chunk);
            }
        }

        return result;
    }

    public static Map<String, ChunkSizeDistributionResult> getChunkCountByStatus(List<Chunk> chunkList) {
        Map<String, ChunkSizeDistributionResult> result = new HashMap<String, ChunkSizeDistributionResult>();
        for (Chunk chunk : chunkList) {
            if (!result.containsKey(chunk.status)) {
                ChunkSizeDistributionResult cr = new ChunkSizeDistributionResult();
                cr.addChunk(chunk);
                result.put(chunk.status, cr);
            } else {
                result.get(chunk.status).addChunk(chunk);
            }
        }

        return result;
    }

    public static Map<String, ChunkSizeDistributionResult> groupingByRepoChunkType(List<Chunk> chunkList) {
        Map<String, ChunkSizeDistributionResult> result = new HashMap<String, ChunkSizeDistributionResult>();
        for (Chunk chunk : chunkList) {
            if (!result.containsKey(chunk.repoChunkType)) {
                ChunkSizeDistributionResult cr = new ChunkSizeDistributionResult();
                cr.addChunk(chunk);
                result.put(chunk.repoChunkType, cr);
            } else {
                result.get(chunk.repoChunkType).addChunk(chunk);
            }
        }

        return result;
    }

    public static Map<String, ChunkSizeDistributionResult> groupingByIsEcEncoded(List<Chunk> chunkList) {
        Map<String, ChunkSizeDistributionResult> result = new HashMap<String, ChunkSizeDistributionResult>();
        result.put("true", new ChunkSizeDistributionResult());
        result.put("false", new ChunkSizeDistributionResult());
        result.put("null", new ChunkSizeDistributionResult());
        for (Chunk chunk : chunkList) {
            if (chunk.isEcEncoded) {
                result.get("true").addChunk(chunk);
            } else if (chunk.hasEcEncodedAttribute) {
                result.get("false").addChunk(chunk);
            } else {
                result.get("null").addChunk(chunk);
            }
        }

        return result;
    }


    public static Map<String, ChunkSizeDistributionResult> copyChunkEC(List<Chunk> chunkList) {
        Map<String, ChunkSizeDistributionResult> result = new TreeMap<String, ChunkSizeDistributionResult>();
        result.put("Copy", new ChunkSizeDistributionResult());
        result.put("EC-True", new ChunkSizeDistributionResult());
        result.put("EC-False", new ChunkSizeDistributionResult());

        for (Chunk chunk : chunkList) {
            if (chunk.type != null && chunk.type.equals("COPY")
                    && chunk.dataType != null && chunk.dataType.equals("REPO")) {
                result.get("Copy").addChunk(chunk);
                if (chunk.hasCopyIsEC) {
                    if (chunk.copyIsEC) result.get("EC-True").addChunk(chunk);
                    else result.get("EC-False").addChunk(chunk);
                } else {
                    if (chunk.status != null) {
                        if (!result.containsKey("EC-null-" + chunk.status)) {
                            ChunkSizeDistributionResult cr = new ChunkSizeDistributionResult();
                            cr.addChunk(chunk);
                            result.put("EC-null-" + chunk.status, cr);
                        } else {
                            result.get("EC-null-" + chunk.status).addChunk(chunk);
                        }
                    }
                }
            }
        }

        return result;
    }


    public static Map<String, PartitionDistributionResult> copyChunkPiringByPartition(List<Chunk> chunkList) {
        Map<String, PartitionDistributionResult> result = new TreeMap<String, PartitionDistributionResult>();
        result.put("parity", new PartitionDistributionResult());

        for (Chunk chunk : chunkList) {
            if (chunk.type != null
                    && (chunk.type.equals("COPY") || chunk.type.equals("PARITY"))
                    && chunk.dataType != null
                    && chunk.dataType.equals("REPO")) {

                if(chunk.type.equals("COPY") && chunk.primary != null && chunk.status.equals("DELETED")) {
                    if (!result.containsKey("vdc-ending-" + chunk.primary.substring(chunk.primary.length()-4))) {
                        PartitionDistributionResult cr = new PartitionDistributionResult();
                        cr.addChunk(chunk);
                        result.put("vdc-ending-" + chunk.primary.substring(chunk.primary.length()-4), cr);
                    } else {
                        result.get("vdc-ending-" + chunk.primary.substring(chunk.primary.length()-4)).addChunk(chunk);
                    }
                }

                if (chunk.type.equals("PARITY")) {
                    result.get("parity").addChunk(chunk);
                }
            }
        }

        return result;
    }


    public static Map<String, ChunkSizeDistributionResult> copyChunkPairing(List<Chunk> chunkList) {
        Map<String, ChunkSizeDistributionResult> result = new TreeMap<String, ChunkSizeDistributionResult>();
        result.put("Copy", new ChunkSizeDistributionResult());

        for (Chunk chunk : chunkList) {
            if (chunk.type != null && chunk.type.equals("COPY")
                    && chunk.dataType != null && chunk.dataType.equals("REPO")) {
                result.get("Copy").addChunk(chunk);

                if(chunk.primary != null) {
                    if (!result.containsKey("Partition-"+ chunk.partition +"-Primary-" + chunk.primary)) {
                        ChunkSizeDistributionResult cr = new ChunkSizeDistributionResult();
                        cr.addChunk(chunk);
                        result.put("Partition-"+ chunk.partition +"-Primary-" + chunk.primary, cr);
                    } else {
                        result.get("Partition-"+ chunk.partition +"-Primary-" + chunk.primary).addChunk(chunk);
                    }
                }
            }
        }

        return result;
    }

    public static Map<String, ChunkSizeDistributionResult> groupingLocalChunkByDatatype(List<Chunk> chunkList) {
        Map<String, ChunkSizeDistributionResult> result = new TreeMap<String, ChunkSizeDistributionResult>();
        result.put("Local", new ChunkSizeDistributionResult());

        for (Chunk chunk : chunkList) {
            if (chunk.type != null && chunk.type.equals("LOCAL")) {
                result.get("Local").addChunk(chunk);

                if(chunk.dataType != null) {
                    if (!result.containsKey("Local-dataType-"+ chunk.dataType)) {
                        ChunkSizeDistributionResult cr = new ChunkSizeDistributionResult();
                        cr.addChunk(chunk);
                        result.put("Local-dataType-"+ chunk.dataType, cr);
                    } else {
                        result.get("Local-dataType-"+ chunk.dataType).addChunk(chunk);
                    }
                }
            }
        }

        return result;
    }


    public static Map<String, ChunkSizeDistributionResult> groupingByTypeDatatypeAndStatus(List<Chunk> chunkList) {
        Map<String, ChunkSizeDistributionResult> result = new TreeMap<String, ChunkSizeDistributionResult>();
        for (Chunk chunk : chunkList) {
            String key = chunk.type + "-" + chunk.dataType + "-" + chunk.status;
            if (!result.containsKey(key)) {
                ChunkSizeDistributionResult cr = new ChunkSizeDistributionResult();
                cr.addChunk(chunk);
                result.put(key, cr);
            } else {
                result.get(key).addChunk(chunk);
            }
        }

        return result;
    }
}
