package io.shum.language;

import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public enum TokenType {
    VALUE, FUNCTION_INVOCATION, TYPE,
    ARROW("->", true),
    MACRO("macro", true),
    FUNC("func", true),
    RETURN("return", true),
    EQUAL("=", true),
    L_CURLY("{", true),
    R_CURLY("}", true),
    END("end", true),
    IF("if", true),
    ELSE("else", true),
    LOOP("loop", true),
    DO("do", true),
    BREAK("break", true),
    LET("let", true),
    VARIABLE_LOAD, VARIABLE_STORE,
    LIST_NOTATION, SET_NOTATION, DICT_NOTATION;

    public final String representation;
    public final boolean isKeyword;

    TokenType() {
        this(null, false);
    }

    TokenType(String representation, boolean isKeyword) {
        this.representation = representation;
        this.isKeyword = isKeyword;
    }

    public static boolean isKnownKeyword(String token) {
        return KNOWN_KEYWORDS.containsKey(token);
    }

    public static TokenType getKeyword(String token) {
        return KNOWN_KEYWORDS.get(token);
    }

    public static final Map<String, TokenType> KNOWN_KEYWORDS =
            Arrays.stream(TokenType.values())
                    .filter(t -> t.isKeyword)
                    .collect(toMap(t -> t.representation, t -> t));
}
