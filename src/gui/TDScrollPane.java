package gui;

import experiment.Experiment;
import tools.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

import static tools.Consts.*;

public class TDScrollPane extends JScrollPane implements MouseListener {
    private final static String NAME = "TDScrollPane/";

    private JTable bodyTable;                   // Inside table

    private Dimension paneDim;                  // (px) Total dimennsion of the pane
    private int paneSize;                       // Size of the pane (px)
    private Dimension vSBDim, hSBDim;           // (px) Dimensions of the scroll bars (prob. equal)
     // (px) Dimensions of the scroll thumbs (prob. equal)

    private int colW, rowH;                     // (px) Column width, row height
    private int cellSize;                       // Size of each cell (px)
    private int sbW;                            // Width of scrollbars (px)
    private int thumbLen;
    private int nVisRows;                       // Number of visible rows/cols

    private MyScrollBarUI vtScrollBarUI;
    private MyScrollBarUI hzScrollBarUI;

    private boolean isHighlighted;
    private MinMax mTargetVtMinMax = new MinMax();
    private MinMax mTargetHzMinMax = new MinMax();

    private boolean mIsCursorIn;
    private boolean mIsMouseHorizontal;

    //-------------------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param dimMM Dimnension in mm
     */
    public TDScrollPane(DimensionD dimMM) {
        // Set the size
        paneDim = new Dimension(
                Utils.mm2px(dimMM.getWidth()),
                Utils.mm2px(dimMM.getHeight()));
        setPreferredSize(paneDim);
    }

    /**
     * Constructor
     * @param nVisRows Number of visible rows = cols
     * @param cellSizeMM Size of each cell (mm)
     * @param sbWMM Width of scrollbar (mm)
     */
    public TDScrollPane(int nVisRows, double cellSizeMM, double sbWMM) {
        this.cellSize = Utils.mm2px(cellSizeMM); // Size of cells in pixels
        this.sbW = Utils.mm2px(sbWMM);
        this.nVisRows = nVisRows;

        int paneSize = nVisRows * cellSize + sbW; // Total size (W=H) of pane
        paneDim = new Dimension(paneSize, paneSize);

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

        // Table properties
        bodyTable.setTableHeader(null);
        bodyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bodyTable.setGridColor(COLORS.TABLE_GRID);
        bodyTable.setForeground(COLORS.TABLE_TEXT);
        bodyTable.setFont(FONTS.TABLE_FONT);
        bodyTable.setEnabled(false);

        // Set the size of columns and rows
        colW = (getPreferredSize().width - getVerticalScrollBar().getWidth()) / nVisCols;
        for (int c = 0; c < nCols; c++) {
            bodyTable.getColumnModel().getColumn(c).setMaxWidth(colW);
        }

        rowH = (getPreferredSize().height - getHorizontalScrollBar().getHeight()) / nVisRows;
        for (int i = 0; i < nRows; i++) {
            bodyTable.setRowHeight(rowH);
        }

        getViewport().add(bodyTable);

        return this;
    }

    /**
     * Set the body table
     * (!) Call after {@link #setScrollBars(double, double)}}
     * @param nRows Number of Rows = num of cols
     * @return Instance
     */
    public TDScrollPane setTable(int nRows) {
        String TAG = NAME + "setTable";

        // Set random data (numbers) for the table
        String[] colNames = new String[nRows];
        Object[][] tableData = new Object[nRows][nRows];
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nRows; j++) {
                tableData[i][j] = Utils.randInt(1, 100);
            }
        }
        DefaultTableModel model = new DefaultTableModel(tableData, colNames);
        bodyTable = new JTable(model);

        // Table properties
        bodyTable.setTableHeader(null);
        bodyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bodyTable.setGridColor(COLORS.TABLE_GRID);
        bodyTable.setForeground(COLORS.TABLE_TEXT);
        bodyTable.setFont(FONTS.TABLE_FONT.deriveFont(12.0f));
        bodyTable.setEnabled(false);

        // Set the size of columns and rows
//        colW = (getPreferredSize().width - getVerticalScrollBar().getWidth()) / nVisRows;
        for (int r = 0; r < nRows; r++) {
            bodyTable.getColumnModel().getColumn(r).setMaxWidth(cellSize);
            bodyTable.setRowHeight(cellSize);
        }

        // Center-align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        bodyTable.setDefaultRenderer(Object.class, centerRenderer);
//
//        rowH = (getPreferredSize().height - getHorizontalScrollBar().getHeight()) / nVisRows;
//        for (int i = 0; i < nRows; i++) {
//            bodyTable.setRowHeight(rowH);
//        }

        getViewport().add(bodyTable);

        paneSize = bodyTable.getPreferredSize().width - sbW; // Set total paneSize

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

        sbW = Utils.mm2px(scrollBarWMM);
        thumbLen = Utils.mm2px(thumbLenWW);

        //-- Set scrollbars
        // Vertical
        vSBDim = new Dimension(sbW, paneSize);
//        Dimension vSBThumbDim = new Dimension(vSBDim.width, thumbLen);
//        UIManager.put("ScrollBar.minimumThumbSize", vSBThumbDim);
//        UIManager.put("ScrollBar.maximumThumbSize", vSBThumbDim);

        vtScrollBarUI = new MyScrollBarUI(
                Color.BLACK,
                COLORS.SCROLLBAR_TRACK,
                Color.BLACK,
                6);
        getVerticalScrollBar().setUI(vtScrollBarUI);
        getVerticalScrollBar().setPreferredSize(vSBDim);

        // Horizontal
        hSBDim = new Dimension(paneSize, sbW);
//        Dimension hSBThumbDim = new Dimension(thumbLen, hSBDim.height);
//        UIManager.put("ScrollBar.minimumThumbSize", hSBThumbDim);
//        UIManager.put("ScrollBar.maximumThumbSize", hSBThumbDim);

        hzScrollBarUI = new MyScrollBarUI(
                Color.BLACK,
                COLORS.SCROLLBAR_TRACK,
                Color.BLACK,
                6); // IMPORTANT to create a new CSBUI
        getHorizontalScrollBar().setUI(hzScrollBarUI);
        getHorizontalScrollBar().setPreferredSize(hSBDim);

        // Set policies
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        return this;
    }

    public TDScrollPane create() {
        getViewport().getView().addMouseListener(this);
        return this;
    }

    /**
     * Highlight one cell
     * @param rcInd Row,Col index
     * @param frameSizeCells Size of fram in cells
     */
    public TDScrollPane highlight(Pair rcInd, int frameSizeCells) {
        String TAG = NAME + "highlight";

        final int rInd = rcInd.getFirst();
        final int cInd = rcInd.getSecond();

        // Highlight the cell
        HighlightTableRenderer hlRenderer = new HighlightTableRenderer(rInd, cInd);
        bodyTable.setDefaultRenderer(bodyTable.getColumnClass(cInd), hlRenderer);

        //-- Show indicators in the scrollbars
        int frOffset = (nVisRows - frameSizeCells) / 2;

        // Vertical
        mTargetVtMinMax.setMin((rInd - (frameSizeCells - 1) - frOffset) * cellSize);
        mTargetVtMinMax.setMax((rInd - frOffset) * cellSize);
        vtScrollBarUI.setHighlightFrame(COLORS.SCROLLBAR_HIGHLIGHT, mTargetVtMinMax);
        getVerticalScrollBar().setUI(vtScrollBarUI);

        // Horizontal
        mTargetHzMinMax.setMin((cInd - (frameSizeCells - 1) - frOffset) * cellSize);
        mTargetHzMinMax.setMax((cInd - frOffset) * cellSize);
        hzScrollBarUI.setHighlightFrame(COLORS.SCROLLBAR_HIGHLIGHT, mTargetHzMinMax);
        getHorizontalScrollBar().setUI(hzScrollBarUI);

        isHighlighted = true;
        return this;
    }

    /**
     * Scroll a certain amount (in both directions)
     * @param vtScrollAmt Vertical scroll amount (px)
     * @param hzScrollAmt Horizontal scroll amount (px)
     */
    public void scroll(int vtScrollAmt, int hzScrollAmt) {
        final String TAG = NAME + "scroll";
        // Scroll only if cursor is inside
        if (mIsCursorIn) {
            final Dimension vpDim = bodyTable.getPreferredSize();
            final int vtExtent = getVerticalScrollBar().getModel().getExtent();
            final int hzExtent = getHorizontalScrollBar().getModel().getExtent();

            if (vtScrollAmt != 0) {
                final Point vpPos = getViewport().getViewPosition();
                final int newY = vpPos.y + vtScrollAmt;
                // Scroll only if amount != 0 and inside the limits
                if (newY >= 0 && newY <= (vpDim.height - vtExtent)) {
                    getViewport().setViewPosition(new Point(vpPos.x, newY));
                    Logs.d(TAG, "vt scrolled to ", newY);
                }
            }

            if (hzScrollAmt != 0) {
                final Point vpPos = getViewport().getViewPosition();
                final int newX = vpPos.x + hzScrollAmt;
                if (newX >= 0 && newX <= (vpDim.width - hzExtent)) {
                    getViewport().setViewPosition(new Point(newX, vpPos.y));
                    Logs.d(TAG, "hz scrolled to ", newX);
                }
            }

            repaint();
        }
    }

    /**
     * Put the specified cell is in the center of the view
     * @param rcInd Row,Column of the cell
     */
    public void centerCell(Pair rcInd) {
        final String TAG = NAME + "centerCell";

        // Check if the lineInd is in the range (to be able to be centered)
        final int nVisRows = Experiment.TD_N_VIS_ROWS;
        final int nRows = Experiment.TD_N_ROWS;
        final int half = nVisRows / 2;
        final MinMax halfViewMinMax = new MinMax(half, (nRows - nVisRows) + half);
        Logs.d(TAG, "rcInd|half:", rcInd.toString(), halfViewMinMax.toString());
        final int rowInd = rcInd.getFirst();
        final int colInd = rcInd.getSecond();
        if (halfViewMinMax.isWithinEx(rowInd) && halfViewMinMax.isWithinEx(colInd)) {
            final Point newPos = new Point(
                    (colInd - half) * cellSize,
                    (rowInd - half) * cellSize
            );

            getViewport().setViewPosition(newPos);
        } else {
            Logs.d(TAG, "ERROR:", "Can't center cell");
        }

        repaint();
    }

    /**
     * Check if the vt,hz scroll values are inside the frames
     * @param vtScrollVal VT scroll value
     * @param hzScrollVal HZ scroll value
     * @return Boolean
     */
    public boolean isInsideFrames(int vtScrollVal, int hzScrollVal) {
        final String TAG = NAME + "isInsideFrames";
        return mTargetVtMinMax.isWithin(vtScrollVal) && mTargetHzMinMax.isWithin(hzScrollVal);
    }

    // Interface impl. ---------------------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {
        Logs.d(NAME, "Mouse Clicked", 0);
    }

    @Override
    public void mousePressed(MouseEvent e) {

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

    /**
     * Renderer class for highlighting a single cell
     */
    private static class HighlightRenderer extends DefaultTableCellRenderer {

        int rowInd, colInd;

        public HighlightRenderer(int rInd, int cInd) {
            rowInd = rInd;
            colInd = cInd;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);

            if (row == rowInd && column == colInd) setBackground(COLORS.CELL_HIGHLIGHT);
            else setBackground(UIManager.getColor("Table.background"));

            return this;

        }
    }

    private static class HighlightTableRenderer extends DefaultTableCellRenderer {

        private final int r, c;

        public HighlightTableRenderer(int row, int col) {
            r = row;
            c = col;

            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (row == r && column == c) setBackground(COLORS.CELL_HIGHLIGHT);
            else setBackground(UIManager.getColor("Table.background"));

            return this;
        }
    }

}
