package io.shum.language.type;

import java.util.Set;

public final class ContainerType implements Type {
    // considers set, list, dict
    public final ShumDataType containerType;
    public final Type elementType;

    public ContainerType(ShumDataType containerType, Type elementType) {
        if (!allowedTypes.contains(containerType)) {
            throw new IllegalArgumentException(String.format("Type %s is not a valid container type", containerType.name));
        }
        this.containerType = containerType;
        this.elementType = elementType;
    }

    private static final Set<ShumDataType> allowedTypes = Set.of(
            ShumDataType.LIST,
            ShumDataType.SET
    );

    @Override
    public ShumDataType getTopLevelDataType() {
        return containerType;
    }
}
