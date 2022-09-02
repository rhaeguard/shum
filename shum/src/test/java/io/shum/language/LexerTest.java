package io.shum.language;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    @Test
    void shellSplit() {
        // [1, 2, 3, 4] print
        // "hello" "world" print
        // 1 2 '"3"' 4 print
        var result = Lexer.shellSplit("[1, 2, 3, 4] print\n\"hello\" \"world\" print\n1 2 '\"3\"' 4 print");
        assertEquals(10, result.size());

        var expectedResults = List.of(
                "[1, 2, 3, 4]", "print", "\"hello\"", "\"world\"", "print", "1", "2", "'\"3\"'", "4", "print"
        );
        for (int i = 0; i < expectedResults.size(); i++) {
            assertEquals(expectedResults.get(i), result.get(i));
        }
    }
}