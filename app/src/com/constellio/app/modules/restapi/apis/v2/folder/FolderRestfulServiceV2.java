package com.constellio.app.modules.restapi.apis.v2.folder;

import com.constellio.app.modules.restapi.apis.v2.core.BaseRestfulServiceV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FilterMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordDtoV2;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("folders")
@Tag(name = "folders")
public class FolderRestfulServiceV2 extends BaseRestfulServiceV2 {

	@Inject
	private FolderServiceV2 folderService;

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get folder", description = "Return the metadata of a folder")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK",
					headers = @Header(name = "ETag", description = "Concurrency control version of the folder", schema = @Schema(type = "string")),
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecordDtoV2.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response getById(@Parameter(required = true, description = "Folder id") @PathParam("id") String id,
							@Parameter(description = "Filter mode") @DefaultValue("SUMMARY") @QueryParam("filterMode") FilterMode filterMode,
							@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
							@Parameter(description = "An ETag value can be specified to retrieve a determined version of the folder.") @HeaderParam(HttpHeaders.IF_MATCH) String eTag,
							@Parameter(hidden = true) @HeaderParam(HttpHeaders.HOST) String host)
			throws Exception {

		validateRequiredParameter(id, "id");
		validateAuthentication(authentication);
		validateETag(eTag);

		String token = AuthorizationUtils.getToken(authentication);
		RecordDtoV2 folder = folderService.get(id, filterMode, token, host, unquoteETag(eTag));

		return Response.ok(folder).tag(folder.getVersion()).build();
	}

}
