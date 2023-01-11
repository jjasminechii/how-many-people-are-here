package queryresponders;

import cse332.interfaces.QueryResponder;
import cse332.types.CensusGroup;
import cse332.types.CornerFindingResult;
import cse332.types.MapCorners;
import paralleltasks.CornerFindingTask;
import paralleltasks.PopulateLockedGridTask;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ComplexLockBased extends QueryResponder {
    public int NUM_THREADS = 4;
    private static final ForkJoinPool POOL = new ForkJoinPool(); // only to invoke CornerFindingTask
    private int[][] array;
    private int numColumns;
    private int numRows;
    private MapCorners map;
    private double cellHeight;
    private double cellWidth;
    private Lock[][] locks;
    private PopulateLockedGridTask[] tasks;
    public ComplexLockBased(CensusGroup[] censusData, int numColumns, int numRows) {
        this.numColumns = numColumns;
        this.numRows = numRows;
        CornerFindingResult CFR = POOL.invoke(new CornerFindingTask(censusData, 0, censusData.length));
        this.map = CFR.getMapCorners();
        totalPopulation = CFR.getTotalPopulation();
        this.cellHeight = (map.north - map.south) / numRows;
        this.cellWidth = (map.east - map.west) / numColumns;
        this.array = new int[numRows + 1][numColumns + 1];
        this.locks = new Lock[numRows + 1][numColumns + 1];
        this.tasks = new PopulateLockedGridTask[NUM_THREADS];

        for (int i = 1; i <= numRows; i++){
            for (int j = 1; j <= numColumns; j++){
                locks[i][j] = new ReentrantLock();
            }
        }

        for (int i = 0; i < NUM_THREADS - 1; i++){
            tasks[i] = new PopulateLockedGridTask(censusData, (i * censusData.length) / NUM_THREADS, ((i + 1) * censusData.length) / NUM_THREADS, numRows,
                    numColumns, map, cellWidth, cellHeight, array, locks);
            tasks[i].start();
        }

        tasks[NUM_THREADS - 1] = new PopulateLockedGridTask(censusData, ((NUM_THREADS - 1) * censusData.length) / NUM_THREADS, ((NUM_THREADS) * censusData.length) / NUM_THREADS, numRows,
                numColumns, map, cellWidth, cellHeight, array, locks);
        tasks[NUM_THREADS - 1].run();

        for (int i = 0; i < NUM_THREADS - 1; i++){
            try {
                tasks[i].join();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        for (int i = 1; i <= numRows; i++) {
            for (int j = 1; j <= numColumns; j++) {
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
