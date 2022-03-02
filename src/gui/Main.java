package gui;

import control.Server;
import javax.swing.*;

import static experiment.Experiment.*;

public class Main {

    private static final int PID = 129;
    private static final TECHNIQUE TECH = TECHNIQUE.MOUSE;

    public static void main(String[] args) {
        ExperimentFrame.get().start(PID, TECH);
    }

    public static void showDialog(String mssg) {
        JOptionPane.showMessageDialog(ExperimentFrame.get(), mssg);
    }
}
