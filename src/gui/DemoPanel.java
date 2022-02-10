package gui;

import experiment.Experiment;
import experiment.Trial;
import tools.*;

import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static experiment.Experiment.*;
import static experiment.Experiment.VT_SCROLL_THUMB_H_mm;
import static tools.Consts.SOUNDS.*;
import static tools.Consts.STRINGS.*;
import static tools.Consts.*;

public class DemoPanel extends JPanel {
    private final static String NAME = "DemoPanel/";
    // -------------------------------------------------------------------------------------------

    // Margins
    private double LR_MARGIN_mm = 20; // (mm) Left-right margin

    // Keystrokes
    private KeyStroke KS_SPACE;
    private KeyStroke KS_ENTER;
    private KeyStroke KS_SLASH;
    private KeyStroke KS_SHIFT;

    // Experiment
    private Experiment mExperiment;
    private Trial mTrial;
    private int mTechInd;
    private List<Experiment.TECHNIQUE> mTechs;

    // Elements
    private VTScrollPane mVTScrollPane;
    private TDScrollPane mTDScrollPane;
    private JPanel mPanel;
    private JLabel mTechLabel;
    private JLabel mTitleLabel;
    private JLabel mNextLabel;
    private JButton mNextButton;
    private Rectangle mVtFrameRect = new Rectangle();
    private Rectangle mHzFrameRect = new Rectangle();
    private AudioClip mHitSound, mMissSound;

    // Other
    private Point mPanePos = new Point();
    private Point mLasPanePos = new Point();
    private Dimension mPaneDim = new Dimension();
    private boolean isVT = false;

    private final Action END_DEMO_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            MainFrame.get().showExperiment();
        }
    };

    private final Action RAND_TRIAL_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {

            // Was the trial a success or a fail?
            if (checkHit().fXs() == 1){
                mHitSound.play();
            }
            else { // Miss
                mMissSound.play();
            }

            remove(1);
            randomTrial();
        }
    };

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

    private final Action SHIFT_KEY_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Logs.d(DemoPanel.NAME, "Shift key released");
        }
    };

    // -------------------------------------------------------------------------------------------

    public DemoPanel(Experiment exp) {
        final String TAG = NAME;

        setLayout(null);
        setBackground(COLORS.PANEL_BG);

        mExperiment = exp;
        mTechs = exp.getPcTechs();
        mTechInd = 0;

        // Map keys
        mapKeys();
        getActionMap().put("ENTER", END_DEMO_ACTION);

        // Show elements
        final int labelY = 400;
        final int labelX = 750;
        mTitleLabel = new JLabel(DEMO_TITLE, JLabel.CENTER);
        mTitleLabel.setFont(new Font("Sans", Font.BOLD, 35));
        mTitleLabel.setBounds(labelX, labelY, 1000, 400);
        add(mTitleLabel, 0);

        mTechLabel = new JLabel("Technique: " + mTechs.get(mTechInd), JLabel.CENTER);
        mTechLabel.setFont(new Font("Sans", Font.PLAIN, 18));
        mTechLabel.setBounds(50, 30, 200, 100);

        mNextButton = new JButton(DEMO_NEXT);
        mNextButton.setFont(new Font("Sans", Font.PLAIN, 20));
        mNextButton.setBounds(labelX + 350, labelY + 300, 300, 50);
        mNextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show the first tech and start demo
                removeAll();
                add(mTechLabel, 0);
                randomTrial();

                // Map the actions
                getActionMap().put("SPACE", RAND_TRIAL_ACTION);
                getActionMap().put("SLASH", SWITCH_TECH_ACTION);
                getActionMap().put("SHIFT", SHIFT_KEY_ACTION);

                // Sync the the first technique
                Experiment.setActiveTechnique(mTechs.get(mTechInd));
                if (mVTScrollPane != null) mVTScrollPane.changeTechnique(mTechs.get(mTechInd));
                if (mTDScrollPane != null) mTDScrollPane.changeTechnique(mTechs.get(mTechInd));

            }
        });
        mNextButton.setFocusable(false);
        add(mNextButton, 0);
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
        Logs.d(TAG, mVTScrollPane.getLineHeight(), Utils.px2mm(mVTScrollPane.getLineHeight()));
        Logs.d(TAG, mTDScrollPane.getCellSize(), Utils.px2mm(mTDScrollPane.getCellSize()));
    }

    /**
     * Show a random trial
     */
    private void randomTrial() {

        // Get a random-generated trial from experiment and show it
        if (isVT) {
            mTrial = mExperiment.randVtTrial();
            isVT = false;
        } else {
            mTrial = mExperiment.randTdTrial();
            isVT = true;
        }

        showTrial();
    }

    /**
     * Show the trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        // If panes aren't created, create them (only once)
        if (mVTScrollPane == null || mTDScrollPane == null) createPanes();

        // Reset the frames
        mVtFrameRect = new Rectangle();
        mHzFrameRect = new Rectangle();

        // Show the trial
        switch (mTrial.getTask()) {
            case VERTICAL -> {

                // Position
                mPaneDim = mVTScrollPane.getPreferredSize();
                mPanePos = randPosition(mPaneDim);
                mVTScrollPane.setBounds(mPanePos.x, mPanePos.y, mPaneDim.width, mPaneDim.height);
                mLasPanePos = mPanePos;

                // Highlight
                int targetLineInd = randVtLineInd();
                mVTScrollPane.highlight(targetLineInd, mTrial.getFrame());
                Logs.d(TAG, "HLIndex= ", targetLineInd);
                add(mVTScrollPane, 1);

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

                // Position
                mPaneDim = mTDScrollPane.getPreferredSize();
                mPanePos = randPosition(mPaneDim);
                mTDScrollPane.setBounds(
                        mPanePos.x, mPanePos.y,
                        mPaneDim.width, mPaneDim.height
                );
                mLasPanePos = mPanePos;

                // Select and highligght the target
                Pair targetInd = randTdInd();
                mTDScrollPane.highlight(targetInd, mTrial.getFrame());

                add(mTDScrollPane, 1);

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

        revalidate();
        repaint();
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
     * Check if each axes of a trial was a hit (1) or a miss (0)
     * Vertical -> hzResult = 1
     * @return Pair of int for each dimension
     */
    private Pair checkHit() {
        if (mTrial.getTask() == TASK.VERTICAL) {
            return new Pair(mVTScrollPane.isTargetInFrame(), 1);
        } else {
            return mTDScrollPane.isTargetInFrames();
        }
    }

    /**
     * Generate a random position for a pane
     * Based on the size and dimensions of the displace area
     * @param paneDim Dimension of the pane
     * @return A random position
     */
    private Point randPosition(Dimension paneDim) {
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

        // General min/max
        int minInd = mVTScrollPane.getNVisibleLines(); // No highlight at the top
        int maxInd = (mVTScrollPane.getNLines() - 1) - mVTScrollPane.getNVisibleLines();
        // Correction based on direction
        if (mTrial.getDirection() == DIRECTION.N) {
            maxInd -= mTrial.getVtDist();
        } else {
            minInd += mTrial.getVtDist();
        }

        return mVTScrollPane.getRandLine(minInd, maxInd);
    }

    /**
     * Get random row,col indexes
     * @return Pair (row, col)
     */
    private Pair randTdInd() {
        String TAG = NAME + "randTdInd";

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
        Pair result = new Pair(Utils.randIntBetween(vtInd), Utils.randIntBetween(hzInd));

        return result;
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
     * Load all the sounds to play later
     */
    private void loadSounds() {
        try {
            final ClassLoader classLoader = getClass().getClassLoader();

            final URL hitURL = new File(Objects.requireNonNull(classLoader.getResource(HIT_SOUND))
                    .getFile()).toURI().toURL();
            final URL missURL = new File(Objects.requireNonNull(classLoader.getResource(MISS_SOUND))
                    .getFile()).toURI().toURL();

            mHitSound = Applet.newAudioClip(hitURL);
            mMissSound = Applet.newAudioClip(missURL);

        } catch ( NullPointerException
                | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Map the keys
     */
    private void mapKeys() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
        KS_SLASH = KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0, true);
        KS_SHIFT = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true);

        getInputMap().put(KS_SPACE, "SPACE");
        getInputMap().put(KS_ENTER, "ENTER");
        getInputMap().put(KS_SLASH, "SLASH");
        getInputMap().put(KS_SHIFT, "SHIFT");


    }




}
