package experiment;

import tools.Utils;

import javax.swing.tree.TreeModel;
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

        @Override
        public String toString() {
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

    // Variables
    private int[] VT_DISTANCES = new int[]{50, 200, 600}; // in lines/cells
    private int[] TD_DISTANCES = new int[]{50, 200};
    private int[] FRAMES = new int[]{3, 5}; // in lines/cells
//    private List<TECHNIQUE> TECH_ORDERS = Arrays.asList(
//            FLICK, DRAG, MOUSE,
//            FLICK, MOUSE, DRAG,
//            DRAG, FLICK, MOUSE,
//            DRAG, MOUSE, FLICK,
//            MOUSE, FLICK, DRAG,
//            MOUSE, DRAG, FLICK);
    private Map<TASK, Integer> N_BLOCKS = Map.of(
            TASK.VERTICAL, 8,
        TASK.TWO_DIM, 4);
    private TreeModel mConstellTree;

    public final static double MOUSE_SCROLL_MULTIP = 50.0;

    // ------------------------------------------------------------------------------------------------------
    // Status
//    private static TECHNIQUE mActiveTechnique = DRAG;
    private static int mDragSensitivity = 2;
    private static double mDragGain = 100;
    private static int mRBSensitivity = 1;
    private static double mRBGain = 1.5;
    private static int mRBDenom = 50;
    private static double mCoef = 0.1;

    //--- Participant's things!
    private int mPId;
    private List<TASK> mPcTasks;
    private List<TECHNIQUE> mPcTechs;
    private ExpNode mPcTree;
//    private List<Chunk> mPcChunks;

    // -------------------------------------------------------------------------------------------------------
    /**
     * Constructor
     * @param pid Participant's Id (from 1)
     */
    public Experiment(int pid) {
        final String TAG = NAME;

        mPId = pid;

        if (mPId % 2 == 0) mPcTasks = Arrays.asList(TASK.TWO_DIM, TASK.VERTICAL);
        else mPcTasks = Arrays.asList(TASK.VERTICAL, TASK.TWO_DIM);

//        switch (mPId % 4) {
//            case 0 -> {
//                mPcTechs = Arrays.asList(FLICK, MOUSE);
//                mPcTasks = Arrays.asList(VERTICAL, TWO_DIM);
//            }
//            case 1 -> {
//                mPcTechs = Arrays.asList(FLICK, MOUSE);
//                mPcTasks = Arrays.asList(TWO_DIM, VERTICAL);
//            }
//            case 2 -> {
//                mPcTechs = Arrays.asList(MOUSE, FLICK);
//                mPcTasks = Arrays.asList(VERTICAL, TWO_DIM);
//            }
//            case 3 -> {
//                mPcTechs = Arrays.asList(MOUSE, FLICK);
//                mPcTasks = Arrays.asList(TWO_DIM, VERTICAL);
//            }
//        }

        mPcTree = new ExpNode<>(pid);

        for (TASK task : mPcTasks) {
            ExpNode taskNode = new ExpNode<>(task);

            for (int i = 0; i < N_BLOCKS.get(task); i++) {
                final ExpNode blockNode = new ExpNode<>(
                        new Block(task, VT_DISTANCES, TD_DISTANCES, FRAMES));
                taskNode.addChild(blockNode);
            }

            mPcTree.addChild(taskNode);
        }

//        for (TECHNIQUE tech : mPcTechs) {
//            ExpNode techNode = new ExpNode<>(tech);
//
//            for (TASK task : mPcTasks) {
//                ExpNode taskNode = new ExpNode<>(task);
//
//                for (int i = 0; i < N_BLOCKS.get(task); i++) {
//                    final ExpNode blockNode = new ExpNode<>(
//                            new Block(task, VT_DISTANCES, TD_DISTANCES, FRAMES));
//                    taskNode.addChild(blockNode);
//                }
//
//                techNode.addChild(taskNode);
//            }
//
//            mPcTree.addChild(techNode);
//        }


    }

    public ExpNode getPcTree() {
        return mPcTree;
    }

    /**
     * Get the order of the techniques to experiment
     * @return List of techniques (n = 3)
     */
    public List<TECHNIQUE> getPcTechs() {
        return mPcTechs;
    }

    /**
     * Get the list of blocks for a TechTask
     * @param techTaskInd Index of the techTask (0 or 1)
     * @return Arraylist of Blocks
     */
    public List<Block> getTechTaskBlocks(int techTaskInd) {
        final List<Block> result = new ArrayList<>();
        final TASK task = mPcTasks.get(techTaskInd);
        for (int i = 0; i < N_BLOCKS.get(task); i++) {
            result.add(new Block(task, VT_DISTANCES, TD_DISTANCES, FRAMES));
        }

        return result;
    }

    /**
     * Get the participant's id
     * @return Participant's id
     */
    public int getPId() {
        return mPId;
    }

    /**
     * Get a random vertical trial
     * @return Random vertical trial
     */
//    public Trial randVtTrial() {
//        int dist = Utils.randElement(VT_DISTANCES);
//        int fr = Utils.randElement(FRAMES);
//        return new Trial(TASK.VERTICAL, DIRECTION.randOne(DIRECTION.N, DIRECTION.S), dist, 0, fr);
//    }
//
//    /**
//     * Get a random 2D trial
//     * @return Random 2D trial
//     */
//    public Trial randTdTrial() {
//        int dist = Utils.randElement(TD_DISTANCES);
//        int fr = Utils.randElement(FRAMES);
//        return new Trial(TASK.TWO_DIM, DIRECTION.randTd(), 0, dist, fr);
//    }
//
//    /**
//     * Set the active technique
//     * @param tech TECHNIQUE
//     */
//    public static void setActiveTechnique(TECHNIQUE tech) {
//        mActiveTechnique = tech;
//
//        final Memo memo = new Memo(CONFIG, TECH, mActiveTechnique.ordinal(), 0);
//        Server.get().send(memo);
//    }


//    public static void setSensitivity(int sens) {
//        if (mActiveTechnique.equals(DRAG)) mDragSensitivity = sens;
//        else if (mActiveTechnique.equals(RATE_BASED)) mRBSensitivity = sens;
//
//        final Memo memo = new Memo(CONFIG, SENSITIVITY, sens, sens);
//        Server.get().send(memo);
//    }
//
//    public static void setGain(double gain) {
//        if (mActiveTechnique.equals(DRAG)) mDragGain = gain;
//        else if (mActiveTechnique.equals(RATE_BASED)) mRBGain = gain;
//
//        final Memo memo = new Memo(CONFIG, GAIN, gain, gain);
//        Server.get().send(memo);
//    }
//
//    public static void setDenom(int denom) {
//        mRBDenom = denom;
//
//        final Memo memo = new Memo(CONFIG, DENOM, denom, denom);
//        Server.get().send(memo);
//    }
//
//    public static void setCoef(double coef) {
//        mCoef = coef;
//
//        final Memo memo = new Memo(CONFIG, COEF, coef, coef);
//        Server.get().send(memo);
//    }
//
//    public static TECHNIQUE getActiveTechnique() {
//        return mActiveTechnique;
//    }

}
