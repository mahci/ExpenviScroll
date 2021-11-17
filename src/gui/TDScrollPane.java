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

        // Set scrollbars
        CustomScrollBarUI csbUI = new CustomScrollBarUI(Color.BLACK, COLORS.SCROLLBAR_TRACK, Color.BLACK, 6);

        vSBDim = new Dimension(sbW, paneDim.height);
        getVerticalScrollBar().setUI(csbUI);
        getVerticalScrollBar().setPreferredSize(vSBDim);

        hSBDim = new Dimension(paneDim.width, sbW);
        getHorizontalScrollBar().setUI(csbUI);
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

        return this;
    }

}
