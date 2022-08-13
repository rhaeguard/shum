package io.shum.asm.instructions;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public sealed interface Instruction
        extends Opcodes
        permits Constant, FunctionCall, FunctionDeclaration, IfElseCondition, MacroDeclaration {
    void apply(MethodVisitor mv);

}
