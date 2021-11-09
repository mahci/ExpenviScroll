package experiment;

import control.Experimenter;
import tools.Utils;

import java.util.*;

import static control.Experimenter.*;

/**
 * Class for each round of the experiment
 * (incl. every value of variabls)
 */
public class Round {

    private final String NAME = "Round--";
    // ------------------------------------------------------------------------------------------

    private class Block {

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
            return "Block{" +
                    "trials=" + trials +
                    '}';
        }
    }

    Block[] blocks = new Block[2];

    // ------------------------------------------------------------------------------------------

    /**
     * Generate the block from the set of values
     * Directions gotten directly from the Experimenter
     * @param distances list of distances (in lines/cols)
     * @param frameSizes list of frame heights/widths (in lines/cols)
     */
    public Round(ScrollMode scMode, List<Integer> distances, List<Integer> frameSizes) {

        // Create all the combination of dists/fHs/dirs
        ArrayList<Trial> allTrialsList = new ArrayList<>();
        for (int d : distances) {
            for (int fH : frameSizes) {
                allTrialsList.add(new Trial(d, fH, Direction.U_R, scMode));
                allTrialsList.add(new Trial(d, fH, Direction.D_L, scMode));
            }
        }

        // Assign randomly to blocks (for each d/fH, only one dir in each subblock)
        blocks[0] = new Block();
        blocks[1] = new Block();
        List<Integer> permInd = Utils.randPerm(allTrialsList.size() / 2);
        for (int ind : permInd) {
            List<Integer> subInd = Utils.randPerm(2); // which subblock gets the top direction?
            blocks[subInd.get(0)].addTrial(allTrialsList.get(ind * 2));
            blocks[subInd.get(1)].addTrial(allTrialsList.get(ind * 2 + 1));
        }
        Collections.shuffle(blocks[0].trials);
        Collections.shuffle(blocks[1].trials);

    }

    /**
     * Get a trial (from the total trials in both blocks)
     * @param trNum Trial number
     * @return Trial
     */
    public Trial getTrial(int trNum) {
        int nTrialsInBlock = blocks[0].trials.size();

        if (trNum <= 0) return new Trial(); // to avoid returing null
        else if (trNum <= nTrialsInBlock) return blocks[0].getTrial(trNum);
        else if (trNum <= 2 * nTrialsInBlock) return blocks[1].getTrial(trNum - nTrialsInBlock);
        else return new Trial();
    }

    @Override
    public String toString() {
        String result = "Round{";

        if (blocks[0] == null) return result + "Not set";

        return result +
                "SB1 = " + blocks[0] + '\n' +
                "SB2 = " + blocks[1] +
                "}";
    }
}
