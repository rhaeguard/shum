package io.shum.asm.instructions;

import io.shum.asm.Context;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public final class LoopStatement implements Instruction, WithScope {
    private final List<Instruction> condition;
    private final List<Instruction> body;
    private Scope scope;

    public LoopStatement(List<Instruction> condition, List<Instruction> body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void apply(MethodVisitor mv) {
        var conditionLabel = new Label();
        var endLabel = new Label();
        // condition check
        mv.visitLabel(conditionLabel);
        if (this.scope == null) {
            this.scope = new Scope(0);
        }
        condition.stream().filter(c -> c instanceof WithScope).forEach(c -> ((WithScope) c).setScope(this.scope));
        condition.forEach(c -> c.apply(mv)); // TODO: better way?
        // convert Boolean to int
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        mv.visitJumpInsn(IFEQ, endLabel);
        // if false jump end
        // true body
        body.stream().filter(c -> c instanceof WithScope).forEach(c -> ((WithScope) c).setScope(this.scope));
        body.forEach(c -> c.apply(mv)); // TODO: better way?
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

    @Override
    public void setScope(Scope scope) {
        this.scope = scope;
    }

}
