package com.emc.ecs.log;

import com.emc.ecs.dtquery.Chunk;
import com.emc.ecs.dtquery.ChunkSizeDistributionResult;
import com.emc.ecs.util.Constants;
import com.emc.ecs.util.StringUtil;
import com.emc.ecs.util.TimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

/**
 * Created by zhengf1 on 1/12/17.
 */
public class LogParser {

    private static final Logger logger = LogManager.getLogger(LogParser.class);

    private Map<String, ChunkLifeCycle> chunks = new HashMap<String, ChunkLifeCycle>();

    private ChunkPhase firstChunkPhase = null;
    private ChunkPhase lastChunkPhase = null;

    private Map<ChunkState,LineParser> lineParserMap = new HashMap<ChunkState, LineParser>();

    public LogParser() {
        lineParserMap.put(ChunkState.Created, new LineParserForChunkCreate());
        lineParserMap.put(ChunkState.Sealed, new LineParserForChunkSeal());
        lineParserMap.put(ChunkState.Replicated, new LineParserForChunkReplicate());
        lineParserMap.put(ChunkState.GcCandidate, new LineParserForChunkGCCandidate());
        lineParserMap.put(ChunkState.CleanupJobStart, new LineParserForChunkCleanupJobStart());
        lineParserMap.put(ChunkState.CleanupJobDone, new LineParserForChunkCleanupJobDone());
        lineParserMap.put(ChunkState.VerificationTaskCreated, new LineParserForChunkVerificationTaskCreated());
        lineParserMap.put(ChunkState.VerificationTaskDone, new LineParserForChunkVerificationDone());
        lineParserMap.put(ChunkState.ChunkFreed, new LineParserForChunkFreed());
    }

    public Map<String, ChunkLifeCycle> getChunkLifeCycleMap() {
        return chunks;
    }

    public void parseDirectory(String directory) throws Exception {
        File dir = new File(directory);
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".txt")) {
                    return true;
                }

                return false;
            }
        };
        File[] chunkFiles =  dir.listFiles(filenameFilter);
        for (File f : chunkFiles) {
            parseLogFile(f.getAbsolutePath());
        }
    }

    public void parseLogFile(String file) throws Exception {
        BufferedReader br = getReaderFromFile(file);

        if (br == null) {
            logger.info("skip processing the file: " + file);
            return;
        }

        String line = null;
        while ( (line = br.readLine()) != null) {
            try {
                if (line.contains("ChunkServer.java (line 790) created chunk")) {
                    parseLine(line, lineParserMap.get(ChunkState.Created));
                } else if (line.contains("ChunkActiveState.java (line 96) seal chunk")) {
                    parseLine(line, lineParserMap.get(ChunkState.Sealed));
                } else if (line.contains("GeoSendTrackerTaskScanner.java (line 419) Successfully commit chunk")) {
                    parseLine(line, lineParserMap.get(ChunkState.Replicated));
                } else if (line.contains("RepoReclaimer.java (line 304) try to get progress for")) {
                    parseLine(line, lineParserMap.get(ChunkState.GcCandidate));
                } else if (line.contains("RepoReclaimer.java (line 334) has collected all references for repo")) {
                    parseLine(line, lineParserMap.get(ChunkState.CleanupJobStart));
                } else if (line.contains("fullChunkReclaimable true, garbageRangeBit -1, repoUsageSize 0")) {
                    parseLine(line, lineParserMap.get(ChunkState.CleanupJobDone));
                } else if (line.contains("verification status is status: SCHEDULED")) {
                    parseLine(line, lineParserMap.get(ChunkState.VerificationTaskCreated));
                } else if (line.contains("gc verification is complete, deleting chunk")) {
                    parseLine(line, lineParserMap.get(ChunkState.VerificationTaskDone));
                } else if (line.contains("are freed, last chunk info with copies: status: DELETING")) {
                    parseLine(line, lineParserMap.get(ChunkState.ChunkFreed));
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void parseLogFile(BufferedReader br, LineParser lineParser) throws Exception {
        String line = null;
        while ( (line = br.readLine()) != null) {
            parseLine(line, lineParser);
        }
    }

    public void parseLine(String line, LineParser lineParser) throws Exception {
        LineParseResult result = lineParser.parseLine(line);
        if (result != null) {
            if (chunks.get(result.chunkId) != null) {
                chunks.get(result.chunkId).addChunkPhase(result.chunkPhase);
            } else {
                ChunkLifeCycle chunkLifeCycle = new ChunkLifeCycle(result.chunkId);
                chunkLifeCycle.addChunkPhase(result.chunkPhase);
                chunks.put(result.chunkId, chunkLifeCycle);
            }

            if (firstChunkPhase == null || result.chunkPhase.timestamp < firstChunkPhase.timestamp ) {
                firstChunkPhase = result.chunkPhase;
            }

            if (lastChunkPhase == null || result.chunkPhase.timestamp > lastChunkPhase.timestamp) {
                lastChunkPhase = result.chunkPhase;
            }
        }
    }

    private ChunkPhase getFirstChunkPhase() {
        if (firstChunkPhase == null) {
            for (ChunkLifeCycle lifeCycle : chunks.values()) {
                for (ChunkPhase chunkPhase: lifeCycle.getChunkPhaseList()) {
                    if (firstChunkPhase == null || firstChunkPhase.timestamp > chunkPhase.timestamp) {
                        firstChunkPhase = chunkPhase;
                    }
                }
            }
        }

        return firstChunkPhase;
    }

    private ChunkPhase getLastChunkPhase() {
        if (lastChunkPhase == null) {
            for (ChunkLifeCycle lifeCycle : chunks.values()) {
                for (ChunkPhase chunkPhase: lifeCycle.getChunkPhaseList()) {
                    if (lastChunkPhase == null || lastChunkPhase.timestamp  < chunkPhase.timestamp) {
                        lastChunkPhase = chunkPhase;
                    }
                }
            }
        }

        return lastChunkPhase;
    }

    private BufferedReader getReaderFromFile(String file) throws Exception {
        BufferedReader br = null;
        if (file.endsWith(".gz")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
            br = new BufferedReader(new InputStreamReader(gzip));
        } else {
            br = new BufferedReader(new FileReader(file));
        }

        return br;
    }


    public TimeBucket getTimeBucketWithPhase() throws Exception {
        TimeBucket timeBucket = new TimeBucket(getFirstChunkPhase().timestamp, getLastChunkPhase().timestamp);
        for (ChunkLifeCycle chunkLifeCycle : chunks.values()) {
            timeBucket.addChunkLifeCycle(chunkLifeCycle);
        }

        return timeBucket;
    }


    public TimeBucket getTimeBucket() {
        TimeBucket timeBucket = new TimeBucket();
        for (ChunkLifeCycle chunkLifeCycle : chunks.values()) {
            for (ChunkPhase chunkPhase : chunkLifeCycle.getChunkPhaseList()) {
                timeBucket.addChunkPhase(chunkPhase);
            }
        }
        return timeBucket;
    }


    public ChunkLifeCycleDistribution getChunkLifetimeDistribution(long interval, ChunkState state) {
        if (state != null && state.name().equals(ChunkState.ChunkFreed.name())) {
            return null;
        }

        long shortest = Long.MAX_VALUE;
        long longest = Long.MIN_VALUE;

        // first pass, to find shortest and longest time span
        for (ChunkLifeCycle chunkLifeCycle : chunks.values()) {
            long timespan = 0;
            timespan = chunkLifeCycle.getPhaseTime(state);
            if (timespan == 0) continue;

            shortest = Math.min(shortest, timespan);
            longest = Math.max(longest, timespan);
        }


        // second pass, to find the
        System.out.println("shortest: " + StringUtil.readableTime(shortest) + ", longest: " +
                StringUtil.readableTime(longest));

        long start = TimeUtil.roundToHour(shortest);
        long end = TimeUtil.roundToHour(longest) + Constants.ONE_HOUR_IN_MILLISECONDS;
        int rangeNumber = (int)((end - start) / interval) + 1;

        ChunkLifeCycleDistribution distributionResult =
                new ChunkLifeCycleDistribution(start, interval, rangeNumber);
        for (ChunkLifeCycle chunkLifeCycle : chunks.values()) {
            logger.debug(chunkLifeCycle);
            long timespan = chunkLifeCycle.getPhaseTime(state);
            logger.debug(chunkLifeCycle);
            logger.debug("time span: " + timespan);
            distributionResult.addChunkTimeSpan(timespan);
        }

        return distributionResult;
    }

    public void saveToFile(String file) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        try {
            for (ChunkLifeCycle chunkLifeCycle : chunks.values()) {
                oos.writeObject(chunkLifeCycle);
            }
        } finally {
           oos.close();
        }
    }

    public void readFromFile(String file) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        try {
            while (true) {
                ChunkLifeCycle temp = (ChunkLifeCycle) ois.readObject();
                chunks.put(temp.getChunkPhaseList().get(0).chunkId, temp);
            }
        } catch (EOFException e) {
            // do nothing
        } finally {
            ois.close();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("first chunk phase: ").append(firstChunkPhase).append("\n");
        sb.append("last chunk  phase: ").append(lastChunkPhase).append("\n");
        for (ChunkLifeCycle lifeCycle : chunks.values()) {
            sb.append(lifeCycle).append("\n");
        }

        return sb.toString();
    }

}
