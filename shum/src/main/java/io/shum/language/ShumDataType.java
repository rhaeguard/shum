package io.shum.language;

public enum ShumDataType {
    INT("int"), DOUBLE("double"), STRING("string");

    final String name;

    ShumDataType(String name) {
        this.name = name;
    }
}