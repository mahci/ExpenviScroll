package experiment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static data.Consts.STRINGS.*;

import static experiment.Experiment.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trial {
    private TASK task;
    private DIRECTION direction;
    private int vtDist;
    private int tdDist;
    private int frame;

    public String toLogString() {
        return task + SP +
                direction + SP +
                vtDist + SP +
                tdDist + SP +
                frame;
    }

    public static String getLogHeader() {
        return "task" + SP +
                "direction" + SP +
                "vt_dist" + SP +
                "td_dist" + SP +
                "frame";
    }
}
