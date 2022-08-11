package io.shum;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class Compiler {

    public static void main(String[] args) {
        Compiler compiler = new Compiler();

        var instructions = List.<Instruction>of(
                new Constant(ShumDataType.INT, "16"), // weight
                new Constant(ShumDataType.INT, "2"), // height
                new Constant(ShumDataType.INT, "2"),  // power
                new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.POW),
                new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.DIV),
                new PrintCall(ShumDataType.INT)
        );

        compiler.compile(instructions, null);
    }

    public void compile(List<Instruction> instructions, Map<String, List<Instruction>> functionMap) {
        var classGenerator = new ClassGenerator("DummyClass");
        classGenerator.generate(instructions);
        classGenerator.saveToFile();
    }

}

class ClassGenerator {

    private final String className;
    private final ClassWriter cw;

    ClassGenerator(String className) {
        this.className = className;
        this.cw = new ClassWriter(0);
    }

    public void generate(List<Instruction> instructions) {
        cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);

        var mg = new MethodGenerator(cw);
        mg.generate("main", "([Ljava/lang/String;)V", instructions);

        cw.visitEnd();
    }

    public void saveToFile() {
        try (var os = new FileOutputStream(String.format("%s.class", className))) {
            os.write(cw.toByteArray());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

}

class MethodGenerator {

    private final ClassWriter cw;

    MethodGenerator(ClassWriter cw) {
        this.cw = cw;
    }

    public void generate(String methodName, String signature, List<Instruction> instructions) {
        var mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, signature, null, null);

        for (var instruction : instructions) {
            instruction.apply(mv);
        }
        mv.visitInsn(RETURN); //add return instruction

        mv.visitEnd();

        mv.visitMaxs(1000, (int) 10); //set max stack and max local variables
    }

}

sealed interface Instruction permits Constant, FunctionCall {
    void apply(MethodVisitor mv);
}

enum ShumDataType {
    INT, STRING
}

final class Constant implements Instruction, Opcodes {

    private final ShumDataType dataType;
    private final String value;

    public Constant(ShumDataType dataType, String value) {
        this.dataType = dataType;
        this.value = value;
    }

    @Override
    public void apply(MethodVisitor mv) {
        switch (dataType) {
            case INT -> mv.visitLdcInsn(Integer.parseInt(value));
            case STRING -> mv.visitLdcInsn(value);
        }
    }
}

sealed interface FunctionCall extends Instruction, Opcodes permits ArithmeticFunctionCall, PrintCall {
}

final class PrintCall implements FunctionCall {

    private final ShumDataType type;

    public PrintCall(ShumDataType type) {
        this.type = type;
    }

    @Override
    public void apply(MethodVisitor mv) {
        String descriptor = switch (type) {
            case STRING -> "Ljava/lang/String;";
            case INT -> "I";
        };
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitInsn(SWAP); // we need to swap because println takes System.out as [this] pointer
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", String.format("(%s)V", descriptor));
    }
}

sealed interface ArithmeticFunctionCall extends FunctionCall permits UnaryArithmeticFunctionCall, BinaryArithmeticFunctionCall {
}

final class UnaryArithmeticFunctionCall implements ArithmeticFunctionCall {
    public enum Operation {
        ABS, NEG, INCR, DECR, NOT
    }

    private final Operation operation;

    public UnaryArithmeticFunctionCall(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void apply(MethodVisitor mv) {
        switch (operation) {
            case ABS -> {
                // TODO: only works for ints now
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I");
            }
            case NEG -> {
                // TODO: only works for ints now
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "negateExact", "(I)I");
            }
            case INCR -> {
                // TODO: only works for ints now
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "incrementExact", "(I)I");
            }
            case DECR -> {
                // TODO: only works for ints now
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "decrementExact", "(I)I");
            }
            case NOT -> {
                Label trueLabel = new Label();
                Label endLabel = new Label();
                mv.visitJumpInsn(IFNE, trueLabel);
                mv.visitInsn(ICONST_1);
                mv.visitJumpInsn(GOTO, endLabel);
                mv.visitLabel(trueLabel);
                mv.visitInsn(ICONST_0);
                mv.visitLabel(endLabel);
            }
        }
    }
}

final class BinaryArithmeticFunctionCall implements ArithmeticFunctionCall {

    public enum Operation {
        ADD, SUB, MUL, DIV, REM, POW
    }

    private final Operation operation;

    public BinaryArithmeticFunctionCall(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void apply(MethodVisitor mv) {

        switch (operation) {
            case ADD -> mv.visitInsn(IADD);
            case SUB -> mv.visitInsn(ISUB);
            case MUL -> mv.visitInsn(IMUL);
            case DIV -> mv.visitInsn(IDIV);
            case REM -> mv.visitInsn(IREM);
            case POW -> {
                mv.visitIntInsn(ISTORE, 0);
                mv.visitInsn(I2D);
                mv.visitIntInsn(ILOAD, 0);
                mv.visitInsn(I2D);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D");
                mv.visitInsn(D2I);
            }
        }
    }
}