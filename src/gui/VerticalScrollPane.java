package gui;

import tools.Consts;
import tools.DoubleDimension;
import tools.Logs;
import tools.Utils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class VerticalScrollPane extends JScrollPane {
    private final static String cName = "VerticalScrollPane";
    //-------------------------------------------------------------------------------------------------

    private final String WRAPPED_FILE_NAME = "./res/wrapped.txt";

    private final Dimension dim; // in px
    private ArrayList<Integer> lineCharCounts = new ArrayList<>();

    private JTextPane linesTextPane;
    private JTextPane bodyTextPane;
    //-------------------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param ddMM Dimention of scroll pane (W/H in mm)
     */
    public VerticalScrollPane(DoubleDimension ddMM) {
        dim = new Dimension(Utils.mm2px(ddMM.getWidth()), Utils.mm2px(ddMM.getHeight()));
        setPreferredSize(dim);
    }

    /**
     * Set the text file for displayed text
     * @param fileName Name of the file
     * @return Instance
     */
    public VerticalScrollPane setText(String fileName, int wrapCharCount, float bodyFontSize) throws IOException {

        // Wrap the file and get the char num of each line
        lineCharCounts = Utils.wrapFile(fileName, WRAPPED_FILE_NAME, wrapCharCount);

        // Body of text
        bodyTextPane = new CustomTextPane(false);
        bodyTextPane.setEditable(false);
        bodyTextPane.setFont(Consts.FONTS.SF_LIGHT.deriveFont(bodyFontSize));
        bodyTextPane.setSelectionColor(Color.WHITE);

        bodyTextPane.read(new FileReader(WRAPPED_FILE_NAME), "wrapped");


        return this;
    }

    /**
     * Set the line numbers (H is the same as the scroll pane)
     * @param lineNumsPaneW Width of the line num pane (mm)
     * @param lineNumsFontSize Font size of the line num pane
     * @return Current instance
     */
    public VerticalScrollPane setLineNums(double lineNumsPaneW, float lineNumsFontSize) {

        // Set dimention
        Dimension lnpDim = new Dimension(Utils.mm2px(lineNumsPaneW), dim.height);

        // Line numbers
        linesTextPane = new JTextPane();
        linesTextPane.setPreferredSize(lnpDim);
        linesTextPane.setBackground(Consts.COLORS.LINE_NUM_BG);
        linesTextPane.setEditable(false);
        Font linesFont = Consts.FONTS.SF_LIGHT
                .deriveFont(lineNumsFontSize)
                .deriveFont(Consts.FONTS.ATTRIB_ITALIC);
        linesTextPane.setFont(linesFont);
        linesTextPane.setForeground(Color.GRAY);
        StyledDocument documentStyle = linesTextPane.getStyledDocument();
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_CENTER);
        documentStyle.setParagraphAttributes(0, documentStyle.getLength(), attributeSet, false);

        linesTextPane.setText(getLineNumbers(lineCharCounts.size()));

        return this;
    }

    /**
     * Set the scroll bar
     * @param scrollBarW Scroll bar width (mm)
     * @param thumbW Scroll thumb width (px) // TODO: change to mm
     * @param thumbH Scroll thumb height (mm)
     * @return Current instance
     */
    public VerticalScrollPane setScrollBar(double scrollBarW, int thumbW, double thumbH) {

        // Set dimentions
        Dimension scBarDim = new Dimension(Utils.mm2px(scrollBarW), dim.height);
        Dimension scThumbDim = new Dimension(thumbW, Utils.mm2px(thumbH));

        // Verticall scroll bar
        getVerticalScrollBar().setUI(new CustomVScrollBarUI());
        getVerticalScrollBar().setPreferredSize(scBarDim);

        // Scroll thumb
        UIManager.put("ScrollBar.minimumThumbSize", scThumbDim);
        UIManager.put("ScrollBar.maximumThumbSize", scThumbDim);

        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return this;
    }

    /**
     * Final creation of the component
     * @return Current instance
     */
    public VerticalScrollPane create() {
        getViewport().add(bodyTextPane);
        setRowHeaderView(linesTextPane);

        return this;
    }

    /**
     * Get the line numbers to show
     * @param nLines Number of lines
     * @return String of line numbers
     */
    public String getLineNumbers(int nLines) {
        Logs.info(this.getClass().getName(), "Total lines = " + nLines);
        StringBuilder text = new StringBuilder("1" + System.getProperty("line.separator"));
        for(int i = 2; i < nLines + 2; i++){
            text.append(i).append(System.getProperty("line.separator"));
        }
        return text.toString();
    }
    //-------------------------------------------------------------------------------------------------

    private static class CustomVScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(244, 244, 244));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g.setColor(Color.BLACK);
            g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

            g.setColor(Consts.COLORS.SCROLLBAR_HIGHLIGHT);
            g.fillRect(trackBounds.x, trackBounds.y + 80, trackBounds.width, getThumbBounds().height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
//        thumbBounds.width -= 2;
//        thumbBounds.height = Utils.mm2px(THUMB_H_mm);
            // Set anti-alias
            Graphics2D graphics2D = (Graphics2D) g;
            graphics2D.setColor(Color.BLACK);
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);


            graphics2D.fillRoundRect(
                    thumbBounds.x + 4, thumbBounds.y,
                    thumbBounds.width - 6, thumbBounds.height,
                    5, 5);
//        Logs.info(getClass().getName(), thumbBounds.toString());
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        protected JButton createZeroButton() {
            JButton button = new JButton("zero button");
            Dimension zeroDim = new Dimension(0,0);
            button.setPreferredSize(zeroDim);
            button.setMinimumSize(zeroDim);
            button.setMaximumSize(zeroDim);
            return button;
        }
    }


}
