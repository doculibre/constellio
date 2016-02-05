package com.constellio.data.dao.services.bigVault;

@SuppressWarnings("serial")
public class RecordDaoException extends Exception {

	public RecordDaoException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordDaoException(String message) {
		super(message);
	}

	public RecordDaoException(Throwable cause) {
		super(cause);
	}

	public static class NoSuchRecordWithId extends RecordDaoException {

		public NoSuchRecordWithId(String id) {
			super("No such record with id '" + id + "'");
		}

	}

	public static class OptimisticLocking extends RecordDaoException {

		private final String id;

		private final Long version;

		public OptimisticLocking(String id, Long version, Throwable t) {
			super(getMessage(id, version), t);
			this.id = id;
			this.version = version;
		}

		private static String getMessage(String id, Long version) {
			return "Optimistic locking while saving solr document with id '" + id + "' in version '" + version + "'";
		}

		public String getId() {
			return id;
		}

		public Long getVersion() {
			return version;
		}
	}
}
