package com.constellio.app.modules.restapi.core.exception.mapper;

import com.constellio.app.ui.i18n.i18n;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.validation.ValidationError;
import org.glassfish.jersey.server.validation.internal.ValidationHelper;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RestApiValidationExceptionMapper extends BaseRestApiExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	@Override
	public Response toResponse(ConstraintViolationException e) {
		RestApiErrorResponse errorResponse = RestApiErrorResponse.builder()
				.code(ValidationHelper.getResponseStatus(e).getStatusCode())
				.description(ValidationHelper.getResponseStatus(e).getReasonPhrase())
				.build();

		ValidationError validationError = toValidationError(e);
		errorResponse.setMessage(i18n.$(validationError.getMessageTemplate(), getLocale(), validationError.getPath()));

		return buildResponse(errorResponse);
	}

	private ValidationError toValidationError(ConstraintViolationException exception) {
		ConstraintViolation constraintViolation = exception.getConstraintViolations().iterator().next();

		ValidationError error = new ValidationError();

		String path = constraintViolation.getPropertyPath().toString();
		int idx = StringUtils.ordinalIndexOf(path, ".", 2);
		error.setPath(idx != -1 ? path.substring(idx + 1, path.length()) : path);

		error.setMessageTemplate(constraintViolation.getMessageTemplate()
				.replace("{", "")
				.replace("}", ""));

		return error;
	}

}
