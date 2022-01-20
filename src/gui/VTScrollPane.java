package gui;

import experiment.Experiment;
import tools.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static tools.Consts.*;

public class VTScrollPane extends JScrollPane implements MouseListener {
    private final static String NAME = "VTScrollPane/";
    //-------------------------------------------------------------------------------------------------
    public static final int WRAP_CHARS_COUNT = 70;
//    private final int WRAP_CHARS_COUNT = 67;
    private final String WRAPPED_FILE_NAME = "./res/wrapped.txt";

    private final Dimension mDim; // in px
    private ArrayList<Integer> mLineCharCounts = new ArrayList<>();

    private JTextPane mLinesTextPane;
    private JTextPane mBodyTextPane;
    private MyScrollBarUI mScrollBarUI;

    private MinMax mTargetMinMax = new MinMax();
    private int mNumLines;

    private boolean mIsCursorIn;
    //-------------------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param ddMM Dimention of scroll pane (W/H in mm)
     */
    public VTScrollPane(DimensionD ddMM) {
        mDim = new Dimension(Utils.mm2px(ddMM.getWidth()), Utils.mm2px(ddMM.getHeight()));
        setPreferredSize(mDim);
    }

    public VTScrollPane(Dimension d) {
        mDim = d;
        setPreferredSize(mDim);
    }

    /**
     * Set the text file for displayed text
     * @param fileName Name of the file
     * @return Instance
     */
    public VTScrollPane setText(String fileName) {
        String TAG = NAME + "setText";

        // Wrap the file and get the char num of each line
        try {
            mLineCharCounts = Utils.wrapFile(fileName, WRAPPED_FILE_NAME, WRAP_CHARS_COUNT);
            mNumLines = mLineCharCounts.size();

            // Body of text
            mBodyTextPane = new CustomTextPane(false);
            mBodyTextPane.read(new FileReader(WRAPPED_FILE_NAME), "wrapped");
            mBodyTextPane.setEditable(false);
            final Font bodyFont = Consts.FONTS.SF_LIGHT.deriveFont(FONTS.TEXT_FONT_SIZE);
            mBodyTextPane.setFont(bodyFont);
            mBodyTextPane.setSelectionColor(Color.WHITE);

            SimpleAttributeSet bodyStyle = new SimpleAttributeSet();
            StyleConstants.setLineSpacing(bodyStyle,FONTS.TEXT_LINE_SPACING);
//            StyleConstants.setFontSize(bodyStyle, FONTS.TEXT_FONT_SIZE_INT);
//            StyleConstants.setFontFamily(bodyStyle, Font.SANS_SERIF);

            final int len = mBodyTextPane.getStyledDocument().getLength();
            mBodyTextPane.getStyledDocument().setParagraphAttributes(0, len, bodyStyle, false);

            getViewport().add(mBodyTextPane);

        } catch (IOException e) {
            Logs.error(TAG, "Problem createing VTScrollPane -> setText");
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set the line numbers (H is the same as the scroll pane)
     * @param lineNumsPaneW Width of the line num pane (mm)
     * @return Current instance
     */
    public VTScrollPane setLineNums(double lineNumsPaneW) {

        // Set dimention
        Dimension lnpDim = new Dimension(Utils.mm2px(lineNumsPaneW), mDim.height);

        // Set up Line numbers
        mLinesTextPane = new JTextPane();
        mLinesTextPane.setPreferredSize(lnpDim);
        mLinesTextPane.setBackground(COLORS.LINE_NUM_BG);
        mLinesTextPane.setEditable(false);
        final Font linesFont = Consts.FONTS.SF_LIGHT
                .deriveFont(FONTS.TEXT_FONT_SIZE)
                .deriveFont(Consts.FONTS.ATTRIB_ITALIC);
        mLinesTextPane.setFont(linesFont);
        mLinesTextPane.setForeground(Color.GRAY);
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLineSpacing(attributeSet,FONTS.TEXT_LINE_SPACING);
        final int len = mBodyTextPane.getStyledDocument().getLength();
        mLinesTextPane.
                getStyledDocument().
                setParagraphAttributes(0, len, attributeSet, false);

        mLinesTextPane.setText(getLineNumbers(mLineCharCounts.size()));

        // Show the line nums
        setRowHeaderView(mLinesTextPane);

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
        Dimension scBarDim = new Dimension(Utils.mm2px(scrollBarW), mDim.height);
//        Dimension scThumbDim = new Dimension(scBarDim.width, Utils.mm2px(thumbH));

        // Verticall scroll bar
        mScrollBarUI = new MyScrollBarUI(
                Color.BLACK,
                COLORS.SCROLLBAR_TRACK,
                Color.BLACK,
                6);
        getVerticalScrollBar().setUI(mScrollBarUI);
        getVerticalScrollBar().setPreferredSize(scBarDim);

        // Scroll thumb
//        UIManager.put("ScrollBar.thumbSize", scThumbDim);

        // Policies
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return this;
    }

    /**
     * Create the pane (last method)
     * @return The VTScrollPane instance
     */
    public VTScrollPane create() {
        getViewport().getView().addMouseListener(this);
        return this;
    }

    /**
     * Highlight a line indicated by lineInd
     * @param lineInd Index of the line (starting from 1)
     * @param frameSizeLines Size of the frame (in lines)
     */
    public void highlight(int lineInd, int frameSizeLines) {
        String TAG = NAME + "highlight";

        // Highlight line
        try {
            int stIndex = 0;
            for (int li = 0; li < lineInd; li++) {
                stIndex += mLineCharCounts.get(li) + 1; // prev. lines + \n
            }
            int endIndex = stIndex + mLineCharCounts.get(lineInd); // highlight the whole line
            Logs.d(TAG, mLineCharCounts.size(), lineInd, frameSizeLines, stIndex, endIndex);
            DefaultHighlightPainter highlighter = new DefaultHighlightPainter(COLORS.CELL_HIGHLIGHT);
            mBodyTextPane.getHighlighter().removeAllHighlights();
            mBodyTextPane.getHighlighter().addHighlight(stIndex, endIndex, highlighter);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Indicator
        int nVisibleLines = getNVisibleLines();
        int frOffset = (nVisibleLines - frameSizeLines) / 2;
        int lineH = getLineHeight();
        mTargetMinMax.setMin((lineInd - (frameSizeLines - 1) - frOffset) * lineH);
        mTargetMinMax.setMax((lineInd - frOffset) * lineH);
        mScrollBarUI.setHighlight(
                COLORS.SCROLLBAR_HIGHLIGHT,
                mTargetMinMax.getMin(),
                mTargetMinMax.getMax());
        getVerticalScrollBar().setUI(mScrollBarUI);
        Logs.d(TAG, "Indicator", nVisibleLines, frameSizeLines, frOffset, lineH,
                mTargetMinMax.getMin(), mTargetMinMax.getMax());
    }

    /**
     * Scroll a certain amount
     * @param scrollAmt Amount to scroll (in px)
     */
    public void scroll(int scrollAmt) {
        final String TAG = NAME + "scroll";
        // Scroll only if cursor is inside
        Logs.d(TAG, mIsCursorIn);
        if (mIsCursorIn) {
            Dimension vpDim = getViewport().getView().getSize(); // Can be also Preferred
            int extent = getVerticalScrollBar().getModel().getExtent();

            Point vpPos = getViewport().getViewPosition();
            int newY = vpPos.y + scrollAmt;
            if (newY != vpPos.y && newY >= 0 && newY <= (vpDim.height - extent)) {
                getViewport().setViewPosition(new Point(vpPos.x, newY));
            }

            repaint();
        }

    }

    /**
     * Put the specified line is in the center of the view
     * @param lineInd Line index (from 0)
     */
    public void centerLine(int lineInd) {
        final String TAG = NAME + "centerLine";

        // Check if the lineInd is in the range (to be able to be centered)
        final int halfViewLines = Experiment.TD_N_VIS_ROWS / 2;
        final int lastCenterLineInd = (mNumLines - Experiment.TD_N_VIS_ROWS) + halfViewLines;
        if (lineInd > halfViewLines && lineInd < lastCenterLineInd) {
            final int newPosY = (lineInd - halfViewLines) * getLineHeight(); // Centering the line

            Point vpPos = getViewport().getViewPosition();
            getViewport().setViewPosition(new Point(vpPos.x, newPosY));
        } else {
            Logs.d(TAG, "Can't center line", lineInd, halfViewLines, lastCenterLineInd);
        }

        repaint();
    }


    /**
     * Add MouseWheelListener to every component
     * @param mwl MouseWheelListener
     */
    public void addWheelListener(MouseWheelListener mwl) {
        getVerticalScrollBar().addMouseWheelListener(mwl);
        mBodyTextPane.addMouseWheelListener(mwl);
        mLinesTextPane.addMouseWheelListener(mwl);
    }

    /**
     * Get the line numbers to show
     * @param nLines Number of lines
     * @return String of line numbers
     */
    public String getLineNumbers(int nLines) {
        Logs.info(this.getClass().getName(), "Total lines = " + nLines);
        StringBuilder text = new StringBuilder("0" + System.getProperty("line.separator"));
        for(int i = 1; i < nLines + 1; i++){
            text.append(i).append(System.getProperty("line.separator"));
        }
        return text.toString();
    }

    /**
     * Get the number of lines
     * @return Number of lines
     */
    public int getNLines() {
        return mNumLines;
    }

    /**
     * Get the height of one line
     * @return Line height (in px)
     */
    public int getLineHeight() {
        String TAG = NAME + "getLineHeight";
//        Logs.d(TAG, "", getPreferredSize().height, getNVisibleLines());
//        return getPreferredSize().height / getNVisibleLines();
        int bodyPaneH = getViewport().getView().getPreferredSize().height;
        Logs.d(TAG, "", bodyPaneH, mNumLines);
        return bodyPaneH / mNumLines;
    }

    /**
     * Get the number of visible lines
     * @return Number of visible lines
     */
    public int getNVisibleLines() {
        String TAG = NAME + "getNVisibleLines";
        return mDim.height / getLineHeight();
    }

    /**
     * Get the maximum value of the scroll bar
     * @return Maximum scroll value
     */
    public int getMaxScrollVal() {
        return getVerticalScrollBar().getMaximum();
    }

    /**
     * Check if a value is inside the frame
     * @param scrollVal Scroll value
     * @return True/false
     */
    public boolean isInsideFrames(int scrollVal) {
        final String TAG = NAME + "isInsideFrames";
        return mTargetMinMax.isWithin(scrollVal);
    }

    /**
     * Get a random line between the two values
     * @param min Min line index (inclusive)
     * @param max Max line index (exclusive)
     * @return Line number
     */
    public int getRandLine(int min, int max) {
        final String TAG = NAME + "getRandLine";

        int lineInd = 0;
        do {
            lineInd = Utils.randInt(min, max);
        } while (mLineCharCounts.get(lineInd) == 0);

        return lineInd;
    }

    //------------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {
        Logs.d(NAME, "Mouse Clicked", 0);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Logs.d(NAME, "Mouse Pressed", 0);
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mIsCursorIn = true;
        Logs.d(NAME, "Mouse Entered", 0);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mIsCursorIn = false;
        Logs.d(NAME, "Mouse Exited", 0);
    }

    //-------------------------------------------------------------------------------------------------

    /***
     * Custom class for scroll bars
     */
    private class CustomScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(244, 244, 244));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g.setColor(Color.BLACK);
            g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

            // Highlight scroll bar rect
            double ratio = trackBounds.height / (getVerticalScrollBar().getMaximum() * 1.0);
            int hlY = (int) (mTargetMinMax.getMin() * ratio);
            int hlH = (int) (mTargetMinMax.getRange() * ratio) + getThumbBounds().height;
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
