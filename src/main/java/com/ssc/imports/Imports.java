package com.ssc.imports;

import com.ssc.results.StringResult;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public class Imports {
    @Context
    public GraphDatabaseAPI db;

    @Context
    public Log log;

    @Procedure(name = "com.ssc.imports.materials", mode = Mode.WRITE)
    @Description("CALL com.ssc.imports.materials(file)")
    public Stream<StringResult> importMaterials(@Name("file") String file) throws InterruptedException {
        long start = System.nanoTime();

        Thread t1 = new Thread(new MaterialsRunnable(file, db, log));
        t1.start();
        t1.join();

        return Stream.of(new StringResult("Materials imported in " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start) + " seconds"));
    }

    @Procedure(name = "com.ssc.imports.variants", mode = Mode.WRITE)
    @Description("CALL com.ssc.imports.variants(file)")
    public Stream<StringResult> importVariants(@Name("file") String file) throws InterruptedException {
        long start = System.nanoTime();

        Thread t1 = new Thread(new VariantsRunnable(file, db, log));
        t1.start();
        t1.join();

        return Stream.of(new StringResult("Variants imported in " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start) + " seconds"));
    }

    @Procedure(name = "com.lenovo.imports.characteristics", mode = Mode.WRITE)
    @Description("CALL com.lenovo.imports.characteristics(file)")
    public Stream<StringResult> importCharacteristic(@Name("file") String file) throws InterruptedException {
        long start = System.nanoTime();

        Thread t1 = new Thread(new CharacteristicRunnable(file, db, log));
        t1.start();
        t1.join();

        return Stream.of(new StringResult("Characteristic imported in " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start) + " seconds"));
    }

    @Procedure(name = "com.lenovo.imports.values", mode = Mode.WRITE)
    @Description("CALL com.lenovo.imports.values(file)")
    public Stream<StringResult> importValues(@Name("file") String file) throws InterruptedException {
        long start = System.nanoTime();

        Thread t1 = new Thread(new ValuesRunnable(file, db, log));
        t1.start();
        t1.join();

        return Stream.of(new StringResult("Values imported in " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start) + " seconds"));
    }

}
