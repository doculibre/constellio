package com.constellio.model.services.batch.controller;

public class BatchProcessControllerRuntimeException extends RuntimeException {

	public BatchProcessControllerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchProcessControllerRuntimeException(String message) {
		super(message);
	}

	public BatchProcessControllerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ControllerAlreadyStarted extends BatchProcessControllerRuntimeException {

		public ControllerAlreadyStarted() {
			super("Batch process controller is already started");
		}

	}

}
