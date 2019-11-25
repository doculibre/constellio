package com.constellio.model.services.contents;

@SuppressWarnings("serial")
public class ContentManagerRuntimeException extends RuntimeException {

	public ContentManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContentManagerRuntimeException(String message) {
		super(message);
	}

	public ContentManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ContentManagerRuntimeException_CannotReadInputStream extends ContentManagerRuntimeException {

		public ContentManagerRuntimeException_CannotReadInputStream(Throwable cause) {
			super("Cannot read content", cause);
		}

	}

	public static class ContentManagerRuntimeException_CannotSaveContent extends ContentManagerRuntimeException {

		public ContentManagerRuntimeException_CannotSaveContent(Throwable cause) {
			super("Cannot save content", cause);
		}

	}

	public static class ContentManagerRuntimeException_CannotReadParsedContent extends ContentManagerRuntimeException {

		public ContentManagerRuntimeException_CannotReadParsedContent(Throwable cause, String hash,
																	  String parsedContent) {
			super("Cannot read parsed content with hash '" + hash + "' and parsed content : \n" + parsedContent, cause);
		}

	}

	public static class ContentManagerRuntimeException_ContentHasNoPreview extends ContentManagerRuntimeException {

		public ContentManagerRuntimeException_ContentHasNoPreview(String hash) {
			super("Content '" + hash + "' has no preview");
		}

	}

	public static class ContentManagerRuntimeException_ContentHasNoAnnotation extends ContentManagerRuntimeException {

		public ContentManagerRuntimeException_ContentHasNoAnnotation(String hash) {
			super("Content '" + hash + "' has no annotation");
		}

	}

	public static class ContentManagerRuntimeException_ContentHasNoThumbnail extends ContentManagerRuntimeException {

		public ContentManagerRuntimeException_ContentHasNoThumbnail(String hash) {
			super("Content '" + hash + "' has no thumbnail");
		}
	}

	public static class ContentManagerRuntimeException_ContentHasNoJpegConversion extends ContentManagerRuntimeException {

		public ContentManagerRuntimeException_ContentHasNoJpegConversion(String hash) {
			super("Content '" + hash + "' has no jpeg conversion");
		}
	}

	public static class ContentManagerRuntimeException_NoSuchContent extends ContentManagerRuntimeException {

		String id;

		public ContentManagerRuntimeException_NoSuchContent(String id) {
			super("No such content for id '" + id + "'");
			this.id = id;
		}

		public ContentManagerRuntimeException_NoSuchContent(String id, Throwable cause) {
			super("No such content for id '" + id + "'", cause);
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}

}
