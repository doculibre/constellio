package com.constellio.app.services.schemas.bulkImport;

public class ImportFileParserException extends Exception {
	public ImportFileParserException(String message) {
		super(message);
	}

	public ImportFileParserException(Throwable t) {
		super(t);
	}

	public static class ImportFileParserException_CannotParseCollection extends ImportFileParserException {
		public ImportFileParserException_CannotParseCollection(String message) {
			super(message);
		}

		public ImportFileParserException_CannotParseCollection(Throwable t) {
			super(t);
		}
	}
}
