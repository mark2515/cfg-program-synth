package synth.core;

import synth.cfg.CFG;
import synth.cfg.NonTerminal;
import synth.cfg.Production;
import synth.cfg.Symbol;
import java.util.*;

public class TopDownEnumSynthesizer implements ISynthesizer {

    /**
     * Synthesize a program f(x, y, z) based on a context-free grammar and examples
     *
     * @param cfg      the context-free grammar
     * @param examples a list of examples
     * @return the program or null to indicate synthesis failure
     */
    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {
        // Initialize worklist with start symbol
        Queue<ASTNode> worklist = new LinkedList<>();
        worklist.add(new ASTNode(cfg.getStartSymbol(), Collections.emptyList()));
        
        while (!worklist.isEmpty()) {
            ASTNode ast = worklist.remove();
            
            // Check if AST is complete (no non-terminals)
            if (isComplete(ast)) {
                // Check if AST satisfies all examples
                if (satisfiesExamples(ast, examples)) {
                    return new Program(ast);
                }
            } else {
                // Expand the AST
                worklist.addAll(expand(ast, cfg));
            }
        }
        
        // Synthesis failed
        return null;
    }
    
    /**
     * Check if an AST is complete (no non-terminal symbols)
     *
     * @param ast the AST node
     * @return true if complete, false otherwise
     */
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
    
    /**
     * Check if an AST satisfies all examples
     *
     * @param ast the AST node
     * @param examples the list of examples
     * @return true if all examples are satisfied, false otherwise
     */
    private boolean satisfiesExamples(ASTNode ast, List<Example> examples) {
        Program program = new Program(ast);
        for (Example example : examples) {
            try {
                int result = Interpreter.evaluate(program, example.getInput());
                if (result != example.getOutput()) {
                    return false;
                }
            } catch (Exception e) {
                // Evaluation error means the program doesn't satisfy
                return false;
            }
        }
        return true;
    }
    
    /**
     * Expand an AST by finding the first non-terminal and applying all productions
     *
     * @param ast the AST node
     * @param cfg the context-free grammar
     * @return list of expanded AST nodes
     */
    private List<ASTNode> expand(ASTNode ast, CFG cfg) {
        List<ASTNode> result = new ArrayList<>();
        
        // If the current node is a non-terminal, expand it
        if (ast.getSymbol().isNonTerminal()) {
            NonTerminal nt = (NonTerminal) ast.getSymbol();
            List<Production> productions = cfg.getProductions(nt);
            
            for (Production prod : productions) {
                // Create children nodes from production arguments
                List<ASTNode> children = new ArrayList<>();
                for (Symbol argSymbol : prod.getArgumentSymbols()) {
                    children.add(new ASTNode(argSymbol, Collections.emptyList()));
                }
                
                // Create new AST node with operator and children
                ASTNode newNode = new ASTNode(prod.getOperator(), children);
                result.add(newNode);
            }
        } else {
            // Current node is terminal, find first non-terminal child and expand it
            List<ASTNode> children = ast.getChildren();
            for (int i = 0; i < children.size(); i++) {
                ASTNode child = children.get(i);
                if (!isComplete(child)) {
                    // Expand this child
                    List<ASTNode> expandedChildren = expand(child, cfg);
                    
                    // Create new AST nodes with expanded child
                    for (ASTNode expandedChild : expandedChildren) {
                        List<ASTNode> newChildren = new ArrayList<>();
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
}