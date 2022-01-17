package tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinMax {
    private int min;
    private int max;

    /**
     * Check if a value is between min and max (inlcusive)
     * @param value Value to check
     * @return True/False
     */
    public boolean isWithin(int value) {return value <= max && value >= min;}

    /**
     * isWithing exclusive of min and max
     * @param value Value to check
     * @return True/false
     */
    public boolean isWithinEx(int value) {return value < max && value > min;}

    /**
     * Get the range
     * @return Range between min and max
     */
    public int getRange() {return max - min;}

    /**
     * Move both the min and max
     * @param minAmt Amount to move min
     * @param maxAmt Amoutn to move max
     */
    public void move(int minAmt, int maxAmt) {
        min += minAmt;
        max += maxAmt;
    }

    public void moveMin(int amt) {
        min += amt;
    }

    public void moveMax(int amt) {
        max += amt;
    }
}
