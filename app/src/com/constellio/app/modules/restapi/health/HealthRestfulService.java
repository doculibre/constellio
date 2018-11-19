package com.constellio.app.modules.restapi.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("health")
@Tag(name = "health")
public class HealthRestfulService {

	@HEAD
	@Operation(summary = "Get health status", description = "Return the health status")
	public Response getHealthy() {
		return Response.noContent().build();
	}

}
