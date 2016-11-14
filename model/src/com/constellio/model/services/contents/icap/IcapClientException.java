package com.constellio.model.services.contents.icap;

public class IcapClientException extends RuntimeException {

	public IcapClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public IcapClientException(String message) {
		super(message);
	}

	public static class IcapCommunicationFailure extends IcapClientException {
        private static final String MESSAGE_KEY = "icap.analysis.communicationFailure";
        
		public IcapCommunicationFailure(final Throwable cause) {
			super(MESSAGE_KEY, cause);
		}
	}

    public static class IcapScanTimedout extends IcapClientException {

        private static final String MESSAGE_KEY = "icap.analysis.timedout";

        public IcapScanTimedout() {
            super(MESSAGE_KEY);
        }

    }

    public static class IcapScanThreatFound extends IcapClientException {

        private static final String MESSAGE_KEY = "icap.analysis.threatFound";

        private final String threatName;

        public IcapScanThreatFound(final String threatName) {
            super(MESSAGE_KEY);

            this.threatName = threatName;
        }

        public String getThreatName() {
            return threatName;
        }

    }

}
