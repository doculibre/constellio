package com.constellio.model.services.records;

public class BulkRecordTransactionHandlerRuntimeException extends RuntimeException {

	public BulkRecordTransactionHandlerRuntimeException(String message) {
		super(message);
	}

	public BulkRecordTransactionHandlerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BulkRecordTransactionHandlerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class BulkRecordTransactionHandlerRuntimeException_Interrupted
			extends BulkRecordTransactionHandlerRuntimeException {
		public BulkRecordTransactionHandlerRuntimeException_Interrupted(InterruptedException e) {
			super("Interrupted", e);
		}
	}

	public static class BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction
			extends BulkRecordTransactionHandlerRuntimeException {
		public BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction(Exception e) {
			super("An exception occured while executing a transaction", e);
		}
	}
}
