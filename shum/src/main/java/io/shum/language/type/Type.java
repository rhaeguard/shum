package io.shum.language.type;

public sealed interface Type permits PrimitiveType, ContainerType {

    ShumDataType getTopLevelDataType();

    static Type intType() {
        return new PrimitiveType(ShumDataType.INT);
    }

    static Type doubleType() {
        return new PrimitiveType(ShumDataType.DOUBLE);
    }

    static Type stringType() {
        return new PrimitiveType(ShumDataType.STRING);
    }

    static Type nothingType() {
        return new PrimitiveType(ShumDataType.NOTHING);
    }
}