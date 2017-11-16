# ssc Proof of Concept
POC for ssc

This project requires Neo4j 3.2.x

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/procedures-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/procedures-1.0-SNAPSHOT.jar neo4j-enterprise-3.2.5/plugins/.


Edit your Neo4j/conf/neo4j.conf file by adding this line:

    dbms.security.procedures.unrestricted=com.ssc.*    
    
(Re)start Neo4j

Create the schema:

    CALL com.ssc.schema.generate;

Import the data:

    CALL com.ssc.imports.characteristics("/Users/maxdemarzi/Projects/ssc_poc/imports/C_CHARACTERISTIC_DESC.csv");
    CALL com.ssc.imports.values("/Users/maxdemarzi/Projects/ssc_poc/imports/C_CHARACTERISTIC_VALUE_DESC.csv");    
    CALL com.ssc.imports.variants("/Users/maxdemarzi/Projects/ssc_poc/imports/C_VARIANT_TABLE.csv");
    CALL com.ssc.imports.materials("/Users/maxdemarzi/Projects/ssc_poc/imports/C_OD.csv");
    
If using Windows you must escape the slashes like so:

    CALL com.ssc.imports.characteristics('C:\\Users\\maxdemarzi\\Projects\\ssc_poc\\imports\\C_CHARACTERISTIC_DESC.csv');

Model:

    (Material)-[:HAS_VARIANT]->(Variant)-[:HAS_VALUE {active:true/false}]->(Value)-[:BELONGS_TO]->(Char)

Queries:

    // Get initial valid options
    CALL com.ssc.options(material)
    CALL com.ssc.options('70SQCTO1WW')

    // Validate options
    CALL com.ssc.validate('material', {options});
    CALL com.ssc.validate('70SQCTO1WW', {ISEHDD_TYPE_SELECTION_5:"MC_HS_480G_SATA6.0_SSD_2.5"});
    CALL com.ssc.validate('70SQCTO1WW', {ISEHDD_TYPE_SELECTION_5:"MC_HS_480G_SATA6.0_SSD_2.5",
                                            ISESP:"INTEL_XEON_E5_2650V2_2.6GHZ"});
    CALL com.ssc.validate('70SQCTO1WW', {ISEHDD_TYPE_SELECTION_5:"MC_HS_480G_SATA6.0_SSD_2.5",
                                            ISESP:"INTEL_XEON_E5_2650V2_2.6GHZ",
                                            BADCHAR:"WRONG_VALUE"});                                            

TODO : Add active     
 