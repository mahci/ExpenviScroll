package experiment;

import lombok.AllArgsConstructor;
import lombok.Data;
import tools.Logs;
import tools.Utils;

import java.util.*;

public class Experiment {
    private final static String NAME = "Experiment/";

    //-- Consts
    // Directions
    public enum DIRECTION {
        N(0), S(1), E(2), W(3), NE(4), NW(5), SE(6), SW(7);
        private final int n;
        DIRECTION(int i) { n = i; }
        // Get a NE/NW/SE/SW randomly
        public static DIRECTION randTd() {
            return DIRECTION.values()[Utils.randInt(4, 8)];
        }
        // Get a NE/SE randomly
        public static DIRECTION randOne(DIRECTION d0, DIRECTION d1) {
            if (Utils.randInt(0, 2) == 0) return d0;
            else return d1;
        }
        // Get the opposite direction (Horizontal)
        public static DIRECTION oppHz(DIRECTION dr) {
            return switch (dr) {
                case N -> N;
                case S -> S;
                case E -> W;
                case W -> E;
                case NE -> NW;
                case NW -> NE;
                case SE -> SW;
                case SW -> SE;
            };
        }
        // Get the opposite direction (Vertical)
        public static DIRECTION oppVt(DIRECTION dr) {
            return switch (dr) {
                case N -> S;
                case S -> N;
                case E -> E;
                case W -> W;
                case NE -> SE;
                case NW -> SW;
                case SE -> NE;
                case SW -> NW;
            };
        }
    }

    // Tasks
    public enum TASK {
        VERTICAL, TWO_DIM;
        private static final TASK[] values = values();

        public static TASK get(int ord) {
            if (ord < values.length) return values[ord];
            else return values[0];
        }

        public String toShowString() {
            if (this.equals(VERTICAL)) return "VERTICAL";
            else return "2D";
        }
    }

    // Techniques
    public enum TECHNIQUE {
        DRAG, RATE_BASED, FLICK, MOUSE;
        private static final TECHNIQUE[] values = values();

        public static TECHNIQUE get(int ord) {
            if (ord < values.length) return values[ord];
            else return values[0];
        }

        public String toShowString() {
            if (this.equals(FLICK)) return "MOOSE";
            else return this.toString();
        }
    }

    // ------------------------------------------------------------------------------------------------------
    //-- Config
    // Vertical
    public static final double VT_SCROLL_BAR_W_mm = 5.0;
    public static final double VT_SCROLL_THUMB_H_mm = 6.0;

    // 2D
    public static final int TD_N_ROWS = 300; // = num of columns
    public static final int TD_N_VIS_ROWS = 25; // = num of visible cols
    public static final double TD_CELL_SIZE_mm = 7.0; // Side of cells in mm
    public static final double TD_SCROLL_BAR_W_mm = 5.0; // Length = side of the pane
    public static final double TD_SCROLL_THUMB_L_mm = 6.0; // Width = width of the scrollbar
    public static final double TD_FRAME_H_mm = 7.0; // Height of frame

    // Coefs
    public static final double MOUSE_GAIN = 30.0;

    // Variables
    private static final int[] VT_DISTANCES = new int[]{50, 200, 600}; // in lines/cells
    private static final int[] TD_DISTANCES = new int[]{50, 200};
    private static final int[] FRAMES = new int[]{3, 5}; // in lines/cells

    private static final Map<TASK, Integer> N_BLOCKS = Map.of(
            TASK.VERTICAL, 8, TASK.TWO_DIM, 4);

    private final List<Constellation> constellations = new ArrayList<>();

    // ------------------------------------------------------------------------------------------------------
    // Status
//    private static int mDragSensitivity = 2;
//    private static double mDragGain = 100;
//    private static int mRBSensitivity = 1;
//    private static double mRBGain = 1.5;
//    private static int mRBDenom = 50;
//    private static double mCoef = 0.1;

    //--- Participant's things!
    private int mPId;

    // -------------------------------------------------------------------------------------------------------
    /**
     * Constructor
     * @param pid Participant's Id (from 1)
     */
    public Experiment(int pid) {
        final String TAG = NAME;

        mPId = pid;

        // Set constellations
        final Part MOUSE_VT = new Part(TECHNIQUE.MOUSE, TASK.VERTICAL);
        final Part MOUSE_2D = new Part(TECHNIQUE.MOUSE, TASK.TWO_DIM);
        final Part MOOSE_VT = new Part(TECHNIQUE.FLICK, TASK.VERTICAL);
        final Part MOOSE_2D = new Part(TECHNIQUE.FLICK, TASK.TWO_DIM);

        constellations.add(new Constellation(
                new Session(MOUSE_VT, MOUSE_2D, MOOSE_VT, MOOSE_2D),
                new Session(MOOSE_2D, MOOSE_VT, MOUSE_2D, MOUSE_VT)));

        constellations.add(new Constellation(
                new Session(MOUSE_VT, MOUSE_2D, MOOSE_VT, MOOSE_2D),
                new Session(MOOSE_2D, MOOSE_VT, MOUSE_2D, MOUSE_VT)));

        constellations.add(new Constellation(
                new Session(MOUSE_2D, MOUSE_VT, MOOSE_2D, MOOSE_VT),
                new Session(MOOSE_VT, MOOSE_2D, MOUSE_VT, MOUSE_2D)));

        constellations.add(new Constellation(
                new Session(MOUSE_2D, MOUSE_VT, MOOSE_2D, MOOSE_VT),
                new Session(MOOSE_VT, MOOSE_2D, MOUSE_VT, MOUSE_2D)));

        constellations.add(new Constellation(
                new Session(MOOSE_VT, MOOSE_2D, MOUSE_VT, MOUSE_2D),
                new Session(MOUSE_2D, MOUSE_VT, MOOSE_2D, MOOSE_VT)));

        constellations.add(new Constellation(
                new Session(MOOSE_VT, MOOSE_2D, MOUSE_VT, MOUSE_2D),
                new Session(MOUSE_2D, MOUSE_VT, MOOSE_2D, MOOSE_VT)));

        constellations.add(new Constellation(
                new Session(MOOSE_2D, MOOSE_VT, MOUSE_2D, MOUSE_VT),
                new Session(MOUSE_VT, MOUSE_2D, MOOSE_VT, MOOSE_2D)));

        constellations.add(new Constellation(
                new Session(MOOSE_2D, MOOSE_VT, MOUSE_2D, MOUSE_VT),
                new Session(MOUSE_VT, MOUSE_2D, MOOSE_VT, MOOSE_2D)));


    }

    /**
     * Get a Part
     * @param sessionInd Index of the session (starting from 0)
     * @param partInd Index of the Part (starting from 0)
     * @return Part
     */
    public Part getPart(int sessionInd, int partInd) {
        final int cInd = mPId % 8; // Rotating through the constellations
        return constellations.get(cInd).sessions[sessionInd].parts[partInd];
    }

    /**
     * Get the participant's id
     * @return Participant's id
     */
    public int getPId() {
        return mPId;
    }

    // -------------------------------------------------------------------------------------------------------
    @Data
    public static class Part {
        private TECHNIQUE tech;
        private TASK task;
        private List<Block> blocks = new ArrayList<>();

        public Part(TECHNIQUE tch, TASK tsk) {
            tech = tch;
            task = tsk;

            // Create blocks
            for (int i = 0; i < N_BLOCKS.get(task); i++) {
                blocks.add(new Block(task, VT_DISTANCES, TD_DISTANCES, FRAMES));
            }
        }

        public int nBlocks() {
            return blocks.size();
        }

        public Block getBlock(int blInd) {
            if (blInd < blocks.size()) return blocks.get(blInd);
            else return null;
        }
    }

    // Session
    public static class Session {
        private Part[] parts = new Part[4];

        public Session(Part... ps) {
            if (ps.length == 4) {
                parts = ps;
            } else {
                Logs.e(Experiment.NAME, "Parts not 4");
            }
        }

        public Part getPart(int ptInd) {
            if (ptInd < 4) return parts[ptInd];
            else return null;
        }
    }

    // Constellation
    public static class Constellation {
        private Session[] sessions = new Session[2];

        public Constellation() { }

        public Constellation(Session s1, Session s2) {
            sessions[0] = s1;
            sessions[1] = s2;
        }

        public Session getSession(int sInd) {
            if (sInd < 2) return sessions[sInd];
            else return null;
        }
    }
}
