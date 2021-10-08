package tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static tools.Consts.DISP.PPI;
import static tools.Consts.DISP.INCH_MM;

public class Utils {

    private final static String cName = "Utils.";
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
        String mName = cName + "mm2px";

        return (int) ((mm / INCH_MM) * PPI);
    }

    /**
     * mm to pixel
     * @param px - pixels
     * @return equivalant in mm
     */
    public static double px2mm(double px) {
        String mName = cName + "px2mm";

        return (int) ((px / PPI) * INCH_MM);
    }

    /**
     * Generate a random permutation of {0, 1, ..., len - 1}
     * @param len - length of the permutation
     * @return Random permutation
     */
    public static List<Integer> randPerm(int len) {
        String mName = cName + "randPerm";

        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            indexes.add(i);
        }
        Collections.shuffle(indexes);

        return indexes;
    }
}
