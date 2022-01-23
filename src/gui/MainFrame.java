package gui;

import experiment.Experiment;
import tools.Logs;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final static String NAME = "MainFrame/";
    // -------------------------------------------------------------------------------------------
    private static MainFrame self; // Singelton instance

    private static DemoPanel mDemoPanel;
    private static ExperimentPanel mExperimentPanel;

    private Experiment mExperiment;

    /**
     * Constructor
     */
    public MainFrame() {
        setDisplayConfig();

        // Create and show an experiment
        final int pid = 123;
        mExperiment = new Experiment(pid);
    }

    public static MainFrame get() {
        if (self == null) self = new MainFrame();
        return self;
    }

    public void showDemo() {
        mDemoPanel = new DemoPanel(mExperiment);
        add(mDemoPanel);
        pack();

        setVisible(true);
    }

    public void showExperiment() {
        getContentPane().removeAll();

        mExperimentPanel = new ExperimentPanel(mExperiment);
        add(mExperimentPanel);
        mExperimentPanel.requestFocusInWindow();

        revalidate();
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
        mExperimentPanel.scroll(vtScrollAmt, hzScrollAmt);
    }

    public static void stopScroll() {
        mExperimentPanel.repaint();
    }

}
