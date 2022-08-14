package io.shum.asm.instructions;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public final class LoopStatement implements Instruction {
    private final MacroDeclaration condition;
    private final MacroDeclaration body;

    public LoopStatement(MacroDeclaration condition, MacroDeclaration body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void apply(MethodVisitor mv) {
        var conditionLabel = new Label();
        var endLabel = new Label();
        // condition check
        mv.visitLabel(conditionLabel);
        condition.apply(mv); // this is expected to produce a Boolean result
        // convert Boolean to int
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        mv.visitJumpInsn(IFEQ, endLabel);
        // if false jump end
        // true body
        body.apply(mv);
        // jump to condition
        mv.visitJumpInsn(GOTO, conditionLabel);
        // end label
        mv.visitLabel(endLabel);
        // the rest
    }

    @Override
    public String toString() {
        return "LoopStatement{" +
                "condition=" + condition +
                ", body=" + body +
                '}';
    }
}
