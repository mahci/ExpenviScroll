package experiment;

import static control.Experimenter.*;

/***
 * Class of each trial
 */
public class Trial {

    private final static String NAME = "Trial--";
    // ------------------------------------------------------------------------------------
    public int distance; // in lines
    public int frameSize; // in lines
    public Direction direction; // UP or DOWN
    public ScrollMode scrollMode;

    // ------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param dist Distance
     * @param fS Fram size (height/width)
     * @param dir Direction
     */
    public Trial(int dist, int fS, Direction dir, ScrollMode scM) {
        distance = dist;
        frameSize = fS;
        direction = dir;
        scrollMode = scM;
    }

    /**
     * Default constructor
     */
    public Trial() {
        distance = 0;
        frameSize = 0;
    }

    @Override
    public String toString() {
        return "Trial{" +
                "distance=" + distance +
                ", frameHeight=" + frameSize +
                ", direction=" + direction +
                '}';
    }

}
