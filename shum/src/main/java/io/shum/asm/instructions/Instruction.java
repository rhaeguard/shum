package io.shum.asm.instructions;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public sealed interface Instruction
        extends Opcodes
        permits Value, FunctionCall, FunctionDeclaration, IfElseCondition, LoopStatement, MacroDeclaration, VariableDeclaration {
    void apply(MethodVisitor mv);
}
