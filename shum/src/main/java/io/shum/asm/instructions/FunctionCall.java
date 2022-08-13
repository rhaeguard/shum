package io.shum.asm.instructions;

import io.shum.asm.instructions.builtin.BuiltInFunctionCall;

public sealed interface FunctionCall
        extends Instruction
        permits UserDefinedFunctionCall, BuiltInFunctionCall {
}
