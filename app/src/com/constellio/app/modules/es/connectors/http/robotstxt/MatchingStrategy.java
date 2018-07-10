package com.constellio.app.modules.es.connectors.http.robotstxt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.constellio.app.modules.es.connectors.http.robotstxt.URLDecoder.decode;
import static com.constellio.app.modules.es.connectors.http.robotstxt.WildcardsCompiler.compile;

public interface MatchingStrategy {
    /**
     * Matches given path with a pattern.
     * @param pattern pattern
     * @param pathToTest path to test
     * @return <code>true</code> if match
     */
    boolean matches(String pattern, String pathToTest);

    /**
     * This strategy recognizes (*) and ($) as wildcards.
     */
    public static final MatchingStrategy DEFAULT = new MatchingStrategy() {
        @Override
        public boolean matches(String pattern, String pathToTest) {
            if (pathToTest==null) return false;
            if (pattern==null || pattern.isEmpty()) return true;

            String relativePath = decode(pathToTest);
    /*
    if (pattern.endsWith("/") && !relativePath.endsWith("/")) {
      relativePath += "/";
    }
    */
            Pattern pt = compile(pattern);
            Matcher matcher = pt.matcher(relativePath);
            return matcher.find() && matcher.start()==0;
        }
    };
}
