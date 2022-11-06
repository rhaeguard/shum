package io.shum.asm.instructions;

public sealed interface Value extends Instruction permits CollectionValue, Constant, DynamicValue {
}
