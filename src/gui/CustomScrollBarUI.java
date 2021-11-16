package gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class CustomScrollBarUI extends BasicScrollBarUI {

    private Color borderColor;
    private Color trackColor;
    private Color thumbColor;
    private Color highlightColor;
    private int offset; // dist. to the sides of thumb

    private int highlightMin, highlightMax; // Min/max value of hightlight area

    public CustomScrollBarUI(Color bdColor, Color trColor, Color thColor, int offs) {
        borderColor = bdColor;
        trackColor = trColor;
        thumbColor = thColor;
        offset = offs;
    }

    public CustomScrollBarUI(Color bdColor, Color trColor, Color thColor, Color hlColor,
                             int offs,
                             int hlMin, int hlMax) {
        borderColor = bdColor;
        trackColor = trColor;
        thumbColor = thColor;
        highlightColor = hlColor;
        offset = offs;

        highlightMin = hlMin;
        highlightMax = hlMax;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
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
                int hlW = (int) ((highlightMax - highlightMin) * ratio) + getThumbBounds().width;

                g.setColor(highlightColor);
                g.fillRect(hlX, trackBounds.height, hlW, trackBounds.height);

            } else { // VERTICAL
                double ratio = trackBounds.height / (scrollbar.getMaximum() * 1.0);
                int hlY = (int) (highlightMin * ratio);
                int hlH = (int) ((highlightMax - highlightMin) * ratio) + getThumbBounds().height;

                g.setColor(highlightColor);
                g.fillRect(trackBounds.x, hlY, trackBounds.width, hlH);
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
