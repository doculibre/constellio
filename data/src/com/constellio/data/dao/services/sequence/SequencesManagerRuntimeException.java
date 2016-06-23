package com.constellio.data.dao.services.sequence;

public class SequencesManagerRuntimeException extends RuntimeException {

	public SequencesManagerRuntimeException(String message) {
		super(message);
	}

	public SequencesManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SequencesManagerRuntimeException(Throwable cause) {
		super(cause);
	}

}
