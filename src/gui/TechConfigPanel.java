package gui;

import control.Server;
import experiment.Experiment;
import tools.Logs;
import tools.Memo;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

import static experiment.Experiment.*;
import static tools.Consts.*;

public class TechConfigPanel extends JPanel implements ItemListener {
    private final String NAME = "TechConfigPanel/";

    private JPanel mCardsPanel;

    private JComboBox mTechniquesCoBx;

    private MyConfigSpinner mDragSensitivitySp;
    private MyConfigSpinner mDragGainSp;

    private MyConfigSpinner mRBSensitivitySp;
    private MyConfigSpinner mRBGainSp;
    private MyConfigSpinner mRBDenomSp;

    private Component mStBox = Box.createRigidArea(new Dimension(20, 0));
    private Component mSepBox = Box.createRigidArea(new Dimension(20, 0));
    private Component mEndBox = Box.createRigidArea(new Dimension(300, 0));

    public TechConfigPanel() {
        final String TAG = NAME + "TechConfigPanel";

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        // Drag models
        final SpinnerModel dragSensitivityModel = new SpinnerNumberModel(
                1,
                1,
                10,
                1);
        mDragSensitivitySp = new MyConfigSpinner(dragSensitivityModel);

        final SpinnerModel dragGainModel = new SpinnerNumberModel(
                50.0,
                10.0,
                1000.0,
                10.0);
        mDragGainSp = new MyConfigSpinner(dragGainModel);

        // Rate-based models
        final SpinnerModel rbSensitivityModel = new SpinnerNumberModel(
                2,
                1,
                10,
                1);
        mRBSensitivitySp = new MyConfigSpinner(rbSensitivityModel);

        final SpinnerModel rbGainModel = new SpinnerNumberModel(
                1.5,
                1.0,
                20.0,
                0.5);
        mRBGainSp = new MyConfigSpinner(rbGainModel);

        final SpinnerModel rbDenomModel = new SpinnerNumberModel(
                100,
                10,
                1000,
                10);
        mRBDenomSp = new MyConfigSpinner(rbDenomModel);

        // Choosing technique
        mTechniquesCoBx = new JComboBox(TECHNIQUE.values());
        mTechniquesCoBx.setEditable(false);
        mTechniquesCoBx.setSelectedItem(TECHNIQUE.DRAG);
//        mTechniquesCoBx.addItemListener(this);
        mTechniquesCoBx.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TECHNIQUE tech = (TECHNIQUE) mTechniquesCoBx.getSelectedItem();
                CardLayout cl = (CardLayout)(mCardsPanel.getLayout());
                cl.show(mCardsPanel, tech.toString());
                Experiment.setActiveTechnique(tech);
            }
        });

        // Drag card
        final JPanel dragCard = new JPanel();
        dragCard.setLayout(new BoxLayout(dragCard, BoxLayout.LINE_AXIS));
        dragCard.add(Box.createRigidArea(new Dimension(20, 0)));
        dragCard.add(new JLabel("Sensitivity: "));
        dragCard.add(mDragSensitivitySp);
        dragCard.add(Box.createRigidArea(new Dimension(20, 0)));
        dragCard.add(new JLabel("Gain: "));
        dragCard.add(mDragGainSp);
        dragCard.add(Box.createRigidArea(new Dimension(300, 0)));

        // RB card
        final JPanel rbCard = new JPanel();
        rbCard.setLayout(new BoxLayout(rbCard, BoxLayout.LINE_AXIS));
        rbCard.add(Box.createRigidArea(new Dimension(20, 0)));
        rbCard.add(new JLabel("Sensitivity: "));
        rbCard.add(mRBSensitivitySp);
        rbCard.add(Box.createRigidArea(new Dimension(20, 0)));
        rbCard.add(new JLabel("Gain: "));
        rbCard.add(mRBGainSp);
        rbCard.add(Box.createRigidArea(new Dimension(20, 0)));
        rbCard.add(new JLabel("Denom: "));
        rbCard.add(mRBDenomSp);
        rbCard.add(Box.createRigidArea(new Dimension(200, 0)));

        // Mouse card
        final JPanel mouseCard = new JPanel();

        //Create the panel that contains the "cards".
        mCardsPanel = new JPanel(new CardLayout());
        mCardsPanel.add(dragCard, TECHNIQUE.DRAG.toString());
        mCardsPanel.add(rbCard, TECHNIQUE.RATE_BASED.toString());
        mCardsPanel.add(mouseCard, TECHNIQUE.MOUSE.toString());

        // Add components
        add(mTechniquesCoBx);
        add(mCardsPanel);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        CardLayout cl = (CardLayout)(mCardsPanel.getLayout());
        Logs.d(NAME, e.getItem().toString(), 0);
        cl.show(mCardsPanel, e.getItem().toString());
        Experiment.setActiveTechnique(TECHNIQUE.valueOf(e.getItem().toString()));
    }

    public void nextTechnique() {
        final CardLayout cl = (CardLayout)(mCardsPanel.getLayout());
        TECHNIQUE nextTechnique = TECHNIQUE.DRAG;
        switch (Experiment.getActiveTechnique()) {
            case DRAG -> nextTechnique = TECHNIQUE.RATE_BASED;
            case RATE_BASED -> nextTechnique = TECHNIQUE.MOUSE;
            case MOUSE -> nextTechnique = TECHNIQUE.DRAG;
        }

        mTechniquesCoBx.setSelectedItem(nextTechnique);
        cl.show(mCardsPanel, nextTechnique.toString());
        Experiment.setActiveTechnique(nextTechnique);
    }

    public void adjustSensitivity(boolean inc) {
        int newValue = 1;
        switch (Experiment.getActiveTechnique()) {
            case DRAG -> newValue = (int) mDragSensitivitySp.change(inc);
            case RATE_BASED -> newValue = (int) mRBSensitivitySp.change(inc);
        }
        Experiment.setSensitivity(newValue);
    }

    public void adjustGain(boolean inc) {
        double newValue = 1;
        switch (Experiment.getActiveTechnique()) {
            case DRAG -> newValue = (double) mDragGainSp.change(inc);
            case RATE_BASED -> newValue = (double) mRBGainSp.change(inc);
        }
        Experiment.setGain(newValue);
    }

    public void adjustDenom(boolean inc) {
        int newValue = 1;
        switch (Experiment.getActiveTechnique()) {
            case RATE_BASED -> newValue = (int) mRBDenomSp.change(inc);
        }
        Experiment.setDenom(newValue);
    }

//    public void adjustGain(boolean inc) {
//        int newValue = 1;
//        switch (Experiment.getActiveTechnique()) {
//            case DRAG -> {
//                final Object nextValue = mDragGainSp.getNextValue();
//                final Object prevValue = mDragGainSp.getPreviousValue();
//
//                if (inc) newValue = (int) nextValue;
//                else if (prevValue != null) newValue = (int) prevValue;
//
//                mDragSensitivitySp.setValue(newValue);
//            }
//
//            case RATE_BASED -> {
//                final Object nextValue = mRBSensitivitySp.getNextValue();
//                final Object prevValue = mRBSensitivitySp.getPreviousValue();
//
//                if (inc) newValue = (int) nextValue;
//                else if (prevValue != null) newValue = (int) prevValue;
//
//                mRBSensitivitySp.setValue(newValue);
//            }
//        }
//
//        Experiment.setSensitivity(newValue);
//    }
//
//    public void adjustSensitivity(boolean inc) {
//        int newValue = 1;
//        switch (Experiment.getActiveTechnique()) {
//            case DRAG -> {
//                final Object nextValue = mDragSensitivitySp.getNextValue();
//                final Object prevValue = mDragSensitivitySp.getPreviousValue();
//
//                if (inc) newValue = (int) nextValue;
//                else if (prevValue != null) newValue = (int) prevValue;
//
//                mDragSensitivitySp.setValue(newValue);
//            }
//
//            case RATE_BASED -> {
//                final Object nextValue = mRBSensitivitySp.getNextValue();
//                final Object prevValue = mRBSensitivitySp.getPreviousValue();
//
//                if (inc) newValue = (int) nextValue;
//                else if (prevValue != null) newValue = (int) prevValue;
//
//                mRBSensitivitySp.setValue(newValue);
//            }
//        }
//
//        Experiment.setSensitivity(newValue);
//    }

    //--------------------------------------------------------------------------------
    private class MyConfigSpinner extends JSpinner {

        public MyConfigSpinner(SpinnerModel model) {
            super(model);
        }

        public void inc() {
            setValue(getNextValue());
        }

        public void dec() {
            if (getPreviousValue() != null) setValue(getPreviousValue());
        }

        public Object change(boolean inc) {
            if (inc) inc();
            else dec();

            return getValue();
        }
    }

}
