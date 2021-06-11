package com.constellio.app.modules.restapi.apis.v2.record;

import com.constellio.app.modules.restapi.apis.v1.core.BaseRestfulService;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FacetMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FetchMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FilterMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.QueryDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordsResultDtoV2;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import com.constellio.app.modules.restapi.core.util.SetUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Path("records")
@Tag(name = "records")
public class RecordRestfulServiceV2 extends BaseRestfulService {

	@Inject
	private RecordServiceV2 recordService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get records", description = "Return a list of records")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(ref = "Record")))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestApiErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response get(
			@Parameter(description = "Record ids") @QueryParam("ids") Set<String> ids,
			@Parameter(description = "Collection") @QueryParam("collection") String collection,
			@Parameter(description = "Schema type") @QueryParam("schemaType") String schemaType,
			@Parameter(description = "Expression") @QueryParam("expression") String expression,
			@Parameter(description = "Fetch mode") @DefaultValue("ID") @QueryParam("mode") FetchMode fetchMode,
			@Parameter(description = "Filter mode") @DefaultValue("SUMMARY") @QueryParam("filterMode") FilterMode filterMode,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.HOST) String host)
			throws Exception {

		if (fetchMode == FetchMode.ID && SetUtils.isNullOrEmpty(ids)) {
			throw new RequiredParameterException("ids");
		}
		if (fetchMode == FetchMode.AUTOCOMPLETE) {
			validateRequiredParameter(schemaType, "schemaType");
			validateRequiredParameter(expression, "expression");
			validateRequiredParameter(collection, "collection");
		}
		validateAuthentication(authentication);

		String token = AuthorizationUtils.getToken(authentication);
		List<RecordDtoV2> records = fetchMode == FetchMode.AUTOCOMPLETE ?
									recordService.getSuggestions(collection, schemaType, expression, filterMode, token, host) :
									recordService.getByIds(ids, filterMode, token, host);

		return Response.ok(records).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Search records", description = "Search records")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecordsResultDtoV2.class))),
			@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestApiErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error"))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json", schema = @Schema(ref = "Error")))})
	public Response get(
			@Parameter(required = true, description = "Query") @Valid QueryDtoV2 query,
			@Parameter(description = "Filter mode") @DefaultValue("SUMMARY") @QueryParam("filterMode") FilterMode filterMode,
			@Parameter(required = true, description = "Bearer {token}") @HeaderParam(HttpHeaders.AUTHORIZATION) String authentication,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.HOST) String host,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) List<Locale> acceptLanguages)
			throws Exception {

		if (query == null) {
			throw new RequiredParameterException("query");
		}
		validateRequiredParameter(query, "query");
		validateAuthentication(authentication);

		if (!SetUtils.isNullOrEmpty(query.getFacetValueIds()) && query.getFacetMode() != FacetMode.SPECIFIC) {
			throw new InvalidParameterCombinationException("query.facetMode", "query.facetValueIds");
		}
		if (SetUtils.isNullOrEmpty(query.getFacetValueIds()) && query.getFacetMode() == FacetMode.SPECIFIC) {
			throw new RequiredParameterException("query.facetValueIds");
		}

		String token = AuthorizationUtils.getToken(authentication);
		RecordsResultDtoV2 recordsResult = recordService.search(query, filterMode, token, host, acceptLanguages);

		return Response.ok(recordsResult).build();
	}

}
