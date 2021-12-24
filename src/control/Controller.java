package control;

import experiment.Experiment;
import gui.MainFrame;
import tools.*;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static tools.Consts.STRINGS.*;
import static experiment.Experiment.TECHNIQUE.*;

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

        private final int vtScrollAmt; // Movement delta / 1 ms
        private final int hzScrollAmt; // Movement delta / 1 ms

        private ConstantScrollRunnable(int vtScrollAmt, int hzScrollAmt) {
            this.vtScrollAmt = vtScrollAmt;
            this.hzScrollAmt = hzScrollAmt;
        }

        @Override
        public void run() {
            String TAG = NAME + "ConstantScrollRunnable";
            try {
                Logs.d(TAG, toScroll);
                while (toScroll) {
                    Logs.d(TAG, "Scrolling with", vtScrollAmt, hzScrollAmt);
                    MainFrame.scroll(vtScrollAmt, hzScrollAmt);
                    Thread.currentThread().sleep(1); // 1 min = 60*1000, 1 sec = 1000
                }
            } catch (InterruptedException e) {
                Logs.d(TAG, "Interrupted!", 0);
                // Pls continue!
                if (toScroll) {
                    Thread.currentThread().resume();
                } else {
                    Thread.currentThread().interrupt();
                }
//                toScroll = false;

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
        Logs.d(TAG, "Received", memo.toString());

        final int vtScrollAmt = Utils.mm2px(memo.getValue1Double());
        final int hzScrollAmt = Utils.mm2px(memo.getValue2Double());

        final Experiment.TECHNIQUE technique = Experiment.TECHNIQUE.valueOf(memo.getMode());
        switch (technique) {
            case DRAG -> {
                MainFrame.scroll(vtScrollAmt, hzScrollAmt);
            }
            case RATE_BASED -> {
                // Stop prev. scrolling if new command has come
                stopScroll();

                // New scrolling
                if (!memo.isStopMemo()) {
                    toScroll = true;
                    scrollThread = new Thread(new ConstantScrollRunnable(vtScrollAmt, hzScrollAmt));
                    scrollThread.start();
                }
            }
            case FLICK -> {
                MainFrame.scroll(vtScrollAmt, hzScrollAmt);
            }
        }

    }

    public void stopScroll() {
        final String TAG = NAME + "stopScroll";
        Logs.d(TAG, "Stop Scroll", "");
        if (scrollThread != null && !scrollThread.isInterrupted()) {
            toScroll = false;
            scrollThread.interrupt();
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
