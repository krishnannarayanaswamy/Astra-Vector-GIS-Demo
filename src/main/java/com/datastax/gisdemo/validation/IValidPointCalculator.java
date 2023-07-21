package com.datastax.gisdemo.validation;

import com.datastax.gisdemo.model.Point;
import com.datastax.gisdemo.model.PostCode;

import java.util.List;

public interface IValidPointCalculator {

    public String mode();

    public List<Point> generateRandomPoints(int numberToGenerate);

    public boolean isResultValid(List<PostCode> postcodes, List<Point> correctResults);

    public List<Point> getPointsInRadius(float longitude, float latitude);

}
