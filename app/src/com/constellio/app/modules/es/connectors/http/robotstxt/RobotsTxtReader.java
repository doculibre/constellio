package com.constellio.app.modules.es.connectors.http.robotstxt;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.constellio.app.modules.es.connectors.http.robotstxt.URLDecoder.decode;

public class RobotsTxtReader {
    private final MatchingStrategy matchingStrategy;
    private final WinningStrategy winningStrategy;

    /**
     * Creates instance of the robots.txt reader with default strategies.
     */
    public RobotsTxtReader() {
        this(MatchingStrategy.DEFAULT, WinningStrategy.DEFAULT);
    }

    /**
     * Creates instance of the robots.txt reader.
     *
     * @param matchingStrategy
     * @param winningStrategy
     */
    public RobotsTxtReader(MatchingStrategy matchingStrategy, WinningStrategy winningStrategy) {
        this.matchingStrategy = matchingStrategy;
        this.winningStrategy = winningStrategy;
    }

    /**
     * Reads robots txt.
     *
     * @param inputStream input stream with robots.txt content.
     * @return parsed robots.txt
     * @throws IOException if reading stream fails
     */
    public RobotsTxt readRobotsTxt(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        Group currentGroup = null;
        boolean startGroup = false;
        RobotsTxtImpl robots = new RobotsTxtImpl(matchingStrategy, winningStrategy);

        for (Entry entry = readEntry(reader); entry != null; entry = readEntry(reader)) {
            switch (entry.getKey().toUpperCase()) {
                case "USER-AGENT":
                    if (!startGroup && currentGroup != null) {
                        robots.addGroup(currentGroup);
                        currentGroup = null;
                    }

                    if (currentGroup == null) {
                        currentGroup = new Group();
                    }

                    currentGroup.addUserAgent(entry.getValue());
                    startGroup = true;
                    break;

                case "DISALLOW":
                    if (currentGroup != null) {
                        boolean access = entry.getValue().isEmpty();
                        currentGroup.addAccess(new Access(entry.getSource(), entry.getValue(), access));
                        startGroup = false;
                    }
                    break;

                case "ALLOW":
                    if (currentGroup != null) {
                        boolean access = !entry.getValue().isEmpty();
                        currentGroup.addAccess(new Access(entry.getSource(), entry.getValue(), access));
                        startGroup = false;
                    }
                    break;

                case "CRAWL-DELAY":
                    if (currentGroup != null) {
                        try {
                            int crawlDelay = Integer.parseInt(entry.getValue());
                            currentGroup.setCrawlDelay(crawlDelay);
                            startGroup = false;
                        } catch (NumberFormatException ex) {
                        }
                    } else {
                    }
                    break;

                case "HOST":
                    robots.setHost(entry.getValue());
                    startGroup = false;
                    break;

                case "SITEMAP":
                    robots.getSitemaps().add(entry.getValue());
                    startGroup = false;
                    break;

                default:
                    startGroup = false;
                    break;
            }
        }

        if (currentGroup != null) {
            robots.addGroup(currentGroup);
        }

        return robots;
    }

    /**
     * Reads next entry from the reader.
     *
     * @return entry or <code>null</code> if no more data in the stream
     * @throws IOException if reading from stream fails
     */
    private Entry readEntry(BufferedReader reader) throws IOException {
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            Entry entry = parseEntry(line);
            if (entry != null) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Parses line into entry.
     * <p>
     * Skip empty lines. Skip comments. Skip invalid lines.
     *
     * @param line line to parse
     * @return entry or <code>null</code> if skipped
     * @throws IOException parsing line fails
     */
    private Entry parseEntry(String line) throws IOException {
        line = StringUtils.trimToEmpty(line);
        if (line.startsWith("#")) {
            return null;
        }

        int colonIndex = line.indexOf(":");
        if (colonIndex < 0) {
            return null;
        }

        String key = StringUtils.trimToNull(line.substring(0, colonIndex));
        if (key == null) {
            return null;
        }

        String rest = line.substring(colonIndex + 1, line.length());
        int hashIndex = rest.indexOf("#");

        String value = StringUtils.trimToEmpty(hashIndex >= 0 ? rest.substring(0, hashIndex) : rest);

        value = decode(value);

        return new Entry(line, key, value);
    }

    /**
     * Local implementation of Map.Entry interface.
     */
    private final static class Entry {

        private final String source;
        private final String key;
        private final String value;

        public Entry(String source, String key, String value) {
            this.source = source;
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getSource() {
            return source;
        }

        @Override
        public String toString() {
            return source;
        }
    }
}
