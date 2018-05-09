package com.emc.ecs.dtquery;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengf1 on 10/24/16.
 */
public class ChunkParser {

    List<Chunk> chunkList = new ArrayList<Chunk>();

    public List<Chunk> getChunkList() {
        return this.chunkList;
    }


    public void parseInput(InputStream is, int partition) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        parseBufferedReader(reader, partition);
    }

    public void parseFile(String file, int partition) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        parseBufferedReader(reader, partition);
    }

    public void parseBufferedReader(BufferedReader reader, int partition) throws Exception {
        String sCurrentLine = null;
        Chunk currentChunk = null;
        while ((sCurrentLine = reader.readLine()) != null) {

            // add new check into chunk list
            if (sCurrentLine.contains(Chunk.BEGIN)) {
                currentChunk = new Chunk();
                currentChunk.partition = partition;
                //currentChunk.rawChunkStrings.append(sCurrentLine);
                chunkList.add(currentChunk);
                continue;
            }

            if (currentChunk != null) {
                //currentChunk.rawChunkStrings.append(sCurrentLine);
                currentChunk.parseLine(sCurrentLine);
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("total chunk nubmer: ").append(chunkList.size()).append("\n");

        return sb.toString();
    }


    public static void main(String[] args) throws Exception {
        ChunkParser parser = new ChunkParser();
        parser.parseFile("chunks.txt", 1);
        System.out.println(parser);

    }
}
