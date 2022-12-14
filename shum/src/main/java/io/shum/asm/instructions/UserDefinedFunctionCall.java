package io.shum.asm.instructions;

import io.shum.asm.Context;
import io.shum.asm.instructions.builtin.*;
import io.shum.utils.Maybe;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.shum.Compiler.MAIN_CLASS_NAME;
import static java.util.Map.entry;

public final class UserDefinedFunctionCall implements FunctionCall {

    private final String functionName;
    private final Context context;

    private UserDefinedFunctionCall(String functionName, Context context) {
        this.functionName = functionName;
        this.context = context;
    }

    public static FunctionCall createFunctionCall(String functionName, Context context) {
        if (PROVIDED_FUNCTIONS.containsKey(functionName)) {
            return PROVIDED_FUNCTIONS.get(functionName).get();
        }
        return new UserDefinedFunctionCall(functionName, context);
    }

    @Override
    public void apply(MethodVisitor mv) {
        // TODO: class name is hardcoded
        // TODO: method descriptor may not always be the same ()V
        // TODO: methods may not always be 'static'

        var maybeFD = context.getFunctionDeclaration(functionName);
        if (maybeFD instanceof Maybe.Some<FunctionDeclaration> fd) {
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    MAIN_CLASS_NAME,
                    functionName,
                    fd.getValue().getDescriptor(),
                    false
            );
        } else {
            var maybeMD = context.getMacroDeclaration(functionName);
            if (maybeMD instanceof Maybe.Some<MacroDeclaration> md) {
                md.getValue().apply(mv);
            } else {
                throw new RuntimeException("There is no function or macro available with the name: "+ functionName);
            }
        }

    }

    private static final Map<String, Supplier<FunctionCall>> PROVIDED_FUNCTIONS = getAllProvidedFunctions();

    private static Map<String, Supplier<FunctionCall>> getAllProvidedFunctions() {
        Map<String, Supplier<FunctionCall>> someEntries =  Map.ofEntries(
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
                //      bitwise operations
                entry("<<", () -> new BitwiseFunctionCall(BitwiseFunctionCall.Operation.SHIFT_LEFT)),
                entry(">>", () -> new BitwiseFunctionCall(BitwiseFunctionCall.Operation.SHIFT_RIGHT)),
                entry("&", () -> new BitwiseFunctionCall(BitwiseFunctionCall.Operation.BITWISE_AND)),
                entry("|", () -> new BitwiseFunctionCall(BitwiseFunctionCall.Operation.BITWISE_OR)),
                // printing
                entry("print", () -> new PrintFunction(PrintFunction.Operation.PRINT)),
                entry("puts", () -> new PrintFunction(PrintFunction.Operation.PUTS)),
                // other crucial functions
                entry("swap", SwapFunction::new),
                entry("drop", DropFunction::new),
                entry("toInt", () -> new CastingFunctionCall(CastingFunctionCall.Operation.CAST_INT))
        );

        var allFunc = new HashMap<>(someEntries);
        allFunc.putAll(StringFunction.getAllSupportedJavaStringOperations());
        allFunc.putAll(DupFunction.getAllDupOperations());
        allFunc.putAll(LogicalFunction.getAllLogicalOperators());
        allFunc.putAll(CollectionFunction.getAllCollectionFunctions());
        return allFunc;
    }

    @Override
    public String toString() {
        return functionName;
    }
}
