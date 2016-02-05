package com.constellio.model.services.parser;

@SuppressWarnings("serial")
public abstract class FileParserException extends Exception {

	private FileParserException(String message, Throwable cause) {
		super(message, cause);
	}

	private FileParserException(String message) {
		super(message);
	}

	public abstract String getDetectedMimetype();

	public static class FileParserException_CannotParse extends FileParserException {

		private String detectedMimetype;

		public FileParserException_CannotParse(Exception e, String detectedMimetype) {
			super("Cannot parse file", e);
			this.detectedMimetype = detectedMimetype;
		}

		public String getDetectedMimetype() {
			return detectedMimetype;
		}
	}

	public static class FileParserException_CannotExtractStyles extends FileParserException {

		private String detectedMimetype;

		public FileParserException_CannotExtractStyles(Exception e, String detectedMimetype) {
			super("Cannot parse file", e);
			this.detectedMimetype = detectedMimetype;
		}

		public String getDetectedMimetype() {
			return detectedMimetype;
		}
	}
}
