package io.shum.asm.instructions;

import io.shum.language.type.ShumDataType;
import org.objectweb.asm.MethodVisitor;

public final class Constant implements Instruction {

    private final ShumDataType dataType;
    private final String value;

    public Constant(ShumDataType dataType, String value) {
        this.dataType = dataType;
        this.value = value;
    }

    public ShumDataType getDataType() {
        return dataType;
    }

    @Override
    public void apply(MethodVisitor mv) {
        switch (dataType) {
            case INT -> {
                mv.visitLdcInsn(Long.parseLong(value));
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            }
            case DOUBLE -> {
                mv.visitLdcInsn(Double.parseDouble(value));
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            }
            case STRING -> mv.visitLdcInsn(value);
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", dataType, value);
    }
}
