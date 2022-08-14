package io.shum.asm.instructions;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;

final public class CastingInstructions implements Opcodes {

    public final static Consumer<MethodVisitor> BOOLEAN_TO_OBJECT = mv -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);

    public final static Consumer<MethodVisitor> INT_TO_LONG_OBJECT = mv -> {
        mv.visitInsn(I2L);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
    };

    public final static Consumer<MethodVisitor> LONG_OBJECT_TO_INT = mv -> {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "intValue", "()I", false);
    };

    public final static Consumer<MethodVisitor> NOTHING = mv -> {};
}
