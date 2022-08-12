package io.shum.asm.instructions;

public sealed interface FunctionCall
        extends Instruction
        permits ArithmeticFunctionCall, ComparisonFunctionCall, DefaultFunctionCall, PrintCall {
}
