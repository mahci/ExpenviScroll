package control;

import tools.Logs;

import java.awt.*;

/***
 * Class responsible for getting the data from the Server and perform the actions
 */
public class Controller {
    private final String cTag = "Controller -- "; // class tag
    private String mTag; // method tag
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
        mTag = cTag + "Controller";

        try {
            robot = new Robot();
        } catch (AWTException e) {
            Logs.error(mTag, "Robot couldn't be initialized!");
            System.exit(1);
        }
    }

    /**
     * Do the scrolling!
     * @return Result (0: success, 1: error)
     */
    public int scroll(int scrollAmt) {
        mTag = cTag + "scroll";

        // problem with the robot
        if (robot == null) {
            Logs.error(mTag, "Can't load the robot!");
            return 1;
        }

        robot.mouseWheel(scrollAmt);

        return 0;
    }



}
