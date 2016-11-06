package com.constellio.model.services.contents.icap;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IcapResponse {

    private static class Builder {

        private int httpStatusCode;

        private boolean timedout;

        private final Map<String, String> headers = new TreeMap<>();

        public Builder statusCode(final int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        public Builder timedout(final boolean timedout) {
            this.timedout = timedout;
            return this;
        }

        public Builder header(final String nom, final String valeur) {
            this.headers.put(nom, valeur);
            return this;
        }

        public IcapResponse build() {
            return new IcapResponse(this);
        }
    }

    private static final String ICAP_HEADER_X_VIRUS_NAME = "X-Virus-Name";

    private static final String ICAP_HEADER_X_VIRUS_ID = "X-Virus-ID";

    private static final String ICAP_HEADER_X_BLOCK_REASON = "X-Block-Reason";

    private static final String ICAP_HEADER_X_WWBLOCK_RESULT = "X-WWBlockResult";

    private static final String ICAP_HEADER_PREVIEW = "Preview";

    private static final String ICAP_HEADER_ENCAPSULATED = "Encapsulated";

    private static final String ICAP_HEADER_SEPARATOR = ":";

    private static final Pattern STATUS_CODE_PATTERN = Pattern.compile("^ICAP/\\d+\\.\\d+ (\\d+) .*$");

    public static IcapResponse parse(final InputStream inputStream) throws IOException {
        Validate.notNull(inputStream);

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        final Builder builder = new Builder();

        try {
            parseStatutCode(bufferedReader, builder);

            parseHeaders(bufferedReader, builder);

            if (hasEncapsuledHeader(builder)) {
                parseEncapsulatedSection(bufferedReader, builder);
            }
        } catch (final SocketTimeoutException e) {
            builder.timedout(true);
        }

        return builder.build();
    }

    private static void parseStatutCode(final BufferedReader bufferedReader, final Builder builder) throws IOException {
        final Matcher matcher = STATUS_CODE_PATTERN.matcher(bufferedReader.readLine());

        if (matcher.matches()) {
            builder.statusCode(Integer.parseInt(matcher.group(1)));
        }
    }

    private static void parseHeaders(final BufferedReader bufferedReader, final Builder builder) throws IOException {
        String line = bufferedReader.readLine();

        while (StringUtils.isNotEmpty(line)) {
            final String[] header = line.split(ICAP_HEADER_SEPARATOR);

            builder.header(header[0].trim(), header[1].trim());

            line = bufferedReader.readLine();
        }
    }

    private static boolean hasEncapsuledHeader(final Builder builder) {
        return builder.headers.containsKey(ICAP_HEADER_ENCAPSULATED);
    }

    private static void parseEncapsulatedSection(final BufferedReader bufferedReader, final Builder builder) throws IOException {
        if (hasEncapsulatedRequestHeader(builder)) {
            parseEncapsulatedHeader(bufferedReader, builder);
        }

        if (hasEncapsulatedResponseHeader(builder)) {
            parseEncapsulatedHeader(bufferedReader, builder);
        }

        if (hasEncapsulatedBody(builder)) {
            parseEncapsulatedBody(bufferedReader, builder);
        }
    }

    private static boolean hasEncapsulatedRequestHeader(final Builder builder) {
        return builder.headers.get(ICAP_HEADER_ENCAPSULATED).contains("req-hdr");
    }

    private static void parseEncapsulatedHeader(final BufferedReader bufferedReader, final Builder builder) throws IOException {
        String ligne = bufferedReader.readLine();

        while (StringUtils.isNotEmpty(ligne)) {
            ligne = bufferedReader.readLine();
        }
    }

    private static boolean hasEncapsulatedResponseHeader(final Builder builder) {
        return builder.headers.get(ICAP_HEADER_ENCAPSULATED).contains("res-hdr");
    }

    private static boolean hasEncapsulatedBody(final Builder builder) {
        return !builder.headers.get(ICAP_HEADER_ENCAPSULATED).contains("null-body");
    }

    private static void parseEncapsulatedBody(final BufferedReader bufferedReader, final Builder builder) throws IOException {
        String ligne = bufferedReader.readLine();

        while (StringUtils.isNotEmpty(ligne)) {
            ligne = bufferedReader.readLine();
        }
    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc3507#section-4.3.3">rfc3507#section-4.3.3</a>
     */
    private final int httpStatusCode;

    private final boolean timedout;

    private final Map<String, String> headers;

    private IcapResponse(final Builder builder) {
        httpStatusCode = builder.httpStatusCode;
        timedout = builder.timedout;
        headers = Collections.unmodifiableMap(new TreeMap<>(builder.headers));
    }

    public boolean isTimedout() {
        return timedout;
    }

    public Integer getPreviewLength() {
        try {
            return Integer.valueOf(headers.get(ICAP_HEADER_PREVIEW));
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    public boolean isClear() {
        return HttpStatus.SC_NO_CONTENT == httpStatusCode;
    }

    boolean isMoreThanPreviewScanNeeded() {
        return HttpStatus.SC_CONTINUE == httpStatusCode;
    }

    public String getThreatDescription() {
        final StringBuilder description = new StringBuilder();

        if (headers.containsKey(ICAP_HEADER_X_VIRUS_ID)) {
            description.append("Virus found - " + headers.get(ICAP_HEADER_X_VIRUS_ID));
        } else if (headers.containsKey(ICAP_HEADER_X_VIRUS_NAME)) {
            description.append("Virus found - " + headers.get(ICAP_HEADER_X_VIRUS_NAME));
        }

        if (headers.containsKey(ICAP_HEADER_X_BLOCK_REASON)) {
            description.append(headers.get(ICAP_HEADER_X_BLOCK_REASON) + " - " + headers.get(ICAP_HEADER_X_WWBLOCK_RESULT));
        }

        return description.toString();
    }

}
