package io.shum.asm.instructions;

import org.objectweb.asm.MethodVisitor;

import java.util.List;

public sealed class FunctionDeclaration implements Instruction permits AnonymousFunctionDeclaration {

    private final String name;
    private final List<Instruction> instructions;

    public FunctionDeclaration(String name, List<Instruction> instructions) {
        this.name = name;
        this.instructions = instructions;
    }

    @Override
    public void apply(MethodVisitor mv) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return name;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, instructions.toString());
    }
}
