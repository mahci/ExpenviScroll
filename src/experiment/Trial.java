package experiment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static tools.Consts.STRINGS.*;

import static experiment.Experiment.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trial {
    private TASK task;
    private DIRECTION direction;
    private int vtDist;
    private int hzDist;
    private int frame;

    public Trial(TASK tsk, DIRECTION dr, int vtD, int fr) {
        task = tsk;
        direction = dr;
        vtDist = vtD;
        frame = fr;
    }

    public String toLogString() {
        return task.ordinal() + SP +
                direction.ordinal() + SP +
                vtDist + SP +
                frame;
    }
}
