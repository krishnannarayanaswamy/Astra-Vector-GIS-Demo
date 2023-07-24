package com.datastax.gisdemo.validation;

import com.datastax.gisdemo.model.Point;
import com.datastax.gisdemo.model.PostCode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class RealPostCodesCircleCalculator implements IValidPointCalculator {

    List<PostCode> realPostCodes = new ArrayList<>();

    public RealPostCodesCircleCalculator() {
        System.out.println("Loading real postcodes to memory");
        String resourcePath = "ukpostcodes.csv";

        // Open the resource file using the class loader
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);

        // Check if the resource file was found
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            // read header line
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineDetails = line.trim().split(",");
                // skip non-geographic post codes (there are some, such as military ones)
                if (lineDetails.length == 4 ) {
                    realPostCodes.add(new PostCode(lineDetails[1],Float.parseFloat(lineDetails[3]), Float.parseFloat(lineDetails[2])));
                }
            }
            System.out.println("Loaded : " + realPostCodes.size() + " postcodes.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String mode() {
        return "real_grid";
    }

    @Override
    public List<Point> generateRandomPoints(int numberToGenerate) {
        List<Point> values = new ArrayList<>();
        // The data loaded was -3000 to -1000 longitude and 52000 to 54000 latitude
        // to avoid edge of grid issues, we will move 100 points inwards for min / max
        double longitudeMin = -2.8;
        double longitudeMax = -1.2;
        double latitudeMin = 52.2;
        double latitudeMax = 53.8;

        Random random = new Random();
        for (int i = 0; i < numberToGenerate; i++) {
            double randomLongitude = longitudeMin + (random.nextDouble() * (longitudeMax - longitudeMin));
            double randomLatitude = latitudeMin + (random.nextDouble() * (latitudeMax - latitudeMin));
            // casting to int to just get the number as a whole number, since that is how the vector is stored
            // and decimals really messes up the ability to consider correctness as a result.
            values.add(new Point(0, (float)randomLongitude, (float)randomLatitude));
        }
        return values;
    }

    public boolean isResultValid(List<PostCode> postcodes, List<Point> correctResults) {

        HashMap<String, PostCode> postcodeMap = new HashMap<>();
        // change the postcodes list to a map, since we know the postcode (long/lat) we are looking for
        for (PostCode postCode : postcodes) {
            postcodeMap.put(postCode.getPostCode(), postCode);
        }

        // the correct results are in distance order, so the first 50 correct results should all appear in the postcodes list
        for (Point point : correctResults) {
   //         System.out.println("Correct Result : " + point.getLongitude() + " , " + point.getLatitude());
            Boolean found = false;
            for (PostCode postcode : postcodes) {
      //          System.out.println("Checking : " + postcode.getLongitude() + " , " + postcode.getLatitude());
                if (point.getLongitude() == postcode.getLongitude() && point.getLatitude() == postcode.getLatitude()) {
                    // match found, continue - while we find matches, we do not trigger the return false.
                    found = true;
                    break;
                }
            }
            if (!found) {
                // we got to the end with no match, must be an incorrect response
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Point> getPointsInRadius(float longitude, float latitude) {
        List<Point> correctPoints = new ArrayList();
        for (PostCode p : realPostCodes) {
            correctPoints.add(new Point(Validation.calculateDistance(longitude, latitude, p.getLongitude(), p.getLatitude()), p.getLongitude(), p.getLatitude()));
        }
        Collections.sort(correctPoints, Comparator.comparingDouble(Point::getDistance));
        return correctPoints.subList(0,50);
    }
}