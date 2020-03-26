package com.constellio.model.frameworks.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationErrors {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationErrors.class);

	private final List<ValidationError> validationErrors = new ArrayList<>();
	private final List<ValidationError> validationWarnings = new ArrayList<>();
	private final List<ValidationError> validationLogs = new ArrayList<>();

	public ValidationErrors() {
	}

	public ValidationErrors(ValidationErrors copy) {
		this.validationErrors.addAll(new ArrayList<>(copy.getValidationErrors()));
		this.validationWarnings.addAll(new ArrayList<>(copy.getValidationWarnings()));
		this.validationLogs.addAll(new ArrayList<>(copy.getValidationLogs()));
	}

	public ValidationError create(Class<?> validatorClass, String code, Map<String, Object> parameters) {

		return new ValidationError(validatorClass, code, parameters);
	}

	public final void add(Class<?> validatorClass, String code) {
		add(validatorClass, code, new HashMap<String, Object>());
	}

	public final void add(ValidationError e, Map<String, Object> parameters) {
		add(e.getValidatorClass(), e.getValidatorErrorCode(), parameters);
	}

	public void add(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		validationErrors.add(create(validatorClass, code, parameters));
	}

	public final void addWarning(Class<?> validatorClass, String code) {
		addWarning(validatorClass, code, new HashMap<String, Object>());
	}

	public final void addWarning(ValidationError e, Map<String, Object> parameters) {
		addWarning(e.getValidatorClass(), e.getValidatorErrorCode(), parameters);
	}

	public void addWarning(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		validationWarnings.add(create(validatorClass, code, parameters));
	}

	public void addLog(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		validationLogs.add(create(validatorClass, code, parameters));
	}

	public final String toMultilineErrorsSummaryString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Validation errors :\n");
		for (ValidationError validationError : getValidationErrors()) {
			if (!sb.toString().isEmpty()) {
				sb.append("\n");
			}
			sb.append(validationError.toMultilineErrorSummaryString());
		}

		sb.append("\nValidation warnings :\n");
		for (ValidationError validationError : getValidationWarnings()) {
			if (!sb.toString().isEmpty()) {
				sb.append("\n");
			}
			sb.append(validationError.toMultilineErrorSummaryString());
		}

		sb.append("\nValidation logs :\n");
		for (ValidationError validationError : getValidationLogs()) {
			if (!sb.toString().isEmpty()) {
				sb.append("\n");
			}
			sb.append(validationError.toMultilineErrorSummaryString());
		}

		return sb.toString();
	}

	public final String toErrorsSummaryString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Validation errors :\n");
		for (ValidationError validationError : getValidationErrors()) {
			if (sb.toString().length() < 1000) {
				if (!sb.toString().isEmpty()) {
					sb.append(", ");
				}
				sb.append(validationError.toErrorSummaryString());
			}
		}

		sb.append("\nValidation warnings :\n");
		for (ValidationError validationError : getValidationWarnings()) {
			if (sb.toString().length() < 1000) {
				if (!sb.toString().isEmpty()) {
					sb.append(", ");
				}
				sb.append(validationError.toErrorSummaryString());
			}
		}

		sb.append("\nValidation logs :\n");
		for (ValidationError validationError : getValidationLogs()) {
			if (sb.toString().length() < 1000) {
				if (!sb.toString().isEmpty()) {
					sb.append(", ");
				}
				sb.append(validationError.toErrorSummaryString());
			}
		}

		return sb.toString();
	}

	public List<ValidationError> getValidationWarnings() {
		return Collections.unmodifiableList(validationWarnings);
	}

	public List<ValidationError> getValidationLogs() {
		return Collections.unmodifiableList(validationLogs);
	}

	public void addAllWarnings(Collection<ValidationError> validationWarnings) {
		this.validationWarnings.addAll(validationWarnings);
	}

	public void addAllLogs(Collection<ValidationError> validationLogs) {
		this.validationLogs.addAll(validationLogs);
	}

	public List<ValidationError> getValidationErrors() {
		return Collections.unmodifiableList(validationErrors);
	}

	public void addAll(Collection<ValidationError> validationErrors) {
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

	public final boolean isEmptyErrorAndWarnings() {
		return getValidationErrors().isEmpty() && getValidationWarnings().isEmpty();
	}

	public final boolean isEmptyErrorWarningAndLogs() {
		return getValidationErrors().isEmpty() && getValidationWarnings().isEmpty() && getValidationLogs().isEmpty();
	}

	public final void throwIfNonEmptyErrorOrWarnings()
			throws ValidationException {
		if (!isEmptyErrorAndWarnings()) {
			throw new ValidationException(this);
		}
	}

	public void addPrefix(String prefix) {
		for (ValidationError error : validationErrors) {
			try {
				error.setParameter(ValidationError.PREFIX, prefix);
			} catch (Exception e) {
				LOGGER.warn("Cannot add prefix to validation error", e);
			}
		}
	}

	public void clearAll() {
		validationErrors.clear();
		validationWarnings.clear();
		validationLogs.clear();
	}
}
