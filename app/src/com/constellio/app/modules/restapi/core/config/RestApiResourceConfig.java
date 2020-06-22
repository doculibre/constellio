package com.constellio.app.modules.restapi.core.config;

import com.constellio.app.modules.restapi.core.config.filter.RestApiLoggingFilter;
import com.constellio.app.modules.restapi.core.config.filter.RestApiTenantFilter;
import com.constellio.app.modules.restapi.document.filter.DocumentValidationFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class RestApiResourceConfig extends ResourceConfig {

	public RestApiResourceConfig() {
		packages("jersey.config.server.provider.packages", "com.constellio.app.modules.restapi");
		register(MultiPartFeature.class);
		register(RestApiTenantFilter.class);
		register(RestApiLoggingFilter.class);
		register(DocumentValidationFilter.class);
		register(new RestApiBinder());
		register(new RestApiOpenApiResource());
	}

}
