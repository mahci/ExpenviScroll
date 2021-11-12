package control;

import experiment.Round;
import experiment.Experiment;
import tools.Logs;

import java.util.Arrays;
import java.util.List;

/***
 * Class responsible for managing the experiment
 */
public class Experimenter {

    private final String cName = "Experimenter.";
    /*-------------------------------------------------------------------------------------*/

    private static Experimenter instance; // Singleton

    // The ongoing experiment
    Experiment experiment;
    int participantId = -1;

    //-- experiment variables
    private List<Integer> distances = Arrays.asList(10, 30, 50, 100); // in lines/cols
    private List<Integer> frameSizes = Arrays.asList(3, 5); // in lines/cols
    public enum Direction {
        U_R,
        D_L
    }
    public enum ScrollMode {
        VERTICAL,
        HORIZONTAL
    }

    /*-------------------------------------------------------------------------------------*/
    /**
     * Get the Singleton instance
     * @return Singeleton instance
     */
    public static Experimenter get() {
        if (instance == null) instance = new Experimenter();
        return instance;
    }

    /**
     * Get an experiment
     */
    public Experiment getExperiment(int pid) {
        participantId = pid;
        experiment = new Experiment(1, distances, frameSizes);

        return experiment;
    }

}
