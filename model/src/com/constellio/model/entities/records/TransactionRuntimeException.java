package com.constellio.model.entities.records;

@SuppressWarnings("serial")
public class TransactionRuntimeException extends RuntimeException {

	private TransactionRuntimeException(String message) {
		super(message);
	}

	public static class RecordIdCollision extends TransactionRuntimeException {

		public RecordIdCollision() {
			super("Two different records added for the same id.");
		}

	}

	public static class RecordsWithoutIds extends TransactionRuntimeException {

		public RecordsWithoutIds() {
			super("Some records have no id, execute transaction before getting id");
		}

	}

	public static class DifferentCollectionsInRecords extends TransactionRuntimeException {

		public DifferentCollectionsInRecords(String collection, String otherCollection, String recordId) {
			super("Different collections in transaction's records with ID "+recordId+": \"" + collection + "\" != \"" + otherCollection + "\"");
		}
	}

	public static class TransactionRuntimeException_ToMuchRecordsInTransaction extends TransactionRuntimeException {

		public TransactionRuntimeException_ToMuchRecordsInTransaction() {
			super("To much records in transaction, limit is 1000.");
		}
	}
}
