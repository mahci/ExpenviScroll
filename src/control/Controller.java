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

    //----------------------------------------------------------------

    // For scrolling constantly!
    private class ConstantScrollRunnable implements Runnable {

        private final int delta; // Movement delta / 1 ms

        private ConstantScrollRunnable(int dlt) {
            delta = dlt;
        }

        @Override
        public void run() {
            String TAG = NAME + "ConstantScrollRunnable";
            try {
                while (!scrollThread.isInterrupted()) {
//                    Logs.infoAll(TAG, "Scrolling with " + delta);
//                    robot.mouseWheel(delta);
//                    MainFrame.scroll(delta / 10);
                    Thread.sleep(1); // 1 min = 60*1000, 1 sec = 1000
                }
            } catch (InterruptedException e) {
                // We need this because when a sleep the interrupt from outside throws an exception
                Thread.currentThread().interrupt();
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
        Logs.infoAll(TAG, memo.toString());

        if (memo.getValueXInt() == 0 & scrollThread != null) { // STOP is received
            Logs.infoAll(TAG, "Stopped!");
            scrollThread.interrupt();
        } else { // Scroll
            // Convert scroll amount to px (in both directions)
            Pair<Integer, Integer> scrollAmt2D = Pair.of(
                    Utils.mm2px(memo.getValueXInt()),
                    Utils.mm2px(memo.getValueYInt()));

            switch (memo.getMode()) {
            case DRAG -> {
                MainFrame.scroll(scrollAmt2D);
            }
            case RB -> {

            }
            }
        }

    }

    /**
     * Drag
     * @param delta Amount to scroll
     * @return Result (0: success, 1: error)
     */
    private int scrollDrag(int delta) {
        String TAG = NAME + "scroll";

        // problem with the robot
        if (robot == null) {
            Logs.errorAll(TAG, "Can't load the robot!");
            return 1;
        }

        Logs.infoAll(TAG, "Scrolling: " + delta);
//        robot.mouseWheel(delta);
//        MainFrame.scroll(delta);;

        return 0;
    }

    /**
     * Rate-based
     * @param delta Scroll amount (notches/ 1 ms)
     * @return Result (0: success, 1: error)
     */
    private int scrollRateBased(int delta) {
        String TAG = NAME + "scroll";

        // problem with the robot
        if (robot == null) {
            Logs.errorAll(TAG, "Can't load the robot!");
            return 1;
        }

        // Stop prev. scrolling if new command has come
        if (scrollThread != null && !scrollThread.isInterrupted()) {
            scrollThread.interrupt();
        }

        scrollThread = new Thread(new ConstantScrollRunnable(delta));
        scrollThread.start();

        return 0;
    }




}
