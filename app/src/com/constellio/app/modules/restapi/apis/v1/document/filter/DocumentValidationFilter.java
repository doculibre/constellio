package com.constellio.app.modules.restapi.apis.v1.document.filter;

import com.constellio.app.modules.restapi.apis.v1.validation.ValidationService;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.restapi.core.util.HttpMethods;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;

@PreMatching
public class DocumentValidationFilter implements ContainerRequestFilter {

	@Inject private ValidationService validationService;

	private static final String DOCUMENT_PATH = "documents";

	@Override
	public void filter(ContainerRequestContext requestContext) {
		if (requestContext.getProperty("urlValidated") != null) {
			throw new UnauthorizedAccessException();
		}

		if (requestContext.getMethod().equals(HttpMethods.POST) ||
			requestContext.getMethod().equals(HttpMethods.PATCH) ||
			requestContext.getMethod().equals(HttpMethods.PUT)) {

			if (!requestContext.getUriInfo().getPath().equals(DOCUMENT_PATH)) {
				return;
			}

			MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();
			String date = queryParameters.getFirst("date");
			String expiration = queryParameters.getFirst("expiration");

			if (date != null && expiration != null) {
				try {
					validationService.validateUrl(date, Integer.valueOf(expiration));
					requestContext.setProperty("urlValidated", true);
				} catch (NumberFormatException ignored) {
				}
			}
		}
	}
}
