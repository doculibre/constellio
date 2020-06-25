package com.constellio.model.services.emails;

public class EmailServicesException extends Exception {
	public EmailServicesException(Exception e) {
		super(e);
	}

	public EmailServicesException(String s) {
		super(s);
	}

	public static class EmailServerException extends EmailServicesException {
		public EmailServerException(Exception e) {
			super(e);
		}
	}

	public static class EmailTempException extends EmailServicesException {
		public EmailTempException(Exception e) {
			super(e);
		}

	}

	public static class EmailPermanentException extends EmailServicesException {
		public EmailPermanentException(Exception e) {
			super(e);
		}
	}

	public static class NullEmailServerException extends EmailServicesException {
		public NullEmailServerException() {
			super("Null email server exception");
		}
	}

	public static class CannotSendEmailException extends EmailServicesException {
		public CannotSendEmailException() {
			super("Cannot send email exception");
		}
	}
}
