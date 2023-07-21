package com.datastax.gisdemo.dataLoading;

import com.datastax.astra.sdk.AstraClient;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.data.CqlVector;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FauxIntDataLoader implements IDataLoader {

    private static final int NUM_THREADS = 20; // Number of threads in the thread pool

    @Override
    public void loadData(AstraClient astraClient) {

        int baseLongitude = -3000;
        try {
            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
            for (int iterator = 0; iterator <= 2000 ; iterator++ ) {
                int iterationLongitude = baseLongitude + iterator;
                Callable<Void> task = new ProcessFauxIntLineTask(astraClient, iterationLongitude);
                executor.submit(task);
            }
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ProcessFauxIntLineTask implements Callable<Void> {
    private int longitude;
    private AstraClient astraClient;

    public ProcessFauxIntLineTask(AstraClient astraClient,  int longitude) {
        this.longitude = longitude;
        this.astraClient = astraClient;
    }

    @Override
    public Void call() {
        int baseLatitude = 52000;
        try {
            BatchStatementBuilder batchBuilder = BatchStatement.builder(DefaultBatchType.LOGGED);
            PreparedStatement insertCoords = astraClient.cqlSession().prepare("insert into demo.postcode_int(postcode, coord) values (?,?)");

            for (int iterator = 0; iterator <= 2000 ; iterator++ ) {
                int iterationLatitude = baseLatitude + iterator;
                String postcode = longitude + "," + iterationLatitude;
                CqlVector<Float> vector = CqlVector.newInstance((float)longitude, (float) iterationLatitude);
                batchBuilder.addStatements(insertCoords.bind(postcode, vector));
            }
            BatchStatement batch =batchBuilder.build();
            astraClient.cqlSession().execute(batch);
            System.out.println("Longitude : " + longitude + " inserted.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}