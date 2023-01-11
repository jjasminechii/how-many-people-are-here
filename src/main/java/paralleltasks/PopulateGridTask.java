package paralleltasks;

import processing.exceptions.NotYetImplementedException;
import processing.types.CensusGroup;
import processing.types.MapCorners;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
/*
   1) This class is used in version 4 to create the initial grid holding the total population for each grid cell
   2) SEQUENTIAL_CUTOFF refers to the maximum number of census groups that should be processed by a single parallel task
   3) Note that merging the grids from the left and right subtasks should NOT be done in this class.
      You will need to implement the merging in parallel using a separate parallel class (MergeGridTask.java)
 */

public class PopulateGridTask extends RecursiveTask<int[][]> {
    final static int SEQUENTIAL_CUTOFF = 10000;
    private static final ForkJoinPool POOL = new ForkJoinPool();
    CensusGroup[] censusGroups;
    int lo, hi, numRows, numColumns;
    MapCorners corners;
    double cellWidth, cellHeight;

    public PopulateGridTask(CensusGroup[] censusGroups, int lo, int hi, int numRows, int numColumns, MapCorners corners, double cellWidth, double cellHeight) {
        this.censusGroups = censusGroups;
        this.lo = lo;
        this.hi = hi;
        this.numRows = numRows;
        this.numColumns = numColumns;
        this.corners = corners;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    @Override
    protected int[][] compute() {
        if(hi - lo <= SEQUENTIAL_CUTOFF){
            return sequentialPopulateGrid();
        }
        int mid = lo + (hi - lo) / 2;
        PopulateGridTask leftTask = new PopulateGridTask(censusGroups, lo, mid, numRows, numColumns,
                                        corners, cellWidth, cellHeight);
        PopulateGridTask rightTask = new PopulateGridTask(censusGroups, mid, hi, numRows, numColumns,
                corners, cellWidth, cellHeight);
        leftTask.fork();
        int[][] rightMap = rightTask.compute();
        int[][] leftMap = leftTask.join();
        POOL.invoke(new MergeGridTask(leftMap, rightMap, 1, numRows, 1, numColumns + 1));
        return leftMap;
    }

    private int[][] sequentialPopulateGrid() {
        int[][]map = new int[numRows + 1][numColumns + 1];
        double lat;
        double lon;
        int col;
        int row;
        for(int i = lo; i < hi; i++){
            lat = censusGroups[i].latitude;
            lon = censusGroups[i].longitude;
            if(lat == corners.north){
                row = numRows;
            }
            else{
                row = (int)((lat - corners.south) / cellHeight) + 1;
            }
            if(lon == corners.east){
                col = numColumns;
            }
            else{
                col = (int)((lon - corners.west) / cellWidth) + 1;
            }
            map[row][col] += censusGroups[i].population;
        }
        return map;
    }
}

