package gui;

import control.Logger;
import control.Server;
import experiment.Block;
import experiment.Experiment;
import experiment.Trial;
import lombok.extern.java.Log;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import static experiment.Experiment.*;
import static experiment.Experiment.VT_SCROLL_THUMB_H_mm;
import static tools.Consts.*;

public class BlockPanel extends JLayeredPane implements MouseMotionListener {
    private final String NAME = "BlockPanel/";

    // Block and related
    private Block mBlock;
    private int mNBlocks; // Just for show
    private int mNSuccessTrials; // Number of successful trials so far

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_RA; // Right arrow

    // Layout & elements
    private Dimension mDim = new Dimension();
    private Point mLasPanePos = new Point();
    private VTScrollPane mVTScrollPane;
    private TDScrollPane mTDScrollPane;
    private Rectangle mVtFrameRect = new Rectangle();
    private Rectangle mHzFrameRect = new Rectangle();
    private JLabel mLevelLabel;
    private JLabel mTechLabel;

    // Logging
    private Logger.GeneralInfo mGenInfo;
    private Logger.TimeInfo mTimeInfo;
    private Logger.InstantInfo mInstantInfo; // Started from here, passed to the scrollPanes, then back to be finished
    private long mTrialStTime;
    private long mBlockStTime;

    // Actions ------------------------------------------------------------------------------------

    // End a trial
    private final Action END_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            final String TAG = BlockPanel.this.NAME +  "END_TRIAL";
            Logs.d(TAG, "End trial");
            final Pair trialResult = checkHit();

            // Log InstantInfo
            Logger.InstantInfo instInfo = new Logger.InstantInfo();
            int nTargetAppear;
            if (mGenInfo.trial.getTask() == Experiment.TASK.VERTICAL) {
                instInfo = mVTScrollPane.getInstantInfo();
                nTargetAppear = mVTScrollPane.getNTargetAppear();
            } else {
                instInfo = mTDScrollPane.getInstantInfo();
                nTargetAppear = mTDScrollPane.getNTargetAppear();
            }
            instInfo.trialEnd = Utils.nowInMillis();
            Logger.get().logInstantInfo(mGenInfo, instInfo);

            // Log TrialInfo
            Logger.TrialInfo trialInfo = new Logger.TrialInfo(
                    (int) (instInfo.targetLastAppear - instInfo.firstScroll),
                    (int) (instInfo.lastScroll - instInfo.targetLastAppear),
                    (int) (instInfo.lastScroll - instInfo.firstScroll),
                    (int) (Utils.nowInMillis() - instInfo.firstScroll),
                    (int) (Utils.nowInMillis() - instInfo.lastScroll),
                    nTargetAppear,
                    trialResult.getFirst(),
                    trialResult.getSecond(),
                    trialResult.fXs());
            Logger.get().logTrialInfo(mGenInfo, trialInfo);

            // Record Trial time
            mTimeInfo = new Logger.TimeInfo();
            mTimeInfo.trialTime = Utils.nowInMillis() - mTrialStTime;

            // Was the trial a success or a fail?
            if (trialResult.fXs() == 1) {
                SOUNDS.play(STRINGS.HIT);
                mNSuccessTrials++;
            }
            else { // Miss
                SOUNDS.play(STRINGS.MISS);
                mBlock.dupeShuffleTrial(mGenInfo.trialNum - 1); // Shuffle the trial into the rest of trials
            }

            // Moving forward
            if (mGenInfo.trialNum < mBlock.getNTrials()) { // More trials in the block
                Logger.get().logTimeInfo(mGenInfo, mTimeInfo); // Log trial TimeInfo

                // Reset the panes values
                mTDScrollPane.reset();
                mVTScrollPane.reset();

                nextTrial();

            } else { // Block is finished
                // Log block time
                mTimeInfo.blockTime = (Utils.nowInMillis() - mBlockStTime) / 1000;
                // Back to the ExperimentFrame
                ExperimentFrame.get().blockFinished(mTimeInfo);
            }
        }
    };

    // Jump to the next trial (without check)
    private final Action ADVANCE_TRIAL = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            remove(0);
            mNSuccessTrials++;

            if (mGenInfo.trialNum < mBlock.getNTrials()) { // More trials in the block
                // Reset the panes values
                mTDScrollPane.reset();
                mVTScrollPane.reset();

                nextTrial();

            } else {
                ExperimentFrame.get().blockFinished(mTimeInfo);
            }
        }
    };


    // Methods ------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param dim Desired dimension of the panel
     * @param nBlocks Total number of blocks (to show on the label)
     */
    public BlockPanel(Dimension dim, int nBlocks) {
        setSize(dim);
        setLayout(null);
        mNBlocks = nBlocks;

        // Create levellabel (don't show yet)
        mLevelLabel = new JLabel("", JLabel.CENTER);
        mLevelLabel.setFont(new Font("Sans", Font.PLAIN, 18));
        mLevelLabel.setBounds(2100, 30, 400, 100);

        // Create tehcnique label
        mTechLabel = new JLabel("", JLabel.CENTER);
        mTechLabel.setFont(new Font("Sans", Font.PLAIN, 18));
        mTechLabel.setBounds(50, 30, 200, 100);

        // Key maps
        mapKeys();

    }

    /**
     * Set the data to show
     * @param block Block
     * @param genInfo GeneralInfo
     * @param timeInfo TimeInfo
     * @return Instance
     */
    public BlockPanel setData(Block block, Logger.GeneralInfo genInfo, Logger.TimeInfo timeInfo) {
        mBlock = block;
        mGenInfo = genInfo;
        mTimeInfo = timeInfo;

        return this;
    }

    @Override
    public void addNotify() {
        super.addNotify();

        // Set the key mappings
        getActionMap().put(KeyEvent.VK_SPACE, END_TRIAL);
        getActionMap().put(KeyEvent.VK_RIGHT, ADVANCE_TRIAL);

        // Start the block timer
        mBlockStTime = Utils.nowInMillis();

        // Start with the first trial
        mGenInfo.trialNum = 1;
        mGenInfo.trial = mBlock.getTrial(mGenInfo.trialNum - 1);
        showTrial();
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
                .setText("./res/lorem-new")
                .setScrollBar(VT_SCROLL_BAR_W_mm, VT_SCROLL_THUMB_H_mm)
                .create();

        // Set to the wheel if technique is wheel
        mTDScrollPane.setWheelEnabled(mGenInfo.tech.equals(TECHNIQUE.MOUSE));
        mVTScrollPane.setWheelEnabled(mGenInfo.tech.equals(TECHNIQUE.MOUSE));

    }

    /**
     * Check if each axes of a trial was a hit (1) or a miss (0)
     * Vertical -> hzResult = -1
     * @return Pair of int for each dimension
     */
    private Pair checkHit() {
        if (mGenInfo.trial.getTask() == Experiment.TASK.VERTICAL) {
            return new Pair(mVTScrollPane.isTargetInFrame(), -1);
        } else {
            return mTDScrollPane.isTargetInFrames();
        }
    }

    /**
     * Go to the next trial
     */
    private void nextTrial() {
        final String TAG = NAME + "nextTrial";

        mGenInfo.trialNum++;
        mGenInfo.trial = mBlock.getTrial(mGenInfo.trialNum - 1);

        // Sync the info to the Moose
        Server.get().send(new Memo(STRINGS.LOG, STRINGS.BLOCK + "_" + STRINGS.TRIAL,
                mGenInfo.blockNum, mGenInfo.trialNum)); // Block/trial *num*

        showTrial();
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        mTrialStTime = Utils.nowInMillis(); // Start timer

        // Start the InstantInfo logging
        mInstantInfo = new Logger.InstantInfo();
        mInstantInfo.trialShow = Utils.nowInMillis();

        // If panes aren't created, create them (only once)
        if (mVTScrollPane == null || mTDScrollPane == null) createPanes();

        // Reset the frames
        mVtFrameRect = new Rectangle();
        mHzFrameRect = new Rectangle();

        // Show the trial
        final Trial trial = mGenInfo.trial;
        switch (trial.getTask()) {

            case VERTICAL -> {
                // Pass GeneralInfo and InstantInfo to the pane
                mVTScrollPane.setGenInfo(mGenInfo);
                mVTScrollPane.setInstantInfo(mInstantInfo);

                // Position
                final Dimension paneDim = mVTScrollPane.getPreferredSize();
                final Point randPos = getRandPosition(paneDim);
                mVTScrollPane.setBounds(randPos.x, randPos.y, paneDim.width, paneDim.height);
                mLasPanePos = randPos;

                // Highlight
                final int targetLineInd = randVtLineInd();
                mVTScrollPane.highlight(targetLineInd, trial.getFrame());
                add(mVTScrollPane, 0);

                // Set frame to be drawn (by paintComponent())
                int lineH = mVTScrollPane.getLineHeight();

                mVtFrameRect.width = Utils.mm2px(Experiment.TD_FRAME_H_mm);
                mVtFrameRect.height = trial.getFrame() * lineH;
                mVtFrameRect.x = randPos.x - mVtFrameRect.width;
                mVtFrameRect.y = randPos.y + ((mVTScrollPane.getNVisibleLines() - trial.getFrame()) / 2) * lineH;

                // Center to a random line (based on the distance and highlited line)
                int centerLineInd = 0;

                if (trial.getDirection() == Experiment.DIRECTION.N)
                    centerLineInd = targetLineInd + trial.getVtDist();
                else
                    centerLineInd = targetLineInd - trial.getVtDist();

                mVTScrollPane.centerLine(centerLineInd);
            }

            // Two-Dim
            case TWO_DIM -> {
                // Pass GeneralInfo and InstantInfo for logging
                mTDScrollPane.setGenInfo(mGenInfo);
                mTDScrollPane.setInstantInfo(mInstantInfo);

                // Position
                final Dimension paneDim = mTDScrollPane.getPreferredSize();
                final Point randPos = getRandPosition(paneDim);
                mTDScrollPane.setBounds(randPos.x, randPos.y, paneDim.width, paneDim.height);
                mLasPanePos = randPos;
                Logs.d(TAG, randPos);
                // Select and highligght the target
                Pair targetInd = randTdCellInd();
                mTDScrollPane.highlight(targetInd, trial.getFrame());

                add(mTDScrollPane, 0);

                // Set frames to be drawn (by paintComponent())
                final int cellSize = Utils.mm2px(Experiment.TD_CELL_SIZE_mm);
                final int frameH = Utils.mm2px(Experiment.TD_FRAME_H_mm);

                mVtFrameRect.width = frameH;
                mVtFrameRect.height = trial.getFrame() * cellSize;
                mVtFrameRect.x = randPos.x - mVtFrameRect.width;
                mVtFrameRect.y = randPos.y + ((Experiment.TD_N_VIS_ROWS - trial.getFrame()) / 2) * cellSize;

                mHzFrameRect.width = trial.getFrame() * cellSize;
                mHzFrameRect.height = frameH;
                mHzFrameRect.x = randPos.x + ((Experiment.TD_N_VIS_ROWS - trial.getFrame()) / 2) * cellSize;
                mHzFrameRect.y = randPos.y - mHzFrameRect.height;

                // Center to a random cell (based on the dists and highlited cell)
                final Pair centerInd = new Pair();
                final int targetRow = targetInd.getFirst();
                final int targetCol = targetInd.getSecond();
                final int vtDist = trial.getTdDist();
                final int hzDist = trial.getTdDist();

                switch (trial.getDirection()) {
                    case NE -> centerInd.set(targetRow + vtDist, targetCol - hzDist);
                    case NW -> centerInd.set(targetRow + vtDist, targetCol + hzDist);
                    case SE -> centerInd.set(targetRow - vtDist, targetCol - hzDist);
                    case SW -> centerInd.set(targetRow - vtDist, targetCol + hzDist);
                }

                mTDScrollPane.centerCell(centerInd);
            }
        }

        // Show level label
        mLevelLabel.setText("Block: " + mGenInfo.blockNum + " / " + mNBlocks +
                " --- " +
                "Successful Trials: " + mNSuccessTrials + "  / " +  mBlock.getTargetNTrials());
        mTechLabel.setText("Technique: " + mGenInfo.tech);
        add(mLevelLabel, 1);
        add(mTechLabel, 1);

        revalidate();
        repaint();
    }

    /**
     * Generate a random position for a pane
     * Based on the size and dimensions of the displace area
     * @param paneDim Dimension of the pane
     * @return A random position
     */
    private Point getRandPosition(Dimension paneDim) {
        String TAG = NAME + "randPosition";

        final int lrMargin = Utils.mm2px(DISP.LR_MARGIN_mm);

        final int minX = lrMargin;
        final int maxX = getWidth() - (lrMargin + paneDim.width);

        final int midY = (getHeight() - paneDim.height) / 2;
        Logs.d(TAG, lrMargin, minX, maxX, midY);
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

        // Correction based on direction
        if (mGenInfo.trial.getDirection() == DIRECTION.N) {
            maxInd -= mGenInfo.trial.getVtDist();
        } else {
            minInd += mGenInfo.trial.getVtDist();
        }

        return mVTScrollPane.getRandLine(minInd, maxInd);
    }

    /**
     * Get random row,col indexes
     * @return Pair (row, col)
     */
    private Pair randTdCellInd() {
        String TAG = NAME + "randTdCellInd";


        // General threshold
        final MinMax vtInd = new MinMax(TD_N_VIS_ROWS + 1, (TD_N_ROWS - 1) - TD_N_VIS_ROWS);
        final MinMax hzInd = new MinMax(TD_N_VIS_ROWS + 1, (TD_N_ROWS - 1) - TD_N_VIS_ROWS);
        // Correction based on direction
        final Trial trial = mGenInfo.trial;
        switch (trial.getDirection()) {
            case NE -> {
                vtInd.move(0, -trial.getTdDist());
                hzInd.move(trial.getTdDist(), 0);
            }
            case NW -> {
                vtInd.move(0, -trial.getTdDist());
                hzInd.move(0, -trial.getTdDist());
            }
            case SE -> {
                vtInd.move(trial.getTdDist(), 0);
                hzInd.move(trial.getTdDist(), 0);
            }
            case SW -> {
                vtInd.move(trial.getTdDist(), 0);
                hzInd.move(0, -trial.getTdDist());
            }
        }

        Pair result = new Pair(Utils.randIntBetween(vtInd), Utils.randIntBetween(hzInd));
        return result;
    }

    /**
     * Scroll the scrollPanes
     * @param vtScrollAmt Vertical scroll amount
     * @param hzScrollAmt Horizontal scroll amount
     */
    public void scroll(int vtScrollAmt, int hzScrollAmt) {
        String TAG = NAME + "scroll";

        switch (mGenInfo.trial.getTask()) {
            case VERTICAL -> mVTScrollPane.scroll(vtScrollAmt);
            case TWO_DIM -> mTDScrollPane.scroll(vtScrollAmt, hzScrollAmt);
        }
    }

    // -------------------------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final String TAG = NAME + "paintComponent";

        Graphics2D g2d = (Graphics2D) g;
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

    private void mapKeys() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_RA = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true);

        getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
        getInputMap().put(KS_RA, KeyEvent.VK_RIGHT);
    }

    // -------------------------------------------------------------------------------------------
    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
