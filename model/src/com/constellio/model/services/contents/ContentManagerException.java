package com.constellio.model.services.contents;

@SuppressWarnings("serial")
public class ContentManagerException extends Exception {

	public ContentManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContentManagerException(String message) {
		super(message);
	}

	public ContentManagerException(Throwable cause) {
		super(cause);
	}

	public static class ContentManagerException_ContentNotParsed extends ContentManagerException {

		String id;

		public ContentManagerException_ContentNotParsed(String id) {
			super("Content with id '" + id + "' has not been parsed");
			this.id = id;
		}

		public ContentManagerException_ContentNotParsed(String id, Throwable cause) {
			super("Content with id '" + id + "' has not been parsed", cause);
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}

}
