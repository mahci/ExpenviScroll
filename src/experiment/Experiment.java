package experiment;

import tools.DimensionD;
import tools.Utils;

import java.util.ArrayList;

public class Experiment {

    private final String NAME = "Experiment";
    // ------------------------------------------------------------------------------------------------------

    // list of rounds in this experiment
    private ArrayList<Round> rounds = new ArrayList<>();

    //-- Config
    // Vertical
    public final DimensionD VT_PANE_DIM_mm = new DimensionD(130.0, 145.0);
    public final double VT_LINENUMS_W_mm = 10;
    public final double VT_SCROLL_BAR_W_mm = 5;
    public final double VT_SCROLL_THUMB_H_mm = 6;
    public final int VT_WRAP_CHARS_COUNT = 82;
    public final int VT_N_VISIBLE_LINES = 41; // By eyes!

    // Horizontal
    public final DimensionD DIM_HZ_PANE_mm = new DimensionD(140.0, 110.0);
    public final double HZ_SCROLL_BAR_H_mm = 5.0;
    public final double HZ_SCROLL_THUMB_W_mm = 6.0;
    public final int HZ_N_ROWS = 25;
    public final int HZ_N_COLS = 200;
    public final int HZ_N_VISIBLE_COLS = 15;

    // 2D
    public final int TD_N_ROWS = 200; // = num of columns
    public final int TD_N_VIS_ROWS = 15; // = num of visible cols
    public final double TD_CELL_SIZE_mm = 10.0; // Side of cells in mm
    public final double TD_SCROLL_BAR_W_mm = 5.0; // Length = side of the pane
    public final double TD_SCROLL_THUMB_L_mm = 6.0; // Width = width of the scrollbar
    public final double TD_FRAME_H_mm = 7.0; // Height of frame

    //-- Mode
//    private Experimenter.ScrollMode mode = Experimenter.ScrollMode.VERTICAL;

    //-- Variables
//    private List<Integer> DISTANCES = Arrays.asList(30, 150); // in lines/cells
//    private List<Integer> FRAMES = Arrays.asList(3, 5); // in lines/cells
//    private List<Integer> VT_AREAS = Arrays.asList(1, 5); // 1: UP(N), 5: DOWN(S)
//    private List<Integer> TD_AREAS = Arrays.asList(2, 4, 6, 8); // NE, SE, SW, NW

    private int[] DISTANCES = new int[]{30, 150}; // in lines/cells
    private int[] FRAMES = new int[]{3, 5}; // in lines/cells
    public enum AREA {
        N(1), S(2), E(3), W(4), NE(5), NW(6), SE(7), SW(8);
        private final int n;
        AREA(int i) { n = i; }
        // Get a N/S randomly
        public static AREA randVt() {
            return AREA.values()[Utils.randInt(0, 2)];
        }
        // Get a NE/NW/SE/SW randomly
        public static AREA randTd() {
            return AREA.values()[Utils.randInt(0, 8)];
        }
    };
    public enum SCROLL_MODE {
        VERTICAL(1), TWO_DIM(2);
        private final int n;
        SCROLL_MODE(int i) { n = i; }
    }

    public enum TECHNIQUE {
        DRAG(1), RATE_BASED(2), MOUSE(3);
        private final int n;
        TECHNIQUE(int i) { n = i; }
    }
    private int VT_REP = 8;
    private int TD_REP = 1;

    int pid;

    // -------------------------------------------------------------------------------------------------------

    /**
     * Constructor with default values
     */
    public Experiment(int pid) {
        this.pid = pid;
        // TODO: We should decide on how to arrange vt and td rounds/trials

        // Generate rounds
//        for (int b = 0; b < nRounds; b++) {
//            rounds.add(new Round(mode, distances, frameSizes));
//        }
    }

    /**
     * Get a block
     * @param roundNum Round number (starting from 1)
     * @return Round
     */
    public Round getRound(int roundNum) {
        if(roundNum > 0 && roundNum <= rounds.size()) return rounds.get(roundNum - 1);
        else return null;
    }

    public Trial randVtTrial() {
        int dist = Utils.randElement(DISTANCES);
        int fr = Utils.randElement(FRAMES);
        return new Trial(SCROLL_MODE.VERTICAL, AREA.randVt(), dist, fr);
    }

    public Trial randTdTrial() {
        int dist = Utils.randElement(DISTANCES);
        int fr = Utils.randElement(FRAMES);
        return new Trial(SCROLL_MODE.TWO_DIM, AREA.randTd(), dist, fr);
    }

}
