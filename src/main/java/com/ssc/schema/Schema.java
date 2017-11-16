package com.ssc.schema;

import com.ssc.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.io.IOException;
import java.util.stream.Stream;

public class Schema {

    @Context
    public GraphDatabaseService db;


    @Procedure(name="com.lenovo.schema.generate",mode= Mode.SCHEMA)
    @Description("CALL com.lenovo.schema.generate() - generate schema")

    public Stream<StringResult> generate() throws IOException {
        org.neo4j.graphdb.schema.Schema schema = db.schema();
        if (!schema.getIndexes(Labels.Material).iterator().hasNext()) {
            schema.constraintFor(Labels.Material)
                    .assertPropertyIsUnique("id")
                    .create();
        }

        if (!schema.getIndexes(Labels.Variant).iterator().hasNext()) {
            schema.constraintFor(Labels.Variant)
                    .assertPropertyIsUnique("id")
                    .create();
        }

        if (!schema.getIndexes(Labels.Characteristic).iterator().hasNext()) {
            schema.constraintFor(Labels.Characteristic)
                    .assertPropertyIsUnique("id")
                    .create();
        }

        if (!schema.getIndexes(Labels.Value).iterator().hasNext()) {
            // TODO: 10/4/17 Create nodekey using Char and id
        }

        return Stream.of(new StringResult("Schema Generated"));
    }

}
