package com.constellio.app.modules.restapi.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("health")
@Tag(name = "health")
public class HealthRestfulService {

	@Inject
	private HealthService healthService;

	@HEAD
	@Operation(summary = "Get health status of rest api", description = "Return the health status of the rest api")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content")
	})
	public Response getRestApiHealth() {
		return Response.noContent().build();
	}

	@GET
	@Operation(summary = "Get health status of constellio", description = "Return the health status of Constellio")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "503", description = "Service Unavailable")
	})
	public Response getConstellioHealth() {
		return healthService.isConstellioHealthy() ?
			   Response.noContent().build() :
			   Response.status(Status.SERVICE_UNAVAILABLE).entity("").type(MediaType.APPLICATION_JSON).build();
	}

}
