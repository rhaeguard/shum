package io.shum.asm.instructions;

import io.shum.language.type.ContainerType;
import io.shum.language.type.ShumDataType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public final class CollectionValue implements Value {

    private final ContainerType type;
    private final List<Value> elements;
    private int localVariableIndex = 1;

    public CollectionValue(ContainerType type, List<Value> elements) {
        this.type = type;

//        if (type.elementType.getTopLevelDataType() != ShumDataType.NOTHING) {
//            var elementType = type.elementType.getTopLevelDataType();
//            for (var element : elements) {
//                if (element.getDataType() != elementType) {
//                    throw new RuntimeException("All the elements of the collection need to be of type: " + element.getDataType().name);
//                }
//            }
//        }

        this.elements = elements;
    }

    @Override
    public void apply(MethodVisitor mv) {
        if (type.containerType == ShumDataType.LIST) {
            mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, localVariableIndex);
            for (var element : elements) {
                mv.visitVarInsn(ALOAD, localVariableIndex);
                element.apply(mv); // this will load the variable onto the stack
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                mv.visitInsn(POP);
            }
            mv.visitVarInsn(ALOAD, localVariableIndex);
        } else if (type.containerType == ShumDataType.SET) {
            mv.visitTypeInsn(Opcodes.NEW, "java/util/HashSet");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashSet", "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, localVariableIndex);
            for (var element : elements) {
                mv.visitVarInsn(ALOAD, localVariableIndex);
                element.apply(mv); // this will load the variable onto the stack
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true);
                mv.visitInsn(POP);
            }
            mv.visitVarInsn(ALOAD, localVariableIndex);
        }
    }

    @Override
    public String toString() {
        return String.format("%s = [%s]", type, elements.toString());
    }
}
