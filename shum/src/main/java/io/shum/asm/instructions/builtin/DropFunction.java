package io.shum.asm.instructions.builtin;

import org.objectweb.asm.MethodVisitor;

public final class DropFunction implements BuiltInFunctionCall{

    @Override
    public void apply(MethodVisitor mv) {
        mv.visitInsn(POP);
    }

    @Override
    public String toString() {
        return "drop";
    }
}
