package io.shum.language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

import static io.shum.language.TokenType.isKnownKeyword;
import static io.shum.language.Utils.isFloatingPoint;
import static io.shum.language.Utils.isQuotedString;

public class Lexer {

    private final File file;

    public Lexer(File file) {
        this.file = file;
    }

    private Token convertTokenToInstruction(String token) {
        if (isQuotedString(token)) return new Token(token, TokenType.VALUE);
        if (isFloatingPoint(token)) return new Token(token, TokenType.VALUE);
        if (isKnownKeyword(token)) return new Token(token, TokenType.getKeyword(token));

        if (token.endsWith("@")) {
            return new Token(token.substring(0, token.length() - 1), TokenType.VARIABLE_LOAD);
        }
        if (token.endsWith("!")) {
            return new Token(token.substring(0, token.length() - 1), TokenType.VARIABLE_STORE);
        }

        if (token.startsWith("[") && token.endsWith("]")) {
            return new Token(token.substring(1, token.length() - 1), TokenType.LIST_NOTATION);
        } else if (token.startsWith("(") && token.endsWith(")")) {
            return new Token(token.substring(1, token.length() - 1), TokenType.SET_NOTATION);
        } else if (token.startsWith("{") && token.endsWith("}")) {
            return new Token(token.substring(1, token.length() - 1), TokenType.DICT_NOTATION);
        }

        return new Token(token, TokenType.FUNCTION_INVOCATION);
    }

    public List<Token> lex() {
        try (var lines = Files.lines(file.toPath())) {
            return lexLines(lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Token> lex(String input) {
        try (var lines = Arrays.stream(input.split(System.lineSeparator()))) {
            return lexLines(lines);
        }
    }

    private List<Token> lexLines(Stream<String> lines) {
        return lines
                .map(String::trim)
                .map(line -> {
                    var pieces = line.split("//"); // ignore comments
                    if (pieces.length == 0) {
                        return null;
                    }
                    return pieces[0];
                })
                .filter(Objects::nonNull)
                .map(Lexer::shellSplit)
                .flatMap(Collection::stream)
                .map(this::convertTokenToInstruction)
                .toList();
    }

    // Reference: https://gist.github.com/raymyers/8077031
    static List<String> shellSplit(CharSequence string) {
        var tokens = new ArrayList<String>();

        boolean escaping = false;
        char quoteChar = ' ';
        boolean quoting = false;
        int lastCloseQuoteIndex = Integer.MIN_VALUE;

        var current = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (escaping) {
                current.append(c);
                escaping = false;
            } else if (c == '\\' && !(quoting && quoteChar == '\'')) {
                escaping = true;
            } else if (quoting && (c == quoteChar || (c == ']' || c == ')'))) {
                quoting = false;
                lastCloseQuoteIndex = i;
                current.append(c);
            } else if (!quoting && (c == '\'' || c == '"' || c == '[' || c == '(')) {
                quoting = true;
                quoteChar = c;
                current.append(c);
            } else if (!quoting && Character.isWhitespace(c)) {
                if (current.length() > 0 || lastCloseQuoteIndex == (i - 1)) {
                    tokens.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0 || lastCloseQuoteIndex == (string.length() - 1)) {
            tokens.add(current.toString());
        }

        return tokens;
    }

}