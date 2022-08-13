package io.shum.asm.instructions.builtin;

import org.objectweb.asm.MethodVisitor;

public final class DupFunction implements BuiltInFunctionCall {
    @Override
    public void apply(MethodVisitor mv) {
        mv.visitInsn(DUP);
    }

    @Override
    public String toString() {
        return "dup";
    }
}
