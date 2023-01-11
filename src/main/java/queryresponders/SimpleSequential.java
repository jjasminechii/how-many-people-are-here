package queryresponders;

import processing.interfaces.QueryResponder;
import processing.types.CensusGroup;
import processing.types.MapCorners;

public class SimpleSequential extends QueryResponder {
    private int numColumns;
    private int numRows;
    private CensusGroup[] censusData;
    private MapCorners map;
    private double columnFactor;
    private double rowFactor;

    public SimpleSequential(CensusGroup[] censusData, int numColumns, int numRows) {
        this.numColumns = numColumns;
        this.numRows = numRows;
        this.censusData = censusData;
        this.map = new MapCorners(censusData[0]);
        for(CensusGroup data : censusData){
            totalPopulation += data.population;
            this.map = map.encompass(new MapCorners(data));
        }
        columnFactor = (map.north - map.south) / numRows;
        rowFactor = (map.east - map.west) / numColumns;
    }

    @Override
    public int getPopulation(int west, int south, int east, int north) {
        if(west < 1 || south < 1 || east < 1 || north < 1 || east < west || north < south ||
                west > numColumns || south > numRows || east > numColumns || north > numRows){
            throw new IllegalArgumentException();
        }
        int population = 0;
        double lat;
        double lon;
        double westVal = map.west + (west - 1) * rowFactor;
        double southVal = map.south + (south - 1) * columnFactor;
        double eastVal = map.west + east * rowFactor;
        double northVal = map.south + north * columnFactor;
        for(CensusGroup data : censusData){
            lat = data.latitude;
            lon = data.longitude;
            if(westVal < lon && southVal < lat && northVal > lat && eastVal > lon){
                population += data.population;
            }
            else if(westVal == lon && (southVal <= lat && northVal > lat)){
                population += data.population;
            }
            else if(southVal == lat && (westVal <= lon && eastVal > lon) ){
                population += data.population;
            }
            else if(eastVal == lon && (southVal <= lat && northVal > lat) && lon == map.east){
                population += data.population;
            }
            else if(northVal == lat && (westVal <= lon && eastVal > lon) && lat == map.north){
                population += data.population;
            }
            else if(lat == northVal && lon == eastVal && lat == map.north && lon == map.east){
                population += data.population;
            }
        }
        return population;
    }
}
