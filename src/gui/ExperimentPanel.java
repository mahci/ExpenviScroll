package gui;

import control.Controller;
import experiment.Experiment;
import experiment.Trial;
import tools.*;

import javax.naming.ldap.Control;
import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

import static experiment.Experiment.*;

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

    // TEMP
    Point panePos = new Point();
    Dimension paneDim = new Dimension();
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
        getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_SPACE, 0, true),
                "SPACE");
//        getActionMap().put("SPACE", nextTrial);
//        getActionMap().put("SPACE", randomTrial);
        getActionMap().put("SPACE", mEndTrialAction);

        // Add the start label
        label = new JLabel("Press SPACE to start the experiment", JLabel.CENTER);
        label.setFont(new Font("Sans", Font.BOLD, 35));
        label.setBounds(850, 500, 1000, 400);
        add(label, 0);


        // [FOR TEST]
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    isScrollingEnabled = true;
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    JTable table = (JTable) tdScrollPane.getViewport().getView();
//                    ((JTable) tdScrollPane.getViewport().getView()).scrollRectToVisible(
//                            table.getCellRect(180, 100, true)
//                    );

                    Rectangle rect = table.getCellRect(1, 13, true);
                    Logs.info(TAG, "CellRect= " + rect);
                    Rectangle viewRect = tdScrollPane.getViewport().getViewRect();

//                    rect.setLocation(rect.x - viewRect.x, rect.y - viewRect.y);

                    int centerX = (viewRect.width - rect.width) / 2;
                    int centerY = (viewRect.height - rect.height) / 2;
                    if (rect.x < centerX) {
                        centerX = -centerX;
                    }
                    if (rect.y < centerY) {
                        centerY = -centerY;
                    }

//                    rect.translate(centerX, centerY);
//                    tdScrollPane.getViewport().scrollRectToVisible(rect);
//                    tdScrollPane.getVerticalScrollBar().setValue(1800);
//                    int cellSize = Utils.mm2px(experiment.TD_CELL_SIZE_mm);
//                    int frOffset = (experiment.TD_N_VIS_ROWS - frameSize) / 2;
//                    int frStartInd = frOffset + 1;
//                    int frEndInd = frOffset + frameSize;
//                    int maxNCells = frOffset + frameSize - 1;
//                    int minNCelss = frOffset;
                }

                if (e.getKeyCode() == KeyEvent.VK_UP) {
//                    Controller.get().testStopScroll();
//                    repaint();
//                    Controller.get().testScroll(-2);
                    scroll(-10, 0);
                }

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                    Controller.get().testStopScroll();
//                    repaint();
//                    Controller.get().testScroll(1);
                    scroll(10, 0);
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    scroll(0, 10);
                }

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    scroll(0, -10);
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    isScrollingEnabled = false;
                }
            }
        });

        addMouseWheelListener(e -> {
            Controller.get().testStopScroll();
//            repaint();
            Controller.get().testScroll(e.getWheelRotation());
//            scroll(10.0 * e.getWheelRotation(), 0.0);
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                Controller.get().testStopScroll();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

    }

    /**
     * Create one instance of each pane
     */
    private void createPanes() {
        String TAG = NAME + "createPanes";

        tdScrollPane = new TDScrollPane(TD_N_VIS_ROWS, TD_CELL_SIZE_mm, TD_SCROLL_BAR_W_mm)
                .setTable(TD_N_ROWS)
                .setScrollBars(TD_SCROLL_BAR_W_mm, TD_SCROLL_THUMB_L_mm);

        // Make vtScrollPane the same size as the td
        vtScrollPane = new VTScrollPane(tdScrollPane.getPreferredSize())
                .setText("lorem.txt", VT_WRAP_CHARS_COUNT, VT_TEXT_FONT_SIZE)
                .setScrollBar(VT_SCROLL_BAR_W_mm, VT_SCROLL_THUMB_H_mm);

    }

    private void showTrial() {
        String TAG = NAME + "showTrial";
        Logs.d(TAG, "Mode", trial.scrollMode().toString());

        // If panes aren't created, create them (only once)
        if (vtScrollPane == null || tdScrollPane == null) createPanes();

        // Reset the frames
        mVtFrameRect = new Rectangle();
        mHzFrameRect = new Rectangle();

        // Show the trial
        switch (trial.scrollMode()) {
            case VERTICAL -> {
                paneDim = vtScrollPane.getPreferredSize();
                panePos = getRandPosition(paneDim);

                vtScrollPane.setBounds(panePos.x, panePos.y, paneDim.width, paneDim.height);
                vtScrollPane.highlight(randVtLineInd(), trial.frame());

                add(vtScrollPane, 0);

                // Set frame to be drawn (by paintComponent())
                int lineH = vtScrollPane.getLineHeight();

                mVtFrameRect.width = Utils.mm2px(experiment.TD_FRAME_H_mm);
                mVtFrameRect.height = trial.frame() * lineH;
                mVtFrameRect.x = panePos.x - mVtFrameRect.width;
                mVtFrameRect.y = panePos.y + ((vtScrollPane.getNVisibleLines() - trial.frame()) / 2) * lineH;
            }
            case TWO_DIM -> {
                paneDim = tdScrollPane.getPreferredSize();
                panePos = getRandPosition(paneDim);

                tdScrollPane.setBounds(panePos.x, panePos.y, paneDim.width, paneDim.height);

                Pair<Integer, Integer> hlInd = randTdInd();
                tdScrollPane.highlight(hlInd.getFirst(), hlInd.getSecond(), trial.frame());

                add(tdScrollPane, 0);

                // Set frames to be drawn (by paintComponent())
                final int cellSize = Utils.mm2px(experiment.TD_CELL_SIZE_mm);
                final int frameH = Utils.mm2px(experiment.TD_FRAME_H_mm);

                mVtFrameRect.width = frameH;
                mVtFrameRect.height = trial.frame() * cellSize;
                mVtFrameRect.x = panePos.x - mVtFrameRect.width;
                mVtFrameRect.y = panePos.y + ((experiment.TD_N_VIS_ROWS - trial.frame()) / 2) * cellSize;

                mHzFrameRect.width = trial.frame() * cellSize;
                mHzFrameRect.height = frameH;
                mHzFrameRect.x = panePos.x + ((experiment.TD_N_VIS_ROWS - trial.frame()) / 2) * cellSize;
                mHzFrameRect.y = panePos.y - mHzFrameRect.height;
            }
        }

        paintTrial = true;
        revalidate();
        repaint();
    }

    private boolean checkSuccess() {
        boolean result = false;
        switch (trial.scrollMode()) {
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
        int offset = (vtScrollPane.getNVisibleLines() - trial.frame()) / 2;
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
        int offset = (experiment.TD_N_VIS_ROWS - trial.frame()) / 2;
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

        switch (trial.scrollMode()) {
        case VERTICAL -> {
            isScrolled = vtScrollPane.scroll(vtScrollAmt);

//            if (isScrolled) repaint();
        }
        case TWO_DIM -> {
            tdScrollPane.scroll(vtScrollAmt, hzScrollAmt);
//            repaint();
//            if (isScrolled) repaint();
        }
        }

    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        String TAG = NAME + "paintComponent";

        Logs.d(TAG, "Painting", 0);

        Graphics2D g2d = (Graphics2D) g;
        Logs.d(TAG, "Painting trial", 0);

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
}
