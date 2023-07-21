package com.datastax.gisdemo;

import com.datastax.gisdemo.dataLoading.FauxIntDataLoader;
import com.datastax.gisdemo.dataLoading.IDataLoader;
import com.datastax.gisdemo.dataLoading.RealDataLoader;
import com.datastax.gisdemo.dataLoading.FauxFloatDataLoader;
import com.datastax.gisdemo.model.PostCode;
import com.datastax.gisdemo.validation.FloatGridCircleCalculator;
import com.datastax.gisdemo.validation.IValidPointCalculator;
import com.datastax.gisdemo.validation.IntGridCircleCalculator;
import com.datastax.gisdemo.validation.Validation;
import com.datastax.astra.sdk.AstraClient;
import com.datastax.oss.driver.api.core.data.CqlVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@SpringBootApplication
public class GISDemoApplication {

    @Autowired
    private AstraClient astraClient;

    public static void main(String[] args) {
        SpringApplication.run(GISDemoApplication.class, args);
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @GetMapping("/getPostCodesRadius")
    public List<PostCode> getPostCodes(@RequestParam String latitude, @RequestParam String longitude, @RequestParam String mode) {
        CqlVector<Float> coords = CqlVector.newInstance(Float.parseFloat(longitude), Float.parseFloat(latitude));
        List<PostCode> returnedPostCodes = AstraDB.getPostCodesFromAstra(astraClient, mode, coords, 75);
        // from our testing, retrieving 75 and then filtering to the nearest 50 provides the correct answer.
        // calculate the distance for each point
        for (PostCode p : returnedPostCodes) {
            p.setDistance(Validation.calculateDistance(coords.get(0) , coords.get(1), p.getLongitude(), p.getLatitude()));
        }
        // sort the distances and return the first 50 - being the nearest 50
        returnedPostCodes.sort(Comparator.comparingDouble(PostCode::getDistance));
        return returnedPostCodes.subList(0,50);
    }

    @GetMapping("/runRandomChecks")
    public String randomChecker(@RequestParam String mode, @RequestParam int iterations, @RequestParam int limit) {
        IValidPointCalculator validator;
        switch (mode) {
            case "float_grid":
                validator = new FloatGridCircleCalculator();
                break;
            case "int_grid":
                validator = new  IntGridCircleCalculator();
                break;
            default:
                return "Invalid Mode - please use float_grid or int_grid for validation checks";
        }
        return Validation.runRandomChecks(astraClient, validator, iterations, limit);
    }

    @GetMapping("/loadPostCodes")
    public String dataLoad(@RequestParam String mode) {
        IDataLoader loader;
        switch (mode) {
            case "real":
                loader = new RealDataLoader();
                break;
            case "float_grid":
                loader = new FauxFloatDataLoader();
                break;
            case "int_grid":
                loader = new FauxIntDataLoader();
                break;
            default:
                return "Invalid Mode - please use real, float_grid or int_grid";
        }
        CompletableFuture.supplyAsync(() -> {
            loader.loadData(astraClient);
            return "Loaded Data";
        }).thenAccept(System.out::println);
        return "loading...";
    }
}
