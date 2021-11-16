package experiment;

import control.Experimenter;
import tools.DoubleDimension;

import java.util.ArrayList;
import java.util.List;

public class Experiment {

    private final String NAME = "Experiment";
    // ------------------------------------------------------------------------------------------------------

    // list of rounds in this experiment
    private ArrayList<Round> rounds = new ArrayList<>();

    //-- Properties
    // Vertical
    public final DoubleDimension DIM_VT_PANE_mm = new DoubleDimension(130.0, 145.0);
    public final double VT_LINENUMS_W_mm = 10;
    public final double VT_SCROLL_BAR_W_mm = 5;
    public final double VT_SCROLL_THUMB_H_mm = 6;
    public final double VT_FRAME_W_mm = 7;
    public final int VT_WRAP_CHARS_COUNT = 82;
    public final int VT_N_VISIBLE_LINES = 41; // By eyes!

    // Horizontal
    public final DoubleDimension DIM_HZ_PANE_mm = new DoubleDimension(140.0, 110.0);
    public final double HZ_SCROLL_BAR_H_mm = 5.0;
    public final double HZ_SCROLL_THUMB_W_mm = 6.0;
    public final double HZ_FRAME_H_mm = 7;
    public final int HZ_N_ROWS = 25;
    public final int HZ_N_COLS = 200;
    public final int HZ_N_VISIBLE_COLS = 15;

    public final double SCROLL_GAIN = 5.0;

    private Experimenter.ScrollMode mode = Experimenter.ScrollMode.VERTICAL;

    // -------------------------------------------------------------------------------------------------------

    /**
     * Constructor with default values
     */
    public Experiment() {

    }

    /**
     * Create an Experiment with the set values
     * @param nRounds Number of rounds in the experiment
     * @param distances List of distances
     * @param frameSizes List of frame heights
     */
    public Experiment(int nRounds, List<Integer> distances, List<Integer> frameSizes) {
        // Generate rounds
        for(int b = 0; b < nRounds; b++) {
            rounds.add(new Round(mode, distances, frameSizes));
        }
    }

    /**
     * Get a block
     * @param roundNum Round number (starting from 1)
     * @return Round
     */
    public Round getRound(int roundNum) {
        if (roundNum > 0 && roundNum <= rounds.size()) return rounds.get(roundNum - 1);
        else return null;
    }

}
