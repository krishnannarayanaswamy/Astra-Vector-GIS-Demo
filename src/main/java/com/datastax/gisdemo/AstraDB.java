package com.datastax.gisdemo;

import com.datastax.gisdemo.model.PostCode;
import com.datastax.astra.sdk.AstraClient;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.data.CqlVector;

import java.util.ArrayList;
import java.util.List;

public class AstraDB {

    public static List<PostCode> getPostCodesFromAstra(AstraClient astraClient, String mode, CqlVector<Float> coords, int limit) {
        List<PostCode> postCodeList = new ArrayList<>();
        String tableName;
        switch (mode) {
            case "float_grid":
                tableName = "postcode_float";
                break;
            case "int_grid":
                tableName = "postcode_int";
                break;
            default:
                tableName = "postcode";
        }
        String query = "select postcode, coord, similarity_euclidean(coord, ?) from demo." + tableName + " order by coord ann of ? limit " + limit;
        ResultSet results = astraClient.cqlSession().execute(query , coords, coords);
        if (results.getAvailableWithoutFetching() > 0) {
            for (Row row : results) {
                CqlVector<Float> coord = row.getVector("coord", Float.class);
                postCodeList.add(new PostCode(row.getString("postcode"), coord.get(0), coord.get(1)));
            }
        }
        return postCodeList;
    }
}
