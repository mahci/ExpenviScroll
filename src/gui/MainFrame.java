package gui;

import control.Experimenter;
import control.Server;
import experiment.Experiment;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainFrame extends JFrame {

    /**
     * Constructor
     */
    public MainFrame() {
        setDisplayConfig();

        // start the Setup panel
        SetupPanel setupPanel = new SetupPanel();
        this.add(setupPanel);

        // Create and show an experiment
        int pid = 0;
        ExperimentPanel expPanel = new ExperimentPanel(Experimenter.get().getExperiment(pid));
        this.add(expPanel);
        this.pack();

        this.setVisible(true);
    }


    /**
     * Set the config for showing panels
     */
    private void setDisplayConfig() {
        setExtendedState(JFrame.MAXIMIZED_BOTH); // maximized frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close on exit

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
    }

}
