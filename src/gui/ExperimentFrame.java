package gui;

import control.Logger;
import control.Server;
import experiment.Block;
import experiment.Experiment;
import experiment.ExpNode;
import tools.Logs;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static tools.Consts.*;
import static experiment.Experiment.*;

public class ExperimentFrame extends JFrame {
    private final static String NAME = "ExperimentFrame/";

    private static ExperimentFrame self; // Singelton instance

    //----------------------------------------------------
    private int mPId = 129;
    private TECHNIQUE mTechnique = TECHNIQUE.MOUSE;
    //----------------------------------------------------

    // Screen params
    private Rectangle scrBound;
    private int scrW, scrH;
    private int frW, frH;

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_ENTER;

    // Elements
    private BlockPanel mBlockPanel;
    private static ExperimentPanel sExperimentPanel;
//    private JPanel mContainerPanel;

    // Experiment
    private Experiment mExperiment;
    private ExpNode mExpTree;
    private int mTechInd;
    private int mTaskInd; // Starts from 0

    // Logging
    private Logger.GeneralInfo mGenInfo;
    private Logger.TimeInfo mTimeInfo;
    private long mExpStTime;

    // Actions ------------------------------------------------------------------------------------
    private final Action START_BLOCK = new AbstractAction() {
        private String TAG = ExperimentFrame.NAME + "START_BLOCK";

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    };

    private final Action END_BRIEF_BREAK = new AbstractAction() {
        private String TAG = ExperimentFrame.NAME + "END_BRIEF_BREAK";

        @Override
        public void actionPerformed(ActionEvent e) {
            mGenInfo.blockNum++;
        }
    };

    // Methods -------------------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ExperimentFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDisplayConfig();
        setBackground(Color.WHITE);
        setupKeyStrokes();

//        mContainerPanel = new JPanel();
//        mContainerPanel.setLayout(null);
    }

    /**
     * Get the instance
     * @return Instance
     */
    public static ExperimentFrame get() {
        if (self == null) self = new ExperimentFrame();
        return self;
    }

    /**
     * Set the active technique
     * @param tech TECHNIQUE
     */
    public void setActiveTechnique(TECHNIQUE tech) {
        mTechnique = tech;
    }

    /**
     * Get the active technique
     * @return TECHNIQUE
     */
    public Experiment.TECHNIQUE getActvieTechnique() {
        return mTechnique;
    }

    public void start(int pid, TECHNIQUE tech) {
        final String TAG = NAME + "start";

        // Setup the experiment
        mExperiment = new Experiment(mPId);
        mExpTree = mExperiment.getPcTree();

        // Init logs
        Logger.get().logParticipant(mPId);
        mGenInfo = new Logger.GeneralInfo();
        mGenInfo.tech = tech;
        mTimeInfo = new Logger.TimeInfo();

        // Show the start panel
        StartPanel stPanel = new StartPanel(String.valueOf(mPId), mGenInfo.tech.toString());
        add(stPanel);
        setVisible(true);

        if (tech.equals(TECHNIQUE.FLICK)) Server.get().openConnection();
    }

    /**
     * Start the experiment (called from StartPanel)
     */
    public void startExperiment() {
        final String TAG = NAME + "startExperiment";
        Logs.d(TAG, "Starting the experiment");

        mExpStTime = Utils.nowInMillis();
        mTaskInd = 0;

        getContentPane().removeAll();
        showTaskStartPanel();
    }

    /**
     * Show the panel at the start of each task
     */
    public void showTaskStartPanel() {
        final String TAG = NAME + "showTaskStartPanel";

        JPanel panel = new JPanel();
        panel.setLayout(null);

        final String techStr = mGenInfo.tech.toString();
        final String taskStr = mExpTree.getChild(mTaskInd).getData().toString();

        final int xPos = 720;
        final int yPos = 200;
        JLabel techLabel = new JLabel("", JLabel.CENTER);
        techLabel.setText("Technique: " + techStr);
        techLabel.setFont(new Font("Sans", Font.BOLD, 30));
        techLabel.setForeground(COLORS.DARK_BLUE);
        techLabel.setBounds(xPos, yPos, 500, 200);
        panel.add(techLabel, 0);

        JLabel taskLabel = new JLabel("", JLabel.CENTER);
        taskLabel.setText("Task: " + taskStr);
        taskLabel.setFont(new Font("Sans", Font.BOLD, 30));
        taskLabel.setForeground(COLORS.GREEN);
        taskLabel.setBounds(xPos + 450, yPos, 800, 200);
        panel.add(taskLabel, 0);

        JLabel instructLabel = new JLabel("", JLabel.CENTER);
        instructLabel.setText("When ready, press SPACE to start");
        instructLabel.setFont(new Font("Sans", Font.BOLD, 50));
        instructLabel.setBounds(xPos + 20, yPos + 200, 1000, 400);
        panel.add(instructLabel, 0);

        panel.getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
        panel.getActionMap().put(KeyEvent.VK_SPACE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Logs.d(TAG, "Space pressed");
                mGenInfo.blockNum = 1;
                showBlock();
            }
        });

        add(panel);
        panel.requestFocusInWindow();
        revalidate();
    }

    /**
     * Create a BlockPanel and show a block
     */
    private void showBlock() {
        final String TAG = NAME + "showBlock";

        final Dimension panelDim = getContentPane().getSize();

        final int nBlocks = mExpTree.getChild(mTaskInd).getNChildren();
        final Block block = (Block) mExpTree.getChild(mTaskInd)
                .getChild(mGenInfo.blockNum - 1)
                .getData();
        mBlockPanel = new BlockPanel(panelDim, nBlocks).setData(block, mGenInfo, mTimeInfo);

        getContentPane().removeAll();
        add(mBlockPanel);
        mBlockPanel.requestFocusInWindow();
        repaint();
    }

    /**
     * Called from the BlockPanel to signal the end of the block
     * @param blockTimeInfo InstantInfo
     */
    public void blockFinished(Logger.TimeInfo blockTimeInfo) {
        final String TAG = NAME + "blockFinished";

        // Log the time so far
        Logger.get().logTimeInfo(mGenInfo, blockTimeInfo);

        // What next?
        if (mGenInfo.blockNum < mExpTree.getChild(mTaskInd).getNChildren()) { // More blocks
            showBriefBreak();
        } else {
            SOUNDS.play(STRINGS.TASK_END);
            showTaskEnd();
        }

    }

    /**
     * Show breaks between blocks
     */
    private void showBriefBreak() {
        getContentPane().removeAll();

        final JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.setColor(COLORS.DARK_GREEN);
                g.drawRect(1000, 250, 570, 200);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createRigidArea(new Dimension(0, 300)));

        // Top label
        JLabel breakLbl = new JLabel("You can now take a break!");
        breakLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        breakLbl.setFont(new Font("Sans", Font.BOLD, 35));
        breakLbl.setForeground(COLORS.DARK_GREEN);
        panel.add(breakLbl);

        panel.add(Box.createRigidArea(new Dimension(0, 300)));

        // Bottom label
        JLabel orderLbl = new JLabel("To coninue, press ENTER");
        orderLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        orderLbl.setFont(new Font("Sans", Font.BOLD, 37));
        orderLbl.setForeground(Color.DARK_GRAY);

        panel.add(orderLbl);

        // Pass by pressing ENTER
        panel.getInputMap().put(KS_ENTER, KeyEvent.VK_ENTER);
        panel.getActionMap().put(KeyEvent.VK_ENTER, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Logs.d(ExperimentFrame.NAME, "Block Num: " + mGenInfo.blockNum);
                mGenInfo.blockNum++;
                showBlock();
            }
        });

        add(panel);
        panel.requestFocusInWindow();
        revalidate();
        repaint();
    }

    /**
     * Show the task end dialog
     */
    private void showTaskEnd() {
        getContentPane().removeAll();
        repaint();

        JDialog taskBreakDialog = new JDialog((JFrame)null, "Task ended", true);
        taskBreakDialog.setSize(1000, 500);
        taskBreakDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(800, 500);
        panel.setBackground(Color.decode("#90caf9"));
        panel.add(Box.createRigidArea(new Dimension(800, 100)));

        // Show label
        final String lblStr = "Thank you! " + mExpTree.getChild(mTaskInd).getData() + " task is finished.";
        JLabel label = new JLabel(lblStr);
        label.setFont(new Font("Sans", Font.BOLD, 30));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(0, 200)));

        // The button depends on whether tasks are finished or not
        JButton button = new JButton();
        button.setFont(new Font("Sans", Font.PLAIN, 25));
        button.setMaximumSize(new Dimension(400, 70));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusable(false);

        if (mTaskInd < mExpTree.getNChildren() - 1) { // There are more tasks to show
            button.setText("Start " + mExpTree.getChild(mTaskInd + 1).getData() + " task");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    taskBreakDialog.dispose();
                    mTaskInd++;
                    showTaskStartPanel();
                }
            });

        } else {
            button.setText("Close the program");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
        }

        panel.add(button);

        taskBreakDialog.add(panel);
        taskBreakDialog.setUndecorated(true);
        taskBreakDialog.setVisible(true);
    }

    /**
     * Show the panel for the end of the program
     */
    private void showProgramEnd() {
//        final JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//
//        // Show the label
////        final int xPos = 750;
////        final int yPos = 400;
//        JLabel titleLbl = new JLabel("", JLabel.CENTER);
//        titleLbl.setFont(new Font("Sans", Font.BOLD, 35));
//        briefBreakLabel.setBounds(xPos, yPos, 1000, 400);
//        panel.add(briefBreakLabel);
//
//        panel.getInputMap().put(KS_ENTER, KeyEvent.VK_ENTER);
//        panel.getActionMap().put(KeyEvent.VK_ENTER, new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Logs.d(ExperimentFrame.NAME, "Block Num: " + mGenInfo.blockNum);
//                mGenInfo.blockNum++;
//                showBlock();
//            }
//        });
//
//        add(panel);
//        panel.requestFocusInWindow();
//        revalidate();
//        repaint();
    }

    /**
     * Show a dialog
     * @param dialog Dialog
     */
    public void showDialog(JDialog dialog) {
        dialog.pack();
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        int frW = dialog.getSize().width;
        int frH = dialog.getSize().height;

        dialog.setLocation(
                ((scrW / 2) - (frW / 2)) + scrBound.x,
                ((scrH / 2) - (frH / 2)) + scrBound.y
        );
        dialog.setVisible(true);
    }

    /**
     * Show a simple message
     * @param mssg Message text
     */
    public void showMessage(String mssg) {
        JOptionPane.showMessageDialog(this, mssg);
    }

    /**
     * Set the config for showing panels
     */
    private void setDisplayConfig() {
        setExtendedState(JFrame.MAXIMIZED_BOTH); // maximized frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close on exit

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();

        scrBound = gd[1].getDefaultConfiguration().getBounds();
        scrW = scrBound.width;
        scrH = scrBound.height;

        frW = getSize().width;
        frH = getSize().height;

        setLocation(
                ((scrW / 2) - (frW / 2)) + scrBound.x,
                ((scrH / 2) - (frH / 2)) + scrBound.y
        );

        // Set mm size in pixels
//        Logs.info("TAG", Toolkit.getDefaultToolkit().getScreenResolution());
    }

    public static void scroll(int vtScrollAmt, int hzScrollAmt) {

    }

    /**
     * Set up the keystrokes
     */
    private void setupKeyStrokes() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
    }

}
