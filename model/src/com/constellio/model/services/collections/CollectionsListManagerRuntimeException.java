package com.constellio.model.services.collections;

public class CollectionsListManagerRuntimeException extends RuntimeException {

	public CollectionsListManagerRuntimeException(String message) {
		super(message);
	}

	public CollectionsListManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CollectionsListManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class CollectionsListManagerRuntimeException_NoSuchCollection
			extends CollectionsListManagerRuntimeException {

		public CollectionsListManagerRuntimeException_NoSuchCollection(String code) {
			super("No such collection with code '" + code + "'");
		}
	}
}
