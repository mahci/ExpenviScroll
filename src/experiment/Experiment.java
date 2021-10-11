package experiment;

import java.util.ArrayList;
import java.util.List;

public class Experiment {

    private final String cName = "Experiment";
    /*-------------------------------------------------------------------------------------*/

    private ArrayList<Block> blocks = new ArrayList<>(); // list of blocks in this experiment

    /*-------------------------------------------------------------------------------------*/
    /**
     * Create an Experiment with the set values
     * @param nBlocks - number of blocks in the experiment
     * @param distances - List of distances
     * @param frameHeights - List of frame heights
     */
    public Experiment(int nBlocks, List<Integer> distances, List<Integer> frameHeights) {
        // generate blocks
        for(int b = 0; b < nBlocks; b++) {
            blocks.add(new Block(distances, frameHeights));
        }
    }

}
