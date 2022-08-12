package io.shum.language;

import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public enum TokenType {
    VALUE, FUNCTION_INVOCATION,
    DEF("def", true),
    FUNC("func", true),
    EQUAL("=", true),
    L_CURLY("{", true),
    R_CURLY("}", true),
    END(".", true);

    public final String representation;
    public final boolean isKeyword;

    TokenType() {
        this(null, false);
    }

    TokenType(String representation, boolean isKeyword) {
        this.representation = representation;
        this.isKeyword = isKeyword;
    }

    public static final Map<String, TokenType> KNOWN_KEYWORDS =
            Arrays.stream(TokenType.values())
                    .filter(t -> t.isKeyword)
                    .collect(toMap(t -> t.representation, t -> t));
}
