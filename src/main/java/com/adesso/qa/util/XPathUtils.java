package com.adesso.qa.util;

/**
 * Small helpers for safely building dynamic XPath expressions from
 * arbitrary Java strings (e.g. test data such as owner or pet names).
 *
 * <p>Pure, stateless, and driver-independent by design, so it can be unit
 * tested directly without a {@code WebDriver} and reused across any Page
 * Object that needs to embed user-supplied values in an XPath.</p>
 */
public final class XPathUtils {

    private XPathUtils() {
        // utility class, not meant to be instantiated
    }

    /**
     * Converts a Java string into a safe XPath string literal, correctly
     * handling values that themselves contain single and/or double quotes
     * (e.g. names like {@code O'Brien}) via XPath's {@code concat()}
     * function, since XPath 1.0 has no escape sequence.
     *
     * @param value the raw value to embed in an XPath expression; must not
     *              be {@code null}
     * @return an XPath expression fragment that evaluates to {@code value},
     *         safe to splice directly into a larger XPath string
     */
    public static String literal(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        String[] parts = value.split("'", -1);
        StringBuilder literal = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            literal.append("'").append(parts[i]).append("'");
            if (i < parts.length - 1) {
                literal.append(", \"'\", ");
            }
        }
        return literal.append(")").toString();
    }
}