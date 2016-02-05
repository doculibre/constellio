package com.constellio.app.services.schemas.bulkImport;

public class ImportError {
	private String invalidElementId;
	private String errorMessage;

	public ImportError(String invalidElementId, String errorMessage) {
		this.invalidElementId = invalidElementId;
		this.errorMessage = errorMessage;
	}

	public String getInvalidElementId() {
		return invalidElementId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getCompleteErrorMessage() {
		return errorMessage + " " + invalidElementId;
	}

	@Override
	public String toString() {
		return getCompleteErrorMessage();
	}
}
