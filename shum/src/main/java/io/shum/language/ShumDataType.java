package io.shum.language;

import java.util.Arrays;
import java.util.Optional;

public enum ShumDataType {
    INT("int", "Ljava/lang/Long;"),
    DOUBLE("double", "Ljava/lang/Double;"),
    STRING("string", "Ljava/lang/String;"),
    LIST("list", "Ljava/util/List;"),
    SET("set", "Ljava/util/Set;");

    public final String name;
    public final String jvmType;

    ShumDataType(String name, String jvmType) {
        this.name = name;
        this.jvmType = jvmType;
    }

    public static boolean contains(String dataTypeName) {
        if (isCollection(dataTypeName)) {
            return getCollectionTypeParameter(dataTypeName)
                    .map(typeParameter -> Arrays.stream(values()).anyMatch(d -> d.name.equals(typeParameter)))
                    .orElse(false);
        }
        return Arrays.stream(values()).anyMatch(d -> d.name.equals(dataTypeName));
    }

    public static ShumDataType getDataType(String name) {
        if (isCollection(name)) {
            if (contains(name)) {
                // basically remove the type parameter
                return switch (name.substring(0, name.indexOf('['))) {
                    case "list" -> LIST;
                    case "set" -> SET;
                    default -> throw new RuntimeException("Not a collection type: " + name);
                };
            }
        }

        return Arrays.stream(values())
                .filter(d -> d.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unsupported data type: " + name));
    }

    private static boolean isCollection(String str) {
        return str.startsWith("list") || str.startsWith("set");
    }

    private static Optional<String> getCollectionTypeParameter(String dataTypeName) {
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