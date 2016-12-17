package com.constellio.model.frameworks.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecoratedValidationsErrors extends ValidationErrors {

	private ValidationErrors errors;

	private Map<String, String> extraParams;

	private boolean hasDecoratedErrors;

	private boolean hasDecoratedWarnings;

	public DecoratedValidationsErrors(ValidationErrors errors, Map<String, String> extraParams) {
		this.extraParams = extraParams;
		this.errors = errors;
	}

	public DecoratedValidationsErrors(ValidationErrors errors) {
		this.extraParams = new HashMap<>();
		this.errors = errors;

	}

	public void buildExtraParams(Map<String, Object> params) {
	}

	@Override
	public void add(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		hasDecoratedErrors = true;
		parameters.putAll(extraParams);

		buildExtraParams(parameters);

		errors.add(validatorClass, code, parameters);
	}

	public boolean hasDecoratedErrors() {
		return hasDecoratedErrors;
	}

	public boolean hasDecoratedErrorsOrWarnings() {
		return hasDecoratedErrors || hasDecoratedWarnings;
	}

	public DecoratedValidationsErrors withParam(String key, String value) {
		this.extraParams.put(key, value);
		return this;
	}

	public List<ValidationError> getValidationErrors() {
		return errors.getValidationErrors();
	}

	public void addAll(List<ValidationError> validationErrors) {
		for (ValidationError error : validationErrors) {
			add(error.getValidatorClass(), error.getValidatorErrorCode(), error.getParameters());
		}
	}

	@Override
	public void addWarning(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		hasDecoratedWarnings = true;
		parameters.putAll(extraParams);

		buildExtraParams(parameters);

		errors.addWarning(validatorClass, code, parameters);
	}

	@Override
	public List<ValidationError> getValidationWarnings() {
		return errors.getValidationWarnings();
	}

	@Override
	public void addAllWarnings(List<ValidationError> validationWarnings) {
		for (ValidationError error : validationWarnings) {
			addWarning(error.getValidatorClass(), error.getValidatorErrorCode(), error.getParameters());
		}
	}
}
