package com.constellio.model.services.batch.xml.list;

@SuppressWarnings("serial")
public class BatchProcessListReaderRuntimeException extends RuntimeException {

	public BatchProcessListReaderRuntimeException() {
		super();
	}

	public BatchProcessListReaderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchProcessListReaderRuntimeException(String message) {
		super(message);
	}

	public BatchProcessListReaderRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class NoBatchProcessesInList extends BatchProcessListReaderRuntimeException {

		public NoBatchProcessesInList() {
			super("No batch processes in list");
		}
	}
}
