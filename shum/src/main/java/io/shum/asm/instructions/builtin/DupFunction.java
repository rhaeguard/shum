package io.shum.asm.instructions.builtin;

import io.shum.asm.instructions.FunctionCall;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;

public final class DupFunction implements BuiltInFunctionCall {

    private final DupOperation operation;

    private DupFunction(DupOperation operation) {
        this.operation = operation;
    }

    private enum DupOperation {
        DUP_1(DUP, "dup"),
        DUP_2(DUP2, "dup2"),
        DUP_1_OVER_1(DUP_X1, "dup1over1"),
        DUP_1_OVER_2(DUP_X2, "dup1over2"),
        DUP_2_OVER_1(DUP2_X1, "dup2over1"),
        DUP_2_OVER_2(DUP2_X2, "dup2over2");

        final int instruction;
        final String name;

        DupOperation(int instruction, String name) {
            this.instruction = instruction;
            this.name = name;
        }
    }

    @Override
    public void apply(MethodVisitor mv) {
        mv.visitInsn(operation.instruction);
    }

    @Override
    public String toString() {
        return operation.name;
    }

    public static Map<String, Supplier<FunctionCall>> getAllDupOperations() {
        return Arrays.stream(DupOperation.values())
                .collect(toMap(
                        e -> e.name,
                        e -> () -> new DupFunction(e)
                ));
    }

}
