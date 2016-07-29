package com.constellio.model.frameworks.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecoratedValidationsErrors extends ValidationErrors {

	private ValidationErrors errors;

	private Map<String, String> extraParams;

	private boolean hasDecoratedErrors;

	public DecoratedValidationsErrors(ValidationErrors errors, Map<String, String> extraParams) {
		this.extraParams = extraParams;
		this.errors = errors;
	}

	public DecoratedValidationsErrors(ValidationErrors errors) {
		this.extraParams = new HashMap<>();
		this.errors = errors;

	}

	@Override
	public void add(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		hasDecoratedErrors = true;
		parameters.putAll(extraParams);
		errors.add(validatorClass, code, parameters);
	}

	public boolean hasDecoratedErrors() {
		return hasDecoratedErrors;
	}

	public DecoratedValidationsErrors withParam(String key, String value) {
		this.extraParams.put(key, value);
		return this;
	}

	public List<ValidationError> getValidationErrors() {
		return errors.getValidationErrors();
	}

	public void addAll(List<ValidationError> validationErrors) {
		throw new UnsupportedOperationException("Method unsupported");
	}
}
