package paralleltasks;

import cse332.exceptions.NotYetImplementedException;

import java.util.concurrent.RecursiveAction;

/*
   1) This class is used by PopulateGridTask to merge two grids in parallel
   2) SEQUENTIAL_CUTOFF refers to the maximum number of grid cells that should be processed by a single parallel task
 */

public class MergeGridTask extends RecursiveAction {
    final static int SEQUENTIAL_CUTOFF = 10;
    int[][] left, right;
    int rowLo, rowHi, colLo, colHi;

    public MergeGridTask(int[][] left, int[][] right, int rowLo, int rowHi, int colLo, int colHi) {
        this.left = left;
        this.right = right;
        this.rowLo = rowLo;
        this.rowHi = rowHi;
        this.colLo = colLo;
        this.colHi = colHi;
    }

    @Override
    protected void compute() {
        if((colHi - colLo) * (rowHi - rowLo) <= SEQUENTIAL_CUTOFF){
            sequentialMergeGird();
        }
        else{
            int rowMid = rowLo + (rowHi - rowLo) / 2 ;
            int colMid = colLo + (colHi - colLo) / 2;
            MergeGridTask topLeftTask = new MergeGridTask(left, right, rowMid, rowHi, colLo, colMid);
            MergeGridTask topRightTask = new MergeGridTask(left, right, rowMid, rowHi, colMid, colHi);
            MergeGridTask bottomLeftTask = new MergeGridTask(left, right, rowLo, rowMid, colLo, colMid);
            MergeGridTask bottomRightTask = new MergeGridTask(left, right, rowLo, rowMid, colMid, colHi);
            topLeftTask.fork();
            topRightTask.fork();
            bottomLeftTask.fork();
            bottomRightTask.compute();
            topLeftTask.join();
            bottomLeftTask.join();
            topRightTask.join();
        }
        
    }

    // according to google gird means "prepare oneself for something difficult or challenging" so this typo is intentional :)
    private void sequentialMergeGird() {
        for(int i = rowLo; i < rowHi; i++){
            for(int j = colLo; j < colHi; j++){
                left[i][j] += right[i][j];
            }
        }
    }
}
