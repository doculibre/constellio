package com.constellio.data.utils;

@SuppressWarnings("serial")
public class TimeOutTaskRuntimeException extends RuntimeException {

	public TimeOutTaskRuntimeException() {
	}

	public TimeOutTaskRuntimeException(String message) {
		super(message);
	}

	public TimeOutTaskRuntimeException(Throwable cause) {
		super(cause);
	}

	public TimeOutTaskRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotDoExcute extends TimeOutTaskRuntimeException {

		public CannotDoExcute(Exception e) {
			super("Cannot doExcute", e);
		}
	}

	public static class Interrupted extends TimeOutTaskRuntimeException {

		public Interrupted(Exception e) {
			super("Interrupted", e);
		}
	}

}
