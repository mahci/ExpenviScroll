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
}
