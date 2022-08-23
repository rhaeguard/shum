package io.shum.asm.instructions;

import io.shum.Compiler;
import io.shum.asm.Context;
import io.shum.utils.Maybe;
import org.objectweb.asm.MethodVisitor;

public final class VariableOperation implements Instruction {
    public enum Operation {
        LOAD, STORE
    }

    private final Operation operation;
    private final String name;
    private final Context context;
    private String functionName;

    public VariableOperation(Operation operation, String name, Context context) {
        this.operation = operation;
        this.name = name;
        this.context = context;
    }

    public void withFunction(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public void apply(MethodVisitor mv) {
        if (functionName == null) {
            var vd = getVariableDeclaration();
            if (operation == Operation.LOAD) {
                mv.visitFieldInsn(GETSTATIC, Compiler.MAIN_CLASS_NAME, name, vd.getDataType().jvmType);
            } else if (operation == Operation.STORE) {
                // assuming that there already exists an appropriate value at the top of the stack
                mv.visitFieldInsn(PUTSTATIC, Compiler.MAIN_CLASS_NAME, name, vd.getDataType().jvmType);
            } else {
                throw new RuntimeException("Unsupported operation: " + operation);
            }
        } else {
            var maybeFd = context.getFunctionDeclaration(functionName);
            if (maybeFd instanceof Maybe.Some<FunctionDeclaration> sfd) {
                apply(mv, sfd.getValue());
            } else {
                throw new RuntimeException("Unknown function: " + functionName);
            }
        }
    }

    private void apply(MethodVisitor mv, FunctionDeclaration fd) {
        var maybeVd = fd.getVariable(name);
        final VariableDeclaration vd;
        if (maybeVd instanceof Maybe.Some<VariableDeclaration> svd) {
            vd = svd.getValue();
        } else {
            throw new RuntimeException("Unknown variable: " + name);
        }

        int localIndex = vd.getLocalVariableIndex();

        if (operation == Operation.LOAD) {
            mv.visitVarInsn(ALOAD, localIndex);
        } else if (operation == Operation.STORE) {
            mv.visitVarInsn(ASTORE, localIndex);
        } else {
            throw new RuntimeException("Unsupported operation: " + operation);
        }
    }

    private VariableDeclaration getVariableDeclaration() {
        var maybeVD = context.getVariableDeclaration(name);
        if (maybeVD instanceof Maybe.Some<VariableDeclaration> svd) {
            return svd.getValue();
        } else {
            throw new RuntimeException("Missing variable declaration info for: " + name);
        }
    }


}
