package paralleltasks;

import processing.exceptions.NotYetImplementedException;
import processing.types.CensusGroup;
import processing.types.MapCorners;

import java.util.concurrent.RecursiveTask;

/*
   1) This class is the parallel version of the getPopulation() method from version 1 for use in version 2
   2) SEQUENTIAL_CUTOFF refers to the maximum number of census groups that should be processed by a single parallel task
   3) The double parameters(w, s, e, n) represent the bounds of the query rectangle
   4) The compute method returns an Integer representing the total population contained in the query rectangle
 */
public class GetPopulationTask extends RecursiveTask<Integer> {
    final static int SEQUENTIAL_CUTOFF = 1000;
    CensusGroup[] censusGroups;
    int lo, hi;
    double w, s, e, n;
    MapCorners grid;

    public GetPopulationTask(CensusGroup[] censusGroups, int lo, int hi, double w, double s, double e, double n, MapCorners grid) {
        this.censusGroups = censusGroups;
        this.lo = lo;
        this.hi = hi;
        this.w = w;
        this.s = s;
        this.e = e;
        this.n = n;
        this.grid = grid;

    }

    // Returns a number for the total population
    @Override
    protected Integer compute() {
        if (hi - lo <= SEQUENTIAL_CUTOFF){
            return sequentialGetPopulation(censusGroups, lo, hi, w, s, e, n);
        }
        int middle = lo + (hi - lo) / 2;
        GetPopulationTask leftTask = new GetPopulationTask(censusGroups, lo, middle, w, s, e, n, grid);
        GetPopulationTask rightTask = new GetPopulationTask(censusGroups, middle, hi, w, s, e, n, grid);
        leftTask.fork();
        Integer rightResult = rightTask.compute();
        Integer leftResult = leftTask.join();
        return rightResult + leftResult;

    }

    private Integer sequentialGetPopulation(CensusGroup[] censusGroups, int lo, int hi, double w, double s, double e, double n) {
        int population = 0;
        double lat;
        double lon;
        for (int i = lo; i < hi; i++){
            lat = censusGroups[i].latitude;
            lon = censusGroups[i].longitude;
            if(w < lon && s < lat && n > lat && e > lon){
                population += censusGroups[i].population;
            }
            else if(w == lon && (s <= lat && n > lat)){
                population += censusGroups[i].population;
            }
            else if(s == lat && (w <= lon && e > lon)){
                population += censusGroups[i].population;
            }
            else if(e == lon && (s <= lat && n > lat) && lon == grid.east){
                population += censusGroups[i].population;
            }
            else if(n == lat && (w <= lon && e > lon) && lat == grid.north){
                population += censusGroups[i].population;
            }
            else if(n == lat && lon == e && lat == grid.north && lon == grid.east){
                population += censusGroups[i].population;
            }
        }
        return population;
    }
}
