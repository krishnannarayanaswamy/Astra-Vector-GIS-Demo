package com.datastax.gisdemo.dataLoading;

import com.datastax.astra.sdk.AstraClient;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.data.CqlVector;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FauxFloatDataLoader implements IDataLoader {

    private static final int NUM_THREADS = 20; // Number of threads in the thread pool

    @Override
    public void loadData(AstraClient astraClient) {

        float baseLongitude = -3.000f;
        float increment = 0.001f;
        try {
        // We need to iterate every 0.001 - 3rd degree is about 111 metres but varies depending on location of course.
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            for (long iterator = 0; iterator <= 2000 ; iterator++ ) {
                float iterationLongitude = baseLongitude + (increment * iterator);
                System.out.println("Starting Longitude " + iterationLongitude);
                Callable<Void> task = new ProcessFauxFloatLineTask(astraClient, iterationLongitude);
                executor.submit(task);
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ProcessFauxFloatLineTask implements Callable<Void> {
    private float longitude;
    private AstraClient astraClient;

    public ProcessFauxFloatLineTask(AstraClient astraClient,  float longitude) {
        this.longitude = longitude;
        this.astraClient = astraClient;
    }

    @Override
    public Void call() {
        float baseLatitude = 52.000f;
        float increment = 0.001f;
        try {
            BatchStatementBuilder batchBuilder = BatchStatement.builder(DefaultBatchType.LOGGED);
            PreparedStatement insertCoords = astraClient.cqlSession().prepare("insert into demo.postcode_float(postcode, coord) values (?,?)");

            for (long iterator = 0; iterator <= 2000 ; iterator++ ) {
                float iterationLatitude = baseLatitude + (increment * iterator);
                String postcode = String.format("%.3f,%.3f", longitude, iterationLatitude);
                CqlVector<Float> vector = CqlVector.newInstance(longitude, iterationLatitude);
                batchBuilder.addStatements(insertCoords.bind(postcode, vector));
            }
            BatchStatement batch = batchBuilder.build();
            astraClient.cqlSession().execute(batch);
            System.out.println("Longitude : " + longitude + " inserted.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}