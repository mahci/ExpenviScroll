package gui;

import control.Controller;
import control.Logger;
import control.Server;
import experiment.Block;
import experiment.Experiment;
import experiment.Trial;
import tools.*;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static experiment.Experiment.*;
import static tools.Consts.*;
import static tools.Consts.SOUNDS.HIT_SOUND;
import static tools.Consts.SOUNDS.MISS_SOUND;
import static tools.Consts.STRINGS.END_EXPERIMENT_MESSAGE;
import static tools.Consts.STRINGS.END_TECH_MESSAGES;

public class ExperimentPanel extends JLayeredPane implements MouseMotionListener {

    private final static String NAME = "ExperimentPanel/";
    // -------------------------------------------------------------------------------------------

    // Margins
    private double LR_MARGIN_mm = 20; // (mm) Left-right margin
    private double TB_MARGIN_mm = 20; // (mm) Left-right margin

    // Keystrokes
    private KeyStroke KS_SPACE;
    private KeyStroke KS_SLASH;
    private KeyStroke KS_ENTER;
    private KeyStroke KS_Q;
    private KeyStroke KS_A;
    private KeyStroke KS_W;
    private KeyStroke KS_S;
    private KeyStroke KS_E;
    private KeyStroke KS_D;
    private KeyStroke KS_RA; // Right arrow
    private KeyStroke KS_DA; // Down arrow

    // Experiment and trial
    private Experiment mExperiment;
    private List<Block> mBlocks; // Blocks in a TechTask
    private Block mBlock;
    private Trial mTrial;
    private int mTechTaskInd; // 0, 1
    private int mBlockInd; // Block num = block ind + 1
    private int mTrialInd; // Trial num = trial ind + 1
    private int mTechInd;
    private List<TECHNIQUE> mTechs;
    private int mNTrialsInBlock; // Not counting the repeated trials
    private boolean mInShortBreak;
    private boolean mInLongBreak;
    private boolean mInBetweenTechs;

    // Elements
    private VTScrollPane mVTScrollPane;
    private HorizontalScrollPane hzScrollPane;
    private TDScrollPane mTDScrollPane;
    private JLabel mLabel;
    private JLabel mLevelLabel;
    private JLabel mTechLabel;
    private TechConfigPanel mConfigPanel;
    private JLabel mShortBreakLabel;

    // Logging
    private Logger.GeneralInfo mGenInfo = new Logger.GeneralInfo();
    private Logger.TrialInfo mTrialInfo = new Logger.TrialInfo();
    private Logger.TimeInfo mTimeInfo = new Logger.TimeInfo();
    private long mExpStTime;
    private long mTrialStTime;
    private long mBlockStTime;
    private long mTechTaskStTime;
    private long mTechStTime;

    private boolean isPaneSet;
    private int targetColNum, randColNum;
    private int targetLineNum, randLineNum;
    private boolean isScrollingEnabled = false;
    private boolean paintTrial;
    private Rectangle mVtFrameRect = new Rectangle();
    private Rectangle mHzFrameRect = new Rectangle();
    private Point mPanePos = new Point();
    private Point mLasPanePos = new Point();
    private Dimension mPaneDim = new Dimension();
    private boolean isVt;

    // -------------------------------------------------------------------------------------------
    // Shart the experiment
    private final Action START_EXP_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Logs.d(ExperimentPanel.NAME, "START_EXP_ACTION");
            mTechs = mExperiment.getPcTechniques();

            mTechTaskInd = 0;
            mBlockInd = 0;
            mTrialInd = 0;
            mTechInd = 0;

            mBlocks = mExperiment.getTechTaskBlocks(mTechTaskInd);
            mBlock = mBlocks.get(mBlockInd);
            mNTrialsInBlock = mBlock.getNTrials();
            mTrial = mBlock.getTrial(mTrialInd);

            // Set the active technique
            Experiment.setActiveTechnique(mTechs.get(mTechInd));
            mTechLabel.setText("Technique: " + mTechs.get(mTechInd));

            // Sync the info to the Moose
            Server.get().send(new Memo(STRINGS.LOG, STRINGS.PID, mExperiment.getPId()));
            Server.get().send(new Memo(STRINGS.LOG, STRINGS.BLOCK_TRIAL,
                    mBlockInd + 1, mTrialInd + 1)); // Block/trial *num*
            Server.get().send(new Memo(STRINGS.LOG, STRINGS.TSK, mTrial.getTask()));

            // Log
            mGenInfo.tech = mTechs.get(mTechInd);
            mGenInfo.blockNum = mBlockInd + 1;
            mGenInfo.trialNum = mTrialInd + 1;
            mGenInfo.trial = mTrial;

            mExpStTime = Utils.nowInMillis();
            mBlockStTime = Utils.nowInMillis();

            // Show trials
            remove(0);
            showTrial();

            // Change the SPACE action
            getActionMap().put("SPACE", END_TRIAL);
        }
    };

    // End a trial
    private final Action END_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            final String TAG = ExperimentPanel.NAME + "mEndTrialAction";

            final Pair trialResult = checkHit();
            Logs.d(TAG, trialResult);
            Logs.d(TAG, checkSuccess());
            // Log InstantInfo
            Logger.InstantInfo scrollInstInfo;
            if (mTrial.getTask() == TASK.VERTICAL) scrollInstInfo = mVTScrollPane.getInstantInfo();
            else scrollInstInfo = mTDScrollPane.getInstantInfo();
            scrollInstInfo.trialEnd = Utils.nowInMillis();
            Logger.get().logInstantInfo(mGenInfo, scrollInstInfo);

            // Log TrialInfo
            mTrialInfo.scrollTime = (int) (scrollInstInfo.lastScroll - scrollInstInfo.firstScroll);
            mTrialInfo.trialTime = (int) (Utils.nowInMillis() - scrollInstInfo.firstScroll);
            mTrialInfo.finishTime = (int) (Utils.nowInMillis() - scrollInstInfo.lastScroll);
            Logs.d(TAG, Utils.nowInMillis(), scrollInstInfo.firstScroll, scrollInstInfo.lastScroll);
            mTrialInfo.vtResult = trialResult.getFirst();
            mTrialInfo.hzResult = trialResult.getSecond();
            Logger.get().logTrialInfo(mGenInfo, mTrialInfo);

            // Record Trial time
            mTimeInfo = new Logger.TimeInfo();
            mTimeInfo.trialTime = Utils.nowInMillis() - mTrialStTime;

            // Was the trial a success or a fail?
            if (trialResult.areBoth(1)){
                playSound(HIT_SOUND);
            }
            else { // Miss
                playSound(MISS_SOUND);

                // Shuffle the trial into the rest of trials
                mBlock.dupeShuffleTrial(mTrialInd);
            }

            remove(0);

            if (mTrialInd < mBlock.getNTrials() - 1) { // More trials in the block
                Logger.get().logTimeInfo(mGenInfo, mTimeInfo); // Log trial TimeInfo
                nextTrial();

            } else if (mBlockInd < mBlocks.size() - 1) { // More blocks in the techTask
                // Add block time and log TimeInfo
                mTimeInfo.blockTime = Utils.nowInMillis() - mBlockStTime;
                Logger.get().logTimeInfo(mGenInfo, mTimeInfo);

                if (mBlockInd == (mBlocks.size() / 2) - 1) showShortBreak(); // Short break after half of blocks
                else nextBlock();

            } else if (mTechTaskInd < 1) { // More techTasks in the technique
                // Add block, techTask time to TimeInfo
                mTimeInfo.blockTime = Utils.nowInMillis() - mBlockStTime;
                mTimeInfo.techTaskTime = (int) ((Utils.nowInMillis() - mTechTaskStTime) / 1000);

                // Time for the break
                showLongBreak();

                // Back from break, go to the next TechTask
                nextTechTask();

            } else if (mTechInd < 2) { // Technique is finished
                // Add block, techTask, technique time and log TimeInfo
                mTimeInfo.blockTime = Utils.nowInMillis() - mBlockStTime;
                mTimeInfo.techTaskTime = (int) ((Utils.nowInMillis() - mTechTaskStTime) / 1000);
                mTimeInfo.techTime = (int) ((Utils.nowInMillis() - mTechStTime) / 1000);
                Logger.get().logTimeInfo(mGenInfo, mTimeInfo);

                endTech();

            } else { // Experiment is finished
                // Add block, techTask, technique time and log TimeInfo
                mTimeInfo.blockTime = Utils.nowInMillis() - mBlockStTime;
                mTimeInfo.techTaskTime = (int) ((Utils.nowInMillis() - mTechTaskStTime) / 1000);
                mTimeInfo.techTime = (int) ((Utils.nowInMillis() - mTechStTime) / 1000);
                mTimeInfo.experimentTime = (int) ((Utils.nowInMillis() - mExpStTime) / 1000);
                Logger.get().logTimeInfo(mGenInfo, mTimeInfo);

                // Close all logs
                Logger.get().closeLogs();

                endExp();
            }

            Logs.d(TAG, "-------------------------------------------------------------");
        }
    };

    // Jump to the next trial (without check)
    private final Action ADVANCE_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            remove(0);

            if (mTrialInd < mBlock.getNTrials() - 1) { // More trials in the block
                nextTrial();

            } else if (mBlockInd < mBlocks.size() - 1) { // More blocks in the techTask
                if (mBlockInd == (mBlocks.size() / 2) - 1) showShortBreak(); // Short break after half of blocks
                else nextBlock();

            } else if (mTechTaskInd < 1) { // More techTasks in the technique
                showLongBreak();
                // ...
                nextTechTask();

            } else if (mTechInd < 2) { // Technique is finished
                mInBetweenTechs = true;
                repaint();
                showTechEndDialog();

            } else { // Experiment is finished
                endExp();
            }
        }
    };

    // Jump to the next block
    private final Action ADVANCE_BLOCK = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            remove(0);

            if (mBlockInd < mBlocks.size() - 1) {
                if (mBlockInd == (mBlocks.size() / 2) - 1) showShortBreak(); // Short break after half of blocks
                else nextBlock();
            } else if (mTechTaskInd < 1) { // More techTasks in the technique
                showLongBreak();
                nextTechTask();
            } else if (mTechInd < 2) { // Technique is finished
                mInBetweenTechs = true;
                repaint();

                showTechEndDialog();
            } else { // Experiment is finished
                endExp();
            }
        }
    };

    // Random trial
    private final Action RANDOM_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            remove(0);

            // Get a random-generated trial from experiment and show it
            if (isVt) {
                mTrial = mExperiment.randVtTrial();
                isVt = false;
            } else {
                mTrial = mExperiment.randTdTrial();
                isVt = true;
            }

            showTrial();
        }
    };

    // Enable scrolling
    private final Action ENABLE_SCROLLING = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hzScrollPane.setWheelScrollingEnabled(enabled);
            Logs.infoAll(NAME, "Scrolling enabled");
        }
    };

    // Switch actions
    private final Action SWITCH_TECH_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (mTechInd < 2) mTechInd++;
            else mTechInd = 0;

            // Update label
            mTechLabel.setText("Technique: " + mTechs.get(mTechInd));

            // Sync Moose
            Experiment.setActiveTechnique(mTechs.get(mTechInd));
            if (mVTScrollPane != null) mVTScrollPane.changeTechnique(mTechs.get(mTechInd));
            if (mTDScrollPane != null) mTDScrollPane.changeTechnique(mTechs.get(mTechInd));
        }
    };

    // End short break
    private final Action END_SHORT_BREAK = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Logs.d(ExperimentPanel.NAME, mInShortBreak);
            if (mInShortBreak) {
                mInShortBreak = false;
                removeAll();
                nextBlock();
            }
        }
    };


    private ConfigAction mNextTechnique = new ConfigAction(STRINGS.TECHNIQUE, true);
    private ConfigAction mIncSensitivity = new ConfigAction(STRINGS.SENSITIVITY, true);
    private ConfigAction mDecSensitivity = new ConfigAction(STRINGS.SENSITIVITY, false);
    private ConfigAction mIncGain = new ConfigAction(STRINGS.GAIN, true);
    private ConfigAction mDecGain = new ConfigAction(STRINGS.GAIN, false);
    private ConfigAction mIncDenom = new ConfigAction(STRINGS.DENOM, true);
    private ConfigAction mDecDenom = new ConfigAction(STRINGS.DENOM, false);

    // -------------------------------------------------------------------------------------------
    /**
     * Create the panel
     * @param exp Experiment to show
     */
    public ExperimentPanel(Experiment exp) {
        String TAG = NAME;
        setLayout(null);

        // Set initial key actions
        mapKeys();
        getActionMap().put("SPACE", START_EXP_ACTION);
        getActionMap().put("SLASH", SWITCH_TECH_ACTION);
        getActionMap().put("ENTER", END_SHORT_BREAK);
        getActionMap().put("Q", mIncSensitivity);
        getActionMap().put("A", mDecSensitivity);
        getActionMap().put("W", mIncGain);
        getActionMap().put("S", mDecGain);
        getActionMap().put("E", mIncDenom);
        getActionMap().put("D", mDecDenom);
        getActionMap().put("RA", ADVANCE_BLOCK);
        getActionMap().put("DA", ADVANCE_TRIAL);

        // Set the experiment and log
        mExperiment = exp;
        Logger.get().logParticipant(exp.getPId());

        // Create and add the start label
        mLabel = new JLabel(STRINGS.EXP_START_MESSAGE, JLabel.CENTER);
        mLabel.setFont(new Font("Sans", Font.BOLD, 35));
        mLabel.setBounds(800, 500, 1000, 400);
        add(mLabel, 0);

        // Create the short break label (don't add it yet)
        mShortBreakLabel = new JLabel(STRINGS.SHORT_BREAK_TEXT, JLabel.CENTER);
        mShortBreakLabel.setFont(new Font("Sans", Font.BOLD, 25));
        mShortBreakLabel.setBounds(800, 500, 1000, 400);

        // Add paramter controls
//        mConfigPanel = new TechConfigPanel();
//        mConfigPanel.setBounds(1000, 1200, 800, 30);
//        add(mConfigPanel, 1);

        // Create levellabel (don't show yet)
        mLevelLabel = new JLabel("", JLabel.CENTER);
        mLevelLabel.setFont(new Font("Sans", Font.PLAIN, 18));
        mLevelLabel.setBounds(2150, 30, 400, 100);
//        add(mLevelLabel, 1);

        // Create tehcnique label
        mTechLabel = new JLabel("", JLabel.CENTER);
        mTechLabel.setFont(new Font("Sans", Font.PLAIN, 18));
        mTechLabel.setBounds(50, 30, 200, 100);
//        add(mTechLabel, 2);
    }

    /**
     * Create one instance of each pane
     */
    private void createPanes() {
        String TAG = NAME + "createPanes";

        mTDScrollPane = new TDScrollPane(TD_N_VIS_ROWS, TD_CELL_SIZE_mm, TD_SCROLL_BAR_W_mm)
                .setTable(TD_N_ROWS)
                .setScrollBars(TD_SCROLL_BAR_W_mm, TD_SCROLL_THUMB_L_mm)
                .create();

        // Make vtScrollPane the same size as the td
        mVTScrollPane = new VTScrollPane(mTDScrollPane.getPreferredSize())
                .setText("lorem.txt")
                .setScrollBar(VT_SCROLL_BAR_W_mm, VT_SCROLL_THUMB_H_mm)
                .create();

        // Set the current technique in panes
        if (mVTScrollPane != null) mVTScrollPane.changeTechnique(mTechs.get(mTechInd));
        if (mTDScrollPane != null) mTDScrollPane.changeTechnique(mTechs.get(mTechInd));

    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";
        Logs.d(TAG, mTrial);
        // Start logging
        final Logger.InstantInfo instantInfo = new Logger.InstantInfo();
        instantInfo.trialShow = Utils.nowInMillis();
        mTrialStTime = Utils.nowInMillis();

        // If panes aren't created, create them (only once)
        if (mVTScrollPane == null || mTDScrollPane == null) createPanes();

        // Reset the frames
        mVtFrameRect = new Rectangle();
        mHzFrameRect = new Rectangle();

        // Show the trial
        switch (mTrial.getTask()) {
            case VERTICAL -> {
                // Pass InstantInfo to VT to continue logging
                mVTScrollPane.setInstantInfo(instantInfo);

                // Position
                mPaneDim = mVTScrollPane.getPreferredSize();
                mPanePos = getRandPosition(mPaneDim);
                mVTScrollPane.setBounds(mPanePos.x, mPanePos.y, mPaneDim.width, mPaneDim.height);
                mLasPanePos = mPanePos;

                // Highlight
                int targetLineInd = randVtLineInd();
                mVTScrollPane.highlight(targetLineInd, mTrial.getFrame());
                Logs.d(TAG, "HLIndex= ", targetLineInd);
                add(mVTScrollPane, 0);

                // Set frame to be drawn (by paintComponent())
                int lineH = mVTScrollPane.getLineHeight();

                mVtFrameRect.width = Utils.mm2px(mExperiment.TD_FRAME_H_mm);
                mVtFrameRect.height = mTrial.getFrame() * lineH;
                mVtFrameRect.x = mPanePos.x - mVtFrameRect.width;
                mVtFrameRect.y = mPanePos.y + ((mVTScrollPane.getNVisibleLines() - mTrial.getFrame()) / 2) * lineH;

                // Center to a random line (based on the distance and highlited line)
                int centerLineInd = 0;
                if (mTrial.getDirection() == DIRECTION.N) centerLineInd = targetLineInd + mTrial.getVtDist();
                else centerLineInd = targetLineInd - mTrial.getVtDist();
                mVTScrollPane.centerLine(centerLineInd);
                Logs.d(TAG, targetLineInd, mTrial.getVtDist(), centerLineInd);
            }

            // Two-Dim
            case TWO_DIM -> {
                // Pass InstantInfo to 2D to continue logging
                mTDScrollPane.setInstantInfo(instantInfo);

                // Position
                mPaneDim = mTDScrollPane.getPreferredSize();
                mPanePos = getRandPosition(mPaneDim);
                mTDScrollPane.setBounds(
                        mPanePos.x, mPanePos.y,
                        mPaneDim.width, mPaneDim.height
                );
                mLasPanePos = mPanePos;

                // Select and highligght the target
                Pair targetInd = randTdInd();
                mTDScrollPane.highlight(targetInd, mTrial.getFrame());

                add(mTDScrollPane, 0);

                // Set frames to be drawn (by paintComponent())
                final int cellSize = Utils.mm2px(mExperiment.TD_CELL_SIZE_mm);
                final int frameH = Utils.mm2px(mExperiment.TD_FRAME_H_mm);

                mVtFrameRect.width = frameH;
                mVtFrameRect.height = mTrial.getFrame() * cellSize;
                mVtFrameRect.x = mPanePos.x - mVtFrameRect.width;
                mVtFrameRect.y = mPanePos.y + ((mExperiment.TD_N_VIS_ROWS - mTrial.getFrame()) / 2) * cellSize;

                mHzFrameRect.width = mTrial.getFrame() * cellSize;
                mHzFrameRect.height = frameH;
                mHzFrameRect.x = mPanePos.x + ((mExperiment.TD_N_VIS_ROWS - mTrial.getFrame()) / 2) * cellSize;
                mHzFrameRect.y = mPanePos.y - mHzFrameRect.height;

                // Center to a random cell (based on the dists and highlited cell)
                final Pair centerInd = new Pair();
                final int targetRow = targetInd.getFirst();
                final int targetCol = targetInd.getSecond();
                final int vtDist = mTrial.getTdDist();
                final int hzDist = mTrial.getTdDist();
                Logs.d(TAG, "Target", targetInd.toString());
                switch (mTrial.getDirection()) {
                    case NE -> centerInd.set(targetRow + vtDist, targetCol - hzDist);
                    case NW -> centerInd.set(targetRow + vtDist, targetCol + hzDist);
                    case SE -> centerInd.set(targetRow - vtDist, targetCol - hzDist);
                    case SW -> centerInd.set(targetRow - vtDist, targetCol + hzDist);
                }

                mTDScrollPane.centerCell(centerInd);
                Logs.d(TAG, "dir|target|center",
                        mTrial.getDirection().toString(), targetInd.toString(), centerInd.toString());
            }
        }

        paintTrial = true;

        // Show level label
        mLevelLabel.setText("Trial: " + (mTrialInd + 1) + "/" + mNTrialsInBlock +
                "  --  Block: " + (mBlockInd + 1) + "/" + mBlocks.size());
        mTechLabel.setText("Technique: " + mTechs.get(mTechInd));
        add(mLevelLabel, 1);
        add(mTechLabel, 1);

        revalidate();
        repaint();
    }

    /**
     * Check whehter the trial was a hit
     * @return True (Hit) / false (Miss)
     */
    private boolean checkSuccess() {
        boolean result = false;
        switch (mTrial.getTask()) {
        case VERTICAL -> {
            final int vtScrollVal = mVTScrollPane.getVerticalScrollBar().getValue();
            result = mVTScrollPane.isInsideFrames(vtScrollVal);
        }
        case TWO_DIM -> {
            final int vtScrollVal = mTDScrollPane.getVerticalScrollBar().getValue();
            final int hzScrollVal = mTDScrollPane.getHorizontalScrollBar().getValue();
            result = mTDScrollPane.isInsideFrames(vtScrollVal, hzScrollVal);
        }
        }

        return result;
    }

    /**
     * Check if a trial was a hit (1) or a miss (0)
     * @return Pair of int for each dimension
     */
    private Pair checkHit() {
        int vtResult = 0; int hzResult = 0;

        if (mTrial.getTask() == TASK.VERTICAL) {
            final int vtScrollVal = mVTScrollPane.getVerticalScrollBar().getValue();
            vtResult = (mVTScrollPane.isInsideFrames(vtScrollVal)) ? 1 : 0;
            hzResult = 1;
        } else {
            final int vtScrollVal = mTDScrollPane.getVerticalScrollBar().getValue();
            final int hzScrollVal = mTDScrollPane.getHorizontalScrollBar().getValue();
            vtResult = (mTDScrollPane.isVtInsideFrame(vtScrollVal)) ? 1 : 0;
            hzResult = (mTDScrollPane.isHzInsideFrame(hzScrollVal)) ? 1 : 0;
        }

        return new Pair(vtResult, hzResult);
    }

    /**
     * Generate a random position for a pane
     * Based on the size and dimensions of the displace area
     * @param paneDim Dimension of the pane
     * @return A random position
     */
    private Point getRandPosition(Dimension paneDim) {
        String TAG = NAME + "randPosition";

        final int lrMargin = Utils.mm2px(LR_MARGIN_mm);

        final int minX = lrMargin;
        final int maxX = getWidth() - (lrMargin + paneDim.width);

        final int midY = (getHeight() - paneDim.height) / 2;

        if (minX >= maxX) return new Point(); // Invalid dimensions
        else {
            int randX = 0;
            do {
                randX = Utils.randInt(minX, maxX);
            } while (Math.abs(randX - mLasPanePos.x) <= paneDim.width); // New position shuold be further than W

            return new Point(randX, midY);
        }
    }

    /**
     * Get a random line index
     * NOTE: Indexes start from 1
     * @return A random line index
     */
    private int randVtLineInd() {
        String TAG = NAME + "randVtLineInd";

        // General min/max
        int minInd = mVTScrollPane.getNVisibleLines(); // No highlight at the top
        int maxInd = (mVTScrollPane.getNLines() - 1) - mVTScrollPane.getNVisibleLines();
        Logs.d(TAG, "NLines, NVisibleLines", mVTScrollPane.getNLines(), mVTScrollPane.getNVisibleLines());
        // Correction based on direction
        if (mTrial.getDirection() == DIRECTION.N) {
            maxInd -= mTrial.getVtDist();
        } else {
            minInd += mTrial.getVtDist();
        }

        Logs.d(TAG, "min/max", minInd, maxInd);
//        return Utils.randInt(minInd, maxInd);
        return mVTScrollPane.getRandLine(minInd, maxInd);
    }

    /**
     * Get random row,col indexes
     * @return Pair (row, col)
     */
    private Pair randTdInd() {
        String TAG = NAME + "randTdInd";
//        int offset = (mExperiment.TD_N_VIS_ROWS - mTrial.getFrame()) / 2;
//        int minInd = offset + 1;
//        int maxInd = mExperiment.TD_N_ROWS - offset;

        // General threshold
        final MinMax vtInd = new MinMax(TD_N_VIS_ROWS + 1, (TD_N_ROWS - 1) - TD_N_VIS_ROWS);
        final MinMax hzInd = new MinMax(TD_N_VIS_ROWS + 1, (TD_N_ROWS - 1) - TD_N_VIS_ROWS);
        // Correction based on direction
        switch (mTrial.getDirection()) {
            case NE -> {
                vtInd.move(0, -mTrial.getTdDist());
                hzInd.move(mTrial.getTdDist(), 0);
            }
            case NW -> {
                vtInd.move(0, -mTrial.getTdDist());
                hzInd.move(0, -mTrial.getTdDist());
            }
            case SE -> {
                vtInd.move(mTrial.getTdDist(), 0);
                hzInd.move(mTrial.getTdDist(), 0);
            }
            case SW -> {
                vtInd.move(mTrial.getTdDist(), 0);
                hzInd.move(0, -mTrial.getTdDist());
            }
        }
        Logs.d(TAG, "vt|hz", vtInd.toString(), hzInd.toString());
        Pair result = new Pair(Utils.randIntBetween(vtInd), Utils.randIntBetween(hzInd));

        return result;
    }


    /**
     * Scroll the scrollPanes for a certain amount
     * @param vtScrollAmt Vertical scroll amount
     * @param hzScrollAmt Horizontal scroll amount
     */
    public void scroll(int vtScrollAmt, int hzScrollAmt) {
        String TAG = NAME + "scroll";

        Logs.d(TAG, "Scrolling", vtScrollAmt, hzScrollAmt);

        if (mTrial != null) {
            switch (mTrial.getTask()) {
                case VERTICAL -> mVTScrollPane.scroll(vtScrollAmt);
                case TWO_DIM -> mTDScrollPane.scroll(vtScrollAmt, hzScrollAmt);
            }
        }
    }

    /**
     * Send the trial num, block num, etc. to the Moose (for logging)
     */
    public void syncStatus() {
        Memo techMemo = new Memo(
                STRINGS.LOG, STRINGS.TECHNIQUE,
                mTechs.get(mTechInd), mTechTaskInd);
        Server.get().send(techMemo);
    }

    /**
     * Go to the next trial
     */
    private void nextTrial() {
        final String TAG = NAME + "nextTrial";

        mTrialInd++;
        mTrial = mBlock.getTrial(mTrialInd);
        mGenInfo.trial = mTrial;

        // Sync the info to the Moose
        Server.get().send(new Memo(STRINGS.LOG, STRINGS.BLOCK_TRIAL,
                mBlockInd + 1, mTrialInd + 1)); // Block/trial *num*

        showTrial();
    }

    /**
     * Go to the next block
     */
    private void nextBlock() {
        mBlockInd++;
        mTrialInd = 0;
        mBlock = mBlocks.get(mBlockInd);
        mNTrialsInBlock = mBlock.getNTrials();
        mTrial = mBlock.getTrial(mTrialInd);

        mGenInfo.blockNum = mBlockInd + 1;
        mGenInfo.trial = mTrial;

        // Log
        mBlockStTime = Utils.nowInMillis();

        // Sync the info to the Moose
        Server.get().send(new Memo(STRINGS.LOG, STRINGS.BLOCK_TRIAL,
                mBlockInd + 1, mTrialInd + 1)); // Block/trial *num*

        showTrial();
    }

    /**
     * Go to the next block
     */
    private void nextTechTask() {
        mTechTaskInd++;
        mBlockInd = 0;
        mTrialInd = 0;
        mBlocks = mExperiment.getTechTaskBlocks(mTechTaskInd);
        mBlock = mBlocks.get(mBlockInd);
        mNTrialsInBlock = mBlock.getNTrials();
        mTrial = mBlock.getTrial(mTrialInd);

        mGenInfo.blockNum = mBlockInd + 1;
        mGenInfo.trial = mTrial;

        // Log start times
        mBlockStTime = Utils.nowInMillis();
        mTechTaskStTime = Utils.nowInMillis();

        // Sync the info to the Moose
        Server.get().send(new Memo(
                STRINGS.LOG, STRINGS.TECHNIQUE,
                mTechs.get(mTechInd), mTechTaskInd + 1)); // Technique/techBlock
        Server.get().send(new Memo(STRINGS.LOG, STRINGS.BLOCK_TRIAL,
                mBlockInd + 1, mTrialInd + 1)); // Block/trial *num*

        showTrial();
    }

    /**
     * The current technique is ended
     */
    private void endTech() {
        mInBetweenTechs = true;
        repaint();

        showTechEndDialog();
    }

    /**
     * Go to the next block
     */
    private void nextTech() {
        mTechInd++;
        mTechTaskInd = 0;
        mBlockInd = 0;
        mTrialInd = 0;
        mBlocks = mExperiment.getTechTaskBlocks(mTechTaskInd);
        mBlock = mBlocks.get(mBlockInd);
        mTrial = mBlock.getTrial(mTrialInd);

        mGenInfo.tech = mTechs.get(mTechInd);
        mGenInfo.blockNum = mBlockInd + 1;
        mGenInfo.trial = mTrial;

        // Set the active technique
        Experiment.setActiveTechnique(mTechs.get(mTechInd));
        mTechLabel.setText("Technique: " + mTechs.get(mTechInd));
        if (mVTScrollPane != null) mVTScrollPane.changeTechnique(mTechs.get(mTechInd));
        if (mTDScrollPane != null) mTDScrollPane.changeTechnique(mTechs.get(mTechInd));

        // Log start times
        mBlockStTime = Utils.nowInMillis();
        mTechTaskStTime = Utils.nowInMillis();
        mTechStTime = Utils.nowInMillis();

        showTrial();
    }

    /**
     * The current technique is ended
     */
    private void endExp() {
        removeAll();
        showExpEndDialog();
    }

    /**
     * Show the short break in in each task
     */
    private void showShortBreak() {
        removeAll();
        mInShortBreak = true;
        add(mShortBreakLabel, 0);

        repaint();
    }

    /**
     * Show the long break between TechTasks
     */
    private void showLongBreak() {
        mInLongBreak = true;

        removeAll();
        repaint();
        MainFrame.get().showDialog(new BreakDialog());

        // ... back from the break
        mInLongBreak = false;
    }

    /**
     * Show the technique end dialog
     */
    public void showTechEndDialog() {
        JDialog dialog = new JDialog((JFrame)null, "Child", true);
        dialog.setSize(1000, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(800, 500);
        panel.setBackground(Color.decode("#90caf9"));
        panel.add(Box.createRigidArea(new Dimension(800, 100)));

        JLabel label = new JLabel(END_TECH_MESSAGES[mTechInd]);
        label.setFont(new Font("Sans", Font.BOLD, 30));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 200)));

        JButton button = new JButton("Continue");
        button.setMaximumSize(new Dimension(300, 50));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
                mInBetweenTechs = false;
                nextTech();
            }
        });
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusable(false);
        panel.add(button);

        dialog.add(panel);
        dialog.setUndecorated(true);
        dialog.setVisible(true);
    }

    /**
     * Show the technique end dialog
     */
    public void showExpEndDialog() {
        JDialog dialog = new JDialog((JFrame)null, "Child", true);
        dialog.setSize(1000, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(800, 500);
        panel.setBackground(Color.decode("#7986CB"));
        panel.add(Box.createRigidArea(new Dimension(800, 100)));

        JLabel label = new JLabel(END_EXPERIMENT_MESSAGE);
        label.setFont(new Font("Sans", Font.BOLD, 30));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 200)));

        JButton button = new JButton("Close");
        button.setMaximumSize(new Dimension(300, 50));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
                System.exit(0);
            }
        });
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusable(false);
        panel.add(button);

        dialog.add(panel);
        dialog.setUndecorated(true);
        dialog.setVisible(true);
    }

    /**
     * Get the display number for the block
     * @return Block num to show to the participants
     */
    private String getDisplayBlockStat() {
        return "";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        String TAG = NAME + "paintComponent";

        Graphics2D g2d = (Graphics2D) g;

        if (!mInBetweenTechs && !mInShortBreak && !mInLongBreak) { // If there is technique to show
            g2d.setColor(Consts.COLORS.CELL_HIGHLIGHT);
            if (mVtFrameRect.width != 0) {
                g2d.fillRect(
                        mVtFrameRect.x, mVtFrameRect.y,
                        mVtFrameRect.width, mVtFrameRect.height
                );
            }

            if (mHzFrameRect.width != 0) {
                g2d.fillRect(
                        mHzFrameRect.x, mHzFrameRect.y,
                        mHzFrameRect.width, mHzFrameRect.height
                );
            }
        }

    }

    /**
     * Play a sound
     * @param resFileName Sound file
     */
    private void playSound(String resFileName) {
        try {
            final ClassLoader classLoader = getClass().getClassLoader();
            final File soundFile = new File(Objects.requireNonNull(classLoader.getResource(resFileName)).getFile());
            final URL url = soundFile.toURI().toURL();

            Applet.newAudioClip(url).play();
        } catch ( NullPointerException
                | IOException e
        ) {
            e.printStackTrace();
        }
    }

    /**
     * Map the keys
     */
    private void mapKeys() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_SLASH = KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0, true);
        KS_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
        KS_Q = KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0, true);
        KS_A = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true);
        KS_W = KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true);
        KS_S = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true);
        KS_E = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, true);
        KS_D = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true);
        KS_RA = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true);
        KS_DA = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true);

        getInputMap().put(KS_SPACE, "SPACE");
        getInputMap().put(KS_SLASH, "SLASH");
        getInputMap().put(KS_ENTER, "ENTER");
        getInputMap().put(KS_Q, "Q");
        getInputMap().put(KS_A, "A");
        getInputMap().put(KS_W, "W");
        getInputMap().put(KS_S, "S");
        getInputMap().put(KS_E, "E");
        getInputMap().put(KS_D, "D");
        getInputMap().put(KS_RA, "RA");
        getInputMap().put(KS_DA, "DA");
    }

    // MouseMotionListenr ---------------------------------------------------------------------------------
    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        final long homingStTime = Logger.get().getHomingStTime();
        if (homingStTime != 0) { // Mocing the mouse after the break
            mTimeInfo.homingTime = Utils.nowInMillis() - homingStTime;
            Logger.get().logTimeInfo(mGenInfo, mTimeInfo); // Log TimeInfo
        }
    }

    //----------------------------------------------------------------------------------------------------
    private class ConfigAction extends AbstractAction {
        private final String TAG = "ExperimentPanel/" + "AdjustSensitivtyAction";
        private String mAction = "";
        private boolean mInc = false;

        public ConfigAction(String action, boolean inc) {
            mAction = action;
            mInc = inc;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (mAction) {
                case STRINGS.TECHNIQUE -> {
                    mConfigPanel.nextTechnique();
                    Controller.get().stopScroll();
                }
                case STRINGS.SENSITIVITY -> mConfigPanel.adjustSensitivity(mInc);
                case STRINGS.GAIN -> mConfigPanel.adjustGain(mInc);
                case STRINGS.DENOM -> mConfigPanel.adjustDenom(mInc);
            }
//
        }
    }


}
