package io.shum.asm.instructions;

import org.objectweb.asm.MethodVisitor;

import java.util.List;

public final class MacroDeclaration implements Instruction {

    private final String name;
    private final List<Instruction> instructions;

    public MacroDeclaration(String name, List<Instruction> instructions) {
        this.name = name;
        this.instructions = instructions;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public void apply(MethodVisitor mv) {
        for (var instruction : instructions) {
            instruction.apply(mv);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, instructions.toString());
    }
}
