package com.constellio.app.modules.es.connectors.smb.service;

import com.constellio.app.modules.es.connectors.spi.LoggedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nicolas on 2016-02-25.
 */
public class SmbLoggedException extends Exception implements LoggedException {
    private String url;

    public SmbLoggedException(String url, String message) {
        super(message);
        this.url = url;
    }

    public SmbLoggedException(String url, String message, Throwable cause) {
        super(message, cause);
        this.url = url;
    }

    public SmbLoggedException(String url, Throwable cause) {
        super(cause);
        this.url = url;
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("url", url);
        return parameters;
    }
}
