package experiment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static experiment.Experiment.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trial {
    private SCROLL_MODE scrollMode;
    private DIRECTION direction;
    private int distance;
    private int frame;

    // ------------------------------------------------------------------------------------

//    public Trial(SCROLL_MODE scMode, DIRECTION ar, int dist, int fr) {
//        scrollMode = scMode;
//        DIRECTION = ar;
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
//                "| DIRECTION=" + DIRECTION +
//                "| distance=" + distance +
//                "| frame=" + frame +
//                '}';
//    }

}
