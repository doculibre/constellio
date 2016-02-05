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

}
