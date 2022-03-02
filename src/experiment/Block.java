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
    private ArrayList<Trial> mTrials = new ArrayList<>();
    private TASK mTask;
    private int mTargetNTrials; // How many trials should be hit? (Initial number before shuffling back fails)

    /**
     * Construct the block of TASK t
     * @param t TASK (vertical or 2D)
     */
    public Block(TASK t, int[] vtDistances, int[] tdDistances, int[] frames) {
        mTask = t;
        if (t.equals(TASK.VERTICAL)) {

            for (int d : vtDistances) {
                for (int f : frames) {
                    mTrials.add(new Trial(t, DIRECTION.N, d, 0, f));
                    mTrials.add(new Trial(t, DIRECTION.S, d, 0, f));
                }
            }

            // Shuffle the trials to make it random
            Collections.shuffle(mTrials);

        } else { // 2D

            for (int d : tdDistances) {
                for (int f : frames) {
                    mTrials.add(new Trial(t, DIRECTION.NE, 0, d, f));
                    mTrials.add(new Trial(t, DIRECTION.SE, 0, d, f));
                    mTrials.add(new Trial(t, DIRECTION.NW, 0, d, f));
                    mTrials.add(new Trial(t, DIRECTION.SW, 0, d, f));
                }
            }

            // Shuffle the trials to make it random
            Collections.shuffle(mTrials);
        }

        // Set the number
        mTargetNTrials = mTrials.size();
    }

    /**
     * Get a trial
     * @param trInd Trial index
     * @return Trial
     */
    public Trial getTrial(int trInd) {
        return mTrials.get(trInd);
    }

    /**
     * Get the number of trials
     * @return Number of trials
     */
    public int getNTrials() {
        return mTrials.size();
    }

    /**
     * Get the number of trials to hit
     * @return Number of trials to hit
     */
    public int getTargetNTrials() {
        return mTargetNTrials;
    }

    /**
     * Get the rest of trials (after and not incl. the trialInd)
     * @param trialInd Trial index
     * @return List of Trials
     */
    public List<Trial> getRestOfTrials(int trialInd) {
        if (trialInd == mTrials.size() - 1) return new ArrayList<>();
        else return mTrials.subList(trialInd + 1, mTrials.size());
    }

    /**
     * Shuffle a duplicate of a Trial to the rest
     * @param trialInd Trial index
     */
    public void dupeShuffleTrial(int trialInd) {
        final Trial trial = mTrials.get(trialInd);
        final int lastInd = mTrials.size() - 1;
        final int insertInd;

        if (trialInd == lastInd) insertInd = lastInd;
        else insertInd = Utils.randInt(trialInd, lastInd);

        mTrials.add(insertInd, trial);
    }

}
