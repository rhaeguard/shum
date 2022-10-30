package io.shum.asm.generation;

import io.shum.asm.instructions.FunctionDeclaration;
import io.shum.asm.instructions.Instruction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class MethodGenerator {

    private final ClassWriter cw;

    MethodGenerator(ClassWriter cw) {
        this.cw = cw;
    }

    public void generate(String methodName, String descriptor, List<Instruction> instructions) {
        var mv = new LocalVariablesSorter(
                ACC_PUBLIC + ACC_STATIC,
                descriptor,
                cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, descriptor, null, null)
        );

        for (var instruction : instructions) {
            instruction.apply(mv);
        }

        mv.visitInsn(RETURN); // add return instruction
        mv.visitEnd();
        mv.visitMaxs(1000, 10); // set max stack and max local variables
    }

    public void generate(FunctionDeclaration fd) {
        var descriptor = fd.getDescriptor();
        var methodName = fd.getName();

        var mv = new LocalVariablesSorter(
                ACC_PUBLIC + ACC_STATIC,
                descriptor,
                cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, descriptor, null, null)
        );

        // load all the parameters to the method call stack
        for (int i = 0; i < fd.getParameterCount(); i++) {
            mv.visitVarInsn(ALOAD, i);
        }

        // initialize all the variables
        for (var variable : fd.getVariables()) {
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE, variable.getLocalVariableIndex());
        }

        for (var instruction : fd.getInstructions()) {
            instruction.apply(mv);
        }

        if (fd.returns()) {
            mv.visitInsn(ARETURN); // add return instruction
        } else {
            mv.visitInsn(RETURN); // add return instruction
        }
        mv.visitEnd();
        mv.visitMaxs(1000, 10); // set max stack and max local variables
    }

}
