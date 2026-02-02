package synth.core;

public class Program {
    private final ASTNode root;

    public Program(ASTNode root) {
        this.root = root;
    }

    public ASTNode getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return String.valueOf(root);
    }
}
