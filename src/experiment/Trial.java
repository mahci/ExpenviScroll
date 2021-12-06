package experiment;

import static experiment.Experiment.*;

/***
 * Class of each trial
 */
public record Trial (SCROLL_MODE scrollMode, AREA area, int distance, int frame) {

    public Trial() {
        this(SCROLL_MODE.VERTICAL, AREA.N, 0, 0);
    }


    // ------------------------------------------------------------------------------------

//    public Trial(SCROLL_MODE scMode, AREA ar, int dist, int fr) {
//        scrollMode = scMode;
//        area = ar;
//        distance = dist;
//        frame = fr;
//    }
//
//    public Trial() {
//
//    }
//
//    @Override
//    public String toString() {
//        return "Trial{" +
//                "scrollMode=" + scrollMode +
//                "| area=" + area +
//                "| distance=" + distance +
//                "| frame=" + frame +
//                '}';
//    }

}
