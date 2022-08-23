package io.shum.language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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

        return new Token(token, TokenType.FUNCTION_INVOCATION);
    }

    public List<Token> lex() {
        try (Stream<String> lines = Files.lines(file.toPath())) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Reference: https://gist.github.com/raymyers/8077031
    private static List<String> shellSplit(CharSequence string) {
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
            } else if (quoting && c == quoteChar) {
                quoting = false;
                lastCloseQuoteIndex = i;
                current.append(c);
            } else if (!quoting && (c == '\'' || c == '"')) {
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