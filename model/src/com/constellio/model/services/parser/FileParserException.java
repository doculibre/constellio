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

		public FileParserException_CannotParse(Throwable t, String detectedMimetype) {
			super("Cannot parse file", t);
			this.detectedMimetype = detectedMimetype;
		}

		public String getDetectedMimetype() {
			return detectedMimetype;
		}
	}

	public static class FileParserException_FileSizeExceedLimitForParsing extends FileParserException {

		private String detectedMimetype;

		public FileParserException_FileSizeExceedLimitForParsing(int maxFileSize, String detectedMimetype) {
			super("Size of file exceed limit of " + maxFileSize + "mo");
			this.detectedMimetype = detectedMimetype;
		}

		public String getDetectedMimetype() {
			return detectedMimetype;
		}
	}

	public static class FileParserException_CannotExtractStyles extends FileParserException {

		private String detectedMimetype;

		public FileParserException_CannotExtractStyles(Throwable t, String detectedMimetype) {
			super("Cannot parse file", t);
			this.detectedMimetype = detectedMimetype;
		}

		public String getDetectedMimetype() {
			return detectedMimetype;
		}
	}
}
