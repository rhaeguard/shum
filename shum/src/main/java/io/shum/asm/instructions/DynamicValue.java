package io.shum.asm.instructions;

import org.objectweb.asm.MethodVisitor;

public final class DynamicValue implements Value {

    private final VariableOperation operation;

    public DynamicValue(VariableOperation operation) {
        this.operation = operation;
    }

    @Override
    public void apply(MethodVisitor mv) {
        this.operation.apply(mv);
    }

}
