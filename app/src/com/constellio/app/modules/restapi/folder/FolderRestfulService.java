package com.constellio.app.modules.restapi.folder;

import com.constellio.app.modules.restapi.core.exception.AtLeastOneParameterRequiredException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.ParametersMustMatchException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.util.CustomHttpHeaders;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.resource.service.ResourceRestfulService;
import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PATCH;
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
						@Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"GET"})) @QueryParam("method") String method,
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
	@Operation(summary = "Create folder", description = "Create a folder<br><br>" +
														"To copy a folder, use " + CustomHttpHeaders.COPY_SOURCE + " header.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Created", headers = @Header(name = "ETag", description = "Concurrency control version of the folder"),
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Folder"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response create(@Parameter(description = "Parent Folder Id") @QueryParam("folderId") String folderId,
						   @Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
						   @Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"PUT"})) @QueryParam("method") String method,
						   @Parameter(required = true, description = "Date") @QueryParam("date") String date,
						   @Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
						   @Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
						   @Valid FolderDto folder,
						   @Parameter(description = "Fields to filter from the JSON response.", example = "[\"directAces\", \"inheritedAces\"]")
						   @QueryParam("filter") Set<String> filters,
						   @Parameter(description = "A folder id can be specified to activate the copy mode.<br>" +
													"The folder structure and all files will be copied. Any values specified in the body will be applied on the copied folder.")
							   @HeaderParam(CustomHttpHeaders.COPY_SOURCE) String copySourceId,
						   @Parameter(description = "The flushing mode indicates how the commits are executed in solr",
								   schema = @Schema(allowableValues = {"NOW, LATER, WITHIN_{X}_SECONDS"})) @DefaultValue("WITHIN_5_SECONDS")
						   @HeaderParam(CustomHttpHeaders.FLUSH_MODE) String flush,
						   @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParameters(serviceKey, method, date, expiration, signature);
		validateFlushValue(flush);
		validateFilterValues(FolderDto.class, filters);
		validateHttpMethod(method, HttpMethods.POST);

		if (folderId != null && folder != null &&
			(folder.getParentFolderId() == null || !folder.getParentFolderId().equals(folderId))) {
			throw new ParametersMustMatchException("folderId", "folder.parentFolderId");
		}

		if (copySourceId == null) {
			if (folder == null) {
				throw new RequiredParameterException("folder");
			}
			if (Strings.isNullOrEmpty(folder.getTitle())) {
				throw new RequiredParameterException("folder.title");
			}
			if (folder.getCategory() == null) {
				throw new RequiredParameterException("folder.category");
			}
			if (folder.getAdministrativeUnit() == null) {
				throw new RequiredParameterException("folder.administrativeUnit");
			}
			if (folder.getOpeningDate() == null) {
				throw new RequiredParameterException("folder.openingDate");
			}
		}

		validateFolder(folder);

		FolderDto createdFolder = copySourceId != null ?
								  folderService.copy(host, folderId, copySourceId, serviceKey, method, date, expiration,
										  signature, folder, flush, filters) :
								  folderService.create(host, folderId, serviceKey, method, date, expiration,
										  signature, folder, flush, filters);

		return Response.status(Response.Status.CREATED).entity(createdFolder).tag(createdFolder.getETag()).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update folder", description = "Update a folder")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = "ETag", description = "Concurrency control version of the folder"),
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Folder"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "409", description = "Conflict. When concurrency control mode (If-Matcher header) is inactive.",
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "412", description = "Precondition Failed. When concurrency control mode (If-Matcher header) is active.",
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response update(@Parameter(required = true, description = "Folder Id") @QueryParam("id") String id,
						   @Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
						   @Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"PUT"})) @QueryParam("method") String method,
						   @Parameter(required = true, description = "Date") @QueryParam("date") String date,
						   @Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
						   @Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
						   @Valid FolderDto folder,
						   @Parameter(description = "Fields to filter from the JSON response.", example = "[\"directAces\", \"inheritedAces\"]")
						   @QueryParam("filter") Set<String> filters,
						   @Parameter(description = "An ETag value can be specified to activate the concurrency control mode.<br>" +
													"Using that mode, a request cannot be fulfilled if the ETag value is not the latest concurrency control version of the folder.")
						   @HeaderParam(HttpHeaders.IF_MATCH) String eTag,
						   @Parameter(description = "The flushing mode indicates how the commits are executed in solr",
								   schema = @Schema(allowableValues = {"NOW, LATER, WITHIN_{X}_SECONDS"})) @DefaultValue("WITHIN_5_SECONDS")
						   @HeaderParam(CustomHttpHeaders.FLUSH_MODE) String flush,
						   @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParametersIncludingId(id, serviceKey, method, date, expiration, signature);
		validateRequiredParameter(folder, "folder");
		validateFlushValue(flush);
		validateFilterValues(FolderDto.class, filters);
		validateHttpMethod(method, HttpMethods.PUT);

		if (!id.equals(folder.getId())) {
			throw new ParametersMustMatchException("id", "folder.id");
		}
		if (Strings.isNullOrEmpty(folder.getTitle())) {
			throw new RequiredParameterException("folder.title");
		}
		if (folder.getCategory() == null) {
			throw new RequiredParameterException("folder.category");
		}
		if (folder.getAdministrativeUnit() == null) {
			throw new RequiredParameterException("folder.administrativeUnit");
		}
		if (folder.getOpeningDate() == null) {
			throw new RequiredParameterException("folder.openingDate");
		}

		validateFolder(folder);

		validateETag(eTag);
		folder.setETag(unquoteETag(eTag));

		FolderDto folderDto = folderService.update(host, id, serviceKey, method, date, expiration, signature, folder, true, flush, filters);

		return Response.ok(folderDto).tag(folderDto.getETag()).build();
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Patch folder", description = "Update a folder partially")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = "ETag", description = "Concurrency control version of the folder"),
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Folder"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "409", description = "Conflict. When concurrency control mode (If-Matcher header) is inactive.",
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "412", description = "Precondition Failed. When concurrency control mode (If-Matcher header) is active.",
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response updatePartial(@Parameter(required = true, description = "Folder Id") @QueryParam("id") String id,
								  @Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
								  @Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"PATCH"})) @QueryParam("method") String method,
								  @Parameter(required = true, description = "Date") @QueryParam("date") String date,
								  @Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
								  @Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
								  @Valid FolderDto folder,
								  @Parameter(description = "Fields to filter from the JSON response.", example = "[\"directAces\", \"inheritedAces\"]")
								  @QueryParam("filter") Set<String> filters,
								  @Parameter(description = "An ETag value can be specified to activate the concurrency control mode.<br>" +
														   "Using that mode, a request cannot be fulfilled if the ETag value is not the latest concurrency control version of the folder.")
								  @HeaderParam(HttpHeaders.IF_MATCH) String eTag,
								  @Parameter(description = "The flushing mode indicates how the commits are executed in solr",
										  schema = @Schema(allowableValues = {"NOW, LATER, WITHIN_{X}_SECONDS"})) @DefaultValue("WITHIN_5_SECONDS")
								  @HeaderParam(CustomHttpHeaders.FLUSH_MODE) String flush,
								  @HeaderParam(HttpHeaders.HOST) String host) throws Exception {


		validateRequiredParametersIncludingId(id, serviceKey, method, date, expiration, signature);
		validateRequiredParameter(folder, "folder");
		validateFlushValue(flush);
		validateFilterValues(FolderDto.class, filters);
		validateHttpMethod(method, HttpMethods.PATCH);

		if (!id.equals(folder.getId())) {
			throw new ParametersMustMatchException("id", "folder.id");
		}

		validateFolder(folder);

		validateETag(eTag);
		folder.setETag(unquoteETag(eTag));

		FolderDto folderDto = folderService.update(host, id, serviceKey, method, date, expiration, signature, folder, true, flush, filters);

		return Response.ok(folderDto).tag(folderDto.getETag()).build();
	}

	@DELETE
	@Operation(summary = "Delete folder", description = "Delete a folder")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response delete(@Parameter(required = true, description = "Folder Id") @QueryParam("id") String id,
						   @Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
						   @Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"DELETE"})) @QueryParam("method") String method,
						   @Parameter(required = true, description = "Date") @QueryParam("date") String date,
						   @Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
						   @Parameter(description = "Folder must be physically deleted", schema = @Schema(type = "boolean", defaultValue = "false")) @QueryParam("physical") String physical,
						   @Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
						   @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParametersIncludingId(id, serviceKey, method, date, expiration, signature);
		validateHttpMethod(method, HttpMethods.DELETE);

		if (physical != null && !physical.equals("true") && !physical.equals("false")) {
			throw new InvalidParameterException("physical", physical);
		}
		Boolean physicalValue = physical != null ? Boolean.valueOf(physical) : null;

		folderService.delete(host, id, serviceKey, method, date, expiration, physicalValue, signature);

		return Response.noContent().build();
	}

	private void validateFolder(FolderDto folder) {
		if (folder == null) {
			return;
		}

		if (folder.getType() != null) {
			if (Strings.isNullOrEmpty(folder.getType().getId()) && Strings.isNullOrEmpty(folder.getType().getCode())) {
				throw new AtLeastOneParameterRequiredException("type.id", "type.code");
			}
			if (!Strings.isNullOrEmpty(folder.getType().getId()) && !Strings.isNullOrEmpty(folder.getType().getCode())) {
				throw new InvalidParameterCombinationException("type.id", "type.code");
			}
		}

		validateAces(folder.getDirectAces());
	}
}
