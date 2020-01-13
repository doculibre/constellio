package com.constellio.model.services.batch.manager;

@SuppressWarnings("serial")
public class BatchProcessesManagerRuntimeException extends RuntimeException {

	public BatchProcessesManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchProcessesManagerRuntimeException(String message) {
		super(message);
	}

	public BatchProcessesManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class BatchProcessesManagerRuntimeException_Timeout extends BatchProcessesManagerRuntimeException {

		public BatchProcessesManagerRuntimeException_Timeout() {
			super("Timeout waiting for batch processes to finish");
		}
	}
}
