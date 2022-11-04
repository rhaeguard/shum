package io.shum.asm.instructions.builtin;

import io.shum.asm.instructions.FunctionCall;
import io.shum.asm.instructions.Instruction;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.shum.asm.instructions.CastingInstructions.*;
import static java.util.Map.entry;

public final class CollectionFunction implements BuiltInFunctionCall {
    private final Function function;

    private CollectionFunction(Function function) {
        this.function = function;
    }

    @Override
    public void apply(MethodVisitor mv) {
        function.apply(mv);
    }

    public static Map<String, Supplier<FunctionCall>> getAllCollectionFunctions() {
        return Map.ofEntries(
                entry("append", () -> new CollectionFunction(Function.APPEND)),
                entry("get", () -> new CollectionFunction(Function.LIST_GET)),
                entry("set", () -> new CollectionFunction(Function.LIST_SET)),
                entry("size", () -> new CollectionFunction(Function.SIZE)),
                entry("deleteElement", () -> new CollectionFunction(Function.DELETE_ELEMENT)),
                entry("deleteAtPos", () -> new CollectionFunction(Function.DELETE_AT))
        );
    }

    public enum Function {
        LIST_GET("java/util/List", "get", "(I)Ljava/lang/Object;", LONG_OBJECT_TO_INT, NOTHING),
        SIZE("java/util/Collection", "size", "()I", NOTHING, INT_TO_LONG_OBJECT),
        DELETE_AT("java/util/List", "remove", "(I)Ljava/lang/Object;", LONG_OBJECT_TO_INT, POP_TOP),
        DELETE_ELEMENT("java/util/Collection", "remove", "(Ljava/lang/Object;)Z", NOTHING, POP_TOP),
        APPEND(mv -> {
            var className = "java/util/Collection";

            mv.visitInsn(DUP);
            mv.visitTypeInsn(INSTANCEOF, className);

            var trueLabel = new Label();
            var endLabel = new Label();
            mv.visitJumpInsn(IFNE, trueLabel);
            mv.visitMethodInsn(INVOKEINTERFACE, className, "add", "(Ljava/lang/Object;)Z", true);
            mv.visitJumpInsn(GOTO, endLabel);
            mv.visitLabel(trueLabel);
            mv.visitMethodInsn(INVOKEINTERFACE, className, "addAll", "(Ljava/util/Collection;)Z", true);
            mv.visitLabel(endLabel);
            POP_TOP.accept(mv);
        }),
        LIST_SET(mv -> {
            // push index
            // push element
            mv.visitInsn(SWAP);
            LONG_OBJECT_TO_INT.accept(mv);
            mv.visitInsn(SWAP);
            // set
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", true);
            // drop return
            mv.visitInsn(POP);
        });

        private final ExistingJavaMethod existingJavaMethod;
        private final Consumer<MethodVisitor> customOperation;

        Function(String className, String methodName, String descriptor, Consumer<MethodVisitor> preprocess, Consumer<MethodVisitor> postprocess) {
            this.existingJavaMethod = new ExistingJavaMethod(className, methodName, descriptor, preprocess, postprocess);
            this.customOperation = null;
        }

        Function(Consumer<MethodVisitor> customOperation) {
            this.customOperation = customOperation;
            this.existingJavaMethod = null;
        }

        public void apply(MethodVisitor mv) {
            if (existingJavaMethod != null) {
                existingJavaMethod.apply(mv);
            } else if (customOperation != null) {
                customOperation.accept(mv);
            }
        }

        private record ExistingJavaMethod(String className,
                                          String functionName,
                                          String functionDescriptor,
                                          Consumer<MethodVisitor> preprocess,
                                          Consumer<MethodVisitor> postprocess) {

            public void apply(MethodVisitor mv) {
                this.preprocess.accept(mv);
                mv.visitMethodInsn(INVOKEINTERFACE, className, functionName, functionDescriptor, true);
                this.postprocess.accept(mv);
            }

        }
    }
}
