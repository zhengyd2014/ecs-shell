package com.emc.ecs.dtquery.DirectoryTable;

import com.emc.ecs.util.UrlUtil;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by zhengf1 on 10/25/16.
 */
public class DirectoryTable {

    public String endpoint;

    List<Entry> chunkTableLevel1;
    List<Entry> chunkTableLevel2;

    public DirectoryTable(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<Entry> getChunkTableLevel1() throws Exception {
        if (chunkTableLevel1 != null) {
            return chunkTableLevel1;
        }

        // to-do: load from file if stored the ct locally

        String urlToRead = "http://" + endpoint + ":9101/diagnostic/CT/1";
        chunkTableLevel1 = XMLParser.parseChunkTable(UrlUtil.openUrlConnection(urlToRead));
        return chunkTableLevel1;
    }

    public List<Entry> getChunkTableLevel2() throws Exception {
        if (chunkTableLevel2 != null) {
            return chunkTableLevel2;
        }

        String urlToRead = "http://" + endpoint + ":9101/diagnostic/CT/2";
        chunkTableLevel2 = XMLParser.parseChunkTable(UrlUtil.openUrlConnection(urlToRead));
        return chunkTableLevel2;
    }

    public static void main(String[] args) throws Exception
    {
        DirectoryTable dt = new DirectoryTable("10.243.85.28");
        System.out.println(dt.getChunkTableLevel1());
    }
}
