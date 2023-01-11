package queryresponders;

import processing.interfaces.QueryResponder;
import processing.types.CensusGroup;
import processing.types.MapCorners;

public class ComplexSequential extends QueryResponder {
    private int[][] array;
    private int numColumns;
    private int numRows;
    private CensusGroup[] censusData;
    private MapCorners map;
    private double rowFactor;
    private double columnFactor;

    public ComplexSequential(CensusGroup[] censusData, int numColumns, int numRows) {
        this.numColumns = numColumns;
        this.numRows = numRows;
        this.censusData = censusData;
        this.map = new MapCorners(censusData[0]);
        array = new int[numRows + 1][numColumns + 1];
        for (CensusGroup data : censusData) {
            totalPopulation += data.population;
            this.map = map.encompass(new MapCorners(data));
        }
        rowFactor = (map.north - map.south) / numRows;
        columnFactor = (map.east - map.west) / numColumns;
        for (CensusGroup data : censusData) {
            int currentRow = (int) ((data.latitude - map.south) / rowFactor);
            int currentColumn = (int) ((data.longitude - map.west) / columnFactor);
            if (currentColumn == numColumns) {
                currentColumn -= 1;
            }
            if (currentRow == numRows) {
                currentRow -= 1;
            }
            array[currentRow + 1][currentColumn + 1] += data.population;
        }
        for (int j = 1; j < array[0].length; j++) {
            for (int i = 1 ; i < array.length; i++) {
                array[i][j] = array[i][j] + array[i][j-1] + array[i - 1][j] - array[i - 1][j - 1];
            }
        }
    }

    @Override
    public int getPopulation(int west, int south, int east, int north) {
        if (west < 1 || south < 1 || east < 1 || north < 1 || east < west || north < south ||
                west > numColumns || south > numRows || east > numColumns || north > numRows) {
            throw new IllegalArgumentException();
        }
        int topLeft;
        int bottomRight;
        int bottomLeftLeft;
        int topRight = array[north][east];
        topLeft = array[north][west - 1];
        bottomRight = array[south - 1][east];
        bottomLeftLeft = array[south - 1][west - 1];
        return topRight - topLeft - bottomRight + bottomLeftLeft;
    }
}


