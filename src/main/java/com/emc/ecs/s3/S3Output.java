package com.emc.ecs.s3;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import de.vandermeer.asciitable.v2.V2_AsciiTable;

import java.util.List;

import static com.emc.ecs.util.Output.printV2AsciiTable;

/**
 * Created by zhengf1 on 1/30/17.
 */
public class S3Output {


    public static void printBuckets(List<Bucket> buckets) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("Name", "Owner", "Creation Date");
        table.addRule();
        for (Bucket bucket : buckets) {
            table.addRow(bucket.getName(),
                    bucket.getOwner().getDisplayName(),
                    bucket.getCreationDate());
        }
        table.addRule();
        printV2AsciiTable(table);
    }


    public static void printObjectSummaries(List<S3ObjectSummary> s3ObjectSummaries) {
        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        table.addRow("Bucket", "Key", "Owner", "Size", "Last Modified");
        table.addRule();
        for (S3ObjectSummary object : s3ObjectSummaries) {
            table.addRow(object.getBucketName(), object.getKey(),
                    object.getOwner().getDisplayName(),
                    object.getSize(), object.getLastModified());
        }
        table.addRule();
        printV2AsciiTable(table);
    }
}
