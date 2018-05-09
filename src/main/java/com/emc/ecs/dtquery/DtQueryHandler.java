package com.emc.ecs.dtquery;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.ShellFactory;
import com.emc.ecs.Settings;
import com.emc.ecs.dtquery.DirectoryTable.DirectoryTable;
import com.emc.ecs.dtquery.DirectoryTable.Entry;
import com.emc.ecs.dtquery.DirectoryTable.XMLParser;
import com.emc.ecs.util.Constants;
import com.emc.ecs.util.Output;
import com.emc.ecs.util.StringUtil;
import com.emc.ecs.util.UrlUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengf1 on 10/24/16.
 */
public class DtQueryHandler {

    ChunkParser parser1;
    ChunkParser parser2;
    ChunkParser parser;
    Settings settings = Settings.getInstance();

    @Command (description="group chunks by chunk's type")
    public void type() {
        Output.printChunkResultWithRange(ChunkAnalyzer.getChunkCountByType(parser.getChunkList()));
    }

    @Command (description="group chunks by chunk's status")
    public void status() {
            Output.printChunkResultWithRange(ChunkAnalyzer.getChunkCountByStatus(parser.getChunkList()));

    }

    @Command (description="group chunks by chunk's isEcCoded")
    public void ec() {
        printChunkResult(ChunkAnalyzer.groupingByIsEcEncoded(parser.getChunkList()));
    }

    @Command (description="group chunks by chunk's repo type")
    public void repotype() {
        printChunkResult(ChunkAnalyzer.groupingByRepoChunkType(parser.getChunkList()));
    }

    @Command (description="switch chunk table level")
    public void setparser(@Param(name="level", description="chunk table level, valid values [1, 2]") int level) {
        if (level == 1) {
            System.out.println("switch to chunk table leve 1");
            parser = parser1;
        } else if (level == 2) {
            System.out.println("switch to chunk table leve 2");
            parser = parser2;
        }
    }



    @Command (description = "grouping COPY chunks by whether it is ECed")
    public void copychunkec() {

            Output.printChunkResultWithRange(ChunkAnalyzer.copyChunkEC(parser.getChunkList()));

    }

    @Command (description = "grouping COPY chunks by partition and its primary site")
    public void copychunkpair() {
        //printChunkResult(ChunkAnalyzer.copyChunkPairing(parser.getChunkList()));
        printPartitionResult(ChunkAnalyzer.copyChunkPiringByPartition(parser.getChunkList()));
    }

    @Command (description = "grouping LOCAL chunks by dataType")
    public void localchunk() {
        printChunkResult(ChunkAnalyzer.groupingLocalChunkByDatatype(parser.getChunkList()));
    }

    @Command (description = "grouping COPY chunks by whether it is ECed")
    public void groupingCombination() {

            Output.printChunkResultWithRange(ChunkAnalyzer.groupingByTypeDatatypeAndStatus(parser.getChunkList()));

    }

    @Command (description = "grouping COPY chunks by whether it is ECed")
    public void printtable() {
        Output.printChunkResultWithoutRange(ChunkAnalyzer.groupingByTypeDatatypeAndStatus(parser.getChunkList()));
    }

    // @Command
    public void printAll() {
        String[] sites = new String[] {"10.243.85.25", "10.243.85.35", "10.243.85.45"};
    }

    // @Command (description="load chunk tables from specified ecs node")
    public void chunktable(@Param(name="IP", description="ECS Node IP") String ip) throws Exception {
        DirectoryTable directoryTable = new DirectoryTable(ip);
        parser1 = new ChunkParser();
        parser2 = new ChunkParser();
        parser = parser1;

        List<Entry> entries = directoryTable.getChunkTableLevel1();
        System.out.println("parsing chunk table level 1: entry size: " + entries.size());
        int index = 0;
        for (Entry entry : entries) {

            System.out.print(index + " ");

            try {
                parser1.parseInput(UrlUtil.openUrlConnection(entry.table_detail_link), index);
            } catch (Exception ex) {
                // just print the error message and keep going
                System.out.println(ex.getMessage());
                System.out.println("skipped and parsing other partitions.");
            }
            index++;
        }
        System.out.println();

        System.out.println();
        System.out.println("parsing chunk table level 2: entry size: " + entries.size());
        entries = directoryTable.getChunkTableLevel2();
        index = 0;
        for (Entry entry : entries) {
            System.out.print(index + " ");
            parser2.parseInput(UrlUtil.openUrlConnection(entry.table_detail_link), index);
            index++;
        }
        System.out.println();
        System.out.println("done");
    }

    // @Command(description = "load chunk info from file")
    public void fromfile(String filename) throws Exception {
        parser = new ChunkParser();
        System.out.println(new File(filename).getAbsolutePath());
        parser.parseFile(filename, 1);
        System.out.println("done");
    }

    private void printChunkResult(Map<String, ChunkSizeDistributionResult> map) {
        StringBuffer sb = new StringBuffer();
        long ecSum = 0;
        long normalSum = 0;
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            String type = (String)it.next();
            sb.append(type).append(" : ").append(map.get(type)).append("\n");
            ecSum += map.get(type).getTotalEcCopies();
            normalSum += map.get(type).getTotalNormalCopies();
        }
        sb.append("total EC copies: " + ecSum + " , total normal copies: " + normalSum);
        sb.append("\ntotal used capacity: " + StringUtil.readableSize(
                (long)(ecSum * Constants.EC_CHUNK_SIZE)
                + normalSum * Constants.NORMAL_CHUNK_SIZE));

        System.out.println(sb.toString());
    }

    private void printChunkResultWithoutRange(Map<String, ChunkSizeDistributionResult> map) {
        StringBuffer sb = new StringBuffer();
        long ecSum = 0;
        long normalSum = 0;
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            String type = (String)it.next();
            sb.append(type).append(" : ").append(map.get(type).toStringWithoutRange());
            ecSum += map.get(type).getTotalEcCopies();
            normalSum += map.get(type).getTotalNormalCopies();
        }
        sb.append("total EC copies: " + ecSum + " , total normal copies: " + normalSum);
        sb.append("\ntotal used capacity: " + StringUtil.readableSize(
                (long)(ecSum * Constants.EC_CHUNK_SIZE)
                        + normalSum * Constants.NORMAL_CHUNK_SIZE));

        System.out.println(sb.toString());
    }

    private void printPartitionResult(Map<String, PartitionDistributionResult> map) {

        StringBuffer sb = new StringBuffer();

        List<PartitionDistributionResult> list = new ArrayList<PartitionDistributionResult>();
        List<String> vdcs = new ArrayList<String>();
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            String type = (String)it.next();
            vdcs.add(type);
            list.add(map.get(type));
        }

        sb.append("partition | ");
        for (int i = 0; i<vdcs.size(); i++) {
            sb.append(vdcs.get(i)).append(" | ");
        }
        sb.append(" wait for paired | ");
        sb.append("\n");

        PartitionDistributionResult shouldPairResult = new PartitionDistributionResult();
        for (int j= 0; j< PartitionDistributionResult.PARTITION_NUMBER; j++) {
            sb.append(j + "  | ");
            int i = 0;
            for (; i<list.size(); i++) {
                PartitionDistributionResult r = list.get(i);
                sb.append(r.getChunkNumber(j)).append("  |  ");
            }

            // make sure array index not out of range
            if (i<2 || i >= list.size()) {
                sb.append(i + " is out of range\n");
                continue;
            }
            int shouldPaireNumber = Math.min(list.get(i-2).getChunkNumber(j), list.get(i-1).getChunkNumber(j));
            sb.append(shouldPaireNumber);
            shouldPairResult.setChunkNumber(j,shouldPaireNumber);
            sb.append("\n");
        }

        sb.append("total | ");
        for (int i = 0; i<list.size(); i++) {
            PartitionDistributionResult r = list.get(i);
            sb.append(r.getTotalNumber()).append("  |  ");
        }
        sb.append(shouldPairResult.getTotalNumber()).append(" | ");
        sb.append("\n");

        System.out.println(sb.toString());
    }




}
