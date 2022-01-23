package experiment;

import tools.Logs;

import java.util.*;
import static experiment.Experiment.*;


/**
 * Class for each round of the experiment
 * (incl. every value of variabls)
 */
public class Round {

    private final String NAME = "Round/";
    // ------------------------------------------------------------------------------------------

    private final class Block {

        private ArrayList<Trial> trials = new ArrayList<>();

        /**
         * Add a trial to the list
         * @param tr - Trial to be added
         */
        public void addTrial(Trial tr) {
            trials.add(tr);
        }

        /**
         * Get a Trial from the list
         * @param trNum - Number of the Trial (starting from 1)
         * @return Trial
         */
        public Trial getTrial(int trNum) {
            if (trNum > 0 && trNum <= trials.size()) return trials.get(trNum - 1);
            else return null;
        }

        @Override
        public String toString() {
            return "Block[" + trials.size() + " trials" +
                    ": " + trials +
                    '}';
        }
    }

    private final Block[] mBlocks = new Block[2];

    // ------------------------------------------------------------------------------------------

    /**
     * Generate the block from the set of values
     * Directions gotten directly from the Experimenter
     * @param distances list of distances (in lines/cols)
     * @param frameSizes list of frame heights/widths (in lines/cols)
     */
    public Round(TASK scMode, int[] distances, int[] frames) {
        final String TAG = NAME + "Round";

        mBlocks[0] = new Block();
        mBlocks[1] = new Block();

        Logs.d(TAG, "Mode", scMode.toString());
//        switch (scMode) {
//            case VERTICAL: {
//                // Create blocks
//                for (int d : distances) {
//                    for (int f : frames) {
//                        DIRECTION a0 = DIRECTION.randOne(DIRECTION.N, DIRECTION.S);
//                        mBlocks[0].addTrial(new Trial(scMode, a0, d, f));
//                        DIRECTION a1 = (a0 == DIRECTION.N) ? DIRECTION.S : DIRECTION.N;
//                        mBlocks[1].addTrial(new Trial(scMode, a1, d, f));
//                    }
//                }
//
//                break;
//            }
//
//            case TWO_DIM: {
//                // Create blocks
//                for (int d : distances) {
//                    for (int f : frames) {
//                        // East in one block
//                        DIRECTION a0 = DIRECTION.randOne(DIRECTION.NE, DIRECTION.SE); // Get an E randomly
//                        mBlocks[0].addTrial(new Trial(scMode, a0, d, f));
//                        DIRECTION a1 = (a0 == DIRECTION.NE) ? DIRECTION.SE : DIRECTION.NE;
//                        mBlocks[0].addTrial(new Trial(scMode, a1, d, f));
//
//                        // West in the other
//                        DIRECTION a2 = DIRECTION.randOne(DIRECTION.NW, DIRECTION.SW); // Get a W randomly
//                        mBlocks[1].addTrial(new Trial(scMode, a2, d, f));
//                        DIRECTION a3 = (a0 == DIRECTION.NW) ? DIRECTION.SW : DIRECTION.NW;
//                        mBlocks[1].addTrial(new Trial(scMode, a3, d, f));
//                    }
//                }
//
//                break;
//            }
//        }

        // Shuffle each block
        Collections.shuffle(mBlocks[0].trials);
        Collections.shuffle(mBlocks[1].trials);

    }

    /**
     * Generate a round
     * @param distances List of distance
     * @param frames List of frame sizes
     */
    public Round(int[] distances, int[] frames) {
        mBlocks[0] = new Block();
        mBlocks[1] = new Block();

//        for (int fr : frames) {
//            // Vertical (N/S)
//            for (int dist : distances) {
//                final DIRECTION d0 = DIRECTION.randOne(DIRECTION.N, DIRECTION.S);
//                final DIRECTION d1 = DIRECTION.oppVt(d0);
//                mBlocks[0].addTrial(new Trial(TASK.VERTICAL, d0, dist, fr));
//                mBlocks[1].addTrial(new Trial(TASK.VERTICAL, d1, dist, fr));
//            }
//
//            // 2D: NE,SE in b0 | NW,SW in b1
//            for (int vtDist : distances) {
//                for (int hzDist : distances) {
//                    final DIRECTION d00 = DIRECTION.randOne(DIRECTION.NE, DIRECTION.SE);
//                    final DIRECTION d01 = DIRECTION.oppVt(d00);
//                    mBlocks[0].addTrial(new Trial(TASK.TWO_DIM, d00, vtDist, hzDist, fr));
//                    mBlocks[0].addTrial(new Trial(TASK.TWO_DIM, d01, vtDist, hzDist, fr));
//
//                    final DIRECTION d10 = DIRECTION.oppHz(d00);
//                    final DIRECTION d11 = DIRECTION.oppHz(d01);
//                    mBlocks[1].addTrial(new Trial(TASK.TWO_DIM, d10, vtDist, hzDist, fr));
//                    mBlocks[1].addTrial(new Trial(TASK.TWO_DIM, d11, vtDist, hzDist, fr));
//                    Logs.d(NAME, d00, d01, d10, d11);
//                }
//            }
//        }

        // Shuffle each block
        Collections.shuffle(mBlocks[0].trials);
        Collections.shuffle(mBlocks[1].trials);
    }

    /**
     * Get the number of trials
     * @return Number of trials
     */
    public int getNTrials() {
        return mBlocks[0].trials.size() * 2;
    }

    /**
     * Get a trial (from the total trials in both blocks)
     * @param trNum Trial number
     * @return Trial
     */
    public Trial getTrial(int trNum) {
        int nTrialsInBlock = mBlocks[0].trials.size();

        if (trNum <= 0) return new Trial(); // to avoid returing null
        else if (trNum <= nTrialsInBlock) return mBlocks[0].getTrial(trNum);
        else if (trNum <= 2 * nTrialsInBlock) return mBlocks[1].getTrial(trNum - nTrialsInBlock);
        else return new Trial();
    }

    @Override
    public String toString() {
        String result = "Round[";

        if (mBlocks[0] == null) return result + "Not set";

        return result +
                "B1: " + mBlocks[0] + '\n' +
                "B2: " + mBlocks[1] +
                "}";
    }
}
