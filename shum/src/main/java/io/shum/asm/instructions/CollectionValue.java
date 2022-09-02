package io.shum.asm.instructions;

import io.shum.language.ShumDataType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public final class CollectionValue implements Instruction {

    public enum CollectionType {
        LIST, SET, DICT
    }

    private final CollectionType type;
    private final ShumDataType elementType;
    private final List<Constant> elements;

    public CollectionValue(CollectionType type, ShumDataType elementType, List<Constant> elements) {
        this.type = type;
        this.elementType = elementType;
        this.elements = elements;
    }

    @Override
    public void apply(MethodVisitor mv) {
        if (type == CollectionType.LIST) {
            mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 1);
            for (var element : elements) {
                mv.visitVarInsn(ALOAD, 1);
                element.apply(mv); // this will load the variable onto the stack
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                mv.visitInsn(POP);
            }
            mv.visitVarInsn(ALOAD, 1);
        } else if (type == CollectionType.SET) {
            mv.visitTypeInsn(Opcodes.NEW, "java/util/HashSet");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 1);
            for (var element : elements) {
                mv.visitVarInsn(ALOAD, 1);
                element.apply(mv); // this will load the variable onto the stack
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true);
                mv.visitInsn(POP);
            }
            mv.visitVarInsn(ALOAD, 1);
        }
    }

    @Override
    public String toString() {
        return String.format("%s[%s] = [%s]", type, elementType, elements.toString());
    }
}
