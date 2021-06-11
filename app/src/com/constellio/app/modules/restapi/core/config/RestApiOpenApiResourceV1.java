package com.constellio.app.modules.restapi.core.config;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.util.Collections;

class RestApiOpenApiResourceV1 extends OpenApiResource {

	RestApiOpenApiResourceV1() {
		Info info = new Info().title("Constellio REST API").description("This is the Constellio REST API v1 server.");

		Server serverV1 = new Server().url("/constellio/rest/v1");

		OpenAPI openAPI = new OpenAPI().info(info).servers(Collections.singletonList(serverV1));

		SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(openAPI).prettyPrint(true)
				.resourcePackages(Collections.singleton("com.constellio.app.modules.restapi.apis.v1"));

		setOpenApiConfiguration(oasConfig);

	}
}
