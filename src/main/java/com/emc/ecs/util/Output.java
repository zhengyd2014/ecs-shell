package com.emc.ecs.util;

import com.emc.ecs.Settings;
import com.emc.ecs.client.management.ECSBucketInfo;
import com.emc.ecs.client.management.ECSResourceUsage;
import com.emc.ecs.client.management.ECSUserInfo;
import com.emc.ecs.log.ChunkPhase;
import com.emc.ecs.log.ChunkState;
import com.emc.ecs.log.TimeBucket;
import com.emc.ecs.stat.StatGroup;
import com.emc.ecs.stat.StatHistory;
import com.emc.ecs.stat.StatParser;
import com.emc.ecs.stat.StatPrimitive;
import com.emc.ecs.stat.StatSnapshot;
import com.emc.ecs.dtquery.ChunkSizeDistributionResult;
import com.emc.storageos.data.datasvcmodels.ObjectNamedRelatedResourceRep;
import com.emc.storageos.data.datasvcmodels.StringHashMapEntry;
import com.emc.storageos.data.datasvcmodels.VirtualArrayRestRep;
import com.emc.storageos.data.datasvcmodels.nodes.DataNodeRestRep;
import com.emc.storageos.data.datasvcmodels.vpool.DataServiceVpoolRestRep;
import com.emc.storageos.data.datasvcmodels.zone.VdcRestRep;
import com.emc.storageos.objcontrol.service.impl.resource.dynamicconfig.model.DynamicConfigEntry;
import com.emc.storageos.objcontrol.service.impl.resource.dynamicconfig.model.DynamicConfigResp;
import de.vandermeer.asciitable.v2.RenderedTable;
import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthLongestLine;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by zhengf1 on 11/12/16.
 */
public class Output {

    public static void printChunkResultWithoutRange(Map<String, ChunkSizeDistributionResult> map) {
        V2_AsciiTable table = new V2_AsciiTable();
        StringBuffer sb = new StringBuffer();

        table.addRule();
        table.addRow("chunk name","chunk number", "capacity", "avg. capacity",
                "sealed", "avg. sealed", "ec copies", "normal copies", "disk usage" ); // headers
        table.addRule();

        sb.append("chunk name,chunk number,capacity,avg. capacity,sealed,avg. sealed,ec copies,normal copies,disk usage\n"); // headers

        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            String type = (String)it.next();
            addChunkResultRow(table,type,map.get(type));
            addChunkResultRow(sb, type, map.get(type));
        }
        table.addRule();


        printV2AsciiTable(table);

    }


    public static void printChunkResultWithRange(Map<String, ChunkSizeDistributionResult> map) {

        List<String> headers = new ArrayList<String>();
        List<List<String>> valueList = new ArrayList<List<String>>();

        StringBuffer sb = new StringBuffer();

        headers.add("Name");
        sb.append("Name");

        Iterator it = map.keySet().iterator();
        int index = 0;
        while (it.hasNext()) {
            String type = (String)it.next();
            List<String> values = new ArrayList<String>();
            ChunkSizeDistributionResult result = map.get(type);
            if (type != null) {
                values.add(type);
            } else {
                values.add("null");
            }
            int sum = 0;

            for (ChunkSizeDistributionResult.SizeRange range : result.getRanges()) {
                if (index == 0) {
                    headers.add(StringUtil.readableSize(range.getStart())
                            + " - " + StringUtil.readableSize(range.getEnd()));
                }
                values.add(String.valueOf(range.getCount()));
                sum += range.getCount();
            }
            values.add(String.valueOf(sum));
            valueList.add(values);
            index++;
        }

        headers.add("Total");


        V2_AsciiTable table = new V2_AsciiTable();

        table.addRule();
        table.addRow(headers.toArray()); // headers
        table.addRule();
        for (List<String> values : valueList){
            table.addRow(values.toArray());
        }
        table.addRule();

        printV2AsciiTable(table);
    }

    public static void printV2AsciiTable(V2_AsciiTable table) {
        V2_AsciiTableRenderer rend = new V2_AsciiTableRenderer();
        rend.setTheme(V2_E_TableThemes.UTF_LIGHT.get());
        rend.setWidth(new WidthLongestLine());
        RenderedTable rt = rend.render(table);
        System.out.println(rt);
    }



    public static void printConfig(DynamicConfigResp config) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("name", "configured value", "default value"); //, "description");
        table.addRule();
        for (DynamicConfigEntry entry : config.getConfigEntries()) {
            if (Constants.CONFIG_PARAMETER_NAMES.contains(entry.getName()) ) {
                table.addRow(entry.getName(),
                        entry.getConfiguredValue() == null ? "null" : entry.getConfiguredValue(),
                        entry.getDefaultValue());
                // entry.getDescription());
            }
        }
        table.addRule();
        printV2AsciiTable(table);
    }


    private static void addChunkResultRow(V2_AsciiTable table, String type, ChunkSizeDistributionResult chunkResult) {
        table.addRow(type,
                String.valueOf(chunkResult.getTotalNumber()),
                StringUtil.readableSize(chunkResult.getTotalCapacity()),
                StringUtil.readableSize(chunkResult.getAverageCapacity()),
                StringUtil.readableSize(chunkResult.getTotalSealedLength()),
                StringUtil.readableSize(chunkResult.getAverageSealedLength()),
                String.valueOf(chunkResult.getTotalEcCopies()),
                String.valueOf(chunkResult.getTotalNormalCopies()),
                StringUtil.readableSize(chunkResult.getTotalUsedCapacity()));
    }

    private static String addChunkResultRow(StringBuffer sb, String type, ChunkSizeDistributionResult chunkResult) {
        sb.append(type).append(",")
                .append(String.valueOf(chunkResult.getTotalNumber())).append(",")
                .append(StringUtil.readableSize(chunkResult.getTotalCapacity())).append(",")
                .append(StringUtil.readableSize(chunkResult.getAverageCapacity())).append(",")
                .append(StringUtil.readableSize(chunkResult.getTotalSealedLength())).append(",")
                .append(StringUtil.readableSize(chunkResult.getAverageSealedLength())).append(",")
                .append(String.valueOf(chunkResult.getTotalEcCopies())).append(",")
                .append(String.valueOf(chunkResult.getTotalNormalCopies())).append(",")
                .append(StringUtil.readableSize(chunkResult.getTotalUsedCapacity())).append("\n");
        return sb.toString();
    }





    /**
     * print a table for stat primitives defined in Constants.STAT_IDS
     *
     * @param stat
     */
    public static void printStatTable(StatGroup stat) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("ID", "Value", "Time Stamp"); //, "description");
        table.addRule();
        for (String id : Constants.STAT_IDS) {
            StatPrimitive primitive = StatParser.findPrimitive(stat, id);
            if (primitive != null) {
                table.addRow(id, primitive.getCounter(), primitive.getTimestampStr());
            }
        }

        table.addRule();
        printV2AsciiTable(table);
    }


    /**
     * find given attribute in stat.
     *
     * the attribute has to be historical type, has a list of snaphots with the format of
     * [{"t" : "1481616000", "key" : "334"}]
     *
     * @param stat
     * @param rowList
     */
    private static void addHistoricalStatToList(JSONArray stat, List<List<String>> rowList) {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        //
        int length = stat.length();
        if (rowList.size() !=0 && rowList.get(0) != null) {
            length = rowList.get(0).size();
        }



        System.out.println(stat.length());

        for (int i = 0; i<length; i++) {
            JSONObject avgSnapshot = stat.getJSONObject(i);
            List<String> row = null;
            if (rowList.size() <= i) {
                row = new ArrayList<String>();
                String dateStr = (String)avgSnapshot.get("t");
                Date d = new Date(Long.parseLong(dateStr) * 1000);
                row.add(sdfDate.format(d));
                rowList.add(row);
            } else {
                row = rowList.get(i);
            }

            // find value key, which is the one other than "t"
            String valueKey = null;
            for (String key : avgSnapshot.keys()) {
                if (!key.equals("t")) {
                    valueKey = key;
                    break;
                }
            }

            String value = "";
            if (valueKey.equals("Bandwidth") || valueKey.equals("Bytes")) {
                value = StringUtil.readableSize((String)avgSnapshot.get(valueKey));
            } else {
                value = (String) avgSnapshot.get(valueKey);
            }
            row.add(value);
        }
    }


    public static void printBuckets(List<ECSBucketInfo> bucketInfos) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("namespace", "bucket", "replication group", "object number", "size (GB)");
        table.addRule();
        for (ECSBucketInfo bucketInfo : bucketInfos) {
            table.addRow(bucketInfo.getNamespace(),
                    bucketInfo.getBucketName(),
                    bucketInfo.getReplicationGroup(),
                    bucketInfo.getObjectNumber(),
                    bucketInfo.getSize());
        }
        table.addRule();
        printV2AsciiTable(table);
    }


    public static void printVdcs(List<VdcRestRep> vdcList) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("Vdc Name", "ID", "End Points", "Secret Key");
        table.addRule();
        for (VdcRestRep vdc : vdcList) {
            table.addRow(vdc.getName(),
                    vdc.getVdcId(),
                    vdc.getInterVdcEndPoints(),
                    vdc.getSecretKeys());
        }
        table.addRule();
        printV2AsciiTable(table);
    }

    public static void printReplicationGroups(List<DataServiceVpoolRestRep> rgList) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("RG Name", "ID", "Mapping");
        table.addRule();
        for (DataServiceVpoolRestRep rg : rgList) {

            String varryMapping = "";
            for (StringHashMapEntry entry : rg.getVarrayMappings()) {
                varryMapping += entry.getName() + "=" + entry.getValue() + " ";
            }
            table.addRow(rg.getName(),
                    rg.getId(),
                    varryMapping);
        }
        table.addRule();
        printV2AsciiTable(table);
    }

    public static void printStoragePools(List<VirtualArrayRestRep> storagePools) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("StoragePool Name", "ID");
        table.addRule();
        for (VirtualArrayRestRep sp : storagePools) {
            table.addRow(sp.getName(),
                    sp.getId());
        }
        table.addRule();
        printV2AsciiTable(table);
    }

    public static void printNamespaces(List<ObjectNamedRelatedResourceRep> namespaces) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("Namespace", "ID");
        table.addRule();
        for (ObjectNamedRelatedResourceRep ns : namespaces) {
            table.addRow(ns.getName(),
                    ns.getId());
        }
        table.addRule();
        printV2AsciiTable(table);
    }

    public static void printNodes(List<DataNodeRestRep> nodes) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("Node Name", "IP", "Version");
        table.addRule();
        for (DataNodeRestRep node : nodes) {
            table.addRow(node.getNodeName(),
                    node.getIp(),
                    node.getVersion());
        }
        table.addRule();
        printV2AsciiTable(table);
    }

    public static void printUsers(List<ECSUserInfo> users) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("Namespace", "User Name", "Secret Key");
        table.addRule();
        for (ECSUserInfo user : users) {
            table.addRow(user.getNamespace(),
                    user.getUserName(),
                    user.getSecretKey());
        }
        table.addRule();
        printV2AsciiTable(table);
    }


    public static void print(List<ChunkPhase> list, int number) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");

        // header
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("Time Stamp", "Chunk ID", "State");
        table.addRule();

        for (int i=0; i<list.size() && i<number; i++){
            ChunkPhase phase = list.get(i);
            table.addRow(df.format(new Date(phase.timestamp)),
                    phase.chunkId,
                    phase.state);
        }
        table.addRule();
        printV2AsciiTable(table);
    }

    public static void print(TimeBucket timeBucket) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH");

        // header
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("Hour",
                ChunkState.Created,
                ChunkState.Sealed,
                ChunkState.Replicated,
                ChunkState.GcCandidate,
                ChunkState.CleanupJobStart,
                ChunkState.CleanupJobDone,
                ChunkState.VerificationTaskCreated,
                ChunkState.VerificationTaskDone,
                ChunkState.ChunkFreed,
                "Total Chunks"
                );
        table.addRule();

        // contents
        for (Long key : timeBucket.getKeys()) {
            table.addRow(df.format(new Date(key)),
                    timeBucket.getChunkNumber(key, ChunkState.Created),
                    timeBucket.getChunkNumber(key, ChunkState.Sealed),
                    timeBucket.getChunkNumber(key, ChunkState.Replicated),
                    timeBucket.getChunkNumber(key, ChunkState.GcCandidate),
                    timeBucket.getChunkNumber(key, ChunkState.CleanupJobStart),
                    timeBucket.getChunkNumber(key, ChunkState.CleanupJobDone),
                    timeBucket.getChunkNumber(key, ChunkState.VerificationTaskCreated),
                    timeBucket.getChunkNumber(key, ChunkState.VerificationTaskDone),
                    timeBucket.getChunkNumber(key, ChunkState.ChunkFreed),
                    timeBucket.getHourlyTotal(key)
            );
        }
        table.addRule();

        // footer
        table.addRow("Summary",
                timeBucket.getTotalByChunkState(ChunkState.Created),
                timeBucket.getTotalByChunkState(ChunkState.Sealed),
                timeBucket.getTotalByChunkState(ChunkState.Replicated),
                timeBucket.getTotalByChunkState(ChunkState.GcCandidate),
                timeBucket.getTotalByChunkState(ChunkState.CleanupJobStart),
                timeBucket.getTotalByChunkState(ChunkState.CleanupJobDone),
                timeBucket.getTotalByChunkState(ChunkState.VerificationTaskCreated),
                timeBucket.getTotalByChunkState(ChunkState.VerificationTaskDone),
                timeBucket.getTotalByChunkState(ChunkState.ChunkFreed),
                ""
        );

        table.addRule();
        printV2AsciiTable(table);
    }


    public static void print(ECSResourceUsage usage) {

        List<String> headers =  Arrays.asList(ECSResourceUsage.TIME_STAMP,
        ECSResourceUsage.CPU,
                ECSResourceUsage.MEMORY,
                ECSResourceUsage.NIC,
                ECSResourceUsage.READ_LATENCY,
                ECSResourceUsage.WRITE_LATENCY
                ,
                ECSResourceUsage.READ_TPS,
                ECSResourceUsage.WRITE_TPS,
                ECSResourceUsage.NIC_RECV,
                ECSResourceUsage.NIC_TRANS
        );
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();

        table.addRow(headers.toArray());

        table.addRule();

        // handle row data
        List<List<String>> rowList = usage.getRows(headers, 500);

        for (int i=0; i<rowList.size()-1; i++) {
            List<String> row = rowList.get(i);
            table.addRow(row.toArray());
        }

        table.addRule();
        printV2AsciiTable(table);

    }

    public static void printResourceUsageTable(JSONObject stat) {

        String cpu = "nodeCpuUtilizationAvg";
        String mem = "nodeMemoryUtilizationAvg";
        String nic = "nodeNicUtilizationAvg";

        String nicBandwidth = "nodeNicBandwidthAvg";
        String nicRec = "nodeNicReceivedBandwidthAvg";
        String nicTrans = "nodeNicTransmittedBandwidthAvg";
        String read_tps = "transactionReadTransactionsPerSec";
        String write_tps = "transactionWriteTransactionsPerSec";
        String memBytes = "nodeMemoryUtilizationBytesAvg";

        String readLatency = "transactionReadLatency";
        String writeLatency = "transactionWriteLatency";

        //System.out.println(stat);
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        //table.addRow("Time Stamp", cpu, mem, nic, nicRec, nicTrans, read_tps, write_tps, memBytes); //, "description");
        table.addRow("Time Stamp", cpu, mem, nic, readLatency, writeLatency, read_tps, write_tps, nicRec, nicTrans); //, "description");

        table.addRule();

        // handle row data
        List<List<String>> rowList = new ArrayList<List<String>>();
        //System.out.println(stat);
        addHistoricalStatToList(stat.getJSONArray(cpu), rowList);
        addHistoricalStatToList(stat.getJSONArray(mem), rowList);
        addHistoricalStatToList(stat.getJSONArray(nic), rowList);


        //addHistoricalStatToList(stat.getJSONArray(memBytes), rowList, true);

        addHistoricalStatToList(stat.getJSONArray(readLatency), rowList);
        addHistoricalStatToList(stat.getJSONArray(writeLatency), rowList);

        addHistoricalStatToList(stat.getJSONArray(read_tps), rowList);
        addHistoricalStatToList(stat.getJSONArray(write_tps), rowList);

        addHistoricalStatToList(stat.getJSONArray(nicRec), rowList);
        addHistoricalStatToList(stat.getJSONArray(nicTrans), rowList);

        for (int i=0; i<rowList.size()-1; i++) {
            List<String> row = rowList.get(i);
            table.addRow(row.toArray());
        }

        table.addRule();
        printV2AsciiTable(table);
    }

    public static void printTree(StatGroup tree) {
        printTree(tree, true, true, "");
    }

    /**
     * Recursively print out the node and all its children.
     *
     */
    private static void printTree(StatGroup node, boolean firstNode, boolean lastNode, String prefix) {

        String self_prefix = prefix;
        if (lastNode) {
            self_prefix += "+-";
        } else {
            self_prefix += "|-";
        }

        StringBuffer nodeSB = new StringBuffer();

        byte[] data = null;
        if (firstNode) {
            nodeSB.append(self_prefix + "/");
        } else {
            nodeSB.append(self_prefix
                    + node.getId());
        }
        System.out.println(nodeSB.toString());


        List<StatGroup> children = node.getGroups();
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                StatGroup childNode = children.get(i);
                String child_prefix = prefix;

                boolean lastChild = false;
                if (i == (children.size() - 1))
                    lastChild = true;

                if (lastNode) {
                    child_prefix += "    ";
                } else {
                    child_prefix += "|   ";
                }

                printTree(childNode, false, lastChild, child_prefix);
            }
        }
    }

    public static void print(StatHistory statHistory) {
        System.out.println(statHistory.getPath());
        String format = "";
        if (statHistory.getPath().contains("deleted_chunks_repo")) {
            format = "chunk";
        }

        for (StatSnapshot snapshot : statHistory.getContinuosSnapshots()) {
            print(snapshot, format);
        }
    }


    private static void print(StatSnapshot snapshot, String format) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (format.equals("chunk")) {
            String capacity = StringUtil.readableSize(Long.parseLong(snapshot.getValue())*128*1024*1024);
            System.out.println(snapshot.getTimeByFormat(sdfDate) + " : " + snapshot.getValue() + " : " + capacity);
        } else {
            System.out.println(snapshot.getTimeByFormat(sdfDate) + " : " + snapshot.getValue());
        }
    }


}
