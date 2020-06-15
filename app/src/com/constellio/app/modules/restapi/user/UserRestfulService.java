package com.constellio.app.modules.restapi.user;

import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.service.BaseRestfulService;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import com.constellio.app.modules.restapi.user.dto.UserConfigDto;
import com.constellio.app.modules.restapi.user.dto.UserSignatureContentDto;
import com.constellio.app.modules.restapi.user.dto.UserSignatureDto;
import com.constellio.model.entities.security.global.UserCredential;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("user")
@Tag(name = "user")
public class UserRestfulService extends BaseRestfulService {

	@Inject
	private UserService userService;

	@GET
	@Path("credentials/signature")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Operation(summary = "Get user signature", description = "Stream the content of the user profile signature")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = "Content-Disposition", description = "Filename used when saving locally"),
					content = @Content(mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary"))),
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response getSignature(
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");

		String token = AuthorizationUtils.getToken(authentication);

		UserSignatureContentDto contentDto = userService.getContent(host, token, serviceKey, UserCredential.ELECTRONIC_SIGNATURE);
		return Response.ok(contentDto.getContent(), contentDto.getMimeType())
				.header("Content-Disposition", "attachment; filename=\"" + contentDto.getFilename() + "\"")
				.build();
	}

	@POST
	@Path("credentials/signature")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Operation(summary = "Set user signature", description = "Set the content of the user profile signature.<br><br>" +
															 "This is a multipart/form-data request. The body must contains two parts:<br>" +
															 "- A metadata part. This part is the metadata of the content in JSON format. Must have a Content-Type header set to 'application/json' and must be named 'userSignature'.<br>" +
															 "- A file part. This part is the content of the user signature. Must be named 'file'.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response setSignature(
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@Valid @FormDataParam("userSignature") UserSignatureDto userSignature,
			@Parameter(schema = @Schema(type = "string", format = "binary")) @FormDataParam("file") InputStream fileStream,
			@Parameter(hidden = true) @FormDataParam("file") FormDataContentDisposition fileHeader,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");

		String token = AuthorizationUtils.getToken(authentication);

		validateRequiredParameter(userSignature, "userSignature");
		validateRequiredParameter(fileStream, "file");

		if (Strings.isNullOrEmpty(userSignature.getFilename())) {
			throw new RequiredParameterException("userSignature.filename");
		}

		userService.setContent(host, token, serviceKey, UserCredential.ELECTRONIC_SIGNATURE, userSignature, fileStream);
		return Response.noContent().build();
	}

	@DELETE
	@Path("credentials/signature")
	@Operation(summary = "Delete user signature", description = "Delete the content of the user profile signature")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response deleteSignature(
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");

		String token = AuthorizationUtils.getToken(authentication);

		userService.deleteContent(host, token, serviceKey, UserCredential.ELECTRONIC_SIGNATURE);
		return Response.noContent().build();
	}

	@GET
	@Path("credentials/initials")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Operation(summary = "Get user initials", description = "Stream the content of the user profile initials")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = "Content-Disposition", description = "Filename used when saving locally"),
					content = @Content(mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary"))),
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response getInitials(
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");

		String token = AuthorizationUtils.getToken(authentication);

		UserSignatureContentDto contentDto = userService.getContent(host, token, serviceKey, UserCredential.ELECTRONIC_INITIALS);
		return Response.ok(contentDto.getContent(), contentDto.getMimeType())
				.header("Content-Disposition", "attachment; filename=\"" + contentDto.getFilename() + "\"")
				.build();
	}

	@POST
	@Path("credentials/initials")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Operation(summary = "Set user initials", description = "Set the content of the user profile initials.<br><br>" +
															"This is a multipart/form-data request. The body must contains two parts:<br>" +
															"- A metadata part. This part is the metadata of the content in JSON format. Must have a Content-Type header set to 'application/json' and must be named 'userInitials'.<br>" +
															"- A file part. This part is the content of the user signature. Must be named 'file'.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response setInitials(
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@Valid @FormDataParam("userInitials") UserSignatureDto userInitials,
			@Parameter(schema = @Schema(type = "string", format = "binary")) @FormDataParam("file") InputStream fileStream,
			@Parameter(hidden = true) @FormDataParam("file") FormDataContentDisposition fileHeader,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");

		String token = AuthorizationUtils.getToken(authentication);

		validateRequiredParameter(userInitials, "userInitials");
		validateRequiredParameter(fileStream, "file");

		if (Strings.isNullOrEmpty(userInitials.getFilename())) {
			throw new RequiredParameterException("userInitials.filename");
		}

		userService.setContent(host, token, serviceKey, UserCredential.ELECTRONIC_INITIALS, userInitials, fileStream);
		return Response.noContent().build();
	}

	@DELETE
	@Path("credentials/initials")
	@Operation(summary = "Delete user initials", description = "Delete the content of the user profile initials")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response deleteInitials(
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");

		String token = AuthorizationUtils.getToken(authentication);

		userService.deleteContent(host, token, serviceKey, UserCredential.ELECTRONIC_INITIALS);
		return Response.noContent().build();
	}

	@GET
	@Path("credentials/config")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get user config value", description = "Return the user config value. (Only metadata of type String multi-value supported at the moment)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Metadata"))),
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response getConfig(
			@Parameter(required = true, description = "Config metadata code") @QueryParam("localCode") String localCode,
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(localCode, "localCode");

		String token = AuthorizationUtils.getToken(authentication);
		UserConfigDto configDto = userService.getConfig(host, token, serviceKey, localCode);
		return Response.ok(configDto).build();
	}

	@POST
	@Path("credentials/config")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Set user config value", description = "Set the user config value. (Only metadata of type String multi-value supported at the moment)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response setConfig(
			@Parameter(required = true, description = "Config metadata code") @QueryParam("localCode") String localCode,
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@Valid UserConfigDto config,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(localCode, "localCode");
		validateRequiredParameter(config, "config");

		String token = AuthorizationUtils.getToken(authentication);
		userService.setConfig(host, token, serviceKey, localCode, config);
		return Response.noContent().build();
	}
}
