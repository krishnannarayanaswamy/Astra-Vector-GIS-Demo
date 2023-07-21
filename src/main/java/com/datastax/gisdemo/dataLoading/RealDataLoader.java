package com.datastax.gisdemo.dataLoading;

import com.datastax.astra.sdk.AstraClient;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.data.CqlVector;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RealDataLoader implements IDataLoader {

    private static final int NUM_THREADS = 20; // Number of threads in the thread pool

    @Override
    public void loadData(AstraClient astraClient) {

             String resourcePath = "ukpostcodes.csv";

            // Open the resource file using the class loader
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);

            // Check if the resource file was found
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                PreparedStatement insertCoords = astraClient.cqlSession().prepare("insert into demo.postcode(postcode, coord) values (?,?)");

                // read header line
                reader.readLine();

                ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

                List<String> linesBatch = new ArrayList<>();
                String line;

                int batchSize = 100;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    linesBatch.add(line);
                    count++;
                    if (count % batchSize == 0) {
                        // have to send in a copy of the list, since it is being modified outside of the thread
                        Callable<Void> task = new ProcessLineTask(astraClient, new ArrayList<String>(linesBatch), insertCoords);
                        executor.submit(task);
                        linesBatch.clear();
                    }

                }
                // Process any remaining lines (if the total number of lines is not a multiple of 100)
                if (!linesBatch.isEmpty()) {
                    Callable<Void> task = new ProcessLineTask(astraClient, linesBatch, insertCoords);
                    executor.submit(task);
                }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ProcessLineTask implements Callable<Void> {
    private List<String> lines;
    private AstraClient astraClient;
    PreparedStatement insertCoords;

    public ProcessLineTask(AstraClient astraClient,  List<String> lines,  PreparedStatement insertCoords) {
        this.lines = lines;
        this.astraClient = astraClient;
        this.insertCoords = insertCoords;
    }

    @Override
    public Void call() {

        try {
            BatchStatementBuilder batchBuilder = BatchStatement.builder(DefaultBatchType.LOGGED);
            PreparedStatement insertCoords = astraClient.cqlSession().prepare("insert into demo.postcode(postcode, coord) values (?,?)");
            for (String line : lines) {
                String[] lineDetails = line.trim().split(",");
                // skip non-geographic post codes (there are some, such as military ones)
                if (lineDetails.length == 4 ) {
                    float longitude = Float.parseFloat(lineDetails[3]);
                    float latitude = Float.parseFloat(lineDetails[2]);
                    CqlVector<Float> vector = CqlVector.newInstance(longitude, latitude);
                    batchBuilder.addStatements(insertCoords.bind(lineDetails[1], vector));
                }
            }
            BatchStatement batch =batchBuilder.build();
            astraClient.cqlSession().execute(batch);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


