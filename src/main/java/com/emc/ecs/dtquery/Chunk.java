package com.emc.ecs.dtquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by zhengf1 on 10/24/16.
 */
public class Chunk {

    static public String BEGIN = "schemaType CHUNK";
    static public String STATUS = "status:";
    static public String DATA_TYPE = "dataType:";
    static public String TYPE = "type:";
    static public String CAPACITY = "capacity:";
    static public String SEALED_LENGTH = "sealedLength:";
    static public String REPO_CHUNK_TYPE = "repoChunkType:";
    static public String IS_EC_ENCODED = "isEcEncoded:";
    static public String PRIMARY = "primary:";

    static public String COPIES = "copies";
    static public String GEOCOPIES = "geoCopies";
    static public String ECCOPIES = "ecCopy";
    static public String COPY_IS_EC = "isEc:";


    public String type;
    public String status;
    public String dataType;

    public String id;
    public int partition;

    public long capacity;
    public long sealedLength;
    public boolean isEcEncoded;
    public String repoChunkType;
    public boolean copyIsEC;
    public String primary;

    // aux attributes
    public boolean hasEcEncodedAttribute;
    public boolean hasCopyIsEC;

    public List<COPY> copyList = new ArrayList<COPY>();
    public COPY currentCopy;


    public StringBuffer rawChunkStrings = new StringBuffer();

    public void parseLine(String line) {
        if (line == null || line.trim().length() == 0) {
            return;
        }

        try {
            line = line.trim();
            String[] tokens = line.split(" ");
            if (tokens[0].equals(STATUS)) {
                this.status = tokens[1].trim();
            } else if (tokens[0].equals(DATA_TYPE)) {
                this.dataType = tokens[1].trim();
            } else if (tokens[0].equals(TYPE)) {
                this.type = tokens[1].trim();
            } else if (tokens[0].equals(CAPACITY)) {
                this.capacity = Long.parseLong(tokens[1].trim());
            } else if (tokens[0].equals(SEALED_LENGTH)) {
                this.sealedLength = Long.parseLong(tokens[1].trim());
            } else if (tokens[0].equals(REPO_CHUNK_TYPE)) {
                this.repoChunkType = tokens[1].trim();
            } else if (tokens[0].equals(IS_EC_ENCODED)) {
                this.hasEcEncodedAttribute = true;
                this.isEcEncoded = Boolean.parseBoolean(tokens[1].trim());
            } else if (tokens[0].equals(PRIMARY)) {
                this.primary = tokens[1].trim();

                // for copies
            } else if (tokens[0].equals(COPIES) || tokens[0].equals(GEOCOPIES) || tokens[0].equals(ECCOPIES)) {
                currentCopy = new COPY();
                copyList.add(currentCopy);
            } else if (tokens[0].equals(COPY_IS_EC)) {
                this.hasCopyIsEC = true;
                this.copyIsEC = Boolean.parseBoolean(tokens[1].trim());
                currentCopy.isEced = this.copyIsEC;
            } else if (tokens[0].toLowerCase().contains("copy")) {
                rawChunkStrings.append("unrecognized copy: " + tokens[0]);
            }
        } catch (Exception e) {
            System.out.println("error when parsing line: " + line);
            System.out.println("====  current chunk ====");
            System.out.println(rawChunkStrings.toString());
            System.out.println("========================");
            e.printStackTrace();
            // System.exit(1);
        }
    }
}
