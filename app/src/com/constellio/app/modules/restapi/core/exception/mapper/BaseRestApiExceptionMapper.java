package com.constellio.app.modules.restapi.core.exception.mapper;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.Language;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Locale;

abstract class BaseRestApiExceptionMapper {

	protected static ModelLayerConfiguration configuration = ConstellioFactories.getInstance().getModelLayerConfiguration();

	@Context
	protected HttpHeaders headers;

	protected Locale getLocale() {
		List<String> supportedLanguages = Language.getAvailableLanguageCodes();

		for (Locale locale : headers.getAcceptableLanguages()) {
			if (supportedLanguages.contains(locale.getLanguage())) {
				return locale;
			}
		}

		return new Locale(configuration.getMainDataLanguage());
	}

	Response buildResponse(RestApiErrorResponse errorResponse) {
		Response response = Response.status(errorResponse.getCode())
				.entity(errorResponse)
				.type(MediaType.APPLICATION_JSON)
				.build();

		if (response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
			response.getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
		}

		return response;
	}

}
