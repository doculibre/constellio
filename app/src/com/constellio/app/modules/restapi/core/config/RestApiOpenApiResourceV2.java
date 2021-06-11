package com.constellio.app.modules.restapi.core.config;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.util.Collections;

class RestApiOpenApiResourceV2 extends OpenApiResource {

	RestApiOpenApiResourceV2() {
		Info info = new Info().title("Constellio REST API").description("This is the Constellio REST API v2 server.");

		Server serverV2 = new Server().url("/constellio/rest/v2");

		OpenAPI openAPI = new OpenAPI().info(info).servers(Collections.singletonList(serverV2));

		SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(openAPI).prettyPrint(true)
				.resourcePackages(Collections.singleton("com.constellio.app.modules.restapi.apis.v2"));

		setOpenApiConfiguration(oasConfig);

	}
}
