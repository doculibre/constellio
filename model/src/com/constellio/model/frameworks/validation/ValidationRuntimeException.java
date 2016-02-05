package com.constellio.model.frameworks.validation;

import java.util.List;

@SuppressWarnings("serial")
public class ValidationRuntimeException extends RuntimeException {

	private final ValidationErrors validationErrors;

	public ValidationRuntimeException(ValidationErrors validationErrors) {
		super(validationErrors.toErrorsSummaryString());
		this.validationErrors = validationErrors;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}

	public List<ValidationError> getValidationErrorsList() {
		return validationErrors.getValidationErrors();
	}
}
