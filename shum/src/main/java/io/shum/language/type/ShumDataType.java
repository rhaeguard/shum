package io.shum.language.type;

import java.util.Arrays;
import java.util.Optional;

public enum ShumDataType {
    INT("int", "Ljava/lang/Long;"),
    DOUBLE("double", "Ljava/lang/Double;"),
    STRING("string", "Ljava/lang/String;"),
    LIST("list", "Ljava/util/List;"),
    SET("set", "Ljava/util/Set;"),
    DICT("dict", "Ljava/util/Map;"),
    NOTHING("_", "_");

    public final String name;
    public final String jvmType;

    ShumDataType(String name, String jvmType) {
        this.name = name;
        this.jvmType = jvmType;
    }

    static Optional<ShumDataType> fromStringTypeParameter(final String typeParam) {
        return Arrays.stream(values())
                .filter(e -> e.name.equalsIgnoreCase(typeParam))
                .findFirst();
    }

    public static Type getDataType(String name) {
        if (isCollection(name)) {
            var maybeContainerElementType = getCollectionTypeParameter(name);
            final ShumDataType elementDataType;
            final String containerTypeString;
            if (maybeContainerElementType.isEmpty()) {
                elementDataType = NOTHING;
                containerTypeString = name;
            } else {
                elementDataType = fromStringTypeParameter(maybeContainerElementType.get())
                        .orElseThrow(() -> new RuntimeException("Unsupported container element data type: " + name));
                containerTypeString = name.substring(0, name.indexOf('['));
            }
            // basically remove the type parameter
            return switch (containerTypeString) {
                case "list" -> new ContainerType(LIST, new PrimitiveType(elementDataType));
                case "set" -> new ContainerType(SET, new PrimitiveType(elementDataType));
                default -> throw new RuntimeException("Not a collection type: " + name);
            };
        }
        return Arrays.stream(values())
                .filter(d -> d.name.equalsIgnoreCase(name))
                .findFirst()
                .map(PrimitiveType::new)
                .orElseThrow(() -> new RuntimeException("Unsupported data type: " + name));
    }

    private static boolean isCollection(String str) {
        // TODO: only considers list or set
        return str.startsWith("list") || str.startsWith("set");
    }

    private static Optional<String> getCollectionTypeParameter(String dataTypeName) {
        // TODO: only considers list or set
        int start = dataTypeName.startsWith("list") ? 4 : 3;
        var substring = dataTypeName.substring(start);
        if (substring.startsWith("[") && substring.endsWith("]")) {
            var typeParameter = substring.substring(1, substring.length() - 1);
            return Optional.of(typeParameter);
        }
        return Optional.empty();
    }

    public String getClassName() {
        return this.jvmType.substring(1).replace(";", "");
    }
}