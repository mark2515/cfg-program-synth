package synth.core;

import java.util.Map;
import java.util.Objects;

public class Example {
    /**
     * Input: mapping from variable names to their values
     */
    private final Map<String, Integer> input;
    /**
     * Output value
     */
    private final int output;

    public Example(Map<String, Integer> input, int output) {
        this.input = input;
        this.output = output;
    }

    public Map<String, Integer> getInput() {
        return input;
    }

    public int getOutput() {
        return output;
    }

    @Override
    public int hashCode() {
        return input.hashCode() ^ output;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Example)) return false;
        Example other = (Example) o;
        return Objects.equals(input, other.input) && output == other.output;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", input, output);
    }
}
