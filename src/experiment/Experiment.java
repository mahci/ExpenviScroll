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
    public final DoubleDimension DIM_VT_PANE_mm = new DoubleDimension(130.0, 140.0);
    public final double VT_LINENUMS_W_mm = 10;
    public final double VT_SCROLL_BAR_W_mm = 5;
    public final double VT_SCROLL_THUMB_H_mm = 6;
    public final int VT_WRAP_CHARS_COUNT = 82;

    // Horizontal
    public final DoubleDimension DIM_HZ_PANE_mm = new DoubleDimension(160.0, 120.0);
    public final double HZ_SCROLL_BAR_H_mm = 5.0;
    public final double HZ_SCROLL_THUMB_W_mm = 6.0;
    public final int HZ_N_ROWS = 20;
    public final int HZ_N_COLS = 200;
    public final int HZ_N_VISIBLE_COLS = 12;

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
            rounds.add(new Round(Experimenter.ScrollMode.HORIZONTAL, distances, frameSizes));
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
