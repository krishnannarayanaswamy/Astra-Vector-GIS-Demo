This is a piece of code to check the use of Vectors and Approximate Nearest Neighbour searching within DataStax Astra to perform geospatial 'nearest' queries. It is a thought experiment more than anything else.

# Setup

## Database Connectivity
This code requires an Astra Database, which is using the Vector capability. (https://docs.datastax.com/en/astra-serverless/docs/vector-search/overview.html)

The application.yaml file must be edited to contain your database id and application token
```
application-token: <your token>
database-id: <your db_id>
```

## MapBox Token
On the index.html page, you will need to supply your mapbox access token on this line:
```        
mapboxApiAccessToken: '<you mapbox token>',
```

## Schema
The schema.cql file contains the schema for the tables used in this code.

There are 3 tables postcode, postcode_float and postcode_int - the main difference between the tables being whether it is the real postcode data for the UK (postcode) and the centre point longitude, latitude values, or whether is is an artifical grid.

The float artifical grid is between -3.000 to -1.000 and 52.000 to 54.000 at an increment of 0.001 per point.

The int artifical grid uses int values for the same, so it is from -3000 to -1000 and 52000 to 54000,

Both exist so that a comparison of whether floating point mathematics vs longitude / latitude inputs which are often more precision interfere with the accuracy of the resturned result.

Manually deploy the schema to the databse.

# Data Load
There are 3 data loaders, 1 per table

* Real Postcide Data : http://localhost:8080/loadPostCodes?mode=real
* Faux Floating Point Grid : http://localhost:8080/loadPostCodes?mode=float_grid
* Faux Int Grid : http://localhost:8080/loadPostCodes?mode=int_grid

# Normal Use
In normal use, open http://localhost:8080/ in chrome, and it will show the map. Click anywhere in the UK, and the nearest postcodes for that location will show on the map as dots. By default it will be selecting 75 points from the database using ANN, calculating the distance, sorting and returning the clesst 50 to the web page.

To change the render to use the floating point fixed grid, change this line to say mode=float_grid, instead of real
```
fetch("http://localhost:8080/getPostCodesRadius?mode=real&longitude="+ info.coordinate[0] + "&latitude=" + info.coordinate[1])
```

## WebGL Errors
If Chrome gives you a WebGL error, ensure Chrome is up to date and paste the following to your chrome address bar:
```
chrome://settings/?search=hardware
```

The most likely cause is that hardware acceleration is not switch on and needs to be.

# Validation
There is a built-in validation mechanism to check the float_grid and int_grid correctness. Since HNSW is 'Approximate', and we are using floating point numbers, it is very easy for the edges of the circle when considering the radius to be slightly incorrect. The nature of ANN (opposed to KNN) means that we know the percentage chance that it will be correct is not always 100%. Selecting more points than required increases this percentage however, as the results later show.

The web service can be called to validate itself by generating a number of random points and checking the results vs the calculation of what should be returned. To run the validation, use a link like this:

http://localhost:8080/runRandomChecks?mode=float_grid&limit=50&iterations=10000

`mode` =`float_grid` or `int_grid` - depending on which you wish to check.
`limit` = limit clause placed on the query to Astra, the validation checks if you have the 50 nearest points, to achieve a good percentage correctness we must over select (see preliminary results next), change the limit to be 50 or higher for the test.
`iterations` = number of tests to run, it will generate this many random points to check and report back the results.

## Preliminary Validation Results
The test results should be taken with some context, the 'incorrectness' observed is always at the outside edge of the circle and not a point close to the focus of the search.

Floats - 10k tests.

| Limit | Incorrect Results |
| --- | ---|
| 50 | 9982 |
| 55 | 9140 |
| 60 | 4960 |
| 65 | 579 |
| 70 | 50 |
| 75 | 0 |

Ints - 10k tests

Limit | Incorrect Results |
| --- | ---|
| 50 | 8455 |
| 55 | 2274 |
| 60 | 0 |
| 65 | 0 |
| 70 | 0 |
| 75 | 0 | 


# Built With
* Spring Boot
* DataStax Driver 4.17
* Deck.GL