package com.constellio.model.services.contents.icap;

public class IcapScanException extends Exception {

    public IcapScanException(final String message) {
        super(message);
    }

    public IcapScanException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
