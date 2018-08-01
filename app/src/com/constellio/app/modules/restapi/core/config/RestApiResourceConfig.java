package com.constellio.app.modules.restapi.core.config;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class RestApiResourceConfig extends ResourceConfig {

	public RestApiResourceConfig() {
		packages("jersey.config.server.provider.packages", "com.constellio.app.modules.restapi");
		register(MultiPartFeature.class);
		register(RestApiLoggingFilter.class);
		register(new RestApiBinder());
		register(new RestApiOpenApiResource());
	}

}
