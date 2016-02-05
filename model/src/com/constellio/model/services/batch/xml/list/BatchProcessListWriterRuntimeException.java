package com.constellio.model.services.batch.xml.list;

@SuppressWarnings("serial")
public class BatchProcessListWriterRuntimeException extends RuntimeException {

	public BatchProcessListWriterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchProcessListWriterRuntimeException(String message) {
		super(message);
	}

	public BatchProcessListWriterRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class CannotHaveTwoBatchProcessInCurrentBatchProcessList extends BatchProcessListWriterRuntimeException {

		public CannotHaveTwoBatchProcessInCurrentBatchProcessList() {
			super("Cannot have two batch processes in batch process current list");
		}
	}

	public static class NoPendingBatchProcessesInList extends BatchProcessListWriterRuntimeException {

		public NoPendingBatchProcessesInList() {
			super("No pending batch processes in list");
		}
	}

	public static class BatchProcessAlreadyFinished extends BatchProcessListWriterRuntimeException {

		public BatchProcessAlreadyFinished() {
			super("Batch process already finished");
		}
	}

	public static class BatchProcessNotFound extends BatchProcessListWriterRuntimeException {

		public BatchProcessNotFound(String id) {
			super("Batch process " + id + " not found");
		}
	}
}
