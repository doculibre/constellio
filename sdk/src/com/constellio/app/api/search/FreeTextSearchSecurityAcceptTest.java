/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.api.search;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.query.logical.FreeTextQuery;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidUserNameOrPassword;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;

@SlowTest
public class FreeTextSearchSecurityAcceptTest extends ConstellioTest {

	String anotherCollection = "anotherCollection";
	TestsSchemasSetup anotherCollectionSetup = new TestsSchemasSetup(anotherCollection);
	ZeSchemaMetadatas anotherCollectionSchema = anotherCollectionSetup.new ZeSchemaMetadatas();
	TestsSchemasSetup zeCollectionSetup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeCollectionSchema = zeCollectionSetup.new ZeSchemaMetadatas();
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
	String anotherCollectionRecord1 = "anotherCollectionRecord1";
	String anotherCollectionRecord2 = "anotherCollectionRecord2";

	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {

		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();

		givenCollection(zeCollection, asList("fr", "en"));
		givenCollection(anotherCollection, asList("fr"));

		defineSchemasManager().using(zeCollectionSetup.withAStringMetadata(whichIsMultivalue).withAContentMetadata());
		defineSchemasManager().using(anotherCollectionSetup.withAStringMetadata(whichIsMultivalue).withAContentListMetadata());

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
				.containsOnly(zeCollectionRecord1, zeCollectionRecord2);
		assertThat(findAllRecordsVisibleBy(userWithAnotherCollectionReadAccess))
				.containsOnly(anotherCollectionRecord1, anotherCollectionRecord2);
	}

	@Test
	public void givenUserInBothCollectionWithReadAccessWhenSearchingWithFreeTextUsingWildcardThenSeeAllRecordsOfBothCollection()
			throws Exception {
		assertThat(findAllRecordsVisibleBy(userWithBothCollectionReadAccess))
				.containsOnly(zeCollectionRecord1, zeCollectionRecord2, anotherCollectionRecord1, anotherCollectionRecord2);
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
	public void whenSearchingUsingWebServiceThenSameResults()
			throws SolrServerException {

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

		whenSearchingWithInvalidPasswordThenException();
		whenSearchingWithNoPasswordThenException();
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
		recordServices.add(new TestRecord(zeCollectionSchema, zeCollectionRecord1).set(Schemas.TITLE, title), zeCollectionUser);
		recordServices.add(new TestRecord(zeCollectionSchema, zeCollectionRecord2).set(Schemas.TITLE, title), zeCollectionUser);
		recordServices.add(new TestRecord(anotherCollectionSchema, anotherCollectionRecord1).set(Schemas.TITLE, title),
				anotherCollectionUser);
		recordServices.add(new TestRecord(anotherCollectionSchema, anotherCollectionRecord2).set(Schemas.TITLE, title),
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

	private void whenSearchingWithInvalidPasswordThenException()
			throws SolrServerException {
		SolrClient solrServer = newSearchClient();
		ModifiableSolrParams solrParams = new ModifiableSolrParams().set("q", "search_txt_fr:perdu");

		try {
			String serviceKey = userServices
					.giveNewServiceToken(userServices.getUserCredential(userWithBothCollectionReadAccess.getUsername()));
			String token = userServices.getToken(serviceKey, userWithBothCollectionReadAccess.getUsername(), "noidea");
			solrParams.set("serviceKey", serviceKey);
			solrParams.set("token", token);
			solrServer.query(solrParams);
			fail("Exception expected");
		} catch (UserServicesRuntimeException_InvalidUserNameOrPassword e) {
			assertThat(e.getMessage())
					.contains("Invalid username " + userWithBothCollectionReadAccess.getUsername() + " or password");
		}
	}

	private void whenSearchingWithNoPasswordThenException()
			throws SolrServerException {
		SolrClient solrServer = newSearchClient();
		ModifiableSolrParams solrParams = new ModifiableSolrParams().set("q", "search_txt_fr:perdu");

		try {
			String serviceKey = userServices
					.giveNewServiceToken(userServices.getUserCredential(userWithBothCollectionReadAccess.getUsername()));
			String token = userServices.getToken(serviceKey, userWithBothCollectionReadAccess.getUsername(), null);
			solrParams.set("serviceKey", serviceKey);
			solrParams.set("token", token);
			solrServer.query(solrParams);
			fail("Exception expected");
		} catch (UserServicesRuntimeException_InvalidUserNameOrPassword e) {
			assertThat(e.getMessage())
					.contains("Invalid username " + userWithBothCollectionReadAccess.getUsername() + " or password");
		}
	}

	private List<String> findAllRecordsVisibleByUsingWebService(UserCredential user)
			throws SolrServerException {
		SolrClient solrServer = newSearchClient();
		ModifiableSolrParams solrParams = new ModifiableSolrParams().set("q", "search_txt_fr:perdu");

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
		users.setUp(userServices);
		userServices.addUpdateUserCredential(users.chuckNorris().withSystemAdminPermission());

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
			throws RecordServicesException, InterruptedException {
		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		AuthorizationDetails zeCollectionAuth = AuthorizationDetails
				.create("1", asList(Role.READ), zeCollection);
		authorizationsServices.add(new Authorization(zeCollectionAuth, asList(users.gandalfLeblancIn(zeCollection).getId()),
				asList(zeCollectionRecord1)), CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		AuthorizationDetails anotherCollectionAuth = AuthorizationDetails
				.create("2", asList(Role.READ), anotherCollection);
		authorizationsServices
				.add(new Authorization(anotherCollectionAuth, asList(users.gandalfLeblancIn(anotherCollection).getId()),
						asList(anotherCollectionRecord1)), CustomizedAuthorizationsBehavior.KEEP_ATTACHED, null);

		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
