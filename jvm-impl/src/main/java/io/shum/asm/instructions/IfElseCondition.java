package io.shum.asm.instructions;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public final class IfElseCondition implements Instruction {

    private final MacroDeclaration trueBranch;
    private final MacroDeclaration falseBranch;

    public IfElseCondition(MacroDeclaration trueBranch, MacroDeclaration falseBranch) {
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public void apply(MethodVisitor mv) {
        var trueLabel = new Label();
        var endLabel = new Label();
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        mv.visitJumpInsn(IFNE, trueLabel);
        falseBranch.apply(mv);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        trueBranch.apply(mv);
        mv.visitLabel(endLabel);
    }

    @Override
    public String toString() {
        return String.format("if\n\t{ %s }\nelse\n\t{ %s }", trueBranch, falseBranch);
    }

}
