package gui;

import javax.swing.*;
import java.awt.*;

public class TechnEndPanel extends JPanel {
    private final static String NAME = "TechnEndPanel/";
    // -------------------------------------------------------------------------------------------

    // Elements
    private JLabel textLabel;
    private JButton continueButton;

    // -------------------------------------------------------------------------------------------
    public TechnEndPanel(String mssg, String nextTech) {
        String TAG = NAME;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        textLabel = new JLabel(mssg);
        textLabel.setFont(new Font("Sans", Font.BOLD, 35));
        add(textLabel);

        continueButton = new JButton("Continue to " + nextTech);
        continueButton.setPreferredSize(new Dimension(300, 50));
        add(continueButton);
    }


}
