package synth.cfg;

import java.util.List;

/**
 * A production is of the form
 * return symbol ::= operator( argSymbol, argSymbol, ... )
 */
public class Production {
    /**
     * return symbol
     */
    private NonTerminal retSymbol;
    /**
     * operator symbol
     */
    private Terminal operator;
    /**
     * argument symbols
     */
    private List<Symbol> argSymbols;

    public Production(NonTerminal returnSymbol, Terminal operator, List<Symbol> argumentSymbols) {
        this.retSymbol = returnSymbol;
        this.operator = operator;
        this.argSymbols = argumentSymbols;
    }

    public NonTerminal getReturnSymbol() {
        return retSymbol;
    }

    public Terminal getOperator() {
        return operator;
    }

    public List<Symbol> getArgumentSymbols() {
        return argSymbols;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(retSymbol).append(" ::= ").append(operator);
        String separator = "";
        if (!argSymbols.isEmpty()) {
            builder.append("(");
            for (Symbol argSymbol : argSymbols) {
                builder.append(separator);
                separator = ", ";
                builder.append(argSymbol);
            }
            builder.append(")");
        }
        return builder.toString();
    }
}
