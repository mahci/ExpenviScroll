package control;

import tools.Logs;

import java.awt.*;

/**
 * Class responsible for getting the data from the Server and perform the actions
 */
public class Controller {
    private final String cName = "Controller -- "; // class tag
    //----------------------------------------------------------------

    public static Controller instance; // Singleton

    private Robot robot;

    /**
     * Get the single instance
     * @return Singleton instnace
     */
    public static Controller self() {
        if (instance == null) instance = new Controller();
        return instance;
    }

    /**
     * Contrsuctor
     */
    private Controller() {
        String TAG = cName;

        try {
            robot = new Robot();
        } catch (AWTException e) {
            Logs.error(TAG, "Robot couldn't be initialized!");
            System.exit(1);
        }
    }

    /**
     * Do the scrolling!
     * @return Result (0: success, 1: error)
     */
    public int scroll(int scrollAmt) {
        String TAG = cName + "scroll";

        // problem with the robot
        if (robot == null) {
            Logs.error(TAG, "Can't load the robot!");
            return 1;
        }

        robot.mouseWheel(scrollAmt);

        return 0;
    }



}
