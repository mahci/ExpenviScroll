package gui;

import control.Controller;
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
import java.util.Objects;

import static experiment.Experiment.*;
import static tools.Consts.*;

public class ExperimentPanel extends JLayeredPane {

    private final static String NAME = "ExperimentPanel/";
    // -------------------------------------------------------------------------------------------

    // Constants
    private double LR_MARGIN_mm = 20; // (mm) Left-right margin
    private double TB_MARGIN_mm = 20; // (mm) Left-right margin

    private final String HIT_SOUND = "hit.wav";
    private final String MISS_SOUND = "miss.wav";

    private KeyStroke KS_SPACE;
    private KeyStroke KS_SLASH;
    private KeyStroke KS_Q;
    private KeyStroke KS_A;
    private KeyStroke KS_W;
    private KeyStroke KS_S;
    private KeyStroke KS_E;
    private KeyStroke KS_D;

    // Experiment and trial
    private Experiment mExperiment;
    private Trial mTrial;

    // Elements
    private VTScrollPane mVTScrollPane;
    private HorizontalScrollPane hzScrollPane;
    private TDScrollPane mTDScrollPane;
    private JLabel mLabel;
    private JLabel levelLabel;
    private TechConfigPanel mConfigPanel;

    // Experiment
    private int mBlockNum = 1; // Round = 2 blocks
    private int mTrialNum = 0;
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
    private long mStartTime;

    // -------------------------------------------------------------------------------------------
    private final Action nextTrial = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int nTrials = mExperiment.getRound(mBlockNum).getNTrials();
            if (mTrialNum < nTrials) {
                mTrialNum++;
                mTrial = mExperiment.getRound(mBlockNum).getTrial(mTrialNum);
            } else {
                removeAll();
                mLabel = new JLabel("Thank you for your participation!");
                add(mLabel, 0);
            }
        }
    };

    private final Action mEndTrialAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            final String TAG = ExperimentPanel.NAME + "mEndTrialAction";

            if (mTrial != null) {
                if (checkSuccess()) playSound(HIT_SOUND);
                else playSound(MISS_SOUND);
                final long trialTime = System.currentTimeMillis() - mStartTime;
                Logs.d(TAG, "", "Trial Time = " + (trialTime / 1000.0));
            }

            remove(0);

            int nTrials = mExperiment.getRound(mBlockNum).getNTrials();
            Logs.d(TAG, "Number of Trials", nTrials);
            if (mTrialNum < nTrials) {
                // Time the trial
                mStartTime = System.currentTimeMillis();

                mTrialNum++;
                mTrial = mExperiment.getRound(mBlockNum).getTrial(mTrialNum);
                Logs.d(TAG, mBlockNum, mTrialNum);
                showTrial();
            } else {
                remove(0);
                mLabel = new JLabel("Thank you for your participation!");
                add(mLabel, 0);
            }
        }
    };

    private final Action mRandomTrialAction = new AbstractAction() {
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

    private final Action mEnableScrollingAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hzScrollPane.setWheelScrollingEnabled(enabled);
            Logs.infoAll(NAME, "Scrolling enabled");
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
     *
     * @param exp Experiment to show
     */
    public ExperimentPanel(Experiment exp) {
        String TAG = NAME;
        setLayout(null);

        // Set key actions
        mapKeys();
        getActionMap().put("SPACE", mEndTrialAction);
        getActionMap().put("SLASH", mNextTechnique);
        getActionMap().put("Q", mIncSensitivity);
        getActionMap().put("A", mDecSensitivity);
        getActionMap().put("W", mIncGain);
        getActionMap().put("S", mDecGain);
        getActionMap().put("E", mIncDenom);
        getActionMap().put("D", mDecDenom);

        // Set the experiment
        mExperiment = exp;

        // Add the start label
        mLabel = new JLabel(STRINGS.WELCOME_MESSAGE, JLabel.CENTER);
        mLabel.setFont(new Font("Sans", Font.BOLD, 35));
        mLabel.setBounds(800, 500, 1000, 400);
        add(mLabel, 0);

        // Add paramter controls
        mConfigPanel = new TechConfigPanel();
        mConfigPanel.setBounds(1000, 1200, 800, 30);
        add(mConfigPanel, 1);

        // Create levellabel (don't show yet)
        levelLabel = new JLabel("", JLabel.CENTER);
        levelLabel.setFont(new Font("Sans", Font.PLAIN, 18));
        levelLabel.setBounds(2300, 30, 200, 100);
        add(levelLabel, 2);
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

    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";
        Logs.d(TAG, "Mode", mTrial.getScrollMode().toString());

        // If panes aren't created, create them (only once)
        if (mVTScrollPane == null || mTDScrollPane == null) createPanes();

        // Reset the frames
        mVtFrameRect = new Rectangle();
        mHzFrameRect = new Rectangle();

        // Show the trial
        switch (mTrial.getScrollMode()) {
            case VERTICAL -> {
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

                // Center to a random line (based on the distance and highlited line
                int centerLineInd = 0;
                if (mTrial.getDirection() == DIRECTION.N) centerLineInd = targetLineInd + mTrial.getVtDist();
                else centerLineInd = targetLineInd - mTrial.getVtDist();
                mVTScrollPane.centerLine(centerLineInd);
                Logs.d(TAG, targetLineInd, mTrial.getVtDist(), centerLineInd);
            }

            // Two-Dim
            case TWO_DIM -> {
                mPaneDim = mTDScrollPane.getPreferredSize();
                mPanePos = getRandPosition(mPaneDim);
                mTDScrollPane.setBounds(
                        mPanePos.x, mPanePos.y,
                        mPaneDim.width, mPaneDim.height
                );
                mLasPanePos = mPanePos;

                // Random target and scroll position
                Pair targetInd = randTdInd();
                mTDScrollPane.highlight(targetInd, mTrial.getFrame());
//                mTDScrollPane.highlight(new Pair(60, 100), mTrial.getFrame());
//                mTDScrollPane.centerCell(new Pair(150, 100));

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
                final int vtDist = mTrial.getVtDist();
                final int hzDist = mTrial.getHzDist();
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
        levelLabel.setText("Trial: " + mTrialNum + " | Block: " + mBlockNum);

        revalidate();
        repaint();
    }

    private boolean checkSuccess() {
        boolean result = false;
        switch (mTrial.getScrollMode()) {
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
        String TAG = NAME + "randVtScrollValue";
        int offset = (mVTScrollPane.getNVisibleLines() - mTrial.getFrame()) / 2;

//        int minInd = offset + 1;
//        int maxInd = vtScrollPane.getNLines() - offset;
        // General min/max
        int minInd = mVTScrollPane.getNVisibleLines(); // No highlight at the top
        int maxInd = (mVTScrollPane.getNLines() - 1) - mVTScrollPane.getNVisibleLines();
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
                vtInd.move(0, -mTrial.getVtDist());
                hzInd.move(mTrial.getHzDist(), 0);
            }
            case NW -> {
                vtInd.move(0, -mTrial.getVtDist());
                hzInd.move(0, -mTrial.getHzDist());
            }
            case SE -> {
                vtInd.move(mTrial.getVtDist(), 0);
                hzInd.move(mTrial.getHzDist(), 0);
            }
            case SW -> {
                vtInd.move(mTrial.getVtDist(), 0);
                hzInd.move(0, -mTrial.getHzDist());
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

        boolean isScrolled = false;

        Logs.d(TAG, "Scrolling", vtScrollAmt, hzScrollAmt);

        if (mTrial != null) {
            switch (mTrial.getScrollMode()) {
                case VERTICAL -> mVTScrollPane.scroll(vtScrollAmt);
                case TWO_DIM -> mTDScrollPane.scroll(vtScrollAmt, hzScrollAmt);
            }
        }

    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        String TAG = NAME + "paintComponent";

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
        KS_Q = KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0, true);
        KS_A = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true);
        KS_W = KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true);
        KS_S = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true);
        KS_E = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, true);
        KS_D = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true);

        getInputMap().put(KS_SPACE, "SPACE");
        getInputMap().put(KS_SLASH, "SLASH");
        getInputMap().put(KS_Q, "Q");
        getInputMap().put(KS_A, "A");
        getInputMap().put(KS_W, "W");
        getInputMap().put(KS_S, "S");
        getInputMap().put(KS_E, "E");
        getInputMap().put(KS_D, "D");
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
