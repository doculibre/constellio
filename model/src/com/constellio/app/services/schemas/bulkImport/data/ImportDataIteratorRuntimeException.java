package com.constellio.app.services.schemas.bulkImport.data;

public class ImportDataIteratorRuntimeException extends RuntimeException {

	private ImportDataIteratorRuntimeException(String message) {
		super(message);
	}

	private ImportDataIteratorRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	private ImportDataIteratorRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ImportDataIteratorRuntimeException_InvalidDate extends ImportDataIteratorRuntimeException {

		String dateFormat;

		String invalidValue;

		public ImportDataIteratorRuntimeException_InvalidDate(String dateFormat, String invalidValue) {
			super("Invalid date '" + invalidValue + "' for format '" + dateFormat + "'");
			this.dateFormat = dateFormat;
			this.invalidValue = invalidValue;
		}

		public String getDateFormat() {
			return dateFormat;
		}

		public String getInvalidValue() {
			return invalidValue;
		}
	}

}
