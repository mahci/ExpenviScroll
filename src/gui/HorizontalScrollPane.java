package gui;

import data.Consts;
import data.DimensionD;
import tools.Logs;
import tools.Utils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class HorizontalScrollPane extends JScrollPane {
    private final static String NAME = "HorizontalScrollPane/";
    //-------------------------------------------------------------------------------------------------

    private JTable bodyTable;

    private Dimension dim; // (px)
    private Dimension scBarDim; // (px) needed for the height of rows
    private Dimension scThumbDim; // (px)
    private int colWidth; // Width of columns (equal)

    protected int targetMinScVal, targetMaxScVal;


    //-------------------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param ddMM Dimention of scroll pane (W/H in mm)
     */
    public HorizontalScrollPane(DimensionD ddMM) {
        dim = new Dimension(Utils.mm2px(ddMM.getWidth()), Utils.mm2px(ddMM.getHeight()));
        setPreferredSize(dim);

        setWheelScrollingEnabled(true);
        requestFocusInWindow();
    }

    /**
     * Set the body table inside
     * @param nRows Number of Rows
     * @param nCols Number of columns
     * @param nFrameCols Number of columns show in the frame
     * @return Instance
     */
    public HorizontalScrollPane setTable(int nRows, int nCols, int nFrameCols) {
        String TAG = NAME + "setTable";

        String[] colNames = new String[nCols];
        Object[][] data = new Object[nRows][nCols];
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                data[i][j] = Utils.randInt(1, nCols * nRows + 1);
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, colNames);
        bodyTable = new JTable(model);

//        bodyTable.setPreferredScrollableViewportSize(bodyTable.getPreferredSize());
        bodyTable.setTableHeader(null);
        bodyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bodyTable.setGridColor(Color.GRAY);
        bodyTable.setFont(new Font("Sans", Font.PLAIN, 10));
        bodyTable.setForeground(Color.DARK_GRAY);
        bodyTable.setEnabled(false);

        // Set the size of columns and rows
        colWidth = getPreferredSize().width / nFrameCols;
        for (int j = 0; j < nCols; j++) {
            bodyTable.getColumnModel().getColumn(j).setMaxWidth(colWidth);
        }

        Logs.d(TAG, getPreferredSize().height);
        Logs.d(TAG, scBarDim.height);
        int rowH = (getPreferredSize().height - scBarDim.height) / nRows;
        for (int i = 0; i < nRows; i++) {
            bodyTable.setRowHeight(rowH);
        }

        return this;
    }

    /**
     * Set the horizontal scroll bar
     * @param scrollBarH Scroll bar height (mm)
     * @param thumbW Thumb width (mm)
     * @return Instance
     */
    public HorizontalScrollPane setScrollBar(double scrollBarH, double thumbW) {
        String TAG = NAME + "setScrollBar";

        // Set dimentions
        int scBarH = Utils.mm2px(scrollBarH);
        int scBarW = dim.width;
        scBarDim = new Dimension(scBarW, scBarH);
        scThumbDim = new Dimension(Utils.mm2px(thumbW), scBarH);

        // Verticall scroll bar
        getHorizontalScrollBar().setUI(new CustomHScrollBarUI(6));
        getHorizontalScrollBar().setPreferredSize(scBarDim);

        // Scroll thumb
        UIManager.put("ScrollBar.thumbSize", scThumbDim);

        // Policies
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        return this;
    }

    /**
     * Final method for creating the pane
     * @return Instance
     */
    public HorizontalScrollPane create() {
        getViewport().add(bodyTable);

        return this;
    }

    /**
     * Highlight a column
     * @param colInd Column indexnumber
     */
    public void higlight(int colInd, int tgMinScVl, int tgMaxScVl) {
        DefaultTableCellRenderer highlightRenderer = new DefaultTableCellRenderer();
        highlightRenderer.setBackground(Consts.COLORS.CELL_HIGHLIGHT);

        bodyTable.revalidate();
        bodyTable.getColumnModel().getColumn(colInd).setCellRenderer(highlightRenderer);

        // Set the target range (for scrollbar rect)
        targetMinScVal = tgMinScVl;
        targetMaxScVal = tgMaxScVl;

        getHorizontalScrollBar().revalidate();
    }

    /**
     * Add MouseWheelListener to every component
     * @param mwl MouseWheelListener
     */
    public void addWheelListener(MouseWheelListener mwl) {
        getHorizontalScrollBar().addMouseWheelListener(mwl);
        bodyTable.addMouseWheelListener(mwl);
    }

    /**
     * Get the width of columns
     * @return Columns' width
     */
    public int getColWidth() {
        return colWidth;
    }


//    @Override
//    public void mouseWheelMoved(MouseWheelEvent e) {
//        String TAG = NAME + "mouseWheelMoved";
//
//        if (e.isShiftDown()) {
//            Logs.info(TAG, "Scrolling with SHIFT");
//        } else {
//            // Do nothing
//        }
//
//    }

    //-------------------------------------------------------------------------------------------------
    private class CustomHScrollBarUI extends BasicScrollBarUI {

        private int thumbHOffset;

        /**
         * Constructor
         * @param thumbHOffset Offset on top and button of the thumb
         */
        protected CustomHScrollBarUI(int thumbHOffset) {
            this.thumbHOffset = thumbHOffset;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            String TAG = NAME + "paintTrack";

            g.setColor(new Color(244, 244, 244));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g.setColor(Color.BLACK);
            g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

            // Highlight scroll bar rect
            double ratio = trackBounds.width / (getHorizontalScrollBar().getMaximum() * 1.0);
            int hlX = (int) (targetMinScVal * ratio);
            int hlW = (int) ((targetMaxScVal - targetMinScVal) * ratio) + getThumbBounds().width;
            g.setColor(Consts.COLORS.SCROLLBAR_HIGHLIGHT);
            g.fillRect(hlX, trackBounds.y, hlW, trackBounds.height);
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
                    thumbBounds.x,
                    thumbBounds.y + (thumbHOffset / 2),
                    thumbBounds.width, thumbBounds.height - thumbHOffset,
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
