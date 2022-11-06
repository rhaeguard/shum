package io.shum.asm.instructions;

import io.shum.Compiler;
import io.shum.asm.Context;
import io.shum.language.type.ShumDataType;
import io.shum.utils.Maybe;
import org.objectweb.asm.MethodVisitor;

// TODO: calling variable operation as Value is not a good idea, imo
// TODO: maybe create a DynamicValue class that will wrap the VariableOperation?
public final class VariableOperation implements Instruction, WithScope {

    public enum Operation {
        LOAD, STORE
    }

    private final Operation operation;
    private final String name;
    private final Context context;
    private Scope scope;

    public VariableOperation(Operation operation, String name, Context context) {
        this.operation = operation;
        this.name = name;
        this.context = context;
    }

    @Override
    public void apply(MethodVisitor mv) {
        if (scope != null) {
            // if scope exists look for the variable in the local context
            var vd = scope.getLocalVariable(name);
            if (vd != null) {
                int localIndex = vd.getVariableIndex();
                if (operation == Operation.LOAD) {
                    mv.visitVarInsn(ALOAD, localIndex);
                } else if (operation == Operation.STORE) {
                    mv.visitTypeInsn(CHECKCAST, vd.getDataType().getTopLevelDataType().getClassName());
                    mv.visitVarInsn(ASTORE, localIndex);
                } else {
                    throw new RuntimeException("Unsupported operation: " + operation);
                }
            } else {
                handleStaticVariable(mv);
            }
        } else {
            handleStaticVariable(mv);
        }
    }

    private void handleStaticVariable(MethodVisitor mv) {
        // look for the variable in the global context
        var shumDataType = getVariableDeclaration();
        if (operation == Operation.LOAD) {
            mv.visitFieldInsn(GETSTATIC, Compiler.MAIN_CLASS_NAME, name, shumDataType.jvmType);
        } else if (operation == Operation.STORE) {
            // assuming that there already exists an appropriate value at the top of the stack
            mv.visitTypeInsn(CHECKCAST, shumDataType.getClassName());
            mv.visitFieldInsn(PUTSTATIC, Compiler.MAIN_CLASS_NAME, name, shumDataType.jvmType);
        } else {
            throw new RuntimeException("Unsupported operation: " + operation);
        }
    }

    @Override
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    private ShumDataType getVariableDeclaration() {
        var maybeVD = context.getVariableDeclaration(name);
        if (maybeVD instanceof Maybe.Some<VariableDeclaration> svd) {
            return svd.getValue().getDataType().getTopLevelDataType();
        } else {
            throw new RuntimeException("Missing variable declaration info for: " + name);
        }
    }


}
