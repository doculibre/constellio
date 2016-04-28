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

	public void add(Class<?> validatorClass, String code) {
		add(validatorClass, code, new HashMap<String, Object>());
	}

	public void add(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		validationErrors.add(new ValidationError(validatorClass.getName() + "_" + code, parameters));
	}

	public void add(String code, Map<String, Object> parameters) {
		validationErrors.add(new ValidationError(code, parameters));
	}

	public String toMultilineErrorsSummaryString() {
		StringBuilder sb = new StringBuilder();
		for (ValidationError validationError : validationErrors) {
			if (!sb.toString().isEmpty()) {
				sb.append("\n");
			}
			sb.append(validationError.toMultilineErrorSummaryString());
		}
		return sb.toString();
	}

	public String toErrorsSummaryString() {
		StringBuilder sb = new StringBuilder();
		for (ValidationError validationError : validationErrors) {
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
}
