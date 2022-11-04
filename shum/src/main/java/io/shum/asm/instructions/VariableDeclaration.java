package io.shum.asm.instructions;

import io.shum.language.type.Type;
import org.objectweb.asm.MethodVisitor;

public final class VariableDeclaration implements Instruction, WithScope {

    private final String name;
    private final Type dataType;
    // used for local variables
    // it is -1 for static variable
    private int variableIndex = -1;

    private Scope scope;

    public String getName() {
        return name;
    }

    public Type getDataType() {
        return dataType;
    }

    public int getVariableIndex() {
        return variableIndex;
    }

    public VariableDeclaration(String name, Type dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    @Override
    public void apply(MethodVisitor mv) {
        this.variableIndex = this.scope.nextIndex(this);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, this.variableIndex);
    }

    @Override
    public void setScope(Scope scope) {
        this.scope = scope;
    }

}
