package com.constellio.model.services.batch.xml.list;

@SuppressWarnings("serial")
public class BatchProcessListReaderException extends Exception {

	public BatchProcessListReaderException() {
		super();
	}

	public BatchProcessListReaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchProcessListReaderException(String message) {
		super(message);
	}

	public BatchProcessListReaderException(Throwable cause) {
		super(cause);
	}

	public static class NoBatchProcessesInList extends BatchProcessListReaderException {

		public NoBatchProcessesInList(String status) {
			super("No batch processes in " + status + " list");
		}
	}
}
