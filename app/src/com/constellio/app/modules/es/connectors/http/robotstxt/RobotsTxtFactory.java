package com.constellio.app.modules.es.connectors.http.robotstxt;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.concurrent.ConcurrentHashMap;

public class RobotsTxtFactory {
    private static final String SEPARATOR = "://";
    private static final String ROBOT_TXT_FILE = "robots.txt";
    private static final int HOURS = 24;
    private final ConcurrentHashMap<String, RobotsTxt> robotsTxt = new ConcurrentHashMap<>();

    public synchronized RobotsTxt getRobotsTxt(String url) {
        String baseUrl = getBaseUrl(url);
        if (baseUrl != null) {
            try {
                RobotsTxt robotsTxt = this.robotsTxt.get(baseUrl);
                if (robotsTxt == null || (robotsTxt.getFetchTime() != null && robotsTxt.getFetchTime().plusHours(HOURS).isBeforeNow())) {
                    URL base = new URL(baseUrl);

                    try (InputStream robotsTxtStream = new URL(base, ROBOT_TXT_FILE).openStream()) {
                        robotsTxt = RobotsTxt.read(robotsTxtStream);
                        this.robotsTxt.put(baseUrl, robotsTxt);
                    } catch (FileNotFoundException | UnknownServiceException e) {
                        robotsTxt = RobotsTxt.read(new ByteArrayInputStream(new byte[0]));
                        this.robotsTxt.put(baseUrl, robotsTxt);
                    }
                }
                return robotsTxt;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private String getBaseUrl(String url) {
        try {
            URL u = new URL(url);

            return StringUtils.join(new String[]{u.getProtocol(), u.getAuthority()}, SEPARATOR);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isAuthorizedPath(String url) {
        RobotsTxt robotsTxt = getRobotsTxt(url);
        if(robotsTxt != null) {
            return robotsTxt.query(null, url);
        }

        return true;
    }
}
