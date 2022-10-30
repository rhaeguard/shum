package io.shum.asm.instructions.builtin;

import org.objectweb.asm.MethodVisitor;

import static io.shum.asm.instructions.CastingInstructions.INT_TO_LONG_OBJECT;
import static io.shum.asm.instructions.CastingInstructions.LONG_OBJECT_TO_INT;

public final class BitwiseFunctionCall implements BuiltInFunctionCall {

    public enum Operation {
        SHIFT_LEFT, SHIFT_RIGHT, BITWISE_AND, BITWISE_OR
    }

    private final Operation operation;

    public BitwiseFunctionCall(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void apply(MethodVisitor mv) {
        switch (operation) {
            case SHIFT_LEFT -> {
                mv.visitInsn(SWAP);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                mv.visitInsn(DUP2_X1);
                mv.visitInsn(POP2);
                LONG_OBJECT_TO_INT.accept(mv);
                mv.visitInsn(LSHL);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            }
            case SHIFT_RIGHT -> {}
            case BITWISE_AND -> {}
            case BITWISE_OR -> {
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                mv.visitInsn(DUP2_X1);
                mv.visitInsn(POP2);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                mv.visitInsn(LOR);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            }
        }
    }
}
