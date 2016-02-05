package com.constellio.app.api.cmis;

public class ConstellioCmisException extends Exception {

	public ConstellioCmisException(String message) {
		super(message);
	}

	public ConstellioCmisException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstellioCmisException(Throwable cause) {
		super(cause);
	}
	
	public static class ConstellioCmisException_ContentAlreadyCheckedOut extends ConstellioCmisException {
		
		public ConstellioCmisException_ContentAlreadyCheckedOut() {
			super("Cannot modify content checked out by other user");
		}
	}
	
	public static class ConstellioCmisException_IOError extends ConstellioCmisException {

		public ConstellioCmisException_IOError(Throwable cause) {
			super(cause);
		}	
	}
	
	public static class ConstellioCmisException_RecordServicesError extends ConstellioCmisException {

		public ConstellioCmisException_RecordServicesError(Throwable cause) {
			super(cause);
		}	
	}
	
	public static class ConstellioCmisException_UnsupportedVersioningState extends ConstellioCmisException {
		
		public ConstellioCmisException_UnsupportedVersioningState() {
			super("Unsupported versioning state");
		}
	}
}
