package experiment;

import lombok.AllArgsConstructor;
import lombok.Data;
import tools.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static experiment.Experiment.*;

@Data
@AllArgsConstructor
public class Block {
    private ArrayList<Trial> trials = new ArrayList<>();
    private TASK task;

    /**
     * Construct the block of TASK t
     * @param t TASK (vertical or 2D)
     */
    public Block(TASK t, int[] vtDistances, int[] tdDistances, int[] frames) {
        task = t;
        if (t.equals(TASK.VERTICAL)) {

            for (int d : vtDistances) {
                for (int f : frames) {
                    trials.add(new Trial(t, DIRECTION.N, d, 0, f));
                    trials.add(new Trial(t, DIRECTION.S, d, 0, f));
                }
            }

            // Shuffle the trials to make it random
            Collections.shuffle(trials);

        } else { // 2D

            for (int d : tdDistances) {
                for (int f : frames) {
                    trials.add(new Trial(t, DIRECTION.NE, 0, d, f));
                    trials.add(new Trial(t, DIRECTION.SE, 0, d, f));
                    trials.add(new Trial(t, DIRECTION.NW, 0, d, f));
                    trials.add(new Trial(t, DIRECTION.SW, 0, d, f));
                }
            }

            // Shuffle the trials to make it random
            Collections.shuffle(trials);
        }
    }

    /**
     * Get a trial
     * @param trInd Trial index
     * @return Trial
     */
    public Trial getTrial(int trInd) {
        return trials.get(trInd);
    }

    /**
     * Get the number of trials
     * @return Number of trials
     */
    public int getNTrials() {
        return trials.size();
    }

    /**
     * Get the rest of trials (after and not incl. the trialInd)
     * @param trialInd Trial index
     * @return List of Trials
     */
    public List<Trial> getRestOfTrials(int trialInd) {
        if (trialInd == trials.size() - 1) return new ArrayList<>();
        else return trials.subList(trialInd + 1, trials.size());
    }

    /**
     * Shuffle a duplicate of a Trial to the trest
     * @param trialInd Trial index
     */
    public void dupeShuffleTrial(int trialInd) {
        final Trial trial = trials.get(trialInd);
        final int lastInd = trials.size() - 1;
        final int insertInd;
        if (trialInd == lastInd) insertInd = lastInd;
        else {
            insertInd = Utils.randInt(trialInd, lastInd);
            trials.add(insertInd, trial);
        }
    }

}
