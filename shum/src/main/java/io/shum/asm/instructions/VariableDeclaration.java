package io.shum.asm.instructions;

import io.shum.language.ShumDataType;
import org.objectweb.asm.MethodVisitor;

public final class VariableDeclaration implements Instruction {

    private final String name;
    private final ShumDataType dataType;
    // used for local variables
    // it is -1 for static variable
    private int localVariableIndex = -1;

    public String getName() {
        return name;
    }

    public ShumDataType getDataType() {
        return dataType;
    }

    public int getLocalVariableIndex() {
        return localVariableIndex;
    }

    public VariableDeclaration(String name, ShumDataType dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public VariableDeclaration withIndex(int index) {
        this.localVariableIndex = index;
        return this;
    }

    @Override
    public void apply(MethodVisitor mv) {
        // TODO
        throw new RuntimeException("Unsupported operation");
    }

}
