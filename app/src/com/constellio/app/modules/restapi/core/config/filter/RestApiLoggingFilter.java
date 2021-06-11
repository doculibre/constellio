package com.constellio.app.modules.restapi.core.config.filter;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import java.util.regex.Pattern;

@PreMatching
@Slf4j
public class RestApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private static final Pattern TOKEN_PATTERN = Pattern.compile("token=([^&]+)");
	private static final Pattern SIGNATURE_PATTERN = Pattern.compile("signature=([^&]+)");

	private static final String TOKEN = "token=XXX";
	private static final String SIGNATURE = "signature=XXX";

	@Override
	public void filter(ContainerRequestContext requestContext) {
		requestContext.setProperty("timestamp", System.currentTimeMillis());

		log.info("HTTP request: "
				.concat(requestContext.getMethod()).concat(" ")
				.concat(getQuery(requestContext)));
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		Object start = requestContext.getProperty("timestamp");
		if (start == null) {
			return;
		}

		long ms = System.currentTimeMillis() - (long) start;

		log.info("HTTP response: "
				.concat(String.valueOf(responseContext.getStatus()).concat(" ")
						.concat(requestContext.getMethod().concat(" "))
						.concat(getQuery(requestContext))).concat(" ")
				.concat(ms + "ms"));
	}

	private String getQuery(ContainerRequestContext requestContext) {
		String query = requestContext.getUriInfo().getRequestUri().getQuery();
		if (query != null) {
			query = TOKEN_PATTERN.matcher(query).replaceAll(TOKEN);
			query = SIGNATURE_PATTERN.matcher(query).replaceAll(SIGNATURE);
		}

		return requestContext.getUriInfo().getRequestUri().getPath().concat(query != null ? "?" + query : "");
	}
}
