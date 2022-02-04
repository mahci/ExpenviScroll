package tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pair {
    private int first;
    private int second;

    public void set(int f, int s) {
        first = f;
        second = s;
    }

    public boolean areBoth(int value) {
        return (first == value) && (second == value);
    }

    /**
     * Basically AND
     * @return
     */
    public int fXs() {
        return first * second;
    }

    /**
     * Increase first
     */
    public void incF() {
        first++;
    }

    /**
     * Increase first
     */
    public void incS() {
        second++;
    }
}
