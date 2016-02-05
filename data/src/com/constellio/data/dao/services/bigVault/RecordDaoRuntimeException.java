package com.constellio.data.dao.services.bigVault;

@SuppressWarnings("serial")
public class RecordDaoRuntimeException extends RuntimeException {

	public RecordDaoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordDaoRuntimeException(String message) {
		super(message);
	}

	public RecordDaoRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RecordDaoRuntimeException_RecordsFlushingFailed extends RecordDaoRuntimeException {

		public RecordDaoRuntimeException_RecordsFlushingFailed(Exception e) {
			super("Records flushing failed", e);
		}

	}

	public static class ReferenceToNonExistentIndex extends RecordDaoRuntimeException {

		private final String id;

		public ReferenceToNonExistentIndex(String id) {
			super("The record cannot be saved, since it references the non-existent index '" + id + "'");
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}

}
