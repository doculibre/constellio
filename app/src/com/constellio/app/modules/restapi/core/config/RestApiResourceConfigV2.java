package com.constellio.app.modules.restapi.core.config;

import com.constellio.app.modules.restapi.core.config.filter.RestApiLoggingFilter;
import com.constellio.app.modules.restapi.core.config.filter.RestApiTenantFilter;
import org.glassfish.jersey.server.ResourceConfig;

public class RestApiResourceConfigV2 extends ResourceConfig {

	public RestApiResourceConfigV2() {
		packages("jersey.config.server.provider.packages", "com.constellio.app.modules.restapi.core");
		packages("jersey.config.server.provider.packages", "com.constellio.app.modules.restapi.apis.v2");
		register(RestApiTenantFilter.class);
		register(RestApiLoggingFilter.class);
		register(new RestApiBinderV2());
		register(new RestApiOpenApiResourceV2());
	}

}
