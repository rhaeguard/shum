package io.shum.asm.instructions.builtin;


public sealed interface ArithmeticFunctionCall
        extends BuiltInFunctionCall
        permits UnaryArithmeticFunctionCall, BinaryArithmeticFunctionCall {
}
