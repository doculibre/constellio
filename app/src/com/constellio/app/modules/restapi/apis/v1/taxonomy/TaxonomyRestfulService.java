package com.constellio.app.modules.restapi.apis.v1.taxonomy;

import com.constellio.app.modules.restapi.apis.v1.core.BaseRestfulService;
import com.constellio.app.modules.restapi.apis.v1.taxonomy.dto.TaxonomyDto;
import com.constellio.app.modules.restapi.apis.v1.taxonomy.dto.TaxonomyNodeDto;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Hidden;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Set;

// IMPORTANT : hidden from swagger doc, only used by teams
@Hidden
@Path("taxonomies")
@Tag(name = "taxonomies")
public class TaxonomyRestfulService extends BaseRestfulService {

	@Inject
	private TaxonomyService taxonomyService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get taxonomies", description = "Return the visible taxonomies of an user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaxonomyDto.class)))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response getTaxonomies(
			@Parameter(required = true, description = "Collection code") @QueryParam("collection") String collection,
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(name = "schemaType", description = "Schema type of nodes to return") @QueryParam("schemaType") String schemaTypeCode,
			@Parameter(name = "user", description = "Username of the user") @QueryParam("user") String username,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {
		if (!areExperimentalServicesEnabled()) {
			return Response.status(Status.METHOD_NOT_ALLOWED).build();
		}
		validateAuthentication(authentication);
		validateRequiredParameter(collection, "collection");
		validateRequiredParameter(serviceKey, "serviceKey");

		String token = AuthorizationUtils.getToken(authentication);
		List<TaxonomyDto> taxonomyDtos = taxonomyService.getTaxonomies(host, token, serviceKey, collection, schemaTypeCode, username);
		return Response.ok(taxonomyDtos).build();
	}

	@GET
	@Path("{id}/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get the nodes of a taxonomy")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaxonomyNodeDto.class)))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response getTaxonomyNodes(
			@Parameter(required = true, description = "Taxonomy id") @PathParam("id") String id,
			@Parameter(required = true, description = "Collection code") @QueryParam("collection") String collection,
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(name = "schemaType", description = "Schema type of nodes to return") @QueryParam("schemaType") String schemaTypeCode,
			@Parameter(name = "user", description = "Username of the user") @QueryParam("user") String username,
			@Parameter(name = "metadata", description = "Codes of metadatas to return", example = "[\"title\", \"description\"]") @QueryParam("metadata") Set<String> metadatas,
			@Parameter(name = "rowsStart", description = "Row index to start from") @QueryParam("rowsStart") Integer rowsStart,
			@Parameter(name = "rowsLimit", description = "Number of nodes to return") @QueryParam("rowsLimit") Integer rowsLimit,
			@Parameter(name = "requireWriteAccess", description = "Indicates if only nodes with write access are returned") @QueryParam("writeAccess") Boolean requireWriteAccess,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {
		if (!areExperimentalServicesEnabled()) {
			return Response.status(Status.METHOD_NOT_ALLOWED).build();
		}
		validateAuthentication(authentication);
		validateRequiredParameter(id, "id");
		validateRequiredParameter(collection, "collection");
		validateRequiredParameter(serviceKey, "serviceKey");

		if ((rowsStart != null && rowsLimit == null) || (rowsStart == null && rowsLimit != null)) {
			throw new InvalidParameterCombinationException("rowsStart", "rowsLimit");
		}

		String token = AuthorizationUtils.getToken(authentication);
		List<TaxonomyNodeDto> taxonomyNodeDtos = taxonomyService.getTaxonomyNodes(host, token, serviceKey, id,
				collection, schemaTypeCode, username, metadatas, rowsStart, rowsLimit, requireWriteAccess, true);
		return Response.ok(taxonomyNodeDtos).build();
	}

}
