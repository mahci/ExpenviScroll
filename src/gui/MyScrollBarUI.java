package gui;

import tools.Logs;
import tools.MinMax;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class MyScrollBarUI extends BasicScrollBarUI {
    private String NAME = "MyScrollBarUI/";

    private Color borderColor;
    private Color trackColor;
    private Color thumbColor;
    private Color highlightColor;
    private int offset; // dist. to the sides of thumb

    private int highlightMin, highlightMax; // Min/max value of hightlight area

    /**
     * Create the scroll bar
     * @param borderClr Border color
     * @param trackClr Track color
     * @param thumbClr Thumb color
     * @param offs Offset
     */
    public MyScrollBarUI(Color borderClr, Color trackClr, Color thumbClr, int offs) {
        borderColor = borderClr;
        trackColor = trackClr;
        thumbColor = thumbClr;
        offset = offs;
    }

    /**
     * Highligh
     * @param hlColor Highlight color
     * @param hlMin Highlight min value
     * @param hlMax Highlight max value
     */
    public void setIndicator(Color hlColor, int hlMin, int hlMax) {
        highlightColor = hlColor;
        highlightMin = hlMin;
        highlightMax = hlMax;
    }

    /**
     * Set the frame
     * @param hlColor Highlight color
     * @param hlMinMax Min/max of the frame
     */
    public void setHighlightFrame(Color hlColor, MinMax hlMinMax) {
        highlightColor = hlColor;
        highlightMin = hlMinMax.getMin();
        highlightMax = hlMinMax.getMax();
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        String TAG = NAME + "paintTrack";

        // Track
        g.setColor(trackColor);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        // Border
        g.setColor(borderColor);
        g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

        // Highlight the rectangle on the track (if there's highlight)
        if (highlightColor != null) {
            if (scrollbar.getOrientation() == HORIZONTAL) {
                double ratio = trackBounds.width / (scrollbar.getMaximum() * 1.0);
                int hlX = (int) (highlightMin * ratio);
                int hlXMax = (int) (highlightMax * ratio);
                int hlW = hlXMax - hlX + thumbRect.width;
                g.setColor(highlightColor);
                g.fillRect(hlX, trackBounds.y + 1, hlW, trackBounds.height);
            } else { // VERTICAL
                double ratio = trackBounds.height / (scrollbar.getMaximum() * 1.0);
                int hlYMin = (int) (highlightMin * ratio);
                int hlYMax = (int) (highlightMax * ratio);
                int hlH = hlYMax - hlYMin + thumbRect.height;
                g.setColor(highlightColor);
                g.fillRect(trackBounds.x + 1, hlYMin, trackBounds.width, hlH);
            }

        }
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D graphics2D = (Graphics2D) g;

        graphics2D.setColor(thumbColor);
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (scrollbar.getOrientation() == HORIZONTAL) {
            graphics2D.fillRoundRect(
                    thumbBounds.x,
                    thumbBounds.y + (offset / 2),
                    thumbBounds.width, thumbBounds.height - offset,
                    5, 5);
        } else { // VERTICAL
            graphics2D.fillRoundRect(
                    thumbBounds.x + (offset / 2),
                    thumbBounds.y,
                    thumbBounds.width - offset, thumbBounds.height,
                    5, 5);
        }

    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    /**
     * Create a dummy button (used for inc/dec buttons
     * @return JButton
     */
    protected JButton createZeroButton() {
        JButton button = new JButton("zero button");
        Dimension zeroDim = new Dimension(0,0);
        button.setPreferredSize(zeroDim);
        button.setMinimumSize(zeroDim);
        button.setMaximumSize(zeroDim);
        return button;
    }
}
