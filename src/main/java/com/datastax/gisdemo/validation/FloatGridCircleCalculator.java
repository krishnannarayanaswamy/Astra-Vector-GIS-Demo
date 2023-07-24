package com.datastax.gisdemo.validation;

import com.datastax.gisdemo.model.Point;
import com.datastax.gisdemo.model.PostCode;

import java.util.*;

public class FloatGridCircleCalculator implements IValidPointCalculator {

    @Override
    public String mode() {
        return "float_grid";
    }

    @Override
    public List<Point> generateRandomPoints(int numberToGenerate) {
        List<Point> values = new ArrayList<>();
        // The data loaded was -3.000 to -1.000 longitude and 52.000 to 54.000 latitude
        // to avoid edge of grid issues, we will move 200 points inwards for min / max
        double longitudeMin = -2.8;
        double longitudeMax = -1.2;
        double latitudeMin = 52.2;
        double latitudeMax = 53.8;

        Random random = new Random();
        for (int i=0; i < numberToGenerate ; i++) {
            double randomLongitude = longitudeMin + (random.nextDouble() * (longitudeMax - longitudeMin));
            double randomLatitude = latitudeMin + (random.nextDouble() * (latitudeMax - latitudeMin));
            values.add(new Point(0, (float)randomLongitude, (float)randomLatitude));
        }
        return values;
    }

    @Override
    public boolean isResultValid(List<PostCode> postcodes, List<Point> correctResults) {

        HashMap<String, PostCode> postcodeMap = new HashMap<>();
        // change the postcodes list to a map, since we know the postcode (long/lat) we are looking for
        for (PostCode postCode : postcodes) {
            postcodeMap.put(postCode.getPostCode(), postCode);
        }

        // the correct results are in distance order, so the first 50 correct results should all appear in the postcodes list
        for (int i =0; i< 50; i++) {
            String key = String.format("%.3f,%.3f", correctResults.get(i).getLongitude() , correctResults.get(i).getLatitude());
            if (!postcodeMap.containsKey(key)) {
                return false;
            }
        }
        return true;

    }

    @Override
    public List<Point> getPointsInRadius(float longitude, float latitude) {

        // The limit is 50, so to check what might be within the answer, we want to select a bounding box of let's
        // say 121 points, which would be a 11x11 grid. for ease, using an odd number, so an 11x11 grid, so that is 5 either
        // side of the origin in both X / Y dimensions
        float radius = 0.005f;

        // move to the bottom left part of the bounding box,
        float startingX = longitude - radius;
        float startingY = latitude - radius;
        float increment = 0.001f;
        List<Point> pointsInCircle = new ArrayList<>();

        // iteration 9 times in each direction, that means we get the 4 down, on the nose and 4 up
        for (int x = 0; x <= 11; x++) {
            for (int y= 0; y <= 11; y++) {
                double distance = Validation.calculateDistance(longitude, latitude, startingX + (increment * x), startingY + (increment * y));
                pointsInCircle.add(new Point(distance, startingX + (increment * x), startingY + (increment * y)));
            }
        }

        // at this stage we have all of the points in a grid around the origin point, and we have the distances, we need to know what the closest are, so we need to order the list
        Collections.sort(pointsInCircle, Comparator.comparingDouble(Point::getDistance));

        return pointsInCircle;
    }
}