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
    private AREA area;
    private int distance;
    private int frame;

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
