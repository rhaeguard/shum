package io.shum.asm.instructions.builtin;

import org.objectweb.asm.MethodVisitor;

public final class SwapFunction implements BuiltInFunctionCall {
    @Override
    public void apply(MethodVisitor mv) {
        mv.visitInsn(SWAP);
    }

    @Override
    public String toString() {
        return "swap";
    }
}
