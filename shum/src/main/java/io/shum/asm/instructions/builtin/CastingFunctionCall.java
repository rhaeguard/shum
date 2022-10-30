package io.shum.asm.instructions.builtin;

import org.objectweb.asm.MethodVisitor;

public final class CastingFunctionCall implements BuiltInFunctionCall{

    public enum Operation {
        CAST_INT, CAST_STRING
    }

    private final Operation operation;

    public CastingFunctionCall(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void apply(MethodVisitor mv) {
        switch (operation) {
            case CAST_INT -> mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
            case CAST_STRING -> mv.visitTypeInsn(CHECKCAST, "java/lang/String");
        }
    }
}
