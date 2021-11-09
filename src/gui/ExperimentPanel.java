package gui;

import control.Controller;
import control.Experimenter;
import experiment.Experiment;
import experiment.Trial;
import tools.DoubleDimension;
import tools.Logs;
import tools.Utils;

import javax.print.attribute.standard.MediaSize;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import static control.Experimenter.*;
import static control.Experimenter.Direction.U_R;

public class ExperimentPanel extends JLayeredPane {

    private final static String NAME = "ExperimentPanel--";
    // -------------------------------------------------------------------------------------------

    // Experiment and trial
    private Experiment experiment;
    private Trial trial;

    // Elements
    private VerticalScrollPane vScrollPane;
    private HorizontalScrollPane hScrollPane;
    private JScrollPane testSP;

    // Experiment
    private int x = 200;
    private int y = 400;
    private int blockNum, trialNum; // Round = 2 blocks

    // -------------------------------------------------------------------------------------------
    private final Action nextTrial = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            x = Utils.randInt(10, getParent().getWidth() - 410);
            trialNum++;
            showTrial();
        }
    };

    private final Action enableScrolling = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hScrollPane.setWheelScrollingEnabled(enabled);
            Logs.infoAll(NAME, "Scrolling enabled");
        }
    };

    // -------------------------------------------------------------------------------------------

    /**
     * Create the panel
     * @param exp Experiment to show
     */
    public ExperimentPanel(Experiment exp) {
        setLayout(null);

        // Set the experiment
        experiment = exp;

        // Map the keys
        getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true),
                "SPACE");
//        getActionMap().put("SPACE", nextTrial);

        // Create the hz scroll pane
        hScrollPane = new HorizontalScrollPane(experiment.DIM_HZ_PANE_mm)
                .setScrollBar(experiment.HZ_SCROLL_BAR_H_mm, experiment.HZ_SCROLL_THUMB_W_mm)
                .setTable(experiment.HZ_N_ROWS, experiment.HZ_N_COLS, experiment.HZ_N_VISIBLE_COLS)
                .create();
        add(hScrollPane, 0);

        // Start with the trials
//        blockNum = 1;
//        trialNum = 1;
//        showTrial();

//        addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {
//            }
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
//                    hScrollPane.setWheelScrollingEnabled(true);
//                }
//
//                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                    Controller.get().scroll(5);
//                }
//
//                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                    Controller.get().scroll(-5);
//                }
//            }
//
//            @Override
//            public void keyReleased(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
//                    hScrollPane.setWheelScrollingEnabled(false);
//                }
//            }
//        });


    }

    /**
     * Show a Trial
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";

        trial = experiment.getRound(blockNum).getTrial(trialNum);
        if (trial.distance == 0) {
            Logs.infoAll(TAG, "Empty trial!");
        } else {
            switch (trial.scrollMode) {
            case VERTICAL -> {

            }
            case HORIZONTAL -> {
                Logs.infoAll(TAG, "Horizontal pane");
                // Create the hz scroll pane
                hScrollPane = new HorizontalScrollPane(experiment.DIM_HZ_PANE_mm)
                        .setScrollBar(experiment.HZ_SCROLL_BAR_H_mm, experiment.HZ_SCROLL_THUMB_W_mm)
                        .setTable(experiment.HZ_N_ROWS, experiment.HZ_N_COLS, experiment.HZ_N_VISIBLE_COLS)
                        .create();
                add(hScrollPane, 0);

                // Choose a random column and set the highlight column accordingly (based on dir/distance)
                int randColNum, hlColNum;
                if (trial.direction.equals(U_R)) { // Scrolling right
                    randColNum = Utils.randInt(1, experiment.HZ_N_COLS - trial.distance);
                    hlColNum = randColNum + trial.distance;
                } else { // Scrolling left
                    randColNum = Utils.randInt(trial.distance + 1, experiment.HZ_N_COLS);
                    hlColNum = randColNum - trial.distance;
                }

                // Scroll to the random column
                int randScrollPosition = randColNum * hScrollPane.getColWidth();
                hScrollPane.getHorizontalScrollBar().setValue(randScrollPosition);

                // Highlight
                hScrollPane.higlight(hlColNum);

            }
            }
        }

//        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

//        if (vScrollPane != null) {
//            Dimension d = vScrollPane.getPreferredSize();
//            vScrollPane.setBounds(x, y, d.width, d.height);
//        }

//        if (hScrollPane != null) {
//            Dimension d = hScrollPane.getPreferredSize();
//            hScrollPane.setBounds(x, y, d.width, d.height);
//        }

//        if (testSP != null) {
//            Dimension d = testSP.getPreferredSize();
//            testSP.setBounds(x, y, d.width, d.height);
//        }

        // Draw the target indicator
//        g.setColor(COLORS.LINE_HIGHLIGHT);
//        g.fillRect(x + 100, y + 513,
//                100, 20);
//        g.fillRect(x - Utils.mm2px(TARGET_INDIC_W_mm), y,
//                Utils.mm2px(TARGET_INDIC_W_mm), 20);
    }


    public void start() throws IOException, BadLocationException {
        final String TAG = NAME + "start";

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


}
