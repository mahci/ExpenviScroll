package gui;

import control.Logger;
import experiment.Experiment;

import javax.swing.*;

public class Main {

    private static final int PID = 120;

    private static final int SESSION = 1;
    private static final int PART = 1;

    /**
     * MAIN
     * @param args Arguments
     */
    public static void main(String[] args) {
        final Experiment experiment = new Experiment(PID);
        Logger.get().logParticipant(PID); // Set up logging

        ExperimentFrame.get()
                .setPart(experiment.getPart(SESSION - 1, PART - 1))
                .showIntro(PID, SESSION, PART);
    }

    /**
     * Show a dialog
     * @param mssg Message to show in the dialog
     */
    public static void showDialog(String mssg) {
        JOptionPane.showMessageDialog(ExperimentFrame.get(), mssg);
    }
}
