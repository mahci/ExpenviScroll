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

    // Experiment and trial
    private Experiment experiment;
    private Trial trial;

    // Elements
    private VTScrollPane vtScrollPane;
    private HorizontalScrollPane hzScrollPane;
    private TDScrollPane tdScrollPane;
    private JLabel label;
    private TechConfigPanel mConfigPanel;

    // Experiment
    private Point panePosition; // Position of the scrolling pane
    private int blockNum = 1; // Round = 2 blocks
    private int trialNum = 1;
    private boolean isPaneSet;
    private int targetColNum, randColNum;
    private int targetLineNum, randLineNum;
    private boolean isScrollingEnabled = false;
//    private int frameSize = 3;
    private boolean paintTrial;
    private Rectangle mVtFrameRect = new Rectangle();
    private Rectangle mHzFrameRect = new Rectangle();

    // Keys
    private final KeyStroke KS_SPACE =
            KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
    private final KeyStroke KS_SLASH =
            KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0, true);
    private final KeyStroke KS_Q =
            KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0, true);
    private final KeyStroke KS_A =
            KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true);
    private final KeyStroke KS_W =
            KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true);
    private final KeyStroke KS_S =
            KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true);
    private final KeyStroke KS_E =
            KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, true);
    private final KeyStroke KS_D =
            KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true);


    // TEMP
    Point mPanePos = new Point();
    Dimension mPaneDim = new Dimension();
    boolean isVt;

    // -------------------------------------------------------------------------------------------
    private final Action nextTrial = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int nTrials = experiment.getRound(blockNum).getNTrials();
            if (trialNum < nTrials) {
                trialNum++;
                trial = experiment.getRound(blockNum).getTrial(trialNum);
            } else {
                removeAll();
                label = new JLabel("Thank you for your participation!");
                add(label, 0);
            }
        }
    };

    private final Action mEndTrialAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (trial != null) {
                if (checkSuccess()) playSound(HIT_SOUND);
                else playSound(MISS_SOUND);
            }

            remove(0);
//            int nTrials = experiment.getRound(blockNum).getNTrials();
//            if (trialNum < nTrials) {
//                trialNum++;
//                trial = experiment.getRound(blockNum).getTrial(trialNum);
//
//                showTrial();
//            } else {
//                remove(0);
//                label = new JLabel("Thank you for your participation!");
//                add(label, 0);
//            }

            // Get a random-generated trial from experiment and show it
            if (isVt) {
                trial = experiment.randVtTrial();
                isVt = false;
            } else {
                trial = experiment.randTdTrial();
                isVt = true;
            }
            showTrial();
        }
    };

    private final Action randomTrial = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            removeAll();
            // Get a random-generated trial from experiment and show it
            if (isVt) {
                trial = experiment.randVtTrial();
                isVt = false;
            } else {
                trial = experiment.randTdTrial();
                isVt = true;
            }
            showTrial();
        }
    };

    private final Action enableScrolling = new AbstractAction() {
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

        // Set the experiment
        experiment = exp;

        // Map the keys
        getInputMap().put(KS_SPACE, "SPACE");
        getInputMap().put(KS_SLASH, "SLASH");
        getInputMap().put(KS_Q, "Q");
        getInputMap().put(KS_A, "A");
        getInputMap().put(KS_W, "W");
        getInputMap().put(KS_S, "S");
        getInputMap().put(KS_E, "E");
        getInputMap().put(KS_D, "D");

        getActionMap().put("SPACE", mEndTrialAction);
        getActionMap().put("SLASH", mNextTechnique);
        getActionMap().put("Q", mIncSensitivity);
        getActionMap().put("A", mDecSensitivity);
        getActionMap().put("W", mIncGain);
        getActionMap().put("S", mDecGain);
        getActionMap().put("E", mIncDenom);
        getActionMap().put("D", mDecDenom);

        // Add the start label
        label = new JLabel("Press SPACE to start the experiment", JLabel.CENTER);
        label.setFont(new Font("Sans", Font.BOLD, 35));
        label.setBounds(850, 500, 1000, 400);
        add(label, 0);

        // Add paramter controls
        mConfigPanel = new TechConfigPanel();
//        configPanel.setBounds(20, 1000, 800, 30);
        mConfigPanel.setBounds(1000, 1200, 800, 30);
        add(mConfigPanel, 1);
    }

    /**
     * Create one instance of each pane
     */
    private void createPanes() {
        String TAG = NAME + "createPanes";

        tdScrollPane = new TDScrollPane(TD_N_VIS_ROWS, TD_CELL_SIZE_mm, TD_SCROLL_BAR_W_mm)
                .setTable(TD_N_ROWS)
                .setScrollBars(TD_SCROLL_BAR_W_mm, TD_SCROLL_THUMB_L_mm)
                .create();

        // Make vtScrollPane the same size as the td
        vtScrollPane = new VTScrollPane(tdScrollPane.getPreferredSize())
                .setText("lorem.txt", VT_WRAP_CHARS_COUNT, VT_TEXT_FONT_SIZE)
                .setScrollBar(VT_SCROLL_BAR_W_mm, VT_SCROLL_THUMB_H_mm)
                .create();


    }

    private void showTrial() {
        String TAG = NAME + "showTrial";
        Logs.d(TAG, "Mode", trial.getScrollMode().toString());

        // If panes aren't created, create them (only once)
        if (vtScrollPane == null || tdScrollPane == null) createPanes();

        // Reset the frames
        mVtFrameRect = new Rectangle();
        mHzFrameRect = new Rectangle();

        // Show the trial
        switch (trial.getScrollMode()) {
            case VERTICAL -> {
                mPaneDim = vtScrollPane.getPreferredSize();
                mPanePos = getRandPosition(mPaneDim);

                vtScrollPane.setBounds(mPanePos.x, mPanePos.y, mPaneDim.width, mPaneDim.height);
                vtScrollPane.highlight(randVtLineInd(), trial.getFrame());

                add(vtScrollPane, 0);

                // Set frame to be drawn (by paintComponent())
                int lineH = vtScrollPane.getLineHeight();

                mVtFrameRect.width = Utils.mm2px(experiment.TD_FRAME_H_mm);
                mVtFrameRect.height = trial.getFrame() * lineH;
                mVtFrameRect.x = mPanePos.x - mVtFrameRect.width;
                mVtFrameRect.y = mPanePos.y + ((vtScrollPane.getNVisibleLines() - trial.getFrame()) / 2) * lineH;
            }
            case TWO_DIM -> {
                mPaneDim = tdScrollPane.getPreferredSize();
                mPanePos = getRandPosition(mPaneDim);

                tdScrollPane.setBounds(mPanePos.x, mPanePos.y, mPaneDim.width, mPaneDim.height);

                Pair<Integer, Integer> hlInd = randTdInd();
                tdScrollPane.highlight(hlInd.getFirst(), hlInd.getSecond(), trial.getFrame());

                add(tdScrollPane, 0);

                // Set frames to be drawn (by paintComponent())
                final int cellSize = Utils.mm2px(experiment.TD_CELL_SIZE_mm);
                final int frameH = Utils.mm2px(experiment.TD_FRAME_H_mm);

                mVtFrameRect.width = frameH;
                mVtFrameRect.height = trial.getFrame() * cellSize;
                mVtFrameRect.x = mPanePos.x - mVtFrameRect.width;
                mVtFrameRect.y = mPanePos.y + ((experiment.TD_N_VIS_ROWS - trial.getFrame()) / 2) * cellSize;

                mHzFrameRect.width = trial.getFrame() * cellSize;
                mHzFrameRect.height = frameH;
                mHzFrameRect.x = mPanePos.x + ((experiment.TD_N_VIS_ROWS - trial.getFrame()) / 2) * cellSize;
                mHzFrameRect.y = mPanePos.y - mHzFrameRect.height;
            }
        }

        paintTrial = true;
        revalidate();
        repaint();
    }

    private boolean checkSuccess() {
        boolean result = false;
        switch (trial.getScrollMode()) {
        case VERTICAL -> {
            final int vtScrollVal = vtScrollPane.getVerticalScrollBar().getValue();
            result = vtScrollPane.isInsideFrames(vtScrollVal);
        }
        case TWO_DIM -> {
            final int vtScrollVal = tdScrollPane.getVerticalScrollBar().getValue();
            final int hzScrollVal = tdScrollPane.getHorizontalScrollBar().getValue();
            result = tdScrollPane.isInsideFrames(vtScrollVal, hzScrollVal);
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

        int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        int tbMargin = Utils.mm2px(TB_MARGIN_mm);

        int minX = lrMargin;
        int maxX = getWidth() - (lrMargin + paneDim.width);

        int midY = (getHeight() - paneDim.height) / 2;
//        int minY = tbMargin;
//        int maxY = getHeight() - (tbMargin + paneDim.height);

        if (minX >= maxX) return new Point(); // Invalid dimensions
        else return new Point(Utils.randInt(minX, maxX), midY);
    }

    /**
     * Get a random line index
     * NOTE: Indexes start from 1
     * @return A random line index
     */
    private int randVtLineInd() {
        String TAG = NAME + "randVtScrollValue";
        int offset = (vtScrollPane.getNVisibleLines() - trial.getFrame()) / 2;
        int minInd = offset + 1;
        int maxInd = vtScrollPane.getNLines() - offset;
        Logs.d(TAG, "values", minInd, maxInd);
        return Utils.randInt(minInd, maxInd);
    }

    /**
     * Get random row,col indexes
     * @return Pair (row, col)
     */
    private Pair<Integer, Integer> randTdInd() {
        String TAG = NAME + "randTdInd";
        int offset = (experiment.TD_N_VIS_ROWS - trial.getFrame()) / 2;
        int minInd = offset + 1;
        int maxInd = experiment.TD_N_ROWS - offset;
        Logs.d(TAG, "values", minInd, maxInd);
        Pair<Integer, Integer> result = Pair.of(
                Utils.randInt(minInd, maxInd),
                Utils.randInt(minInd, maxInd));
        return result;
    }


    /**
     * Scroll the 2D scrollPane
     * @param vtScrollAmt Vertical scroll amount
     * @param hzScrollAmt Horizontal scroll amount
     */
    public void scroll(int vtScrollAmt, int hzScrollAmt) {
        String TAG = NAME + "scroll";

        boolean isScrolled = false;

        Logs.d(TAG, "Scrolling", vtScrollAmt, hzScrollAmt);

        switch (trial.getScrollMode()) {
        case VERTICAL -> vtScrollPane.scroll(vtScrollAmt);
        case TWO_DIM -> tdScrollPane.scroll(vtScrollAmt, hzScrollAmt);
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
