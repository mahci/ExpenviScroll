package control;

import experiment.Block;
import tools.Logs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * Class responsible for managing the experiment
 */
public class Experimenter {

    private final String cName = "Experimenter.";
    /*-------------------------------------------------------------------------------------*/

    private static Experimenter instance; // Singleton

    //-- experiment variables
    private List<Integer> distances = Arrays.asList(50, 100, 200, 300); // in lines
    private List<Integer> frameHeights = Arrays.asList(3, 6); // in lines
    public enum Direction {
        UP,
        DOWN
    }

    //-- participate's info
    private int pid = -1;

    /*-------------------------------------------------------------------------------------*/
    /**
     * Get the Singleton instance
     * @return Singeleton instance
     */
    public static Experimenter self() {
        if (instance == null) instance = new Experimenter();
        return instance;
    }

    /**
     * Start experimenting
     * @param pid - Id of the participant
     */
    public void start(int pid) {
        this.pid = pid;
    }

    /**
     * For testing
     */
    public void testBlocks() {
        String mTag = cName + "testBlocks";

        Block block = new Block(distances, frameHeights);
        Logs.info(mTag, block.toString());
    }


}
