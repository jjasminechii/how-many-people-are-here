package queryresponders;

import cse332.exceptions.NotYetImplementedException;
import cse332.interfaces.QueryResponder;
import cse332.types.CensusGroup;
import cse332.types.CornerFindingResult;
import cse332.types.MapCorners;
import paralleltasks.CornerFindingTask;
import paralleltasks.GetPopulationTask;

import java.util.concurrent.ForkJoinPool;

public class SimpleParallel extends QueryResponder {
    private static final ForkJoinPool POOL = new ForkJoinPool();
    private int numColumns;
    private int numRows;
    private CensusGroup[] censusData;
    private MapCorners map;
    private double columnFactor;
    private double rowFactor;

    public SimpleParallel(CensusGroup[] censusData, int numColumns, int numRows) {
        this.numColumns = numColumns;
        this.numRows = numRows;
        this.censusData = censusData;
        this.map = new MapCorners(censusData[0]);

        CornerFindingResult CFR = POOL.invoke(new CornerFindingTask(censusData, 0, censusData.length));

        totalPopulation = CFR.getTotalPopulation();
        map = CFR.getMapCorners();

        columnFactor = (map.north - map.south) / numRows;
        rowFactor = (map.east - map.west) / numColumns;

    }

    @Override
    public int getPopulation(int west, int south, int east, int north) {
        if(west < 1 || south < 1 || east < 1 || north < 1 || east < west || north < south ||
                west > numColumns || south > numRows || east > numColumns || north > numRows){
            throw new IllegalArgumentException();
        }
        double westVal = map.west + (west - 1) * rowFactor;
        double southVal = map.south + (south - 1) * columnFactor;
        double eastVal = map.west + east * rowFactor;
        double northVal = map.south + north * columnFactor;
        return POOL.invoke(new GetPopulationTask(censusData, 0, censusData.length,
                westVal, southVal, eastVal, northVal, map));

    }
}
