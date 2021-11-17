package gui;

import tools.Consts;
import tools.DimensionD;
import tools.Logs;
import tools.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
     * Set the scroll bars
     * @param scrollBarWMM Scroll bar length (for both directions) (mm)
     * @param thumbLenWW Thumb length (for both directions) (mm)
     * @return Instance
     */
    public TDScrollPane setScrollBars(double scrollBarWMM, double thumbLenWW) {
        String TAG = NAME + "setScrollBars";

        int sbW = Utils.mm2px(scrollBarWMM);
        int thumbLen = Utils.mm2px(thumbLenWW);

        //-- Set scrollbars
        // Vertical
        vSBDim = new Dimension(sbW, paneDim.height);
        Dimension vSBThumbDim = new Dimension(vSBDim.width, thumbLen);
        UIManager.put("ScrollBar.minimumThumbSize", vSBThumbDim);
        UIManager.put("ScrollBar.maximumThumbSize", vSBThumbDim);

        CustomScrollBarUI vCustomSBUI = new CustomScrollBarUI(
                Color.BLACK,
                COLORS.SCROLLBAR_TRACK,
                Color.BLACK,
                6);
        getVerticalScrollBar().setUI(vCustomSBUI);
        getVerticalScrollBar().setPreferredSize(vSBDim);

        // Horizontal
        hSBDim = new Dimension(paneDim.width, sbW);
        Dimension hSBThumbDim = new Dimension(thumbLen, hSBDim.height);
        UIManager.put("ScrollBar.minimumThumbSize", hSBThumbDim);
        UIManager.put("ScrollBar.maximumThumbSize", hSBThumbDim);

        CustomScrollBarUI hCustomSBUI= new CustomScrollBarUI(
                Color.BLACK,
                COLORS.SCROLLBAR_TRACK,
                Color.BLACK,
                6); // IMPORTANT to create a new CSBUI
//        hCustomSBUI.setHighlight(COLORS.LINE_COL_HIGHLIGHT, 100, 200);
        getHorizontalScrollBar().setUI(hCustomSBUI);
        getHorizontalScrollBar().setPreferredSize(hSBDim);

        // Set policies
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        return this;
    }

    /**
     * Highlight one cell
     * @param rInd Row index
     * @param cInd Column index
     */
    public void highlight(int rInd, int cInd) {
        CellRenderer cellHighlighter = new CellRenderer(rInd, cInd);

//        bodyTable.revalidate();
        bodyTable.getColumnModel().getColumn(cInd).setCellRenderer(cellHighlighter);

        // Set highlight in the scrollbars
        CustomScrollBarUI vCSBUI = (CustomScrollBarUI) getVerticalScrollBar().getUI();
        vCSBUI.setHighlight(COLORS.SCROLLBAR_HIGHLIGHT, 100, 200);

        CustomScrollBarUI hCSBUI = (CustomScrollBarUI) getHorizontalScrollBar().getUI();
        hCSBUI.setHighlight(COLORS.SCROLLBAR_HIGHLIGHT, 100, 200);

    }

    //-------------------------------------------------------------------------------------------------

    /**
     * Renderer class for highlighting a single cell
     */
    private class CellRenderer extends DefaultTableCellRenderer {

        int rowInd, colInd;

        public CellRenderer(int rInd, int cInd) {
            rowInd = rInd;
            colInd = cInd;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);

            if (row == rowInd && column == colInd) setBackground(COLORS.LINE_COL_HIGHLIGHT);
            else setBackground(table.getBackground());

            return this;

        }
    }
}
