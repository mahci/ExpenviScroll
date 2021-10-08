package experiment;

import tools.Utils;

import java.util.*;

import static control.Experimenter.Direction;

/***
 * Class for each block of the experiment
 */
public class Block {

    private final String cName = "Block";
    /*-------------------------------------------------------------------------------------*/

    public class Subblock {

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
            return "Subblock{" +
                    "trials=" + trials +
                    '}';
        }
    }

    Subblock[] subblocks = new Subblock[2];

    /*-------------------------------------------------------------------------------------*/

    /**
     * Generate the block from the set of values
     * Directions gotten directly from the Experimenter
     * @param distances - list of distances (in lines)
     * @param frameHeights - list of heights (in lines)
     */
    public Block(List<Integer> distances, List<Integer> frameHeights) {

        // Create all the combination of dists/fHs/dirs
        ArrayList<Trial> allTrialsList = new ArrayList<>();
        for (int d : distances) {
            for (int fH : frameHeights) {
                allTrialsList.add(new Trial(d, fH, Direction.UP));
                allTrialsList.add(new Trial(d, fH, Direction.DOWN));
            }
        }

        // Assign randomly to subblocks (for each d/fH, only one dir in each subblock)
        subblocks[0] = new Subblock();
        subblocks[1] = new Subblock();
        List<Integer> permInd = Utils.randPerm(allTrialsList.size() / 2);
        for (int ind : permInd) {
            List<Integer> subInd = Utils.randPerm(2); // which subblock gets the top direction?
            subblocks[subInd.get(0)].addTrial(allTrialsList.get(ind * 2));
            subblocks[subInd.get(1)].addTrial(allTrialsList.get(ind * 2 + 1));
        }
        Collections.shuffle(subblocks[0].trials);
        Collections.shuffle(subblocks[1].trials);

    }

    @Override
    public String toString() {
        String result = "Block{";

        if (subblocks[0] == null) return result + "Not set";

        return result +
                "SB1 = " + subblocks[0] + '\n' +
                "SB2 = " + subblocks[1] +
                "}";
    }
}
