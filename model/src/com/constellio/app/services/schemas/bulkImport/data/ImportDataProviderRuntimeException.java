package com.constellio.app.services.schemas.bulkImport.data;

public class ImportDataProviderRuntimeException extends RuntimeException {

	private ImportDataProviderRuntimeException(String message) {
		super(message);
	}

	private ImportDataProviderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImportDataProviderRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ImportDataProviderRuntimeException_InvalidDate extends ImportDataProviderRuntimeException {

		String dateFormat;

		String invalidValue;

		public ImportDataProviderRuntimeException_InvalidDate(String dateFormat, String invalidValue) {
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
