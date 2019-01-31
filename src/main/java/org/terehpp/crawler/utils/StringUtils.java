package org.terehpp.crawler.utils;

/**
 * String Utils.
 */
public class StringUtils {
    private StringUtils() {
    }

    /**
     * Check if String is 'empty'
     *
     * @param cs string.
     * @return Result of check.
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if String is not 'empty'
     *
     * @param cs string.
     * @return Result of check.
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }
}
