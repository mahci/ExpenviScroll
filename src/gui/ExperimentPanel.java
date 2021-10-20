package gui;

import experiment.Experiment;
import tools.Logs;
import tools.Utils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;

import tools.Consts.*;

public class ExperimentPanel extends JLayeredPane {

    private final static String cName = "ExperimentPanel";
    /*-------------------------------------------------------------------------------------*/

    //--- Sizes
    private final double SCROLL_PANE_W_mm = 130.0;
    private final double SCROLL_PANE_H_mm = 140.0;
    private final Dimension SCROLL_PANE_DIM = new Dimension(
            Utils.mm2px(SCROLL_PANE_W_mm),
            Utils.mm2px(SCROLL_PANE_H_mm));

    private final double LINE_NUMS_PANE_W_mm = 10;
    private final Dimension LINE_NUMS_PANE_DIM = new Dimension(
            Utils.mm2px(LINE_NUMS_PANE_W_mm),
            Utils.mm2px(SCROLL_PANE_H_mm));

    private final double SCROLL_BAR_W_mm = 5;
    private final Dimension SCROLL_BAR_DIM = new Dimension(
            Utils.mm2px(SCROLL_BAR_W_mm),
            Utils.mm2px(SCROLL_PANE_H_mm));

    private final int SCROLL_THUMB_W = 5;
    private final double SCROLL_THUMB_H_mm = 6;
    private final Dimension SCROLL_PANE_THUMB_DIM = new Dimension(
            SCROLL_THUMB_W,
            Utils.mm2px(SCROLL_THUMB_H_mm));

    private final double TARGET_INDIC_W_mm = 7;

    //-- Defaults
    private final int WRAP_CHARS_COUNT = 82;
    private final Highlighter.HighlightPainter highlighter =
            new DefaultHighlighter.DefaultHighlightPainter(COLORS.LINE_HIGHLIGHT);
    private final float BODY_FONT_SIZE = 12.2f;
    private final float LINE_NUM_FONT_SIZE = 12.2f;

    /*-------------------------------------------------------------------------------------*/

    // the experiment to show
    Experiment experiment;

    private JTextPane linesTextPane;
    private JTextArea bodyTextArea;
    private JTextPane bodyTextPane;
    private JScrollPane scrollPane;

    private int x = 200;
    private int y = 400;

    private final Action move = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            x = Utils.randInt(10, getParent().getWidth() - 410);

            repaint();
        }
    };

    /**
     * Create the panel
     * @param exp - Experiment to show
     */
    public ExperimentPanel() {
        setLayout(null);

        // set the experiment
//        experiment = exp;

        // map the keys
        getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true),
                "SPACE");
//        getActionMap().put("SPACE", move);

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (scrollPane != null) {
            Dimension d = scrollPane.getPreferredSize();
            scrollPane.setBounds(x, y, d.width, d.height);
        }

        // Draw the target indicator
        g.setColor(COLORS.LINE_HIGHLIGHT);
        g.fillRect(x - Utils.mm2px(TARGET_INDIC_W_mm), y,
                Utils.mm2px(TARGET_INDIC_W_mm), 100);
    }


    public void start() throws IOException, BadLocationException {
        final String mName = "ExperimentalPanel--start";

        // Wrap the file
        ArrayList<Integer> lineCharCounts = Utils.wrapFile(
                "./res/lorem.txt",
                "./res/lorem_wrapped.txt",
                WRAP_CHARS_COUNT);

//        bodyTextArea = new JTextArea();
//        bodyTextArea.setEditable(false);
//        bodyTextArea.setFont(FONTS.SF_REGULAR.deriveFont(BODY_FONT_SIZE));
//
//        bodyTextArea.read(new FileReader("./res/lorem_wrapped.txt"), "Lorem");

        // Body of text
        bodyTextPane = new CustomTextPane(false);
        bodyTextPane.setEditable(false);
        bodyTextPane.setFont(FONTS.SF_LIGHT.deriveFont(BODY_FONT_SIZE));
        bodyTextPane.setSelectionColor(Color.WHITE);

        bodyTextPane.read(new FileReader("./res/lorem_wrapped.txt"), "Lorem");

        // Line numbers
        linesTextPane = new JTextPane();
        linesTextPane.setPreferredSize(LINE_NUMS_PANE_DIM);
        linesTextPane.setBackground(COLORS.LINE_NUM_BG);
        linesTextPane.setEditable(false);
        Font linesFont = FONTS.SF_LIGHT
                .deriveFont(LINE_NUM_FONT_SIZE)
                .deriveFont(FONTS.ATTRIB_ITALIC);
        linesTextPane.setFont(linesFont);
        linesTextPane.setForeground(Color.GRAY);
        StyledDocument documentStyle = linesTextPane.getStyledDocument();
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_CENTER);
        documentStyle.setParagraphAttributes(0, documentStyle.getLength(), attributeSet, false);

        linesTextPane.setText(getLineNumbers(lineCharCounts.size()));

        int lineNum = 2;
        int stIndex = 0;
        for (int li = 0; li < lineNum - 1; li++) {
            stIndex += lineCharCounts.get(li) + 1; // prev. lines + \n
        }
        int endIndex = stIndex + lineCharCounts.get(lineNum - 1);
        bodyTextPane.getHighlighter().removeAllHighlights();
        bodyTextPane.getHighlighter().addHighlight(stIndex, endIndex, highlighter);
        Logs.info(mName, stIndex + " to " + endIndex);

        // Scroll pane
        scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(SCROLL_PANE_DIM);

        UIManager.put("ScrollBar.minimumThumbSize", SCROLL_PANE_THUMB_DIM);
        UIManager.put("ScrollBar.maximumThumbSize", SCROLL_PANE_THUMB_DIM);

        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(SCROLL_BAR_DIM);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getViewport().add(bodyTextPane);
        scrollPane.setRowHeaderView(linesTextPane);

        add(scrollPane, 0);

    }

    public String getLineNumbers(int nLines) {
        Logs.info(this.getClass().getName(), "Total lines = " + nLines);
        String text = "1" + System.getProperty("line.separator");
        for(int i = 2; i < nLines + 2; i++){
            text += i + System.getProperty("line.separator");
        }
        return text;
    }
}
