package com.constellio.app.modules.restapi.apis.v1.category;

import com.constellio.app.modules.restapi.apis.v1.category.dto.CategoryDto;
import com.constellio.app.modules.restapi.apis.v1.resource.service.ResourceRestfulService;
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
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

@Hidden
@Path("categories")
@Tag(name = "categories")
public class CategoryRestfulService extends ResourceRestfulService {

	@Inject
	private CategoryService categoryService;

	@GET
	@Path("search")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Search categories", description = "Search for a list of categories")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Category"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response search(
			@Parameter(required = true, description = "The search expression used to filter results")
			@QueryParam("expression") String expression,
			@Parameter(required = true, description = "Collection") @QueryParam("collection") String collection,
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {
		if (!areExperimentalServicesEnabled()) {
			return Response.status(Status.METHOD_NOT_ALLOWED).build();
		}
		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(expression, "expression");
		validateRequiredParameter(collection, "collection");

		String token = AuthorizationUtils.getToken(authentication);
		List<CategoryDto> categories = categoryService.search(host, token, serviceKey, collection, expression);
		return Response.ok(categories).build();
	}
}
