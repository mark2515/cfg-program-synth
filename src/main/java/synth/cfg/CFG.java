package synth.cfg;

import java.util.List;
import java.util.Map;

public class CFG {
    /**
     * map from non-terminal (return) symbols to all their productions
     */
    private final Map<NonTerminal, List<Production>> symbolToProductions;
    /**
     * start symbol of the grammar
     */
    private final NonTerminal startSymbol;

    public CFG(NonTerminal startSymbol, Map<NonTerminal, List<Production>> symbolToProductions) {
        this.startSymbol = startSymbol;
        this.symbolToProductions = symbolToProductions;
    }

    public NonTerminal getStartSymbol() {
        return startSymbol;
    }

    public List<Production> getProductions(NonTerminal symbol) {
        return symbolToProductions.get(symbol);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Start symbol: ").append(startSymbol).append(System.lineSeparator());
        builder.append("Productions:").append(System.lineSeparator());
        for (NonTerminal retSymbol : symbolToProductions.keySet()) {
            builder.append(symbolToProductions.get(retSymbol)).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
