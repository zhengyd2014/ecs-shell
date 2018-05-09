package com.emc.ecs.client.management;

import com.emc.ecs.util.StringUtil;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by zhengf1 on 1/1/17.
 */
public class ECSResourceUsage {

    private JSONObject stat;
    private Map<String, JSONArray> resouceMap = new HashMap<String, JSONArray>();
    private int minLengthOfList;

    public static int DEFAUL_HOURS = 480;  // default to retrive 20 days of data

    private static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String TIME_STAMP = "Time Stamp";
    public static String CPU = "nodeCpuUtilizationAvg";
    public static String MEMORY = "nodeMemoryUtilizationAvg";
    public static String NIC = "nodeNicUtilizationAvg";
    public static String BANDWIDTH = "nodeNicBandwidthAvg";

    public static String NIC_RECV = "nodeNicReceivedBandwidthAvg";
    public static String NIC_TRANS = "nodeNicTransmittedBandwidthAvg";
    public static String READ_TPS = "transactionReadTransactionsPerSec";
    public static String WRITE_TPS = "transactionWriteTransactionsPerSec";
    public static String MEM_BYTES = "nodeMemoryUtilizationBytesAvg";

    public static String READ_LATENCY = "transactionReadLatency";
    public static String WRITE_LATENCY = "transactionWriteLatency";


    public ECSResourceUsage(JSONObject stat) {
        this.stat = stat;
        resouceMap.put(CPU, stat.getJSONArray(CPU));
        resouceMap.put(MEMORY, stat.getJSONArray(MEMORY));
        resouceMap.put(NIC, stat.getJSONArray(NIC));
        resouceMap.put(BANDWIDTH, stat.getJSONArray(BANDWIDTH));
        resouceMap.put(NIC_RECV, stat.getJSONArray(NIC_RECV));
        resouceMap.put(NIC_TRANS, stat.getJSONArray(NIC_TRANS));
        resouceMap.put(READ_TPS, stat.getJSONArray(READ_TPS));
        resouceMap.put(WRITE_TPS, stat.getJSONArray(WRITE_TPS));
        resouceMap.put(MEM_BYTES, stat.getJSONArray(MEM_BYTES));
        resouceMap.put(READ_LATENCY, stat.getJSONArray(READ_LATENCY));
        resouceMap.put(WRITE_LATENCY, stat.getJSONArray(WRITE_LATENCY));

        minLengthOfList = getMinLengthOfList();
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    public List<List<String>> getRows(List<String> columeNames, int rowNumber) {
        List<List<String>> result = new ArrayList<List<String>>();

        int result_row_number = (rowNumber < minLengthOfList) ? rowNumber : minLengthOfList;

        for (int i=0; i<result_row_number; i++) {
            List<String> row = new ArrayList<String>();
            boolean hasTimeStampColumn = false;

            // add time stamp
            for (String name : columeNames) {
                if (resouceMap.containsKey(name)) {
                    JSONArray resource = resouceMap.get(name);
                    JSONObject avgSnapshot = (JSONObject) resource.get(i + resource.length() - minLengthOfList);

                    if (!hasTimeStampColumn) {
                        String dateStr = (String) avgSnapshot.get("t");
                        Date d = new Date(Long.parseLong(dateStr) * 1000);
                        row.add(sdfDate.format(d));
                        hasTimeStampColumn = true;
                    }

                    // find value key, which is the one other than "t"
                    String valueKey = null;
                    for (String key : avgSnapshot.keys()) {
                        if (!key.equals("t")) {
                            valueKey = key;
                            break;
                        }
                    }

                    // format value for certain columns
                    String value = "";
                    if (valueKey.equals("Bandwidth") || valueKey.equals("Bytes")) {
                        value = StringUtil.readableSize((String) avgSnapshot.get(valueKey));
                    } else {
                        value = (String) avgSnapshot.get(valueKey);
                    }
                    row.add(value);
                } else if (name.equalsIgnoreCase(TIME_STAMP)) {
                    // do nothing , time stamp will be added by the first meaningful column
                } else {
                    row.add("0");  // default value
                }
            }

            result.add(row);
        }

        return result;
    }


    private int getMinLengthOfList() {
        int minLength = Integer.MAX_VALUE;
        for (String key : resouceMap.keySet()) {
            if (resouceMap.get(key).length() < minLength) {
                minLength = resouceMap.get(key).length();
            }
        }

        return minLength;
    }
}
