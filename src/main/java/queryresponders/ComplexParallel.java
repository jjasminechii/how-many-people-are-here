package queryresponders;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.QueryResponder;
import cse332.types.CensusGroup;
import cse332.types.CornerFindingResult;
import cse332.types.MapCorners;
import paralleltasks.CornerFindingTask;
import paralleltasks.PopulateGridTask;

import java.util.concurrent.ForkJoinPool;

public class ComplexParallel extends QueryResponder {
    private static final ForkJoinPool POOL = new ForkJoinPool();
    private int[][] array;
    private int numColumns;
    private int numRows;
    private MapCorners map;
    private double rowFactor;
    private double columnFactor;

    public ComplexParallel(CensusGroup[] censusData, int numColumns, int numRows) {
        this.numColumns = numColumns;
        this.numRows = numRows;
        CornerFindingResult CFR = POOL.invoke(new CornerFindingTask(censusData, 0, censusData.length));
        this.map = CFR.getMapCorners();
        totalPopulation = CFR.getTotalPopulation();
        rowFactor = (map.north - map.south) / numRows;
        columnFactor = (map.east - map.west) / numColumns;
        this.array = POOL.invoke(new PopulateGridTask(censusData, 0, censusData.length, numRows, numColumns,
                                map, columnFactor, rowFactor));
        for(int i = 1; i <= numRows; i++){
            for(int j = 1; j <= numColumns; j++){
                array[i][j] = array[i][j] + array[i - 1][j] + array[i][j - 1] - array[i - 1][j - 1];
            }
        }
    }

    @Override
    public int getPopulation(int west, int south, int east, int north) {
        if (west < 1 || south < 1 || east < 1 || north < 1 || east < west || north < south ||
                west > numColumns || south > numRows || east > numColumns || north > numRows) {
            throw new IllegalArgumentException();
        }
        return array[north][east] - array[south - 1][east] - array[north][west - 1] + array[south - 1][west - 1];
    }
}
