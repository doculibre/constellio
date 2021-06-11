package com.constellio.app.modules.restapi.apis.v1.url;

import com.constellio.app.modules.restapi.core.exception.AtLeastOneParameterRequiredException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterWithHttpMethodException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.core.util.CustomHttpHeaders;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.constellio.app.modules.restapi.core.util.HttpMethods.DELETE;
import static com.constellio.app.modules.restapi.core.util.HttpMethods.GET;
import static com.constellio.app.modules.restapi.core.util.HttpMethods.POST;

@Path("urls")
@Tag(name = "urls")
public class UrlRestfulService {

	@Inject
	private UrlService urlService;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "Get url", description = "Return a pre-signed URL")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestApiErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response get(@Parameter(required = true, description = "Token") @QueryParam("token") String token,
						@Parameter(required = true, description = "Service Key") @QueryParam("serviceKey") String serviceKey,
						@Parameter(required = true, description = "Schema Type") @QueryParam("schemaType") SchemaTypes schemaType,
						@Parameter(required = true, description = "HTTP Method", schema = @Schema(allowableValues = {"GET", "PATCH", "POST", "PUT", "DELETE"})) @QueryParam("method") String method,
						@Parameter(description = "Id") @QueryParam("id") String id,
						@Parameter(description = "Folder Id (POST only)") @QueryParam("folderId") String folderId,
						@Parameter(required = true, description = "Expiration in seconds") @QueryParam("expiration") Integer expiration,
						@Parameter(description = "Version (GET only). Set to 'last' for latest version.") @QueryParam("version") String version,
						@Parameter(description = "Physical (DELETE only). Set to true to physically delete the resource.",
								schema = @Schema(type = "boolean", defaultValue = "false")) @QueryParam("physical") String physical,
						@HeaderParam(HttpHeaders.HOST) String host,
						@HeaderParam(CustomHttpHeaders.COPY_SOURCE) String copySourceId) throws Exception {

		if (token == null) {
			throw new RequiredParameterException("token");
		}
		if (serviceKey == null) {
			throw new RequiredParameterException("serviceKey");
		}
		if (schemaType == null) {
			throw new RequiredParameterException("schemaType");
		}
		if (method == null) {
			throw new RequiredParameterException("method");
		}
		if (expiration == null) {
			throw new RequiredParameterException("expiration");
		}

		if (!HttpMethods.contains(method)) {
			throw new InvalidParameterException("method", method);
		}

		if (!Strings.isNullOrEmpty(id) && !Strings.isNullOrEmpty(folderId)) {
			throw new InvalidParameterCombinationException("id", "folderId");
		}

		if (schemaType == SchemaTypes.DOCUMENT) {
			if (Strings.isNullOrEmpty(id) && Strings.isNullOrEmpty(folderId)) {
				throw new AtLeastOneParameterRequiredException("id", "folderId");
			}
			if (Strings.isNullOrEmpty(folderId) && method.equals(POST)) {
				throw new RequiredParameterException("folderId");
			}
		} else if (schemaType == SchemaTypes.FOLDER) {
			if (Strings.isNullOrEmpty(id) && Strings.isNullOrEmpty(folderId) && !method.equals(POST)) {
				throw new AtLeastOneParameterRequiredException("id", "folderId");
			}
		}

		if (!Strings.isNullOrEmpty(id) && method.equals(POST)) {
			throw new InvalidParameterWithHttpMethodException("id", method);
		}

		if (!Strings.isNullOrEmpty(physical) && !method.equals(DELETE)) {
			throw new InvalidParameterWithHttpMethodException("physical", method);
		}

		if (!Strings.isNullOrEmpty(version) && !method.equals(GET)) {
			throw new InvalidParameterWithHttpMethodException("version", method);
		}

		if (physical != null && !physical.equals("true") && !physical.equals("false")) {
			throw new InvalidParameterException("physical", physical);
		}

		if (copySourceId != null && !method.equals(POST)) {
			throw new InvalidParameterWithHttpMethodException("copySourceId", copySourceId);
		}

		String url = urlService.getSignedUrl(host, token, serviceKey, schemaType, method, id, folderId,
				String.valueOf(expiration), version, physical, copySourceId);
		return Response.ok(url).build();
	}

}
