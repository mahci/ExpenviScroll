package gui;

import experiment.Experiment;
import experiment.Trial;
import tools.Logs;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ExperimentPanel extends JLayeredPane {

    private final static String NAME = "ExperimentPanel/";
    // -------------------------------------------------------------------------------------------

    // Constants
    private double LR_MARGIN_mm = 20; // (mm) Left-right margin
    private double TB_MARGIN_mm = 20; // (mm) Left-right margin

    // Experiment and trial
    private Experiment experiment;
    private Trial trial;

    // Elements
    private VerticalScrollPane vtScrollPane;
    private HorizontalScrollPane hzScrollPane;
    private JLabel label;

    // Experiment
    private Point panePosition; // Position of the scrolling pane
    private int blockNum = 1;
    private int trialNum = 0; // Round = 2 blocks
    private boolean isPaneSet;
    private int targetColNum, randColNum;
    private int targetLineNum, randLineNum;
    private int vtLineH;
    private boolean isScrollingEnabled = false;

    // -------------------------------------------------------------------------------------------
    private final Action nextTrial = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int nTrials = experiment.getRound(blockNum).getNTrials();
            if (trialNum < nTrials) {
                trialNum++;
                trial = experiment.getRound(blockNum).getTrial(trialNum);
//                label.setBounds(850, label.getY() + 100, 1000, 400);
//

            } else {
//                removeAll();
//                label = new JLabel("Thank you for your participation!");
//                add(label, 0);
            }

//            repaint();
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
        getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true),
                "SPACE");
        getActionMap().put("SPACE", nextTrial);

        // Add the start label
//        label = new JLabel("Press SPACE to start the experiment", JLabel.CENTER);
//        label.setFont(new Font("Sans", Font.BOLD, 35));
//        label.setBounds(850, 500, 1000, 400);
//        add(label, 0);

        TDScrollPane tdScrollPane = new TDScrollPane(experiment.TD_PANE_DIM_mm)
                    .setScrollBars(
                            experiment.TD_SCROLL_BAR_W_mm,
                            experiment.TD_SCROLL_THUMB_L_mm)
                    .setTable(
                            experiment.TD_N_ROWS,
                            experiment.TD_N_COLS,
                            experiment.TD_N_VISIBLE_ROWS,
                            experiment.TD_N_VISIBLE_COLS);

        Dimension d = tdScrollPane  .getPreferredSize();
        tdScrollPane.setBounds(500, 300, d.width, d.height);
        add(tdScrollPane, 1);


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
                    hzScrollPane.getHorizontalScrollBar().setValue(getTargetRange(targetColNum).x);
                    Logs.infoAll(TAG, targetColNum, trial.frameSize);
                    Logs.infoAll(TAG, hzScrollPane.getHorizontalScrollBar().getValue());
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                    int currentValue = hzScrollPane.getHorizontalScrollBar().getValue();
//                    hzScrollPane.getHorizontalScrollBar().setValue(currentValue + hzScrollPane.getColWidth());
                }

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                    int currentValue = hzScrollPane.getHorizontalScrollBar().getValue();
//                    hzScrollPane.getHorizontalScrollBar().setValue(getTargetRange(targetColNum).x);
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
            // Do nothing!
        });

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

        int minY = tbMargin;
        int maxY = getHeight() - (tbMargin + paneDim.height);

        if (minX >= maxX || minY >= maxY) return new Point();
        else return new Point(Utils.randInt(minX, maxX), Utils.randInt(minY, maxY));
    }

    /**
     * Get the min,max scroll value for the target
     * @param targetIndex Target column/line number (from 1) (!) Should ALWAYS be > offset!
     * @return Two values (sroll pixels) indicating range (x = min, y = max)
     */
    private Point getTargetRange(int targetIndex) {
        String TAG = NAME + "getTargetRange";

        Point result = new Point();

        switch (trial.scrollMode) {
        case VERTICAL -> {
            int nVisibleLines = experiment.VT_N_VISIBLE_LINES;
            vtLineH = vtScrollPane.getHeight() / nVisibleLines;

            // Number of lines to each side of the (centered) frame
            int offset = (nVisibleLines - trial.frameSize) / 2;

            if (targetIndex > nVisibleLines) {
                result.x = ((targetIndex - nVisibleLines) + offset + 1) * vtLineH;
                result.y = result.x + (trial.frameSize * vtLineH);
            } else if (offset < targetIndex && targetIndex <= (trial.frameSize + offset)) {
                result.x = 0;
                result.y = ((trial.frameSize + offset) - targetIndex) * vtLineH;
            }
        }
        case HORIZONTAL -> {
            int nVisibleCols = experiment.HZ_N_VISIBLE_COLS;
            int colW = hzScrollPane.getColWidth();

            // Number of cols/lines to each side of the (centered) frame
            int offset = (nVisibleCols - trial.frameSize) / 2;

            if (targetIndex > nVisibleCols) {
                result.x = ((targetIndex - nVisibleCols) + offset + 1) * colW;
                result.y = result.x + (trial.frameSize * colW);
            } else if (offset < targetIndex && targetIndex <= (trial.frameSize + offset)) {
                result.x = 0;
                result.y = ((trial.frameSize + offset) - targetIndex) * colW;
            }
        }
        }

        return result;
    }

    public void scroll(int delta) {

        if (trial != null) {
            switch (trial.scrollMode) {
            case VERTICAL -> {
                int currentValue = vtScrollPane.getVerticalScrollBar().getValue();
                vtScrollPane.getVerticalScrollBar().setValue(currentValue + delta);
            }
            case HORIZONTAL -> {
                int currentValue = hzScrollPane.getHorizontalScrollBar().getValue();
                hzScrollPane.getHorizontalScrollBar().setValue(currentValue + delta);
            }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        String TAG = NAME + "paintComponent";
        super.paintComponent(g);

        if (trial != null) {

        }
    }
}
