package gui;

import tools.Consts;
import tools.DoubleDimension;
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
    private JScrollBar scrollBar;

    private Dimension dim; // (px)
    private Dimension scBarDim; // (px) needed for the height of rows
    private Dimension scThumbDim; // (px)
    private int colWidth; // Width of columns (equal)


    //-------------------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param ddMM Dimention of scroll pane (W/H in mm)
     */
    public HorizontalScrollPane(DoubleDimension ddMM) {
        Logs.addTag(getClass().getSimpleName());
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

        // Set the size of columns and rows
        colWidth = getPreferredSize().width / nFrameCols;
        for (int j = 0; j < nCols; j++) {
            bodyTable.getColumnModel().getColumn(j).setMaxWidth(colWidth);
        }

        Logs.info(TAG, getPreferredSize().height);
        Logs.info(TAG, scBarDim.height);
        int rowH = (getPreferredSize().height - scBarDim.height) / nRows;
        for (int i = 0; i < nRows; i++) {
            bodyTable.setRowHeight(rowH);
        }

        return this;
    }

    /**
     * Set the horizontal scroll bar
     * @param scrollBarH Scroll bar height (mm)
     * @param thumbH Thumb height (px)
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
//        UIManager.put("ScrollBar.minimumThumbSize", scThumbDim);
//        UIManager.put("ScrollBar.maximumThumbSize", scThumbDim);

        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

//        scrollBar = getHorizontalScrollBar();
//        scrollBar.addAdjustmentListener(new AdjustmentListener() {
//            @Override
//            public void adjustmentValueChanged(AdjustmentEvent e) {
//
//            }
//        });

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
    public void higlight(int colInd) {
        DefaultTableCellRenderer highlightRenderer = new DefaultTableCellRenderer();
        highlightRenderer.setBackground(Consts.COLORS.LINE_COL_HIGHLIGHT);

        bodyTable.getColumnModel().getColumn(colInd).setCellRenderer(highlightRenderer);
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
    private static class CustomHScrollBarUI extends BasicScrollBarUI {

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
            g.setColor(new Color(244, 244, 244));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g.setColor(Color.BLACK);
            g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

//            g.setColor(Consts.COLORS.SCROLLBAR_HIGHLIGHT);
//            g.fillRect(trackBounds.x, trackBounds.y + 80, trackBounds.width, getThumbBounds().height);
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
