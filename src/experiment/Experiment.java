package experiment;

import control.Server;
import tools.DimensionD;
import tools.Logs;
import tools.Memo;
import tools.Utils;

import java.util.ArrayList;

import static experiment.Experiment.TECHNIQUE.DRAG;
import static experiment.Experiment.TECHNIQUE.RATE_BASED;
import static tools.Consts.STRINGS.*;

public class Experiment {
    private final static String NAME = "Experiment/";
    // ------------------------------------------------------------------------------------------------------

    // list of rounds in this experiment
    private ArrayList<Round> mRounds = new ArrayList<>();

    //-- Config
    // Vertical
    public static final DimensionD VT_PANE_DIM_mm = new DimensionD(130.0, 145.0);
    public static final double VT_LINENUMS_W_mm = 10;
    public static final double VT_SCROLL_BAR_W_mm = 5;
    public static final double VT_SCROLL_THUMB_H_mm = 6;
    public static final int VT_WRAP_CHARS_COUNT = 73; // Manual
    public static final float VT_TEXT_FONT_SIZE = 24.0f;
    public final float LINE_NUM_FONT_SIZE = 12.2f;
    public final int VT_N_VISIBLE_LINES = 41; // By eyes!

    // Horizontal
    public final DimensionD DIM_HZ_PANE_mm = new DimensionD(140.0, 110.0);
    public final double HZ_SCROLL_BAR_H_mm = 5.0;
    public final double HZ_SCROLL_THUMB_W_mm = 6.0;
    public final int HZ_N_ROWS = 25;
    public final int HZ_N_COLS = 200;
    public final int HZ_N_VISIBLE_COLS = 15;

    // 2D
    public static final int TD_N_ROWS = 300; // = num of columns
    public static final int TD_N_VIS_ROWS = 25; // = num of visible cols
    public static final double TD_CELL_SIZE_mm = 7.0; // Side of cells in mm
    public static final double TD_SCROLL_BAR_W_mm = 5.0; // Length = side of the pane
    public static final double TD_SCROLL_THUMB_L_mm = 6.0; // Width = width of the scrollbar
    public static final double TD_FRAME_H_mm = 7.0; // Height of frame

    //-- Variables
    private int[] DISTANCES = new int[]{30, 150}; // in lines/cells
    private int[] FRAMES = new int[]{3, 5}; // in lines/cells
    public enum AREA {
        N(0), S(1), E(2), W(3), NE(4), NW(5), SE(6), SW(7);
        private final int n;
        AREA(int i) { n = i; }
        // Get a NE/NW/SE/SW randomly
        public static AREA randTd() {
            return AREA.values()[Utils.randInt(4, 8)];
        }
        // Get a NE/SE randomly
        public static AREA randOne(AREA... areas) {
            return values()[Utils.randInt(0, areas.length)];
        }
    };
    public enum SCROLL_MODE {
        VERTICAL(1), TWO_DIM(2);
        private final int n;
        SCROLL_MODE(int i) { n = i; }
    }

    public enum TECHNIQUE {
        DRAG(1), RATE_BASED(2), FLICK(3), MOUSE(4);
        private final int n;
        TECHNIQUE(int i) { n = i; }
    }

    private int VT_REP = 4;
    private int TD_REP = 2;

    int pid;

    // Status
    private static TECHNIQUE mActiveTechnique = DRAG;
    private static int mDragSensitivity = 2;
    private static double mDragGain = 100;
    private static int mRBSensitivity = 1;
    private static double mRBGain = 1.5;
    private static int mRBDenom = 50;

    // -------------------------------------------------------------------------------------------------------

    /**
     * Constructor with default values
     */
    public Experiment(int pid) {
        final String TAG = NAME;
        this.pid = pid;

        // Generate rounds
        // [TEMP] one round of each mode
        mRounds.add(new Round(DISTANCES, FRAMES));
    }

    /**
     * Get a block
     * @param roundNum Round number (starting from 1)
     * @return Round
     */
    public Round getRound(int roundNum) {
        if(roundNum > 0 && roundNum <= mRounds.size()) return mRounds.get(roundNum - 1);
        else return null;
    }

    public Trial randVtTrial() {
        int dist = Utils.randElement(DISTANCES);
        int fr = Utils.randElement(FRAMES);
        return new Trial(SCROLL_MODE.VERTICAL, AREA.randOne(AREA.N, AREA.S), dist, fr);
    }

    public Trial randTdTrial() {
        int dist = Utils.randElement(DISTANCES);
        int fr = Utils.randElement(FRAMES);
        return new Trial(SCROLL_MODE.TWO_DIM, AREA.randTd(), dist, fr);
    }

    public static void setActiveTechnique(TECHNIQUE tech) {
        mActiveTechnique = tech;

        final Memo memo = new Memo(CONFIG, TECHNIQUE, mActiveTechnique.ordinal(), 0);
        Server.get().send(memo);
    }

    public static void setSensitivity(int sens) {
        if (mActiveTechnique.equals(DRAG)) mDragSensitivity = sens;
        else if (mActiveTechnique.equals(RATE_BASED)) mRBSensitivity = sens;

        final Memo memo = new Memo(CONFIG, SENSITIVITY, sens, sens);
        Server.get().send(memo);
    }

    public static void setGain(double gain) {
        if (mActiveTechnique.equals(DRAG)) mDragGain = gain;
        else if (mActiveTechnique.equals(RATE_BASED)) mRBGain = gain;

        final Memo memo = new Memo(CONFIG, GAIN, gain, gain);
        Server.get().send(memo);
    }

    public static void setDenom(int denom) {
        mRBDenom = denom;

        final Memo memo = new Memo(CONFIG, DENOM, denom, denom);
        Server.get().send(memo);
    }

    public static TECHNIQUE getActiveTechnique() {
        return mActiveTechnique;
    }



}
