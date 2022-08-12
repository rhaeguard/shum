package io.shum.language;

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

    public static boolean isDoubleQuoted(String str) {
        return str.startsWith("\"") && str.endsWith("\"");
    }

    public static boolean isSingleQuoted(String str) {
        return str.startsWith("'") && str.endsWith("'");
    }
}
