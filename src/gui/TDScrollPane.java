package gui;

import control.Logger;
import experiment.Experiment;
import tools.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

import static tools.Consts.*;
import static control.Logger.*;

public class TDScrollPane extends JScrollPane implements MouseListener, MouseWheelListener, AdjustmentListener {
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

    // Status
    private boolean mCursorIn;
    private boolean mTargetVisible;
    private boolean mShiftDown;
    private Pair mLastScrollVals = new Pair();  // Keep the last scroll values (vt,hz) {to calculate the diff}

    // Elements
    private MyScrollBarUI vtScrollBarUI;
    private MyScrollBarUI hzScrollBarUI;

    // Traget
    private MinMax mFrameVtMinMax = new MinMax(); // MinMax of verical frame
    private MinMax mFrametHzMinMax = new MinMax(); // MinMax of horizontal frame
    private Pair mTargetInd = new Pair(); // Row and col ind of the target
    private MinMax mTargetFullVisVtScVals = new MinMax(); // Vt scroll values for target fully visible
    private MinMax mTargetPartVisVtScVals = new MinMax(); // Vt scroll values for target partially visible
    private MinMax mTargetFullVisHzScVals = new MinMax(); // Hz scroll values for target fully visible
    private MinMax mTargetPartVisHzScVals = new MinMax(); // Hz scroll values for target partially visible

    // For logging
    private GeneralInfo mGenInfo = new GeneralInfo();
    private InstantInfo mInstantInfo = new InstantInfo();
    private ScrollInfo mScrollInfo = new ScrollInfo();
    private int mNTargetAppear;

    // Keystrokes
    private KeyStroke KS_SHIFT;
    private KeyStroke KS_L;

    private final Action SHIFT_RELEASE = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            mShiftDown = false;
        }
    };

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

        setBorder(BorderFactory.createLineBorder(COLORS.VIEW_BORDER));
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
                COLORS.VIEW_BORDER,
                COLORS.SCROLLBAR_TRACK,
                COLORS.SCROLLBAR_THUMB,
                6);
        getVerticalScrollBar().setUI(vtScrollBarUI);
        getVerticalScrollBar().setPreferredSize(vSBDim);

        // Horizontal
        hSBDim = new Dimension(paneSize, sbW);
//        Dimension hSBThumbDim = new Dimension(thumbLen, hSBDim.height);
//        UIManager.put("ScrollBar.minimumThumbSize", hSBThumbDim);
//        UIManager.put("ScrollBar.maximumThumbSize", hSBThumbDim);

        hzScrollBarUI = new MyScrollBarUI(
                COLORS.VIEW_BORDER,
                COLORS.SCROLLBAR_TRACK,
                COLORS.SCROLLBAR_THUMB,
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
        addMouseWheelListener(this);
        setWheelScrollingEnabled(true);
        getVerticalScrollBar().addAdjustmentListener(this);
        getHorizontalScrollBar().addAdjustmentListener(this);
        mapKeys();

        return this;
    }

    /**
     * Set some flags according to the technique
     * @param tech New technique
     */
    public void changeTechnique(Experiment.TECHNIQUE tech) {
        if (tech == Experiment.TECHNIQUE.MOUSE) {
            setWheelScrollingEnabled(true);
        } else {
            setWheelScrollingEnabled(false);
        }
    }

    /**
     * Highlight one cell
     * @param targetRCInd Row,Col index
     * @param frameSizeCells Size of fram in cells
     */
    public TDScrollPane highlight(Pair targetRCInd, int frameSizeCells) {
        String TAG = NAME + "highlight";
        final int nVisibleRows = Experiment.TD_N_VIS_ROWS;

        mTargetInd = targetRCInd;
        final int rInd = targetRCInd.getFirst();
        final int cInd = targetRCInd.getSecond();

        // Highlight the cell
        HighlightTableRenderer hlRenderer = new HighlightTableRenderer(rInd, cInd);
        bodyTable.setDefaultRenderer(bodyTable.getColumnClass(cInd), hlRenderer);

        //-- Show indicators in the scrollbars
        int frOffset = (nVisRows - frameSizeCells) / 2;

        // Vertical
        mFrameVtMinMax.setMin((rInd - (frameSizeCells - 1) - frOffset) * cellSize);
        mFrameVtMinMax.setMax((rInd - frOffset) * cellSize);
//        vtScrollBarUI.setHighlightFrame(COLORS.SCROLLBAR_HIGHLIGHT, mTargetVtMinMax);

        final int targetVtPos = (rInd - nVisibleRows + 1) * cellSize;
        vtScrollBarUI.setVtIndicator(COLORS.SCROLLBAR_INDIC, targetVtPos);
        getVerticalScrollBar().setUI(vtScrollBarUI);

        // Horizontal
        mFrametHzMinMax.setMin((cInd - (frameSizeCells - 1) - frOffset) * cellSize);
        mFrametHzMinMax.setMax((cInd - frOffset) * cellSize);
//        hzScrollBarUI.setHighlightFrame(COLORS.SCROLLBAR_HIGHLIGHT, mTargetHzMinMax);

        final int targetHzPos = (cInd - nVisibleRows + 1) * cellSize;
        hzScrollBarUI.setHzIndicator(COLORS.SCROLLBAR_INDIC, targetHzPos);
        getHorizontalScrollBar().setUI(hzScrollBarUI);

//        isHighlighted = true;

        //-- Set scroll values once to use later
        // Full visibility
        mTargetFullVisVtScVals.setMin((rInd - nVisRows + 1) * cellSize);
        mTargetFullVisVtScVals.setMax(rInd * cellSize);

        mTargetFullVisHzScVals.setMin((cInd - nVisRows + 1) * cellSize);
        mTargetFullVisHzScVals.setMax(cInd * cellSize);

        // Partial visibility
        mTargetPartVisVtScVals.setMin((rInd - nVisRows) * cellSize);
        mTargetPartVisVtScVals.setMax((rInd + 1) * cellSize);

        mTargetPartVisHzScVals.setMin((cInd - nVisRows) * cellSize);
        mTargetPartVisHzScVals.setMax((cInd + 1) * cellSize);


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
        if (mCursorIn) {
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

            // Log
            logScroll();
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
        final int rowInd = rcInd.getFirst();
        final int colInd = rcInd.getSecond();

        // Center the cell
        if (halfViewMinMax.isWithinEx(rowInd) && halfViewMinMax.isWithinEx(colInd)) {
            final Point newPos = new Point(
                    (colInd - half) * cellSize,
                    (rowInd - half) * cellSize
            );

            getViewport().setViewPosition(newPos);
        } else {
            Logs.d(TAG, "ERROR:", "Can't center cell");
        }

        // Set the scroll values
        mLastScrollVals.set(getVerticalScrollBar().getValue(), getHorizontalScrollBar().getValue());

        repaint();
    }

    /**
     * Check if the Target is inside the frame
     * @return 1: inside, 0: outside
     */
    public Pair isTargetInFrames() {
        final int vtScrollVal = getVerticalScrollBar().getValue();
        final int hzScrollVal = getHorizontalScrollBar().getValue();

        final int vtRes = mFrameVtMinMax.isWithin(vtScrollVal) ? 1 : 0;
        final int hzRes = mFrametHzMinMax.isWithin(hzScrollVal) ? 1 : 0;

        return new Pair(vtRes, hzRes);
    }

    /**
     * Check if the target is visible (has entered the viewport)
     * @param fully Check for fully visible (true) or partially (false)
     * @return True/false
     */
    public boolean isTargetVisible(boolean fully) {
        String TAG = NAME + "isTargetVisible";

        final int vtScrollVal = getVerticalScrollBar().getValue();
        final int hzScrollVal = getHorizontalScrollBar().getValue();

        if (fully) {
            return mTargetFullVisVtScVals.isWithin(vtScrollVal) &&
                    mTargetFullVisHzScVals.isWithin(hzScrollVal);
        } else {
            return mTargetPartVisVtScVals.isWithin(vtScrollVal) &&
                    mTargetPartVisHzScVals.isWithin(hzScrollVal);
        }

    }

    /**
     * Return cell size in px
     * @return Cell size (W = H) in px
     */
    public int getCellSize() {
        return cellSize;
    }

    /**
     * Set the GeneralInfo
     * @param genInfo GeneralInfo
     */
    public void setGenInfo(GeneralInfo genInfo) {
        mGenInfo = genInfo;
    }

    /**
     * Set the InstantInfo
     * @param instInfo InstantInfo
     */
    public void setInstantInfo(InstantInfo instInfo) {
        mInstantInfo = instInfo;
    }

    /**
     * Get the InstantInfo instance (to continue filling in other classes)
     * @return Logger.InstantInfo
     */
    public Logger.InstantInfo getInstantInfo() {
        return mInstantInfo;
    }

    /**
     * Get the number of target appearances
     * @return number of target appearances
     */
    public int getNTargetAppear() {
        return mNTargetAppear;
    }

    /**
     * Log scrolling data (so not to repeat it in mouse/scroll)
     */
    private void logScroll() {
        final String TAG = NAME + "logScroll";

        final long nowMillis = Utils.nowInMillis();

        if (mInstantInfo.firstScroll == 0) mInstantInfo.firstScroll = nowMillis;
        else mInstantInfo.lastScroll = nowMillis;

        if (isTargetVisible(true)) { // Target becomes visible
            if (!mTargetVisible) { // Target wasn't already visible
                mNTargetAppear++;

                if (mInstantInfo.targetFirstAppear == 0) mInstantInfo.targetFirstAppear = nowMillis;
                else mInstantInfo.targetLastAppear = nowMillis;

                mTargetVisible = true;
            }
        } else {
            mTargetVisible = false;
        }

        // Only during the experiment
        if (mGenInfo.trial != null) {
            mScrollInfo.abX = MouseInfo.getPointerInfo().getLocation().x;
            mScrollInfo.abY = MouseInfo.getPointerInfo().getLocation().y;
            mScrollInfo.vtAmt = getVerticalScrollBar().getValue() - mLastScrollVals.getFirst();
            mScrollInfo.hzAmt = getHorizontalScrollBar().getValue() - mLastScrollVals.getSecond();
            mScrollInfo.moment = Utils.nowInMillis();

            Logger.get().logScrollInfo(mGenInfo, mScrollInfo);

            mLastScrollVals.set(getVerticalScrollBar().getValue(), getHorizontalScrollBar().getValue());
        }
    }

    /**
     * Reset all the values saved for logging
     */
    public void reset() {
        mInstantInfo = new Logger.InstantInfo();
        mNTargetAppear = 0;
    }

    /**
     * Map the keys
     */
    private void mapKeys() {
        KS_SHIFT = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KS_SHIFT, "SHIFT");
        getActionMap().put("SHIFT", SHIFT_RELEASE);
    }

    // MouseListener ========================================================================================
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mCursorIn = true;
        if (mInstantInfo.firstEntry == 0) mInstantInfo.firstEntry = Utils.nowInMillis();
        else mInstantInfo.lastEntry = Utils.nowInMillis();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mCursorIn = false;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        final String TAG = NAME + "mouseWheelMoved";

        if (isWheelScrollingEnabled() && mCursorIn) {
            mScrollInfo.wheelRot = e.getPreciseWheelRotation(); // Info needed from wheel
            Logs.d(TAG, e.getPreciseWheelRotation());
            logScroll();

            if (e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK) {
                mShiftDown = true;
            }
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {

        // Add the scrolling values to the scrollInfo and log it
//        if (mShiftDown) mScrollInfo.hzAmt = e.getValue() - mLastScrollVals
//        else mScrollInfo.vtAmt = e.getValue() - mLastScrollVals.getFirst();


//        Logs.d(NAME, e.getValue());
    }

    // Custom ScrollBar ========================================================================================
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
