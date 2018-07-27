package com.constellio.app.modules.restapi.core.config;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.util.Collections;

class RestApiOpenApiResource extends OpenApiResource {

    RestApiOpenApiResource() {
        Info info = new Info().title("Constellio REST API").description("This is the Constellio REST API server.");

        // TODO property?
        Server server = new Server().url("/constellio/rest/v1");

        OpenAPI openAPI = new OpenAPI().info(info).servers(Collections.singletonList(server));

        SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(openAPI).prettyPrint(true)
                .resourcePackages(Collections.singleton("com.constellio.app.modules.restapi"));

        setOpenApiConfiguration(oasConfig);

    }
}
