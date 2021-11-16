package gui;

import tools.Consts;
import tools.DimensionD;
import tools.Logs;
import tools.Utils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static tools.Consts.*;

public class TDScrollPane extends JScrollPane {
    private final static String NAME = "TDScrollPane";

    private JTable bodyTable;                   // Inside table

    private Dimension paneDim;                  // (px) Total dimennsion of the pane
    private Dimension vSBDim, hSBDim;           // (px) Dimensions of the scroll bars (prob. equal)
     // (px) Dimensions of the scroll thumbs (prob. equal)

    private int colW, rowH;                     // (px) Column width, row height
    private int nVisCols, nVisRows;             // Number of visible columns, rows

    //-------------------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param dimMM Dimnension in mm
     */
    public TDScrollPane(DimensionD dimMM) {
        // Set the size
        paneDim = new Dimension(Utils.mm2px(dimMM.getWidth()), Utils.mm2px(dimMM.getHeight()));
        setPreferredSize(paneDim);
    }

    /**
     * Set the body table
     * (!) Call after {@link #setScrollBars(double, double)}}
     * @param nRows Number of Rows
     * @param nCols Number of columns
     * @param nVisRows Number of visible rows
     * @param nVisCols Number of visible columns
     * @return Instance
     */
    public TDScrollPane setTable(int nRows, int nCols, int nVisRows, int nVisCols) {
        String TAG = NAME + "setTable";

        // Set random data (numbers) for the table
        String[] colNames = new String[nCols];
        Object[][] tableData = new Object[nRows][nCols];
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                tableData[i][j] = Utils.randInt(1, nCols * nRows + 1);
            }
        }
        DefaultTableModel model = new DefaultTableModel(tableData, colNames);
        bodyTable = new JTable(model);
        Logs.info(TAG, "bodyTable data generated");
        // Table properties
        bodyTable.setTableHeader(null);
        bodyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bodyTable.setGridColor(COLORS.TABLE_GRID);
        bodyTable.setForeground(COLORS.TABLE_TEXT);
        bodyTable.setFont(FONTS.TABLE_FONT);
        bodyTable.setEnabled(false);

        Logs.info(TAG, "bodyTable props set");
        // Set the size of columns and rows
        colW = (getPreferredSize().width - getVerticalScrollBar().getWidth()) / nVisCols;
        for (int c = 0; c < nCols; c++) {
            bodyTable.getColumnModel().getColumn(c).setMaxWidth(colW);
        }

        rowH = (getPreferredSize().height - getHorizontalScrollBar().getHeight()) / nVisRows;
        for (int i = 0; i < nRows; i++) {
            bodyTable.setRowHeight(rowH);
        }
        Logs.info(TAG, "Dim: " + getPreferredSize());
        return this;
    }

    /**
     * Set the scroll bars
     * @param scrollBarWMM Scroll bar length (for both directions) (mm)
     * @param thumbLenWW Thumb length (for both directions) (mm)
     * @return Instance
     */
    public TDScrollPane setScrollBars(double scrollBarWMM, double thumbLenWW) {
        String TAG = NAME + "setScrollBars";

        int sbW = Utils.mm2px(scrollBarWMM);
        int thumbLen = Utils.mm2px(thumbLenWW);

        // Set scrollbars
        CustomScrollBarUI csbUI = new CustomScrollBarUI(Color.BLACK, COLORS.SCROLLBAR_TRACK, Color.BLACK, 6);

        vSBDim = new Dimension(sbW, paneDim.height);
//        getVerticalScrollBar().setUI(csbUI);
        getVerticalScrollBar().setPreferredSize(vSBDim);

        hSBDim = new Dimension(paneDim.width, sbW);
//        getHorizontalScrollBar().setUI(csbUI);
        getHorizontalScrollBar().setPreferredSize(hSBDim);

        // Set thumbs
        Dimension vSBThumbDim = new Dimension(vSBDim.width, thumbLen);
        Dimension hSBThumbDim = new Dimension(thumbLen, hSBDim.height);
        UIManager.put("ScrollBar.minimumThumbSize", vSBThumbDim);
        UIManager.put("ScrollBar.maximumThumbSize", vSBThumbDim);

        // Policies
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        Logs.info(TAG, "Scrollbars set");
        return this;
    }

    public TDScrollPane create() {
        getViewport().add(bodyTable);
        return this;
    }


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

            g.setColor(Consts.COLORS.SCROLLBAR_HIGHLIGHT);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
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
