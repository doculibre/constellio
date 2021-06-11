package com.constellio.app.modules.restapi.apis.v2.document;

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

@Path("documents")
@Tag(name = "documents")
public class DocumentRestfulServiceV2 extends BaseRestfulServiceV2 {

	@Inject
	private DocumentServiceV2 documentService;

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get document", description = "Return the metadata of a document")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK",
					headers = @Header(name = "ETag", description = "Concurrency control version of the document", schema = @Schema(type = "string")),
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Record"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response getById(@Parameter(required = true, description = "Document id") @PathParam("id") String id,
							@Parameter(description = "Filter mode") @DefaultValue("SUMMARY") @QueryParam("filterMode") FilterMode filterMode,
							@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
							@Parameter(description = "An ETag value can be specified to retrieve a determined version of the document.") @HeaderParam(HttpHeaders.IF_MATCH) String eTag,
							@Parameter(hidden = true) @HeaderParam(HttpHeaders.HOST) String host)
			throws Exception {

		validateRequiredParameter(id, "id");
		validateAuthentication(authentication);
		validateETag(eTag);

		String token = AuthorizationUtils.getToken(authentication);
		RecordDtoV2 document = documentService.get(id, filterMode, token, host, unquoteETag(eTag));

		return Response.ok(document).tag(document.getVersion()).build();
	}

}
