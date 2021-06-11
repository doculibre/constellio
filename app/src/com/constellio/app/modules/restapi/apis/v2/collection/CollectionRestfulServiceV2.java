package com.constellio.app.modules.restapi.apis.v2.collection;

import com.constellio.app.modules.restapi.apis.v1.collection.dto.CollectionDto;
import com.constellio.app.modules.restapi.apis.v2.core.BaseRestfulServiceV2;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("collections")
@Tag(name = "collections")
public class CollectionRestfulServiceV2 extends BaseRestfulServiceV2 {

	@Inject
	private CollectionServiceV2 collectionService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get collections", description = "Return the collections")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CollectionDto.class)))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response getCollections(
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);

		String token = AuthorizationUtils.getToken(authentication);
		List<CollectionDto> collectionDtos = collectionService.getCollections(host, token);
		return Response.ok(collectionDtos).build();
	}

}
