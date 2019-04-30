package com.constellio.app.modules.restapi.folder;

import com.constellio.app.modules.restapi.core.service.ResourceRestfulService;
import com.constellio.app.modules.restapi.core.util.CustomHttpHeaders;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("folders")
@Tag(name = "folders")
public class FolderRestfulService extends ResourceRestfulService {

	@Inject
	private FolderService folderService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get folder", description = "Return the metadata of a folder")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = "ETag", description = "Concurrency control version of the folder"),
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Folder"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response get(@Parameter(required = true, description = "Folder Id") @QueryParam("id") String id,
						@Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
						@Parameter(required = true, description = "HTTP Method") @QueryParam("method") String method,
						@Parameter(required = true, description = "Date") @QueryParam("date") String date,
						@Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
						@Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
						@Parameter(name = "filter", description = "Fields to filter from the JSON response.", example = "[\"directAces\", \"inheritedAces\"]")
						@QueryParam("filter") Set<String> filters,
						@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParametersIncludingId(id, serviceKey, method, date, expiration, signature);
		validateFilterValues(FolderDto.class, filters);
		validateHttpMethod(method, HttpMethods.GET);

		FolderDto folder = folderService.get(host, id, serviceKey, method, date, expiration, signature, filters);
		return Response.ok(folder).tag(folder.getETag()).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(@QueryParam("folderId") String folderId,
						   @QueryParam("serviceKey") String serviceKey,
						   @QueryParam("method") String method,
						   @QueryParam("date") String date,
						   @QueryParam("expiration") Integer expiration,
						   @QueryParam("signature") String signature,
						   @Valid @FormDataParam("folder") FolderDto folder,
						   @QueryParam("filter") Set<String> filters,
						   @HeaderParam(CustomHttpHeaders.COPY_SOURCE) String copySource,
						   @DefaultValue("WITHIN_5_SECONDS") @HeaderParam(CustomHttpHeaders.FLUSH_MODE) String flush,
						   @HeaderParam(HttpHeaders.HOST) String host) throws Exception {
		// TODO
		// if copySource not empty, copy the folder, otherwise create the FolderDto received
		return Response.noContent().build();
	}


	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@QueryParam("id") String id,
						   @QueryParam("serviceKey") String serviceKey,
						   @QueryParam("method") String method,
						   @QueryParam("date") String date,
						   @QueryParam("expiration") Integer expiration,
						   @QueryParam("signature") String signature,
						   @Valid @FormDataParam("folder") FolderDto folder,
						   @QueryParam("filter") Set<String> filters,
						   @HeaderParam(HttpHeaders.IF_MATCH) String eTag,
						   @DefaultValue("WITHIN_5_SECONDS") @HeaderParam(CustomHttpHeaders.FLUSH_MODE) String flush,
						   @HeaderParam(HttpHeaders.HOST) String host) throws Exception {
		// TODO
		return Response.noContent().build();
	}


}
