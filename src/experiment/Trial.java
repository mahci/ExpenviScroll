package experiment;

import static control.Experimenter.Direction;

/***
 * Class of each trial
 */
public class Trial {

    private final static String cName = "Trial";
    private static String mName = cName + "."; // for logging
    /*-------------------------------------------------------------------------------------*/

    private int distance; // in lines
    private int frameHeight; // in lines
    private Direction direction; // UP or DOWN

//    public static class Vars {
//        private int distance; // in lines
//        private int frameHeight; // in lines
//        private Direction direction; // UP or DOWN
//
//        /**
//         * Constructor
//         * @param dist - distance
//         * @param fH - frame height
//         * @param dir - direction
//         */
//        public Vars(int dist, int fH, Direction dir) {
//            distance = dist;
//            frameHeight = fH;
//            direction = dir;
//        }
//    }

//    private Vars vars;
    /*-------------------------------------------------------------------------------------*/

    /**
     * Constructor
     * @param v vars
     */
//    public Trial(Vars v) {
//        this.vars = v;
//    }

    public Trial(int dist, int fH, Direction dir) {
        this.distance = dist;
        this.frameHeight = fH;
        this.direction = dir;
    }

    @Override
    public String toString() {
        return "Trial{" +
                "distance=" + distance +
                ", frameHeight=" + frameHeight +
                ", direction=" + direction +
                '}';
    }
}
