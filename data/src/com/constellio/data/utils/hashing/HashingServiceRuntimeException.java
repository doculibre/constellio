package com.constellio.data.utils.hashing;

@SuppressWarnings("serial")
public class HashingServiceRuntimeException extends RuntimeException {

	public HashingServiceRuntimeException() {
	}

	public HashingServiceRuntimeException(String message) {
		super(message);
	}

	public HashingServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public HashingServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotGetHashFromReader extends HashingServiceRuntimeException {

		public CannotGetHashFromReader(Throwable cause) {
			super(cause);
		}
	}

	public static class CannotGetHashFromStream extends HashingServiceRuntimeException {

		public CannotGetHashFromStream(Throwable cause) {
			super(cause);
		}
	}

	public static class NoSuchAlgorithm extends HashingServiceRuntimeException {

		public NoSuchAlgorithm(String algotithm, Throwable cause) {
			super("No such algorithm " + algotithm, cause);
		}
	}

}
