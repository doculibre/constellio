package com.constellio.app.modules.restapi.core.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class RestApiObjectMapper implements ContextResolver<ObjectMapper> {

	private final ObjectMapper defaultObjectMapper;

	public RestApiObjectMapper() {
		defaultObjectMapper = createDefaultMapper();
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return defaultObjectMapper;
	}

	private static ObjectMapper createDefaultMapper() {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JodaModule());
		objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

		return objectMapper;
	}
}
