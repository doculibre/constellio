package com.constellio.model.services.batch.state;

public class BatchProcessProgressionServicesException extends Exception {

	public BatchProcessProgressionServicesException(String message) {
		super(message);
	}

	public BatchProcessProgressionServicesException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchProcessProgressionServicesException(Throwable cause) {
		super(cause);
	}

	public static class BatchProcessProgressionServicesException_OptimisticLocking
			extends BatchProcessProgressionServicesException {

		public BatchProcessProgressionServicesException_OptimisticLocking(int index) {
			super("Progression of batch process was changed by two threads at the same moment, there is already a batch process part #"
					+ index);
		}
	}
}
