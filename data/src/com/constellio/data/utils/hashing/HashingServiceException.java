package com.constellio.data.utils.hashing;

@SuppressWarnings("serial")
public class HashingServiceException extends Exception {

	public HashingServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public HashingServiceException(String message) {
		super(message);
	}

	public HashingServiceException(Throwable cause) {
		super(cause);
	}

	public static class Timeout extends HashingServiceException {

		public Timeout(int timeout) {
			super("Could not calculate hash in less than " + timeout + "ms");
		}

	}

	public static class CannotReadContent extends HashingServiceException {

		public CannotReadContent(Throwable cause) {
			super("Could not read content", cause);
		}

	}

	public static class CannotHashContent extends HashingServiceException {

		public CannotHashContent(Throwable cause) {
			super("Could not hash content", cause);
		}

	}

}
