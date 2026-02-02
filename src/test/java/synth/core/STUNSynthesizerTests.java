package synth.core;

import org.junit.Assert;
import org.junit.Test;
import synth.cfg.CFG;
import synth.cfg.NonTerminal;
import synth.cfg.Production;
import synth.cfg.Terminal;
import java.util.*;

public class STUNSynthesizerTests {

    private CFG buildCFG() {
        NonTerminal startSymbol = new NonTerminal("E");
        Map<NonTerminal, List<Production>> symbolToProductions = new HashMap<>();

        {
            NonTerminal retSymbol = new NonTerminal("E");
            List<Production> prods = new ArrayList<>();
            prods.add(new Production(new NonTerminal("E"), new Terminal("Ite"),
                    List.of(new NonTerminal("B"), new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("E"), new Terminal("Add"),
                    List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("E"), new Terminal("Multiply"),
                    List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("E"), new Terminal("x"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("y"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("z"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("1"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("2"), Collections.emptyList()));
            prods.add(new Production(new NonTerminal("E"), new Terminal("3"), Collections.emptyList()));
            symbolToProductions.put(retSymbol, prods);
        }
        {
            NonTerminal retSymbol = new NonTerminal("B");
            List<Production> prods = new ArrayList<>();
            prods.add(new Production(new NonTerminal("B"), new Terminal("Lt"),
                    List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("Eq"),
                    List.of(new NonTerminal("E"), new NonTerminal("E"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("And"),
                    List.of(new NonTerminal("B"), new NonTerminal("B"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("Or"),
                    List.of(new NonTerminal("B"), new NonTerminal("B"))));
            prods.add(new Production(new NonTerminal("B"), new Terminal("Not"),
                    List.of(new NonTerminal("B"))));
            symbolToProductions.put(retSymbol, prods);
        }

        return new CFG(startSymbol, symbolToProductions);
    }

    // Helper for building a single example
    private Example makeExample(int x, int y, int z, int out) {
        Map<String, Integer> env = new HashMap<>();
        env.put("x", x);
        env.put("y", y);
        env.put("z", z);
        return new Example(env, out);
    }

     // Helper to check whether an AST contains an "Ite" operator anywhere
    private boolean containsIte(ASTNode node) {
        if (node.getSymbol().isTerminal()
                && "Ite".equals(node.getSymbol().getName())) {
            return true;
        }
        for (ASTNode child : node.getChildren()) {
            if (containsIte(child)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testSynthesizeSimpleAddExpression() {
        // Target function: f(x, y, z) = x + y
        List<Example> examples = List.of(
                makeExample(1, 2, 0, 3),
                makeExample(5, 7, 42, 12),
                makeExample(0, 3, 3, 3)
        );

        CFG cfg = buildCFG();
        STUNSynthesizer synthesizer = new STUNSynthesizer();
        Program program = synthesizer.synthesize(cfg, examples);

        Assert.assertNotNull("Synthesizer should find a program", program);

        // Check that the synthesized program matches all training examples
        for (Example ex : examples) {
            int result = Interpreter.evaluate(program, ex.getInput());
            Assert.assertEquals("Program should satisfy all examples",
                    ex.getOutput(), result);
        }

        Assert.assertFalse("Expected a flat expression without Ite for this simple task",
                containsIte(program.getRoot()));
    }

    @Test
    public void testSynthesizePiecewiseMinWithConditional() {
        // Target function (intuitively): f(x, y, z) = min(x, y)
        List<Example> examples = List.of(
                makeExample(1, 3, 0, 1),  // x < y
                makeExample(5, 2, 0, 2),  // x > y
                makeExample(7, 7, 0, 7),  // x == y
                makeExample(0, 3, 0, 0)   // x < y, and result is x
        );

        CFG cfg = buildCFG();
        STUNSynthesizer synthesizer = new STUNSynthesizer();
        Program program = synthesizer.synthesize(cfg, examples);

        Assert.assertNotNull("Synthesizer should find a program for the piecewise task", program);

        // The synthesized program must satisfy all the given examples
        for (Example ex : examples) {
            int result = Interpreter.evaluate(program, ex.getInput());
            Assert.assertEquals("Program should satisfy all piecewise examples",
                    ex.getOutput(), result);
        }

        Assert.assertTrue("Expected synthesized program to contain an Ite for piecewise behavior",
                containsIte(program.getRoot()));
    }

    @Test
    public void testSynthesizeReturnsNullOnConflictingOutputs() {
        Map<String, Integer> env = new HashMap<>();
        env.put("x", 1);
        env.put("y", 2);
        env.put("z", 3);

        Example e1 = new Example(env, 4);
        Example e2 = new Example(env, 5);

        List<Example> examples = List.of(e1, e2);

        CFG cfg = buildCFG();
        STUNSynthesizer synthesizer = new STUNSynthesizer();
        Program program = synthesizer.synthesize(cfg, examples);

        Assert.assertNull("Synthesizer should return null for inconsistent examples", program);
    }
}