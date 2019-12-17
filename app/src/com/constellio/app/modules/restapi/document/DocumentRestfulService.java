package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.core.exception.AtLeastOneParameterRequiredException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.ParametersMustMatchException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.util.CustomHttpHeaders;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.document.dto.DocumentContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
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
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Path("documents")
@Tag(name = "documents")
public class DocumentRestfulService extends ResourceRestfulService {

	@Inject
	private DocumentService documentService;

	@GET
	@Path("content")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Operation(summary = "Get content", description = "Stream the content of a document")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = "Content-Disposition", description = "Filename used when saving locally"),
					content = @Content(mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response getContent(@Parameter(required = true, description = "Document Id") @QueryParam("id") String id,
							   @Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
							   @Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"GET"})) @QueryParam("method") String method,
							   @Parameter(required = true, description = "Date") @QueryParam("date") String date,
							   @Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
							   @Parameter(required = true, description = "Document Version. Use 'last' for latest version.") @QueryParam("version") String version,
							   @Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
							   @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParametersIncludingId(id, serviceKey, method, date, expiration, signature);
		validateRequiredParameter(version, "version");
		validateHttpMethod(method, HttpMethods.GET);

		DocumentContentDto documentContentDto = documentService.getContent(host, id, serviceKey, method, date, expiration, version, signature);
		return Response.ok(documentContentDto.getContent(), documentContentDto.getMimeType())
				.header("Content-Disposition", "attachment; filename=\"" + documentContentDto.getFilename() + "\"")
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get document", description = "Return the metadata of a document")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = "ETag", description = "Concurrency control version of the document"),
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Document"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response get(@Parameter(required = true, description = "Document Id") @QueryParam("id") String id,
						@Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
						@Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"GET"})) @QueryParam("method") String method,
						@Parameter(required = true, description = "Date") @QueryParam("date") String date,
						@Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
						@Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
						@Parameter(name = "filter", description = "Fields to filter from the JSON response.", example = "[\"directAces\", \"inheritedAces\"]")
						@QueryParam("filter") Set<String> filters,
						@Parameter(description = "An ETag value can be specified to retrieve a determined version of the document.")
							@HeaderParam(HttpHeaders.IF_MATCH) String eTag,
						@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParametersIncludingId(id, serviceKey, method, date, expiration, signature);
		validateFilterValues(DocumentDto.class, filters);
		validateHttpMethod(method, HttpMethods.GET);

		validateETag(eTag);

		DocumentDto document = documentService.get(host, id, serviceKey, method, date, expiration, signature,
				filters, unquoteETag(eTag));
		return Response.ok(document).tag(document.getETag()).build();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create document", description = "Create a document (content & metadata).<br><br>" +
														  "This is a multipart/form-data request. The body must contains two parts:<br>" +
														  "- A metadata part. This part is the metadata of the document in JSON format. Must have a Content-Type header set to 'application/json' and must be named 'document'.<br>" +
														  "- A file part. This part is the content of the document. Must be named 'file'.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Created", headers = @Header(name = "ETag", description = "Concurrency control version of the document"),
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Document"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response create(
			@Parameter(required = true, description = "Folder Id") @QueryParam("folderId") String folderId,
			@Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"POST"})) @QueryParam("method") String method,
			@Parameter(required = true, description = "Date") @QueryParam("date") String date,
			@Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
			@Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
			@Valid @FormDataParam("document") DocumentDto document,
			@Parameter(schema = @Schema(type = "string", format = "binary")) @FormDataParam("file") InputStream fileStream,
			@Parameter(hidden = true) @FormDataParam("file") FormDataContentDisposition fileHeader,
			@Parameter(description = "Fields to filter from the JSON response.", example = "[\"directAces\", \"inheritedAces\"]")
			@QueryParam("filter") Set<String> filters,
			@Parameter(description = "A document id list can be specified to activate the merge mode.<br>" +
									 "The new document will be created by merging all documents provided in the list.")
			@HeaderParam(CustomHttpHeaders.MERGE_SOURCE) String mergeSourceIds,
			@Parameter(description = "The flushing mode indicates how the commits are executed in solr",
					schema = @Schema(allowableValues = {"NOW, LATER, WITHIN_{X}_SECONDS"})) @DefaultValue("WITHIN_5_SECONDS")
			@HeaderParam(CustomHttpHeaders.FLUSH_MODE) String flush,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParametersIncludingFolderId(folderId, serviceKey, method, date, expiration, signature);
		validateRequiredParameter(document, "document");
		validateFlushValue(flush);
		validateFilterValues(DocumentDto.class, filters);
		validateHttpMethod(method, HttpMethods.POST);

		if (document.getFolderId() == null) {
			throw new RequiredParameterException("document.folderId");
		}
		if (!document.getFolderId().equals(folderId)) {
			throw new ParametersMustMatchException("folderId", "document.folderId");
		}
		if (Strings.isNullOrEmpty(document.getTitle())) {
			throw new RequiredParameterException("document.title");
		}

		List<String> mergeSourceIdList = null;
		if (!StringUtils.isBlank(mergeSourceIds)) {
			mergeSourceIdList = Arrays.asList(mergeSourceIds.split(","));
		}

		validateContent(document, fileStream, mergeSourceIdList);
		validateDocument(document);

		if (document.getContent() != null && document.getContent().getFilename() == null) {
			throw new RequiredParameterException("content.filename");
		}

		DocumentDto createdDocument = mergeSourceIds == null || mergeSourceIds.isEmpty() ?
									  documentService.create(host, folderId, serviceKey, method, date, expiration,
											  signature, document, fileStream, flush, filters) :
									  documentService.merge(host, folderId, serviceKey, method, date, expiration,
											  signature, document, mergeSourceIdList, flush, filters);

		return Response.status(Response.Status.CREATED).entity(createdDocument).tag(createdDocument.getETag()).build();
	}

	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Update document", description = "Update a document (content & metadata).<br><br>" +
														  "This is a multipart/form-data request. The body must contains two parts:<br>" +
														  "- A metadata part. This part is the metadata of the document in JSON format. Must have a Content-Type header set to 'application/json' and must be named 'document'.<br>" +
														  "- A file part. This part is the content of the document. Must be named 'file'.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = "ETag", description = "Concurrency control version of the document"),
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Document"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "409", description = "Conflict. When concurrency control mode (If-Matcher header) is inactive.",
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "412", description = "Precondition Failed. When concurrency control mode (If-Matcher header) is active.",
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response update(@Parameter(required = true, description = "Document Id") @QueryParam("id") String id,
						   @Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
						   @Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"PUT"})) @QueryParam("method") String method,
						   @Parameter(required = true, description = "Date") @QueryParam("date") String date,
						   @Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
						   @Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
						   @Valid @FormDataParam("document") DocumentDto document,
						   @Parameter(schema = @Schema(type = "string", format = "binary")) @FormDataParam("file") InputStream fileStream,
						   @Parameter(hidden = true) @FormDataParam("file") FormDataContentDisposition fileHeader,
						   @Parameter(description = "Fields to filter from the JSON response.", example = "[\"directAces\", \"inheritedAces\"]")
						   @QueryParam("filter") Set<String> filters,
						   @Parameter(description = "An ETag value can be specified to activate the concurrency control mode.<br>" +
													"Using that mode, a request cannot be fulfilled if the ETag value is not the latest concurrency control version of the document.")
						   @HeaderParam(HttpHeaders.IF_MATCH) String eTag,
						   @Parameter(description = "The flushing mode indicates how the commits are executed in solr",
								   schema = @Schema(allowableValues = {"NOW, LATER, WITHIN_{X}_SECONDS"})) @DefaultValue("WITHIN_5_SECONDS")
						   @HeaderParam(CustomHttpHeaders.FLUSH_MODE) String flush,
						   @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParametersIncludingId(id, serviceKey, method, date, expiration, signature);
		validateRequiredParameter(document, "document");
		validateFlushValue(flush);
		validateFilterValues(DocumentDto.class, filters);
		validateHttpMethod(method, HttpMethods.PUT);

		if (!id.equals(document.getId())) {
			throw new ParametersMustMatchException("id", "document.id");
		}
		if (Strings.isNullOrEmpty(document.getTitle())) {
			throw new RequiredParameterException("document.title");
		}
		if (Strings.isNullOrEmpty(document.getFolderId())) {
			throw new RequiredParameterException("document.folderId");
		}

		validateContent(document, fileStream);
		validateDocument(document);

		validateETag(eTag);
		document.setETag(unquoteETag(eTag));

		DocumentDto updatedDocument = documentService.update(host, id, serviceKey, method, date, expiration, signature,
				document, fileStream, false, flush, filters);

		return Response.ok(updatedDocument).tag(updatedDocument.getETag()).build();
	}

	@PATCH
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Patch document", description = "Update a document partially (content & metadata).<br><br>" +
														 "This is a multipart/form-data request. The body must contains two parts:<br>" +
														 "- A metadata part. This part is the metadata of the document in JSON format. Must have a Content-Type header set to 'application/json' and must be named 'document'.<br>" +
														 "- A file part. This part is the content of the document. Must be named 'file'.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = "ETag", description = "Concurrency control version of the document"),
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Document"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "409", description = "Conflict. When concurrency control mode (If-Matcher header) is inactive.",
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "412", description = "Precondition Failed. When concurrency control mode (If-Matcher header) is active.",
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response updatePartial(@Parameter(required = true, description = "Document Id") @QueryParam("id") String id,
								  @Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
								  @Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"PATCH"})) @QueryParam("method") String method,
								  @Parameter(required = true, description = "Date") @QueryParam("date") String date,
								  @Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
								  @Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
								  @Valid @FormDataParam("document") DocumentDto document,
								  @Parameter(schema = @Schema(type = "string", format = "binary")) @FormDataParam("file") InputStream fileStream,
								  @Parameter(hidden = true) @FormDataParam("file") FormDataContentDisposition fileHeader,
								  @Parameter(description = "Fields to filter from the JSON response.") @QueryParam("filter") Set<String> filters,
								  @Parameter(description = "An ETag value can be specified to activate the concurrency control mode.<br>" +
														   "Using that mode, a request cannot be fulfilled if the ETag value is not the latest concurrency control version of the document.")
								  @HeaderParam(HttpHeaders.IF_MATCH) String eTag,
								  @Parameter(description = "The flushing mode indicates how the commits are executed in solr",
										  schema = @Schema(allowableValues = {"NOW, LATER, WITHIN_{X}_SECONDS"})) @DefaultValue("WITHIN_5_SECONDS")
								  @HeaderParam(CustomHttpHeaders.FLUSH_MODE) String flush,
								  @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParametersIncludingId(id, serviceKey, method, date, expiration, signature);
		validateRequiredParameter(document, "document");
		validateFlushValue(flush);
		validateFilterValues(DocumentDto.class, filters);
		validateHttpMethod(method, HttpMethods.PATCH);

		if (!id.equals(document.getId())) {
			throw new ParametersMustMatchException("id", "document.id");
		}

		validateContent(document, fileStream);
		validateDocument(document);

		validateETag(eTag);
		document.setETag(unquoteETag(eTag));

		DocumentDto updatedDocument = documentService.update(host, id, serviceKey, method, date, expiration, signature,
				document, fileStream, true, flush, filters);

		return Response.ok(updatedDocument).tag(updatedDocument.getETag()).build();
	}

	@DELETE
	@Operation(summary = "Delete document", description = "Delete a document")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response delete(@Parameter(required = true, description = "Document Id") @QueryParam("id") String id,
						   @Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
						   @Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"DELETE"})) @QueryParam("method") String method,
						   @Parameter(required = true, description = "Date") @QueryParam("date") String date,
						   @Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
						   @Parameter(description = "Document must be physically deleted", schema = @Schema(type = "boolean", defaultValue = "false")) @QueryParam("physical") String physical,
						   @Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
						   @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParametersIncludingId(id, serviceKey, method, date, expiration, signature);
		validateHttpMethod(method, HttpMethods.DELETE);

		if (physical != null && !physical.equals("true") && !physical.equals("false")) {
			throw new InvalidParameterException("physical", physical);
		}
		Boolean physicalValue = physical != null ? Boolean.valueOf(physical) : null;

		documentService.delete(host, id, serviceKey, method, date, expiration, physicalValue, signature);

		return Response.noContent().build();
	}

	private void validateContent(DocumentDto document, InputStream fileStream, List<String> mergeSourceIds) {
		if (mergeSourceIds == null || mergeSourceIds.isEmpty()) {
			validateContent(document, fileStream);
		} else {
			if (document.getContent() != null) {
				throw new InvalidParameterCombinationException("mergeSourceIds", "content");
			}
			if (fileStream != null) {
				throw new InvalidParameterCombinationException("mergeSourceIds", "fileStream");
			}
		}
	}

	private void validateContent(DocumentDto document, InputStream fileStream) {
		if (document.getContent() == null && fileStream != null) {
			throw new RequiredParameterException("document.content");
		}
		if (document.getContent() != null && fileStream == null) {
			throw new RequiredParameterException("file");
		}
	}

	private void validateDocument(DocumentDto document) {
		if (document.getType() != null) {
			if (Strings.isNullOrEmpty(document.getType().getId()) && Strings.isNullOrEmpty(document.getType().getCode())) {
				throw new AtLeastOneParameterRequiredException("type.id", "type.code");
			}
			if (!Strings.isNullOrEmpty(document.getType().getId()) && !Strings.isNullOrEmpty(document.getType().getCode())) {
				throw new InvalidParameterCombinationException("type.id", "type.code");
			}
		}

		validateAces(document.getDirectAces());
	}

}
