package paralleltasks;

import processing.exceptions.NotYetImplementedException;
import processing.types.CensusGroup;
import processing.types.MapCorners;

import java.util.concurrent.locks.Lock;

/*
   1) This class is used in version 5 to create the initial grid holding the total population for each grid cell
        - You should not be using the ForkJoin framework but instead should make use of threads and locks
        - Note: the resulting grid after all threads have finished running should be the same as the final grid from
          PopulateGridTask.java
 */

public class PopulateLockedGridTask extends Thread {
    CensusGroup[] censusGroups;
    int lo, hi, numRows, numColumns;
    MapCorners corners;
    double cellWidth, cellHeight;
    int[][] populationGrid;
    Lock[][] lockGrid;


    public PopulateLockedGridTask(CensusGroup[] censusGroups, int lo, int hi, int numRows, int numColumns, MapCorners corners,
                                  double cellWidth, double cellHeight, int[][] popGrid, Lock[][] lockGrid) {
        this.censusGroups = censusGroups;
        this.lo = lo;
        this.hi = hi;
        this.numRows = numRows;
        this.numColumns = numColumns;
        this.corners = corners;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.populationGrid = popGrid;
        this.lockGrid = lockGrid;
    }

    @Override
    public void run() {
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
            lockGrid[row][col].lock();
            populationGrid[row][col] += censusGroups[i].population;
            lockGrid[row][col].unlock();
        }
    }
}
