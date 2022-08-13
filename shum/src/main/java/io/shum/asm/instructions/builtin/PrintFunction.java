package io.shum.asm.instructions.builtin;

import org.objectweb.asm.MethodVisitor;

public final class PrintFunction implements BuiltInFunctionCall {

    @Override
    public void apply(MethodVisitor mv) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // we need to swap because println takes System.out as [this] pointer
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
    }

    @Override
    public String toString() {
        return "print";
    }
}
