package experiment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static experiment.Experiment.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trial {
    private TASK scrollMode;
    private DIRECTION direction;
    private int vtDist;
    private int hzDist;
    private int frame;

    public Trial(TASK scMode, DIRECTION dr, int vtD, int fr) {
        scrollMode = scMode;
        direction = dr;
        vtDist = vtD;
        frame = fr;
    }

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
