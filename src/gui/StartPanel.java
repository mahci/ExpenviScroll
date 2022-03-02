package gui;

import tools.Logs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class StartPanel extends JPanel {

    public StartPanel(String pid, String tech) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Box.createRigidArea(new Dimension(0, 300)));

//        JPanel pcPanel = new JPanel();
//        pcPanel.setLayout(new FlowLayout());
//        pcPanel.setMaximumSize(new Dimension(500, 50));

        JLabel pcIdLabel = new JLabel("Participant Id: " + pid);
        pcIdLabel.setAlignmentX(CENTER_ALIGNMENT);
        pcIdLabel.setFont(new Font("Sans", Font.PLAIN, 25));
        pcIdLabel.setPreferredSize(new Dimension(150, 30));
        add(pcIdLabel);

//        pcPanel.add(Box.createRigidArea(new Dimension(20, 0)));
//
//        JLabel pcId = new JLabel(pid);
//        pcId.setFont(new Font("Sans", Font.PLAIN, 20));
//        pcId.setPreferredSize(new Dimension(150, 30));
//        pcPanel.add(pcId);

//        JTextField pcIdField = new JTextField();
//        pcIdField.setFont(new Font("Sans", Font.PLAIN, 20));
//        pcIdField.setPreferredSize(new Dimension(100, 30));
//        pcPanel.add(pcIdField);

//        add(pcPanel);

        add(Box.createRigidArea(new Dimension(0, 100)));

        JButton techBtn = new JButton("Start with " + tech);
        techBtn.setAlignmentX(CENTER_ALIGNMENT);
        techBtn.setFont(new Font("Sans", Font.BOLD, 25));
        techBtn.setMaximumSize(new Dimension(400, 70));
        add(techBtn);


//        JPanel btnPanel = new JPanel();
//        btnPanel.setLayout(new FlowLayout());
//        btnPanel.setMaximumSize(new Dimension(600, 60));
//
//        JButton firstTechBtn = new JButton("Mouse");
//        firstTechBtn.setFont(new Font("Sans", Font.PLAIN, 20));
//        firstTechBtn.setPreferredSize(new Dimension(200, 50));
//        btnPanel.add(firstTechBtn);
//
//        btnPanel.add(Box.createRigidArea(new Dimension(100, 0)));
//
//        JButton secondTechBtn = new JButton("Flick");
//        secondTechBtn.setFont(new Font("Sans", Font.PLAIN, 20));
//        secondTechBtn.setPreferredSize(new Dimension(200, 50));
//        btnPanel.add(secondTechBtn);
//
//        add(btnPanel);

        techBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Logs.d("BlockPanel", "Clicked");
                ExperimentFrame.get().startExperiment();
//                SwingUtilities.getWindowAncestor(StartPanel.this).dispose();
            }
        });
    }
}
