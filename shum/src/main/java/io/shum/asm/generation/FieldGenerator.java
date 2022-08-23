package io.shum.asm.generation;

import io.shum.asm.instructions.VariableDeclaration;
import org.objectweb.asm.ClassWriter;

import static org.objectweb.asm.Opcodes.*;

public class FieldGenerator {

    private final ClassWriter cw;

    FieldGenerator(ClassWriter cw) {
        this.cw = cw;
    }

    public void generate(VariableDeclaration vd) {
        int access = ACC_PUBLIC + ACC_STATIC;
        var name = vd.getName();
        var type = vd.getDataType().jvmType;
        final var fv = cw.visitField(
                access,
                name,
                type,
                null,
                null
        );
        fv.visitEnd();
    }

}
