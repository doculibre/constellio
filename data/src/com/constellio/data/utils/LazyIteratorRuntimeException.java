package com.constellio.data.utils;

public class LazyIteratorRuntimeException extends RuntimeException {

	public LazyIteratorRuntimeException(String message) {
		super(message);
	}

	public LazyIteratorRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LazyIteratorRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class LazyIteratorRuntimeException_IncorrectUsage extends LazyIteratorRuntimeException {

		public LazyIteratorRuntimeException_IncorrectUsage() {
			super("Cannot call next, since there is no more results");
		}

	}

	public static class LazyIteratorRuntimeException_RemoveNotAvailable extends LazyIteratorRuntimeException {

		public LazyIteratorRuntimeException_RemoveNotAvailable() {
			super("Remove is not available");
		}

	}
}
