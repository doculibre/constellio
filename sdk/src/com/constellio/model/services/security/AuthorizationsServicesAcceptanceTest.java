package com.constellio.model.services.security;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserPermissionsChecker;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SingletonSecurityModel;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.search.FreeTextSearchServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.FreeTextQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithIdOnRecord;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchPrincipalWithUsername;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup.*;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.enums.GroupAuthorizationsInheritance.FROM_CHILD_TO_PARENT;
import static com.constellio.model.entities.records.wrappers.Event.*;
import static com.constellio.model.entities.schemas.Schemas.*;
import static com.constellio.model.entities.security.Role.*;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;
import static com.constellio.model.entities.security.global.AuthorizationModificationRequest.modifyAuthorizationOnRecord;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.GROUP_AUTHORIZATIONS_INHERITANCE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.*;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

public class AuthorizationsServicesAcceptanceTest extends BaseAuthorizationsServicesAcceptanceTest {

	//TODO Mieux tester la journalisation des modifications

	boolean checkIfChuckNorrisHasAccessToEverythingInZeCollection = true;

	LogicalSearchQuery recordsWithPrincipalPath = new LogicalSearchQuery(
			fromAllSchemasIn(zeCollection).where(PRINCIPAL_PATH).isNotNull());

	@Before
	public void enableAfterTestValidation() {
		checkIfChuckNorrisHasAccessToEverythingInZeCollection = true;
	}

	@After
	public void checkIfARecordHasAnInvalidAuthorization() {
		ensureNoRecordsHaveAnInvalidAuthorization();
	}

	@After
	public void checkIfChuckNorrisHasAccessToEverythingInZeCollection()
			throws Exception {

		if (records != null && checkIfChuckNorrisHasAccessToEverythingInZeCollection) {
			getDataLayerFactory().getDataLayerLogger().setQueryLoggingEnabled(false).setQueryDebuggingMode(false);
			List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.chuckNorrisIn(zeCollection));
			List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(
					users.chuckNorrisIn(zeCollection));
			List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
					users.chuckNorrisIn(zeCollection));

			assertThat(foldersWithReadFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithWriteFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithDeleteFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
		}
	}

	@After
	public void checkIfAliceSeeAndCanModifyEverythingInCollection2()
			throws Exception {
		if (otherCollectionRecords != null) {
			getDataLayerFactory().getDataLayerLogger().setQueryLoggingEnabled(false).setQueryDebuggingMode(false);
			List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.aliceIn(anotherCollection));
			List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.aliceIn(anotherCollection));
			List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
					users.aliceIn(anotherCollection));

			assertThat(foldersWithReadFound)
					.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithWriteFound)
					.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithDeleteFound).hasSize(0);
		}
	}

	@After
	public void checkIfBobSeeAndCanDeleteEverythingInCollection2()
			throws Exception {

		if (otherCollectionRecords != null) {
			getDataLayerFactory().getDataLayerLogger().setQueryLoggingEnabled(false).setQueryDebuggingMode(false);
			List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.bobIn(anotherCollection));
			List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.bobIn(anotherCollection));
			List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(users.bobIn(anotherCollection));

			assertThat(foldersWithReadFound)
					.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithWriteFound).hasSize(0);
			assertThat(foldersWithDeleteFound)
					.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
		}
	}


	boolean checkIfDakotaSeeAndCanDeleteEverythingInCollection2 = true;

	@After
	public void checkIfDakotaSeeAndCanDeleteEverythingInCollection2()
			throws Exception {
		if (otherCollectionRecords != null && checkIfDakotaSeeAndCanDeleteEverythingInCollection2
			&& taxonomiesManager.getPrincipalTaxonomy(anotherCollection) == null) {
			getDataLayerFactory().getDataLayerLogger().setQueryLoggingEnabled(false).setQueryDebuggingMode(false);
			List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.dakotaIn(anotherCollection));
			List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.dakotaIn(anotherCollection));
			List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
					users.dakotaIn(anotherCollection));

			assertThat(foldersWithReadFound)
					.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
			assertThat(foldersWithWriteFound).hasSize(0);
			assertThat(foldersWithDeleteFound).hasSize(0);
		}
	}

	@After
	public void validateNoAuthorizationsToRecalculate()
			throws Exception {

		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, getModelLayerFactory());
			for (Authorization auth : schemas.getAllAuthorizationsInUnmodifiableState()) {
				if (auth.hasModifiedStatusSinceLastTokenRecalculate()) {
					fail("authorization '" + auth.getId() + "' on target '" + auth.getTarget() + "'");
				}
			}
		}
	}

	@Test
	public void whenRecordIsSecurableThenHasAncestors()
			throws Exception {

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, ATTACHED_ANCESTORS).containsOnly(
				tuple(TAXO1_FOND1, asList(TAXO1_FOND1)),
				tuple(TAXO1_FOND1_1, asList(TAXO1_FOND1, TAXO1_FOND1_1)),
				tuple(FOLDER4_1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, FOLDER4, FOLDER4_1)),
				tuple(FOLDER4_2, asList(TAXO1_FOND1, TAXO1_CATEGORY2, FOLDER4, FOLDER4_2)),
				tuple(FOLDER2, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2)),
				tuple(FOLDER1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER1)),
				tuple(TAXO1_CATEGORY1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1)),
				tuple(FOLDER2_2_DOC2, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_2, FOLDER2_2_DOC2)),
				tuple(FOLDER3, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3)),
				tuple(FOLDER4, asList(TAXO1_FOND1, TAXO1_CATEGORY2, FOLDER4)),
				tuple(FOLDER2_2_DOC1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_2, FOLDER2_2_DOC1)),
				tuple(FOLDER4_2_DOC1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, FOLDER4, FOLDER4_2, FOLDER4_2_DOC1)),
				tuple(FOLDER1_DOC1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER1, FOLDER1_DOC1)),
				tuple(FOLDER2_1, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_1)),
				tuple(FOLDER2_2, asList(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2, FOLDER2_2)),
				tuple(TAXO1_CATEGORY2, asList(TAXO1_FOND1, TAXO1_CATEGORY2)),
				tuple(TAXO1_CATEGORY2_1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1)),
				tuple(FOLDER3_DOC1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)),
				tuple(FOLDER4_1_DOC1, asList(TAXO1_FOND1, TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1))
		);

		detach(FOLDER4);
		detach(FOLDER2);

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, ATTACHED_ANCESTORS).containsOnly(
				tuple("taxo1_fond1", asList("taxo1_fond1")),
				tuple("taxo1_fond1_1", asList("taxo1_fond1", "taxo1_fond1_1")),
				tuple("taxo1_category1", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1")),
				tuple("taxo1_category2", asList("taxo1_fond1", "taxo1_category2")),
				tuple("taxo1_category2_1", asList("taxo1_fond1", "taxo1_category2", "taxo1_category2_1")),
				tuple("folder1", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1", "folder1")),
				tuple("folder3", asList("taxo1_fond1", "taxo1_category2", "taxo1_category2_1", "folder3")),
				tuple("folder1_doc1", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1", "folder1", "folder1_doc1")),
				tuple("folder3_doc1", asList("taxo1_fond1", "taxo1_category2", "taxo1_category2_1", "folder3", "folder3_doc1")),
				tuple("folder4", asList("folder4", "-taxo1_fond1", "-taxo1_category2")),
				tuple("folder4_1", asList("folder4", "-taxo1_fond1", "-taxo1_category2", "folder4_1")),
				tuple("folder4_2", asList("folder4", "-taxo1_fond1", "-taxo1_category2", "folder4_2")),
				tuple("folder4_1_doc1", asList("folder4", "-taxo1_fond1", "-taxo1_category2", "folder4_1", "folder4_1_doc1")),
				tuple("folder4_2_doc1", asList("folder4", "-taxo1_fond1", "-taxo1_category2", "folder4_2", "folder4_2_doc1")),
				tuple("folder2", asList("folder2", "-taxo1_fond1", "-taxo1_fond1_1", "-taxo1_category1")),
				tuple("folder2_1", asList("folder2", "-taxo1_fond1", "-taxo1_fond1_1", "-taxo1_category1", "folder2_1")),
				tuple("folder2_2", asList("folder2", "-taxo1_fond1", "-taxo1_fond1_1", "-taxo1_category1", "folder2_2")),
				tuple("folder2_2_doc2", asList("folder2", "-taxo1_fond1", "-taxo1_fond1_1", "-taxo1_category1", "folder2_2", "folder2_2_doc2")),
				tuple("folder2_2_doc1", asList("folder2", "-taxo1_fond1", "-taxo1_fond1_1", "-taxo1_category1", "folder2_2", "folder2_2_doc1"))
		);

		reset(FOLDER2);
		detach(FOLDER4_1);

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, ATTACHED_ANCESTORS).containsOnly(
				tuple("taxo1_fond1", asList("taxo1_fond1")),
				tuple("taxo1_fond1_1", asList("taxo1_fond1", "taxo1_fond1_1")),
				tuple("taxo1_category1", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1")),
				tuple("taxo1_category2", asList("taxo1_fond1", "taxo1_category2")),
				tuple("taxo1_category2_1", asList("taxo1_fond1", "taxo1_category2", "taxo1_category2_1")),
				tuple("folder1", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1", "folder1")),
				tuple("folder3", asList("taxo1_fond1", "taxo1_category2", "taxo1_category2_1", "folder3")),
				tuple("folder1_doc1", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1", "folder1", "folder1_doc1")),
				tuple("folder3_doc1", asList("taxo1_fond1", "taxo1_category2", "taxo1_category2_1", "folder3", "folder3_doc1")),
				tuple("folder2", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1", "folder2")),
				tuple("folder2_1", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1", "folder2", "folder2_1")),
				tuple("folder2_2", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1", "folder2", "folder2_2")),
				tuple("folder2_2_doc2", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1", "folder2", "folder2_2", "folder2_2_doc2")),
				tuple("folder2_2_doc1", asList("taxo1_fond1", "taxo1_fond1_1", "taxo1_category1", "folder2", "folder2_2", "folder2_2_doc1")),
				tuple("folder4_1", asList("folder4_1", "-folder4", "--taxo1_fond1", "--taxo1_category2")),
				tuple("folder4_1_doc1", asList("folder4_1", "-folder4", "--taxo1_fond1", "--taxo1_category2", "folder4_1_doc1")),
				tuple("folder4", asList("folder4", "-taxo1_fond1", "-taxo1_category2")),
				tuple("folder4_2", asList("folder4", "-taxo1_fond1", "-taxo1_category2", "folder4_2")),
				tuple("folder4_2_doc1", asList("folder4", "-taxo1_fond1", "-taxo1_category2", "folder4_2", "folder4_2_doc1"))
		);
	}

	@Test
	public void whenRecordIsSecurableThenHasInheritedRemovedAuths()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_FOND1).giving(ROLE1));
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));

		LogicalSearchQuery query = new LogicalSearchQuery(
				fromAllSchemasIn(zeCollection).where(ALL_REMOVED_AUTHS).isNotNull());
		assertThatRecords(searchServices.search(query)).extractingMetadatas(IDENTIFIER, ALL_REMOVED_AUTHS).isEmpty();

		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		modify(authorizationOnRecord(auth2, FOLDER3).removingItOnRecord());
		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, ALL_REMOVED_AUTHS).containsOnly(
				tuple(TAXO1_FOND1, new ArrayList<>()),
				tuple(TAXO1_FOND1_1, new ArrayList<>()),
				tuple(TAXO1_CATEGORY1, asList(auth1)),
				tuple(TAXO1_CATEGORY2, new ArrayList<>()),
				tuple(TAXO1_CATEGORY2_1, new ArrayList<>()),

				tuple(FOLDER1, asList(auth1)),
				tuple(FOLDER1_DOC1, asList(auth1)),
				tuple(FOLDER2, asList(auth1)),
				tuple(FOLDER2_1, asList(auth1)),
				tuple(FOLDER2_2, asList(auth1)),
				tuple(FOLDER2_2_DOC2, asList(auth1)),
				tuple(FOLDER2_2_DOC1, asList(auth1)),
				tuple(FOLDER3, asList(auth2)),
				tuple(FOLDER3_DOC1, asList(auth2)),
				tuple(FOLDER4, new ArrayList<>()),
				tuple(FOLDER4_1, new ArrayList<>()),
				tuple(FOLDER4_1_DOC1, new ArrayList<>()),
				tuple(FOLDER4_2, new ArrayList<>()),
				tuple(FOLDER4_2_DOC1, new ArrayList<>())
		);

		detach(FOLDER2_2);
		detach(FOLDER3_DOC1);

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, ALL_REMOVED_AUTHS).containsOnly(
				tuple(TAXO1_FOND1, new ArrayList<>()),
				tuple(TAXO1_FOND1_1, new ArrayList<>()),
				tuple(TAXO1_CATEGORY1, asList(auth1)),
				tuple(TAXO1_CATEGORY2, new ArrayList<>()),
				tuple(TAXO1_CATEGORY2_1, new ArrayList<>()),

				tuple(FOLDER1, asList(auth1)),
				tuple(FOLDER1_DOC1, asList(auth1)),
				tuple(FOLDER2, asList(auth1)),
				tuple(FOLDER2_1, asList(auth1)),
				tuple(FOLDER2_2, new ArrayList<>()),
				tuple(FOLDER2_2_DOC2, new ArrayList<>()),
				tuple(FOLDER2_2_DOC1, new ArrayList<>()),
				tuple(FOLDER3, asList(auth2)),
				tuple(FOLDER3_DOC1, new ArrayList<>()),
				tuple(FOLDER4, new ArrayList<>()),
				tuple(FOLDER4_1, new ArrayList<>()),
				tuple(FOLDER4_1_DOC1, new ArrayList<>()),
				tuple(FOLDER4_2, new ArrayList<>()),
				tuple(FOLDER4_2_DOC1, new ArrayList<>())
		);

	}

	@Test
	public void whenRecordIsSecurableThenHasAccuratePrincipalsWithSpecificAuthorization()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_FOND1).giving(ROLE1));
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));

		LogicalSearchQuery query = new LogicalSearchQuery(
				fromAllSchemasIn(zeCollection).where(TOKENS).isNotNull());
		assertThatRecords(searchServices.search(query)).extractingMetadatas(IDENTIFIER, TOKENS).isEmpty();

		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		modify(authorizationOnRecord(auth2, FOLDER3).removingItOnRecord());
		auth3 = add(authorizationForGroup(heroes).on(FOLDER4).giving(ROLE1));

		String heroesId = users.heroesIn(zeCollection).getId();
		String sidekicksId = users.sidekicksIn(zeCollection).getId();
		String legendsId = users.legendsIn(zeCollection).getId();
		String rumorsId = users.rumorsIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();
		String gandalfId = users.gandalfIn(zeCollection).getId();

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, TOKENS).containsOnly(
				tuple(TAXO1_FOND1, new ArrayList<>()),
				tuple(TAXO1_FOND1_1, new ArrayList<>()),
				tuple(TAXO1_CATEGORY1, new ArrayList<>()),
				tuple(TAXO1_CATEGORY2, new ArrayList<>()),
				tuple(TAXO1_CATEGORY2_1, new ArrayList<>()),

				tuple(FOLDER1, new ArrayList<>()),
				tuple(FOLDER1_DOC1, new ArrayList<>()),
				tuple(FOLDER2, new ArrayList<>()),
				tuple(FOLDER2_1, new ArrayList<>()),
				tuple(FOLDER2_2, new ArrayList<>()),
				tuple(FOLDER2_2_DOC2, new ArrayList<>()),
				tuple(FOLDER2_2_DOC1, new ArrayList<>()),
				tuple(FOLDER3, new ArrayList<>()),
				tuple(FOLDER3_DOC1, new ArrayList<>()),
				tuple(FOLDER4, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_1, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_1_DOC1, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_2, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_2_DOC1, asList("role1_" + heroesId, "role1_" + sidekicksId))
		);

		String copyOfAuth1 = detach(FOLDER2_2).get(auth1);
		Map<String, String> newFolder3Auths = detach(FOLDER3_DOC1);

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, TOKENS).containsOnly(
				tuple(TAXO1_FOND1, new ArrayList<>()),
				tuple(TAXO1_FOND1_1, new ArrayList<>()),
				tuple(TAXO1_CATEGORY1, new ArrayList<>()),
				tuple(TAXO1_CATEGORY2, new ArrayList<>()),
				tuple(TAXO1_CATEGORY2_1, new ArrayList<>()),

				tuple(FOLDER1, new ArrayList<>()),
				tuple(FOLDER1_DOC1, new ArrayList<>()),
				tuple(FOLDER2, new ArrayList<>()),
				tuple(FOLDER2_1, new ArrayList<>()),
				tuple(FOLDER2_2, asList("role1_" + bobId)),
				tuple(FOLDER2_2_DOC2, asList("role1_" + bobId)),
				tuple(FOLDER2_2_DOC1, asList("role1_" + bobId)),
				tuple(FOLDER3, new ArrayList<>()),
				tuple(FOLDER3_DOC1, asList("role1_" + heroesId, "role1_" + sidekicksId, "role1_" + bobId)),
				tuple(FOLDER4, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_1, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_1_DOC1, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_2, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_2_DOC1, asList("role1_" + heroesId, "role1_" + sidekicksId))
		);

		modify(authorizationOnRecord(newFolder3Auths.get(auth1), FOLDER3_DOC1).withNewPrincipalIds(gandalfId));
		modify(authorizationOnRecord(newFolder3Auths.get(auth2), FOLDER3_DOC1).withNewPrincipalIds(legendsId));

		assertThatRecords(searchServices.search(recordsWithPrincipalPath))
				.extractingMetadatas(IDENTIFIER, TOKENS).containsOnly(
				tuple(TAXO1_FOND1, new ArrayList<>()),
				tuple(TAXO1_FOND1_1, new ArrayList<>()),
				tuple(TAXO1_CATEGORY1, new ArrayList<>()),
				tuple(TAXO1_CATEGORY2, new ArrayList<>()),
				tuple(TAXO1_CATEGORY2_1, new ArrayList<>()),

				tuple(FOLDER1, new ArrayList<>()),
				tuple(FOLDER1_DOC1, new ArrayList<>()),
				tuple(FOLDER2, new ArrayList<>()),
				tuple(FOLDER2_1, new ArrayList<>()),
				tuple(FOLDER2_2, asList("role1_" + bobId)),
				tuple(FOLDER2_2_DOC2, asList("role1_" + bobId)),
				tuple(FOLDER2_2_DOC1, asList("role1_" + bobId)),
				tuple(FOLDER3, new ArrayList<>()),
				tuple(FOLDER3_DOC1, asList("role1_" + legendsId, "role1_" + rumorsId, "role1_" + gandalfId)),
				tuple(FOLDER4, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_1, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_1_DOC1, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_2, asList("role1_" + heroesId, "role1_" + sidekicksId)),
				tuple(FOLDER4_2_DOC1, asList("role1_" + heroesId, "role1_" + sidekicksId))
		);
	}

	@Test
	public void givenRoleAuthorizationsOnPrincipalConceptsThenInheritedInHierarchy()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth3 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2_1).giving(ROLE1));
		auth4 = add(authorizationForUser(sasquatch).on(FOLDER1).giving(ROLE2));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1).forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1).forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingRoles(ROLE1).forPrincipals(alice),
				authOnRecord(FOLDER1).givingRoles(ROLE2).forPrincipals(sasquatch)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithRole(ROLE1).containsOnly(bob, charles, dakota, gandalf, robin);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).containsOnly(admin, bob, charles, dakota, gandalf, robin);
			verifyRecord.usersWithRole(ROLE2).isEmpty();
			verifyRecord.usersWithRole(ROLE3).isEmpty();
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE1).containsOnly(bob, alice, charles, dakota, gandalf, robin);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1)
					.containsOnly(admin, bob, alice, charles, dakota, gandalf, robin);
			verifyRecord.usersWithRole(ROLE2).isEmpty();
			verifyRecord.usersWithRole(ROLE3).isEmpty();
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(FOLDER1)) {
			verifyRecord.usersWithRole(ROLE2).containsOnly(sasquatch);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}
	}

	@Test
	public void givenAuthsOnChildGroupThenOnlyInheritedIfChildToParentMode()
			throws Exception {

		auth1 = add(authorizationForGroup(legends).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = add(authorizationForGroup(rumors).on(TAXO1_CATEGORY2).giving(ROLE2));

		//TODO Should be inherited in child groups : Robin would have ROLE1
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).containsOnly(sasquatch, gandalf, admin, alice, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).containsOnly(sasquatch, admin);
		}

		givenConfig(GROUP_AUTHORIZATIONS_INHERITANCE, FROM_CHILD_TO_PARENT);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).containsOnly(gandalf, admin, alice, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).containsOnly(sasquatch, gandalf, admin, alice, edouard);
		}
	}

	@Test
	public void givenRolesOfAuthorizationAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewAccessAndRoles(ROLE2, ROLE3)))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2).withNewAccessAndRoles(ROLE1, ROLE3)))
				.isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE2, ROLE3).forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1, ROLE3).forPrincipals(heroes)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3_DOC1, FOLDER4_1_DOC1, FOLDER4_2)) {
			verifyRecord.usersWithRole(ROLE1).containsOnly(charles, dakota, gandalf, robin);
			verifyRecord.usersWithRole(ROLE2).containsOnly(bob);
			verifyRecord.usersWithRole(ROLE3).containsOnly(bob, charles, dakota, gandalf, robin);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void givenAccessAuthorizationsOnPrincipalConceptsThenInheritedInHierarchy()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth3 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2_1).givingReadWriteAccess());
		auth4 = add(authorizationForUser(sasquatch).on(FOLDER1).givingReadWriteAccess());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER1).givingReadWrite().forPrincipals(sasquatch)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, dakota, gandalf, chuck, robin);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, alice, charles, dakota, chuck, robin, gandalf);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(FOLDER1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(sasquatch, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithHierarchyWriteAccess().containsOnly(bob, charles, dakota, gandalf, chuck, robin);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithHierarchyWriteAccess().containsOnly(bob, alice, charles, dakota, chuck, robin, gandalf);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(FOLDER1)) {
			verifyRecord.usersWithHierarchyWriteAccess().containsOnly(sasquatch, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void givenWriteOnlyAccessAuthorizationsThenReadAndWriteAccessReceived()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingWriteAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingWriteAccess());
		auth3 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2_1).givingWriteAccess());
		auth4 = add(authorizationForUser(sasquatch).on(FOLDER1).givingWriteAccess());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingWrite().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingWrite().forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingWrite().forPrincipals(alice),
				authOnRecord(FOLDER1).givingWrite().forPrincipals(sasquatch)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, dakota, gandalf, chuck, robin);
			verifyRecord.usersWithReadAccess().containsOnly(bob, charles, dakota, gandalf, chuck, robin);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, alice, charles, dakota, chuck, robin, gandalf);
			verifyRecord.usersWithReadAccess().containsOnly(bob, alice, charles, dakota, chuck, robin, gandalf);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}


		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(sasquatch, chuck);
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1,
				TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1, FOLDER1, FOLDER1_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}
	}

	@Test
	public void givenDeleteOnlyAccessAuthorizationsThenReadAndDeleteAccessReceived()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingDeleteAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingDeleteAccess());
		auth3 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2_1).givingDeleteAccess());
		auth4 = add(authorizationForUser(sasquatch).on(FOLDER1).givingDeleteAccess());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingDelete().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingDelete().forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingDelete().forPrincipals(alice),
				authOnRecord(FOLDER1).givingDelete().forPrincipals(sasquatch)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(bob, charles, dakota, gandalf, chuck, robin);
			verifyRecord.usersWithReadAccess().containsOnly(bob, charles, dakota, gandalf, chuck, robin);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(bob, alice, charles, dakota, chuck, robin, gandalf);
			verifyRecord.usersWithReadAccess().containsOnly(bob, alice, charles, dakota, chuck, robin, gandalf);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(sasquatch, chuck);
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1,
				TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1, FOLDER1, FOLDER1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
		}

	}

	@Test
	public void givenAccessTypesOfAuthorizationAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(chuck);
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
		}

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewAccessAndRoles(WRITE, DELETE)))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2).withNewAccessAndRoles(WRITE)))
				.isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(heroes)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER4, FOLDER4_1_DOC1, FOLDER3_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, gandalf, bob, robin, chuck);
			verifyRecord.usersWithDeleteAccess().containsOnly(bob, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void givenPrincipalsAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewPrincipalIds(robin)))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2).withNewPrincipalIds(legends, bob)))
				.isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(robin),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(legends, bob)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_2_DOC1, TAXO1_CATEGORY2_1, FOLDER3,
				FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob, robin, chuck);
			verifyRecord.usersWithWriteAccess().containsOnly(robin, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void givenPrincipalsAreModifiedOnRecordOfAuthorizationKeepingAttachedThenDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2_1).withNewPrincipalIds(robin)))
				.is(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2_1).withNewPrincipalIds(legends, bob)))
				.is(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(bob).removedOnRecords(TAXO1_CATEGORY2_1),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(heroes).removedOnRecords(TAXO1_CATEGORY2_1),
				authOnRecord(TAXO1_CATEGORY2_1).givingReadWrite().forPrincipals(robin),
				authOnRecord(TAXO1_CATEGORY2_1).givingRead().forPrincipals(legends, bob)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, gandalf, robin, bob, chuck);
			verifyRecord.usersWithWriteAccess().containsOnly(bob, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob, robin, chuck);
			verifyRecord.usersWithWriteAccess().containsOnly(robin, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void whenModifyingAnInvalidAuthorizationOnRecordThenException()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(FOLDER4).givingReadAccess());
		auth2 = add(authorizationForGroup(heroes).on(FOLDER4).givingReadAccess());

		detach(FOLDER4_1);

		try {
			modify(authorizationOnRecord(auth1, FOLDER4_1).withNewPrincipalIds(robin));
			fail("Exception expected");
		} catch (NoSuchAuthorizationWithIdOnRecord e) {
			//OK
		}

		try {
			modify(authorizationOnRecord(auth2, FOLDER4_1).withNewPrincipalIds(robin));
			fail("Exception expected");
		} catch (NoSuchAuthorizationWithIdOnRecord e) {
			//OK
		}

		try {
			modify(authorizationOnRecord("invalidAuth", FOLDER4_1).withNewPrincipalIds(robin));
			fail("Exception expected");
		} catch (NoSuchAuthorizationWithId e) {
			//OK
		}
	}

	@Test
	public void givenPrincipalsAreModifiedOnRecordOfAuthorizationDetachingThenDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		Map<String, String> copies = detach(FOLDER3);
		String auth1CopyInCategory2_1 = copies.get(auth1);
		String auth2CopyInCategory2_1 = copies.get(auth2);

		request1 = modify(authorizationOnRecord(auth1CopyInCategory2_1, FOLDER3).withNewPrincipalIds(robin));

		assertThat(request1).isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(heroes),
				authOnRecord(FOLDER3).givingRead().forPrincipals(robin),
				authOnRecord(FOLDER3).givingRead().forPrincipals(heroes)
		);

		request2 = modify(authorizationOnRecord(auth2CopyInCategory2_1, FOLDER3).withNewPrincipalIds(legends, bob));
		assertThat(request2).isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(heroes),
				authOnRecord(FOLDER3).givingRead().forPrincipals(robin),
				authOnRecord(FOLDER3).givingRead().forPrincipals(legends, bob)
		);

		assertThatAuth(auth1).hasPrincipals(bob);
		assertThatAuth(auth2).hasPrincipals(heroes);
		assertThatAuth(auth1CopyInCategory2_1).hasPrincipals(robin);
		assertThatAuth(auth2CopyInCategory2_1).hasPrincipals(legends, bob);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, gandalf, robin, bob, chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob, chuck, robin);
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			if (verifyRecord.recordId.equals(FOLDER3)) {
				verifyRecord.detachedAuthorizationFlag().isTrue();
			} else {
				verifyRecord.detachedAuthorizationFlag().isFalse();
			}
		}

	}

	@Test
	public void givenGroupAuthorizationsWhenAddOrRemoveUsersInGroupThenInstantaneousEffectOnSecurity()
			throws Exception {

		add(authorizationForGroup(heroes).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		add(authorizationForGroup(heroes).on(TAXO1_CATEGORY1).giving(ROLE1));
		add(authorizationForGroup(heroes).on(FOLDER4).givingReadWriteDeleteAccess());
		add(authorizationForGroup(heroes).on(FOLDER4).giving(ROLE2));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER1, FOLDER2, FOLDER2_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, gandalf, chuck, robin);
			verifyRecord.usersWithRole(ROLE1).containsOnly(charles, dakota, gandalf, robin);
		}

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(charles, dakota, gandalf, chuck, robin);
			verifyRecord.usersWithRole(ROLE2).containsOnly(charles, dakota, gandalf, robin);
		}

		givenUser(charles).isRemovedFromGroup(heroes);
		givenUser(robin).isRemovedFromGroup(sidekicks);
		givenUser(sasquatch).isAddedInGroup(heroes);
		givenUser(edouard).isAddedInGroup(sidekicks);

		ConstellioFactories.getInstance().onRequestEnded();
		ConstellioFactories.getInstance().onRequestStarted();

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER1, FOLDER2, FOLDER2_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(sasquatch, dakota, gandalf, chuck, edouard);
			verifyRecord.usersWithRole(ROLE1).containsOnly(sasquatch, dakota, gandalf, edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(sasquatch, dakota, gandalf, chuck, edouard);

			verifyRecord.usersWithRole(ROLE2).containsOnly(sasquatch, dakota, gandalf, edouard);
		}

	}

	@Test
	public void whenAddingAndRemovingAuthorizationToAGroupThenAppliedToAllUsers()
			throws Exception {

		GlobalGroup group = userServices.createGlobalGroup("vilains", "Vilains", new ArrayList<String>(), null, GlobalGroupStatus.ACTIVE, true);
		userServices.addUpdateGlobalGroup(group);
		userServices.setGlobalGroupUsers("vilains", asList(users.bob()));
		forUser(bob).assertThatRecordsWithReadAccess().isEmpty();

		auth1 = add(authorizationForGroup("vilains").on(TAXO1_CATEGORY1).givingReadAccess());
		forUser(bob).assertThatRecordsWithReadAccess().containsOnly(TAXO1_CATEGORY1, FOLDER1, FOLDER1_DOC1, FOLDER2, FOLDER2_1,
				FOLDER2_2, FOLDER2_2_DOC1, FOLDER2_2_DOC2);

		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		forUser(bob).assertThatRecordsWithReadAccess().isEmpty();

	}

	@Test
	public void givenAuthorizationWhenModifyingAuthorizationWithoutPrincipalsThenValidationException()
			throws Exception {

		String aliceId = users.aliceIn(zeCollection).getId();

		try {
			add(authorizationInCollection(zeCollection).givingReadAccess().on(FOLDER4));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords e) {
			//OK
		}

		try {
			add(authorizationInCollection(zeCollection).givingReadAccess().forPrincipalsIds(new ArrayList<String>())
					.on(FOLDER4));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords e) {
			//OK
		}

		try {
			add(authorizationInCollection(zeCollection).givingReadAccess().forPrincipalsIds(asList(aliceId)));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords e) {
			//OK
		}

		auth1 = add(authorizationForUser(alice).on(FOLDER4).givingReadAccess());

		try {
			modify(authorizationOnRecord(auth1, FOLDER4).withNewPrincipalIds(new ArrayList<String>()));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords e) {
			//OK
		}

		try {
			modify(authorizationOnRecord(auth1, FOLDER4_1).withNewPrincipalIds(new ArrayList<String>()));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords e) {
			//OK
		}
	}

	@Test
	public void givenAuthorizationWhenModifyingAuthorizationWithInvalidPrincipalsThenValidationException()
			throws Exception {

		try {
			auth1 = add(authorizationInCollection(zeCollection).givingReadAccess().forPrincipalsIds("inexistentId1")
					.on(TAXO1_CATEGORY1));
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.NoSuchPrincipalWithUsername e) {
			//OK
		}

		try {
			List<String> roles = asList(READ);
			addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), "inexistentId2");
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.InvalidTargetRecordId e) {
			//OK
		}

		auth1 = add(authorizationForUser(alice).on(FOLDER4).givingReadAccess());

		//Cannot modify an authorization with an invalid principal id
		try {
			modify(authorizationOnRecord(auth1, FOLDER4).withNewPrincipalIds(asList("inexistentId3")));
			fail("Exception expected");
		} catch (NoSuchPrincipalWithUsername e) {
			//OK
		}
		try {
			modify(authorizationOnRecord(auth1, FOLDER4_1).withNewPrincipalIds(asList("inexistentId4")));
			fail("Exception expected");
		} catch (NoSuchPrincipalWithUsername e) {
			//OK
		}
		//Nothing changed
		assertThatAuth(auth1).hasPrincipals(alice);

	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotDetachConcept.class)
	public void whenTryToDetachConceptThenException()
			throws Exception {

		detach(TAXO1_CATEGORY2);
	}

	@Test
	public void whenDetachingASecurableRecordThenCustomAuthKeptAndRemovedAuthNotCopied()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(FOLDER4).givingReadAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER4).givingReadWriteDeleteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER4_1).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, FOLDER4_1).removingItOnRecord());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).removedOnRecords(FOLDER4_1).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(dakota)
		);

		detach(FOLDER4_1);
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4_1).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(dakota)
		);

		//Detaching it twice, nothing changes...
		detach(FOLDER4_1);
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4_1).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(dakota)
		);

		reset(FOLDER4_1);
		verifyRecord(FOLDER4_1).detachedAuthorizationFlag().isFalse();
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles)
		);

		//Resetting it twice, nothing changes
		reset(FOLDER4_1);
		verifyRecord(FOLDER4_1).detachedAuthorizationFlag().isFalse();
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles)
		);
	}

	@Test
	public void whenDetachingARootSecurableRecordThenCustomAuthKeptAndRemovedAuthNotCopied()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth3 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingReadWriteDeleteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER4).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).removedOnRecords(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(dakota)
		);

		detach(FOLDER4);
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(alice),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(dakota)
		);

		reset(FOLDER4);
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(alice),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(charles)
		);
	}

	@Test
	public void whenResettingASecurableRecordThenCustomAuthDeletedAndRemovedAuthReenabled()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(FOLDER4).givingReadAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER4).givingReadWriteDeleteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER4_1).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, FOLDER4_1).removingItOnRecord());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).removedOnRecords(FOLDER4_1).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(dakota)
		);

		reset(FOLDER4_1);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWriteDelete().forPrincipals(charles)
		);

		verifyRecord(FOLDER4_1).detachedAuthorizationFlag().isFalse();

	}

	@Test
	public void whenResettingARootSecurableRecordThenCustomAuthDeletedAndRemovedAuthReenabled()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth3 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingReadWriteDeleteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER4).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).removedOnRecords(FOLDER4).givingRead().forPrincipals(alice),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(charles),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(dakota)
		);

		reset(FOLDER4);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(alice),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWriteDelete().forPrincipals(charles)
		);

		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();

	}

	@Test
	public void givenDetachedRecordNoMatterWhatIsDoneOnItsAncestorsThenNotInheritedOnTheDetachedRecord()
			throws Exception {

		auth1 = add(authorizationForGroup(heroes).on(FOLDER4).givingReadAccess());
		auth2 = add(authorizationForGroup(legends).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForUser(bob).on(FOLDER4_1).givingReadAccess());

		Map<String, String> newAuths = detach(FOLDER4_1);
		verifyRecord(records.folder4().getId()).detachedAuthorizationFlag().isFalse();
		verifyRecord(records.folder4_1().getId()).detachedAuthorizationFlag().isTrue();
		forUser(charles).assertThatAllFoldersAndDocuments().contains(FOLDER4_1, FOLDER4_1_DOC1);

		modify(authorizationOnRecord(newAuths.get(auth1), FOLDER4_1).removingItOnRecord());

		verifyRecord(records.folder4().getId()).detachedAuthorizationFlag().isFalse();
		verifyRecord(records.folder4_1().getId()).detachedAuthorizationFlag().isTrue();
		forUser(charles).assertThatAllFoldersAndDocuments().doesNotContain(FOLDER4_1, FOLDER4_1_DOC1);

		//Even if we reset folder4, still no access on folder4_1
		reset(FOLDER4);
		forUser(charles).assertThatAllFoldersAndDocuments().doesNotContain(FOLDER4_1, FOLDER4_1_DOC1);

		//Even if we add an auth on folder4 or category2, still no access on folder4_1
		auth4 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingReadAccess());
		auth5 = add(authorizationForUser(charles).on(FOLDER4).givingReadAccess());
		forUser(charles).assertThatAllFoldersAndDocuments().doesNotContain(FOLDER4_1, FOLDER4_1_DOC1);

		//If you we reattach the record, it gains the auths
		reset(FOLDER4_1);
		forUser(charles).assertThatAllFoldersAndDocuments().contains(FOLDER4_1, FOLDER4_1_DOC1);
	}

	private void reset(String id) {
		services.reset(get(id));
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void whenModifyingAuthorizationPrincipalsThenCreateCopyIfInherited()
			throws Exception {

		auth1 = add(authorizationForUsers(alice, bob, charles).on(FOLDER4).givingReadAccess());

		assertThat(modify(authorizationOnRecord(auth1, FOLDER4_2).withNewPrincipalIds(alice, charles)))
				.is(creatingACopy()).isNot(deleted());

		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck, alice, bob, charles);
		verifyRecord(FOLDER4_1).usersWithReadAccess().containsOnly(chuck, alice, bob, charles);
		verifyRecord(FOLDER4_2).usersWithReadAccess().containsOnly(chuck, alice, charles);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).removedOnRecords(FOLDER4_2).givingRead().forPrincipals(alice, bob, charles),
				authOnRecord(FOLDER4_2).givingRead().forPrincipals(alice, charles)
		);

		assertThat(modify(authorizationOnRecord(auth1, FOLDER4).withNewPrincipalIds(alice, bob, dakota)))
				.isNot(creatingACopy()).isNot(deleted());

		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck, alice, bob, dakota);
		verifyRecord(FOLDER4_1).usersWithReadAccess().containsOnly(chuck, alice, bob, dakota);
		verifyRecord(FOLDER4_2).usersWithReadAccess().containsOnly(chuck, alice, charles);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER4).removedOnRecords(FOLDER4_2).givingRead().forPrincipals(alice, bob, dakota),
				authOnRecord(FOLDER4_2).givingRead().forPrincipals(alice, charles)
		);

	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddAuhtorizationInNonPrincipalTaxonomy.class)
	public void whenAddingAuthorizationOnAconceptOfASecondaryTaxonomyThenException()
			throws Exception {

		add(authorizationForGroups(heroes).on(TAXO2_STATION2_1).givingReadAccess());
	}

	@Test
	public void whenGetRecordsAuthorizationsThenObtainsAuthorizations()
			throws Exception {

		auth1 = add(authorizationForGroup(legends).on(FOLDER4).givingReadAccess());
		auth2 = add(authorizationForPrincipals(heroes, dakota).on(FOLDER2).givingReadWriteAccess());
		auth3 = add(authorizationForUser(dakota).on(TAXO1_CATEGORY1).givingReadAccess());

		List<Record> records = recordServices.getRecordsCaches().getCache(zeCollection).getAllValues(Authorization.SCHEMA_TYPE);
		assertThat(records)
				.hasSize(3);

		assertThatAuthorizationsOn(FOLDER2).containsOnly(
				authOnRecord(FOLDER2).givingReadWrite().forPrincipals(dakota, heroes),
				authOnRecord(TAXO1_CATEGORY1).givingRead().forPrincipals(dakota));

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends));

		assertThatAuthorizationsOn(TAXO1_CATEGORY1).containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRead().forPrincipals(dakota));

		assertThatAuthorizationsFor(legends).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends));

		assertThatAuthorizationsFor(heroes).containsOnly(
				authOnRecord(FOLDER2).givingReadWrite().forPrincipals(heroes, dakota));

		assertThatAuthorizationsFor(dakota).containsOnly(
				authOnRecord(FOLDER2).givingReadWrite().forPrincipals(heroes, dakota),
				authOnRecord(TAXO1_CATEGORY1).givingRead().forPrincipals(dakota));

		assertThatAuthorizationsFor(gandalf).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends),
				authOnRecord(FOLDER2).givingReadWrite().forPrincipals(heroes, dakota));

		assertThatAuthorizationsFor(edouard).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends));
	}

	@Test
	public void whenNewGroupsAreAssignedToAUserThenLostPreviousGroupAccessAndGainNewGroupAccess()
			throws Exception {

		List<String> roles = asList(READ);

		auth1 = add(authorizationForGroups(legends).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = add(authorizationForGroups(heroes).on(FOLDER2).givingReadAccess());
		auth3 = add(authorizationForUser(alice).on(FOLDER1).givingReadAccess());
		forUser(alice).assertThatRecordsWithReadAccess().containsOnly(
				TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER1, FOLDER1_DOC1, FOLDER3, FOLDER3_DOC1, FOLDER4, FOLDER4_1,
				FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1);

		givenUser(alice).isRemovedFromGroup(legends);

		forUser(alice).assertThatRecordsWithReadAccess().containsOnly(FOLDER1, FOLDER1_DOC1);

		givenUser(alice).isAddedInGroup(heroes);

		ConstellioFactories.getInstance().onRequestEnded();
		ConstellioFactories.getInstance().onRequestStarted();

		forUser(alice).assertThatRecordsWithReadAccess().containsOnly(
				FOLDER1, FOLDER1_DOC1, FOLDER2, FOLDER2_1, FOLDER2_2, FOLDER2_2_DOC1, FOLDER2_2_DOC2);
	}

	@Test
	public void givenAuthorizationsWithStartAndEndDateOnConceptThenOnlyActiveDuringSpecifiedTimerange()
			throws Exception {

		setTimeToCalling(date(2016, 4, 4));

		//A daily authorizaiton
		auth1 = add(authorizationForUser(aliceWonderland).on(TAXO1_FOND1_1)
				.startingOn(date(2016, 4, 5)).endingOn(date(2016, 4, 5)).givingReadWriteAccess());

		//A 4 day authorizaiton
		auth2 = add(authorizationForUser(bob).on(TAXO1_FOND1_1)
				.startingOn(date(2016, 4, 5)).endingOn(date(2016, 4, 8)).givingReadWriteAccess());

		//A future authorization
		auth3 = add(authorizationForUser(charles).on(TAXO1_FOND1_1)
				.startingOn(date(2016, 4, 7)).givingReadWriteAccess());

		//An authorization with an end
		auth4 = add(authorizationForUser(dakota).on(TAXO1_FOND1_1)
				.endingOn(date(2016, 4, 6)).givingReadWriteAccess());

		auth5 = add(authorizationForUser(edouard).on(TAXO1_FOND1_1).givingReadWriteAccess());

		//An authorization started in the past
		auth6 = add(authorizationForUser(gandalf).on(TAXO1_FOND1_1)
				.during(date(2016, 4, 3), date(2016, 4, 6)).givingReadWriteAccess());

		//An authorization already finished
		try {
			auth7 = add(authorizationForUser(sasquatch).on(TAXO1_FOND1_1)
					.during(date(2016, 4, 1), date(2016, 4, 3)).givingReadWriteAccess());
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.EndDateLessThanCurrentDate e) {
			//OK
		}

		services.refreshActivationForAllAuths(collectionsListManager.getCollections());

		setTimeToCalling(date(2016, 4, 4));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, gandalf);
		}

		setTimeToCalling(date(2016, 4, 5));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, alice, bob, gandalf);
		}

		setTimeToCalling(date(2016, 4, 6));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, bob, gandalf);
		}

		setTimeToCalling(date(2016, 4, 7));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, edouard, bob, charles);
		}

		setTimeToCalling(date(2016, 4, 8));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, charles, edouard, bob);
		}

		setTimeToCalling(date(2016, 4, 9));
		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, charles, edouard);
		}

		assertThatAllAuthorizationsIds().containsOnly(auth1, auth2, auth3, auth4, auth5, auth6);
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		assertThatAllAuthorizationsIds().containsOnly(auth3, auth5);

	}

	@Test
	public void givenAuthorizationsWithStartAndEndDateOnNonConceptRecordThenOnlyActiveDuringSpecifiedTimerange()
			throws Exception {

		setTimeToCalling(date(2016, 4, 4));

		//A daily authorizaiton
		auth1 = add(authorizationForUser(aliceWonderland).on(FOLDER4)
				.startingOn(date(2016, 4, 5)).endingOn(date(2016, 4, 5)).givingReadWriteAccess());

		//A 4 day authorizaiton
		auth2 = add(authorizationForUser(bob).on(FOLDER4)
				.startingOn(date(2016, 4, 5)).endingOn(date(2016, 4, 8)).givingReadWriteAccess());

		//A future authorization
		auth3 = add(authorizationForUser(charles).on(FOLDER4)
				.startingOn(date(2016, 4, 7)).givingReadWriteAccess());

		//An authorization with an end
		auth4 = add(authorizationForUser(dakota).on(FOLDER4)
				.endingOn(date(2016, 4, 6)).givingReadWriteAccess());

		auth5 = add(authorizationForUser(edouard).on(FOLDER4).givingReadWriteAccess());

		//An authorization started in the past
		auth6 = add(authorizationForUser(gandalf).on(FOLDER4)
				.during(date(2016, 4, 3), date(2016, 4, 6)).givingReadWriteAccess());

		//An authorization already finished
		try {
			auth7 = add(authorizationForUser(sasquatch).on(FOLDER4)
					.during(date(2016, 4, 1), date(2016, 4, 3)).givingReadWriteAccess());
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.EndDateLessThanCurrentDate e) {
			//OK
		}

		services.refreshActivationForAllAuths(collectionsListManager.getCollections());

		setTimeToCalling(date(2016, 4, 4));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, gandalf);
		}

		setTimeToCalling(date(2016, 4, 5));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, alice, bob, gandalf);
		}

		setTimeToCalling(date(2016, 4, 6));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, bob, gandalf);
		}

		setTimeToCalling(date(2016, 4, 7));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, edouard, bob, charles);
		}

		setTimeToCalling(date(2016, 4, 8));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, charles, edouard, bob);
		}

		setTimeToCalling(date(2016, 4, 9));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, charles, edouard);
		}

		assertThatAllAuthorizationsIds().containsOnly(auth1, auth2, auth3, auth4, auth5, auth6);
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		assertThatAllAuthorizationsIds().containsOnly(auth3, auth5);

	}

	@Test
	public void givenAuthorizationsWithStartAndEndDateOnFolderThenOnlyActiveDuringSpecifiedTimerange()
			throws Exception {

		setTimeToCalling(date(2016, 4, 4));

		//A daily authorizaiton
		auth1 = add(authorizationForUser(aliceWonderland).on(FOLDER4)
				.startingOn(date(2016, 4, 5)).endingOn(date(2016, 4, 5)).givingReadWriteAccess());

		//A 4 day authorizaiton
		auth2 = add(authorizationForUser(bob).on(FOLDER4)
				.startingOn(date(2016, 4, 5)).endingOn(date(2016, 4, 8)).givingReadWriteAccess());

		//A future authorization
		auth3 = add(authorizationForUser(charles).on(FOLDER4)
				.startingOn(date(2016, 4, 7)).givingReadWriteAccess());

		//An authorization with an end
		auth4 = add(authorizationForUser(dakota).on(FOLDER4)
				.endingOn(date(2016, 4, 6)).givingReadWriteAccess());

		auth5 = add(authorizationForUser(edouard).on(FOLDER4).givingReadWriteAccess());

		//An authorization started in the past
		auth6 = add(authorizationForUser(gandalf).on(FOLDER4)
				.during(date(2016, 4, 3), date(2016, 4, 6)).givingReadWriteAccess());

		//An authorization already finished
		try {
			auth7 = add(authorizationForUser(sasquatch).on(FOLDER4)
					.during(date(2016, 4, 1), date(2016, 4, 3)).givingReadWriteAccess());
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.EndDateLessThanCurrentDate e) {
			//OK
		}

		services.refreshActivationForAllAuths(collectionsListManager.getCollections());

		setTimeToCalling(date(2016, 4, 4));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, gandalf);
		}

		setTimeToCalling(date(2016, 4, 5));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, alice, bob, gandalf);
		}

		setTimeToCalling(date(2016, 4, 6));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, dakota, edouard, bob, gandalf);
		}

		setTimeToCalling(date(2016, 4, 7));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, edouard, bob, charles);
		}

		setTimeToCalling(date(2016, 4, 8));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, charles, edouard, bob);
		}

		setTimeToCalling(date(2016, 4, 9));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(chuck, charles, edouard);
		}

		assertThatAllAuthorizationsIds().containsOnly(auth1, auth2, auth3, auth4, auth5, auth6);
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		assertThatAllAuthorizationsIds().containsOnly(auth3, auth5);

	}

	@Test
	public void givenUserWithCollectionAccessThenHasAccessNoMatterTheRecordsAuthorizationAndHasNoRolePermissions()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(users.edouardIn(zeCollection).setCollectionReadAccess(true).setSystemAdmin(true));
		transaction.add(users.charlesIn(zeCollection).setCollectionReadAccess(true));
		transaction.add(users.dakotaIn(zeCollection).setCollectionWriteAccess(true));
		transaction.add(users.sasquatchIn(zeCollection).setCollectionWriteAccess(true).setCollectionDeleteAccess(true));
		transaction.add(users.aliceIn(zeCollection).setCollectionDeleteAccess(true));
		recordServices.execute(transaction);
		auth1 = add(authorizationForUser(charles).on(TAXO1_FOND1_1).givingReadWriteAccess());
		auth2 = add(authorizationForUser(dakota).on(TAXO1_FOND1_1).givingReadAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, alice, chuck, edouard, sasquatch);
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, chuck, sasquatch);
			verifyRecord.usersWithDeleteAccess().containsOnly(alice, chuck, sasquatch);
			verifyRecord.usersWithPermission("aPermission").containsOnly(admin, edouard);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_FOND1, TAXO1_CATEGORY2_1, FOLDER3)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, alice, chuck, edouard, sasquatch);
			verifyRecord.usersWithWriteAccess().containsOnly(dakota, chuck, sasquatch);
			verifyRecord.usersWithDeleteAccess().containsOnly(alice, chuck, sasquatch);
			verifyRecord.usersWithPermission("aPermission").containsOnly(admin, edouard);
		}

		assertThat(users.edouardIn(zeCollection).hasReadAccess().globally()).isTrue();
		assertThat(users.edouardIn(zeCollection).hasWriteAccess().globally()).isFalse();
		assertThat(users.edouardIn(zeCollection).hasDeleteAccess().globally()).isFalse();

		assertThat(users.dakotaIn(zeCollection).hasReadAccess().globally()).isTrue();
		assertThat(users.dakotaIn(zeCollection).hasWriteAccess().globally()).isTrue();
		assertThat(users.dakotaIn(zeCollection).hasDeleteAccess().globally()).isFalse();

		assertThat(users.aliceIn(zeCollection).hasReadAccess().globally()).isTrue();
		assertThat(users.aliceIn(zeCollection).hasWriteAccess().globally()).isFalse();
		assertThat(users.aliceIn(zeCollection).hasDeleteAccess().globally()).isTrue();

		assertThat(users.sasquatchIn(zeCollection).hasReadAccess().globally()).isTrue();
		assertThat(users.sasquatchIn(zeCollection).hasWriteAccess().globally()).isTrue();
		assertThat(users.sasquatchIn(zeCollection).hasDeleteAccess().globally()).isTrue();
	}

	//@Test
	public void givenGroupWithCollectionAccessThenHasAccessNoMatterTheRecordsAuthorizationAndHasNoRolePermissions()
			throws Exception {

		//Valider que ça ne donne pas un accès permission

		//Donner au groupe Heroes, une permission globale RW
		//Donner au groupe Legends, une permission globale RD
		//Donner au groupe Rumors, une permission globale RW
		//Donner à Charles (groupe Heroes) une permission globale R
		//Donner à Dakota (groupe Heroes) une permission globale RD
		//Donner à Alice (groupe Legends) aucune permission

		//Sur quelques records :

		//Valider que Charles (groupe Heroes) a RW
		//Valider que Dakota (groupe Heroes) a RWD
		//Valider que Gandalf (groupe Heroes et Legends) a RWD

		//Valider que Alice (Legends) a RD
		//Valider que Édouard (Legends) a RD
		//Valider que Sasquatch (Rumors) a RWD - toutes les permissions globales données dans Legends sont héritées par le sous-groupe rumors
		//Valider que Bob n'a rien (aucun groupe)

		//fail("todo");
	}

	@Test
	public void givenUserWithDeletePermissionOnRecordsThenCanOnlyDeleteRecordsIfHasPermissionOnWholeHierarchy()
			throws Exception {

		recordServices.logicallyDelete(records.folder2(), users.chuckNorrisIn(zeCollection));

		//Bob has no delete permission
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission the whole hierarchy
		auth1 = add(authorizationForUser(bob).on(TAXO1_FOND1).givingReadWriteDeleteAccess());
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on folder 4
		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on category 1
		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUser(bob).assertHasDeletePermissionOnHierarchyOf(FOLDER2).isFalse();

	}

	@Test
	public void givenUserWithDeletePermissionOnRecordsThenCanOnlyRestoreRecordsIfHasPermissionOnWholeHierarchy()
			throws Exception {

		recordServices.logicallyDelete(records.folder1(), users.chuckNorrisIn(zeCollection));
		recordServices.logicallyDelete(records.folder4(), users.chuckNorrisIn(zeCollection));
		recordServices.logicallyDelete(records.taxo1_fond1_1(), users.chuckNorrisIn(zeCollection));

		//Bob has no delete permission
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission the whole hierarchy
		auth1 = add(authorizationForUser(bob).on(TAXO1_FOND1).givingReadWriteDeleteAccess());
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on folder 4
		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isTrue();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

		//Bob has a delete permission on category 1
		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY1).removingItOnRecord());
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER4).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_FOND1_1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(TAXO1_CATEGORY1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER1).isFalse();
		forUser(bob).assertHasRestorePermissionOnHierarchyOf(FOLDER2).isFalse();

	}

	//@Test
	//Deprecated, remove services and this test
	public void givenUserWithDeletePermissionOnPrincipalConceptButNotOnSomeRecordsThenCanOnlyDeleteConceptIfExcludingRecords()
			throws Exception {

		//Bob has no delete permission
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO1_CATEGORY2).isFalse();
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptIncludingRecords(TAXO1_CATEGORY2).isFalse();

		//Bob has a delete permission the whole category2 hierarchy
		auth1 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteDeleteAccess());
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO1_CATEGORY2).isTrue();
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptIncludingRecords(TAXO1_CATEGORY2).isTrue();

		//Bob has a delete permission on category2, but not the whole hierarchy
		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO1_CATEGORY2).isTrue();
		forUser(bob).assertHasDeletePermissionOnPrincipalConceptIncludingRecords(TAXO1_CATEGORY2).isFalse();

		try {
			forUser(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(FOLDER1);
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.RecordIsNotAConceptOfPrincipalTaxonomy e) {
			//OK
		}

		try {
			forUser(bob).assertHasDeletePermissionOnPrincipalConceptExcludingRecords(TAXO2_STATION2);
			fail("Exception expected");
		} catch (AuthorizationsServicesRuntimeException.RecordIsNotAConceptOfPrincipalTaxonomy e) {
			//OK
		}

	}

	@Test
	public void whenGetConceptsForWhichUserHasPermissionThenReturnTheGoodConcepts()
			throws Exception {

		recordServices.update(users.dakotaIn(zeCollection).setUserRoles(asList(ROLE3)));
		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY1).giving(ROLE1));
		auth2 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth3 = add(authorizationForUser(sasquatch).on(TAXO1_FOND1_1).giving(ROLE2));

		forUser(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_NO_ROLE).isEmpty();
		forUser(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1).isEmpty();
		forUser(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1_AND_ROLE2).isEmpty();
		forUser(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE2).isEmpty();
		forUser(dakota).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE3).containsOnly(
				TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1);

		forUser(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_NO_ROLE).isEmpty();
		forUser(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1).containsOnly(
				TAXO1_CATEGORY1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1);
		forUser(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(
				TAXO1_CATEGORY1, TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, TAXO1_FOND1_1);
		forUser(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE2).containsOnly(
				TAXO1_FOND1_1, TAXO1_CATEGORY1);
		forUser(sasquatch).assertThatConceptsForWhichUserHas(PERMISSION_OF_ROLE3).isEmpty();

		for (String permission : asList(PERMISSION_OF_ROLE1, PERMISSION_OF_ROLE1_AND_ROLE2, PERMISSION_OF_ROLE2,
				PERMISSION_OF_ROLE3)) {
			forUserInAnotherCollection(sasquatch).assertThatConceptsForWhichUserHas(permission).isEmpty();
		}

	}

	@Test
	public void whenGetUsersWithGlobalPermissionThenReturnTheGoodUsers()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(users.sasquatchIn(zeCollection).setUserRoles(asList(ROLE1)));
		transaction.add(users.robinIn(zeCollection).setUserRoles(asList(ROLE2)));
		recordServices.execute(transaction);
		auth1 = add(authorizationForUser(robin).on(TAXO1_FOND1).giving(ROLE3));

		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_NO_ROLE).isEmpty();
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE1).containsOnly(sasquatch);
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(sasquatch, robin);
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE2).containsOnly(robin);
		assertThatUsersWithGlobalPermissionInZeCollection(PERMISSION_OF_ROLE3).isEmpty();
	}

	@Test
	public void whenGetUsersWithPermissionOnConceptThenReturnTheGoodUsers()
			throws Exception {

		recordServices.update(users.aliceIn(zeCollection).setUserRoles(asList(ROLE3)));
		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).giving(ROLE2));
		auth2 = add(authorizationForUser(robin).on(TAXO1_FOND1).giving(ROLE1));
		auth3 = add(authorizationForUser(gandalf).on(FOLDER1).giving(ROLE1));

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1)) {
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_NO_ROLE).containsOnly(admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1).containsOnly(robin, gandalf, admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(robin, gandalf, admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE3).containsOnly(alice, admin);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_FOND1, TAXO1_FOND1_1, TAXO1_CATEGORY1, FOLDER2_2_DOC1)) {
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_NO_ROLE).containsOnly(admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1).containsOnly(robin, admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(robin, admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE2).containsOnly(admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE3).containsOnly(alice, admin);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3_DOC1, FOLDER4_1)) {
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_NO_ROLE).containsOnly(admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1).containsOnly(robin, admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE1_AND_ROLE2).containsOnly(robin, sasquatch, admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE2).containsOnly(sasquatch, admin);
			verifyRecord.assertThatUsersWithPermission(PERMISSION_OF_ROLE3).containsOnly(alice, admin);
		}

	}


	@Test
	public void givenInheritedAuthorizationFromConceptsAndCollectionWhenCheckingPermissionsExcludingInheritanceThenExcluded()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2_1).giving(ROLE2));
		auth3 = add(authorizationForUser(charles).on(FOLDER3).giving(ROLE3));
		recordServices.update(users.dakotaLIndienIn(zeCollection).setUserRoles(asList(ROLE1, ROLE2, ROLE3)));

		/*****
		 * Using default behavior (including, global and inherited)
		 */


		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).on(records.taxo1_category2())).isTrue();
		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).on(records.taxo1_category2_1())).isTrue();
		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).on(records.folder3())).isTrue();
		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).onSomething()).isTrue();

		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).on(records.taxo1_category2())).isFalse();
		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).on(records.taxo1_category2_1())).isTrue();
		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).on(records.folder3())).isTrue();
		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).onSomething()).isTrue();

		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).on(records.taxo1_category2())).isFalse();
		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).on(records.taxo1_category2_1())).isFalse();
		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).on(records.folder3())).isTrue();
		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).onSomething()).isTrue();

		for (String permission : asList(PERMISSION_OF_ROLE1, PERMISSION_OF_ROLE2, PERMISSION_OF_ROLE3)) {
			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).on(records.taxo1_category2())).isTrue();
			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).on(records.taxo1_category2_1())).isTrue();
			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).on(records.folder3())).isTrue();
			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).onSomething()).isTrue();
		}

		/*****
		 * Excluding global
		 */

		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).specificallyOn(records.taxo1_category2())).isTrue();
		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).specificallyOn(records.taxo1_category2_1())).isFalse();
		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).specificallyOn(records.folder3())).isFalse();
		//assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).onSomething()).isTrue();

		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).specificallyOn(records.taxo1_category2())).isFalse();
		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).specificallyOn(records.taxo1_category2_1())).isTrue();
		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).specificallyOn(records.folder3())).isFalse();
		//assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).setIncludeGlobalAccess(false).onSomething()).isTrue();

		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).specificallyOn(records.taxo1_category2())).isFalse();
		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).specificallyOn(records.taxo1_category2_1())).isFalse();
		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).specificallyOn(records.folder3())).isTrue();
		//assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).setIncludeGlobalAccess(false).onSomething()).isTrue();

		for (String permission : asList(PERMISSION_OF_ROLE1, PERMISSION_OF_ROLE2, PERMISSION_OF_ROLE3)) {
			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).specificallyOn(records.taxo1_category2())).isFalse();
			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).specificallyOn(records.taxo1_category2_1())).isFalse();
			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).specificallyOn(records.folder3())).isFalse();
			//assertThat(users.dakotaLIndienIn(zeCollection).has(permission).specificallyOn().onSomething()).isTrue();
		}

		//		/*****
		//		 * Excluding inherited
		//		 */
		//
		//		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).setIncludeAccessFromTargetInheritance(false).on(records.taxo1_category2())).isTrue();
		//		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).setIncludeAccessFromTargetInheritance(false).on(records.taxo1_category2_1())).isTrue();
		//		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).setIncludeAccessFromTargetInheritance(false).on(records.folder3())).isTrue();
		//		assertThat(users.aliceIn(zeCollection).has(PERMISSION_OF_ROLE1).setIncludeAccessFromTargetInheritance(false).onSomething()).isTrue();
		//
		//		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).setIncludeAccessFromTargetInheritance(false).on(records.taxo1_category2())).isFalse();
		//		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).setIncludeAccessFromTargetInheritance(false).on(records.taxo1_category2_1())).isTrue();
		//		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).setIncludeAccessFromTargetInheritance(false).on(records.folder3())).isTrue();
		//		assertThat(users.bobIn(zeCollection).has(PERMISSION_OF_ROLE2).setIncludeAccessFromTargetInheritance(false).onSomething()).isTrue();
		//
		//		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).setIncludeAccessFromTargetInheritance(false).on(records.taxo1_category2())).isFalse();
		//		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).setIncludeAccessFromTargetInheritance(false).on(records.taxo1_category2_1())).isFalse();
		//		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).setIncludeAccessFromTargetInheritance(false).on(records.folder3())).isTrue();
		//		assertThat(users.charlesIn(zeCollection).has(PERMISSION_OF_ROLE3).setIncludeAccessFromTargetInheritance(false).onSomething()).isTrue();
		//
		//		for (String permission : asList(PERMISSION_OF_ROLE1, PERMISSION_OF_ROLE2, PERMISSION_OF_ROLE3)) {
		//			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).setIncludeAccessFromTargetInheritance(false).on(records.taxo1_category2())).isTrue();
		//			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).setIncludeAccessFromTargetInheritance(false).on(records.taxo1_category2_1())).isTrue();
		//			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).setIncludeAccessFromTargetInheritance(false).on(records.folder3())).isTrue();
		//			assertThat(users.dakotaLIndienIn(zeCollection).has(permission).setIncludeAccessFromTargetInheritance(false).onSomething()).isTrue();
		//		}

	}

	@Test
	public void whenGetUsersWithPermissionOnConceptExcludingInheritedAuthorizationsThenReturnTheGoodUsers()
			throws Exception {

		recordServices.update(users.aliceIn(zeCollection).setUserRoles(asList(ROLE3)));
		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).giving(ROLE2));
		auth2 = add(authorizationForUser(robin).on(TAXO1_FOND1).giving(ROLE1));
		auth3 = add(authorizationForUser(gandalf).on(FOLDER1).giving(ROLE1));

		for (RecordVerifier verifyRecord : $(TAXO1_FOND1)) {
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1)
					.containsOnly(robin);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1_AND_ROLE2)
					.containsOnly(robin);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE2).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE3).isEmpty();
		}

		for (RecordVerifier verifyRecord : $(FOLDER1)) {
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1)
					.containsOnly(gandalf);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1_AND_ROLE2)
					.containsOnly(gandalf);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE2).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE3).isEmpty();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2)) {
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1_AND_ROLE2)
					.containsOnly(sasquatch);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE2)
					.containsOnly(sasquatch);
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE3).isEmpty();
		}

		for (RecordVerifier verifyRecord : $(FOLDER2, FOLDER1_DOC1, TAXO1_FOND1_1, TAXO1_CATEGORY1, TAXO1_CATEGORY2_1,
				FOLDER2_2_DOC1)) {
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_NO_ROLE).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1).isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1_AND_ROLE2)
					.isEmpty();
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE3).isEmpty();
		}

	}

	@Test
	public void givenUserHasAuthorizationsWithRolesThenHasValidPermissionsOnRecords()
			throws Exception {

		roleManager.addRole(new Role(zeCollection, "roleA", "First role", asList("operation1", "operation2")));
		roleManager.addRole(new Role(zeCollection, "roleB", "Second role", asList("operation3", "operation4")));
		roleManager.addRole(new Role(zeCollection, "roleC", "Third role", asList("operation5", "operation6")));

		recordServices.update(users.sasquatchIn(zeCollection).setUserRoles(asList("roleA")));

		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY1).giving("roleB"));

		Record folder1Inside = records.folder1();
		Record folder2Inside = records.folder2();
		Record folder3Outside = records.folder3();
		Record folder4Outside = records.folder4();

		User sasquatchInZeCollection = users.sasquatchIn(zeCollection);
		assertThat(sasquatchInZeCollection.has("operation1").onSomething()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onSomething()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation5").onSomething()).isFalse();

		assertThat(sasquatchInZeCollection.has("operation1").on(folder1Inside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation1").on(folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").on(folder1Inside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").on(folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.has("operation5").on(folder1Inside)).isFalse();
		assertThat(sasquatchInZeCollection.has("operation5").on(folder3Outside)).isFalse();

		assertThat(sasquatchInZeCollection.has("operation1").onAll(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onAll(folder1Inside, folder2Inside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onAll(folder1Inside, folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.has("operation5").onAll(folder1Inside, folder2Inside)).isFalse();

		assertThat(sasquatchInZeCollection.has("operation1").onAny(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onAny(folder1Inside, folder2Inside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onAny(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onAny(folder3Outside, folder4Outside)).isFalse();
		assertThat(sasquatchInZeCollection.has("operation5").onAny(folder1Inside, folder3Outside)).isFalse();

		assertThat(sasquatchInZeCollection.hasAny("operation1", "operation3").on(folder1Inside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAny("operation1", "operation3").on(folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAny("operation3", "operation4").on(folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAny("operation3", "operation4").onAny(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAny("operation1", "operation3").onAll(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAny("operation3", "operation4").onAll(folder1Inside, folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAny("operation5", "operation6").onAny(folder1Inside, folder3Outside)).isFalse();

		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation3").on(folder1Inside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation2").on(folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation3").on(folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAll("operation3", "operation4").on(folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAll("operation3", "operation4").onAny(folder1Inside, folder3Outside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation3", "operation6").onAny(folder1Inside, folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAll("operation3", "operation4").onAll(folder1Inside, folder2Inside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation4").onAll(folder1Inside, folder2Inside)).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation3", "operation4").onAll(folder1Inside, folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation4").onAll(folder1Inside, folder3Outside)).isFalse();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation2").onAll(folder1Inside, folder3Outside)).isTrue();

		assertThat(users.sasquatchIn(anotherCollection).hasAny("operation1", "operation2", "operation3", "operation4",
				"operation5", "operation6", "otherPermission").globally()).isFalse();

		assertThat(userServices.has(sasquatch).globalPermissionInAnyCollection("operation1")).isTrue();
		assertThat(userServices.has(sasquatch).globalPermissionInAnyCollection("operation3")).isFalse();

	}

	@Test
	public void whenModifyingMultipleFieldsAtOnceOnAnAuthorizationOfARecordThenAllApplied()
			throws Exception {

		setTimeToCalling(date(2012, 10, 1));
		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(sasquatch));
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(sasquatch, chuck);

		modify(modifyAuthorizationOnRecord(auth1, records.taxo1_category2())
				.withNewStartDate(date(2012, 10, 2))
				.withNewPrincipalIds(users.bobIn(zeCollection).getId())
		);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(bob).startingOn(date(2012, 10, 2)));
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(chuck);

		setTimeToCalling(date(2012, 10, 2));
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();

		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);

	}

	@Test
	public void whenModifyingMultipleFieldsAtOnceOnAnAuthorizationInheritedByARecordThenAllApplied()
			throws Exception {

		setTimeToCalling(date(2012, 10, 1));
		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(sasquatch));
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(sasquatch, chuck);
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(sasquatch, chuck);

		modify(modifyAuthorizationOnRecord(auth1, records.folder4())
				.withNewStartDate(date(2012, 10, 2))
				.withNewPrincipalIds(users.bobIn(zeCollection).getId())
		);

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).removedOnRecords(FOLDER4).givingReadWrite().forPrincipals(sasquatch),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(bob).startingOn(date(2012, 10, 2)));
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(chuck, sasquatch);
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		setTimeToCalling(date(2012, 10, 2));
		services.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();

		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(chuck, sasquatch);
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck, bob);
	}


	@Test
	public void whenDeleteAuthorizationThenDeletedFromEveryRecords()
			throws Exception {

		detach(FOLDER4);
		auth1 = addWithoutUser(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addWithoutUser(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = addWithoutUser(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());

		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(bob, charles, chuck);

		services.execute(authorizationDeleteRequest(auth2, zeCollection)
				.setReattachIfLastAuthDeleted(false)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(charles));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(charles, chuck);

		services.execute(authorizationDeleteRequest(auth3, zeCollection)
				.setReattachIfLastAuthDeleted(false)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(alice));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		services.execute(authorizationDeleteRequest(auth1, zeCollection)
				.setReattachIfLastAuthDeleted(false)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().isEmpty();
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		recordServices.flush();

		assertThatRecords(schemas.searchEvents(ALL)).extractingMetadatas(RECORD_ID, PERMISSION_USERS, TYPE, USERNAME)
				.containsOnly(
						tuple("folder4", "Bob 'Elvis' Gratton", "delete_permission_folder", "chuck"),
						tuple("taxo1_category2", "Alice Wonderland", "delete_permission_category", "chuck"),
						tuple("folder4", "Charles-François Xavier", "delete_permission_folder", "chuck")
				);
	}

	@Test
	public void whenDeleteMoreThan1000AuthorizationsThenDeletedFromEveryRecords()
			throws Exception {

		reset(FOLDER4);
		detach(FOLDER4);
		FolderSchema schema = setup.folderSchema;

		List<Record> folder4SubFolders = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			Record record = new TestRecord(schema);
			record.set(schema.title(), aString());
			record.set(schema.parent(), FOLDER4);

			try {
				getModelLayerFactory().newRecordServices().add(record);
				folder4SubFolders.add(record);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}

		auth1 = addWithoutUser(authorizationForUser(bob).on(FOLDER4).givingReadWriteDeleteAccess());
		auth2 = addWithoutUser(authorizationForUser(charles).on(FOLDER4).givingReadAccess());

		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(bob, charles, chuck);
		verifyRecord(FOLDER4).usersWithWriteAccess().containsOnly(bob, chuck);
		verifyRecord(FOLDER4).usersWithDeleteAccess().containsOnly(bob, chuck);

		services.execute(authorizationDeleteRequest(auth1, zeCollection)
				.setReattachIfLastAuthDeleted(false)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().containsOnly(authOnRecord(FOLDER4).givingRead().forPrincipals(charles));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
		verifyRecord(FOLDER4).usersWithReadAccess().doesNotContain(bob);

		recordServices.flush();

		assertThatRecords(schemas.searchEvents(ALL)).extractingMetadatas(RECORD_ID, PERMISSION_USERS, TYPE, USERNAME)
				.containsOnly(tuple("folder4", "Bob 'Elvis' Gratton", "delete_permission_folder", "chuck"));

		checkIfChuckNorrisHasAccessToEverythingInZeCollection = false;
	}

	@Test
	public void whenDeleteLastAuthorizationOfAttachedRecordThenNoAuthorization()
			throws Exception {

		auth1 = addWithoutUser(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addWithoutUser(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = addWithoutUser(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, FOLDER4).removingItOnRecord());

		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(bob, charles, chuck);

		services.execute(authorizationDeleteRequest(auth2, zeCollection)
				.setReattachIfLastAuthDeleted(true)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).removedOnRecords(FOLDER4).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(charles));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(charles, chuck);

		services.execute(authorizationDeleteRequest(auth3, zeCollection)
				.setReattachIfLastAuthDeleted(true)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).removedOnRecords(FOLDER4).givingReadWrite().forPrincipals(alice));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		services.execute(authorizationDeleteRequest(auth1, zeCollection)
				.setReattachIfLastAuthDeleted(true)
				.setExecutedBy(users.chuckNorrisIn(zeCollection)));

		assertThatAllAuthorizations().isEmpty();
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		recordServices.flush();

		assertThatRecords(schemas.searchEvents(ALL)).extractingMetadatas(RECORD_ID, PERMISSION_USERS, TYPE, USERNAME)
				.containsOnly(
						tuple("folder4", "Bob 'Elvis' Gratton", "delete_permission_folder", "chuck"),
						tuple("taxo1_category2", "Alice Wonderland", "delete_permission_category", "chuck"),
						tuple("folder4", "Charles-François Xavier", "delete_permission_folder", "chuck")
				);
	}

	@Test
	public void whenDeleteLastAuthorizationOfDetachedRecordThenReattachDependingOnOption()
			throws Exception {

		detach(FOLDER4);
		auth1 = addWithoutUser(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addWithoutUser(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = addWithoutUser(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());

		assertThatRecords(schemas.searchEvents(ALL)).isEmpty();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(bob, charles, chuck);

		services.execute(authorizationDeleteRequest(auth2, zeCollection).setReattachIfLastAuthDeleted(true));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(charles));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(charles, chuck);

		services.execute(authorizationDeleteRequest(auth3, zeCollection).setReattachIfLastAuthDeleted(true));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(alice));
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(alice, chuck);

		services.execute(authorizationDeleteRequest(auth1, zeCollection).setReattachIfLastAuthDeleted(true));

		assertThatAllAuthorizations().isEmpty();
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isFalse();
		verifyRecord(FOLDER4).usersWithReadAccess().containsOnly(chuck);

		recordServices.flush();

		assertThatRecords(schemas.searchEvents(ALL)).isEmpty();
	}

	@Test
	public void whenCreatingAndModifyingAuthWithoutUserThenNoEventCreated()
			throws Exception {

		auth1 = addWithoutUser(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addWithoutUser(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = addWithoutUser(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewPrincipalIds(users.dakotaIn(zeCollection).getId()));
		modify(authorizationOnRecord(auth1, FOLDER4).withNewPrincipalIds(users.edouardLechatIn(zeCollection).getId()));

		assertThatRecords(schemas.searchEvents(ALL)).isEmpty();
	}

	@Test
	public void whenCreatingAndModifyingAuthWithUserThenNoEventCreated()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());

		modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewPrincipalIds(users.dakotaIn(zeCollection).getId())
				.setExecutedBy(users.gandalfIn(zeCollection)));
		modify(authorizationOnRecord(auth1, FOLDER4).withNewPrincipalIds(users.edouardLechatIn(zeCollection).getId())
				.setExecutedBy(users.gandalfIn(zeCollection)));
		modify(authorizationOnRecord(auth2, FOLDER4).withNewPrincipalIds(users.edouardLechatIn(zeCollection).getId())
				.setExecutedBy(users.gandalfIn(zeCollection)));

		assertThatRecords(schemas.searchEvents(ALL)).extractingMetadatas(RECORD_ID, PERMISSION_USERS, TYPE, USERNAME)
				.containsOnly(
						tuple("folder4", "Bob 'Elvis' Gratton", "grant_permission_folder", "dakota"),
						tuple("taxo1_category2", "Alice Wonderland", "grant_permission_category", "dakota"),
						tuple("folder4", "Charles-François Xavier", "grant_permission_folder", "dakota"),

						tuple("folder4", "Dakota L'Indien", "modify_permission_folder", "gandalf"),
						tuple("folder4", "Bob 'Elvis' Gratton", "modify_permission_folder", "gandalf"),
						tuple("taxo1_category2", "Alice Wonderland", "modify_permission_category", "gandalf")

				);

		Event event = schemas.searchEvents(where(schemas.eventType()).isEqualTo("modify_permission_category")).get(0);
		assertThat(event.getDelta().replace("\n", "")).isEqualTo("Utilisateurs :-[Dakota L'Indien]+[Alice Wonderland]");
	}

	@Test
	public void whenAConceptIsAssignedToANewParentThenTokensUpdated()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2_1).givingReadWriteAccess());

		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(FOLDER2).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(TAXO1_CATEGORY2_1).usersWithReadAccess().containsOnly(bob, charles, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(bob, charles, chuck);

		recordServices.update(records.taxo1_category2_1().set(setup.category.parentOfCategory(), TAXO1_CATEGORY1));
		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(FOLDER2).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(TAXO1_CATEGORY2_1).usersWithReadAccess().containsOnly(alice, charles, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(alice, charles, chuck);

		recordServices.update(records.taxo1_category2_1().set(setup.category.parentOfCategory(), null));
		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(FOLDER2).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(TAXO1_CATEGORY2_1).usersWithReadAccess().containsOnly(charles, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(charles, chuck);
	}

	@Test
	public void whenARecordIsAssignedToANewConceptThenTokensUpdated()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER3).givingReadWriteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER4).givingReadWriteAccess());

		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(FOLDER2).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(bob, charles, chuck);
		verifyRecord(FOLDER3_DOC1).usersWithReadAccess().containsOnly(bob, charles, chuck);

		recordServices.update(records.folder3().set(setup.folderSchema.taxonomy1(), TAXO1_CATEGORY1));
		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(alice, charles, chuck);
		verifyRecord(FOLDER3_DOC1).usersWithReadAccess().containsOnly(alice, charles, chuck);

		recordServices.update(records.folder3()
				.set(setup.folderSchema.taxonomy1(), null)
				.set(setup.folderSchema.parent(), FOLDER4));
		verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().containsOnly(alice, chuck);
		verifyRecord(TAXO1_CATEGORY2).usersWithReadAccess().containsOnly(bob, chuck);
		verifyRecord(FOLDER3).usersWithReadAccess().containsOnly(bob, charles, dakota, chuck);
		verifyRecord(FOLDER3_DOC1).usersWithReadAccess().containsOnly(bob, charles, dakota, chuck);
	}

	@Test
	@SlowTest
	public void givenAGroupHasALotOfUsersThenNoBAtchProcessRequired()
			throws Exception {

		createDummyUsersInLegendsGroup(100);
		getModelLayerFactory().getBatchProcessesController().close();
		try {
			auth1 = services.add(authorizationForGroup(legends).on(TAXO1_CATEGORY1).givingReadWriteAccess());
			verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().hasSize(105);

			services.execute(authorizationDeleteRequest(auth1, zeCollection));
			verifyRecord(TAXO1_CATEGORY1).usersWithReadAccess().hasSize(1);

		} finally {
			getModelLayerFactory().getBatchProcessesController().initialize();
		}
	}

	@Test
	public void givenAccessAuthForAUserWhenTheUserIsDisabledThenHeStillHasTheAuthButIsNoLongerReceivingItsBenefits()
			throws Exception {

		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		auth2 = add(authorizationForUser(sasquatch).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForUser(sasquatch).on(FOLDER3).givingReadDeleteAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().contains(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().contains(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().contains(sasquatch);
		}

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingReadWrite().forPrincipals(sasquatch),
				authOnRecord(FOLDER4).givingRead().forPrincipals(sasquatch),
				authOnRecord(FOLDER3).givingReadDelete().forPrincipals(sasquatch)
		);

		userServices.addUpdateUserCredential(users.sasquatch().setStatus(UserCredentialStatus.SUSPENDED));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingReadWrite().forPrincipals(sasquatch),
				authOnRecord(FOLDER4).givingRead().forPrincipals(sasquatch),
				authOnRecord(FOLDER3).givingReadDelete().forPrincipals(sasquatch)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().doesNotContain(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().doesNotContain(sasquatch);
		}

	}

	@Test
	public void givenARoleAuthForAUserWhenTheUserIsDisabledThenHeStillHasTheAuthButIsNoLongerReceivingItsBenefits()
			throws Exception {

		auth1 = add(authorizationForUser(sasquatch).on(TAXO1_CATEGORY1).giving(ROLE1));
		auth2 = add(authorizationForUser(sasquatch).on(FOLDER4).giving(ROLE1));
		auth2 = add(authorizationForUser(sasquatch).on(FOLDER3).giving(ROLE2));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).contains(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).contains(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).contains(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch);
		}

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(sasquatch),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(sasquatch),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(sasquatch)
		);

		userServices.addUpdateUserCredential(users.sasquatch().setStatus(UserCredentialStatus.SUSPENDED));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(sasquatch),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(sasquatch),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(sasquatch)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).doesNotContain(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).doesNotContain(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).doesNotContain(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch);
		}

	}

	@Test
	public void givenAccessAuthForAGroupWhenTheGroupIsDisabledThenUsersStillHasTheAuthsButTheyNoLongerReceivingItsBenefits()
			throws Exception {

		auth1 = add(authorizationForGroup(legends).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		auth2 = add(authorizationForGroup(legends).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForGroup(legends).on(FOLDER3).givingReadDeleteAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().contains(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().contains(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().contains(edouard, sasquatch);
		}

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingReadWrite().forPrincipals(legends),
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends),
				authOnRecord(FOLDER3).givingReadDelete().forPrincipals(legends)
		);

		userServices.addUpdateGlobalGroup(users.legends().setStatus(GlobalGroupStatus.INACTIVE));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingReadWrite().forPrincipals(legends),
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends),
				authOnRecord(FOLDER3).givingReadDelete().forPrincipals(legends)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().doesNotContain(edouard, sasquatch);
		}

		reenableLegends();
		userServices.addUpdateGlobalGroup(users.rumors().setStatus(GlobalGroupStatus.INACTIVE));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(sasquatch).contains(edouard);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().doesNotContain(sasquatch).contains(edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().doesNotContain(sasquatch).contains(edouard);
		}

	}

	@Test
	public void givenARoleAuthForAGroupWhenTheGroupIsDisabledThenUsersStillHasTheAuthsButTheyNoLongerReceivingItsBenefits()
			throws Exception {

		auth1 = add(authorizationForGroup(legends).on(TAXO1_CATEGORY1).giving(ROLE1));
		auth2 = add(authorizationForGroup(legends).on(FOLDER4).giving(ROLE1));
		auth3 = add(authorizationForGroup(legends).on(FOLDER3).giving(ROLE2));

		assertThat(schemas.getAllUsersInGroup(users.legendsIn(zeCollection), true, true))
				.extracting("username").contains("edouard", "sasquatch");

		assertThat(schemas.getAllUsersInGroup(users.rumorsIn(zeCollection), true, true))
				.extracting("username").contains("sasquatch").doesNotContain("edouard");

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).contains(edouard, sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(edouard, sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard, sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).contains(edouard, sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).contains(edouard, sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard, sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithRole(ROLE1).contains(edouard, sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(edouard, sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard, sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard, sasquatch);
		}

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(legends),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(legends),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(legends)
		);

		userServices.addUpdateGlobalGroup(users.legends().setStatus(GlobalGroupStatus.INACTIVE));

		assertThat(schemas.getAllUsersInGroup(users.legendsIn(zeCollection), true, true))
				.extracting("username").doesNotContain("edouard", "sasquatch");

		assertThat(schemas.getAllUsersInGroup(users.rumorsIn(zeCollection), true, true))
				.extracting("username").doesNotContain("sasquatch", "edouard");

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(legends),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(legends),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(legends)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).doesNotContain(edouard, sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(edouard, sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(edouard, sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).doesNotContain(edouard, sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).doesNotContain(edouard, sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(edouard, sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(edouard, sasquatch);
		}

		reenableLegends();
		userServices.addUpdateGlobalGroup(users.rumors().setStatus(GlobalGroupStatus.INACTIVE));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).describedAs(verifyRecord.recordId).contains(edouard).doesNotContain(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(edouard).doesNotContain(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard).doesNotContain(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard).doesNotContain(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).contains(edouard).doesNotContain(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).contains(edouard).doesNotContain(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard).doesNotContain(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard).doesNotContain(sasquatch);
		}

	}

	private void reenableLegends() {

		userServices.addUpdateGlobalGroup(users.legends().setStatus(GlobalGroupStatus.ACTIVE));
		userServices.addUpdateUserCredential(users.edouardLechat().addGlobalGroup("legends"));
		userServices.addUpdateUserCredential(users.alice().addGlobalGroup("legends"));
		userServices.addUpdateUserCredential(users.gandalfLeblanc().addGlobalGroup("legends"));
	}

	@Test
	public void givenAccessAuthForASubGroupWhenTheGroupIsDisabledThenUsersStillHasTheAuthsButTheyNoLongerReceivingItsBenefits()
			throws Exception {

		auth1 = add(authorizationForGroup(rumors).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		auth2 = add(authorizationForGroup(rumors).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForGroup(rumors).on(FOLDER3).givingReadDeleteAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().contains(sasquatch).doesNotContain(edouard);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().contains(sasquatch).doesNotContain(edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().contains(sasquatch).doesNotContain(edouard);
		}

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingReadWrite().forPrincipals(rumors),
				authOnRecord(FOLDER4).givingRead().forPrincipals(rumors),
				authOnRecord(FOLDER3).givingReadDelete().forPrincipals(rumors)
		);

		userServices.addUpdateGlobalGroup(users.legends().setStatus(GlobalGroupStatus.INACTIVE));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingReadWrite().forPrincipals(rumors),
				authOnRecord(FOLDER4).givingRead().forPrincipals(rumors),
				authOnRecord(FOLDER3).givingReadDelete().forPrincipals(rumors)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().doesNotContain(edouard, sasquatch);
		}

		reenableLegends();
		userServices.addUpdateGlobalGroup(users.rumors().setStatus(GlobalGroupStatus.INACTIVE));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().doesNotContain(edouard, sasquatch);
		}
	}

	@Test
	public void givenARoleAuthForASubGroupWhenTheGroupIsDisabledThenUsersStillHasTheAuthsButTheyNoLongerReceivingItsBenefits()
			throws Exception {

		auth1 = add(authorizationForGroup(rumors).on(TAXO1_CATEGORY1).giving(ROLE1));
		auth2 = add(authorizationForGroup(rumors).on(FOLDER4).giving(ROLE1));
		auth3 = add(authorizationForGroup(rumors).on(FOLDER3).giving(ROLE2));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).contains(sasquatch).doesNotContain(edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(sasquatch).doesNotContain(edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch).doesNotContain(edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch).doesNotContain(edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).contains(sasquatch).doesNotContain(edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).contains(sasquatch).doesNotContain(edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch).doesNotContain(edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch).doesNotContain(edouard);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithRole(ROLE1).contains(sasquatch).doesNotContain(edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(sasquatch).doesNotContain(edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch).doesNotContain(edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch).doesNotContain(edouard);
		}

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(rumors)
		);

		userServices.addUpdateGlobalGroup(users.legends().setStatus(GlobalGroupStatus.INACTIVE));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(rumors)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).doesNotContain(sasquatch, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).doesNotContain(sasquatch, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
		}

		reenableLegends();
		userServices.addUpdateGlobalGroup(users.rumors().setStatus(GlobalGroupStatus.INACTIVE));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(rumors)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).doesNotContain(sasquatch, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).doesNotContain(sasquatch, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
		}
	}

	@Test
	public void givenGroupInheritanceFromChildrenAndAccessAuthForAGroupWhenTheGroupIsDisabledThenUsersStillHasTheAuthsButTheyNoLongerReceivingItsBenefits()
			throws Exception {

		givenConfig(GROUP_AUTHORIZATIONS_INHERITANCE, FROM_CHILD_TO_PARENT);

		auth1 = add(authorizationForGroup(legends).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		auth2 = add(authorizationForGroup(legends).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForGroup(legends).on(FOLDER3).givingReadDeleteAccess());

		LogicalSearchQuery query = new LogicalSearchQuery(from(setup.folderSchema.instance()).where(IDENTIFIER).isEqualTo("folder4"));
		query.filteredWithUser(users.aliceIn(zeCollection));

		assertThat(searchServices.hasResults(query)).isTrue();

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().contains(edouard).doesNotContain(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().contains(edouard).doesNotContain(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().contains(edouard).doesNotContain(sasquatch);
		}

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingReadWrite().forPrincipals(legends),
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends),
				authOnRecord(FOLDER3).givingReadDelete().forPrincipals(legends)
		);

		userServices.addUpdateGlobalGroup(users.legends().setStatus(GlobalGroupStatus.INACTIVE));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingReadWrite().forPrincipals(legends),
				authOnRecord(FOLDER4).givingRead().forPrincipals(legends),
				authOnRecord(FOLDER3).givingReadDelete().forPrincipals(legends)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().doesNotContain(edouard, sasquatch);
		}

		reenableLegends();
		userServices.addUpdateGlobalGroup(users.rumors().setStatus(GlobalGroupStatus.INACTIVE));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(sasquatch).contains(edouard);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().doesNotContain(sasquatch).contains(edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().doesNotContain(sasquatch).contains(edouard);
		}

	}

	@Test
	public void givenGroupInheritanceFromChildrenAndARoleAuthForAGroupWhenTheGroupIsDisabledThenUsersStillHasTheAuthsButTheyNoLongerReceivingItsBenefits()
			throws Exception {

		givenConfig(GROUP_AUTHORIZATIONS_INHERITANCE, FROM_CHILD_TO_PARENT);

		auth1 = add(authorizationForGroup(legends).on(TAXO1_CATEGORY1).giving(ROLE1));
		auth2 = add(authorizationForGroup(legends).on(FOLDER4).giving(ROLE1));
		auth3 = add(authorizationForGroup(legends).on(FOLDER3).giving(ROLE2));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).contains(edouard).doesNotContain(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(edouard).doesNotContain(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard).doesNotContain(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard).doesNotContain(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).contains(edouard).doesNotContain(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).contains(edouard).doesNotContain(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard).doesNotContain(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard).doesNotContain(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithRole(ROLE1).contains(edouard).doesNotContain(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(edouard).doesNotContain(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard).doesNotContain(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(edouard).doesNotContain(sasquatch);
		}

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(legends),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(legends),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(legends)
		);

		userServices.addUpdateGlobalGroup(users.legends().setStatus(GlobalGroupStatus.INACTIVE));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(legends),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(legends),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(legends)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).doesNotContain(edouard, sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(edouard, sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(edouard, sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).doesNotContain(edouard, sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).doesNotContain(edouard, sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(edouard, sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(edouard, sasquatch);
		}

		reenableLegends();
		userServices.addUpdateGlobalGroup(users.rumors().setStatus(GlobalGroupStatus.INACTIVE));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).doesNotContain(sasquatch).contains(edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(sasquatch).contains(edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch).contains(edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch).contains(edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).doesNotContain(sasquatch).contains(edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).doesNotContain(sasquatch).contains(edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch).contains(edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch).contains(edouard);
		}

	}

	@Test
	public void givenGroupInheritanceFromChildrenAndAccessAuthForASubGroupWhenTheGroupIsDisabledThenUsersStillHasTheAuthsButTheyNoLongerReceivingItsBenefits()
			throws Exception {

		givenConfig(GROUP_AUTHORIZATIONS_INHERITANCE, FROM_CHILD_TO_PARENT);

		auth1 = add(authorizationForGroup(rumors).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		auth2 = add(authorizationForGroup(rumors).on(FOLDER4).givingReadAccess());
		auth3 = add(authorizationForGroup(rumors).on(FOLDER3).givingReadDeleteAccess());

		getModelLayerFactory().getSecurityModelCache().models.remove(zeCollection);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().contains(sasquatch, edouard);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().contains(sasquatch, edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().contains(sasquatch, edouard);
		}

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingReadWrite().forPrincipals(rumors),
				authOnRecord(FOLDER4).givingRead().forPrincipals(rumors),
				authOnRecord(FOLDER3).givingReadDelete().forPrincipals(rumors)
		);

		userServices.addUpdateGlobalGroup(users.legends().setStatus(GlobalGroupStatus.INACTIVE));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingReadWrite().forPrincipals(rumors),
				authOnRecord(FOLDER4).givingRead().forPrincipals(rumors),
				authOnRecord(FOLDER3).givingReadDelete().forPrincipals(rumors)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().doesNotContain(edouard, sasquatch);
		}

		reenableLegends();
		userServices.addUpdateGlobalGroup(users.rumors().setStatus(GlobalGroupStatus.INACTIVE));

		//TODO Should not be required
		//Cache invalidation problemgetModelLayerFactory().getSecurityModelCache().removeFromAllCaches(zeCollection);
		reindex();

		assertThat(users.edouardIn(zeCollection).hasReadAccess().on(record(TAXO1_CATEGORY1))).isFalse();

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2)) {
			verifyRecord.usersWithWriteAccess().doesNotContain(edouard, sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithDeleteAccess().doesNotContain(edouard, sasquatch);
		}
	}

	@Test
	public void givenGroupInheritanceFromChildrenAndARoleAuthForASubGroupWhenTheGroupIsDisabledThenUsersStillHasTheAuthsButTheyNoLongerReceivingItsBenefits()
			throws Exception {

		givenConfig(GROUP_AUTHORIZATIONS_INHERITANCE, FROM_CHILD_TO_PARENT);

		auth1 = add(authorizationForGroup(rumors).on(TAXO1_CATEGORY1).giving(ROLE1));
		auth2 = add(authorizationForGroup(rumors).on(FOLDER4).giving(ROLE1));
		auth3 = add(authorizationForGroup(rumors).on(FOLDER3).giving(ROLE2));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).contains(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).contains(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).contains(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithRole(ROLE1).contains(sasquatch);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(sasquatch);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").contains(sasquatch);
		}

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(rumors)
		);

		userServices.addUpdateGlobalGroup(users.legends().setStatus(GlobalGroupStatus.INACTIVE));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(rumors)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).doesNotContain(sasquatch, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).doesNotContain(sasquatch, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
		}

		reenableLegends();
		userServices.addUpdateGlobalGroup(users.rumors().setStatus(GlobalGroupStatus.INACTIVE));

		//The auths are still existing. Should the user have been disabled by a mistake, it does not lose its auth when reactivated
		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY1).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER4).givingRoles(ROLE1).forPrincipals(rumors),
				authOnRecord(FOLDER3).givingRoles(ROLE2).forPrincipals(rumors)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER2, FOLDER4, FOLDER4_1)) {
			verifyRecord.usersWithRole(ROLE1).doesNotContain(sasquatch, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE1, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE2).doesNotContain(sasquatch, edouard);
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE2).doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithRoleForRecord(ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
			assertThat(services.getUsersWithPermissionOnRecord(PERMISSION_OF_ROLE2, record(verifyRecord.recordId)))
					.extracting("username").doesNotContain(sasquatch, edouard);
		}
	}

	@Test
	public void givenAuthorizationOnRecordWhenPhysicallyDeletingTheRecordThenAuthorizationDeleted()
			throws Exception {

		checkIfChuckNorrisHasAccessToEverythingInZeCollection = false;
		auth1 = add(authorizationForUser(bob).on(FOLDER1).givingReadAccess());
		auth2 = add(authorizationForGroup(heroes).on(FOLDER2_1).givingReadAccess());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(FOLDER1).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER2_1).givingRead().forPrincipals(heroes)
		);

		givenRecordIsLogicallyThenPhysicallyDeleted(FOLDER1);
		givenRecordIsLogicallyThenPhysicallyDeleted(FOLDER2);

		assertThatAllAuthorizations().isEmpty();

	}

	@Test
	public void givenMultipleUserFiltersThenReturnsOnlyIfEveryFiltersAreTrue()
			throws Exception {
		auth1 = add(authorizationForUser(bob).on(FOLDER1).givingReadAccess());

		User bob = users.bobIn(zeCollection);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchQuery queryWithAccessOnly = new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(zeCollection).where(IDENTIFIER).isEqualTo(FOLDER1))
				.filteredWithUser(bob);
		LogicalSearchQuery queryWithPermissionOnly = new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(zeCollection).where(IDENTIFIER).isEqualTo(FOLDER1))
				.filteredWithUser(bob, PERMISSION_OF_ROLE1);
		LogicalSearchQuery queryWithMultipleFilters = new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(zeCollection).where(IDENTIFIER).isEqualTo(FOLDER1))
				.filteredWithUser(bob, asList(Role.READ, PERMISSION_OF_ROLE1));

		assertThat(searchServices.getResultsCount(queryWithAccessOnly)).isEqualTo(1);
		assertThat(searchServices.getResultsCount(queryWithPermissionOnly)).isEqualTo(0);
		assertThat(searchServices.getResultsCount(queryWithMultipleFilters)).isEqualTo(0);

		auth2 = add(authorizationForUser(bob.getUsername()).on(FOLDER1).giving(ROLE1));
		for (RecordVerifier verifyRecord : $(FOLDER1)) {
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1)
					.contains(bob.getUsername());
		}

		recordServices.refresh(bob);

		assertThat(searchServices.getResultsCount(queryWithAccessOnly)).isEqualTo(1);
		assertThat(searchServices.getResultsCount(queryWithPermissionOnly)).isEqualTo(1);
		assertThat(searchServices.getResultsCount(queryWithMultipleFilters)).isEqualTo(1);
	}


	@Test
	public void givenUserHasGlobalRoleWhenSearchingUsingPermissionThenNothingIsFiltered()
			throws Exception {
		auth1 = add(authorizationForUser(bob).on(FOLDER1).givingReadAccess());

		User bob = users.bobIn(zeCollection);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchQuery queryWithAccessOnly = new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(zeCollection).where(IDENTIFIER).isEqualTo(FOLDER1))
				.filteredWithUser(bob);
		LogicalSearchQuery queryWithPermissionOnly = new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(zeCollection).where(IDENTIFIER).isEqualTo(FOLDER1))
				.filteredWithUser(bob, PERMISSION_OF_ROLE1);
		LogicalSearchQuery queryWithMultipleFilters = new LogicalSearchQuery()
				.setCondition(fromAllSchemasIn(zeCollection).where(IDENTIFIER).isEqualTo(FOLDER1))
				.filteredWithUser(bob, asList(Role.READ, PERMISSION_OF_ROLE1));

		assertThat(searchServices.getResultsCount(queryWithAccessOnly)).isEqualTo(1);
		assertThat(searchServices.getResultsCount(queryWithPermissionOnly)).isEqualTo(0);
		assertThat(searchServices.getResultsCount(queryWithMultipleFilters)).isEqualTo(0);

		List<String> roles = new ArrayList<>(bob.getUserRoles());
		roles.add(ROLE1);
		recordServices.update(bob.setUserRoles(roles));

		for (RecordVerifier verifyRecord : $(FOLDER1)) {
			verifyRecord.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(PERMISSION_OF_ROLE1)
					.doesNotContain(bob.getUsername());
		}

		recordServices.refresh(bob);

		assertThat(searchServices.getResultsCount(queryWithAccessOnly)).isEqualTo(1);
		assertThat(searchServices.getResultsCount(queryWithPermissionOnly)).isEqualTo(1);
		assertThat(searchServices.getResultsCount(queryWithMultipleFilters)).isEqualTo(1);
	}

	@Test
	public void whenRecordIsDetachedThenOnlyInheritedAuthsAreDetached()
			throws Exception {

		auth1 = add(authorizationForUser(bob).on(TAXO1_FOND1).givingReadAccess());
		auth2 = add(authorizationForGroup(heroes).on(TAXO1_FOND1).givingReadAccess());

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1).containsOnly(
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1_DOC1).containsOnly(
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_FOND1).givingRead().forPrincipals(heroes)
		).hasSize(2);

		detach(FOLDER4);

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1_DOC1).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingRead().forPrincipals(heroes)
		).hasSize(2);

		detach(FOLDER4_1);

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(FOLDER4).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1).containsOnly(
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(heroes)
		).hasSize(2);

		assertThatAuthorizationsOn(FOLDER4_1_DOC1).containsOnly(
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(bob),
				authOnRecord(FOLDER4_1).givingRead().forPrincipals(heroes)
		).hasSize(2);

	}

	@Test
	public void whenARecordReceiveNonOverridingAuthFromMetadataProvidingSecurityThenAllApplied()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER_TYPE2).givingReadWriteAccess());
		auth5 = add(authorizationForUser(edouard).on(FOLDER_TYPE3).givingReadWriteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, chuck);
		}

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1)
				.set(setup.folderSchema.secondReferenceMetadataProvidingSecurity(), FOLDER_TYPE2)
				.set(setup.folderSchema.thirdReferenceMetadataWhichDoesNotProvideSecurity(), FOLDER_TYPE3));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, dakota, chuck);
		}

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE3)
				.set(setup.folderSchema.secondReferenceMetadataProvidingSecurity(), FOLDER_TYPE2)
				.set(setup.folderSchema.thirdReferenceMetadataWhichDoesNotProvideSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, edouard, dakota, chuck);
		}

		//givenARecordWithOverridingAuthFromMetadataProvidingSecurityWhenMetadataIsSetToNullThenRecoverInheritingAuthsAndLoseAuthsFromMetadata
		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), null)
				.set(setup.folderSchema.secondReferenceMetadataProvidingSecurity(), null));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, chuck);
		}

	}

	@Test
	public void whenARecordReceiveOverridingAuthFromMetadataProvidingSecurityThenOnlySpecificAuthsAreApplied()
			throws Exception {
		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER_TYPE2).givingReadWriteAccess().andOverridingInheritedAuths());
		auth5 = add(authorizationForUser(edouard).on(FOLDER_TYPE3).givingReadWriteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, chuck);
		}

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1)
				.set(setup.folderSchema.secondReferenceMetadataProvidingSecurity(), FOLDER_TYPE2)
				.set(setup.folderSchema.thirdReferenceMetadataWhichDoesNotProvideSecurity(), FOLDER_TYPE3));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, dakota, chuck);
		}

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE3)
				.set(setup.folderSchema.secondReferenceMetadataProvidingSecurity(), FOLDER_TYPE2)
				.set(setup.folderSchema.thirdReferenceMetadataWhichDoesNotProvideSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, edouard, dakota, chuck);
		}

		//givenARecordHasOverridingAuthFromMetadataProvidingSecurityWhenMetadataIsSetToAnotherValueNotOverridingThenRecoverInheritingAuthsAndReceiveNewAuths
		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE3)
				.set(setup.folderSchema.secondReferenceMetadataProvidingSecurity(), FOLDER_TYPE1)
				.set(setup.folderSchema.thirdReferenceMetadataWhichDoesNotProvideSecurity(), FOLDER_TYPE2));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, edouard, chuck);
		}
	}

	@Test
	public void givenARecordWithMultipleOverridingAuthsFromMetadataProvidingSecurityThenRecoverInheritedAuthsWhenTheLastOneIsRemoved()
			throws Exception {
		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess());
		auth4 = add(authorizationForUser(dakota).on(FOLDER_TYPE2).givingReadWriteAccess().andOverridingInheritedAuths());
		auth5 = add(authorizationForUser(edouard).on(FOLDER_TYPE3).givingReadWriteAccess().andOverridingInheritedAuths());

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, chuck);
		}

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		//givenARecordHasNonOverridingAuthFromMetadataProvidingSecurityWhenMetadataIsSetToAnotherValueOverridingThenLoseInheritingAuthsAndReceiveNewAuths
		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE2)
				.set(setup.folderSchema.secondReferenceMetadataProvidingSecurity(), FOLDER_TYPE3));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, dakota, edouard, chuck);
		}

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE2)
				.set(setup.folderSchema.secondReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, dakota, chuck);
		}

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), null));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}
	}

	@Test
	public void givenRecordProvidingSecurityHasItsAuthsModifiedThenChangesAlwaysAppliedToSecurableRecords()
			throws Exception {
		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, chuck);
		}

		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		modify(modifyAuthorizationOnRecord(auth3, records.folderType1()).withNewOverridingInheritedAuths(true));
		reindex();//TODO Improve impact modification
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, chuck);
		}

		modify(modifyAuthorizationOnRecord(auth3, records.folderType1()).withNewOverridingInheritedAuths(false));
		reindex();//TODO Improve impact modification
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		modify(modifyAuthorizationOnRecord(auth3, records.folderType1()).withNewPrincipalIds(gandalf));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, gandalf, chuck);
		}

		auth4 = add(authorizationForUser(dakota).on(FOLDER_TYPE1).givingReadWriteAccess().andOverridingInheritedAuths());
		reindex();//TODO Improve impact modification
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, gandalf, dakota, chuck);
		}

		modify(modifyAuthorizationOnRecord(auth3, records.folderType1()).removingItOnRecord());
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, dakota, chuck);
		}

		modify(modifyAuthorizationOnRecord(auth4, records.folderType1()).removingItOnRecord());
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, chuck);
		}

	}

	@Test
	public void givenARecordHasOverridingAuthFromMetadataProvidingSecurityWhenAuthIsRemovedOnChildThenNoMoreAppliedButDoesNotRecoverParentInheritedAuths()
			throws Exception {

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess().andOverridingInheritedAuths());
		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));
		waitForBatchProcess();
		assertThat(users.charlesIn(zeCollection).hasReadAccess().on(record(FOLDER4))).isTrue();

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, chuck);
		}

		modify(modifyAuthorizationOnRecord(auth3, records.folder4_1()).removingItOnRecord());

		verifyRecord(FOLDER4).usersWithWriteAccess().contains(bob, charles, chuck);
		verifyRecord(FOLDER4_1).usersWithWriteAccess().contains(bob, chuck);
	}

	@Test
	public void givenARecordReceivingNonOVerridingAuthsFromMetadataProvidingSecurityIsDetachedThenReceivedAuthsAreNotDuplicatedAndRecord()
			throws Exception {
		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess());

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		detach(FOLDER4);

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(charles),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(bob)
		);
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
	}

	@Test
	public void givenARecordReceivingOverridingAuthsFromMetadataProvidingIsDetachedThenInheritedAndReceivedByMetadataAuthsAreNotDuplicated()
			throws Exception {
		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess().andOverridingInheritedAuths());

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, chuck);
		}

		detach(FOLDER4);

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, chuck);
		}

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(charles)
		);
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
	}

	@Test
	public void givenARecordReceivingMetadataAuthsFromMetadataProvidingIsDetachedThenInheritedAndReceivedByMetadataAuthsAreNotDuplicated()
			throws Exception {
		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess());

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		detach(FOLDER4);

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		assertThatAuthorizationsOn(FOLDER4).containsOnly(
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(bob),
				authOnRecord(FOLDER4).givingReadWrite().forPrincipals(charles)
		);
		verifyRecord(FOLDER4).detachedAuthorizationFlag().isTrue();
	}

	@Test
	public void givenARecordWithParentReceivingOverridingAuthsFromMetadataProvidingIsDetachedThenEverythingDuplicated()
			throws Exception {
		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess().andOverridingInheritedAuths());

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, chuck);
		}

		detach(FOLDER4_1);

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, chuck);
		}

		assertThatAuthorizationsOn(FOLDER4_1).containsOnly(
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(bob),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(charles)
		);
		verifyRecord(FOLDER4_1).detachedAuthorizationFlag().isTrue();
	}

	@Test
	public void givenARecordWithParentReceivingMetadataAuthsFromMetadataProvidingIsDetachedThenEverythingIsDuplicated()
			throws Exception {
		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess());

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		detach(FOLDER4_1);

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		assertThatAuthorizationsOn(FOLDER4_1).containsOnly(
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(bob),
				authOnRecord(FOLDER4_1).givingReadWrite().forPrincipals(charles)
		);
		verifyRecord(FOLDER4_1).detachedAuthorizationFlag().isTrue();
	}

	@Test
	public void givenARecordWithGrandParentReceivingOverridingAuthsFromMetadataProvidingIsDetachedThenEverythingDuplicated()
			throws Exception {
		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess().andOverridingInheritedAuths());

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, chuck);
		}

		detach(FOLDER4_2_DOC1);

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, chuck);
		}

		assertThatAuthorizationsOn(FOLDER4_2_DOC1).containsOnly(
				authOnRecord(FOLDER4_2_DOC1).givingReadWrite().forPrincipals(bob),
				authOnRecord(FOLDER4_2_DOC1).givingReadWrite().forPrincipals(charles)
		);
		verifyRecord(FOLDER4_2_DOC1).detachedAuthorizationFlag().isTrue();
	}

	@Test
	public void givenARecordWithGrandParentReceivingMetadataAuthsFromMetadataProvidingIsDetachedThenEverythingIsDuplicated()
			throws Exception {
		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1).givingReadWriteAccess());

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		detach(FOLDER4_2_DOC1);

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		assertThatAuthorizationsOn(FOLDER4_2_DOC1).containsOnly(
				authOnRecord(FOLDER4_2_DOC1).givingReadWrite().forPrincipals(alice),
				authOnRecord(FOLDER4_2_DOC1).givingReadWrite().forPrincipals(bob),
				authOnRecord(FOLDER4_2_DOC1).givingReadWrite().forPrincipals(charles)
		);
		verifyRecord(FOLDER4_2_DOC1).detachedAuthorizationFlag().isTrue();
	}

	@Test
	public void givenARecordReceivingMetadataAuthsFromMetadataProvidingHasStartAndEndDateThenOnlyAppliedDuringInterval()
			throws Exception {

		setTimeToCalling(date(2016, 4, 4));

		auth1 = add(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER4).givingReadWriteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER_TYPE1)
				.givingReadWriteAccess().during(date(2016, 4, 3), date(2016, 4, 6)));
		auth4 = add(authorizationForUser(dakota).on(FOLDER_TYPE1)
				.givingReadWriteAccess().during(date(2016, 4, 4), date(2016, 4, 5)));
		auth5 = add(authorizationForUser(edouard).on(FOLDER_TYPE2)
				.givingReadWriteAccess().during(date(2016, 4, 5), date(2016, 4, 5)).andOverridingInheritedAuths());

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, chuck);
		}

		recordServices.update(records.folder4()
				.set(setup.folderSchema.firstReferenceMetadataProvidingSecurity(), FOLDER_TYPE1)
				.set(setup.folderSchema.secondReferenceMetadataProvidingSecurity(), FOLDER_TYPE2)
				.set(setup.folderSchema.thirdReferenceMetadataWhichDoesNotProvideSecurity(), FOLDER_TYPE3));

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, dakota, chuck);
		}

		setTimeToCalling(date(2016, 4, 5));
		UserPermissionsChecker checker = users.aliceIn(zeCollection).hasWriteAccess();
		assertThat(checker.on(record(FOLDER4))).isFalse();

		assertThat(checker.on(record(FOLDER4_1))).isFalse();

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, dakota, edouard, chuck);
		}

		setTimeToCalling(date(2016, 4, 6));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, charles, chuck);
		}

		setTimeToCalling(date(2016, 4, 7));
		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(alice, bob, chuck);
		}

	}

	@Test
	public void givenUserHasGlobalAccessOrNoAccessThenNegativeAuthorizationsDoesNotAffectTheirAccesses()
			throws Exception {

		recordServices.update(users.aliceIn(zeCollection).setCollectionReadAccess(true));
		auth1 = add(authorizationForUser(alice).on(FOLDER4).givingNegativeReadWriteAccess());
		auth2 = add(authorizationForUser(chuck).on(FOLDER1).givingNegativeReadWriteAccess());
		auth3 = add(authorizationForGroups(legends).on(FOLDER2).givingNegativeReadWriteDeleteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER2, TAXO1_CATEGORY2, FOLDER1_DOC1, FOLDER2_2, FOLDER4)) {
			verifyRecord.usersWithReadAccess().containsOnly(alice, chuck);
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			verifyRecord.usersWithDeleteAccess().containsOnly(chuck);
		}

	}

	@Test
	public void givenUserHasNoAccessesWhenReceivingNegativeAuthorizationsThenStillHasNoAccesses() {

		auth1 = add(authorizationForUser(bob).on(FOLDER3).givingNegativeReadWriteAccess());
		auth2 = add(authorizationForUser(bob).on(FOLDER1).givingNegativeReadWriteAccess());
		auth3 = add(authorizationForUser(bob).on(FOLDER2).givingNegativeReadDeleteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER2, FOLDER1_DOC1, FOLDER2_2, FOLDER3)) {
			verifyRecord.usersWithReadAccess().containsOnly(chuck);
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			verifyRecord.usersWithDeleteAccess().containsOnly(chuck);
		}
	}

	@Test
	public void givenUserIsInheritingAccessesFromItsGroupThenNegativeAuthorizationsDoesRestrictTheirAccesses()
			throws Exception {

		recordServices.update(users.aliceIn(zeCollection).setCollectionReadAccess(true));

		auth1 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadWriteDeleteAccess());
		auth2 = add(authorizationForGroup(heroes).on(FOLDER1).givingReadWriteDeleteAccess());
		auth3 = add(authorizationForGroup(heroes).on(FOLDER2).givingReadWriteDeleteAccess());

		auth4 = add(authorizationForUser(charles).on(FOLDER2).givingNegativeReadWriteAccess());
		auth5 = add(authorizationForUser(charles).on(FOLDER3).givingNegativeDeleteAccess());
		auth6 = add(authorizationForUser(charles).on(FOLDER4).givingNegativeWriteAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER1, FOLDER1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(dakota, gandalf, charles, alice, chuck, robin);
			verifyRecord.usersWithWriteAccess().containsOnly(dakota, gandalf, charles, chuck, robin);
			verifyRecord.usersWithDeleteAccess().containsOnly(dakota, gandalf, charles, chuck, robin);
		}

		for (RecordVerifier verifyRecord : $(FOLDER2, FOLDER2_1)) {
			verifyRecord.usersWithReadAccess().containsOnly(dakota, gandalf, alice, chuck, robin);
			verifyRecord.usersWithWriteAccess().containsOnly(dakota, gandalf, chuck, robin);
			verifyRecord.usersWithDeleteAccess().containsOnly(dakota, gandalf, chuck, robin);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(dakota, gandalf, alice, chuck, robin, charles);
			verifyRecord.usersWithWriteAccess().containsOnly(dakota, gandalf, chuck, robin, charles);
			verifyRecord.usersWithDeleteAccess().containsOnly(dakota, gandalf, chuck, robin);
		}

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2)) {
			verifyRecord.usersWithReadAccess().containsOnly(dakota, gandalf, alice, chuck, robin, charles);
			verifyRecord.usersWithWriteAccess().containsOnly(dakota, gandalf, chuck, robin);
			verifyRecord.usersWithDeleteAccess().containsOnly(dakota, gandalf, chuck, robin, charles);
		}

		modify(authorizationOnRecord(auth6, FOLDER4_1).removingItOnRecord());

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_2)) {
			verifyRecord.usersWithReadAccess().containsOnly(dakota, gandalf, alice, chuck, robin, charles);
			verifyRecord.usersWithWriteAccess().containsOnly(dakota, gandalf, chuck, robin);
			verifyRecord.usersWithDeleteAccess().containsOnly(dakota, gandalf, chuck, robin, charles);
		}

		for (RecordVerifier verifyRecord : $(FOLDER4_1, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(dakota, gandalf, alice, chuck, robin, charles);
			verifyRecord.usersWithWriteAccess().containsOnly(dakota, gandalf, chuck, robin, charles);
			verifyRecord.usersWithDeleteAccess().containsOnly(dakota, gandalf, chuck, robin, charles);
		}

		modify(authorizationOnRecord(auth5, FOLDER3).removingItOnRecord());
		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(dakota, gandalf, alice, chuck, robin, charles);
			verifyRecord.usersWithWriteAccess().containsOnly(dakota, gandalf, chuck, robin, charles);
			verifyRecord.usersWithDeleteAccess().containsOnly(dakota, gandalf, chuck, robin, charles);
		}
	}

	@Test
	public void givenUserIsInheritingNegativeAccessesFromItsGroupThenPositiveAuthorizationsDoesNotCounterTheNegativeAccesses() {

		auth1 = add(authorizationForGroup(heroes).on(FOLDER3).givingNegativeReadWriteAccess());
		auth2 = add(authorizationForGroup(heroes).on(FOLDER2).givingNegativeReadDeleteAccess());

		auth3 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth4 = add(authorizationForUser(charles).on(FOLDER1).givingReadWriteAccess());
		auth5 = add(authorizationForUser(charles).on(FOLDER2_1).givingReadDeleteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1, FOLDER4)) {
			verifyRecord.usersWithReadAccess().contains(charles);
			verifyRecord.usersWithWriteAccess().contains(charles);
			verifyRecord.usersWithDeleteAccess().doesNotContain(charles);
		}

		for (RecordVerifier verifyRecord : $(FOLDER3, FOLDER2, FOLDER2_1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(charles);
			verifyRecord.usersWithWriteAccess().doesNotContain(charles);
			verifyRecord.usersWithDeleteAccess().doesNotContain(charles);
		}
	}

	@Test
	public void givenUserIsInheritingNegativeAndPositiveAccessesFromItsGroupThenNegativeAlwaysWins() {

		auth1 = add(authorizationForGroup(heroes).on(FOLDER3).givingNegativeReadWriteAccess());
		auth2 = add(authorizationForGroup(heroes).on(FOLDER2).givingNegativeReadDeleteAccess());
		auth3 = add(authorizationForGroup(heroes).on(FOLDER1).givingNegativeReadDeleteAccess());

		auth4 = add(authorizationForGroup(legends).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth5 = add(authorizationForGroup(legends).on(FOLDER1).givingReadWriteAccess());
		auth6 = add(authorizationForGroup(legends).on(FOLDER2_1).givingReadDeleteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER4)) {
			verifyRecord.usersWithReadAccess().contains(gandalf);
			verifyRecord.usersWithWriteAccess().contains(gandalf);
			verifyRecord.usersWithDeleteAccess().doesNotContain(gandalf);
		}

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1, FOLDER3, FOLDER2, FOLDER2_1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(gandalf);
			verifyRecord.usersWithWriteAccess().doesNotContain(gandalf);
			verifyRecord.usersWithDeleteAccess().doesNotContain(gandalf);
		}
	}

	@Test
	public void givenUserHasNegativeAndPositiveAccessesThenNegativeAlwaysWins() {

		auth1 = add(authorizationForUser(charles).on(FOLDER3).givingNegativeReadWriteAccess());
		auth2 = add(authorizationForUser(charles).on(FOLDER2).givingNegativeReadDeleteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER1).givingNegativeReadDeleteAccess());

		auth4 = add(authorizationForUser(charles).on(FOLDER3).givingReadWriteAccess());
		auth5 = add(authorizationForUser(charles).on(FOLDER4).givingReadWriteAccess());
		auth6 = add(authorizationForUser(charles).on(FOLDER1).givingReadWriteAccess());
		auth7 = add(authorizationForUser(charles).on(FOLDER2_1).givingReadDeleteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER4)) {
			verifyRecord.usersWithReadAccess().contains(charles);
			verifyRecord.usersWithWriteAccess().contains(charles);
			verifyRecord.usersWithDeleteAccess().doesNotContain(charles);
		}

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1, FOLDER3, FOLDER2, FOLDER2_1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(charles);
			verifyRecord.usersWithWriteAccess().doesNotContain(charles);
			verifyRecord.usersWithDeleteAccess().doesNotContain(charles);
		}
	}

	@Test
	public void givenUserHasNegativeAccessesFromTheRecordInheritanceThenDoesNotReceivePositiveAuthorizationsOnTheRecordItself() {

		auth1 = add(authorizationForUser(charles).on(FOLDER3).givingNegativeReadWriteAccess());
		auth2 = add(authorizationForUser(charles).on(FOLDER2).givingNegativeReadDeleteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER1).givingNegativeReadDeleteAccess());

		auth4 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2_1).givingReadWriteAccess());
		auth5 = add(authorizationForUser(charles).on(FOLDER1_DOC1).givingReadWriteAccess());
		auth6 = add(authorizationForUser(charles).on(FOLDER2_1).givingReadDeleteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1, FOLDER3, FOLDER2, FOLDER2_1, FOLDER4)) {
			verifyRecord.usersWithReadAccess().doesNotContain(charles);
			verifyRecord.usersWithWriteAccess().doesNotContain(charles);
			verifyRecord.usersWithDeleteAccess().doesNotContain(charles);
		}

		detach(FOLDER1_DOC1);
		detach(FOLDER2);

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1, FOLDER3, FOLDER2, FOLDER2_1, FOLDER4)) {
			verifyRecord.usersWithReadAccess().doesNotContain(charles);
			verifyRecord.usersWithWriteAccess().doesNotContain(charles);
			verifyRecord.usersWithDeleteAccess().doesNotContain(charles);
		}
	}

	@Test
	public void givenUserHasPositiveAccessesFromTheRecordInheritanceWhenReceivingNegativeAuthsOnTheRecordThenLooseAccess() {

		auth1 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingReadDeleteAccess());
		auth2 = add(authorizationForUser(charles).on(FOLDER2).givingReadDeleteAccess());
		auth3 = add(authorizationForUser(charles).on(FOLDER1).givingReadDeleteAccess());

		auth4 = add(authorizationForUser(charles).on(FOLDER3).givingNegativeReadWriteAccess());
		auth5 = add(authorizationForUser(charles).on(FOLDER1_DOC1).givingNegativeReadWriteAccess());
		auth6 = add(authorizationForUser(charles).on(FOLDER2_1).givingNegativeReadDeleteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER1_DOC1, FOLDER3, FOLDER2_1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(charles);
			verifyRecord.usersWithWriteAccess().doesNotContain(charles);
			verifyRecord.usersWithDeleteAccess().doesNotContain(charles);
		}

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER4, FOLDER2)) {
			verifyRecord.usersWithReadAccess().contains(charles);
			verifyRecord.usersWithWriteAccess().doesNotContain(charles);
			verifyRecord.usersWithDeleteAccess().contains(charles);
		}

		detach(FOLDER1_DOC1);
		detach(FOLDER2);

		for (RecordVerifier verifyRecord : $(FOLDER1_DOC1, FOLDER3, FOLDER2_1)) {
			verifyRecord.usersWithReadAccess().doesNotContain(charles);
			verifyRecord.usersWithWriteAccess().doesNotContain(charles);
			verifyRecord.usersWithDeleteAccess().doesNotContain(charles);
		}

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER4, FOLDER2)) {
			verifyRecord.usersWithReadAccess().contains(charles);
			verifyRecord.usersWithWriteAccess().doesNotContain(charles);
			verifyRecord.usersWithDeleteAccess().contains(charles);
		}

	}

	@Test
	public void givenUserHasPositiveAndNegativeAccessesInMultipleCollectionsWhenFederateSearchingThenOnlyReturnRecordsWithAccess()
			throws Exception {
		checkIfDakotaSeeAndCanDeleteEverythingInCollection2 = false;
		for (String collection : asList(zeCollection, anotherCollection)) {
			recordServices.update(users.dakotaLIndienIn(collection).setCollectionReadAccess(false));
			recordServices.update(users.gandalfLeblancIn(collection).setCollectionReadAccess(false));
			recordServices.update(users.charlesIn(collection).setCollectionReadAccess(false));
		}
		if (anothercollectionSetup.getTaxonomy2() == null) {
			anothercollectionSetup.setUp();
		}

		getModelLayerFactory().getTaxonomiesManager().setPrincipalTaxonomy(anothercollectionSetup.getTaxonomy2(), getModelLayerFactory().getMetadataSchemasManager());
		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE);

		Taxonomy principalTaxoInAnotherCollection = getModelLayerFactory().getTaxonomiesManager().getPrincipalTaxonomy(anotherCollection);
		assertThat(principalTaxoInAnotherCollection.getCode()).isEqualTo("taxo2");

		auth1 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadDeleteAccess());
		auth2 = add(authorizationForGroup(legends).on(FOLDER3).givingNegativeReadWriteAccess());

		auth3 = add(authorizationForUser(charles).on(FOLDER3).givingNegativeReadWriteAccess());

		//Donne accès en lecture au dossier 2
		auth4 = add(authorizationForGroupInAnotherCollection(heroes).on(otherCollectionRecords.taxo2_station2_1()).givingReadWriteDeleteAccess());
		auth5 = add(authorizationForUserInAnotherCollection(charles).on(otherCollectionRecords.folder4()).givingReadWriteDeleteAccess());

		auth6 = add(authorizationForGroupInAnotherCollection(heroes).on(otherCollectionRecords.folder4_1()).givingNegativeReadWriteDeleteAccess());
		auth7 = add(authorizationForUserInAnotherCollection(charles).on(otherCollectionRecords.folder2_1()).givingNegativeReadWriteDeleteAccess());

		waitForBatchProcess();

		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		SecurityModel securityModel = getModelLayerFactory().newRecordServices().getSecurityModel(zeCollection);
		assertThat(securityModel.getAuthorizationsToPrincipal(users.charlesIn(zeCollection).getId(), false))
				.extracting("details.id").containsOnly(auth3);
		assertThat(securityModel.getAuthorizationsToPrincipal(users.heroesIn(zeCollection).getId(), false))
				.extracting("details.id").containsOnly(auth1);

		securityModel = getModelLayerFactory().newRecordServices().getSecurityModel(anotherCollection);
		assertThat(securityModel.getAuthorizationsToPrincipal(users.charlesIn(anotherCollection).getId(), false))
				.extracting("details.id").containsOnly(auth5, auth7);
		assertThat(securityModel.getAuthorizationsToPrincipal(users.heroesIn(anotherCollection).getId(), false))
				.extracting("details.id").containsOnly(auth4, auth6);

		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0).setQueryLoggingEnabled(true).setQueryDebuggingMode(true);

		assertThatAllFoldersVisibleBy(charles).containsOnly(
				"folder4", "folder4_1", "folder4_2", "anotherCollection_folder2", "anotherCollection_folder2_2",
				"anotherCollection_folder4", "anotherCollection_folder4_2", "anotherCollection_folder4_1");

		assertThatAllFoldersVisibleBy(dakota).containsOnly(
				"folder4", "folder4_1", "folder4_2", "folder3", "anotherCollection_folder2", "anotherCollection_folder2_2", "anotherCollection_folder2_1");

		assertThatAllFoldersVisibleBy(gandalf).containsOnly(
				"folder4", "folder4_1", "folder4_2", "anotherCollection_folder2", "anotherCollection_folder2_2", "anotherCollection_folder2_1");

	}

	@Test
	public void givenUserWithCollectionAccessAndPermissionAuthOnAConceptThenSecurisedRecordsReturnedByPermissionFilter()
			throws RecordServicesException {
		recordServices.update(users.aliceIn(zeCollection).setCollectionAllAccess(true));
		add(authorizationForUsers(alice, bob, charles).on(FOLDER1).giving(ROLE1));
		add(authorizationForUser(bob).on(FOLDER1).givingReadWriteAccess());
		add(authorizationForUser(charles).on(TAXO1_CATEGORY1).givingReadWriteAccess());

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1)) {
			LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(
					fromAllSchemasIn(zeCollection).where(IDENTIFIER).is(verifyRecord.recordId));
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(1);

			//Alice
			logicalSearchQuery.filteredWithUser(users.aliceIn(zeCollection), PERMISSION_OF_ROLE1);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(1);

			logicalSearchQuery.filteredWithUser(users.aliceIn(zeCollection), PERMISSION_OF_ROLE2);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(0);

			//Bob
			logicalSearchQuery.filteredWithUser(users.bobIn(zeCollection), PERMISSION_OF_ROLE1);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(1);

			logicalSearchQuery.filteredWithUser(users.bobIn(zeCollection), PERMISSION_OF_ROLE2);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(0);

			//Charles
			logicalSearchQuery.filteredWithUser(users.charlesIn(zeCollection), PERMISSION_OF_ROLE1);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(1);

			logicalSearchQuery.filteredWithUser(users.charlesIn(zeCollection), PERMISSION_OF_ROLE2);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(0);
		}
	}


	@Test
	public void givenUserWithCollectionAccessAndPermissionAuthOnASecurisedRecordThenRecordsAndItsChildrenReturnedByPermissionFilter()
			throws RecordServicesException {
		recordServices.update(users.aliceIn(zeCollection).setCollectionAllAccess(true));
		add(authorizationForUsers(alice, bob, charles).on(FOLDER1).giving(ROLE1));
		add(authorizationForUser(bob).on(FOLDER1).givingReadWriteAccess());
		add(authorizationForUser(charles).on(TAXO1_CATEGORY1).givingReadWriteAccess());


		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER1_DOC1)) {

			LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(
					fromAllSchemasIn(zeCollection).where(IDENTIFIER).is(verifyRecord.recordId));
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(1);

			//Alice
			logicalSearchQuery.filteredWithUser(users.aliceIn(zeCollection), PERMISSION_OF_ROLE1);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(1);

			logicalSearchQuery.filteredWithUser(users.aliceIn(zeCollection), PERMISSION_OF_ROLE2);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(0);


			//Bob
			logicalSearchQuery.filteredWithUser(users.bobIn(zeCollection), PERMISSION_OF_ROLE1);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(1);

			logicalSearchQuery.filteredWithUser(users.bobIn(zeCollection), PERMISSION_OF_ROLE2);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(0);

			//Charles
			logicalSearchQuery.filteredWithUser(users.charlesIn(zeCollection), PERMISSION_OF_ROLE1);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(1);

			logicalSearchQuery.filteredWithUser(users.charlesIn(zeCollection), PERMISSION_OF_ROLE2);
			assertThat(searchServices.query(logicalSearchQuery).getNumFound()).isEqualTo(0);
		}
	}


	private ListAssert<String> assertThatAllFoldersVisibleBy(String username) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("fq", "schema_s:folder_*");
		params.set("q", "*:*");
		params.set("fl", "id");
		params.set("rows", "1000");

		UserServices userServices = getModelLayerFactory().newUserServices();

		FreeTextSearchServices freeTextSearchServices = new FreeTextSearchServices(getModelLayerFactory());
		QueryResponse queryResponse = freeTextSearchServices.search(new FreeTextQuery(params).filteredByUser(userServices.getUserCredential(username)));

		List<String> ids = new ArrayList<>();
		for (SolrDocument document : queryResponse.getResults()) {
			ids.add((String) document.getFieldValue("id"));
		}

		return assertThat(ids);
	}

	//Unsupported negative autorisation @Test
	public void givenUserHasGlobalAccessOrNoPermissionsThenNegativeAuthorizationsDoesNotAffectTheirPermissions() {

		auth1 = add(authorizationForUser(alice).on(FOLDER3).givingNegative(ROLE1));
		auth2 = add(authorizationForUser(chuckNorris).on(FOLDER1).givingNegative(ROLE1));
		auth3 = add(authorizationForGroups(legends).on(FOLDER2).givingNegative(ROLE1));

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER2, FOLDER1_DOC1, FOLDER2_2, FOLDER3)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).containsOnly(alice, chuck);
		}

	}

	//Unsupported negative autorisation @Test
	public void givenUserHasNoPermissionsWhenReceivingNegativeAuthorizationsThenStillHasNoPermissions() {

		auth1 = add(authorizationForUser(bob).on(FOLDER3).givingNegative(ROLE1));
		auth2 = add(authorizationForUser(bob).on(FOLDER1).givingNegative(ROLE1));
		auth3 = add(authorizationForUser(bob).on(FOLDER2).givingNegative(ROLE1));

		for (RecordVerifier verifyRecord : $(FOLDER1, FOLDER2, FOLDER1_DOC1, FOLDER2_2, FOLDER3)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).containsOnly(alice, chuck);
		}
	}

	//Unsupported negative autorisation @Test
	public void givenUserIsInheritingPermissionsFromItsGroupThenNegativeAuthorizationsDoesRestrictTheirPermissions
	() {

		auth1 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingNegative(ROLE1));
		auth2 = add(authorizationForGroup(heroes).on(FOLDER1).givingNegative(ROLE1));
		auth3 = add(authorizationForGroup(heroes).on(FOLDER2).givingNegative(ROLE1));

		auth4 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2_1).givingNegative(ROLE1));
		auth5 = add(authorizationForUser(charles).on(FOLDER2).givingNegative(ROLE1));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER1, FOLDER1_DOC1, FOLDER4)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).containsOnly(dakota, gandalf, charles, alice, chuck);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER2, FOLDER2_1)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).containsOnly(dakota, gandalf, alice, chuck);
		}

	}

	//Unsupported negative autorisation @Test
	public void givenUserIsInheritingNegativePermissionsFromItsGroupThenPositiveAuthorizationsDoesNotCounterTheNegativePermissions
	() {

		auth1 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2_1).givingNegative(ROLE1));
		auth2 = add(authorizationForGroup(heroes).on(FOLDER2).givingNegative(ROLE1));

		auth3 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingNegative(ROLE1));
		auth4 = add(authorizationForUser(charles).on(FOLDER1).givingNegative(ROLE1));
		auth5 = add(authorizationForUser(charles).on(FOLDER2_1).givingNegative(ROLE1));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER1, FOLDER1_DOC1, FOLDER4)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(charles);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER2, FOLDER2_1)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(charles);
		}
	}

	//Unsupported negative autorisation @Test
	public void givenUserIsInheritingNegativeAndPositivePermissionsFromItsGroupThenNegativeAlwaysWins() {

		auth1 = add(authorizationForGroup(heroes).on(TAXO1_CATEGORY2_1).givingNegative(ROLE1));
		auth2 = add(authorizationForGroup(heroes).on(FOLDER2).givingNegative(ROLE1));
		auth3 = add(authorizationForGroup(heroes).on(FOLDER1).givingNegative(ROLE1));

		auth4 = add(authorizationForGroup(legends).on(TAXO1_CATEGORY2).givingNegative(ROLE1));
		auth5 = add(authorizationForGroup(legends).on(FOLDER1).givingNegative(ROLE1));
		auth6 = add(authorizationForGroup(legends).on(FOLDER2_1).givingNegative(ROLE1));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(gandalf);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER1, FOLDER1_DOC1, FOLDER3, FOLDER2, FOLDER2_1)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(gandalf);
		}
	}

	//Unsupported negative autorisation @Test
	public void givenUserHasNegativeAndPositivePermissionsThenNegativeAlwaysWins() {

		auth1 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2_1).givingNegative(ROLE1));
		auth2 = add(authorizationForUser(charles).on(FOLDER2).givingNegative(ROLE1));
		auth3 = add(authorizationForUser(charles).on(FOLDER1).givingNegative(ROLE1));

		auth4 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingNegative(ROLE1));
		auth5 = add(authorizationForUser(charles).on(FOLDER1).givingNegative(ROLE1));
		auth6 = add(authorizationForUser(charles).on(FOLDER2_1).givingNegative(ROLE1));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(charles);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER1, FOLDER1_DOC1, FOLDER3, FOLDER2, FOLDER2_1)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(charles);
		}
	}

	//Unsupported negative autorisation @Test
	public void givenUserHasNegativePermissionsFromTheRecordInheritanceThenDoesNotReceivePositiveAuthorizationsOnTheRecordItself
	() {

		auth1 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingNegative(ROLE1));
		auth2 = add(authorizationForUser(charles).on(FOLDER2).givingNegative(ROLE1));
		auth3 = add(authorizationForUser(charles).on(FOLDER1).givingNegative(ROLE1));

		auth4 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2_1).givingNegative(ROLE1));
		auth5 = add(authorizationForUser(charles).on(FOLDER1_DOC1).givingNegative(ROLE1));
		auth6 = add(authorizationForUser(charles).on(FOLDER2_1).givingNegative(ROLE1));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER1, FOLDER1_DOC1, FOLDER3, FOLDER2, FOLDER2_1,
				TAXO1_CATEGORY2, FOLDER4)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(charles);
		}

		detach(FOLDER1_DOC1);
		detach(FOLDER2);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER1, FOLDER1_DOC1, FOLDER3, FOLDER2, FOLDER2_1,
				TAXO1_CATEGORY2, FOLDER4)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(charles);
		}
	}

	//Unsupported negative autorisation @Test
	public void givenUserHasPositivePermissionsFromTheRecordInheritanceWhenReceivingNegativeAuthsOnTheRecordThenLoosePermissions
	() {

		auth1 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2).givingNegative(ROLE1));
		auth2 = add(authorizationForUser(charles).on(FOLDER2).givingNegative(ROLE1));
		auth3 = add(authorizationForUser(charles).on(FOLDER1).givingNegative(ROLE1));

		auth4 = add(authorizationForUser(charles).on(TAXO1_CATEGORY2_1).givingNegative(ROLE1));
		auth5 = add(authorizationForUser(charles).on(FOLDER1_DOC1).givingNegative(ROLE1));
		auth6 = add(authorizationForUser(charles).on(FOLDER2_1).givingNegative(ROLE1));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER1_DOC1, FOLDER3, FOLDER2_1)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(charles);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER1, FOLDER4, FOLDER2)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(charles);
		}

		detach(FOLDER1_DOC1);
		detach(FOLDER2);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER1_DOC1, FOLDER3, FOLDER2_1)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).contains(charles);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER1, FOLDER4, FOLDER2)) {
			verifyRecord.usersWithPermission(PERMISSION_OF_ROLE1).doesNotContain(charles);
		}

	}

	@Test
	public void whenCacheIsInvalidatedThenInvalidatedOnAllInstances()
			throws Exception {
		linkEventBus(getModelLayerFactory(), getModelLayerFactory("other-instance"));

		SecurityModelCache instance1Cache = getModelLayerFactory().getSecurityModelCache();
		auth1 = addWithoutUser(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());

		SecurityModelCache instance2Cache = getModelLayerFactory("other-instance").getSecurityModelCache();

		assertThat(instance1Cache.getCached(zeCollection)).is(containingAuthWithId(auth1));
		assertThat(instance2Cache.getCached(zeCollection)).isNull();

		createAFolderOnInstance2();
		assertThat(instance1Cache.getCached(zeCollection)).is(containingAuthWithId(auth1));
		assertThat(instance2Cache.getCached(zeCollection)).is(containingAuthWithId(auth1));

		GlobalGroup group = userServices.newGlobalGroup().setCode("zeGroup").setName("Ze ultimate group");
		userServices.addUpdateGlobalGroup(group);
		assertThat(instance1Cache.getCached(zeCollection)).isNull();
		assertThat(instance2Cache.getCached(zeCollection)).isNull();

	}

	@Test
	public void whenAuthIsCreatedOrDeletedThenCacheIsUpdated()
			throws Exception {
		linkEventBus(getModelLayerFactory(), getModelLayerFactory("other-instance"));
		String aliceId = users.aliceIn(zeCollection).getId();
		String bobId = users.bobIn(zeCollection).getId();

		createAFolderOnInstance1();
		createAFolderOnInstance2();

		SecurityModelCache instance1Cache = getModelLayerFactory().getSecurityModelCache();
		auth1 = addWithoutUser(authorizationForUser(alice).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		SecurityModelCache instance2Cache = getModelLayerFactory("other-instance").getSecurityModelCache();

		assertThat(instance1Cache.getCached(zeCollection)).isNotNull();
		assertThat(instance2Cache.getCached(zeCollection)).isNotNull();
		assertThat(instance1Cache.getCached(zeCollection).getAuthorizationsOnTarget(TAXO1_CATEGORY2))
				.extracting("details.id").contains(auth1);
		assertThat(instance1Cache.getCached(zeCollection).getAuthorizationsToPrincipal(aliceId, false))
				.extracting("details.id").contains(auth1);
		assertThat(instance1Cache.getCached(zeCollection).getAuthorizationWithId(auth1).getDetails().getRoles())
				.containsOnly(Role.READ, Role.WRITE);

		assertThat(instance2Cache.getCached(zeCollection)).isNotNull();
		assertThat(instance2Cache.getCached(zeCollection).getAuthorizationsOnTarget(TAXO1_CATEGORY2))
				.extracting("details.id").contains(auth1);
		assertThat(instance2Cache.getCached(zeCollection).getAuthorizationsToPrincipal(aliceId, false))
				.extracting("details.id").contains(auth1);
		assertThat(instance2Cache.getCached(zeCollection).getAuthorizationWithId(auth1).getDetails().getRoles())
				.containsOnly(Role.READ, Role.WRITE);

		services.execute(modifyAuthorizationOnRecord(auth1, record(TAXO1_CATEGORY2))
				.withNewPrincipalIds(bobId).withNewAccessAndRoles(Role.READ, Role.DELETE));

		assertThat(instance1Cache.getCached(zeCollection)).isNotNull();
		assertThat(instance2Cache.getCached(zeCollection)).isNotNull();
		assertThat(instance1Cache.getCached(zeCollection).getAuthorizationsOnTarget(TAXO1_CATEGORY2))
				.extracting("details.id").contains(auth1);
		assertThat(instance1Cache.getCached(zeCollection).getAuthorizationsToPrincipal(aliceId, false)).isEmpty();
		assertThat(instance1Cache.getCached(zeCollection).getAuthorizationsToPrincipal(bobId, false))
				.extracting("details.id").contains(auth1);
		assertThat(instance1Cache.getCached(zeCollection).getAuthorizationWithId(auth1).getDetails().getRoles())
				.containsOnly(Role.READ, Role.DELETE);

		assertThat(instance2Cache.getCached(zeCollection)).isNotNull();
		assertThat(instance2Cache.getCached(zeCollection).getAuthorizationsOnTarget(TAXO1_CATEGORY2))
				.extracting("details.id").contains(auth1);
		assertThat(instance2Cache.getCached(zeCollection).getAuthorizationsToPrincipal(aliceId, false)).isEmpty();
		assertThat(instance2Cache.getCached(zeCollection).getAuthorizationsToPrincipal(bobId, false))
				.extracting("details.id").contains(auth1);
		assertThat(instance2Cache.getCached(zeCollection).getAuthorizationWithId(auth1).getDetails().getRoles())
				.containsOnly(Role.READ, Role.DELETE);

		services.execute(modifyAuthorizationOnRecord(auth1, record(TAXO1_CATEGORY2)).removingItOnRecord());

		assertThat(instance1Cache.getCached(zeCollection)).isNotNull();
		assertThat(instance2Cache.getCached(zeCollection)).isNotNull();
		assertThat(instance1Cache.getCached(zeCollection).getAuthorizationsOnTarget(TAXO1_CATEGORY2))
				.extracting("details.id").doesNotContain(auth1);
		assertThat(instance1Cache.getCached(zeCollection).getAuthorizationsToPrincipal(aliceId, false))
				.extracting("details.id").doesNotContain(auth1);
		assertThat(instance1Cache.getCached(zeCollection).getAuthorizationWithId(auth1)).isNull();

		assertThat(instance2Cache.getCached(zeCollection)).isNotNull();
		assertThat(instance2Cache.getCached(zeCollection).getAuthorizationsOnTarget(TAXO1_CATEGORY2))
				.extracting("details.id").doesNotContain(auth1);
		assertThat(instance2Cache.getCached(zeCollection).getAuthorizationsToPrincipal(aliceId, false))
				.extracting("details.id").doesNotContain(auth1);
		assertThat(instance2Cache.getCached(zeCollection).getAuthorizationWithId(auth1)).isNull();

	}

	private void createAFolderOnInstance1() {

		Record record = new TestRecord(setup.folderSchema);
		record.set(setup.folderSchema.title(), aString());
		record.set(setup.folderSchema.parent(), FOLDER4);
		try {
			getModelLayerFactory().newRecordServices().add(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		recordServices.physicallyDeleteNoMatterTheStatus(record, User.GOD, new RecordPhysicalDeleteOptions());

	}

	private void createAFolderOnInstance2() {

		MetadataSchema schema = getModelLayerFactory("other-instance").getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchema("folder_default");
		RecordServices recordServices = getModelLayerFactory("other-instance").newRecordServices();
		Record record = new TestRecord(schema);
		record.set(schema.getMetadata("title"), aString());
		record.set(schema.getMetadata("parent"), FOLDER4);
		try {
			recordServices.add(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		recordServices.physicallyDeleteNoMatterTheStatus(record, User.GOD, new RecordPhysicalDeleteOptions());
	}

	private Condition<? super SingletonSecurityModel> containingAuthWithId(final String id) {
		return new Condition<SingletonSecurityModel>() {
			@Override
			public boolean matches(SingletonSecurityModel value) {
				assertThat(value).describedAs("Cached security model").isNotNull();
				assertThat(value.getAuthorizationWithId(id)).describedAs("Authorization with id '" + id + "'").isNotNull();
				return true;
			}
		};

	}

	public void setTimeToCalling(LocalDate newDate) {
		givenTimeIs(newDate);

		getModelLayerFactory().getModelLayerBackgroundThreadsManager()
				.getAuthorizationWithTimeRangeTokenUpdateBackgroundAction().run();
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
