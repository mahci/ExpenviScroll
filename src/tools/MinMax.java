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

    public boolean isWithin(int value) {
        return value <= max && value >= min;
    }

    public int getRange() {
        return max - min;
    }
}
