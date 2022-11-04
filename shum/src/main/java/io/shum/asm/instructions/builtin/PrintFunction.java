package io.shum.asm.instructions.builtin;

import org.objectweb.asm.MethodVisitor;

public final class PrintFunction implements BuiltInFunctionCall {


    public enum Operation {
        PRINT, PUTS
    }

    private final Operation operation;

    public PrintFunction(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void apply(MethodVisitor mv) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // we need to swap because println takes System.out as [this] pointer
        mv.visitInsn(SWAP);
        switch (operation) {
            case PRINT -> mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
            case PUTS -> mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/Object;)V", false);
        }
    }

    @Override
    public String toString() {
        return "print";
    }
}
