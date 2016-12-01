package com.constellio.model.services.contents.icap;

public class IcapException extends RuntimeException {

    public static class CommunicationFailure extends IcapException {

        private static final String MESSAGE_KEY = "icap.analysis.communicationFailure";

        public CommunicationFailure(final Throwable cause, final String fileName) {
            super(MESSAGE_KEY, cause, fileName);
        }

    }

    public static class TimeoutException extends IcapException {

        private static final String MESSAGE_KEY = "icap.analysis.timedout";

        public TimeoutException(final String fileName) {
            super(MESSAGE_KEY, fileName);
        }

    }

    public static class ThreatFoundException extends IcapException {

        private static final String MESSAGE_KEY = "icap.analysis.threatFound";

        private final String threatName;

        public ThreatFoundException(final String threatName, final String fileName) {
            super(MESSAGE_KEY, fileName);

            this.threatName = threatName;
        }

        public String getThreatName() {
            return threatName;
        }

    }

    private String fileName;

    IcapException(final String message, final String fileName) {
        super(message);

        this.fileName = fileName;
    }

    IcapException(final String message, final Throwable cause, final String fileName) {
        super(message, cause);

        this.fileName = fileName;
	}

	public IcapException(final String message) {
		super(message);
	}

    public IcapException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public String getFileName() {
        return fileName;
    }

}
