package synth.core;

import synth.cfg.CFG;
import synth.cfg.NonTerminal;
import synth.cfg.Production;
import synth.cfg.Symbol;
import synth.cfg.Terminal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class STUNSynthesizer implements ISynthesizer {

    private static final int MAX_EXPR_NODES = 9;

    
    // Maximum size (number of AST nodes) allowed for synthesized boolean conditions
    private static final int MAX_COND_NODES = 7;

    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {
        if (examples == null || examples.isEmpty()) {
            // No examples: return null to indicate "no information"
            return null;
        }

        if (hasConflictingOutputs(examples)) {
            return null;
        }

        ASTNode flat = synthesizeFlatExpressionWithoutIte(cfg, examples);
        ASTNode root = flat;
        if (root == null) {
            // If no unconditional solution exists within the size bound, fall back
            // to the full STUN-based synthesis that is allowed to use Ite
            root = synthesizeForExamples(cfg, examples);
        }
        if (root == null) {
            return null;
        }
        return new Program(root);
    }

    private boolean hasConflictingOutputs(List<Example> examples) {
        Map<Map<String, Integer>, Integer> seen = new HashMap<>();
        for (Example ex : examples) {
            Map<String, Integer> inputEnv = ex.getInput();
            Integer previousOutput = seen.get(inputEnv);
            if (previousOutput == null) {
                seen.put(inputEnv, ex.getOutput());
            } else if (previousOutput != ex.getOutput()) {
                return true;
            }
        }
        return false;
    }

    private ASTNode synthesizeFlatExpressionWithoutIte(CFG cfg, List<Example> examples) {
        Deque<ASTNode> worklist = new ArrayDeque<>();
        worklist.add(new ASTNode(cfg.getStartSymbol(), Collections.emptyList()));

        while (!worklist.isEmpty()) {
            ASTNode candidate = worklist.removeFirst();

            if (countNodes(candidate) > MAX_EXPR_NODES) {
                continue;
            }

            if (isComplete(candidate)) {
                if (satisfiesAllExamples(candidate, examples)) {
                    return candidate;
                }
            } else {
                for (ASTNode expanded : expandWithoutIte(candidate, cfg)) {
                    worklist.addLast(expanded);
                }
            }
        }

        return null;
    }

     // Recursively synthesize an AST that is consistent with all given examples
    private ASTNode synthesizeForExamples(CFG cfg, List<Example> examples) {
        // Breadth-first enumeration over candidate expressions starting from the start symbol "E"
        Deque<ASTNode> worklist = new ArrayDeque<>();
        worklist.add(new ASTNode(cfg.getStartSymbol(), Collections.emptyList()));

        while (!worklist.isEmpty()) {
            ASTNode candidate = worklist.removeFirst();

            if (countNodes(candidate) > MAX_EXPR_NODES) {
                continue;
            }

            if (isComplete(candidate)) {
                // Evaluate candidate on all examples and partition into "good" and "bad"
                List<Example> good = new ArrayList<>();
                List<Example> bad = new ArrayList<>();
                partitionExamplesByCandidate(candidate, examples, good, bad);

                if (good.isEmpty()) {
                    // Candidate is wrong on all examples, skip
                    continue;
                }

                if (bad.isEmpty()) {
                    // Candidate works for all examples, done
                    return candidate;
                }

                ASTNode guard = synthesizeGuard(cfg, good, bad);
                if (guard == null) {
                    // This partial solution cannot be unified cleanly; try another candidate
                    continue;
                }

                ASTNode elseBranch = synthesizeForExamples(cfg, bad);
                if (elseBranch != null) {
                    return makeIteNode(guard, candidate, elseBranch);
                }
                // Recursion failed for this candidate; continue enumerating
            } else {
                // Incomplete AST: expand the first non-terminal in a top-down fashion
                for (ASTNode expanded : expand(candidate, cfg)) {
                    worklist.addLast(expanded);
                }
            }
        }

        // No consistent program found within search bounds
        return null;
    }

    // Check if a complete candidate expression matches all examples
    private boolean satisfiesAllExamples(ASTNode expr, List<Example> examples) {
        for (Example ex : examples) {
            Integer value = tryEvalExpr(expr, ex.getInput());
            if (value == null || value != ex.getOutput()) {
                return false;
            }
        }
        return true;
    }

    private ASTNode synthesizeGuard(CFG cfg, List<Example> positives, List<Example> negatives) {
        // Start enumeration from the boolean non-terminal "B"
        NonTerminal boolStart = new NonTerminal("B");
        Deque<ASTNode> worklist = new ArrayDeque<>();
        worklist.add(new ASTNode(boolStart, Collections.emptyList()));

        while (!worklist.isEmpty()) {
            ASTNode candidate = worklist.removeFirst();

            if (countNodes(candidate) > MAX_COND_NODES) {
                continue;
            }

            if (isComplete(candidate)) {
                if (guardMatchesClassification(candidate, positives, negatives)) {
                    return candidate;
                }
            } else {
                for (ASTNode expanded : expand(candidate, cfg)) {
                    worklist.addLast(expanded);
                }
            }
        }

        return null;
    }

    private boolean guardMatchesClassification(ASTNode guard,
                                               List<Example> positives,
                                               List<Example> negatives) {
        for (Example ex : positives) {
            Boolean v = tryEvalPred(guard, ex.getInput());
            if (v == null || !v) {
                return false;
            }
        }
        for (Example ex : negatives) {
            Boolean v = tryEvalPred(guard, ex.getInput());
            if (v == null || v) {
                return false;
            }
        }
        return true;
    }

    private void partitionExamplesByCandidate(ASTNode expr,
                                              List<Example> all,
                                              List<Example> good,
                                              List<Example> bad) {
        for (Example ex : all) {
            Integer value = tryEvalExpr(expr, ex.getInput());
            if (value != null && value == ex.getOutput()) {
                good.add(ex);
            } else {
                bad.add(ex);
            }
        }
    }

    private Integer tryEvalExpr(ASTNode expr, Map<String, Integer> env) {
        try {
            return Interpreter.evaluate(new Program(expr), env);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Boolean tryEvalPred(ASTNode pred, Map<String, Integer> env) {
        try {
            Interpreter interpreter = new Interpreter(env);
            return interpreter.evalPred(pred);
        } catch (RuntimeException e) {
            return null;
        }
    }

    // Check if an AST is complete, i.e., contains no non-terminal symbols anywhere     
    private boolean isComplete(ASTNode ast) {
        if (ast.getSymbol().isNonTerminal()) {
            return false;
        }
        for (ASTNode child : ast.getChildren()) {
            if (!isComplete(child)) {
                return false;
            }
        }
        return true;
    }

    private List<ASTNode> expand(ASTNode ast, CFG cfg) {
        List<ASTNode> result = new ArrayList<>();

        if (ast.getSymbol().isNonTerminal()) {
            NonTerminal nt = (NonTerminal) ast.getSymbol();
            List<Production> productions = cfg.getProductions(nt);
            if (productions == null) {
                return result;
            }
            for (Production prod : productions) {
                List<ASTNode> children = new ArrayList<>();
                for (Symbol argSymbol : prod.getArgumentSymbols()) {
                    children.add(new ASTNode(argSymbol, Collections.emptyList()));
                }
                ASTNode newNode = new ASTNode(prod.getOperator(), children);
                result.add(newNode);
            }
        } else {
            List<ASTNode> children = ast.getChildren();
            for (int i = 0; i < children.size(); i++) {
                ASTNode child = children.get(i);
                if (!isComplete(child)) {
                    List<ASTNode> expandedChildren = expand(child, cfg);
                    for (ASTNode expandedChild : expandedChildren) {
                        List<ASTNode> newChildren = new ArrayList<>(children.size());
                        for (int j = 0; j < children.size(); j++) {
                            if (j == i) {
                                newChildren.add(expandedChild);
                            } else {
                                newChildren.add(children.get(j));
                            }
                        }
                        ASTNode newNode = new ASTNode(ast.getSymbol(), newChildren);
                        result.add(newNode);
                    }
                    // Only expand the first non-terminal child
                    break;
                }
            }
        }

        return result;
    }

    private List<ASTNode> expandWithoutIte(ASTNode ast, CFG cfg) {
        List<ASTNode> result = new ArrayList<>();

        if (ast.getSymbol().isNonTerminal()) {
            NonTerminal nt = (NonTerminal) ast.getSymbol();
            List<Production> productions = cfg.getProductions(nt);
            if (productions == null) {
                return result;
            }
            for (Production prod : productions) {
                if ("E".equals(nt.getName()) && "Ite".equals(prod.getOperator().getName())) {
                    continue;
                }
                List<ASTNode> children = new ArrayList<>();
                for (Symbol argSymbol : prod.getArgumentSymbols()) {
                    children.add(new ASTNode(argSymbol, Collections.emptyList()));
                }
                ASTNode newNode = new ASTNode(prod.getOperator(), children);
                result.add(newNode);
            }
        } else {
            List<ASTNode> children = ast.getChildren();
            for (int i = 0; i < children.size(); i++) {
                ASTNode child = children.get(i);
                if (!isComplete(child)) {
                    List<ASTNode> expandedChildren = expandWithoutIte(child, cfg);
                    for (ASTNode expandedChild : expandedChildren) {
                        List<ASTNode> newChildren = new ArrayList<>(children.size());
                        for (int j = 0; j < children.size(); j++) {
                            if (j == i) {
                                newChildren.add(expandedChild);
                            } else {
                                newChildren.add(children.get(j));
                            }
                        }
                        ASTNode newNode = new ASTNode(ast.getSymbol(), newChildren);
                        result.add(newNode);
                    }
                    break;
                }
            }
        }

        return result;
    }

    // Count the total number of nodes in an AST
    private int countNodes(ASTNode ast) {
        int count = 1;
        for (ASTNode child : ast.getChildren()) {
            count += countNodes(child);
        }
        return count;
    }

    // Construct an Ite(B, E1, E2) AST node
    private ASTNode makeIteNode(ASTNode cond, ASTNode thenBranch, ASTNode elseBranch) {
        Terminal iteOp = new Terminal("Ite");
        List<ASTNode> children = new ArrayList<>(3);
        children.add(cond);
        children.add(thenBranch);
        children.add(elseBranch);
        return new ASTNode(iteOp, children);
    }
}
