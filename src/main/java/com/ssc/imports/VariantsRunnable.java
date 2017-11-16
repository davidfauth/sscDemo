package com.ssc.imports;

import com.ssc.schema.Labels;
import com.ssc.schema.RelationshipTypes;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

public class VariantsRunnable implements Runnable {
    private static final int TRANSACTION_LIMIT = 1000;
    private String file;
    private GraphDatabaseAPI db;
    private Log log;

    public VariantsRunnable(String file, GraphDatabaseAPI db, Log log) {
        this.file = file;
        this.db = db;
        this.log = log;
    }

    @Override
    public void run() {
        Reader in;
        Iterable<CSVRecord> records = null;
        try {
            in = new FileReader("/" + file);
            records = CSVFormat.EXCEL.withHeader().parse(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error("Variants Import - File not found: " + file);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Variants Import - IO Exception: " + file);
        }

        Transaction tx = db.beginTx();
        try {
            int count = 0;

            assert records != null;
            for (CSVRecord record : records) {
                count++;
                String id = record.get("VARTAB");
                String charId = record.get("CHAR");
                String valueId = record.get("CHARVALUE");
                Integer sequence = Integer.valueOf(record.get("SEQUENCE"));

                Node variant = db.findNode(Labels.Variant, "id", id);
                if (variant == null) {
                    variant = db.createNode(Labels.Variant);
                    variant.setProperty("id", id);
                }

                HashMap<Node, Relationship> values = new HashMap<>();
                variant.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_VALUE)
                        .forEach(e -> values.put(e.getEndNode(), e));


                Node characteristic = db.findNode(Labels.Characteristic, "id", charId);
                for (Relationship r : characteristic.getRelationships(Direction.INCOMING, RelationshipTypes.BELONGS_TO)) {
                    Node value = r.getStartNode();
                    String valueId2 = (String) value.getProperty("id");
                    if (valueId2.equals(valueId)) {
                        if (values.keySet().contains(value)) {
                            Relationship r2 = values.get(value);
                            int[] sequences = (int[])r2.getProperty("sequences");
                            int[] newSequences = new int[sequences.length + 1];
                            System.arraycopy(sequences, 0, newSequences, 0, sequences.length);
                            newSequences[sequences.length] = sequence;
                            r2.setProperty("sequences", newSequences);
                        } else {
                            Relationship r2 = variant.createRelationshipTo(value, RelationshipTypes.HAS_VALUE);
                            r2.setProperty("sequences", new int[]{ sequence });
                        }
                    }
                }

                if (count % TRANSACTION_LIMIT == 0) {
                    tx.success();
                    tx.close();
                    tx = db.beginTx();
                }
            }

            tx.success();
        } finally {
            tx.close();
        }

    }
}