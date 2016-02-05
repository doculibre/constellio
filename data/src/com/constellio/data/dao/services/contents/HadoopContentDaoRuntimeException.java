package com.constellio.data.dao.services.contents;

public class HadoopContentDaoRuntimeException extends ContentDaoRuntimeException {

	public HadoopContentDaoRuntimeException(String message) {
		super(message);
	}

	public HadoopContentDaoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public HadoopContentDaoRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class HadoopContentDaoRuntimeException_DatastoreFailure extends HadoopContentDaoRuntimeException {

		public HadoopContentDaoRuntimeException_DatastoreFailure(Throwable cause) {
			super("Hadoop datastore failure", cause);
		}
	}
}
