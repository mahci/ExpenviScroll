package gui;

import experiment.Experiment;
import experiment.Trial;
import tools.Consts;
import tools.Logs;
import tools.Utils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import static control.Experimenter.Direction.*;
import static tools.Consts.COLORS;
import static tools.Consts.FONTS;

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

//                TDScrollPane tdScrollPane = new TDScrollPane(experiment.DIM_VT_PANE_mm)
//                    .setScrollBars(experiment.VT_SCROLL_BAR_W_mm, experiment.VT_SCROLL_THUMB_H_mm)
//                    .setTable(300, experiment.HZ_N_COLS, experiment.HZ_N_ROWS, experiment.HZ_N_VISIBLE_COLS)
//                    .create();
//
//
//                add(tdScrollPane, 1);
                removeAll();
                vtScrollPane = new VerticalScrollPane(experiment.DIM_VT_PANE_mm)
                            .setText("./res/lorem.txt", experiment.VT_WRAP_CHARS_COUNT, FONTS.TEXT_BODY_FONT_SIZE)
                            .setLineNums(experiment.VT_LINENUMS_W_mm, FONTS.LINE_NUM_FONT_SIZE)
                            .setScrollBar(experiment.VT_SCROLL_BAR_W_mm, experiment.HZ_SCROLL_THUMB_W_mm)
                            .create();

                add(vtScrollPane, 0);
            } else {
//                removeAll();
//                label = new JLabel("Thank you for your participation!");
//                add(label, 0);
            }

            repaint();
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

        //    @Override
//    public void paint(Graphics g) {
//        super.paint(g);
//
////        if (vScrollPane != null) {
////            Dimension d = vScrollPane.getPreferredSize();
////            vScrollPane.setBounds(x, y, d.width, d.height);
////        }
//
////        if (hScrollPane != null) {
////            Dimension d = hScrollPane.getPreferredSize();
////            hScrollPane.setBounds(x, y, d.width, d.height);
////        }
//
////        if (testSP != null) {
////            Dimension d = testSP.getPreferredSize();
////            testSP.setBounds(x, y, d.width, d.height);
////        }
//
//        // Draw the target indicator
////        g.setColor(COLORS.LINE_HIGHLIGHT);
////        g.fillRect(x + 100, y + 513,
////                100, 20);
////        g.fillRect(x - Utils.mm2px(TARGET_INDIC_W_mm), y,
////                Utils.mm2px(TARGET_INDIC_W_mm), 20);
//    }


//        vScrollPane = new VerticalScrollPane(DIM_SCROLL_PANE)
//                .setText("./res/lorem.txt", WRAP_CHARS_COUNT, BODY_FONT_SIZE)
//                .setLineNums(LINE_NUMS_PANE_W_mm, LINE_NUM_FONT_SIZE)
//                .setScrollBar(SCROLL_BAR_W_mm, SCROLL_THUMB_W, SCROLL_THUMB_H_mm)
//                .create();
//        add(vScrollPane, 0);


//        requestFocusInWindow();

//        hzScrollPane = new HorizontalScrollPane(DIM_HZ_SCROLL_PANE)
//                .setTable()
//                .create();
//        add(hzScrollPane, 0);

        // Wrap the file
//        ArrayList<Integer> lineCharCounts = Utils.wrapFile(
//                "./res/lorem.txt",
//                "./res/lorem_wrapped.txt",
//                WRAP_CHARS_COUNT);
//
////        bodyTextArea = new JTextArea();
////        bodyTextArea.setEditable(false);
////        bodyTextArea.setFont(FONTS.SF_REGULAR.deriveFont(BODY_FONT_SIZE));
////
////        bodyTextArea.read(new FileReader("./res/lorem_wrapped.txt"), "Lorem");
//
//        // Body of text
//        bodyTextPane = new CustomTextPane(false);
//        bodyTextPane.setEditable(false);
//        bodyTextPane.setFont(FONTS.SF_LIGHT.deriveFont(BODY_FONT_SIZE));
//        bodyTextPane.setSelectionColor(Color.WHITE);
//
//        bodyTextPane.read(new FileReader("./res/lorem_wrapped.txt"), "Lorem");
//
//        // Line numbers
//        linesTextPane = new JTextPane();
//        linesTextPane.setPreferredSize(LINE_NUMS_PANE_DIM);
//        linesTextPane.setBackground(COLORS.LINE_NUM_BG);
//        linesTextPane.setEditable(false);
//        Font linesFont = FONTS.SF_LIGHT
//                .deriveFont(LINE_NUM_FONT_SIZE)
//                .deriveFont(FONTS.ATTRIB_ITALIC);
//        linesTextPane.setFont(linesFont);
//        linesTextPane.setForeground(Color.GRAY);
//        StyledDocument documentStyle = linesTextPane.getStyledDocument();
//        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
//        StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_CENTER);
//        documentStyle.setParagraphAttributes(0, documentStyle.getLength(), attributeSet, false);
//
//        linesTextPane.setText(getLineNumbers(lineCharCounts.size()));
//
//        int lineNum = 2;
//        int stIndex = 0;
//        for (int li = 0; li < lineNum - 1; li++) {
//            stIndex += lineCharCounts.get(li) + 1; // prev. lines + \n
//        }
//        int endIndex = stIndex + lineCharCounts.get(lineNum - 1);
//        bodyTextPane.getHighlighter().removeAllHighlights();
//        bodyTextPane.getHighlighter().addHighlight(stIndex, endIndex, highlighter);
//        Logs.info(mName, stIndex + " to " + endIndex);

        // Scroll pane
//        scrollPane = new JScrollPane();
//        scrollPane.setPreferredSize(SCROLL_PANE_DIM);
//
//        UIManager.put("ScrollBar.minimumThumbSize", SCROLL_PANE_THUMB_DIM);
//        UIManager.put("ScrollBar.maximumThumbSize", SCROLL_PANE_THUMB_DIM);
//
//        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
//        scrollPane.getVerticalScrollBar().setPreferredSize(SCROLL_BAR_DIM);
//
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//
//        scrollPane.getViewport().add(bodyTextPane);
//        scrollPane.setRowHeaderView(linesTextPane);

//        add(scrollPane, 0);

}
