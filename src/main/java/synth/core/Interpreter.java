package synth.core;

import java.util.Map;

public class Interpreter {

    /**
     * A static method to evaluate a program given an environment.
     *
     * @param program     the program to be evaluated
     * @param environment mapping from all variable names to their values
     * @return the value of the program expression
     */
    public static int evaluate(Program program, Map<String, Integer> environment) {
        Interpreter interpreter = new Interpreter(environment);
        return interpreter.evalExpr(program.getRoot());
    }

    /**
     * mapping from all variable names to their values
     */
    private final Map<String, Integer> environment;

    public Interpreter(Map<String, Integer> environment) {
        this.environment = environment;
    }

    public int evalExpr(ASTNode expr) {
        switch (expr.getSymbol().getName()) {
            case "Ite":
                return evalIte(expr);
            case "Add":
                return evalAdd(expr);
            case "Multiply":
                return evalMultiply(expr);
            case "x":
            case "y":
            case "z":
                return evalVar(expr);
            case "1":
            case "2":
            case "3":
                return evalConst(expr);
            default:
                throw new RuntimeException("Cannot evaluate expression " + expr);
        }
    }

    public boolean evalPred(ASTNode pred) {
        switch (pred.getSymbol().getName()) {
            case "Lt":
                return evalLt(pred);
            case "Eq":
                return evalEq(pred);
            case "And":
                return evalAnd(pred);
            case "Or":
                return evalOr(pred);
            case "Not":
                return evalNot(pred);
            default:
                throw new RuntimeException("Cannot evaluate predicate " + pred);
        }
    }

    public int evalIte(ASTNode ite) {
        if (evalPred(ite.getChild(0))) {
            return evalExpr(ite.getChild(1));
        } else {
            return evalExpr(ite.getChild(2));
        }
    }

    public int evalAdd(ASTNode add) {
        return evalExpr(add.getChild(0)) + evalExpr(add.getChild(1));
    }

    public int evalMultiply(ASTNode multiply) {
        return evalExpr(multiply.getChild(0)) * evalExpr(multiply.getChild(1));
    }

    public int evalVar(ASTNode v) {
        return environment.get(v.getSymbol().getName());
    }

    public int evalConst(ASTNode c) {
        return Integer.parseInt(c.getSymbol().getName());
    }

    public boolean evalLt(ASTNode lt) {
        return evalExpr(lt.getChild(0)) < evalExpr(lt.getChild(1));
    }

    public boolean evalEq(ASTNode eq) {
        return evalExpr(eq.getChild(0)) == evalExpr(eq.getChild(1));
    }

    public boolean evalAnd(ASTNode and) {
        return evalPred(and.getChild(0)) && evalPred(and.getChild(1));
    }

    public boolean evalOr(ASTNode or) {
        return evalPred(or.getChild(0)) || evalPred(or.getChild(1));
    }

    public boolean evalNot(ASTNode not) {
        return !evalPred(not.getChild(0));
    }
}
