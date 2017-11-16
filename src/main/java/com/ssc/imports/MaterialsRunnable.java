package com.ssc.imports;

import com.ssc.schema.Labels;
import com.ssc.schema.RelationshipTypes;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class MaterialsRunnable implements Runnable {
    private static final int TRANSACTION_LIMIT = 1000;
    private String file;
    private GraphDatabaseAPI db;
    private Log log;

    public MaterialsRunnable(String file, GraphDatabaseAPI db, Log log) {
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
            log.error("Materials Import - File not found: " + file);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Materials Import - IO Exception: " + file);
        }

        Transaction tx = db.beginTx();
        try {
            int count = 0;

            assert records != null;
            for (CSVRecord record : records) {
                count++;
                String id = record.get("MATERIAL");
                String vartabId = record.get("VARTAB");
                Node material = db.findNode(Labels.Material, "id", id);
                if (material == null) {
                    material = db.createNode(Labels.Material);
                    material.setProperty("id", id);
                }

                Node variant = db.findNode(Labels.Variant, "id", vartabId);

                material.createRelationshipTo(variant, RelationshipTypes.HAS_VARIANT);

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