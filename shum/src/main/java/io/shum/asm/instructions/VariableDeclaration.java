package io.shum.asm.instructions;

import io.shum.language.type.Type;
import org.objectweb.asm.MethodVisitor;

public final class VariableDeclaration implements Instruction, WithScope {

    private final String name;
    private final Type dataType;
    private final Value initialValue;
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
        this(name, dataType, null);
    }

    public VariableDeclaration(String name, Type dataType, Value initialValue) {
        this.name = name;
        this.dataType = dataType;
        this.initialValue = initialValue;
    }

    @Override
    public void apply(MethodVisitor mv) {
        this.variableIndex = this.scope.nextIndex(this);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, this.variableIndex);
    }

    public void setVariableIndex(int index) {
        this.variableIndex = index;
    }

    @Override
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Value getInitialValue() {
        return initialValue;
    }

    public boolean hasInitialValue() {
        return initialValue != null;
    }
}
