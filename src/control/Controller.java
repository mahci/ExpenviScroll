package control;

import gui.MainFrame;
import tools.*;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static tools.Consts.STRINGS.*;

/**
 * Class responsible for getting the data from the Server and perform the actions
 */
public class Controller {
    private final String NAME = "Controller/"; // class tag
    //----------------------------------------------------------------

    public static Controller instance; // Singleton

    private Thread scrollThread;
    private Robot robot;

    private ExecutorService executor;
    private ThreadGroup scrollThreadGroup;
    private boolean toScroll = false;

    //----------------------------------------------------------------

    // For scrolling constantly!
    private class ConstantScrollRunnable implements Runnable {

        private final double vtScrollMM; // Movement delta / 1 ms
        private final double hzScrollMM; // Movement delta / 1 ms

        private ConstantScrollRunnable(double vtScrollMM, double hzScrollMM) {
            this.vtScrollMM = vtScrollMM;
            this.hzScrollMM = hzScrollMM;
        }

        @Override
        public void run() {
            String TAG = NAME + "ConstantScrollRunnable";
            try {
                while (toScroll) {
                    Logs.d(TAG, "Scrolling with", vtScrollMM);
                    MainFrame.scroll(vtScrollMM, hzScrollMM);
                    Thread.currentThread().sleep(1); // 1 min = 60*1000, 1 sec = 1000
                }
            } catch (InterruptedException e) {
                // We need this because when a sleep the interrupt from outside throws an exception
                Thread.currentThread().interrupt();
                toScroll = false;
            }
        }
    }



    //----------------------------------------------------------------

    /**
     * Get the single instance
     * @return Singleton instnace
     */
    public static Controller get() {
        if (instance == null) instance = new Controller();
        return instance;
    }

    /**
     * Contrsuctor
     */
    private Controller() {
        String TAG = NAME;

        try {
            robot = new Robot();
            executor = Executors.newCachedThreadPool(); // Init executerService for running threads
            scrollThreadGroup = new ThreadGroup("Scrolls");

        } catch (AWTException e) {
            Logs.error(TAG, "Robot couldn't be initialized!");
            System.exit(1);
        }
    }

    /**
     * Perform the action (can be scrolling or stopping a scroll)
     * @param memo Memo containing info
     */
    public void perform(Memo memo) {
        String TAG = NAME + "scroll";
        Logs.d(TAG, "Received", memo.toString(), memo.getValue1());

        switch (memo.getMode()) {
        case DRAG -> {
            double vtScrollMM = memo.getValue1Double();
            double hzScrollMM = memo.getValue2Double();

            MainFrame.scroll(vtScrollMM, hzScrollMM);
        }
        case RB -> {
            // Stop prev. scrolling if new command has come
            if (scrollThreadGroup != null) {
                Logs.d(TAG, "RB", "Interrupted!");
//                scrollThreadGroup.stop();
                toScroll = false;
//                MainFrame.stopScroll();
            }

            if (memo.getValue1().equals(STOP)) {
//                MainFrame.stopScroll();
                scrollThreadGroup.stop();
            } else {
                Logs.d(TAG, "RB", memo.getValue1());
                double vtScrollMM = memo.getValue1Double();
                double hzScrollMM = memo.getValue2Double();

                toScroll = true;
                scrollThread = new Thread(scrollThreadGroup, new ConstantScrollRunnable(vtScrollMM, hzScrollMM));
                scrollThread.start();
            }

        }
        }

    }

    public void testScroll(int vtAmt) {
        toScroll = true;
        scrollThread = new Thread(new ConstantScrollRunnable(vtAmt, 0));
        scrollThread.start();
    }

    public void testStopScroll() {

        if (scrollThread != null && !scrollThread.isInterrupted()) {
            toScroll = false;
            scrollThread.stop();
        }

//        scrollThread.interrupt();
    }



}
