package synth.cfg;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for Symbol classes (Terminal and NonTerminal).
 */

public class SymbolTests {

    @Test
    public void testTerminalCreation() {
        Terminal terminal = new Terminal("Add");
        Assert.assertEquals("Add", terminal.getName());
        Assert.assertTrue(terminal.isTerminal());
        Assert.assertFalse(terminal.isNonTerminal());
    }

    @Test
    public void testNonTerminalCreation() {
        NonTerminal nonTerminal = new NonTerminal("E");
        Assert.assertEquals("E", nonTerminal.getName());
        Assert.assertFalse(nonTerminal.isTerminal());
        Assert.assertTrue(nonTerminal.isNonTerminal());
    }

    @Test
    public void testTerminalEquality() {
        Terminal t1 = new Terminal("Add");
        Terminal t2 = new Terminal("Add");
        Terminal t3 = new Terminal("Multiply");
        
        Assert.assertEquals(t1, t2);
        Assert.assertNotEquals(t1, t3);
        Assert.assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void testNonTerminalEquality() {
        NonTerminal n1 = new NonTerminal("E");
        NonTerminal n2 = new NonTerminal("E");
        NonTerminal n3 = new NonTerminal("B");
        
        Assert.assertEquals(n1, n2);
        Assert.assertNotEquals(n1, n3);
        Assert.assertEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    public void testTerminalAndNonTerminalNotEqual() {
        Terminal terminal = new Terminal("E");
        NonTerminal nonTerminal = new NonTerminal("E");
        
        Assert.assertNotEquals(terminal, nonTerminal);
    }

    @Test
    public void testTerminalToString() {
        Terminal terminal = new Terminal("Add");
        Assert.assertEquals("Add", terminal.toString());
    }

    @Test
    public void testNonTerminalToString() {
        NonTerminal nonTerminal = new NonTerminal("E");
        Assert.assertEquals("E", nonTerminal.toString());
    }
}