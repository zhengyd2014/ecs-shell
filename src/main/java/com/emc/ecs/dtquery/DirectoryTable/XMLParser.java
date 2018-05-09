package com.emc.ecs.dtquery.DirectoryTable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengf1 on 10/25/16.
 */
public class XMLParser {

    public static List<Entry> parseChunkTable(InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(is);
        doc.getDocumentElement().normalize();

        List<Entry> result = new ArrayList<Entry>();
        NodeList nList = doc.getElementsByTagName("entry");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Entry entry = new Entry();
                Element eElement = (Element) nNode;
                entry.table_detail_link = eElement.getElementsByTagName("table_detail_link").item(0).getTextContent();
                entry.id = eElement.getElementsByTagName("id").item(0).getTextContent();
                entry.owner_ipaddress = eElement.getElementsByTagName("owner_ipaddress").item(0).getTextContent();
                result.add(entry);
            }
        }

        return result;
    }

}
