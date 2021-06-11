package com.constellio.app.modules.restapi.apis.v1.record;

import com.constellio.app.modules.restapi.apis.v1.core.BaseRestfulService;
import com.constellio.app.modules.restapi.apis.v1.record.dto.MetadataDto;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

// IMPORTANT : hidden from swagger doc, only used by teams
@Hidden
@Path("record")
@Tag(name = "record")
public class RecordRestfulService extends BaseRestfulService {

	@Inject
	private RecordService recordService;

	@GET
	@Path("metadata")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get record metadata value", description = "Return the metadata value of the specified record.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Metadata"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response getRecordMetadata(
			@Parameter(required = true, description = "Record id") @QueryParam("id") String id,
			@Parameter(required = true, description = "Metadata code") @QueryParam("metadataCode") String metadataCode,
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {
		if (!areExperimentalServicesEnabled()) {
			return Response.status(Status.METHOD_NOT_ALLOWED).build();
		}
		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(id, "id");
		validateRequiredParameter(metadataCode, "metadataCode");

		String token = AuthorizationUtils.getToken(authentication);
		MetadataDto metadataDto = recordService.getRecordMetadata(host, token, serviceKey, id, metadataCode);
		return Response.ok(metadataDto).build();
	}

	@POST
	@Path("metadata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Set record metadata value", description = "Set the metadata value of the specified record.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response setRecordMetadata(
			@Parameter(required = true, description = "Record id") @QueryParam("id") String id,
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@Valid MetadataDto metadata,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {
		if (!areExperimentalServicesEnabled()) {
			return Response.status(Status.METHOD_NOT_ALLOWED).build();
		}
		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(id, "id");
		validateRequiredParameter(metadata, "metadata");
		validateRequiredParameter(metadata.getCode(), "metadata.code");

		String token = AuthorizationUtils.getToken(authentication);
		recordService.setRecordMetadata(host, token, serviceKey, id, metadata);
		return Response.noContent().build();
	}
}
