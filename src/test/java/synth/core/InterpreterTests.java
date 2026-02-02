package synth.core;

import org.junit.Assert;
import org.junit.Test;
import synth.cfg.Terminal;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for interpreters.
 * Note that there are only three variables x, y, z and three constants 1, 2, 3.
 */
public class InterpreterTests {

    /**
     * Build an environment where x=10, y=15, z=20
     *
     * @return the environment map
     */
    private Map<String, Integer> buildEnvironment() {
        Map<String, Integer> env = new HashMap<>();
        env.put("x", 10);
        env.put("y", 15);
        env.put("z", 20);
        return env;
    }

    @Test
    public void testInterpreter1() {
        // Add(x, y)
        Program program = new Program(
                new ASTNode(new Terminal("Add"),
                        List.of(
                                new ASTNode(new Terminal("x"), Collections.emptyList()),
                                new ASTNode(new Terminal("y"), Collections.emptyList()))
                ));
        int result = Interpreter.evaluate(program, buildEnvironment());
        Assert.assertEquals(25, result);
    }

    @Test
    public void testInterpreter2() {
        // Multiply(z, 2)
        Program program = new Program(
                new ASTNode(new Terminal("Multiply"),
                        List.of(
                                new ASTNode(new Terminal("z"), Collections.emptyList()),
                                new ASTNode(new Terminal("2"), Collections.emptyList()))
                ));
        int result = Interpreter.evaluate(program, buildEnvironment());
        Assert.assertEquals(40, result);
    }

    @Test
    public void testInterpreter3() {
        // Ite(Lt(x, 3), Add(y, z), Multiply(y, z))
        Program program = new Program(
                new ASTNode(new Terminal("Ite"),
                        List.of(
                                new ASTNode(new Terminal("Lt"),
                                        List.of(
                                                new ASTNode(new Terminal("x"), Collections.emptyList()),
                                                new ASTNode(new Terminal("3"), Collections.emptyList()))),
                                new ASTNode(new Terminal("Add"),
                                        List.of(
                                                new ASTNode(new Terminal("y"), Collections.emptyList()),
                                                new ASTNode(new Terminal("z"), Collections.emptyList()))),
                                new ASTNode(new Terminal("Multiply"),
                                        List.of(
                                                new ASTNode(new Terminal("y"), Collections.emptyList()),
                                                new ASTNode(new Terminal("z"), Collections.emptyList())))
                        )));
        int result = Interpreter.evaluate(program, buildEnvironment());
        Assert.assertEquals(300, result);
    }
}
