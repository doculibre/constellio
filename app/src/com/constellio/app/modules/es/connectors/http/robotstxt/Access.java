package com.constellio.app.modules.es.connectors.http.robotstxt;

public class Access {
    public static final Access ALLOW = new Access("Allow: /", "/", true, true);
    public static final Access DISALLOW = new Access("Disallow: /", "/", false, true);

    private final String source;
    private final boolean constant;
    private final String accessPath;
    private final boolean accessAllowed;

    /**
     * Creates instance of the access.
     * @param source source of the information
     * @param accessPath access path
     * @param accessAllowed access to the path
     */
    public Access(String source, String accessPath, boolean accessAllowed) {
        this(source, accessPath, accessAllowed, false);
    }

    /**
     * Creates instance of the access.
     * @param source source of the definition
     * @param accessPath access path
     * @param accessAllowed access to the path
     * @param constant indicates if this is constant
     */
    Access(String source, String accessPath, boolean accessAllowed, boolean constant) {
        this.source = source;
        this.constant = constant;
        this.accessPath = accessPath;
        this.accessAllowed = accessAllowed;
    }

    /**
     * Gets path.
     * @return path
     */
    public String getPath() {
        return accessPath;
    }

    /**
     * Check if this section gives an access
     * @return
     */
    public boolean hasAccess() {
        return accessAllowed;
    }

    /**
     * Checks if path matches access path
     * @param path path to check
     * @param matchingStrategy matcher
     * @return <code>true</code> if path matches access path
     */
    public boolean matches(String path, MatchingStrategy matchingStrategy) {
        return path!=null && matchingStrategy.matches(accessPath, path);
    }

    @Override
    public String toString() {
        return source;
    }
}
