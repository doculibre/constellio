package com.constellio.app.modules.restapi.taxonomy;

import com.constellio.app.modules.restapi.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.taxonomy.dto.TaxonomyDto;
import com.constellio.app.modules.restapi.taxonomy.dto.TaxonomyNodeDto;
import com.constellio.app.modules.restapi.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.sdk.tests.TestRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class TaxonomyRestfulServiceAcceptanceTest extends BaseRestfulServiceAcceptanceTest {

	private Taxonomy taxonomy;

	@Before
	public void setUp() throws Exception {
		setUpTest();

		webTarget = newWebTarget("v1/taxonomies", new ObjectMapper());
	}

	//
	// GET taxonomies
	//

	@Test
	public void testGetTaxonomies() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyDto> taxonomies = response.readEntity(newTaxonomyList());
		assertThatRecords(taxonomies).extracting("code", "titles", "schemaTypes").containsOnly(
				new Tuple("plan", ImmutableMap.of("fr", "Plan de classification", "en", "File plan"), singletonList("category")),
				new Tuple("admUnits", ImmutableMap.of("fr", "Unités administratives", "en", "Departments"), singletonList("administrativeUnit")));
	}

	@Test
	public void testGetTaxonomiesWitCustomTaxonomy() throws Exception {
		createCustomTaxonomy();

		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("schemaType", taxonomy.getSchemaTypes().get(0)).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyDto> taxonomies = response.readEntity(newTaxonomyList());
		assertThatRecords(taxonomies).extracting("code", "titles", "schemaTypes").containsOnly(
				new Tuple(taxonomy.getCode(), ImmutableMap.of("fr", "Ma taxonomie", "en", "My taxonomy"), singletonList(taxonomy.getCode().concat("Type"))));
	}

	@Test
	public void testGetTaxonomiesWithSameUserParam() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("user", "bob").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyDto> taxonomies = response.readEntity(newTaxonomyList());
		assertThatRecords(taxonomies).extracting("code", "titles", "schemaTypes").containsOnly(
				new Tuple("plan", ImmutableMap.of("fr", "Plan de classification", "en", "File plan"), singletonList("category")),
				new Tuple("admUnits", ImmutableMap.of("fr", "Unités administratives", "en", "Departments"), singletonList("administrativeUnit")));
	}

	@Test
	public void testGetTaxonomiesWithUserParamWithAccessToCollection() throws Exception {
		recordServices.update(userServices.getUserInCollection("bob", zeCollection).setCollectionReadAccess(true));
		queryCounter.reset();
		commitCounter.reset();

		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("user", "robin").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyDto> taxonomies = response.readEntity(newTaxonomyList());
		assertThatRecords(taxonomies).extracting("code", "titles", "schemaTypes").containsOnly(
				new Tuple("plan", ImmutableMap.of("fr", "Plan de classification", "en", "File plan"), singletonList("category")),
				new Tuple("admUnits", ImmutableMap.of("fr", "Unités administratives", "en", "Departments"), singletonList("administrativeUnit")));
	}

	@Test
	public void testGetTaxonomiesWithUserParamWithoutAccessToCollection() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("user", "robin").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithSchemaTypeParam() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("schemaType", "category").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyDto> taxonomies = response.readEntity(newTaxonomyList());
		assertThatRecords(taxonomies).extracting("code", "titles", "schemaTypes").containsOnly(
				new Tuple("plan", ImmutableMap.of("fr", "Plan de classification", "en", "File plan"), singletonList("category")));
	}

	@Test
	public void testGetTaxonomiesWithoutAuthorizationHeader() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithEmptyAuthorizationHeader() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "").get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithInvalidSchemeInAuthorizationHeader() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Digest ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithoutSchemeInAuthorizationHeader() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, token).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithExpiredToken() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(expiredToken)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new ExpiredTokenException().getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithInvalidToken() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(fakeToken)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithoutServiceKeyParam() {
		Response response = webTarget
				.queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testGetTaxonomiesWithInvalidServiceKeyParam() {
		Response response = webTarget
				.queryParam("serviceKey", fakeServiceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithoutCollectionParam() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "collection"));
	}

	@Test
	public void testGetTaxonomiesWithInvalidCollectionParam() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", "fakeCollection").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeCollection").getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithInvalidUserParam() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("user", "fakeUser").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeUser").getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithInvalidSchemaTypeParam() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("schemaType", "fakeSchemaType").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeSchemaType").getValidationError()));
	}

	@Test
	public void testGetTaxonomiesWithUnallowedHostHeader() {
		Response response = webTarget
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, fakeHost).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(expiredToken)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnallowedHostException(fakeHost).getValidationError()));
	}

	// TODO faire un test avec cache not loaded

	//
	// GET taxonomies/id/nodes
	//

	@Test
	public void testGetTaxonomyRootNodes() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyNodeDto> nodes = response.readEntity(newTaxonomyNodeList());
		assertThatRecords(nodes).extracting("id", "schemaType", "linkable", "hasChildren", "metadatas").containsOnly(
				new Tuple("categoryId_X", "category", false, true, Collections.emptyMap()),
				new Tuple("categoryId_Z", "category", false, true, Collections.emptyMap()));
	}

	@Test
	public void testGetTaxonomyRootNodesForCustomTaxonomy() throws Exception {
		createCustomTaxonomy();

		Response response = buildNodesQuery(taxonomy.getCode())
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyNodeDto> nodes = response.readEntity(newTaxonomyNodeList());
		assertThatRecords(nodes).extracting("id", "schemaType", "linkable", "hasChildren", "metadatas").containsOnly(
				new Tuple("rootConcept", taxonomy.getCode().concat("Type"), false, true, Collections.emptyMap()));
	}

	@Test
	public void testGetTaxonomyChildrenNodes() {
		Response response = buildNodesQuery("categoryId_X100")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyNodeDto> nodes = response.readEntity(newTaxonomyNodeList());
		assertThatRecords(nodes).extracting("id", "schemaType", "linkable", "hasChildren", "metadatas").containsOnly(
				new Tuple("categoryId_X110", "category", false, true, Collections.emptyMap()),
				new Tuple("categoryId_X120", "category", false, true, Collections.emptyMap()),
				new Tuple("A16", "folder", false, true, Collections.emptyMap()),
				new Tuple("A17", "folder", false, true, Collections.emptyMap()),
				new Tuple("A18", "folder", false, true, Collections.emptyMap()),
				new Tuple("C06", "folder", false, true, Collections.emptyMap()),
				new Tuple("B06", "folder", false, true, Collections.emptyMap()),
				new Tuple("C32", "folder", false, true, Collections.emptyMap()),
				new Tuple("B32", "folder", false, true, Collections.emptyMap()));
	}

	@Test
	public void testGetTaxonomyChildrenNodesForCustomTaxonomy() throws Exception {
		createCustomTaxonomy();

		Response response = buildNodesQuery("rootConcept")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyNodeDto> nodes = response.readEntity(newTaxonomyNodeList());
		assertThatRecords(nodes).extracting("id", "schemaType", "linkable", "hasChildren", "metadatas").containsOnly(
				new Tuple("childConcept", taxonomy.getCode().concat("Type"), false, true, Collections.emptyMap()));
	}

	@Test
	public void testGetTaxonomyNodesWithMetadataParam() {
		Response response = buildNodesQuery("categoryId_X100")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("metadata", "code").queryParam("metadata", "title").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyNodeDto> nodes = response.readEntity(newTaxonomyNodeList());
		assertThatRecords(nodes).extracting("id", "schemaType", "linkable", "hasChildren", "metadatas").containsOnly(
				new Tuple("categoryId_X110", "category", false, true, ImmutableMap.of("code", "X110", "title", "X110")),
				new Tuple("categoryId_X120", "category", false, true, ImmutableMap.of("code", "X120", "title", "X120")),
				new Tuple("A16", "folder", false, true, ImmutableMap.of("title", "Chat")),
				new Tuple("A17", "folder", false, true, ImmutableMap.of("title", "Chauve-souris")),
				new Tuple("A18", "folder", false, true, ImmutableMap.of("title", "Cheval")),
				new Tuple("C06", "folder", false, true, ImmutableMap.of("title", "Chou-fleur")),
				new Tuple("B06", "folder", false, true, ImmutableMap.of("title", "Framboise")),
				new Tuple("C32", "folder", false, true, ImmutableMap.of("title", "Maïs")),
				new Tuple("B32", "folder", false, true, ImmutableMap.of("title", "Pêche")));
	}

	@Test
	public void testGetTaxonomyNodesWithRowsStartAndRowsLimitParams() {
		Response response = buildNodesQuery("categoryId_X100")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("rowsStart", 2).queryParam("rowsLimit", 2).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyNodeDto> nodes = response.readEntity(newTaxonomyNodeList());
		assertThatRecords(nodes).extracting("id", "schemaType", "linkable", "hasChildren", "metadatas").containsOnly(
				new Tuple("A16", "folder", false, true, Collections.emptyMap()),
				new Tuple("A17", "folder", false, true, Collections.emptyMap()));
	}

	@Test
	public void testGetTaxonomyNodesWithWriteAccessParams() {
		authorizationsServices.add(AuthorizationAddRequest.authorizationForUsers(users.bobIn(zeCollection))
				.givingNegativeWriteAccess().on("00000000103"));

		Response response = buildNodesQuery("A01")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("writeAccess", true).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyNodeDto> nodes = response.readEntity(newTaxonomyNodeList());
		assertThatRecords(nodes).extracting("id").doesNotContain("00000000103");
	}


	@Test
	public void testGetTaxonomyNodesWithSameUserParam() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("user", "bob").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyNodeDto> nodes = response.readEntity(newTaxonomyNodeList());
		assertThatRecords(nodes).extracting("id", "schemaType", "linkable", "hasChildren", "metadatas").containsOnly(
				new Tuple("categoryId_X", "category", false, true, Collections.emptyMap()),
				new Tuple("categoryId_Z", "category", false, true, Collections.emptyMap()));
	}

	@Test
	public void testGetTaxonomyNodesWithUserParamWithAccessToCollection() throws Exception {
		recordServices.update(userServices.getUserInCollection("bob", zeCollection).setCollectionReadAccess(true));
		queryCounter.reset();
		commitCounter.reset();

		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("user", "robin").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyNodeDto> taxonomies = response.readEntity(newTaxonomyNodeList());
		assertThat(taxonomies).isEmpty();
	}

	@Test
	public void testGetTaxonomyNodesWithUserParamWithoutAccessToCollection() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("user", "robin").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthorizedAccessException().getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithSchemaTypeParam() {
		Response response = buildNodesQuery("categoryId_X100")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("schemaType", "category").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

		List<TaxonomyNodeDto> nodes = response.readEntity(newTaxonomyNodeList());
		assertThatRecords(nodes).extracting("id", "schemaType", "linkable", "hasChildren", "metadatas").containsOnly(
				new Tuple("categoryId_X110", "category", true, false, Collections.emptyMap()),
				new Tuple("categoryId_X120", "category", true, false, Collections.emptyMap()));
	}

	@Test
	public void testGetTaxonomyNodesWithoutAuthorizationHeader() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithEmptyAuthorizationHeader() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "").get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithInvalidSchemeInAuthorizationHeader() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Digest ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithoutSchemeInAuthorizationHeader() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, token).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
		assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidAuthenticationException().getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithExpiredToken() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(expiredToken)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new ExpiredTokenException().getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithInvalidToken() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(fakeToken)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithoutServiceKeyParam() {
		Response response = buildNodesQuery("plan")
				.queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testGetTaxonomyNodesWithInvalidServiceKeyParam() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", fakeServiceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnauthenticatedUserException().getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithoutCollectionParam() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(NOT_NULL_MESSAGE, "collection"));
	}

	@Test
	public void testGetTaxonomyNodesWithInvalidCollectionParam() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", "fakeCollection").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeCollection").getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithInvalidIdPathParam() {
		Response response = buildNodesQuery("fakeId")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeId").getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithInvalidUserParam() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("user", "fakeUser").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeUser").getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithInvalidSchemaTypeParam() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("schemaType", "fakeSchemaType").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new RecordNotFoundException("fakeSchemaType").getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithInvalidMetadataParam() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("metadata", "fakeMetadata").request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	public void testGetTaxonomyNodesWithRowsStartAndMissingRowsLimit() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("rowsStart", 1).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidParameterCombinationException("rowsStart", "rowsLimit").getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithRowsLimitAndMissingRowsStart() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection)
				.queryParam("rowsLimit", 1).request()
				.header(HttpHeaders.HOST, host).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(token)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new InvalidParameterCombinationException("rowsStart", "rowsLimit").getValidationError()));
	}

	@Test
	public void testGetTaxonomyNodesWithUnallowedHostHeader() {
		Response response = buildNodesQuery("plan")
				.queryParam("serviceKey", serviceKey).queryParam("collection", zeCollection).request()
				.header(HttpHeaders.HOST, fakeHost).header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(expiredToken)).get();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());

		assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).isEqualTo(i18n.$(new UnallowedHostException(fakeHost).getValidationError()));
	}

	private GenericType<List<TaxonomyDto>> newTaxonomyList() {
		return new GenericType<List<TaxonomyDto>>() {
		};
	}

	private GenericType<List<TaxonomyNodeDto>> newTaxonomyNodeList() {
		return new GenericType<List<TaxonomyNodeDto>>() {
		};
	}

	private WebTarget buildNodesQuery(String id) {
		webTarget = newWebTarget("v1/taxonomies/" + id + "/nodes", new ObjectMapper());
		return webTarget;
	}

	private void createCustomTaxonomy() throws Exception {
		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "Ma taxonomie");
		labelTitle.put(Language.English, "My taxonomy");

		ValueListServices valueListServices = new ValueListServices(getAppLayerFactory(), zeCollection);
		taxonomy = valueListServices.createTaxonomy(labelTitle, true);

		MetadataSchema schema = rm.getTypes().getDefaultSchema(taxonomy.getSchemaTypes().get(0));
		Record rootConcept = new TestRecord(schema, "rootConcept")
				.set(Schemas.CODE, "CodeA").set(Schemas.TITLE, "Root concept");
		Record childConcept = new TestRecord(schema, "childConcept").set(schema.get("parent"), "rootConcept")
				.set(Schemas.CODE, "CodeA1").set(Schemas.TITLE, "Child concept");
		Transaction transaction = new Transaction();
		transaction.add(rootConcept);
		transaction.add(childConcept);

		Metadata metadataFolder = new ValueListServices(getAppLayerFactory(), zeCollection)
				.createAMultivalueClassificationMetadataInGroup(taxonomy, Folder.SCHEMA_TYPE,
						"Ma taxonomie", "Ma taxonomie tab label");

		transaction.add(records.getFolder_A01().set(metadataFolder, Collections.singletonList(childConcept)).getWrappedRecord());
		recordServices.execute(transaction);

		queryCounter.reset();
		commitCounter.reset();
	}
}
