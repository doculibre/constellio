package com.constellio.model.frameworks.validation;

@SuppressWarnings("serial")
public class ValidationException extends Exception {

	private final ValidationErrors validationErrors;

	public ValidationException(ValidationErrors validationErrors) {
		super(validationErrors.toErrorsSummaryString());
		this.validationErrors = validationErrors;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}
}
