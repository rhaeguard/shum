package io.shum.language.type;

import java.util.Set;

public final class PrimitiveType implements Type {
    // considers int, double, str
    final ShumDataType type;

    public PrimitiveType(ShumDataType type) {
        if (!allowedTypes.contains(type)) {
            throw new IllegalArgumentException(String.format("Type %s is not a primitive type", type.name));
        }
        this.type = type;
    }

    private static final Set<ShumDataType> allowedTypes = Set.of(
            ShumDataType.INT,
            ShumDataType.DOUBLE,
            ShumDataType.STRING,
            ShumDataType.NOTHING
    );

    @Override
    public ShumDataType getTopLevelDataType() {
        return type;
    }
}
