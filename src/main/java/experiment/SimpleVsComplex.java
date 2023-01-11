package experiment;

import cse332.types.CensusGroup;
import main.PopulationQuery;
import queryresponders.ComplexSequential;
import queryresponders.SimpleSequential;
import java.util.Random;

public class SimpleVsComplex {
    private static final int NUM_ROWS = 100;
    private static final int NUM_COLUMNS = 500;
    private static final int NUM_QUERIES = 1;
    private static final int NUM_TESTS = 30;
    private static boolean WARMUP_RUNS = true;
    private static final int NUM_WARMUP = 10;

    public static void main (String args[]){
        CensusGroup[] data = PopulationQuery.parse("CenPop2010.txt");
        double totalSimple = 0;
        double totalComplex = 0;
        for(int i = 0; i < NUM_TESTS; i++){
            if(i == NUM_WARMUP){
                WARMUP_RUNS = false;
            }
            int[][] queries = buildCoordinates(NUM_COLUMNS, NUM_ROWS, NUM_QUERIES);
            SimpleSequential simple = new SimpleSequential(data, NUM_COLUMNS, NUM_ROWS);
            ComplexSequential complex = new ComplexSequential(data, NUM_COLUMNS, NUM_ROWS);
            totalSimple += compareSimple(simple, queries);
            totalComplex += compareComplex(complex, queries);
        }
        double timeSimple = totalSimple / (NUM_TESTS - NUM_WARMUP);
        double timeComplex = totalComplex / (NUM_TESTS - NUM_WARMUP);
        System.out.println("The average time to answer " + NUM_QUERIES + " query for SimpleSequential is " + timeSimple + " ns");
        System.out.println("The average time to answer " + NUM_QUERIES + " query for ComplexSequential is " + timeComplex + " ns");
        System.out.println();

    }
    public static int[][] buildCoordinates(int columns, int rows, int query){
        int[][] queries = new int[query][4];
        Random rand = new Random();
        for(int i = 0; i < query; i++){
            int west = rand.nextInt(columns) + 1;
            int south = rand.nextInt(rows) + 1;
            int east = rand.nextInt(columns - west + 1) + west;
            int north = rand.nextInt(rows - south + 1) + south;
            int[] returnQuery = {west,south, east, north};
            queries[i] = returnQuery;
        }
        return queries;
    }

    private static double compareSimple(SimpleSequential testSimple, int[][] queries) {
        long totalTime = 0;
        long startTime = System.nanoTime();
        for (int i = 0; i < NUM_QUERIES; i++) {
            testSimple.getPopulation(queries[i][0], queries[i][1], queries[i][2], queries[i][3]);
        }
        long endTime = System.nanoTime();
        totalTime += (endTime - startTime);
        if(WARMUP_RUNS){
            return 0;
        }
        return totalTime;
    }

    private static double compareComplex(ComplexSequential testComplex, int[][] queries) {
        long totalTime = 0;
        long startTime = System.nanoTime();
        for (int i = 0; i < NUM_QUERIES; i++) {
            testComplex.getPopulation(queries[i][0], queries[i][1], queries[i][2], queries[i][3]);
        }
        long endTime = System.nanoTime();
        totalTime += (endTime - startTime);
        if(WARMUP_RUNS){
            return 0;
        }
        return totalTime;
    }
}
