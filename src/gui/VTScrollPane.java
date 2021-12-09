package gui;

import tools.Consts;
import tools.DimensionD;
import tools.Logs;
import tools.Utils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.MouseWheelListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static tools.Consts.COLORS;

public class VTScrollPane extends JScrollPane {
    private final static String NAME = "VTScrollPane/";
    //-------------------------------------------------------------------------------------------------

    private final String WRAPPED_FILE_NAME = "./res/wrapped.txt";

    private final Dimension dim; // in px
    private ArrayList<Integer> lineCharCounts = new ArrayList<>();

    private JTextPane linesTextPane;
    private JTextPane bodyTextPane;
    private MyScrollBarUI scrollBarUI;

    protected int targetMinScVal, targetMaxScVal;
    private int nLines;
    //-------------------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param ddMM Dimention of scroll pane (W/H in mm)
     */
    public VTScrollPane(DimensionD ddMM) {
        dim = new Dimension(Utils.mm2px(ddMM.getWidth()), Utils.mm2px(ddMM.getHeight()));
        setPreferredSize(dim);
    }

    public VTScrollPane(Dimension d) {
        dim = d;
        setPreferredSize(dim);
    }

    /**
     * Set the text file for displayed text
     * @param fileName Name of the file
     * @return Instance
     */
    public VTScrollPane setText(String fileName, int wrapCharCount, float bodyFontSize) {
        String TAG = NAME + "setText";

        // Wrap the file and get the char num of each line
        try {
            lineCharCounts = Utils.wrapFile(fileName, WRAPPED_FILE_NAME, wrapCharCount);
            nLines = lineCharCounts.size();

            // Body of text
            bodyTextPane = new CustomTextPane(false);
            bodyTextPane.read(new FileReader(WRAPPED_FILE_NAME), "wrapped");
            bodyTextPane.setEditable(false);
            bodyTextPane.setFont(Consts.FONTS.TABLE_FONT.deriveFont(bodyFontSize));
            bodyTextPane.setSelectionColor(Color.WHITE);

            SimpleAttributeSet lineSpace = new SimpleAttributeSet();
            StyleConstants.setLineSpacing(lineSpace,0.2f);
            StyleConstants.setFontSize(lineSpace, 20);
            StyleConstants.setFontFamily(lineSpace, "Dialog");
            int len = bodyTextPane.getStyledDocument().getLength();
            bodyTextPane.getStyledDocument().setParagraphAttributes(0, len, lineSpace, true);

            getViewport().add(bodyTextPane);

        } catch (IOException e) {
            Logs.error(TAG, "Problem createing VTScrollPane -> setText");
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set the line numbers (H is the same as the scroll pane)
     * @param lineNumsPaneW Width of the line num pane (mm)
     * @param lineNumsFontSize Font size of the line num pane
     * @return Current instance
     */
    public VTScrollPane setLineNums(double lineNumsPaneW, float lineNumsFontSize) {

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
     * @param thumbH Scroll thumb height (mm)
     * @return Current instance
     */
    public VTScrollPane setScrollBar(double scrollBarW, double thumbH) {
        String TAG = NAME + "setScrollBar";
        // Set dimentions
        Dimension scBarDim = new Dimension(Utils.mm2px(scrollBarW), dim.height);
//        Dimension scThumbDim = new Dimension(scBarDim.width, Utils.mm2px(thumbH));

        // Verticall scroll bar
        scrollBarUI = new MyScrollBarUI(
                Color.BLACK,
                COLORS.SCROLLBAR_TRACK,
                Color.BLACK,
                6);
        getVerticalScrollBar().setUI(scrollBarUI);
        getVerticalScrollBar().setPreferredSize(scBarDim);

        // Scroll thumb
//        UIManager.put("ScrollBar.thumbSize", scThumbDim);

        // Policies
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return this;
    }

    public void highlight(int lineInd, int frameSizeLines) {
        String TAG = NAME + "highlight";
        // Highlight line
        try {
            int stIndex = 0;
            for (int li = 0; li < lineInd - 1; li++) {
                stIndex += lineCharCounts.get(li) + 1; // prev. lines + \n
            }
            int endIndex = stIndex + lineCharCounts.get(lineInd - 1);

            DefaultHighlightPainter highlighter = new DefaultHighlightPainter(COLORS.CELL_HIGHLIGHT);
            bodyTextPane.getHighlighter().removeAllHighlights();
            bodyTextPane.getHighlighter().addHighlight(stIndex, endIndex, highlighter);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Indicator
        int nVisibleLines = getNVisibleLines();
        int frOffset = (nVisibleLines - frameSizeLines) / 2;
        int lineH = getLineHeight();
        int vtMinThreshold = (lineInd - (frameSizeLines - 1) - frOffset) * lineH;
        int vtMaxThreshold = (lineInd - frOffset) * lineH;
        scrollBarUI.setHighlight(
                COLORS.SCROLLBAR_HIGHLIGHT,
                vtMinThreshold,
                vtMaxThreshold);
        getVerticalScrollBar().setUI(scrollBarUI);
        Logs.d(TAG, "Indicator", nVisibleLines, frameSizeLines, frOffset, lineH,
                vtMinThreshold, vtMaxThreshold);
    }

    public boolean scroll(int scrollAmt) {
        Dimension vpDim = getViewport().getView().getSize(); // Can be also Preferred
        int extent = getVerticalScrollBar().getModel().getExtent();

        Point vpPos = getViewport().getViewPosition();
        int newY = vpPos.y + scrollAmt;
        if (newY != vpPos.y && newY >= 0 && newY <= (vpDim.height - extent)) {
            getViewport().setViewPosition(new Point(vpPos.x, newY));
            return true;
        }

        return false;
    }

    /**
     * Add MouseWheelListener to every component
     * @param mwl MouseWheelListener
     */
    public void addWheelListener(MouseWheelListener mwl) {
        getVerticalScrollBar().addMouseWheelListener(mwl);
        bodyTextPane.addMouseWheelListener(mwl);
        linesTextPane.addMouseWheelListener(mwl);
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

    public int getNLines() {
        return nLines;
    }

    public int getLineHeight() {
        String TAG = NAME + "getLineHeight";
//        Logs.d(TAG, "", getPreferredSize().height, getNVisibleLines());
//        return getPreferredSize().height / getNVisibleLines();
        int bodyPaneH = getViewport().getView().getPreferredSize().height;
        Logs.d(TAG, "", bodyPaneH, nLines);
        return bodyPaneH / nLines;
    }

    public int getNVisibleLines() {
        String TAG = NAME + "getNVisibleLines";
        return dim.height / getLineHeight();
    }

    public int getMaxScrollVal() {
        return getVerticalScrollBar().getMaximum();
    }



    //-------------------------------------------------------------------------------------------------

    private class CustomScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(244, 244, 244));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g.setColor(Color.BLACK);
            g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

            // Highlight scroll bar rect
            double ratio = trackBounds.height / (getVerticalScrollBar().getMaximum() * 1.0);
            int hlY = (int) (targetMinScVal * ratio);
            int hlH = (int) ((targetMaxScVal - targetMinScVal) * ratio) + getThumbBounds().height;
            g.setColor(Consts.COLORS.SCROLLBAR_HIGHLIGHT);
            g.fillRect(trackBounds.x, hlY, trackBounds.width, hlH);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
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
