package io.shum.language;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @Test
    void isProperFunctionName() {
        Map<String, Boolean> input = new HashMap<>();
        input.put("foo", true);
        input.put("foo_bar", true);
        input.put("f223232_dasdad_1231231", true);
        input.put("1foo", false);
        input.put("foo!", false);
        input.put("$foo", false);
        input.put("fo00", true);
        input.put("foo-bar-baz", true);
        input.put("f0000912312939213912931293912931", true);

        input.forEach((k, v) ->
                assertEquals(v,
                        Utils.isProperFunctionName(k), format("Expected %s for the input '%s'", v.toString(), k)));

    }
}