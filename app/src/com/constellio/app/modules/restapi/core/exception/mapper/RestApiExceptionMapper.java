package com.constellio.app.modules.restapi.core.exception.mapper;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Locale;

@Provider
@Slf4j
public class RestApiExceptionMapper extends BaseRestApiExceptionMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(Throwable e) {
		Locale locale = getLocale();
		RestApiErrorResponse errorResponse = RestApiErrorResponse.builder().build();

		if (e instanceof BaseRestApiException) {
			BaseRestApiException ex = (BaseRestApiException) e;

			errorResponse.setCode(ex.getStatus().getStatusCode());
			errorResponse.setDescription(ex.getStatus().getReasonPhrase());
			errorResponse.setMessage(i18n.$(ex.getValidationError(), locale));
		} else if (e instanceof WebApplicationException) {
			WebApplicationException ex = (WebApplicationException) e;

			errorResponse.setCode(ex.getResponse().getStatusInfo().getStatusCode());
			errorResponse.setDescription(ex.getResponse().getStatusInfo().getReasonPhrase());
			errorResponse.setMessage(ex.getLocalizedMessage());
		} else if (e instanceof ValidationException) {
			ValidationException ex = (ValidationException) e;

			errorResponse.setCode(Response.Status.BAD_REQUEST.getStatusCode());
			errorResponse.setDescription(Response.Status.BAD_REQUEST.getReasonPhrase());
			errorResponse.setMessage(i18n.$(ex.getErrors().getValidationErrors().get(0), locale));
		} else if (e instanceof ValidationRuntimeException) {
			ValidationRuntimeException ex = (ValidationRuntimeException) e;

			errorResponse.setCode(Response.Status.BAD_REQUEST.getStatusCode());
			errorResponse.setDescription(Response.Status.BAD_REQUEST.getReasonPhrase());
			errorResponse.setMessage(i18n.$(ex.getValidationErrorsList().get(0), locale));
		} else {
			log.error(e.getMessage(), e);

			errorResponse.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			errorResponse.setDescription(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
			errorResponse.setMessage(e.getMessage());
		}

		return buildResponse(errorResponse);
	}
}