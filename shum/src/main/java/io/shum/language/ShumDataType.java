package io.shum.language;

import java.util.Arrays;

public enum ShumDataType {
    INT("int", "Ljava/lang/Long;"),
    DOUBLE("double", "Ljava/lang/Double;"),
    STRING("string", "Ljava/lang/String;");

    public final String name;
    public final String jvmType;

    ShumDataType(String name, String jvmType) {
        this.name = name;
        this.jvmType = jvmType;
    }

    public static boolean contains(String dataTypeName) {
        return Arrays.stream(values()).anyMatch(d -> d.name.equals(dataTypeName));
    }

    public static ShumDataType getDataType(String name) {
        return Arrays.stream(values())
                .filter(d -> d.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unsupported data type: " + name));
    }
}