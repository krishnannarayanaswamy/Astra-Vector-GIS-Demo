package com.datastax.gisdemo.validation;

import com.datastax.gisdemo.AstraDB;
import com.datastax.gisdemo.model.Point;
import com.datastax.gisdemo.model.PostCode;
import com.datastax.astra.sdk.AstraClient;
import com.datastax.oss.driver.api.core.data.CqlVector;

import java.util.List;

public class Validation {

    public static String runRandomChecks(AstraClient astraClient, IValidPointCalculator validator, int iterations, int limit) {
        int totalCallsMade=0;
        int incorrectResults = 0;

        List<Point> pointsToCheck = validator.generateRandomPoints(iterations);

        for (Point p : pointsToCheck) {
            // go get the results from the DB for this point to check
            CqlVector<Float> coords = CqlVector.newInstance(p.getLongitude(), p.getLatitude());
            List<PostCode> results = AstraDB.getPostCodesFromAstra(astraClient, validator.mode(), coords, limit);
            // generate the correct results for the point
            List<Point> correctResults = validator.getPointsInRadius(p.getLongitude(), p.getLatitude());
            // check and add to totals
            boolean isCorrect = validator.isResultValid(results, correctResults);
            if (!isCorrect) {
                incorrectResults++;
            }
            totalCallsMade++;
            System.out.println("Calls : "+totalCallsMade +" Incorrect Results : "+incorrectResults);
        }
        return "Calls : "+totalCallsMade +" Incorrect Results : "+incorrectResults;
    }

    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}