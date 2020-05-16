package com.constellio.app.api.search;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.query.logical.FreeTextQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

// Confirm @SlowTest
public class SearchWebServiceSecurityAcceptTest extends ConstellioTest {

	String anotherCollection = "otherCollection";
	TestsSchemasSetup anotherCollectionSetup = new TestsSchemasSetup(anotherCollection);
	ZeSchemaMetadatas anotherCollectionSchema = anotherCollectionSetup.new ZeSchemaMetadatas();
	TestsSchemasSetup zeCollectionSetup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeCollectionSchema = zeCollectionSetup.new ZeSchemaMetadatas();
	TestsSchemasSetup.AnotherSchemaMetadatas zeCollectionUnsecuredSchema = zeCollectionSetup.new AnotherSchemaMetadatas();
	RecordServices recordServices;
	UserServices userServices;

	UserCredential userWithZeCollectionReadAccess;
	UserCredential userWithAnotherCollectionReadAccess;
	UserCredential userWithBothCollectionReadAccess;
	UserCredential userInBothCollectionWithoutAnyAccess;
	UserCredential userInNoCollection;
	UserCredential userWithSomeRecordAccess;
	UserCredential systemAdmin;

	String zeCollectionRecord1 = "zeCollectionRecord1";
	String zeCollectionRecord2 = "zeCollectionRecord2";
	String zeCollectionNonSecuredRecord1 = "zeCollectionNonSecuredRecord1";
	String zeCollectionNonSecuredRecord2 = "zeCollectionNonSecuredRecord2";
	String anotherCollectionRecord1 = "anotherCollectionRecord1";
	String anotherCollectionRecord2 = "anotherCollectionRecord2";

	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTest(users),
				withCollection(anotherCollection)
		);

		MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();

		for (String collection : asList(zeCollection, anotherCollection)) {
			MetadataSchemaTypesBuilder metadataSchemaTypesBuilder = schemasManager.modify(collection);
			metadataSchemaTypesBuilder.getSchemaType(Authorization.SCHEMA_TYPE).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			metadataSchemaTypesBuilder.getSchemaType(User.SCHEMA_TYPE).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			metadataSchemaTypesBuilder.getSchemaType(Group.SCHEMA_TYPE).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			metadataSchemaTypesBuilder.getSchemaType(SearchEvent.SCHEMA_TYPE).setRecordCacheType(RecordCacheType.FULLY_CACHED);
		}

		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();

		//		givenCollection(zeCollection, asList("fr", "en"));
		//		givenCollection(anotherCollection, asList("fr"));

		defineSchemasManager().using(zeCollectionSetup.withSecurityFlag(true)
				.withAStringMetadata(whichIsMultivalue).withAContentMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getSchemaType("anotherSchemaType").setSecurity(false);
					}
				}));
		defineSchemasManager().using(anotherCollectionSetup.withSecurityFlag(true)
				.withAStringMetadata(whichIsMultivalue).withAContentListMetadata());

		//		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
		//			@Override
		//			public void alter(MetadataSchemaTypesBuilder types) {
		//				types.getSchemaType(zeCollectionSetup.)
		//			}
		//		});

		setupUsers();
		assertThatUserIsInCollection(users.alice().getUsername(), "zeCollection");
		setupUserPermissions();
		setupRecords();
		setupAuthorizations();
	}

	@Test
	public void givenUserWithCollectionReadAccessWhenSearchingWithFreeTextUsingWildcardThenSeeAllRecordsOfTheCollection()
			throws Exception {

		assertThat(findAllRecordsVisibleBy(userWithZeCollectionReadAccess))
				.containsOnly(zeCollectionRecord1, zeCollectionRecord2, zeCollectionNonSecuredRecord1,
						zeCollectionNonSecuredRecord2);
		assertThat(findAllRecordsVisibleBy(userWithAnotherCollectionReadAccess))
				.containsOnly(anotherCollectionRecord1, anotherCollectionRecord2);
	}

	@Test
	public void givenUserInBothCollectionWithReadAccessWhenSearchingWithFreeTextUsingWildcardThenSeeAllRecordsOfBothCollection()
			throws Exception {
		assertThat(findAllRecordsVisibleBy(userWithBothCollectionReadAccess))
				.containsOnly(zeCollectionRecord1, zeCollectionRecord2, anotherCollectionRecord1, anotherCollectionRecord2,
						zeCollectionNonSecuredRecord1, zeCollectionNonSecuredRecord2);
	}

	@Test
	public void givenUserInBothCollectionWithoutReadAccessWhenSearchingWithFreeTextUsingWildcardThenSeeNothing()
			throws Exception {

		assertThat(findAllRecordsVisibleBy(userInBothCollectionWithoutAnyAccess)).isEmpty();
	}

	@Test
	public void givenUserInNoCollectionWhenSearchingWithFreeTextUsingWildcardThenSeeNothing()
			throws Exception {

		assertThat(findAllRecordsVisibleBy(userInNoCollection)).isEmpty();
	}

	@Test
	public void givenUserWithSomeRecordAccessWhenSearchingWithFreeTextUsingWildcardThenSeeOnlyAccessibleRecords()
			throws Exception {

		assertThat(findAllRecordsVisibleBy(userWithSomeRecordAccess)).containsOnly(zeCollectionRecord1, anotherCollectionRecord1);
	}

	@Test
	public void whenCheckingIfSecurityEnabledThenBasedOnCollectionAndSchema()
			throws Exception {

		FreeTextSearchServices searchServices = new FreeTextSearchServices(getModelLayerFactory());

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("q", "*:*");
		params.add("fq", "collection_s:zeCollection");
		params.add("fq", "schema_s:anotherSchemaType");
		assertThat(searchServices.isSecurityEnabled(params)).isFalse();

		params = new ModifiableSolrParams();
		params.add("q", "*:*");
		params.add("fq", "schema_s:anotherSchemaType");
		assertThat(searchServices.isSecurityEnabled(params)).isTrue();

		params = new ModifiableSolrParams();
		params.add("q", "*:*");
		params.add("fq", "collection_s:zeCollection");
		assertThat(searchServices.isSecurityEnabled(params)).isTrue();

		params = new ModifiableSolrParams();
		params.add("q", "*:*");
		params.add("fq", "collection_s:zeCollection");
		params.add("fq", "schema_s:zeSchemaType");
		assertThat(searchServices.isSecurityEnabled(params)).isTrue();

		params = new ModifiableSolrParams();
		params.add("q", "collection_s:zeCollection");
		params.add("fq", "schema_s:anotherSchemaType");
		assertThat(searchServices.isSecurityEnabled(params)).isFalse();

		params = new ModifiableSolrParams();
		params.add("q", "collection_s:zeCollection");
		params.add("fq", "schema_s:zeSchemaType");
		assertThat(searchServices.isSecurityEnabled(params)).isTrue();

		params = new ModifiableSolrParams();
		params.add("fq", "collection_s:zeCollection");
		params.add("q", "schema_s:anotherSchemaType");
		assertThat(searchServices.isSecurityEnabled(params)).isFalse();

		params = new ModifiableSolrParams();
		params.add("fq", "collection_s:zeCollection");
		params.add("q", "schema_s:zeSchemaType");
		assertThat(searchServices.isSecurityEnabled(params)).isTrue();

	}

	@Test
	public void testSearchEventPresentOnWebServiceQuery() throws IOException, SolrServerException {

		Toggle.ADVANCED_SEARCH_CONFIGS.enable();

		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.add("q", "*:*");
		solrParams.add("fq", "collection_s:zeCollection");

		SolrClient solrServer = newSearchClient();

		String serviceKey = userServices.giveNewServiceToken(userServices.getUserCredential(systemAdmin.getUsername()));
		String token = userServices.getToken(serviceKey, systemAdmin.getUsername(), "youshallnotpass");
		solrParams.set("serviceKey", serviceKey);
		solrParams.set("token", token);
		solrServer.query(solrParams);

		getModelLayerFactory().getDataLayerFactory().getEventsVaultServer().flush();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		MetadataSchemaType searchEventSchemaType = rm.searchEventSchemaType();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(searchEventSchemaType).returnAll());

		List<Record> recordList = getModelLayerFactory().newSearchServices().search(query);

		assertThat(recordList.size()).isEqualTo(1);
		assertThat(rm.wrapSearchEvent(recordList.get(0)).getQuery()).isEqualTo("*:*");
	}


	@Test
	public void givenUserWithSomeAccessWhenSearchingUsingWebServiceWithOnNonSecuredSchemaThenSeeAllResults()
			throws SolrServerException, IOException {

		assertThatUserIsInCollection(users.alice().getUsername(), "zeCollection");
		assertThat(findAllRecordsVisibleByUsingWebService(userWithZeCollectionReadAccess))
				.containsOnly(zeCollectionRecord1, zeCollectionRecord2);
		assertThat(findAllRecordsVisibleByUsingWebService(userWithAnotherCollectionReadAccess))
				.containsOnly(anotherCollectionRecord1, anotherCollectionRecord2);
		assertThat(findAllRecordsVisibleByUsingWebService(userWithBothCollectionReadAccess))
				.containsOnly(zeCollectionRecord1, zeCollectionRecord2, anotherCollectionRecord1, anotherCollectionRecord2);

		assertThat(findAllRecordsVisibleByUsingWebService(userInBothCollectionWithoutAnyAccess)).isEmpty();

		assertThat(findAllRecordsVisibleByUsingWebService(userInNoCollection)).isEmpty();

		assertThat(findAllRecordsVisibleByUsingWebService(userWithSomeRecordAccess))
				.containsOnly(zeCollectionRecord1, anotherCollectionRecord1);

		assertThat(findAllRecordsVisibleOfNonSecuredSchemaByUsingWebService(userWithSomeRecordAccess))
				.containsOnly(zeCollectionNonSecuredRecord1, zeCollectionNonSecuredRecord2);

		whenSearchingWithAvalidServiceKeyFromAnotherUserThenException();
		whenSearchingWithInvalidTokenThenException();
		whenSearchingWithNoTokenThenException();
		whenSearchingWithNoServiceKeyThenException();
	}

	@Test
	public void whenSearchingUsingWebServiceThenSameResults()
			throws SolrServerException, IOException {

		assertThatUserIsInCollection(users.alice().getUsername(), "zeCollection");
		assertThat(findAllRecordsVisibleByUsingWebService(userWithZeCollectionReadAccess))
				.containsOnly(zeCollectionRecord1, zeCollectionRecord2);
		assertThat(findAllRecordsVisibleByUsingWebService(userWithAnotherCollectionReadAccess))
				.containsOnly(anotherCollectionRecord1, anotherCollectionRecord2);
		assertThat(findAllRecordsVisibleByUsingWebService(userWithBothCollectionReadAccess))
				.containsOnly(zeCollectionRecord1, zeCollectionRecord2, anotherCollectionRecord1, anotherCollectionRecord2);

		assertThat(findAllRecordsVisibleByUsingWebService(userInBothCollectionWithoutAnyAccess)).isEmpty();

		assertThat(findAllRecordsVisibleByUsingWebService(userInNoCollection)).isEmpty();

		assertThat(findAllRecordsVisibleByUsingWebService(userWithSomeRecordAccess))
				.containsOnly(zeCollectionRecord1, anotherCollectionRecord1);

		whenSearchingWithAvalidServiceKeyFromAnotherUserThenException();
		whenSearchingWithInvalidTokenThenException();
		whenSearchingWithNoTokenThenException();
		whenSearchingWithNoServiceKeyThenException();
	}

	@Test
	public void whenSearchingThenDoNotFindEvents()
			throws Exception {

		SolrClient solrServer = newSearchClient();
		ModifiableSolrParams solrParams = new ModifiableSolrParams().set("q", "eventType_s:RECORD_UPDATE");
		String serviceKey = userServices.giveNewServiceToken(userServices.getUserCredential(systemAdmin.getUsername()));
		String token = userServices.getToken(serviceKey, systemAdmin.getUsername(), "youshallnotpass");
		solrParams.set("serviceKey", serviceKey);
		solrParams.set("token", token);
		assertThat(solrServer.query(solrParams).getResults()).isEmpty();

		solrParams = new ModifiableSolrParams().set("q", "type_s:RECORD_UPDATE");
		solrParams.set("serviceKey", serviceKey);
		solrParams.set("token", token);
		solrParams.set("searchEvents", "false");
		assertThat(solrServer.query(solrParams).getResults()).isEmpty();

	}

	private void setupRecords()
			throws IOException, RecordServicesException {

		User zeCollectionUser = userServices.getUserInCollection(userWithBothCollectionReadAccess.getUsername(), zeCollection);
		User anotherCollectionUser = userServices.getUserInCollection(userWithBothCollectionReadAccess.getUsername(),
				anotherCollection);

		String title = "Au secours, je suis perdu";
		recordServices.add(new TestRecord(zeCollectionSchema, zeCollectionRecord1).set(TITLE, title), zeCollectionUser);
		recordServices.add(new TestRecord(zeCollectionSchema, zeCollectionRecord2).set(TITLE, title), zeCollectionUser);
		recordServices.add(new TestRecord(zeCollectionUnsecuredSchema, zeCollectionNonSecuredRecord1).set(TITLE, title));
		recordServices.add(new TestRecord(zeCollectionUnsecuredSchema, zeCollectionNonSecuredRecord2).set(TITLE, title));
		recordServices.add(new TestRecord(anotherCollectionSchema, anotherCollectionRecord1).set(TITLE, title),
				anotherCollectionUser);
		recordServices.add(new TestRecord(anotherCollectionSchema, anotherCollectionRecord2).set(TITLE, title),
				anotherCollectionUser);
	}

	private List<String> findAllRecordsVisibleBy(UserCredential user) {
		FreeTextSearchServices freeTextSearchServices = getModelLayerFactory().newFreeTextSearchServices();
		SolrParams solrParams = new ModifiableSolrParams().set("q", "search_txt_fr:perdu");
		UserServices userServices = getModelLayerFactory().newUserServices();
		UserCredential refreshedUser = userServices.getUser(user.getUsername());
		List<String> records = new ArrayList<>();
		QueryResponse response = freeTextSearchServices.search(new FreeTextQuery(solrParams).filteredByUser(refreshedUser));
		for (SolrDocument result : response.getResults()) {
			records.add((String) result.getFieldValue("id"));
		}

		return records;
	}

	private void whenSearchingWithInvalidTokenThenException()
			throws SolrServerException, IOException {
		SolrClient solrServer = newSearchClient();
		ModifiableSolrParams solrParams = new ModifiableSolrParams().set("q", "search_txt_fr:perdu");

		String serviceKey = userServices
				.giveNewServiceToken(userServices.getUserCredential(userWithBothCollectionReadAccess.getUsername()));
		solrParams.set("serviceKey", serviceKey);
		solrParams.set("token", "noidea");
		try {

			solrServer.query(solrParams);
			fail("Exception expected");
		} catch (RuntimeException e) {
			assertThat(e.getMessage())
					.contains("invalid authentification information");
		}
	}

	private void whenSearchingWithAvalidServiceKeyFromAnotherUserThenException()
			throws SolrServerException, IOException {
		SolrClient solrServer = newSearchClient();
		ModifiableSolrParams solrParams = new ModifiableSolrParams().set("q", "search_txt_fr:perdu");

		String serviceKey = userServices
				.giveNewServiceToken(userServices.getUserCredential(userWithBothCollectionReadAccess.getUsername()));
		String anotherUserToken = userServices.generateToken(userWithZeCollectionReadAccess.getUsername());
		solrParams.set("serviceKey", serviceKey);
		solrParams.set("token", anotherUserToken);

		try {
			assertThat(solrServer.query(solrParams).getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
			fail("Exception expected");
		} catch (RuntimeException e) {
			assertThat(e.getMessage())
					.contains("invalid authentification information");
		}
	}

	private void whenSearchingWithNoTokenThenException()
			throws SolrServerException, IOException {
		SolrClient solrServer = newSearchClient();
		ModifiableSolrParams solrParams = new ModifiableSolrParams().set("q", "search_txt_fr:perdu");

		String serviceKey = userServices
				.giveNewServiceToken(userServices.getUserCredential(userWithBothCollectionReadAccess.getUsername()));
		solrParams.set("serviceKey", serviceKey);
		try {

			solrServer.query(solrParams);
			fail("Exception expected");
		} catch (RuntimeException e) {
			assertThat(e.getMessage())
					.contains("invalid authentification information");
		}
	}

	private void whenSearchingWithNoServiceKeyThenException()
			throws SolrServerException, IOException {
		SolrClient solrServer = newSearchClient();
		ModifiableSolrParams solrParams = new ModifiableSolrParams().set("q", "search_txt_fr:perdu");

		String token = userServices.generateToken(userWithBothCollectionReadAccess.getUsername());
		solrParams.set("token", token);
		try {

			solrServer.query(solrParams);
			fail("Exception expected");
		} catch (RuntimeException e) {
			assertThat(e.getMessage())
					.contains("invalid authentification information");
		}
	}

	private List<String> findAllRecordsVisibleByUsingWebService(UserCredential user)
			throws SolrServerException, IOException {
		SolrClient solrServer = newSearchClient();
		ModifiableSolrParams solrParams = new ModifiableSolrParams().set("q", "schema_s:zeSchemaType*");

		String serviceKey = userServices.giveNewServiceToken(userServices.getUserCredential(user.getUsername()));
		String token = userServices.getToken(serviceKey, user.getUsername(), "youshallnotpass");
		solrParams.set("serviceKey", serviceKey);
		solrParams.set("token", token);
		List<String> records = new ArrayList<>();
		QueryResponse response = solrServer.query(solrParams);
		for (SolrDocument result : response.getResults()) {
			records.add((String) result.getFieldValue("id"));
		}

		return records;
	}

	private List<String> findAllRecordsVisibleOfNonSecuredSchemaByUsingWebService(UserCredential user)
			throws SolrServerException, IOException {
		SolrClient solrServer = newSearchClient();
		ModifiableSolrParams solrParams = new ModifiableSolrParams().set("q", "schema_s:anotherSchemaType*");
		solrParams.add("fq", "collection_s:zeCollection");
		String serviceKey = userServices.giveNewServiceToken(userServices.getUserCredential(user.getUsername()));
		String token = userServices.getToken(serviceKey, user.getUsername(), "youshallnotpass");
		solrParams.set("serviceKey", serviceKey);
		solrParams.set("token", token);
		List<String> records = new ArrayList<>();
		QueryResponse response = solrServer.query(solrParams);
		for (SolrDocument result : response.getResults()) {
			records.add((String) result.getFieldValue("id"));
		}

		return records;
	}

	private void setupUsers()
			throws RecordServicesException, InterruptedException {
		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.addUpdateUserCredential(users.chuckNorris().setSystemAdminEnabled());

		userWithZeCollectionReadAccess = users.alice();
		userWithAnotherCollectionReadAccess = users.bob();
		userWithBothCollectionReadAccess = users.charles();
		userInBothCollectionWithoutAnyAccess = users.dakotaLIndien();
		userInNoCollection = users.edouardLechat();
		userWithSomeRecordAccess = users.gandalfLeblanc();
		systemAdmin = users.chuckNorris();

		userServices.addUserToCollection(userWithZeCollectionReadAccess, zeCollection);
		userServices.addUserToCollection(userWithAnotherCollectionReadAccess, anotherCollection);
		userServices.addUserToCollection(userWithBothCollectionReadAccess, zeCollection);
		userServices.addUserToCollection(userWithBothCollectionReadAccess, anotherCollection);
		userServices.addUserToCollection(userInBothCollectionWithoutAnyAccess, zeCollection);
		userServices.addUserToCollection(userInBothCollectionWithoutAnyAccess, anotherCollection);
		userServices.addUserToCollection(userWithSomeRecordAccess, zeCollection);
		userServices.addUserToCollection(userWithSomeRecordAccess, anotherCollection);

		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();
		authenticationService.changePassword(systemAdmin.getUsername(), "youshallnotpass");
		authenticationService.changePassword(userWithZeCollectionReadAccess.getUsername(), "youshallnotpass");
		authenticationService.changePassword(userWithAnotherCollectionReadAccess.getUsername(), "youshallnotpass");
		authenticationService.changePassword(userWithBothCollectionReadAccess.getUsername(), "youshallnotpass");
		authenticationService.changePassword(userInBothCollectionWithoutAnyAccess.getUsername(), "youshallnotpass");
		authenticationService.changePassword(userInNoCollection.getUsername(), "youshallnotpass");
		authenticationService.changePassword(userWithSomeRecordAccess.getUsername(), "youshallnotpass");

		waitForBatchProcess();

		userWithZeCollectionReadAccess = userServices.getUserCredential(userWithZeCollectionReadAccess.getUsername());
		userWithAnotherCollectionReadAccess = userServices.getUserCredential(userWithAnotherCollectionReadAccess.getUsername());
		userWithBothCollectionReadAccess = userServices.getUserCredential(userWithBothCollectionReadAccess.getUsername());
		userInBothCollectionWithoutAnyAccess = userServices.getUserCredential(userInBothCollectionWithoutAnyAccess.getUsername());
		userInNoCollection = userServices.getUserCredential(userInNoCollection.getUsername());
		userWithSomeRecordAccess = userServices.getUserCredential(userWithSomeRecordAccess.getUsername());
		systemAdmin = userServices.getUserCredential(systemAdmin.getUsername());
	}

	private void assertThatUserIsInCollection(String username, String collection) {
		UserCredential userCredential = userServices.getUser(username);
		assertThatUserIsInCollection(userCredential, collection);
	}

	private void assertThatUserIsInCollection(UserCredential userCredential, String collection) {
		assertThat(userCredential.getCollections()).contains(collection);
	}

	private void setupUserPermissions()
			throws RecordServicesException {
		recordServices.update(userServices.getUserInCollection(userWithZeCollectionReadAccess.getUsername(),
				zeCollection).setCollectionReadAccess(true));
		recordServices.update(userServices.getUserInCollection(userWithAnotherCollectionReadAccess.getUsername(),
				anotherCollection)
				.setCollectionReadAccess(true));
		recordServices
				.update(userServices.getUserInCollection(userWithBothCollectionReadAccess.getUsername(),
						zeCollection).setCollectionWriteAccess(true));
		recordServices.update(userServices.getUserInCollection(userWithBothCollectionReadAccess.getUsername(), anotherCollection)
				.setCollectionWriteAccess(true));
	}

	private void setupAuthorizations()
			throws Exception {
		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		authorizationsServices.add(authorizationForUsers(users.gandalfLeblancIn(zeCollection))
				.on(zeCollectionRecord1).givingReadAccess());

		authorizationsServices.add(authorizationForUsers(users.gandalfLeblancIn(anotherCollection))
				.on(anotherCollectionRecord1).givingReadAccess());

		waitForBatchProcess();
	}
}
