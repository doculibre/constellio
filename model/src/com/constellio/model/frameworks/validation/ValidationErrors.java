package com.constellio.model.frameworks.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationErrors {

	private final List<ValidationError> validationErrors;

	public ValidationErrors() {
		validationErrors = new ArrayList<>();
	}

	public final void add(Class<?> validatorClass, String code) {
		add(validatorClass, code, new HashMap<String, Object>());
	}

	public final void add(ValidationError e, Map<String, Object> parameters) {
		add(e.getValidatorClass(), e.getValidatorErrorCode(), parameters);
	}

	public void add(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		validationErrors.add(new ValidationError(validatorClass, code, parameters));
	}

	public final String toMultilineErrorsSummaryString() {
		StringBuilder sb = new StringBuilder();
		for (ValidationError validationError : getValidationErrors()) {
			if (!sb.toString().isEmpty()) {
				sb.append("\n");
			}
			sb.append(validationError.toMultilineErrorSummaryString());
		}
		return sb.toString();
	}

	public final String toErrorsSummaryString() {
		StringBuilder sb = new StringBuilder();
		for (ValidationError validationError : getValidationErrors()) {
			if (sb.toString().length() < 1000) {
				if (!sb.toString().isEmpty()) {
					sb.append(", ");
				}
				sb.append(validationError.toErrorSummaryString());
			}
		}
		return sb.toString();
	}

	public List<ValidationError> getValidationErrors() {
		return Collections.unmodifiableList(validationErrors);
	}

	public void addAll(List<ValidationError> validationErrors) {
		this.validationErrors.addAll(validationErrors);
	}

	public final boolean isEmpty() {
		return getValidationErrors().isEmpty();
	}

	public final void throwIfNonEmpty()
			throws ValidationException {
		if (!isEmpty()) {
			throw new ValidationException(this);
		}
	}
}
