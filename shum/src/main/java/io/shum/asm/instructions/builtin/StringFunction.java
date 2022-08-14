package io.shum.asm.instructions.builtin;

import io.shum.asm.instructions.FunctionCall;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.shum.asm.instructions.CastingInstructions.*;

public final class StringFunction implements BuiltInFunctionCall {

    private final StringOperation operation;

    public StringFunction(StringOperation operation) {
        this.operation = operation;
    }

    @Override
    public void apply(MethodVisitor mv) {
        operation.apply(mv);
    }

    public static Map<String, Supplier<FunctionCall>> getAllSupportedJavaStringOperations() {
        return Arrays.stream(StringOperation.values())
                .collect(Collectors.toMap(
                        e -> e.shortName,
                        e -> () -> new StringFunction(e)
                ));
    }


    public enum StringOperation {
        UPPER("upper", "toUpperCase", "()Ljava/lang/String;"),
        LOWER("lower", "toLowerCase", "()Ljava/lang/String;"),
        TRIM("trim", "trim", "()Ljava/lang/String;"),
        STARTS_WITH("startsWith", "startsWith", "(Ljava/lang/String;)Z", NOTHING, BOOLEAN_TO_OBJECT),
        ENDS_WITH("endsWith", "endsWith", "(Ljava/lang/String;)Z", NOTHING, BOOLEAN_TO_OBJECT),
        IS_EMPTY("isEmpty", "isEmpty", "()Z", NOTHING, BOOLEAN_TO_OBJECT),
        IS_BLANK("isBlank", "isBlank", "()Z", NOTHING, BOOLEAN_TO_OBJECT),
        LENGTH("len", "length", "()I", NOTHING, INT_TO_LONG_OBJECT),
        CONTAINS("contains", "contains", "(Ljava/lang/CharSequence;)Z", NOTHING, BOOLEAN_TO_OBJECT),
        SUBSTRING("substr", "substring", "(II)Ljava/lang/String;", mv -> {
            LONG_OBJECT_TO_INT.accept(mv);
            mv.visitInsn(SWAP);
            LONG_OBJECT_TO_INT.accept(mv);
            mv.visitInsn(SWAP);
        }, NOTHING),
        CONCAT("++", "concat", "(Ljava/lang/String;)Ljava/lang/String;"),
        /* Custom methods using existing Java methods */
        IS_UPPER("isUpper", mv -> {
            mv.visitInsn(DUP);
            UPPER.apply(mv);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            BOOLEAN_TO_OBJECT.accept(mv);
        }),
        IS_LOWER("isLower", mv -> {
            mv.visitInsn(DUP);
            LOWER.apply(mv);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            BOOLEAN_TO_OBJECT.accept(mv);
        });

        final String shortName;
        final ExistingJavaMethod existingJavaMethod;
        final Consumer<MethodVisitor> extendedJavaMethod;

        StringOperation(String shortName, String originalFunctionName, String functionDescriptor) {
            this(shortName, originalFunctionName, functionDescriptor, NOTHING, NOTHING);
        }

        StringOperation(String shortName,
                        String originalFunctionName,
                        String functionDescriptor,
                        Consumer<MethodVisitor> preprocess,
                        Consumer<MethodVisitor> postprocess
        ) {
            this.shortName = shortName;
            this.existingJavaMethod = new ExistingJavaMethod(
                    originalFunctionName, functionDescriptor, preprocess, postprocess
            );
            this.extendedJavaMethod = null;
        }

        StringOperation(String shortName, Consumer<MethodVisitor> extendedJavaMethod) {
            this.shortName = shortName;
            this.existingJavaMethod = null;
            this.extendedJavaMethod = extendedJavaMethod;
        }

        public void apply(MethodVisitor mv) {
            if (existingJavaMethod != null) {
                existingJavaMethod.apply(mv);
            } else if (extendedJavaMethod != null) {
                extendedJavaMethod.accept(mv);
            }
        }

        record ExistingJavaMethod(String originalFunctionName,
                                  String functionDescriptor,
                                  Consumer<MethodVisitor> preprocess,
                                  Consumer<MethodVisitor> postprocess) {

            public void apply(MethodVisitor mv) {
                this.preprocess.accept(mv);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", this.originalFunctionName, this.functionDescriptor, false);
                this.postprocess.accept(mv);
            }

        }
    }
}
