package gui;

import control.Logger;
import control.Server;
import data.Memo;
import experiment.Block;
import data.Consts;
import tools.Logs;
import data.Pair;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import static data.Consts.*;
import static experiment.Experiment.*;

public class ExperimentFrame extends JFrame {
    private final static String NAME = "ExperimentFrame/";

    private static ExperimentFrame self; // Singelton instance

    //----------------------------------------------------
    private int mPId;
//    private TECHNIQUE mTechnique = TECHNIQUE.MOUSE;
    //----------------------------------------------------

    // Screen params
    private Rectangle mScrBound;
    private int mScrW, mScrH;
    private int mFrW, mFrH;

    // Keys
    private KeyStroke KS_SPACE;
    private KeyStroke KS_ENTER;

    // Elements
    private BlockPanel mBlockPanel;
    private static ExperimentPanel sExperimentPanel;
//    private JPanel mContainerPanel;

    // Part
    private Part mPart;
    private int mPartId;
    private int mTechInd;
    private int mTaskInd; // Starts from 0

    // Logging
    private Logger.GeneralInfo mGenInfo;
    private Logger.TimeInfo mTimeInfo;
    private long mPartStTime;

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
     * Set the part to show
     * @param pt Part
     * @return ExperimentFrame modified instance
     */
    public ExperimentFrame setPart(Part pt) {
        mPart = pt;

        // Init logs
        mTimeInfo = new Logger.TimeInfo();
        mGenInfo = new Logger.GeneralInfo();
        mGenInfo.tech = pt.getTech();

        return this;
    }

    /**
     * Show intro
     */
    public void showIntro(int pid, int sessionId, int partId) {
        final String TAG = NAME + "start";

        mPId = pid;
        mPartId = partId;
        mGenInfo.session = sessionId;
        mGenInfo.part = partId;

        // Show the Intro panel
        final ActionListener showPartStartPanel = e -> showPartStartPanel();
        IntroPanel stPanel = new IntroPanel(mPId, sessionId, partId, showPartStartPanel);
        add(stPanel);
        setVisible(true);

        // If Moose, start Server and send the init experiment info
        if (mGenInfo.tech.equals(TECHNIQUE.FLICK)) {
            Server.get().connectAndSyncExp();
        }
    }

    /**
     * Start the experiment (called from IntroPanel)
     */
    public void showPartStartPanel() {
        final String TAG = NAME + "startPart";

        mGenInfo.blockNum = 1;
        final String techStr = mPart.getTech().toShowString();
        final String taskStr = mPart.getTask().toShowString();

        getContentPane().removeAll();

        // Show the Start panel
        final AbstractAction startFirstBlock = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBlock();
            }
        };
        PartStartPanel startPanel = new PartStartPanel(techStr, taskStr, startFirstBlock);
        add(startPanel);
        startPanel.requestFocusInWindow();
        revalidate();
    }

    /**
     * Create a BlockPanel and show a block
     */
    public void showBlock() {
        final String TAG = NAME + "showBlock";
        Logs.d(TAG, mGenInfo.blockNum);
        if (mGenInfo.blockNum == 1) mPartStTime = Utils.nowInMillis(); // Start timing

        final Dimension panelDim = getContentPane().getSize();
        final int nBlocks = mPart.nBlocks();
        final Block block = mPart.getBlock(mGenInfo.blockNum - 1);

        getContentPane().removeAll();

        mBlockPanel = new BlockPanel(panelDim, nBlocks).setData(block, mGenInfo, mTimeInfo);
        add(mBlockPanel);
        mBlockPanel.requestFocusInWindow();
        mBlockPanel.start();
        repaint();
    }

    /**
     * Called from the BlockPanel to signal the end of the block
     * @param timeInfo InstantInfo gotten from the block
     */
    public void blockFinished(Logger.TimeInfo timeInfo) {
        final String TAG = NAME + "blockFinished";

        // What next?
        if (mGenInfo.blockNum < mPart.nBlocks()) { // More blocks
            Logger.get().logTimeInfo(mGenInfo, timeInfo); // Log block disp time

            showBriefBreak();
        } else { // Part is finished
            SOUNDS.play(STRINGS.TASK_END);

            // Log block and part disp time
            timeInfo.partDispTime = (Utils.nowInMillis() - mPartStTime) / 1000;
            Logger.get().logTimeInfo(mGenInfo, timeInfo);

            // Tell the Moose to end
            Server.get().send(new Memo(STRINGS.LOG, STRINGS.END, "", ""));

            showPartEnd();
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
                mGenInfo.blockNum++;
                Logs.d(ExperimentFrame.NAME, "Block Num: " + mGenInfo.blockNum);
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
    private void showPartEnd() {
        getContentPane().removeAll();
        repaint();

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(Box.createRigidArea(new Dimension(0, 350)));

        // Create the panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(mScrW, 600));
        panel.setBackground(COLORS.GREEN_A200);

        panel.add(Box.createRigidArea(new Dimension(0, 250)));

        final String lblStr = "Thank you! Part " + mPartId + " is now finished.";
        JLabel label = new JLabel(lblStr);
        label.setFont(new Font("Sans", Font.BOLD, 45));
        label.setForeground(COLORS.GRAY_900);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);

        // Close after 3 s
        final TimerTask closingTask = new TimerTask() {
            public void run() {
                System.exit(0);
            }
        };
        Timer closingTimer = new Timer();
        long delay = 5 * 1000; // ms
        closingTimer.schedule(closingTask, delay);

        getContentPane().add(panel);
        panel.requestFocusInWindow();
        revalidate();
        repaint();
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
                ((mScrW / 2) - (frW / 2)) + mScrBound.x,
                ((mScrH / 2) - (frH / 2)) + mScrBound.y
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

        mScrBound = gd[1].getDefaultConfiguration().getBounds();
        mScrW = mScrBound.width;
        mScrH = mScrBound.height;

        mFrW = getSize().width;
        mFrH = getSize().height;

        setLocation(
                ((mScrW / 2) - (mFrW / 2)) + mScrBound.x,
                ((mScrH / 2) - (mFrH / 2)) + mScrBound.y
        );
    }

    /**
     * Send the scroll action to the BlockPanel
     * @param vtScrollAmt Vertical scroll amount
     * @param hzScrollAmt Horizontal scroll amount
     */
    public void scroll(int vtScrollAmt, int hzScrollAmt) {
        if (mBlockPanel != null) mBlockPanel.scroll(vtScrollAmt, hzScrollAmt);
    }

    /**
     * Set up the keystrokes
     */
    private void setupKeyStrokes() {
        KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);
        KS_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
    }

    /**
     * Get the center of the screen
     * @return Pair (x,y)
     */
    public Pair getScreenCenter() {
        return new Pair(mScrW / 2, mScrH / 2);
    }

    // Panels --------------------------------------------------------------------------------------------
    private class IntroPanel extends JPanel {

        public IntroPanel(int pId, int sessionId, int partId, ActionListener btnAction) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createRigidArea(new Dimension(0, 200)));

            // PId
            JLabel pcIdLabel = new JLabel("Participant Id: " + pId, SwingConstants.CENTER);
            pcIdLabel.setAlignmentX(CENTER_ALIGNMENT);
            pcIdLabel.setFont(new Font("Sans", Font.PLAIN, 25));
            pcIdLabel.setPreferredSize(new Dimension(400, 50));
            add(pcIdLabel);

            add(Box.createRigidArea(new Dimension(0, 200)));

            // Session Part
            JPanel sessPartPanel = new JPanel();
            sessPartPanel.setLayout(new BoxLayout(sessPartPanel, BoxLayout.X_AXIS));
            sessPartPanel.setAlignmentX(CENTER_ALIGNMENT);
            sessPartPanel.setMaximumSize(new Dimension(600, 60));
            sessPartPanel.add(Box.createHorizontalGlue());

            JLabel sessLabel = new JLabel("Session: " + sessionId, SwingConstants.CENTER);
            sessLabel.setFont(new Font("Sans", Font.PLAIN, 25));
            sessLabel.setPreferredSize(new Dimension(150, 0));
            sessPartPanel.add(sessLabel);

            sessPartPanel.add(Box.createRigidArea(new Dimension(60, 0)));

            JLabel partLabel = new JLabel("Part: " + partId, SwingConstants.CENTER);
            partLabel.setFont(new Font("Sans", Font.PLAIN, 25));
            partLabel.setPreferredSize(new Dimension(150, 0));
            sessPartPanel.add(partLabel);

            sessPartPanel.add(Box.createHorizontalGlue());
            add(sessPartPanel);

            add(Box.createRigidArea(new Dimension(0, 30)));

            JButton startBtn = new JButton("Start");
            startBtn.setAlignmentX(CENTER_ALIGNMENT);
            startBtn.setFont(new Font("Sans", Font.BOLD, 25));
            startBtn.setMaximumSize(new Dimension(400, 70));
            add(startBtn);

            startBtn.addActionListener(btnAction);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            final Graphics2D g2d = (Graphics2D) g;
            final Pair scrCenter = ExperimentFrame.get().getScreenCenter();

            final int y = scrCenter.second - 470;
            g2d.setColor(Consts.COLORS.BLUE_800);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(scrCenter.first - 200, y, scrCenter.first + 200, y);
        }
    }

    private class PartStartPanel extends JPanel {
        private KeyStroke KS_SPACE;

        public PartStartPanel(String device, String task, AbstractAction spaceAction) {
            KS_SPACE = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createRigidArea(new Dimension(0, 600)));

            // Instruction label
            JLabel instructLabel = new JLabel("When ready, press SPACE to start", JLabel.CENTER);
            instructLabel.setAlignmentX(CENTER_ALIGNMENT);
            instructLabel.setFont(new Font("Sans", Font.BOLD, 50));
            instructLabel.setForeground(COLORS.GRAY_900);
            add(instructLabel);

            add(Box.createRigidArea(new Dimension(0, 200)));

            // Device-task panel
            JPanel deviceTaskPnl = new JPanel();
            deviceTaskPnl.setLayout(new BoxLayout(deviceTaskPnl, BoxLayout.X_AXIS));
            deviceTaskPnl.setAlignmentX(CENTER_ALIGNMENT);
            deviceTaskPnl.setMaximumSize(new Dimension(500, 60));
            deviceTaskPnl.add(Box.createHorizontalGlue());

            JLabel deviceLbl = new JLabel(device, SwingConstants.CENTER);
            deviceLbl.setFont(new Font("Sans", Font.BOLD, 30));
            deviceLbl.setPreferredSize(new Dimension(200, 0));
            if (device == "MOUSE") deviceLbl.setForeground(Consts.COLORS.DARK_BLUE);
            else deviceLbl.setForeground(Consts.COLORS.DARK_RED);
            deviceTaskPnl.add(deviceLbl);

            deviceTaskPnl.add(Box.createRigidArea(new Dimension(20, 0)));

            JLabel dashLbl = new JLabel("-", SwingConstants.CENTER);
            dashLbl.setFont(new Font("Sans", Font.BOLD, 50));
            dashLbl.setForeground(COLORS.GRAY_700);
            dashLbl.setPreferredSize(new Dimension(50, 0));
            deviceTaskPnl.add(dashLbl);

            deviceTaskPnl.add(Box.createRigidArea(new Dimension(20, 0)));

            JLabel taskLbl = new JLabel(task, SwingConstants.CENTER);
            taskLbl.setFont(new Font("Sans", Font.BOLD, 30));
            taskLbl.setForeground(COLORS.DARK_GREEN);
            taskLbl.setPreferredSize(new Dimension(200, 0));
            deviceTaskPnl.add(taskLbl);

            deviceTaskPnl.add(Box.createHorizontalGlue());

            add(deviceTaskPnl);

            // Set action
            getInputMap().put(KS_SPACE, KeyEvent.VK_SPACE);
            getActionMap().put(KeyEvent.VK_SPACE, spaceAction);

        }
    }


}
