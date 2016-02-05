package com.constellio.model.conf.ldap;

public class InvalidUrlRuntimeException extends RuntimeException {
    private final String url;

    public InvalidUrlRuntimeException(String url, String message) {
        super(message + " with url " + url);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
