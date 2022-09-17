package io.shum.asm.instructions;

import io.shum.language.type.Type;
import org.objectweb.asm.MethodVisitor;

public final class VariableDeclaration implements Instruction {

    private final String name;
    private final Type dataType;
    // used for local variables
    // it is -1 for static variable
    private int localVariableIndex = -1;

    public String getName() {
        return name;
    }

    public Type getDataType() {
        return dataType;
    }

    public int getLocalVariableIndex() {
        return localVariableIndex;
    }

    public VariableDeclaration(String name, Type dataType) {
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
