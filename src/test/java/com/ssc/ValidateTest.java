package com.ssc;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class ValidateTest {

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withProcedure(Procedures.class);

    @Test
    public void shouldValidate() {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY);
        HashMap<String, Object> row = getResultRow(response);

        assertEquals(ANSWER_SET, row);
    }

    private static final HashMap<String, Object> QUERY = new HashMap<String, Object>(){{
        put("statements", new ArrayList<Map<String, Object>>() {{
            add(new HashMap<String, Object>() {{
                put("statement", "CALL com.ssc.validate('server1', {cpu:'intel', memory:'12gb'}) YIELD value RETURN value");
            }});
        }});
    }};

    private static final String MODEL_STATEMENT =
            "CREATE (m:Material {id:'server1'})" +
                    "CREATE (v1:Variant {id:'cpu1'})" +
                    "CREATE (v2:Variant {id:'memory1'})" +
                    "CREATE (l1a:Value {id:'intel'})" +
                    "CREATE (l1b:Value {id:'amd'})" +
                    "CREATE (l2a:Value {id:'8gb'})" +
                    "CREATE (l2b:Value {id:'16gb'})" +
                    "CREATE (c1:Characteristic {id:'cpu'})" +
                    "CREATE (c2:Characteristic {id:'memory'})" +

                    "CREATE (m)-[:HAS_VARIANT]->(v1)" +
                    "CREATE (m)-[:HAS_VARIANT]->(v2)" +
                    "CREATE (v1)-[:HAS_VALUE {sequences:['1']}]->(l1a)" +
                    "CREATE (v1)-[:HAS_VALUE {sequences:['2']}]->(l1b)" +
                    "CREATE (v2)-[:HAS_VALUE {sequences:['1']}]->(l2a)" +
                    "CREATE (v2)-[:HAS_VALUE {sequences:['2']}]->(l2b)" +
                    "CREATE (l1a)-[:BELONGS_TO]->(c1)" +
                    "CREATE (l1b)-[:BELONGS_TO]->(c1)" +
                    "CREATE (l2a)-[:BELONGS_TO]->(c2)" +
                    "CREATE (l2b)-[:BELONGS_TO]->(c2)" ;



    private static final HashMap<String, Object>  ANSWER_SET = new HashMap<String, Object> (){{
        put("valid", new HashMap<String, Object>() {{ put("cpu", "intel"); }});
        put("invalid", new HashMap<String, Object>() {{ put("memory", "12gb"); }});
    }};


    static HashMap<String, Object> getResultRow(HTTP.Response response) {
        Map<String, ArrayList<HashMap<String, ArrayList<Map>>>> actual = response.content();
        ArrayList<HashMap<String, ArrayList<Map>>> results = actual.get("results");
        HashMap<String, ArrayList<Map>> result = results.get(0);
        ArrayList<Map> data = result.get("data");
        ArrayList<Map> row = (ArrayList<Map>)data.get(0).get("row");
        return (HashMap<String, Object>)row.get(0);
    }
}
