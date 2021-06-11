package com.constellio.app.modules.restapi.apis.v2.record;

import com.constellio.app.modules.restapi.apis.v2.BaseRestfulServiceV2AcceptanceTest;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FacetDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FacetMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FetchMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FilterMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.QueryDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordsResultDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.SortDtoV2;
import com.constellio.app.modules.restapi.core.exception.CollectionNotFoundException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class RecordRestfulServiceV2AcceptanceTest extends BaseRestfulServiceV2AcceptanceTest {

	private static List<String> recordIds;
	private static String authorizationHeaderValue;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		recordIds = asList(records.document_A19, records.folder_A01);
		authorizationHeaderValue = "Bearer ".concat(token);
	}

	// GET

	@Test
	public void givenIdsThenSummaryRecordsReturned() {
		Response response = doGetByIdsRequest(recordIds, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		List<RecordDtoV2> records = response.readEntity(recordDtosList());
		assertThat(records).extracting("id", "schemaType")
				.containsOnly(tuple(recordIds.get(0), Document.SCHEMA_TYPE), tuple(recordIds.get(1), Folder.SCHEMA_TYPE));

		records.forEach(recordDto -> {
			assertThat(recordDto.getMetadatas()).isNotEmpty();
			boolean hasNonSummaryMetadata = recordDto.getMetadatas().keySet().stream()
					.anyMatch(metadataCode -> !SchemaUtils.isSummary(schemasManager.getSchemaTypes(zeCollection)
							.getSchemaType(recordDto.getSchemaType()).getDefaultSchema().get(metadataCode)));
			assertThat(hasNonSummaryMetadata).isFalse();
		});
	}

	@Test
	public void givenIdsAndFilterModeAllThenFullRecordsReturned() {
		Response response = doGetByIdsRequest(recordIds, authorizationHeaderValue, FilterMode.ALL);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isLessThanOrEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		List<RecordDtoV2> records = response.readEntity(recordDtosList());
		assertThat(records).extracting("id", "schemaType")
				.containsOnly(tuple(recordIds.get(0), Document.SCHEMA_TYPE), tuple(recordIds.get(1), Folder.SCHEMA_TYPE));

		records.forEach(recordDto -> {
			assertThat(recordDto.getMetadatas()).isNotEmpty();
			boolean hasNonSummaryMetadata = recordDto.getMetadatas().keySet().stream()
					.anyMatch(metadataCode -> !SchemaUtils.isSummary(schemasManager.getSchemaTypes(zeCollection)
							.getSchemaType(recordDto.getSchemaType()).getDefaultSchema().get(metadataCode)));
			assertThat(hasNonSummaryMetadata).isTrue();
		});
	}

	@Test
	public void givenIdsWithMissingIdsParametersThenBadRequestException() {
		Response response = doGetByIdsRequest(Collections.emptyList(), authorizationHeaderValue, FilterMode.ALL);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new RequiredParameterException("ids").getValidationError()));
	}

	@Test
	public void givenAutocompleteThenCorrectSuggestedRecordsReturned() {
		Response response = doGetAutocompleteRequest(zeCollection, "user", "a", authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		List<RecordDtoV2> records = response.readEntity(recordDtosList());
		assertThat(records).hasSize(2);
		assertThat(records).extracting("id", "schemaType").containsOnly(
				tuple(users.adminIn(zeCollection).getId(), User.SCHEMA_TYPE),
				tuple(users.aliceIn(zeCollection).getId(), User.SCHEMA_TYPE));
	}

	@Test
	public void givenAutocompleteWithMissingCollectionParameterThenBadRequestException() {
		Response response = doGetAutocompleteRequest(null, "user", "a", authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new RequiredParameterException("collection").getValidationError()));
	}

	@Test
	public void givenAutocompleteWithInvalidCollectionParameterThenBadRequestException() {
		Response response = doGetAutocompleteRequest("fakeCollection", "user", "a", authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new CollectionNotFoundException("fakeCollection").getValidationError()));
	}

	@Test
	public void givenAutocompleteWithMissingSchemaTypeParameterThenBadRequestException() {
		Response response = doGetAutocompleteRequest(zeCollection, "", "a", authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new RequiredParameterException("schemaType").getValidationError()));
	}

	@Test
	public void givenAutocompleteWithInvalidSchemaTypeParameterThenBadRequestException() {
		Response response = doGetAutocompleteRequest(zeCollection, "fakeSchemaType", "a", authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new RecordNotFoundException("fakeSchemaType").getValidationError()));
	}

	@Test
	public void givenAutocompleteWithMissingExpressionParameterThenBadRequestException() {
		Response response = doGetAutocompleteRequest(zeCollection, "user", " ", authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new RequiredParameterException("expression").getValidationError()));
	}

	// POST

	@Test
	public void givenQueryWithConstellioFacetModeThenRecordsAndFacetsReturned() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection)
				.schemaTypes(new HashSet<>(asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE)))
				.expression("abeille")
				.facetMode(FacetMode.CONSTELLIO).build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		RecordsResultDtoV2 recordsResultDto = response.readEntity(RecordsResultDtoV2.class);
		assertThat(recordsResultDto.getRecords()).hasSize(5);
		assertThat(recordsResultDto.getReferences()).isNotEmpty();

		assertThat(recordsResultDto.getFacets()).extracting("facetId", "facetName").contains(
				tuple("schema_s", "Type"));
		FacetDtoV2 facet = recordsResultDto.getFacets().stream().filter(f -> f.getFacetId().equals("schema_s"))
				.findAny().orElseThrow(RuntimeException::new);
		assertThat(facet.getValues()).extracting("id", "name", "count").containsOnly(
				tuple("schema_s:document_default", "Document", 4L), tuple("schema_s:folder_default", "Dossier", 1L));

		recordsResultDto.getRecords().forEach(recordDto -> {
			assertThat(recordDto.getMetadatas()).isNotEmpty();
			boolean hasNonSummaryMetadata = recordDto.getMetadatas().keySet().stream()
					.anyMatch(metadataCode -> !SchemaUtils.isSummary(schemasManager.getSchemaTypes(zeCollection)
							.getSchemaType(recordDto.getSchemaType()).getDefaultSchema().get(metadataCode)));
			assertThat(hasNonSummaryMetadata).isFalse();
		});
	}

	@Test
	public void givenQueryWithSpecificFacetModeThenFacetAppliedOnRecordsReturned() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection)
				.schemaTypes(new HashSet<>(asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE))).expression("abeille")
				.facetMode(FacetMode.SPECIFIC).facetValueIds(singleton("schema_s:folder_default")).build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		RecordsResultDtoV2 recordsResultDto = response.readEntity(RecordsResultDtoV2.class);
		assertThat(recordsResultDto.getRecords()).hasSize(1);
		assertThat(recordsResultDto.getReferences()).isNotEmpty();
		assertThat(recordsResultDto.getFacets()).isNull();
	}

	@Test
	public void givenQueryWithSpecificFacetModeAndNoFacetIdsThenBadRequestException() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection)
				.schemaTypes(new HashSet<>(asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE))).expression("abeille")
				.facetMode(FacetMode.SPECIFIC).build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new RequiredParameterException("query.facetValueIds").getValidationError()));
	}

	@Test
	public void givenQueryWithConstellioFacetModeAndFacetIdsThenBadRequestException() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection)
				.schemaTypes(new HashSet<>(asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE))).expression("abeille")
				.facetMode(FacetMode.CONSTELLIO).facetValueIds(singleton("schema_s:folder_default")).build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new InvalidParameterCombinationException("query.facetMode", "query.facetValueIds").getValidationError()));
	}

	@Test
	public void givenQueryWithExpressionThenRecordsReturned() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection)
				.schemaTypes(singleton(Folder.SCHEMA_TYPE))
				.expression("Abeille").build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		RecordsResultDtoV2 recordsResultDto = response.readEntity(RecordsResultDtoV2.class);
		assertThat(recordsResultDto.getRecords()).hasSize(1);
		assertThat(recordsResultDto.getRecords()).extracting("id", "schemaType")
				.containsOnly(tuple(records.folder_A01, Folder.SCHEMA_TYPE));
		assertThat(recordsResultDto.getFacets()).isNull();

		assertThat(recordsResultDto.getReferences()).extracting("id", "title", "code", "description").contains(
				tuple(records.getUnit10a().getId(), records.getUnit10a().getTitle(), records.getUnit10a().getCode(), records.getUnit10a().getDescription()),
				tuple(records.getCategory_X110().getId(), records.getCategory_X110().getTitle(), records.getCategory_X110().getCode(), records.getCategory_X110().getDescription()),
				tuple(records.getRule2().getId(), records.getRule2().getTitle(), records.getRule2().getCode(), records.getRule2().getDescription()));

		recordsResultDto.getRecords().forEach(recordDto -> {
			assertThat(recordDto.getMetadatas()).isNotEmpty();
			boolean hasNonSummaryMetadata = recordDto.getMetadatas().keySet().stream()
					.anyMatch(metadataCode -> !SchemaUtils.isSummary(schemasManager.getSchemaTypes(zeCollection)
							.getSchemaType(recordDto.getSchemaType()).getDefaultSchema().get(metadataCode)));
			assertThat(hasNonSummaryMetadata).isFalse();
		});
	}

	@Test
	public void givenQueryWithExpressionAndNoSchemaTypesThenReturnedRecordsLimitedToSpecificSchemaTypes() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection).expression("*:*").build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		RecordsResultDtoV2 recordsResultDto = response.readEntity(RecordsResultDtoV2.class);
		assertThat(recordsResultDto.getRecords()).isNotEmpty();
		assertThat(recordsResultDto.getRecords()).extracting("schemaType").doesNotContain("ddvContainerRecordType");
	}

	@Test
	public void givenQueryWithFilterModeAllThenFullRecordsReturned() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection)
				.schemaTypes(singleton(Folder.SCHEMA_TYPE))
				.expression("Abeille").build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue, FilterMode.ALL);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		RecordsResultDtoV2 recordsResultDto = response.readEntity(RecordsResultDtoV2.class);
		assertThat(recordsResultDto.getRecords()).hasSize(1);
		assertThat(recordsResultDto.getFacets()).isNull();
		assertThat(recordsResultDto.getReferences()).isNotEmpty();

		recordsResultDto.getRecords().forEach(recordDto -> {
			assertThat(recordDto.getMetadatas()).isNotEmpty();
			boolean hasNonSummaryMetadata = recordDto.getMetadatas().keySet().stream()
					.anyMatch(metadataCode -> !SchemaUtils.isSummary(schemasManager.getSchemaTypes(zeCollection)
							.getSchemaType(recordDto.getSchemaType()).getDefaultSchema().get(metadataCode)));
			assertThat(hasNonSummaryMetadata).isTrue();
		});
	}

	@Test
	public void givenQueryWithSortingThenRecordsReturnedInCorrectOrder() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection)
				.schemaTypes(singleton(Document.SCHEMA_TYPE)).expression("abeille")
				.sorting(singletonList(SortDtoV2.builder()
						.metadata(Document.TITLE).ascending(true).build()))
				.build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		RecordsResultDtoV2 recordsResultDto = response.readEntity(RecordsResultDtoV2.class);
		assertThat(recordsResultDto.getRecords()).hasSize(4);
		assertThat(recordsResultDto.getFacets()).isNull();
		assertThat(recordsResultDto.getReferences()).isNotNull();

		List<String> recordTitles = recordsResultDto.getRecords().stream()
				.map(record -> record.getMetadatas().get(Document.TITLE).get(0))
				.collect(Collectors.toList());
		assertThat(recordTitles).containsExactly("Abeille - Histoire", "Abeille - Livre de recettes",
				"Abeille - Petit guide", "Abeille - Typologie");
	}

	@Test
	public void givenQueryWithInvalidSortingThenBadRequestException() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection)
				.schemaTypes(singleton(Document.SCHEMA_TYPE)).expression("abeille")
				.sorting(singletonList(SortDtoV2.builder().ascending(true).build()))
				.build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(NOT_NULL_MESSAGE, "sorting[0].metadata"));
	}

	@Test
	public void givenQueryWithRowsStartAndRowsLimitThenRecordsReturned() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection)
				.schemaTypes(singleton(Document.SCHEMA_TYPE))
				.expression("abeille").build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());
		RecordsResultDtoV2 recordsResultDto = response.readEntity(RecordsResultDtoV2.class);
		assertThat(recordsResultDto.getRecords()).hasSize(4);

		QueryDtoV2 query2 = QueryDtoV2.builder().collection(zeCollection)
				.schemaTypes(singleton(Document.SCHEMA_TYPE))
				.expression("abeille").rowsStart(2).rowsLimit(1).build();
		Response response2 = doPostByQueryRequest(query2, authorizationHeaderValue);
		assertThat(response2.getStatus()).isEqualTo(OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		RecordsResultDtoV2 recordsResultDto2 = response2.readEntity(RecordsResultDtoV2.class);
		assertThat(recordsResultDto2.getRecords()).hasSize(1);
		assertThat(recordsResultDto2.getFacets()).isNull();
		assertThat(recordsResultDto2.getReferences()).isNotNull();

		assertThat(recordsResultDto.getRecords().get(2)).isEqualTo(recordsResultDto2.getRecords().get(0));
	}

	@Test
	public void givenQueryWithoutQueryParameterThenBadRequestException() {
		Response response = doPostByQueryRequest(null, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(new RequiredParameterException("query").getValidationError()));
	}

	@Test
	public void givenQueryWithoutCollectionThenBadRequestException() {
		QueryDtoV2 query = QueryDtoV2.builder().schemaTypes(singleton(Folder.SCHEMA_TYPE)).build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
		assertNoSolrQueryAndCommitCalls();

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo($(NOT_NULL_MESSAGE, "collection"));
	}

	@Test
	public void givenQueryWithoutSchemaTypesThenAllSchemaTypesOfCollectionUsed() {
		QueryDtoV2 query = QueryDtoV2.builder().collection(zeCollection).expression("Abeille").build();
		Response response = doPostByQueryRequest(query, authorizationHeaderValue);
		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		RecordsResultDtoV2 recordsResultDto = response.readEntity(RecordsResultDtoV2.class);
		Set<String> schemaTypes = recordsResultDto.getRecords().stream()
				.map(RecordDtoV2::getSchemaType).collect(Collectors.toSet());
		assertThat(schemaTypes).contains(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE);
	}

	// TODO auth tests

	private Response doGetByIdsRequest(List<String> ids, String authHeaderValue) {
		return doGetByIdsRequest(ids, authHeaderValue, null);
	}

	private Response doGetByIdsRequest(List<String> ids, String authHeaderValue, FilterMode filterMode) {
		WebTarget target = newWebTarget("v2/records").queryParam("mode", FetchMode.ID);
		for (String id : ids) {
			target = target.queryParam("ids", id);
		}
		target = filterMode != null ? target.queryParam("filterMode", filterMode.name()) : target;
		return authHeaderValue != null ?
			   target.request().header(HttpHeaders.AUTHORIZATION, authHeaderValue).get() :
			   target.request().get();
	}

	private Response doGetAutocompleteRequest(String collection, String schemaType, String expression,
											  String authHeaderValue) {
		return doGetAutocompleteRequest(collection, schemaType, expression, authHeaderValue, null);
	}

	private Response doGetAutocompleteRequest(String collection, String schemaType, String expression,
											  String authHeaderValue, FilterMode filterMode) {
		WebTarget target = newWebTarget("v2/records").queryParam("mode", FetchMode.AUTOCOMPLETE);
		target = target.queryParam("collection", collection);
		target = target.queryParam("schemaType", schemaType);
		target = target.queryParam("expression", expression);
		target = filterMode != null ? target.queryParam("filterMode", filterMode.name()) : target;
		return authHeaderValue != null ?
			   target.request().header(HttpHeaders.AUTHORIZATION, authHeaderValue).get() :
			   target.request().get();
	}

	private Response doPostByQueryRequest(QueryDtoV2 query, String authHeaderValue) {
		return doPostByQueryRequest(query, authHeaderValue, null);
	}

	private Response doPostByQueryRequest(QueryDtoV2 query, String authHeaderValue, FilterMode filterMode) {
		WebTarget target = newWebTarget("v2/records");
		target = filterMode != null ? target.queryParam("filterMode", filterMode.name()) : target;
		Builder request = authHeaderValue != null ?
						  target.request().header(HttpHeaders.AUTHORIZATION, authHeaderValue) :
						  target.request();
		return request.post(entity(query, APPLICATION_JSON_TYPE));
	}

	private GenericType<List<RecordDtoV2>> recordDtosList() {
		return new GenericType<List<RecordDtoV2>>() {
		};
	}

}
