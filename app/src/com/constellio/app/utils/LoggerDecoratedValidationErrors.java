package com.constellio.app.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.frameworks.validation.DecoratedValidationsErrors;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class LoggerDecoratedValidationErrors extends DecoratedValidationsErrors {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerDecoratedValidationErrors.class);

	public LoggerDecoratedValidationErrors(ValidationErrors errors) {
		super(errors);
	}

	@Override
	public void add(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		super.add(validatorClass, code, parameters);
		String message = i18n.$(new ValidationError(validatorClass, code, parameters));
		LOGGER.error(message);

	}

	@Override
	public void addWarning(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		super.addWarning(validatorClass, code, parameters);
		String message = i18n.$(new ValidationError(validatorClass, code, parameters));
		LOGGER.warn(message);
	}

	@Override
	public final void buildExtraParams(Map<String, Object> params) {
		super.buildExtraParams(params);
	}
}
