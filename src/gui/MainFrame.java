package gui;

import control.Server;
import experiment.Experiment;
import tools.Logs;
import tools.Memo;

import javax.swing.*;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.util.Arrays;

import static experiment.Experiment.TECHNIQUE.*;
import static tools.Consts.STRINGS.*;

public class MainFrame extends JFrame {
    private final static String NAME = "MainFrame/";

    private static ExperimentPanel experimentPanel;

    /**
     * Constructor
     */
    public MainFrame() {
        setDisplayConfig();

        // Create and show an experiment
        final int pid = 7;
        final Experiment experiment = new Experiment(pid);
        Logs.d(NAME, experiment.getListOfTechniques());
        experimentPanel = new ExperimentPanel(experiment);
        add(experimentPanel);
        pack();

        setVisible(true);
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

        // Set mm size in pixels
//        Logs.info("TAG", Toolkit.getDefaultToolkit().getScreenResolution());
    }

    public static void scroll(int vtScrollAmt, int hzScrollAmt) {
        experimentPanel.scroll(vtScrollAmt, hzScrollAmt);
    }

    public static void stopScroll() {
        experimentPanel.repaint();
    }



}
