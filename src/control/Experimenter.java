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

    // Vars
    private List<Integer> distances = Arrays.asList(50, 100, 200, 300); // in lines
    private List<Integer> frameHeights = Arrays.asList(3, 6); // in lines
    public static enum Direction {
        UP,
        DOWN
    }

    /**
     * Get the Singleton instance
     * @return Singeleton instance
     */
    public static Experimenter self() {
        if (instance == null) instance = new Experimenter();
        return instance;
    }

    /**
     * Constructor
     */
    public Experimenter() {

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
