package io.shum.asm.instructions.builtin;

import io.shum.asm.instructions.CastingInstructions;
import io.shum.asm.instructions.FunctionCall;
import io.shum.asm.instructions.Instruction;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;

public final class LogicalFunction implements BuiltInFunctionCall {

    private final LogicalOperation operation;

    public LogicalFunction(LogicalOperation op) {
        this.operation = op;
    }

    public enum LogicalOperation {
        AND(IAND, "and"),
        OR(IOR, "or"),
        XOR(IXOR, "xor");

        final int instruction;
        final String name;

        LogicalOperation(int instruction, String name) {
            this.instruction = instruction;
            this.name = name;
        }
    }


    @Override
    public void apply(MethodVisitor mv) {
        CastingInstructions.BOOL_OBJECT_BOOL_PRIMITIVE.accept(mv);
        mv.visitInsn(SWAP);
        CastingInstructions.BOOL_OBJECT_BOOL_PRIMITIVE.accept(mv);
        mv.visitInsn(SWAP);
        mv.visitInsn(operation.instruction);
        CastingInstructions.BOOLEAN_TO_OBJECT.accept(mv);
    }

    public static Map<String, Supplier<FunctionCall>> getAllLogicalOperators() {
        return Arrays.stream(LogicalOperation.values())
                .collect(toMap(
                        e -> e.name,
                        e -> () -> new LogicalFunction(e)
                ));
    }
}
