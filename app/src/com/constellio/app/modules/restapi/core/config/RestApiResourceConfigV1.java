package com.constellio.app.modules.restapi.core.config;

import com.constellio.app.modules.restapi.apis.v1.document.filter.DocumentValidationFilter;
import com.constellio.app.modules.restapi.core.config.filter.RestApiLoggingFilter;
import com.constellio.app.modules.restapi.core.config.filter.RestApiTenantFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class RestApiResourceConfigV1 extends ResourceConfig {

	public RestApiResourceConfigV1() {
		packages("jersey.config.server.provider.packages", "com.constellio.app.modules.restapi.core");
		packages("jersey.config.server.provider.packages", "com.constellio.app.modules.restapi.apis.v1");
		register(MultiPartFeature.class);
		register(RestApiTenantFilter.class);
		register(RestApiLoggingFilter.class);
		register(DocumentValidationFilter.class);
		register(new RestApiBinderV1());
		register(new RestApiOpenApiResourceV1());
	}

}
