package com.constellio.data.utils;

public class DelayedRuntimeException extends RuntimeException {

	public DelayedRuntimeException(String message) {
		super(message);
	}

	public static class DelayedRuntimeException_NotYetDefined extends DelayedRuntimeException {

		public DelayedRuntimeException_NotYetDefined() {
			super("Cannot obtain value before it is defined");
		}
	}

	public static class DelayedRuntimeException_AlreadyDefined extends DelayedRuntimeException {

		public DelayedRuntimeException_AlreadyDefined() {
			super("Cannot redefine the value");
		}
	}
}
