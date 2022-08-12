package io.shum.asm.instructions;

import org.objectweb.asm.MethodVisitor;

public final class BinaryArithmeticFunctionCall implements ArithmeticFunctionCall {

    public enum Operation {
        ADD(LADD, DADD), SUB(LSUB, DSUB), MUL(LMUL, DMUL), DIV(LDIV, DDIV), REM(LREM, DREM), POW(NOP, NOP);

        final int longOpCode;
        final int doubleOpCode;

        Operation(int longOpCode, int doubleOpCode) {
            this.longOpCode = longOpCode;
            this.doubleOpCode = doubleOpCode;
        }
    }

    private final Operation operation;

    public BinaryArithmeticFunctionCall(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void apply(MethodVisitor mv) {

        switch (operation) {
            case ADD, SUB, MUL, DIV, REM -> executeForLong(mv);
            case POW -> {
                // [num, pow] -> both are expected to be long
                mv.visitInsn(DUP_X1);
                // -> [pow, num, pow]
                mv.visitInsn(POP);
                // -> [pow, num]
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "doubleValue", "()D", false);
                // -> [pow, num.double]
                mv.visitInsn(DUP2_X1);
                // -> [num.double, pow, num.double]
                mv.visitInsn(POP2);
                // -> [num.double, pow]
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "doubleValue", "()D", false);
                // -> [num.double, pow.double]
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
                // -> [res.double]
                mv.visitInsn(D2L);
                // -> [res.long]
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                // -> [res.Long]
            }
        }
    }

    // TODO: does not work for doubles
    // TODO: implement for doubles
    private void executeForLong(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
        mv.visitInsn(DUP2_X1);
        mv.visitInsn(POP2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
        mv.visitInsn(DUP2_X2);
        mv.visitInsn(POP2);
        mv.visitInsn(operation.longOpCode);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
    }

    @Override
    public String toString() {
        return operation.name();
    }
}
