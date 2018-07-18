package com.constellio.app.modules.es.connectors.http.robotstxt;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class RobotsTxt {
    /**
     * Checks access to the given HTTP path.
     * @param userAgent user agent to be used evaluate authorization
     * @param path path to access
     * @return <code>true</code> if there is an access to the requested path
     */
    public abstract boolean query(String userAgent, String path);

    /**
     * Gets host.
     * @return host or <code>null</code> if no host declared
     */
    public abstract String getHost();

    /**
     * Gets site maps.
     * @return list of site map URL's.
     */
    public abstract List<String> getSitemaps();

    /**
     * Gets a list of disallowed resources.
     * @param userAgent user agent
     * @return list of disallowed resources
     */
    public abstract List<String> getDisallowList(String userAgent);

    /**
     * Gets time when robot.txt was fetched.
     * @return
     */
    public abstract DateTime getFetchTime();

    /**
     * Reads robots.txt available at the URL.
     * @param input stream of content
     * @return parsed robots.txt object
     * @throws IOException if unable to read content.
     */
    public static RobotsTxt read(InputStream input) throws IOException {
        RobotsTxtReader reader = new RobotsTxtReader();
        return reader.readRobotsTxt(input);
    }
}
