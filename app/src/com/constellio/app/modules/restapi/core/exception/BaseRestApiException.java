package com.constellio.app.modules.restapi.core.exception;

import com.constellio.model.frameworks.validation.ValidationError;
import lombok.Getter;

import javax.ws.rs.core.Response.StatusType;
import java.util.Collections;
import java.util.Map;

@Getter
public abstract class BaseRestApiException extends RuntimeException {

	protected StatusType status;
	protected ValidationError validationError;

	protected void buildValidationError(Class<?> validatorClass, String code, Map<String, Object> parameters) {
		validationError = new ValidationError(validatorClass, code, parameters);
	}

	protected void buildValidationError(String code, Map<String, Object> parameters) {
		buildValidationError(BaseRestApiException.class, code, parameters);
	}

	protected void buildValidationError(String code) {
		buildValidationError(code, Collections.<String, Object>emptyMap());
	}

}
