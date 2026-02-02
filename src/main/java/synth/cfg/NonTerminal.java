package synth.cfg;

import java.util.Objects;

public class NonTerminal extends Symbol {
    public NonTerminal(String name) {
        super(name);
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public boolean isNonTerminal() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NonTerminal)) return false;
        NonTerminal other = (NonTerminal) o;
        return Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
