package tools;

import java.util.concurrent.ThreadLocalRandom;

import static tools.Consts.DISP.PPI;
import static tools.Consts.DISP.INCH_MM;

public class Utils {

    private final static String cName = "Utils";
    private static String mName = cName + "."; // changed inside each method
    /*-------------------------------------------------------------------------------------*/

    /**
     * Returns a random int between the min (inclusive) and the bound (exclusive)
     * @param min Minimum (inclusive)
     * @param bound Bound (exclusive)
     * @return Random int
     * @throws IllegalArgumentException if bound < min
     */
    public static int randInt(int min, int bound) throws IllegalArgumentException {
        return ThreadLocalRandom.current().nextInt(min, bound);
    }

    /**
     * mm to pixel
     * @param mm - millimeters
     * @return equivalant in pixels
     */
    public static int mm2px(double mm) {
        mName += "mm2px";

        return (int) ((mm / INCH_MM) * PPI);
    }

    /**
     * mm to pixel
     * @param px - pixels
     * @return equivalant in mm
     */
    public static double px2mm(double px) {
        mName += "px2mm";

        return (int) ((px / PPI) * INCH_MM);
    }
}
