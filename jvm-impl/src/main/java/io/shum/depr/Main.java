package io.shum.depr;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static org.objectweb.asm.Opcodes.*;

interface Instruction {
    public void apply(MethodVisitor mv);
}

enum ShumDataType {
    INT, DOUBLE, STRING, BOOL
}

class Constant implements Instruction, Opcodes {
    final ShumDataType dataType;
    final String value;

    public Constant(String value, ShumDataType dataType) {
        this.dataType = dataType;
        this.value = value;
    }

    @Override
    public void apply(MethodVisitor mv) {
        switch (dataType) {
            case INT -> {
                mv.visitIntInsn(BIPUSH, Integer.parseInt(value));
            }
            default -> throw new UnsupportedOperationException();
        }
    }
}

class FunctionCall implements Instruction, Opcodes {
    final String name;

    public FunctionCall(String name) {
        this.name = name;
    }

    @Override
    public void apply(MethodVisitor mv) {
        if ("show".equals(name)) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V");
        } else if ("+".equals(name)) {
            mv.visitInsn(IADD);
        }
    }
}

public class Main {

    public static void main(String[] args) throws IOException {
        var instructions = new ArrayList<Instruction>();
        instructions.add(new Constant("1", ShumDataType.INT));
        instructions.add(new Constant("2", ShumDataType.INT));
        instructions.add(new FunctionCall("+"));
        instructions.add(new FunctionCall("show"));

        ClassWriter cw = new ClassWriter(0);

        MethodVisitor mv;
        String name = "Hello";
        cw.visit(52, ACC_PUBLIC + ACC_SUPER, name, null, "java/lang/Object", null);
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

            for (Instruction instruction : instructions) {
                instruction.apply(mv);
            }
            mv.visitInsn(RETURN); //add return instruction

            mv.visitMaxs(1000, (int) 10); //set max stack and max local variables
            mv.visitEnd();
        }
        cw.visitEnd();

        OutputStream os = new FileOutputStream("Hello.class");
        os.write(cw.toByteArray());
        os.close();
    }

}

interface StackObject {
}

abstract class ValueObject implements StackObject {
}

abstract class NumberObject extends ValueObject {

    public abstract int getIntValue();

    @Override
    public String toString() {
        return String.valueOf(this.getIntValue());
    }
}

class IntObject extends NumberObject {
    private final int data;

    public IntObject(int data) {
        this.data = data;
    }

    @Override
    public int getIntValue() {
        return this.data;
    }
}


