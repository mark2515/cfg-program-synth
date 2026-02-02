package synth.cfg;

import java.util.Objects;

public class Terminal extends Symbol {
    public Terminal(String name) {
        super(name);
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public boolean isNonTerminal() {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Terminal)) return false;
        Terminal other = (Terminal) o;
        return Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
