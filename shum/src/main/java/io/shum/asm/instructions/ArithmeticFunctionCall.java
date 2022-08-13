package io.shum.asm.instructions;

public sealed interface ArithmeticFunctionCall
        extends FunctionCall
        permits UnaryArithmeticFunctionCall, BinaryArithmeticFunctionCall {
}
