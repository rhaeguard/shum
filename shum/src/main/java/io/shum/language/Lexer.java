package io.shum.language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class Lexer {

    private Token convertTokenToInstruction(String token) {
        if (token.startsWith("\"") && token.endsWith("\"") || token.startsWith("'") && token.endsWith("'")) {
            return new Token(token, TokenType.VALUE);
        }
        if (Utils.isFloatingPoint(token)) {
            return new Token(token, TokenType.VALUE);
        }

        if (TokenType.KNOWN_KEYWORDS.containsKey(token)) {
            return new Token(token, TokenType.KNOWN_KEYWORDS.get(token));
        }

        return new Token(token, TokenType.FUNCTION_INVOCATION);
    }

    public List<Token> lex(File file) {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return lines
                    .map(String::trim)
                    .map(line -> line.split("//")[0])
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