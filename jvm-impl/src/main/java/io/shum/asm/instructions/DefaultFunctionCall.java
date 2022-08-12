package io.shum.asm.instructions;

import org.objectweb.asm.MethodVisitor;

import java.util.Map;
import java.util.function.Supplier;

import static java.util.Map.entry;

public final class DefaultFunctionCall implements FunctionCall {

    private final String functionName;

    private DefaultFunctionCall(String functionName) {
        this.functionName = functionName;
    }

    public static Supplier<FunctionCall> createFunctionCall(String functionName) {
        if (PROVIDED_FUNCTIONS.containsKey(functionName)) {
            return PROVIDED_FUNCTIONS.get(functionName);
        }
        return () -> new DefaultFunctionCall(functionName);
    }

    @Override
    public void apply(MethodVisitor mv) {
        // TODO: class name is hardcoded
        // TODO: method descriptor may not always be the same ()V
        // TODO: methods may not always be 'static'
        mv.visitMethodInsn(INVOKESTATIC, "DummyClass", functionName, "()V", false);
    }

    private static final Map<String, Supplier<FunctionCall>> PROVIDED_FUNCTIONS = Map.ofEntries(
            // unary functions
            entry("abs", () -> new UnaryArithmeticFunctionCall(UnaryArithmeticFunctionCall.Operation.ABS)),
            entry("neg", () -> new UnaryArithmeticFunctionCall(UnaryArithmeticFunctionCall.Operation.NEG)),
            entry("incr", () -> new UnaryArithmeticFunctionCall(UnaryArithmeticFunctionCall.Operation.INCR)),
            entry("decr", () -> new UnaryArithmeticFunctionCall(UnaryArithmeticFunctionCall.Operation.DECR)),
            // binary functions
            //      arithmetic operations
            entry("+", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.ADD)),
            entry("-", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.SUB)),
            entry("*", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.MUL)),
            entry("/", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.DIV)),
            entry("%", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.REM)),
            entry("pow", () -> new BinaryArithmeticFunctionCall(BinaryArithmeticFunctionCall.Operation.POW)),
            //      comparison operations
            entry(">", () -> new ComparisonFunctionCall(ComparisonFunctionCall.Operation.GREATER_THAN)),
            entry(">=", () -> new ComparisonFunctionCall(ComparisonFunctionCall.Operation.GREATER_EQUAL)),
            entry("<", () -> new ComparisonFunctionCall(ComparisonFunctionCall.Operation.LESS_THAN)),
            entry("<=", () -> new ComparisonFunctionCall(ComparisonFunctionCall.Operation.LESS_EQUAL)),
            entry("==", () -> new ComparisonFunctionCall(ComparisonFunctionCall.Operation.EQUAL)),
            entry("!=", () -> new ComparisonFunctionCall(ComparisonFunctionCall.Operation.NOT_EQUAL)),
            entry("not", () -> new ComparisonFunctionCall(ComparisonFunctionCall.Operation.NOT)),
            // printing
            entry("print", PrintCall::new),
            // other crucial functions
            entry("dup", () -> null),
            entry("swap", () -> null)

    );

    @Override
    public String toString() {
        return functionName;
    }
}
