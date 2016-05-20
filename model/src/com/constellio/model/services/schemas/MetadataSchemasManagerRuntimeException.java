package com.constellio.model.services.schemas;

@SuppressWarnings("serial")
public class MetadataSchemasManagerRuntimeException extends RuntimeException {

	public MetadataSchemasManagerRuntimeException() {
	}

	public MetadataSchemasManagerRuntimeException(String message) {
		super(message);
	}

	public MetadataSchemasManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public MetadataSchemasManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class NoSuchValidatorClass extends MetadataSchemasManagerRuntimeException {

		public NoSuchValidatorClass(String validatorClassName, Exception e) {
			super("No such validator class : " + validatorClassName, e);
		}
	}

	public static class CannotUpdateDocument extends MetadataSchemasManagerRuntimeException {

		public CannotUpdateDocument(String document, Exception e) {
			super("Cannot update document : " + document, e);
		}
	}

	public static class MetadataSchemasManagerRuntimeException_NoSuchCollection extends MetadataSchemasManagerRuntimeException {

		public MetadataSchemasManagerRuntimeException_NoSuchCollection(String collection) {
			super("No such collection '" + collection + "'");
		}
	}

}
