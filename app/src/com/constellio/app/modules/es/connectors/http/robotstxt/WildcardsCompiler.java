package com.constellio.app.modules.es.connectors.http.robotstxt;

import java.util.regex.Pattern;

public class WildcardsCompiler {
    /**
     * Compiles wildcard pattern into a regular expression.
     * <p>
     * Allowed wildcards:<br>
     * <br>
     * &nbsp;&nbsp;&nbsp;* - matches any sequence of characters<br>
     * &nbsp;&nbsp;&nbsp;$ - matches end of sequence<br>
     * @param patternWithWildcards pattern with wildcards
     * @return compiled pattern
     */
    public static Pattern compile(String patternWithWildcards) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<patternWithWildcards.length(); i++) {
            char c = patternWithWildcards.charAt(i);
            switch (c) {
                case '*':
                    sb.append(".*");
                    break;
                case '$':
                    if (i==patternWithWildcards.length()-1) {
                        sb.append(c);
                    } else {
                        sb.append("[").append(c).append("]");
                    }
                    break;
                case '[':
                case ']':
                    sb.append("[").append("\\").append(c).append("]");
                    break;
                default:
                    sb.append("[").append(c).append("]");
            }
        }
        return Pattern.compile(sb.toString());
    }
}
