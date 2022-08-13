package io.shum.language;

import java.util.regex.Pattern;

public class Utils {
    public static boolean isInteger(String token) {
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isFloatingPoint(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isProperFunctionName(String token) {
        // a-zA-Z_0-9-
        var compile = Pattern.compile("[a-zA-Z_\\-][a-zA-Z_0-9\\-]*");
        return compile.matcher(token).matches();
    }

    public static boolean isDoubleQuoted(String str) {
        return str.startsWith("\"") && str.endsWith("\"");
    }

    public static boolean isSingleQuoted(String str) {
        return str.startsWith("'") && str.endsWith("'");
    }
}
