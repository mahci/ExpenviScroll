package gui;

import tools.Consts.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class CustomScrollBarUI extends BasicScrollBarUI {

    private final double THUMB_W_mm = 3;
    private final double THUMB_H_mm = 5;

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(new Color(244, 244, 244));
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        g.setColor(Color.BLACK);
        g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

        g.setColor(COLORS.SCROLLBAR_HIGHLIGHT);
        g.fillRect(trackBounds.x, trackBounds.y + 80, trackBounds.width, getThumbBounds().height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
//        thumbBounds.width -= 2;
//        thumbBounds.height = Utils.mm2px(THUMB_H_mm);
        // Set anti-alias
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRoundRect(
                thumbBounds.x + 4, thumbBounds.y,
                thumbBounds.width - 6, thumbBounds.height,
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
