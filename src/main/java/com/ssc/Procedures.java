package com.ssc;

import com.ssc.results.MapResult;
import com.ssc.schema.Labels;
import com.ssc.schema.RelationshipTypes;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory; 
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.FileWriter;
import java.io.StringWriter; 
import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.Map.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class Procedures {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    // Step 1: Create the Vector Nodes
    @Description("CALL com.lenovo.validate(material, options)")
    @Procedure(name = "com.lenovo.validate", mode = Mode.READ)
    public Stream<MapResult> Validate(@Name("material") String materialId, @Name("options") Map<String, Object> options) {
        Node material = db.findNode(Labels.Material, "id", materialId);
        HashMap<String, Object> results = new HashMap<>();
        HashMap<String, Object> validValues = new HashMap<>();
        HashMap<String, Object> invalidValues = new HashMap<>();

        // Verify material exists or return error.
        if (material == null) {
            return Stream.of(new MapResult(new HashMap<String, Object>() {{
                put("error", "Material: " + materialId + " not found");
            }}));
        } else {
            // Collect the valid options
            Set<String> valid = new HashSet<>();

            for (Relationship r : material.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_VARIANT)) {
                Node variant = r.getEndNode();
                for (Relationship r2 : variant.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_VALUE)){
                    boolean active = (boolean) r2.getProperty("active", true);
                    if (active) {
                        Node value = r2.getEndNode();
                        String valueId = (String) value.getProperty("id");
                        Node characteristic = value.getSingleRelationship(RelationshipTypes.BELONGS_TO, Direction.OUTGOING).getEndNode();
                        String characteristicId = (String) characteristic.getProperty("id");
                        valid.add(characteristicId + "-" + valueId);
                    }
                }
            }
            // Separate valid and invalid entries
            for (Map.Entry<String, Object> entry : options.entrySet()) {
                String check = entry.getKey() + "-" + entry.getValue().toString();
                if (valid.contains(check)) {
                    validValues.put(entry.getKey(), entry.getValue().toString());
                } else {
                    invalidValues.put(entry.getKey(), entry.getValue().toString());
                }
            }

            results.put("valid", validValues);
            results.put("invalid", invalidValues);
        }

        // Return results
        return Stream.of(new MapResult(results));
    }


    // Step 1: Create the Vector Nodes
    @Description("CALL com.lenovo.options(material, options)")
    @Procedure(name = "com.lenovo.options", mode = Mode.READ)
    public Stream<StringResult> Options(@Name("material") String materialId, @Name("options") Map<String, String> options) {
		JsonFactory jsonfactory = new JsonFactory();
		Writer writer = new StringWriter();
		String json = null;
	
        Node material = db.findNode(Labels.Material, "id", materialId);
        HashMap<String, Object> results = new HashMap<>();
		String materialID = (String)  material.getProperty("id");
		HashMap<String, List<String>> variantMap = new HashMap<String, List<String>>();
		HashMap<String, String> variantValueMap = new HashMap<String, String>();
		String tmpString = "";
        // Verify material exists or return error.
        if (material == null) {
            return Stream.of(new StringResult(json));
        } else {
            // Collect the valid options
			List<String> arraylist1 = new ArrayList<String>();
			List<String> variantValueList = new ArrayList<String>();
			
			int i = 0;
            for (Relationship r : material.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_VARIANT)) {
                Node variant = r.getEndNode();
				
				arraylist1.add(((String) variant.getProperty("id")));
				String strCurrentValue = (String) variant.getProperty("id");
				
				if (options.get(strCurrentValue) !=null){
					tmpString = options.get(strCurrentValue);
				}else{
					for (Relationship rHasValue : variant.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_VALUE)) {
						boolean active = (boolean) rHasValue.getProperty("active", true);
	                    if (active) {
							Node variantValue = rHasValue.getEndNode();
							variantValueList.add(((String) variantValue.getProperty("id")));
							tmpString = tmpString + "\"" + (String) variantValue.getProperty("id") + ",\"";
						}
					}
				}
				variantValueMap.put((String) variant.getProperty("id"),tmpString);
				tmpString = "";
				variantValueList.clear();
            }
			arraylist1.sort(String::compareToIgnoreCase);
			variantMap.put(materialID,arraylist1);
			
        }
		Iterator<String> countryIterator = options.keySet().iterator();
		while (countryIterator.hasNext()) {
		    String code = countryIterator.next();
		    System.out.println(code);
		}

		try {
				
		        JsonGenerator jsonGenerator = jsonfactory.createJsonGenerator(writer);
		        jsonGenerator.writeStartObject();
		        jsonGenerator.writeArrayFieldStart("Material");
		        jsonGenerator.writeStartObject();
				jsonGenerator.writeArrayFieldStart("Options");
				for (Map.Entry<String, String> entry : variantValueMap.entrySet()) {
					String check = "\"" + entry.getKey() + "\":" + entry.getValue().toString();
					jsonGenerator.writeString(check);
				}
		        jsonGenerator.writeEndArray();
		        jsonGenerator.writeEndObject();
		
				jsonGenerator.writeEndArray();
		        jsonGenerator.writeEndObject();
		        jsonGenerator.close();
		        json = writer.toString();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
        // Return results
        return Stream.of(new StringResult(json));
    }

    }
