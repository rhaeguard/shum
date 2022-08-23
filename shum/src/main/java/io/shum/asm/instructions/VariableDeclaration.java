package io.shum.asm.instructions;

import io.shum.language.ShumDataType;
import org.objectweb.asm.MethodVisitor;

public final class VariableDeclaration implements Instruction {

    private final String name;
    private final ShumDataType dataType;

    public String getName() {
        return name;
    }

    public ShumDataType getDataType() {
        return dataType;
    }

    public VariableDeclaration(String name, ShumDataType dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    @Override
    public void apply(MethodVisitor mv) {
        // TODO
        throw new RuntimeException("Unsupported operation");
    }

}
