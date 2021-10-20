package gui;

import control.Experimenter;
import control.Server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainFrame extends JFrame {

    public MainFrame() {
        // Set the properties of the frame
        setExtendedState(JFrame.MAXIMIZED_BOTH); // maximized frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close on exit

        // start the Setup panel
        SetupPanel setupPanel = new SetupPanel();
        this.add(setupPanel);


        ExperimentPanel expPanel = new ExperimentPanel();

        this.add(expPanel);
        this.pack();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();

        Rectangle scrBound = gd[1].getDefaultConfiguration().getBounds();
        int scrW = scrBound.width;
        int scrH = scrBound.height;

        int frW = getSize().width;
        int frH = getSize().height;

        setLocation(
                ((scrW / 2) - (frW / 2)) + scrBound.x,
                ((scrH / 2) - (frH / 2)) + scrBound.y
        );

        try {
            expPanel.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setVisible(true);

        // [Test] Experimenter
        Experimenter.self().testBlocks();

//        showPanel(new ExperimentPanel());
    }

    public void showPanel(JPanel panel) {
        getContentPane().removeAll();
        revalidate();
        add(panel);
        getContentPane().invalidate();
        getContentPane().validate();

        setVisible(true);
    }
}
