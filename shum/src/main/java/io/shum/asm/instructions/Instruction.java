package io.shum.asm.instructions;

import io.shum.asm.instructions.builtin.StringFunction;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public sealed interface Instruction
        extends Opcodes
        permits Constant, FunctionCall, FunctionDeclaration, IfElseCondition, LoopStatement, MacroDeclaration {
    void apply(MethodVisitor mv);

}
