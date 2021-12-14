package experiment;

import tools.Logs;
import tools.Utils;

import javax.print.attribute.standard.MediaSize;
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
    public Round(Experiment.SCROLL_MODE scMode, int[] distances, int[] frames) {
        final String TAG = NAME + "Round";

        mBlocks[0] = new Block();
        mBlocks[1] = new Block();

        Logs.d(TAG, "Mode", scMode.toString());
        switch (scMode) {
            case VERTICAL: {
                // Create blocks
                for (int d : distances) {
                    for (int f : frames) {
                        AREA a0 = AREA.randOne(AREA.N, AREA.S);
                        mBlocks[0].addTrial(new Trial(scMode, a0, d, f));
                        AREA a1 = (a0 == AREA.N) ? AREA.S : AREA.N;
                        mBlocks[1].addTrial(new Trial(scMode, a1, d, f));
                    }
                }

                break;
            }

            case TWO_DIM: {
                // Create blocks
                Logs.d(TAG, "Creating TD trials", 0);
                for (int d : distances) {
                    for (int f : frames) {
                        // East in one block
                        AREA a0 = AREA.randOne(AREA.NE, AREA.SE); // Get an E randomly
                        mBlocks[0].addTrial(new Trial(scMode, a0, d, f));
                        AREA a1 = (a0 == AREA.NE) ? AREA.SE : AREA.NE;
                        mBlocks[0].addTrial(new Trial(scMode, a1, d, f));

                        // West in the other
                        AREA a2 = AREA.randOne(AREA.NW, AREA.SW); // Get a W randomly
                        mBlocks[1].addTrial(new Trial(scMode, a2, d, f));
                        AREA a3 = (a0 == AREA.NW) ? AREA.SW : AREA.NW;
                        mBlocks[1].addTrial(new Trial(scMode, a3, d, f));
                    }
                }

                break;
            }
        }

        // Shuffle each block
        Collections.shuffle(mBlocks[0].trials);
        Collections.shuffle(mBlocks[1].trials);

    }

    public Round(int[] distances, int[] frames) {
        mBlocks[0] = new Block();
        mBlocks[1] = new Block();

        for (int d : distances) {
            for (int f : frames) {
                // Vertical (N/S)
                AREA a0 = AREA.randOne(AREA.N, AREA.S);
                mBlocks[0].addTrial(new Trial(SCROLL_MODE.VERTICAL, a0, d, f));
                AREA a1 = (a0 == AREA.N) ? AREA.S : AREA.N;
                mBlocks[1].addTrial(new Trial(SCROLL_MODE.VERTICAL, a1, d, f));

                // 2D: East in one block
                a0 = AREA.randOne(AREA.NE, AREA.SE); // Get an E randomly
                mBlocks[0].addTrial(new Trial(SCROLL_MODE.TWO_DIM, a0, d, f));
                a1 = (a0 == AREA.NE) ? AREA.SE : AREA.NE;
                mBlocks[0].addTrial(new Trial(SCROLL_MODE.TWO_DIM, a1, d, f));

                // 2D: West in the other
                AREA a2 = AREA.randOne(AREA.NW, AREA.SW); // Get a W randomly
                mBlocks[1].addTrial(new Trial(SCROLL_MODE.TWO_DIM, a2, d, f));
                AREA a3 = (a0 == AREA.NW) ? AREA.SW : AREA.NW;
                mBlocks[1].addTrial(new Trial(SCROLL_MODE.TWO_DIM, a3, d, f));
            }
        }

        // Shuffle each block
        Collections.shuffle(mBlocks[0].trials);
        Collections.shuffle(mBlocks[1].trials);
    }

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
