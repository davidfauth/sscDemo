package com.ssc.imports;

import com.ssc.schema.Labels;
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

public class CharacteristicRunnable implements Runnable {
    private static final int TRANSACTION_LIMIT = 1000;
    private String file;
    private GraphDatabaseAPI db;
    private Log log;

    public CharacteristicRunnable(String file, GraphDatabaseAPI db, Log log) {
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
            log.error("CharacteristicRunnable Import - File not found: " + file);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("CharacteristicRunnable Import - IO Exception: " + file);
        }

        Transaction tx = db.beginTx();
        try {
            int count = 0;

            assert records != null;
            for (CSVRecord record : records) {
                count++;
                String id = record.get("CHAR");
                Node characteristic = db.findNode(Labels.Characteristic, "id", id);
                if (characteristic == null) {
                    characteristic = db.createNode(Labels.Characteristic);
                    characteristic.setProperty("id", id);

                    String lang = record.get("LANGUAGE");
                    String desc = record.get("DESC");
                    characteristic.setProperty("lang-" + lang, desc);
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