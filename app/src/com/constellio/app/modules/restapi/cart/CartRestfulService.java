package com.constellio.app.modules.restapi.cart;

import com.constellio.app.modules.restapi.cart.dto.CartDto;
import com.constellio.app.modules.restapi.core.service.BaseRestfulService;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("cart")
@Tag(name = "cart")
public class CartRestfulService extends BaseRestfulService {

	@Inject
	private CartService cartService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get favorite group", description = "Return the metadata of a favorite group")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Cart"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response get(@Parameter(required = true, description = "Cart Id") @QueryParam("id") String id,
						@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
						@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
						@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(id, "cartId");

		String token = AuthorizationUtils.getToken(authentication);
		CartDto cart = cartService.getCart(host, token, serviceKey, id);
		return Response.ok(cart).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Create favorite group", description = "Create a favorite group.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Cart"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response create(
			@Parameter(required = true, description = "Collection") @QueryParam("collection") String collection,
			@Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@Valid CartDto cart,
			@HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(collection, "collection");
		validateRequiredParameter(cart, "cart");

		String token = AuthorizationUtils.getToken(authentication);
		CartDto createdCart = cartService.createCart(host, token, serviceKey, collection, cart);
		return Response.status(Response.Status.CREATED).entity(createdCart).build();
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Patch favorite group", description = "Update favorite group name")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Cart"))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response updatePartial(@Parameter(required = true, description = "Cart Id") @QueryParam("id") String id,
								  @Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
								  @Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
								  @Valid CartDto cart,
								  @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(id, "cartId");
		validateRequiredParameter(cart, "cart");

		String token = AuthorizationUtils.getToken(authentication);
		CartDto updatedCard = cartService.updateCart(host, token, serviceKey, id, cart);
		return Response.ok(updatedCard).build();
	}

	@DELETE
	@Operation(summary = "Delete favorite group", description = "Delete a favorite group")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response delete(@Parameter(required = true, description = "Cart Id") @QueryParam("id") String id,
						   @Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
						   @Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
						   @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(id, "cartId");

		String token = AuthorizationUtils.getToken(authentication);
		cartService.deleteCart(host, token, serviceKey, id);
		return Response.noContent().build();
	}

	@DELETE
	@Path("content")
	@Operation(summary = "Delete favorite group content", description = "Delete content in a favorite group")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No Content"),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response deleteContent(@Parameter(required = true, description = "Cart Id") @QueryParam("id") String id,
								  @Parameter(required = true, description = "Service key") @QueryParam("serviceKey") String serviceKey,
								  @Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
								  @HeaderParam(HttpHeaders.HOST) String host) throws Exception {

		validateAuthentication(authentication);
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(id, "cartId");

		String token = AuthorizationUtils.getToken(authentication);
		cartService.deleteCartContent(host, token, serviceKey, id);
		return Response.noContent().build();
	}
}
