package com.constellio.app.services.collections;

@SuppressWarnings("serial")
public class CollectionsManagerRuntimeException extends RuntimeException {

	public CollectionsManagerRuntimeException(String message) {
		super(message);
	}

	public CollectionsManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CollectionsManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class CollectionsManagerRuntimeException_CollectionWithGivenCodeAlreadyExists
			extends CollectionsManagerRuntimeException {

		public CollectionsManagerRuntimeException_CollectionWithGivenCodeAlreadyExists(String code) {
			super("A collection with the code '" + code + "' already exist.");
		}
	}

	public static class CollectionsManagerRuntimeException_CollectionNotFound extends CollectionsManagerRuntimeException {

		public CollectionsManagerRuntimeException_CollectionNotFound(String code, Exception e) {
			super("A collection not found: " + code, e);
		}
	}

	public static class CollectionsManagerRuntimeException_CannotCreateCollectionRecord
			extends CollectionsManagerRuntimeException {

		public CollectionsManagerRuntimeException_CannotCreateCollectionRecord(String code, Exception e) {
			super("Cannot create collection record: " + code, e);
		}
	}

	public static class CollectionsManagerRuntimeException_CollectionLanguageMustIncludeSystemMainDataLanguage
			extends CollectionsManagerRuntimeException {

		public CollectionsManagerRuntimeException_CollectionLanguageMustIncludeSystemMainDataLanguage(
				String mainDataLanguage) {
			super("Collection's languages must include system main data language '" + mainDataLanguage + "'");
		}
	}

	public static class CollectionsManagerRuntimeException_InvalidLanguage
			extends CollectionsManagerRuntimeException {

		public CollectionsManagerRuntimeException_InvalidLanguage(String language) {
			super("Language '" + language + "' is not supported.");
		}
	}

	public static class CollectionsManagerRuntimeException_InvalidCode extends CollectionsManagerRuntimeException {
		public CollectionsManagerRuntimeException_InvalidCode(String code) {
			super("Invalid code: " + code);
		}
	}

	public static class CollectionsManagerRuntimeException_CannotRemoveCollection extends CollectionsManagerRuntimeException {
		public CollectionsManagerRuntimeException_CannotRemoveCollection(String collection, Throwable cause) {
			super("Cannot remove collection from big vault: " + collection, cause);
		}
	}

	public static class CollectionsManagerRuntimeException_CannotMigrateCollection extends CollectionsManagerRuntimeException {
		public CollectionsManagerRuntimeException_CannotMigrateCollection(String collection,
																		  Throwable cause) {
			super("Cannot migrate collection '" + collection + "'", cause);
		}
	}
}
