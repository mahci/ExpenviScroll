import control.Server;
import gui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        Server.get().start();
    }

}
