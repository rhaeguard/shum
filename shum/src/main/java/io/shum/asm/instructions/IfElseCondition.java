package io.shum.asm.instructions;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public final class IfElseCondition implements Instruction, WithScope {

    private final List<Instruction> trueBranch;
    private final List<Instruction> falseBranch;

    private Scope scope;

    public IfElseCondition(List<Instruction> trueBranch, List<Instruction> falseBranch) {
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public void apply(MethodVisitor mv) {
        var trueLabel = new Label();
        var endLabel = new Label();
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        mv.visitJumpInsn(IFNE, trueLabel);
        if (this.scope == null) {
            this.scope = new Scope(0);
        }
        falseBranch.stream().filter(b -> b instanceof WithScope).forEach(b -> ((WithScope) b).setScope(this.scope.cloneScope()));
        falseBranch.forEach(b -> b.apply(mv)); // TODO: make better?
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        trueBranch.stream().filter(b -> b instanceof WithScope).forEach(b -> ((WithScope) b).setScope(this.scope.cloneScope()));
        trueBranch.forEach(b -> b.apply(mv)); // TODO: make better?
        mv.visitLabel(endLabel);
    }

    @Override
    public String toString() {
        return String.format("if\n\t{ %s }\nelse\n\t{ %s }", trueBranch, falseBranch);
    }

    @Override
    public void setScope(Scope scope) {
        this.scope = scope;
    }

}
