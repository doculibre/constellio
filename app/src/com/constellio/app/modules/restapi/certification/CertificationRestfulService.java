package com.constellio.app.modules.restapi.certification;

import com.constellio.app.modules.restapi.certification.dto.CertificationDto;
import com.constellio.app.modules.restapi.certification.dto.RectangleDto;
import com.constellio.app.modules.restapi.core.exception.AtLeastOneParameterRequiredException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.util.CustomHttpHeaders;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
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
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Path("certification")
@Tag(name = "certification")
public class CertificationRestfulService extends ResourceRestfulService {

	@Inject
	private CertificationService certificationService;

	@Context
	private HttpServletRequest httpRequest;

	@javax.ws.rs.POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create Signature Annotation", description = "Create a signature annotation json (content & metadata) for now compatible for PDFJS.<br><br>" +
																	  "This is a json request,  the metadata of the annotation is in JSON format. " +
																	  "Must have a Content-Type header set to 'application/json' and must be named 'certification'.<br>")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Created", headers = @Header(name = "ETag", description = "Concurrency control version of the document"),
					content = @Content(mediaType = "application/json", schema = @Schema(ref = "Document"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response create(
			@Parameter(required = true, description = "Document Id") @QueryParam("documentId") String documentId,
			@Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"POST"})) @QueryParam("method") String method,
			@Parameter(required = true, description = "Date") @QueryParam("date") String date,
			@Parameter(required = true, description = "Expiration") @QueryParam("expiration") Integer expiration,
			@Parameter(required = true, description = "Signature") @QueryParam("signature") String signature,
			@Valid @FormDataParam("certification") CertificationDto certification,
			@Parameter(description = "A document id list can be specified to activate the merge mode.<br>" +
									 "The id list must be provided as a string without space and each id separated by a comma.<br>" +
									 "The new document will be created by merging all documents provided in the list.")
			@HeaderParam(CustomHttpHeaders.MERGE_SOURCE) String mergeSourceIds,
			@Parameter(description = "The flushing mode indicates how the commits are executed in solr",
					schema = @Schema(allowableValues = {"NOW, LATER, WITHIN_{X}_SECONDS"})) @DefaultValue("WITHIN_5_SECONDS")
			@HeaderParam(CustomHttpHeaders.FLUSH_MODE) String flush,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateRequiredParametersIncludingId(documentId, serviceKey, method, date, expiration, signature);
		validateRequiredParameter(certification, "certification");
		validateFlushValue(flush);
		validateHttpMethod(method, HttpMethods.POST);

		if (certification.getDocumentId() == null) {
			throw new RequiredParameterException("certification.documentId");
		}

		List<String> documentIdsToMerge = null;
		if (StringUtils.isNotBlank(mergeSourceIds)) {
			documentIdsToMerge = Arrays.asList(mergeSourceIds.split(","));
		}

		validateCertification(certification);

		CertificationDto createdCertification = //CollectionUtils.isEmpty(documentIdsToMerge) ?
				certificationService.create(host, documentId, serviceKey, method, date, expiration,
						signature, certification, flush, isUrlValidated()); //:
		//certificationService.merge(host, documentId, serviceKey, method, date, expiration,
		//		signature, certification, documentIdsToMerge, flush, isUrlValidated());

		return Response.status(Response.Status.CREATED).entity(createdCertification).tag(createdCertification.getETag()).build();
	}

	private void validateCertification(CertificationDto certification) {
		if (Strings.isNullOrEmpty(certification.getImageData())) {
			throw new AtLeastOneParameterRequiredException("type.id", "type.code");
		}
		validateRectangle(certification.getPosition());
	}

	protected void validateRectangle(RectangleDto rectangle) {

		if (rectangle == null) {
			throw new InvalidParameterException("Rectangle is null", "null");
		}
	}

	private boolean isUrlValidated() {
		return httpRequest.getAttribute("urlValidated") != null;
	}
}

