package io.shum.asm.instructions.builtin;

import io.shum.asm.instructions.Instruction;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public final class ComparisonFunctionCall implements BuiltInFunctionCall {

    public enum Operation {
        GREATER_THAN, GREATER_EQUAL, LESS_THAN, LESS_EQUAL, EQUAL, NOT_EQUAL, NOT
    }

    private final Operation operation;

    public ComparisonFunctionCall(Operation operation) {
        this.operation = operation;
    }

    // TODO: needs to support doubles as well
    private void compareTwoNumbers(int instruction, MethodVisitor mv) {
        // we want to compare [a, b] which both are presumably java.lang.Long
        // we need to convert both into primitive long and compare
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
        mv.visitInsn(DUP2_X1);
        mv.visitInsn(POP2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
        mv.visitInsn(DUP2_X2);
        mv.visitInsn(POP2);

        var trueLabel = new Label();
        var endLabel = new Label();
        mv.visitInsn(LCMP);
        mv.visitJumpInsn(instruction, trueLabel);
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(endLabel);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
    }

    private void performNegation(MethodVisitor mv) {
        var trueLabel = new Label();
        var endLabel = new Label();
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "intValue", "()I", false);
        mv.visitJumpInsn(IFNE, trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(endLabel);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
    }

    @Override
    public void apply(MethodVisitor mv) {
        switch (operation) {
            case GREATER_THAN -> compareTwoNumbers(IFGT, mv);
            case GREATER_EQUAL -> compareTwoNumbers(IFGE, mv);
            case LESS_THAN -> compareTwoNumbers(IFLT, mv);
            case LESS_EQUAL -> compareTwoNumbers(IFLE, mv);
            case EQUAL -> compareTwoNumbers(IFEQ, mv);
            case NOT_EQUAL -> compareTwoNumbers(IFNE, mv);
            case NOT -> performNegation(mv);
        }
    }

    @Override
    public String toString() {
        return operation.name();
    }
}
