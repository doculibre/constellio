package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.core.exception.*;
import com.constellio.app.modules.restapi.core.util.*;
import com.constellio.app.modules.restapi.document.dto.AceDto;
import com.constellio.app.modules.restapi.document.dto.DocumentContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Set;
import java.util.regex.Pattern;

@Path("documents")
@Tag(name = "documents")
public class DocumentRestfulService {

	@Inject
	private DocumentService documentService;

	private static Pattern FLUSH_WITHIN_PATTERN = Pattern.compile("^WITHIN_\\d+_SECONDS$");

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

		validateRequiredParameters(serviceKey, method, date, expiration, signature);
		if (id == null) {
			throw new RequiredParameterException("id");
		}
		if (version == null) {
			throw new RequiredParameterException("version");
		}

		if (!method.equals(HttpMethods.GET)) {
			throw new InvalidParameterException("method", method);
		}

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
						@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParameters(serviceKey, method, date, expiration, signature);
		if (id == null) {
			throw new RequiredParameterException("id");
		}
		validateFilterValues(filters);

		if (!method.equals(HttpMethods.GET)) {
			throw new InvalidParameterException("method", method);
		}

		DocumentDto document = documentService.get(host, id, serviceKey, method, date, expiration, signature, filters);

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
			@Parameter(description = "The flushing mode indicates how the commits are executed in solr",
					schema = @Schema(allowableValues = {"NOW, LATER, WITHIN_{X}_SECONDS"})) @DefaultValue("WITHIN_5_SECONDS")
			@HeaderParam(CustomHttpHeaders.FLUSH_MODE) String flush,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParameters(serviceKey, method, date, expiration, signature);
		if (folderId == null) {
			throw new RequiredParameterException("folderId");
		}
		if (document == null) {
			throw new RequiredParameterException("document");
		}

		validateFlushValue(flush);
		validateFilterValues(filters);

		if (!method.equals(HttpMethods.POST)) {
			throw new InvalidParameterException("method", method);
		}

		if (document.getFolderId() == null) {
			throw new RequiredParameterException("document.folderId");
		}
		if (!document.getFolderId().equals(folderId)) {
			throw new ParametersMustMatchException("folderId", "document.folderId");
		}
		if (Strings.isNullOrEmpty(document.getTitle())) {
			throw new RequiredParameterException("document.title");
		}

		validateDocument(document, fileStream);

		if (document.getContent() != null && document.getContent().getFilename() == null) {
			throw new RequiredParameterException("content.filename");
		}

		DocumentDto createdDocument = documentService.create(host, folderId, serviceKey, method, date, expiration,
				signature, document, fileStream, flush, filters);

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

		validateRequiredParameters(serviceKey, method, date, expiration, signature);
		if (id == null) {
			throw new RequiredParameterException("id");
		}
		if (document == null) {
			throw new RequiredParameterException("document");
		}

		validateFlushValue(flush);
		validateFilterValues(filters);

		if (!method.equals(HttpMethods.PUT)) {
			throw new InvalidParameterException("method", method);
		}

		if (!id.equals(document.getId())) {
			throw new ParametersMustMatchException("id", "document.id");
		}
		if (Strings.isNullOrEmpty(document.getTitle())) {
			throw new RequiredParameterException("document.title");
		}
		if (Strings.isNullOrEmpty(document.getFolderId())) {
			throw new RequiredParameterException("document.folderId");
		}

		validateDocument(document, fileStream);

		if (eTag != null && !StringUtils.isUnsignedLong(eTag)) {
			throw new InvalidParameterException("ETag", eTag);
		}
		document.setETag(eTag);

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

		validateRequiredParameters(serviceKey, method, date, expiration, signature);
		if (id == null) {
			throw new RequiredParameterException("id");
		}
		if (document == null) {
			throw new RequiredParameterException("document");
		}

		validateFlushValue(flush);
		validateFilterValues(filters);

		if (!method.equals(HttpMethods.PATCH)) {
			throw new InvalidParameterException("method", method);
		}

		if (!id.equals(document.getId())) {
			throw new ParametersMustMatchException("id", "document.id");
		}

		validateDocument(document, fileStream);

		if (eTag != null && !StringUtils.isUnsignedLong(eTag)) {
			throw new InvalidParameterException("ETag", eTag);
		}
		document.setETag(eTag);

		DocumentDto updatedDocument = documentService.update(host, id, serviceKey, method, date, expiration, signature,
				document, fileStream, true, flush, filters);

		return Response.ok(updatedDocument).tag(document.getETag()).build();
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

		validateRequiredParameters(serviceKey, method, date, expiration, signature);
		if (id == null) {
			throw new RequiredParameterException("id");
		}

		if (!method.equals(HttpMethods.DELETE)) {
			throw new InvalidParameterException("method", method);
		}

		if (physical != null && !physical.equals("true") && !physical.equals("false")) {
			throw new InvalidParameterException("physical", physical);
		}
		Boolean physicalValue = physical != null ? Boolean.valueOf(physical) : null;

		documentService.delete(host, id, serviceKey, method, date, expiration, physicalValue, signature);

		return Response.noContent().build();
	}

	private void validateRequiredParameters(String serviceKey, String method, String date, Integer expiration,
											String signature) {
		if (serviceKey == null) {
			throw new RequiredParameterException("serviceKey");
		}
		if (method == null) {
			throw new RequiredParameterException("method");
		}
		if (expiration == null) {
			throw new RequiredParameterException("expiration");
		}
		if (date == null) {
			throw new RequiredParameterException("date");
		}
		if (signature == null) {
			throw new RequiredParameterException("signature");
		}
	}

	private void validateDocument(DocumentDto document, InputStream fileStream) {
		if (document.getContent() == null && fileStream != null) {
			throw new RequiredParameterException("document.content");
		}
		if (document.getContent() != null && fileStream == null) {
			throw new RequiredParameterException("file");
		}

		if (document.getType() != null) {
			if (Strings.isNullOrEmpty(document.getType().getId()) && Strings.isNullOrEmpty(document.getType().getCode())) {
				throw new AtLeastOneParameterRequiredException("type.id", "type.code");
			}
			if (!Strings.isNullOrEmpty(document.getType().getId()) && !Strings.isNullOrEmpty(document.getType().getCode())) {
				throw new InvalidParameterCombinationException("type.id", "type.code");
			}
		}

		for (int i = 0; i < ListUtils.nullToEmpty(document.getDirectAces()).size(); i++) {
			AceDto ace = document.getDirectAces().get(i);
			for (String permission : ace.getPermissions()) {
				if (!Permissions.contains(permission)) {
					throw new InvalidParameterException(String.format("directAces[%d].permissions", i), permission);
				}
			}
		}
	}

	private void validateFlushValue(String value) {
		if (value.equals("NOW") || value.equals("LATER")) {
			return;
		}

		if (!FLUSH_WITHIN_PATTERN.matcher(value).matches()) {
			throw new InvalidParameterException(CustomHttpHeaders.FLUSH_MODE, value);
		}

		String seconds = value.split("_")[1];
		if (seconds.equals("0") || !StringUtils.isUnsignedInteger(seconds + "000")) {
			throw new InvalidParameterException(CustomHttpHeaders.FLUSH_MODE, value);
		}
	}

	private void validateFilterValues(Set<String> values) {
		for (String value : values) {
			try {
				DocumentDto.class.getDeclaredField(value);
			} catch (NoSuchFieldException e) {
				throw new InvalidParameterException("filter", value);
			}
		}
	}

}
