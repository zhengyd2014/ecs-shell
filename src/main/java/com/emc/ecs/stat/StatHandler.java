package com.emc.ecs.stat;

import asg.cliche.Command;
import com.emc.ecs.Settings;
import com.emc.ecs.client.ECSClient;
import com.emc.ecs.client.management.ECSInfo;
import com.emc.ecs.util.Output;
import com.emc.ecs.util.UrlUtil;
import com.emc.storageos.data.datasvcmodels.VirtualArrayRestRep;
import com.emc.storageos.data.datasvcmodels.syslog.SyslogServerConfig;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhengf1 on 11/29/16.
 */
public class StatHandler {

    private String ecs;
    private List<StatHistory> historyList;


    public StatHandler(String ecs) {
        this.ecs = ecs;
    }

    @Command
    public void getStat() throws Exception {
        String statUrl = "https://" + ecs + ":4443/stat/aggregate";
        JSONObject jsonObject = getJSONObjectFromUrl(statUrl);
//        Output.printTree(StatParser.parseGroup(jsonObject));
        Output.printStatTable(StatParser.parseGroup(jsonObject));
    }


    @Command
    public void listHistoryPathes(String path) throws Exception {

        // List<StatHistory> filteredHistory = new ArrayList<StatHistory>();
        for (StatHistory history : getHistoryList()) {
            if (history.getPath().toLowerCase().contains(path.toLowerCase())) {
                // filteredHistory.add(history);
                System.out.println(history.getPath());
            }
        }

    }

    @Command
    public void getHistoryByPath(String path) throws Exception{
        StatHistory result = null;
        for (StatHistory history : getHistoryList()) {
            if (history.getPath().equalsIgnoreCase(path)) {
                result = history;
                break;
            }
        }

        if (result != null) {
            Output.print(result);
        } else {
            System.out.println("stat history: " + path + " does not exist.");
        }
    }


    private List<StatHistory> loadHistory() throws Exception{
        String url = "https://" + ecs + ":4443/stat/aggregate_history";
        JSONArray jsonArray = getJSONArrayFromUrl(url);

        //
        // convert jsonarry to a list of StatHistory
        //
        List<StatHistory> histories = new ArrayList<StatHistory>();
        for (int i=0; i<jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            StatHistory history = new StatHistory(jsonObject.getString("treePathToPrimitive"));
            JSONArray snapshots = jsonObject.getJSONArray("snapshots");
            for (int j=0; j<snapshots.length(); j++) {
                JSONObject snapshot = snapshots.getJSONObject(j);
                try {
                    history.addSnapshot(new StatSnapshot(snapshot.getString("value"), snapshot.getLong("timestamp")));
                } catch (RuntimeException re) {
                    System.out.println(history.getPath() + ": " + re.getMessage());

                    System.out.println(snapshot);
                }
            }
            histories.add(history);
        }

        return histories;
    }

    private List<StatHistory> getHistoryList() throws Exception {
        if (historyList == null) {
            historyList = loadHistory();
        }

        return historyList;
    }

    private JSONObject getJSONObjectFromUrl(String url) throws Exception {
        InputStream is = UrlUtil.openUrlConnection(url);
        String response =  UrlUtil.convertStreamToString(is);
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject;
    }

    private JSONArray getJSONArrayFromUrl(String url) throws Exception {
        InputStream is = UrlUtil.openUrlConnection(url);
        String response =  UrlUtil.convertStreamToString(is);
        JSONArray array = new JSONArray(response);
        return array;
    }

}
