package com.datastax.gisdemo.validation;

import com.datastax.gisdemo.model.Point;
import com.datastax.gisdemo.model.PostCode;

import java.util.*;

public class IntGridCircleCalculator implements IValidPointCalculator {

    @Override
    public String mode() {
        return "int_grid";
    }

    @Override
    public List<Point> generateRandomPoints(int numberToGenerate) {
        List<Point> values = new ArrayList<>();
        // The data loaded was -3000 to -1000 longitude and 52000 to 54000 latitude
        // to avoid edge of grid issues, we will move 100 points inwards for min / max
        double longitudeMin = -2900;
        double longitudeMax = -1100;
        double latitudeMin = 52100;
        double latitudeMax = 53900;

        Random random = new Random();
        for (int i=0; i < numberToGenerate ; i++) {
            double randomLongitude = longitudeMin + (random.nextDouble() * (longitudeMax - longitudeMin));
            double randomLatitude = latitudeMin + (random.nextDouble() * (latitudeMax - latitudeMin));
            // casting to int to just get the number as a whole number, since that is how the vector is stored
            // and decimals really messes up the ability to consider correctness as a result.
            values.add(new Point(0, (int)randomLongitude, (int)randomLatitude));
        }
        return values;
    }

    public  boolean isResultValid(List<PostCode> postcodes, List<Point> correctResults) {

        HashMap<String, PostCode> postcodeMap = new HashMap<>();
        // change the postcodes list to a map, since we know the postcode (long/lat) we are looking for
        for (PostCode postCode : postcodes) {
            postcodeMap.put(postCode.getPostCode(), postCode);
        }

        // the correct results are in distance order, so the first 50 correct results should all appear in the postcodes list
        for (int i =0; i< 50; i++) {
            String key = (int)correctResults.get(i).getLongitude() + "," + (int)correctResults.get(i).getLatitude();
            if (!postcodeMap.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Point> getPointsInRadius(float longitude, float latitude) {
        int centerX = (int)longitude; // Center longitude of the circle
        int centerY = (int)latitude; // Center latitude of the circle

        // The limit is 50, so to check what might be within the answer, we want to select a bounding box of let's
        // say 121 points, which would be a 11x11 grid. for ease, using an odd number, so an 11x11 grid, so that is 5 either
        // side of the origin in both X / Y dimensions
        int radius = 5;

        // move to the bottom left part of the bounding box,
        int startingX = (int) longitude - radius;
        int startingY = (int) latitude - radius;
        List<Point> pointsInCircle = new ArrayList<>();

        // iteration 9 times in each direction, that means we get the 4 down, on the nose and 4 up
        for (int x = 0; x <= 11; x++) {
            for (int y= 0; y <= 11; y++) {
                double distance = Validation.calculateDistance(centerX, centerY, startingX + x, startingY + y);
                pointsInCircle.add(new Point(distance, startingX + x, startingY +  y));
            }
        }

        // at this stage we have all of the points in a grid around the origin point, and we have the distances, we need to know what the closest are, so we need to order the list
        Collections.sort(pointsInCircle, Comparator.comparingDouble(Point::getDistance));
        return pointsInCircle;
    }
}