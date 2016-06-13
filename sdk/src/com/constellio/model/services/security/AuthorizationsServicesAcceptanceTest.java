package com.constellio.model.services.security;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.REMOVED_AUTHORIZATIONS;
import static com.constellio.model.entities.security.CustomizedAuthorizationsBehavior.KEEP_ATTACHED;
import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER2;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER2_2_DOC1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER3;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER3_DOC1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER4;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER4_1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER4_1_DOC1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER4_2;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.FOLDER4_2_DOC1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.TAXO1_CATEGORY1;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.TAXO1_CATEGORY2;
import static com.constellio.model.services.security.SecurityAcceptanceTestSetup.TAXO1_CATEGORY2_1;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationBuilder;
import com.constellio.model.entities.security.global.AuthorizationModificationRequest;
import com.constellio.model.entities.security.global.AuthorizationModificationResponse;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidPrincipalsIds;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidTargetRecordsIds;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup.Records;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class AuthorizationsServicesAcceptanceTest extends ConstellioTest {
	String anotherCollection = "anotherCollection";
	SecurityAcceptanceTestSetup anothercollectionSetup = new SecurityAcceptanceTestSetup(anotherCollection);
	String ZE_ROLE = "zeRoleCode";
	String ZE_GROUP = "zeGroupCode";
	String zeUnusedRoleCode = "zeNotUsed";
	SecurityAcceptanceTestSetup setup = new SecurityAcceptanceTestSetup(zeCollection);
	MetadataSchemasManager schemasManager;
	SearchServices searchServices;
	RecordServices recordServices;
	TaxonomiesManager taxonomiesManager;
	CollectionsListManager collectionsListManager;
	AuthorizationsServices authorizationsServices;
	UserServices userServices;
	SchemasRecordsServices schemas;

	Records records;
	Records otherCollectionRecords;
	Users users = new Users();
	RolesManager roleManager;

	String ROLE1 = "role1";
	String ROLE2 = "role2";
	String ROLE3 = "role3";

	String PERMISSION_OF_NO_ROLE = "permissionOfNoRole";
	String PERMISSION_OF_ROLE1 = "permissionOfRole1";
	String PERMISSION_OF_ROLE2 = "permissionOfRole2";
	String PERMISSION_OF_ROLE1_AND_ROLE2 = "permissionOfRole1AndRole2";

	final String VERSION_HISTORY_READ = "VERSION_HISTORY_READ";
	final String VERSION_HISTORY = "VERSION_HISTORY";

	AuthorizationModificationResponse request1, request2, request3;

	List<String> initialFinishedBatchProcesses;

	String auth1, auth2, auth3, auth4, auth5;

	@Before
	public void setUp()
			throws Exception {

		customSystemPreparation(new CustomSystemPreparation() {
			@Override
			public void prepare() {

				givenCollection(zeCollection).withAllTestUsers();
				givenCollection(anotherCollection).withAllTestUsers();

				setServices();

				defineSchemasManager().using(setup);
				taxonomiesManager.addTaxonomy(setup.getTaxonomy1(), schemasManager);
				taxonomiesManager.addTaxonomy(setup.getTaxonomy2(), schemasManager);

				defineSchemasManager().using(anothercollectionSetup);
				taxonomiesManager.addTaxonomy(anothercollectionSetup.getTaxonomy1(), schemasManager);
				taxonomiesManager.addTaxonomy(anothercollectionSetup.getTaxonomy2(), schemasManager);

				RolesManager rolesManager = getModelLayerFactory().getRolesManager();
				rolesManager.addRole(
						new Role(zeCollection, ROLE1, "Ze role 1", asList(PERMISSION_OF_ROLE1, PERMISSION_OF_ROLE1_AND_ROLE2)));
				rolesManager.addRole(
						new Role(zeCollection, ROLE2, "Ze role 2", asList(PERMISSION_OF_ROLE2, PERMISSION_OF_ROLE1_AND_ROLE2)));
				rolesManager.addRole(new Role(zeCollection, ROLE3, "Ze role 3", new ArrayList<String>()));

				try {
					givenChuckNorrisSeesEverything();
					givenAliceCanModifyEverythingAndBobCanDeleteEverythingAndDakotaReadEverythingInAnotherCollection();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			private void setServices() {
				recordServices = getModelLayerFactory().newRecordServices();
				taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
				searchServices = getModelLayerFactory().newSearchServices();
				authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
				schemasManager = getModelLayerFactory().getMetadataSchemasManager();
				roleManager = getModelLayerFactory().getRolesManager();
				collectionsListManager = getModelLayerFactory().getCollectionsListManager();
				userServices = getModelLayerFactory().newUserServices();
				schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
				users.setUp(getModelLayerFactory().newUserServices());
			}

			@Override
			public void initializeFromCache() {
				setServices();
				setup.refresh(schemasManager);
				anothercollectionSetup.refresh(schemasManager);
			}
		});

		waitForBatchProcess();
		initialFinishedBatchProcesses = new ArrayList<>();
		for (BatchProcess batchProcess : getModelLayerFactory().getBatchProcessesManager().getFinishedBatchProcesses()) {
			initialFinishedBatchProcesses.add(batchProcess.getId());
		}

	}

	private ListAssert<String> assertThatBatchProcessDuringTest() {

		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		List<String> allBatchProcesses = new ArrayList<>();
		List<String> batchProcessesUsingTests = new ArrayList<>();
		List<BatchProcess> finishedBatchProcesses = getModelLayerFactory().getBatchProcessesManager().getFinishedBatchProcesses();
		for (BatchProcess batchProcess : finishedBatchProcesses) {
			allBatchProcesses.add(batchProcess.getId());
			if (!initialFinishedBatchProcesses.contains(batchProcess.getId())) {
				batchProcessesUsingTests.add(batchProcess.getQuery());
			}
		}

		initialFinishedBatchProcesses = allBatchProcesses;

		return assertThat(batchProcessesUsingTests);

	}

	private void givenTaxonomy1IsThePrincipalAndSomeRecords() {
		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, "taxo1");
		taxonomiesManager.setPrincipalTaxonomy(taxonomy, schemasManager);
		records = setup.givenRecords(recordServices);
		otherCollectionRecords = anothercollectionSetup.givenRecords(recordServices);
	}

	private void givenTaxonomy2IsThePrincipalAndSomeRecords() {
		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, "taxo2");
		taxonomiesManager.setPrincipalTaxonomy(taxonomy, schemasManager);
		records = setup.givenRecords(recordServices);
		otherCollectionRecords = anothercollectionSetup.givenRecords(recordServices);
	}

	@After
	public void checkIfChuckNorrisHasAccessToEverythingInZeCollection()
			throws Exception {

		List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.chuckNorrisIn(zeCollection));
		List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(
				users.chuckNorrisIn(zeCollection));
		List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
				users.chuckNorrisIn(zeCollection));

		assertThat(foldersWithReadFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithWriteFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithDeleteFound).containsOnly(records.allFoldersAndDocumentsIds().toArray(new String[0]));
	}

	@After
	public void checkIfAliceSeeAndCanModifyEverythingInCollection2()
			throws Exception {
		List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.aliceIn(anotherCollection));
		List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.aliceIn(anotherCollection));
		List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
				users.aliceIn(anotherCollection));

		assertThat(foldersWithReadFound).containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithWriteFound).containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithDeleteFound).hasSize(0);
	}

	@After
	public void checkIfBobSeeAndCanDeleteEverythingInCollection2()
			throws Exception {
		List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.bobIn(anotherCollection));
		List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.bobIn(anotherCollection));
		List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(users.bobIn(anotherCollection));

		assertThat(foldersWithReadFound).containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithWriteFound).hasSize(0);
		assertThat(foldersWithDeleteFound)
				.containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
	}

	@After
	public void checkIfDakotaSeeAndCanDeleteEverythingInCollection2()
			throws Exception {
		List<String> foldersWithReadFound = findAllFoldersAndDocuments(users.dakotaIn(anotherCollection));
		List<String> foldersWithWriteFound = findAllFoldersAndDocumentsWithWritePermission(users.dakotaIn(anotherCollection));
		List<String> foldersWithDeleteFound = findAllFoldersAndDocumentsWithDeletePermission(
				users.dakotaIn(anotherCollection));

		assertThat(foldersWithReadFound).containsOnly(otherCollectionRecords.allFoldersAndDocumentsIds().toArray(new String[0]));
		assertThat(foldersWithWriteFound).hasSize(0);
		assertThat(foldersWithDeleteFound).hasSize(0);
	}

	// TODO @Test Fix event logging
	public void givenBobHasReadAccessToCategory1ThenBobSeesFolder1AndFolder2()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		long eventsCount = fetchEventCount();
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		assertThat(fetchEventCount()).isEqualTo(eventsCount + 1);
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder2().getId(), records.folder2_1().getId(),
				records.folder2_2().getId(), records.folder1_doc1().getId(), records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId());
	}

	private RecordVerifier forRecord(String id) {
		return new RecordVerifier(id);
	}

	private class RecordVerifier {
		String recordId;

		public RecordVerifier(String recordId) {
			this.recordId = recordId;
		}

		public ListAssert<Object> usersWithRole(String role) {
			return assertThat(authorizationsServices.getUsersWithRoleForRecord(role, get(recordId)))
					.describedAs("users with role '" + role + "' on record '" + recordId + "'").extracting("username");
		}

		public ListAssert<Object> assertThatUsersWithPermission(String permission) {
			return assertThat(authorizationsServices.getUsersWithPermissionOnRecord(permission, get(recordId)))
					.describedAs("users with permission '" + permission + "' on record '" + recordId + "'")
					.extracting("username");
		}

		public ListAssert<String> usersWithReadAccess() {

			Record record = get(recordId);
			List<User> allUsers = userServices.getAllUsersInCollection(zeCollection);

			List<String> usersWithReadAccess = new ArrayList<>();
			for (User user : allUsers) {
				if (hasReadAccess(user, record)) {
					usersWithReadAccess.add(user.getUsername());
				}
			}

			return assertThat(usersWithReadAccess).describedAs("read access on record '" + recordId + "'");
		}

		public ListAssert<String> usersWithWriteAccess() {

			Record record = get(recordId);
			List<User> allUsers = userServices.getAllUsersInCollection(zeCollection);

			List<String> usersWithWriteAccess = new ArrayList<>();
			for (User user : allUsers) {
				if (hasWriteAccess(user, record)) {
					usersWithWriteAccess.add(user.getUsername());
				}
			}

			return assertThat(usersWithWriteAccess).describedAs("write access on record '" + recordId + "'");
		}

		public ListAssert<String> usersWithDeleteAccess() {

			Record record = get(recordId);
			List<User> allUsers = userServices.getAllUsersInCollection(zeCollection);

			List<String> usersWithDeleteAccess = new ArrayList<>();
			for (User user : allUsers) {
				if (hasDeleteAccess(user, record)) {
					usersWithDeleteAccess.add(user.getUsername());
				}
			}

			return assertThat(usersWithDeleteAccess).describedAs("delete access on record '" + recordId + "'");
		}

		public BooleanAssert detachedAuthorizationFlag() {
			Record record = get(recordId);
			return assertThat(Boolean.TRUE == record.get(Schemas.IS_DETACHED_AUTHORIZATIONS))
					.describedAs("detach authorization flag on record '" + recordId + "'");
		}
	}

	private boolean hasReadAccess(User user, Record record) {
		boolean hasAccessUsingWrapperMethod = user.hasReadAccess().on(record);

		boolean hasAccessUsingSearchTokens = searchServices.hasResults(new LogicalSearchQuery().filteredWithUser(user)
				.setCondition(fromAllSchemasIn(zeCollection).where(IDENTIFIER).isEqualTo(record)));

		if (hasAccessUsingWrapperMethod && !hasAccessUsingSearchTokens) {
			fail("User has read access using wrapper method, but not using search");
		}

		if (!hasAccessUsingWrapperMethod && hasAccessUsingSearchTokens) {
			fail("User has read access using search, but not using wrapper method");
		}
		return hasAccessUsingWrapperMethod;
	}

	private boolean hasWriteAccess(User user, Record record) {
		boolean hasAccessUsingWrapperMethod = user.hasWriteAccess().on(record);

		boolean hasAccessUsingSearchTokens = searchServices.hasResults(new LogicalSearchQuery().filteredWithUserWrite(user)
				.setCondition(fromAllSchemasIn(zeCollection).where(IDENTIFIER).isEqualTo(record)));

		if (hasAccessUsingWrapperMethod && !hasAccessUsingSearchTokens) {
			fail("User has read access using wrapper method, but not using search");
		}

		if (!hasAccessUsingWrapperMethod && hasAccessUsingSearchTokens) {
			fail("User has read access using search, but not using wrapper method");
		}
		return hasAccessUsingWrapperMethod;
	}

	private boolean hasDeleteAccess(User user, Record record) {
		boolean hasAccessUsingWrapperMethod = user.hasDeleteAccess().on(record);

		boolean hasAccessUsingSearchTokens = searchServices.hasResults(new LogicalSearchQuery().filteredWithUserDelete(user)
				.setCondition(fromAllSchemasIn(zeCollection).where(IDENTIFIER).isEqualTo(record)));

		if (hasAccessUsingWrapperMethod && !hasAccessUsingSearchTokens) {
			fail("User has read access using wrapper method, but not using search");
		}

		if (!hasAccessUsingWrapperMethod && hasAccessUsingSearchTokens) {
			fail("User has read access using search, but not using wrapper method");
		}
		return hasAccessUsingWrapperMethod;
	}

	private AuthorizationBuilder authorization() {
		return new AuthorizationBuilder(zeCollection);
	}

	private AuthorizationBuilder authorization(String existingAuthorizationId) {

		return new AuthorizationBuilder(zeCollection);
	}

	private AuthorizationBuilder authorizationForUsers(String... usernames) {

		User[] usersArray = new User[usernames.length];

		for (int i = 0; i < usernames.length; i++) {
			usersArray[i] = userServices.getUserInCollection(usernames[i], zeCollection);
		}

		return new AuthorizationBuilder(zeCollection).forUsers(usersArray);
	}

	private AuthorizationBuilder authorizationForGroups(String... groups) {

		Group[] groupsArray = new Group[groups.length];

		for (int i = 0; i < groups.length; i++) {
			groupsArray[i] = userServices.getGroupInCollection(groups[i], zeCollection);
		}

		return new AuthorizationBuilder(zeCollection).forGroups(groupsArray);
	}

	private AuthorizationBuilder authorizationForUser(String username) {

		User[] usersArray = new User[] { userServices.getUserInCollection(username, zeCollection) };
		return new AuthorizationBuilder(zeCollection).forUsers(usersArray);
	}

	private AuthorizationBuilder authorizationForGroup(String group) {

		Group[] groupsArray = new Group[] { userServices.getGroupInCollection(group, zeCollection) };
		return new AuthorizationBuilder(zeCollection).forGroups(groupsArray);
	}

	private long fetchEventCount() {
		LogicalSearchCondition condition = from(schemasManager.getSchemaTypes(zeCollection).getSchemaType(Event.SCHEMA_TYPE))
				.returnAll();
		return searchServices.getResultsCount(condition);
	}

	private RecordVerifier verifyRecord(String id) {
		return new RecordVerifier(id);
	}

	private List<RecordVerifier> $(String... ids) {
		List<RecordVerifier> verifiers = new ArrayList<>();

		for (String id : ids) {
			verifiers.add(new RecordVerifier(id));
		}

		return verifiers;
	}

	private VerifiedAuthorization authOnRecord(String recordId) {
		return new VerifiedAuthorization(recordId);
	}

	private class VerifiedAuthorization {

		String recordId;

		Set<String> principals;

		Set<String> removedOnRecords = new HashSet<>();

		List<String> roles;

		LocalDate start;

		LocalDate end;

		private VerifiedAuthorization(String recordId) {
			this.recordId = recordId;
		}

		private VerifiedAuthorization forPrincipals(String... principals) {
			this.principals = new HashSet<>(asList(principals));
			return this;
		}

		private VerifiedAuthorization forPrincipalIds(List<String> principals) {
			this.principals = new HashSet<>(toPrincipalCodes(principals.toArray(new String[] {})));
			return this;
		}

		private VerifiedAuthorization removedOnRecords(String... removedOnRecords) {
			this.removedOnRecords = new HashSet<>(asList(removedOnRecords));
			return this;
		}

		private VerifiedAuthorization givingRoles(String... roles) {
			this.roles = asList(roles);
			return this;
		}

		private VerifiedAuthorization givingRead() {
			this.roles = asList(READ);
			return this;
		}

		private VerifiedAuthorization givingReadWrite() {
			this.roles = asList(READ, WRITE);
			return this;
		}

		private VerifiedAuthorization givingReadWriteDelete() {
			this.roles = asList(READ, WRITE, DELETE);
			return this;
		}

		private VerifiedAuthorization startingOn(LocalDate start) {
			this.start = start;
			return this;
		}

		private VerifiedAuthorization endingOn(LocalDate end) {
			this.end = end;
			return this;
		}

		public String getRecordId() {
			return recordId;
		}

		public Set<String> getPrincipals() {
			return principals;
		}

		public Set<String> getRemovedOnRecords() {
			return removedOnRecords;
		}

		public List<String> getRoles() {
			return roles;
		}

		public LocalDate getStart() {
			return start;
		}

		public LocalDate getEnd() {
			return end;
		}

		@Override
		public String toString() {
			return "VerifiedAuthorization{" +
					"recordId='" + recordId + '\'' +
					", principals=" + principals +
					", removedOnRecords=" + removedOnRecords +
					", roles=" + roles +
					", start=" + start +
					", end=" + end +
					'}';
		}
	}

	private ListAssert<VerifiedAuthorization> assertThatAllAuthorizations() {

		List<VerifiedAuthorization> authorizations = new ArrayList<>();
		for (AuthorizationDetails details : getModelLayerFactory().getAuthorizationDetailsManager()
				.getAuthorizationsDetails(zeCollection).values()) {
			Authorization authorization = authorizationsServices.getAuthorization(zeCollection, details.getId());

			List<String> removedOnRecords = searchServices.searchRecordIds(fromAllSchemasIn(zeCollection).where(
					REMOVED_AUTHORIZATIONS).isEqualTo(authorization.getDetail().getId()));

			authorizations.add(authOnRecord(authorization.getGrantedOnRecord())
					.forPrincipalIds(authorization.getGrantedToPrincipals())
					.givingRoles(details.getRoles().toArray(new String[0]))
					.removedOnRecords(removedOnRecords.toArray(new String[0])));
		}
		return assertThat(authorizations).usingFieldByFieldElementComparator();

	}

	private class AuthorizationVerifier {

		String authId;

		public AuthorizationVerifier(String authId) {
			this.authId = authId;
		}

		public AuthorizationVerifier isDeleted() {
			assertThat(getModelLayerFactory().getAuthorizationDetailsManager().get(zeCollection, authId))
					.describedAs("Authorization supposed to be deleted").isNull();
			return this;
		}

		public AuthorizationVerifier isTargetting(String recordId) {
			return this;
		}

		public AuthorizationVerifier isOnlyRemovedOn(String... recordIds) {
			return this;
		}

		public AuthorizationVerifier hasPrincipals(String... principals) {
			List<String> expectedPrincipals = toPrincipalIds(principals);
			com.constellio.model.entities.security.Authorization authorization = authorizationsServices
					.getAuthorization(zeCollection, authId);
			assertThat(authorization.getGrantedToPrincipals()).describedAs("principals")
					.containsOnly(expectedPrincipals.toArray(new String[0]));
			return this;
		}

	}

	private List<String> toPrincipalCodes(String... principalIds) {
		List<String> codes = new ArrayList<>();
		for (String principalId : principalIds) {
			Record record = recordServices.getDocumentById(principalId);
			if (record.getSchemaCode().startsWith("user")) {
				codes.add(schemas.wrapUser(record).getUsername());
			} else {
				codes.add(schemas.wrapGroup(record).getCode());
			}
		}

		return codes;
	}

	private List<String> toPrincipalIds(String... principals) {
		List<String> ids = new ArrayList<>();
		for (String principal : principals) {
			try {
				ids.add(userServices.getUserInCollection(zeCollection, principal).getId());
			} catch (Exception e) {
				ids.add(userServices.getGroupInCollection(zeCollection, principal).getId());
			}
		}
		return ids;
	}

	private AuthorizationVerifier assertThatAuth(String id) {
		return new AuthorizationVerifier(id);
	}

	private UserAction givenUser(String username) {
		return new UserAction(username);
	}

	private class UserAction {

		String username;

		public UserAction(String username) {
			this.username = username;
		}

		public UserAction isRemovedFromGroup(String group) {
			userServices.addUpdateUserCredential(userServices.getUser(username).withRemovedGlobalGroup(group));
			try {
				waitForBatchProcess();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return this;
		}

		public UserAction isAddedInGroup(String group) {
			userServices.addUpdateUserCredential(userServices.getUser(username).withNewGlobalGroup(group));
			try {
				waitForBatchProcess();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return this;
		}
	}

	private AuthorizationModificationRequest authorizationOnRecord(String authorizationId, String recordId) {
		return new AuthorizationModificationRequest(authorizationId, recordId, zeCollection);
	}

	private AuthorizationModificationResponse modify(AuthorizationModificationRequest request) {
		return authorizationsServices.execute(request);
	}

	private Condition<? super AuthorizationModificationResponse> deleted() {
		return new Condition<AuthorizationModificationResponse>() {
			@Override
			public boolean matches(AuthorizationModificationResponse value) {
				return value.isAuthorizationDeleted();
			}
		};
	}

	private Condition<? super AuthorizationModificationResponse> creatingACopy() {
		return new Condition<AuthorizationModificationResponse>() {
			@Override
			public boolean matches(AuthorizationModificationResponse value) {
				return value.getIdOfAuthorizationCopy() != null;
			}
		};
	}

	//Notes :
	//TODO TestgetUsersWithPermission

	@Test
	//Case 1
	public void givenRoleAuthorizationsOnPrincipalConceptsThenInheritedInHierarchy()
			throws Exception {

		//Replacing
		// givenBobHasReadRoleOnCategory1WhenGettingUsersWithReadRoleOnRecordThenBobReturned
		// givenLegendsHaveReadRoleOnCategory1WhenGettingUsersWithReadRoleOnRecordThenAliceAndEdouardReturned
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth3 = addKeepingAttached(authorizationForUser(alice).on(TAXO1_CATEGORY2_1).giving(ROLE1));

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1).forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1).forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingRoles(ROLE1).forPrincipals(alice)
		);

		//TODO Bug! Robin should have ROLE1
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithRole(ROLE1).containsOnly(bob, charles, dakota, gandalf);
			verifyRecord.usersWithRole(ROLE2).isEmpty();
			verifyRecord.usersWithRole(ROLE3).isEmpty();
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithRole(ROLE1).containsOnly(bob, alice, charles, dakota, gandalf);
			verifyRecord.usersWithRole(ROLE2).isEmpty();
			verifyRecord.usersWithRole(ROLE3).isEmpty();
			verifyRecord.usersWithWriteAccess().containsOnly(chuck);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}
		assertThatBatchProcessDuringTest().hasSize(7);
	}

	//TODO Support this usecase @Test
	//Case 2
	public void givenRolesOfAuthorizationAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).giving(ROLE1));
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).giving(ROLE1));

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).withNewAccessAndRoles(ROLE2)))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2).withNewAccessAndRoles(ROLE1, ROLE3)))
				.isNot(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE2).forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRoles(ROLE1, ROLE3).forPrincipals(heroes)
		);

		//TODO Bug! Robin should have ROLE1 and ROLE3
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER3_DOC1, FOLDER4_1_DOC1, FOLDER4_2)) {
			verifyRecord.usersWithRole(ROLE1).containsOnly(bob, charles, dakota, gandalf);
			verifyRecord.usersWithRole(ROLE2).containsOnly(bob);
			verifyRecord.usersWithRole(ROLE3).containsOnly(bob, charles, dakota, gandalf);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		assertThatBatchProcessDuringTest().hasSize(7);
	}

	@Test
	//Case 4
	public void givenAccessAuthorizationsOnPrincipalConceptsThenInheritedInHierarchy()
			throws Exception {

		//Replacing
		//- givenBobHasReadRoleOnCategory1WhenGettingUsersWithReadRoleOnRecordThenBobReturned
		//- whenAddingAndRemovingAuthorizationToAGroupThenAppliedToAllUsers
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth3 = addKeepingAttached(authorizationForUser(alice).on(TAXO1_CATEGORY2_1).givingReadWriteAccess());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingReadWrite().forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingReadWrite().forPrincipals(alice)
		);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1, FOLDER4_1_DOC1, FOLDER4_2, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, charles, dakota, gandalf, chuck, robin);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(bob, alice, charles, dakota, chuck, robin, gandalf);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		assertThatBatchProcessDuringTest().hasSize(7);
	}

	//TODO Support this usecase @Test
	//Case 5
	public void givenAccessTypesOfAuthorizationAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2)) {
			verifyRecord.usersWithDeleteAccess().isEmpty();
			verifyRecord.usersWithWriteAccess().isEmpty();
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
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, gandalf, bob, robin);
			verifyRecord.usersWithDeleteAccess().containsOnly(bob);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		assertThatBatchProcessDuringTest().hasSize(7);
	}

	@Test
	//Case 7
	public void givenPrincipalsAreModifiedOnSameRecordOfAuthorizationThenNotDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadWriteAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

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
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2_1).withNewPrincipalIds(robin)))
				.is(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2_1).withNewPrincipalIds(legends, bob)))
				.is(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob).removedOnRecords(TAXO1_CATEGORY2_1),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(heroes).removedOnRecords(TAXO1_CATEGORY2_1),
				authOnRecord(TAXO1_CATEGORY2_1).givingRead().forPrincipals(robin),
				authOnRecord(TAXO1_CATEGORY2_1).givingRead().forPrincipals(legends, bob)
		);

		//TODO Bug! Robin should have ROLE3
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, gandalf, robin, bob, alice);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob, robin);
			verifyRecord.usersWithWriteAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

	}

	@Test
	public void givenPrincipalsAreModifiedOnRecordOfAuthorizationDetachingThenDuplicatedAndInstantaneousEffectOnSecurity()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		request1 = modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2_1).withNewPrincipalIds(robin).detaching());
		assertThat(request1).is(creatingACopy()).isNot(deleted());

		assertThatAllAuthorizations().containsOnly(
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(bob),
				authOnRecord(TAXO1_CATEGORY2).givingRead().forPrincipals(heroes),
				authOnRecord(TAXO1_CATEGORY2_1).givingRead().forPrincipals(robin),
				authOnRecord(TAXO1_CATEGORY2_1).givingRead().forPrincipals(legends, bob)
		);

		assertThatAuth(auth1).hasPrincipals(bob);
		assertThatAuth(auth2).hasPrincipals(heroes);
		assertThatAuth(request1.getIdOfAuthorizationCopy()).hasPrincipals(robin);
		assertThatAuth(request2.getIdOfAuthorizationCopy()).hasPrincipals(legends, bob);

		//TODO Bug! Robin should have ROLE3
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, gandalf, robin, bob, alice);
			verifyRecord.detachedAuthorizationFlag().isFalse();
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob, robin);
			verifyRecord.usersWithWriteAccess().containsOnly(sasquatch, gandalf, edouard, alice, bob);
			if (verifyRecord.recordId.equals(TAXO1_CATEGORY2_1)) {
				verifyRecord.detachedAuthorizationFlag().isTrue();
			} else {
				verifyRecord.detachedAuthorizationFlag().isFalse();
			}
		}

	}

	@Test
	public void givenAuthRemovedOnRecordOfAuthorizationThenDeletedAndRemovedOnAllRecordHierarchy()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2).removingItOnRecord()))
				.isNot(creatingACopy()).is(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2).removingItOnRecord()))
				.isNot(creatingACopy()).is(deleted());

		assertThatAuth(auth1).isDeleted();
		assertThatAuth(auth2).isDeleted();

		//TODO Bug! Robin should have ROLE3
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, TAXO1_CATEGORY2_1, FOLDER4, FOLDER4_1_DOC1, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(alice);
		}

	}

	@Test
	public void givenAuthDisabledOnRecordInheritingItThenNotDeletedAndDisabledOnAllRecordHierarchy()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2_1).removingItOnRecord()))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2_1).removingItOnRecord()))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth1, FOLDER4_1).removingItOnRecord()))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, FOLDER4_1).removingItOnRecord()))
				.isNot(creatingACopy()).isNot(deleted());

		assertThatAuth(auth1).isTargetting(TAXO1_CATEGORY2).isOnlyRemovedOn(TAXO1_CATEGORY2_1, FOLDER4_1);
		assertThatAuth(auth2).isTargetting(TAXO1_CATEGORY2).isOnlyRemovedOn(TAXO1_CATEGORY2_1, FOLDER4_1);

		//TODO Bug! Robin should have ROLE3
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, gandalf, robin, bob, alice);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1, FOLDER4_1, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(alice);
		}

	}

	@Test
	public void givenAuthDisabledByOnRecordInheritingItThenNotDeletedAndDisabledOnAllRecordHierarchy()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		auth1 = addKeepingAttached(authorizationForUser(bob).on(TAXO1_CATEGORY2).givingReadAccess());
		auth2 = addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY2).givingReadAccess());

		assertThat(modify(authorizationOnRecord(auth1, TAXO1_CATEGORY2_1).removingItOnRecord().detaching()))
				.isNot(creatingACopy()).isNot(deleted());
		assertThat(modify(authorizationOnRecord(auth2, TAXO1_CATEGORY2_1).removingItOnRecord().detaching()))
				.isNot(creatingACopy()).isNot(deleted());

		//TODO Bug! Robin should have ROLE3
		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2, FOLDER4, FOLDER4_1_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(charles, dakota, gandalf, robin, bob, alice);
		}

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY2_1, FOLDER3, FOLDER3_DOC1)) {
			verifyRecord.usersWithReadAccess().containsOnly(alice);
		}

	}

	@Test
	public void givenGroupAuthorizationsWhenAddOrRemoveUsersInGroupThenInstantaneousEffectOnSecurity()
			throws Exception {

		//Replacing
		//- whenAddingAndRemovingGroupToAUserThenHeReceivesAndLoseGroupAuthorizations
		//- whenAddingAndRemovingUserToAGroupThenHeReceivesAndLoseGroupAuthorizations

		givenTaxonomy1IsThePrincipalAndSomeRecords();

		addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY1).givingReadWriteAccess());
		addKeepingAttached(authorizationForGroup(heroes).on(TAXO1_CATEGORY1).giving(ROLE1));
		addKeepingAttached(authorizationForGroup(heroes).on(FOLDER4).givingReadWriteDeleteAccess());
		addKeepingAttached(authorizationForGroup(heroes).on(FOLDER4).giving(ROLE2));

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER1, FOLDER2, FOLDER2_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(charles, dakota, gandalf, chuck, robin);
			//TODO Bug : Robin expected
			verifyRecord.usersWithRole(ROLE1).containsOnly(charles, dakota, gandalf);
		}

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(charles, dakota, gandalf, chuck, robin);

			//TODO Bug : Robin expected
			verifyRecord.usersWithRole(ROLE2).containsOnly(charles, dakota, gandalf);
		}

		assertThatBatchProcessDuringTest().hasSize(12);

		givenUser(charles).isRemovedFromGroup(heroes);
		givenUser(robin).isRemovedFromGroup(sidekicks);
		givenUser(sasquatch).isAddedInGroup(heroes);
		givenUser(edouard).isAddedInGroup(sidekicks);

		for (RecordVerifier verifyRecord : $(TAXO1_CATEGORY1, FOLDER1, FOLDER2, FOLDER2_2_DOC1)) {
			verifyRecord.usersWithWriteAccess().containsOnly(sasquatch, dakota, gandalf, chuck, edouard);
			//TODO Bug : Edouard expected
			verifyRecord.usersWithRole(ROLE1).containsOnly(sasquatch, dakota, gandalf);
		}

		for (RecordVerifier verifyRecord : $(FOLDER4, FOLDER4_1, FOLDER4_2_DOC1)) {
			verifyRecord.usersWithDeleteAccess().containsOnly(sasquatch, dakota, gandalf, chuck, edouard);

			//TODO Bug : Edouard expected
			verifyRecord.usersWithRole(ROLE2).containsOnly(sasquatch, dakota, gandalf);
		}

		assertThatBatchProcessDuringTest().hasSize(0);

	}

	@Test
	public void whenAddingAndRemovingAuthorizationToAGroupThenAppliedToAllUsers()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();

		GlobalGroup group = userServices.createGlobalGroup(
				"vilains", "Vilains", new ArrayList<String>(), null, GlobalGroupStatus.ACTIVE);
		userServices.addUpdateGlobalGroup(group);
		userServices.setGlobalGroupUsers("vilains", asList(users.bob()));

		assertThat(users.bobIn(zeCollection))
				.has(noAuthorizationsToRead(records.taxo1_category1(), records.folder1(), records.folder2()));

		List<String> roles = asList(READ);
		Authorization authorization = addAuthorizationWithoutDetaching(roles,
				asList(userServices.getGroupInCollection("vilains", zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		assertThat(users.bobIn(zeCollection))
				.has(authorizationsToRead(records.taxo1_category1(), records.folder1(), records.folder2()));

		authorizationsServices.delete(authorization.getDetail(), User.GOD);
		waitForBatchProcess();
		assertThat(users.bobIn(zeCollection))
				.has(noAuthorizationsToRead(records.taxo1_category1(), records.folder1(), records.folder2()));

	}

	@Test
	public void givenUserHasCollectionReadThenHasReadOnlyOnAnyRecord()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);
		recordServices.update(bob.setCollectionReadAccess(true).getWrappedRecord());

		assertThat(authorizationsServices.canRead(bob, records.folder1())).isTrue();
		assertThat(authorizationsServices.canWrite(bob, records.folder1())).isFalse();
		assertThat(authorizationsServices.canDelete(bob, records.folder1())).isFalse();
	}

	@Test
	public void givenUserHasCollectionWriteThenHasReadAndWriteOnlyOnAnyRecord()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);
		recordServices.update(bob.setCollectionWriteAccess(true).getWrappedRecord());

		assertThat(authorizationsServices.canRead(bob, records.folder1())).isTrue();
		assertThat(authorizationsServices.canWrite(bob, records.folder1())).isTrue();
		assertThat(authorizationsServices.canDelete(bob, records.folder1())).isFalse();
	}

	@Test
	public void givenUserHasCollectionDeleteThenHasReadAndDeleteOnAnyRecord()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);
		recordServices.update(bob.setCollectionDeleteAccess(true).getWrappedRecord());

		assertThat(authorizationsServices.canRead(bob, records.folder1())).isTrue();
		assertThat(authorizationsServices.canWrite(bob, records.folder1())).isFalse();
		assertThat(authorizationsServices.canDelete(bob, records.folder1())).isTrue();
	}

	@Test
	public void givenUserHasCollectionWriteAndDeleteThenHasAllAuthsOnAnyRecord()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);
		recordServices.update(bob.setCollectionWriteAccess(true).setCollectionDeleteAccess(true).getWrappedRecord());

		assertThat(authorizationsServices.canRead(bob, records.folder1())).isTrue();
		assertThat(authorizationsServices.canWrite(bob, records.folder1())).isTrue();
		assertThat(authorizationsServices.canDelete(bob, records.folder1())).isTrue();
	}

	@Test
	public void givenLegendsHaveReadAccessToStation2_1ThenTheySeeFolder2()
			throws Exception {
		givenTaxonomy2IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo2_station2_1().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2().getId(), records.folder2_1().getId(), records.folder2_2().getId(),
				records.folder2_2_doc1().getId(), records.folder2_2_doc2().getId());
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2().getId(), records.folder2_1().getId(), records.folder2_2().getId(),
				records.folder2_2_doc1().getId(), records.folder2_2_doc2().getId());
	}

	@Test
	public void givenHeroesHaveReadAccessToCategory2ThenTheySeeFolder3AndFolder4()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));

		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder3().getId(), records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_2().getId(), records.folder3_doc1().getId(), records.folder4_1_doc1().getId(),
				records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder3().getId(), records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_2().getId(), records.folder3_doc1().getId(), records.folder4_1_doc1().getId(),
				records.folder4_2_doc1().getId());
	}

	@Test
	public void givenHeroesHaveReadAccessToCategory1WhenModifyingAuthorizationsReplacincingHeroesByBobThenOnlyBobSeesFoldersFromCategory1()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorization = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();
		authorization.setGrantedToPrincipals(asList(users.bobIn(zeCollection).getId()));
		modifyAuthorizationWithoutDetaching(authorization);

		waitForBatchProcess();

		List<String> foundBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundBobRecords)
				.containsOnly(records.folder1().getId(), records.folder1_doc1().getId(), records.folder2().getId(),
						records.folder2_1().getId(), records.folder2_2().getId(), records.folder2_2_doc1().getId(),
						records.folder2_2_doc2().getId());
		assertThat(foundXavierRecords).isEmpty();
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void whenAddingAuthorizationWithoutPrincipalsAndTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, new ArrayList<String>(), new ArrayList<String>());
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void whenAddingAuthorizationWithoutPrincipalsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, new ArrayList<String>(), asList(records.taxo1_category1().getId()));
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void whenAddingAuthorizationWithoutTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()), new ArrayList<String>());
	}

	@Test(expected = InvalidPrincipalsIds.class)
	public void whenAddingAuthorizationWithInvalidPrincipalsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, asList("inexistentId"), asList(records.taxo1_category1().getId()));
	}

	@Test(expected = InvalidTargetRecordsIds.class)
	public void whenAddingAuthorizationWithInvalidTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList("inexistentId"));
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void givenAuthorizationWhenModifyingAuthorizationWithoutPrincipalsAndTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		authorization.setGrantedToPrincipals(new ArrayList<String>());
		authorization.setGrantedOnRecords(new ArrayList<String>());

		modifyAuthorizationWithoutDetaching(authorization);
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void givenAuthorizationWhenModifyingAuthorizationWithoutTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		authorization.setGrantedOnRecords(new ArrayList<String>());

		modifyAuthorizationWithoutDetaching(authorization);
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords.class)
	public void givenAuthorizationWhenModifyingAuthorizationWithoutPrincipalsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		authorization.setGrantedToPrincipals(new ArrayList<String>());

		modifyAuthorizationWithoutDetaching(authorization);
	}

	@Test(expected = InvalidPrincipalsIds.class)
	public void givenAuthorizationWhenModifyingAuthorizationWithInvalidPrincipalsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		authorization.setGrantedToPrincipals(asList("inexistentId"));

		modifyAuthorizationWithoutDetaching(authorization);
	}

	@Test(expected = InvalidTargetRecordsIds.class)
	public void givenAuthorizationWhenModifyingAuthorizationWithInvalidTargetRecordsThenValidationException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		authorization.setGrantedOnRecords(asList("inexistentId"));

		modifyAuthorizationWithoutDetaching(authorization);
	}

	@Test
	public void whenHeroesHaveAccessToCategory1And2ThenDakotaAndXavierSeeAllFolders()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId(), records.taxo1_category2().getId()));
		waitForBatchProcess();

		List<String> foundDakotaRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundDakotaRecords).containsOnly(records.folder1().getId(), records.folder1_doc1().getId(),
				records.folder2().getId(), records.folder2_1().getId(), records.folder2_2().getId(),
				records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId(), records.folder3().getId(), records.folder3_doc1().getId(),
				records.folder4().getId(),
				records.folder4_1().getId(), records.folder4_1_doc1().getId(), records.folder4_2().getId(),
				records.folder4_2_doc1().getId());
		assertThat(foundXavierRecords).isEqualTo(foundDakotaRecords);
	}

	@Test
	public void whenHeroesAndAliceHaveAccessToCategory2_1AndFolder4_2ThenXavierDakotaAndAliceSeeFolder4_2AndFolder3()
			throws Exception {

		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId(), users.aliceIn(zeCollection).getId()),
				asList(records.taxo1_category2_1().getId(), records.folder4_2().getId()));
		waitForBatchProcess();

		List<String> foundDakotaRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundAliceRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundDakotaRecords).containsOnly(records.folder3().getId(), records.folder3_doc1().getId(),
				records.folder4_2().getId(), records.folder4_2_doc1().getId());
		assertThat(foundDakotaRecords).isEqualTo(foundXavierRecords).isEqualTo(foundAliceRecords);
	}

	@Test
	public void givenHeroesHaveAccessToCategory1WhenModifyingAuhtorizationReplacingCategory1ByFolder4_2ThenXavierAndDakotaOnlySeeFolder4_2()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorization = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();
		authorization.setGrantedOnRecords(asList(records.folder4_2().getId()));
		modifyAuthorizationWithoutDetaching(authorization);
		waitForBatchProcess();

		List<String> foundDakotaRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundDakotaRecords).containsOnly(records.folder4_2().getId(), records.folder4_2_doc1().getId());
		assertThat(foundXavierRecords).isEqualTo(foundDakotaRecords);
	}

	@Test
	public void givenHeroesAndLegendHaveAccessToCategory2WhenAddBobAndRemoveHeroesFromFolder4AndResetItThenOnlyHeroesAndLegendHaveAccessToFolder4()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationHeroes = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(records.folder4().getId()));
		waitForBatchProcess();
		authorizationsServices.removeAuthorizationOnRecord(authorizationHeroes, records.folder4(),
				KEEP_ATTACHED);
		waitForBatchProcess();
		authorizationsServices.reset(records.folder4());
		waitForBatchProcess();

		List<String> foundHeroesRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundLegendsRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		List<String> foundBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundHeroesRecords).contains(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2().getId(), records.folder4_2_doc1().getId());
		assertThat(foundHeroesRecords).isEqualTo(foundLegendsRecords);
		assertThat(foundBobRecords).doesNotContain(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2().getId(), records.folder4_2_doc1().getId());
	}

	@Test
	public void givenHeroesAndLegendHaveAccessToCategory2WhenAddBobAndRemoveHeroesFromFolder4DetachingAndResetItThenOnlyHeroesAndLegendHaveAccessToFolder4()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationHeroes = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(records.folder4().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		authorizationsServices.removeAuthorizationOnRecord(authorizationHeroes, records.folder4(),
				CustomizedAuthorizationsBehavior.DETACH);
		waitForBatchProcess();
		authorizationsServices.reset(records.folder4());
		waitForBatchProcess();

		List<String> foundHeroesRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundLegendsRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		List<String> foundBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundHeroesRecords).contains(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2().getId(), records.folder4_2_doc1().getId());
		assertThat(foundHeroesRecords).isEqualTo(foundLegendsRecords);
		assertThat(foundBobRecords).doesNotContain(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2().getId(), records.folder4_2_doc1().getId());
	}

	@Test
	public void givenMultipleAuthorizationAddedAtSameMomentThenAllOk()
			throws RolesManagerRuntimeException, InterruptedException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationHeroes = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		List<Record> folders = asList(records.folder1(), records.folder2(), records.folder3(), records.folder4(),
				records.folder5());
		for (Record folder : folders) {
			addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.chuckNorrisIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.dakotaIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.edouardLechatIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.gandalfIn(zeCollection).getId()), asList(folder.getId()));
			addAuthorizationWithoutDetaching(roles, asList(users.charlesIn(zeCollection).getId()), asList(folder.getId()));
		}
		waitForBatchProcess();

		assertThat(findAllFoldersAndDocuments(users.aliceIn(zeCollection)))
				.contains(records.folder1().getId(), records.folder2().getId(),
						records.folder3().getId(), records.folder4().getId(), records.folder5().getId());
	}

	@Test
	public void givenHeroesAndLegendHaveAccessToFolder4AndBobToFolder4_1WhenRemoveHeroesFromFolder4_1AndResetFolder4AndAddHeroesToFolder4ThenHeroesSeeFolder4_1()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationHeroes = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(records.folder4_1().getId()));
		authorizationsServices.removeAuthorizationOnRecord(authorizationHeroes, records.folder4_1(),
				KEEP_ATTACHED);
		authorizationsServices.reset(records.folder4());
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()), asList(records.folder4().getId()));
		waitForBatchProcess();

		List<String> foundHeroesRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundHeroesRecords).contains(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId());
	}

	@Test
	public void givenHeroesAndLegendHaveAccessToFolder4AndBobToFolder4_1WhenRemoveHeroesDetachingFromFolder4_1AndResetFolder4AndAddHeroesToFolder4ThenHeroesDontSeeFolder4_1()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationHeroes = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(records.folder4_1().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		authorizationsServices.removeAuthorizationOnRecord(authorizationHeroes, records.folder4_1(),
				CustomizedAuthorizationsBehavior.DETACH);
		authorizationsServices.reset(records.folder4());
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()), asList(records.folder4().getId()));
		waitForBatchProcess();

		List<String> foundHeroesRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundHeroesRecords).doesNotContain(records.folder4_1().getId(), records.folder4_1_doc1().getId());
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotDetachConcept.class)
	public void whenTryToDetachConceptThenException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationDetaching(roles, asList(users.heroesIn(zeCollection).getId()), asList(records.taxo1_category2().getId()));
	}

	@Test
	public void givenHeroesAndLegendsHaveWriteAccessToFolder4AndHeroesAndBobHasReadAccessToFolder4WhenRemove1stAuthorizationsFromFolder4_1ThenHeroesAndBobOnlyHaveReadAccessAndLegendsHasNoAccess()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> writePermissionRoles = Arrays.asList(Role.WRITE);
		List<String> readPermissionRoles = Arrays.asList(READ);
		addAuthorizationWithoutDetaching(readPermissionRoles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		Authorization writeAuthorizationHeroesAndLegends = addAuthorizationWithoutDetaching(writePermissionRoles,
				asList(users.heroesIn(zeCollection).getId(), users.legendsIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(readPermissionRoles,
				asList(users.heroesIn(zeCollection).getId(), users.bobIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		authorizationsServices.removeAuthorizationOnRecord(writeAuthorizationHeroesAndLegends, records.folder4_1(),
				CustomizedAuthorizationsBehavior.DETACH);
		waitForBatchProcess();

		List<String> foundReadPermissionLegendsRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		List<String> foundReadPermissionHeroesRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundReadPermissionBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		List<String> foundWritePermissionLegendsRecords = findAllFoldersAndDocumentsWithWritePermission(
				users.aliceIn(zeCollection));
		List<String> foundWritePermissionHeroesRecords = findAllFoldersAndDocumentsWithWritePermission(
				users.charlesIn(zeCollection));
		List<String> foundWritePermissionBobRecords = findAllFoldersAndDocumentsWithWritePermission(users.bobIn(zeCollection));
		assertThat(foundReadPermissionLegendsRecords).containsOnly(records.folder4().getId(), records.folder4_2().getId(),
				records.folder4_2_doc1().getId());
		assertThat(foundWritePermissionLegendsRecords).containsOnly(records.folder4().getId(), records.folder4_2().getId(),
				records.folder4_2_doc1().getId());
		assertThat(foundWritePermissionHeroesRecords).containsOnly(records.folder4().getId(), records.folder4_2().getId(),
				records.folder4_2_doc1().getId());
		assertThat(foundWritePermissionBobRecords).isEmpty();
		assertThat(foundReadPermissionHeroesRecords).contains(records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2().getId(), records.folder4_2_doc1().getId()).isEqualTo(
				foundReadPermissionBobRecords);
	}

	@Test
	public void givenAliceAndBobHaveAccessToFolder4AndBobHasNoLongerAccessToFolder4_2AndCharlesHasAccessToFolder4_2WithoutDetachingWhenAddingDakotaAndDetachingThenOnlyAliceBobAndDakotaHaveAccessToFolder4_2()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList(records.folder4().getId()));
		Authorization authorizationBob = addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		authorizationsServices.removeAuthorizationOnRecord(authorizationBob, records.folder4_2(),
				KEEP_ATTACHED);
		addAuthorizationWithoutDetaching(roles, asList(users.charlesIn(zeCollection).getId()),
				asList(records.folder4_2().getId()));
		addAuthorizationDetaching(roles, asList(users.dakotaIn(zeCollection).getId()), asList(records.folder4_2().getId()));
		waitForBatchProcess();

		List<String> foundAliceRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		List<String> foundBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundDakotaRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundAliceRecords).contains(records.folder4_2().getId());
		assertThat(foundBobRecords).doesNotContain(records.folder4_2().getId());
		assertThat(foundXavierRecords).contains(records.folder4_2().getId());
		assertThat(foundDakotaRecords).contains(records.folder4_2().getId());
	}

	@Test
	@Ignore
	public void givenAliceAndBobHaveAccessToFolder4AndBobHasNoLongerAccessToFolder4_2AndXavierHasAccessToFolder4_2DetachingWhenRemovingAliceAndAddingDakotaToFolder4_2ThenXavierAndDakotaHaveAccessToFolder4_2()
			throws Exception {
		// This test used to rely on invalid behaviour of the AuthorizationServices when detaching
		// Also, I do not believe the case here represents anything we do (or want to do) in the application
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		Authorization authorizationAlice = addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		Authorization authorizationBob = addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		authorizationsServices.removeAuthorizationOnRecord(authorizationBob, records.folder4_2(),
				KEEP_ATTACHED);
		addAuthorizationDetaching(roles, asList(users.charlesIn(zeCollection).getId()), asList(records.folder4_2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		authorizationsServices.removeAuthorizationOnRecord(authorizationAlice, records.folder4_2(),
				KEEP_ATTACHED);
		addAuthorizationWithoutDetaching(roles, asList(users.dakotaIn(zeCollection).getId()),
				asList(records.folder4_2().getId()));
		waitForBatchProcess();

		List<String> foundAliceRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		List<String> foundBobRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		List<String> foundXavierRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		List<String> foundDakotaRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundAliceRecords).doesNotContain(records.folder4_2().getId());
		assertThat(foundBobRecords).doesNotContain(records.folder4_2().getId());
		assertThat(foundXavierRecords).contains(records.folder4_2().getId());
		assertThat(foundDakotaRecords).contains(records.folder4_2().getId());
	}

	@Test(expected = AuthorizationsServicesRuntimeException.CannotAddAuhtorizationInNonPrincipalTaxonomy.class)
	public void whenAddingAuthorizationOnAconceptOfASecondaryTaxonomyThenException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		addAuthorizationDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo2_station2_1().getId()));
	}

	@Test
	public void givenLegendsHaveAuthWhenAddingAuthToAliceThenAliceInheritsLegendsAuthsAlongsideHerOwn()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2_1().getId()));
		waitForBatchProcess();
		addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList(records.folder1().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords)
				.containsOnly(records.folder1().getId(), records.folder3().getId(), records.folder1_doc1().getId(),
						records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder3().getId(), records.folder3_doc1().getId());
	}

	@Test
	public void whenGetRecordsAuthorizationsThenObtainsAuthorizations()
			throws Exception {

		givenTaxonomy1IsThePrincipalAndSomeRecords();
		String legends = users.legendsIn(zeCollection).getId();
		String folder2 = records.folder2().getId();
		String folder4 = records.folder4().getId();
		String taxo1_category1 = records.taxo1_category1().getId();
		String heroes = users.heroesIn(zeCollection).getId();
		String dakota = users.dakotaIn(zeCollection).getId();
		String gandalf = users.gandalfIn(zeCollection).getId();
		String edouard = users.edouardLechatIn(zeCollection).getId();

		List<String> readRoles = asList(READ);
		List<String> writeRoles = asList(Role.WRITE);
		addAuthorizationWithoutDetaching(readRoles, asList(legends), asList(folder4));
		addAuthorizationWithoutDetaching(writeRoles, asList(heroes, dakota), asList(folder2, folder4));
		addAuthorizationWithoutDetaching(readRoles, asList(dakota), asList(taxo1_category1));
		waitForBatchProcess();

		assertThat(authorizationsServices.getRecordAuthorizations(get(folder2))).hasSize(2)
				.has(authorizationGrantingRolesOnTo(writeRoles, asList(folder2, folder4), asList(heroes, dakota)))
				.has(authorizationGrantingRolesOnTo(readRoles, asList(taxo1_category1), asList(dakota)));

		assertThat(authorizationsServices.getRecordAuthorizations(get(folder4))).hasSize(2)
				.has(authorizationGrantingRolesOnTo(readRoles, asList(folder4), asList(legends)))
				.has(authorizationGrantingRolesOnTo(writeRoles, asList(folder2, folder4), asList(heroes, dakota)));

		assertThat(authorizationsServices.getRecordAuthorizations(get(taxo1_category1))).hasSize(1)
				.has(authorizationGrantingRolesOnTo(readRoles, asList(taxo1_category1), asList(dakota)));

		assertThat(authorizationsServices.getRecordAuthorizations(get(legends))).hasSize(1)
				.has(authorizationGrantingRolesOnTo(readRoles, asList(folder4), asList(legends)));

		assertThat(authorizationsServices.getRecordAuthorizations(get(heroes))).hasSize(1)
				.has(authorizationGrantingRolesOnTo(writeRoles, asList(folder2, folder4), asList(heroes, dakota)));

		List<Authorization> authorizations = authorizationsServices.getRecordAuthorizations(get(dakota));
		System.out.println(authorizations);
		assertThat(authorizationsServices.getRecordAuthorizations(get(dakota))).hasSize(2)
				.has(authorizationGrantingRolesOnTo(writeRoles, asList(folder2, folder4), asList(heroes, dakota)))
				.has(authorizationGrantingRolesOnTo(readRoles, asList(taxo1_category1), asList(dakota)));

		assertThat(authorizationsServices.getRecordAuthorizations(get(gandalf))).hasSize(2)
				.has(authorizationGrantingRolesOnTo(readRoles, asList(folder4), asList(legends)))
				.has(authorizationGrantingRolesOnTo(writeRoles, asList(folder2, folder4), asList(heroes, dakota)));

		assertThat(authorizationsServices.getRecordAuthorizations(get(edouard))).hasSize(1)
				.has(authorizationGrantingRolesOnTo(readRoles, asList(folder4), asList(legends)));
	}

	private Condition<? super List<Authorization>> authorizationGrantingRolesOnTo(final List<String> roles,
			final List<String> records, final List<String> principals) {
		return new Condition<List<Authorization>>() {

			@Override
			public boolean matches(List<Authorization> value) {
				for (Authorization authorization : value) {

					try {

						assertThat(authorization.getDetail().getRoles()).containsAll(roles).hasSize(roles.size());
						assertThat(authorization.getGrantedToPrincipals()).containsAll(principals).hasSize(principals.size());
						assertThat(authorization.getGrantedOnRecords()).containsAll(records).hasSize(records.size());

						return true;
					} catch (Throwable e) {
						//Continue
					}
				}
				return false;
			}
		};
	}

	private Record get(String recordId) {
		return recordServices.getDocumentById(recordId);
	}

	@Test
	public void givenLegendsAndHeroesHaveAuthsWhenAddingAuthToGandalfThenGandalfInheritsBothGroupsAuthsAlongsideHisOwn()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.folder4().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()), asList(records.folder2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.gandalfIn(zeCollection).getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.gandalfIn(zeCollection));

		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder2().getId(), records.folder2_1().getId(),
				records.folder2_2().getId(), records.folder1_doc1().getId(), records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId(), records.folder4().getId(), records.folder4_1().getId(),
				records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
	}

	@Test
	public void givenBobHasReadAccessToFolder2_2_doc2ThenBobSeesOnlyFolder2_2_doc2()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.folder2_2_doc2().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2_2_doc2().getId());
	}

	@Test
	public void givenLegendsHaveAuthWhenAddingAuthToAliceAndRemovingAliceFromLegendsThenAliceLosesLegendsAuthsButKeepsHerOwn()
			throws Exception {

		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		String aliceId = users.aliceIn(zeCollection).getId();
		String legendsId = users.legendsIn(zeCollection).getId();

		addAuthorizationWithoutDetaching("ZeFirst", roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching("ZeSecond", roles, asList(users.aliceIn(zeCollection).getId()),
				asList(records.folder1().getId()));
		waitForBatchProcess();
		assertThat(users.aliceIn(zeCollection).getUserTokens()).containsOnly(
				"r__ZeFirst",
				"r__ZeSecond",
				"r_" + aliceId,
				"w_" + aliceId,
				"d_" + aliceId,
				"r_" + legendsId,
				"w_" + legendsId,
				"d_" + legendsId);
		userServices.addUpdateUserCredential(userServices.getUserCredential("alice").withRemovedGlobalGroup("legends"));

		User alice = users.aliceIn(zeCollection);
		assertThat(users.aliceIn(zeCollection).getUserTokens()).containsOnly(
				"r__ZeSecond",
				"r_" + aliceId,
				"w_" + aliceId,
				"d_" + aliceId);

		List<String> foundRecords = findAllFoldersAndDocuments(alice);
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder1_doc1().getId());
	}

	@Test
	public void givenLegendsHaveAuthWhenAddingAuthToAliceAndMovingAliceToHeroesThenAliceLosesLegendsAuthsButKeepsHerOwn()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()), asList(records.folder2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList(records.folder1().getId()));
		waitForBatchProcess();
		String heroes = users.heroesIn(zeCollection).getId();
		recordServices.update(users.aliceIn(zeCollection).setUserGroups(asList(heroes)));

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder2().getId(), records.folder2_1().getId(),
				records.folder2_2().getId(), records.folder1_doc1().getId(), records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId());
	}

	@Test
	public void givenBobHasReadAccessToStation2WhenRemovingBobsAuthOnFolder2ThenBobSeesOnlyFolder1()
			throws Exception {
		givenTaxonomy2IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo2_station2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		for (Authorization recordAuth : authorizationsServices.getRecordAuthorizations(records.folder2())) {
			if (recordAuth.getGrantedToPrincipals().contains(users.bobIn(zeCollection).getId())) {
				authorizationsServices.removeAuthorizationOnRecord(recordAuth, records.folder2(),
						CustomizedAuthorizationsBehavior.DETACH);
			}
		}
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder1_doc1().getId());
	}

	@Test
	public void givenBobHasReadAccessToStation2WhenRemovingAndReaddingBobsAuthOnFolder2ThenBobSeesFolder1AndFolder2()
			throws Exception {
		givenTaxonomy2IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo2_station2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		for (Authorization recordAuth : authorizationsServices.getRecordAuthorizations(records.folder2())) {
			if (recordAuth.getGrantedToPrincipals().contains(users.bobIn(zeCollection).getId())) {
				authorizationsServices.removeAuthorizationOnRecord(recordAuth, records.folder2(),
						CustomizedAuthorizationsBehavior.DETACH);
			}
		}
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()), asList(records.folder2().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder2().getId(), records.folder2_1().getId(),
				records.folder2_2().getId(), records.folder1_doc1().getId(), records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId());
	}

	@Test
	public void givenLegendsHaveAuthsInTheFutureThenAuthsNotActiveNow()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationForDates(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.taxo1_category1().getId()),
				new LocalDate(2032, 10, 22), new LocalDate(2033, 10, 22));
		waitForBatchProcess();

		recordServices.update(users.aliceIn(zeCollection).setUserGroups(new ArrayList<String>()));

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).isEmpty();
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).isEmpty();
	}

	@Test
	public void givenLegendsHaveAuthsInTheFutureWhenTimePassesToItsActivePeriodThenAuthsActive()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationForDates(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.taxo1_category1().getId()),
				new LocalDate(2032, 10, 22), new LocalDate(2033, 10, 22));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).isEmpty();
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).isEmpty();

		givenTimeIs(new LocalDate(2032, 12, 22));
		authorizationsServices.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).contains(records.folder1().getId(), records.folder2().getId());
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).contains(records.folder1().getId(), records.folder2().getId());
	}

	@Test
	public void givenLegendsHaveExpiredAuthsThenAuthsNotActiveNow()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		givenTimeIs(new LocalDate(2013, 10, 21));
		addAuthorizationForDates(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.taxo1_category1().getId()),
				new LocalDate(2013, 10, 22), new LocalDate(2017, 10, 22));

		waitForBatchProcess();
		assertThat(findAllFoldersAndDocuments(users.aliceIn(zeCollection))).isEmpty();
		assertThat(findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection))).isEmpty();

		givenTimeIs(new LocalDate(2013, 10, 23));
		//givenTimeIs(new LocalDate(2013, 10, 22));
		//TODO Authorization should become effective the first day!
		authorizationsServices.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();
		assertThat(findAllFoldersAndDocuments(users.aliceIn(zeCollection))).isNotEmpty();
		assertThat(findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection))).isNotEmpty();

		givenTimeIs(new LocalDate(2017, 10, 23));
		authorizationsServices.refreshActivationForAllAuths(collectionsListManager.getCollections());
		waitForBatchProcess();
		assertThat(findAllFoldersAndDocuments(users.aliceIn(zeCollection))).isEmpty();
		assertThat(findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection))).isEmpty();

	}

	@Test
	public void givenBobHasReadAccessToStation2ThenBobSeesFolder1AndFolder2()
			throws Exception {
		givenTaxonomy2IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo2_station2().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder1().getId(), records.folder2().getId(), records.folder2_1().getId(),
				records.folder2_2().getId(), records.folder1_doc1().getId(), records.folder2_2_doc1().getId(),
				records.folder2_2_doc2().getId());
	}

	@Test
	public void givenLegendsHaveReadAuthToFolder2WhenGivingWriteAuthToAliceThenEdouardReadsFolder2AndAliceWritesFolder2()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = Arrays.asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.legendsIn(zeCollection).getId()), asList(records.folder2().getId()));
		roles = Arrays.asList(Role.WRITE);
		addAuthorizationDetaching(roles, asList(users.aliceIn(zeCollection).getId()), asList(records.folder2().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2().getId(), records.folder2_1().getId(), records.folder2_2().getId(),
				records.folder2_2_doc1().getId(), records.folder2_2_doc2().getId());
		foundRecords = findAllFoldersAndDocumentsWithWritePermission(users.aliceIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder2().getId(), records.folder2_1().getId(), records.folder2_2().getId(),
				records.folder2_2_doc1().getId(), records.folder2_2_doc2().getId());
	}

	@Test
	public void givenHeroesAndAliceHaveAuthToCategory2AndEdouardHasAuthToCategory2_1ThenAllButBobSeeFolder3()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId(), users.aliceIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.edouardLechatIn(zeCollection).getId()),
				asList(records.taxo1_category2_1().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).contains(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).contains(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundRecords).contains(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundRecords).contains(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.gandalfIn(zeCollection));
		assertThat(foundRecords).contains(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).isEmpty();
	}

	@Test
	public void givenHeroesAndAliceHaveAuthToCategory2AndEdouardHasAuthToFolder4ThenAllButBobSeeFolder4()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId(), users.aliceIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(roles, asList(users.edouardLechatIn(zeCollection).getId()),
				asList(records.folder4().getId()));
		waitForBatchProcess();

		// List<String> foundRecords = findAllFoldersAndDocuments(users.chuckNorrisIn(zeCollection));
		// assertThat(foundRecords).containsOnly(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
		// records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());

		List<String> foundRecords = findAllFoldersAndDocuments(users.aliceIn(zeCollection));
		assertThat(foundRecords).contains(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.edouardLechatIn(zeCollection));
		assertThat(foundRecords).contains(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundRecords).contains(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundRecords).contains(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.gandalfIn(zeCollection));
		assertThat(foundRecords).contains(records.folder4().getId(), records.folder4_1().getId(), records.folder4_2().getId(),
				records.folder4_1_doc1().getId(), records.folder4_2_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.bobIn(zeCollection));
		assertThat(foundRecords).isEmpty();
	}

	@Test
	public void givenLegendsAndHeroesHaveAuthToCategory2WhenRemovingHeroesAuthAndAddingDakotaAuthOnCategory2ThenHeroesSeeFolder3ButNot4()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles,
				asList(users.legendsIn(zeCollection).getId(), users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		for (Authorization recordAuth : authorizationsServices.getRecordAuthorizations(records.folder4())) {
			if (recordAuth.getGrantedToPrincipals().contains(users.heroesIn(zeCollection).getId())) {
				authorizationsServices.removeAuthorizationOnRecord(recordAuth, records.folder4(),
						CustomizedAuthorizationsBehavior.DETACH);
			}
		}
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		addAuthorizationWithoutDetaching(roles, asList(users.dakotaIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();

		List<String> foundRecords = findAllFoldersAndDocuments(users.dakotaLIndienIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder3().getId(), records.folder3_doc1().getId());
		foundRecords = findAllFoldersAndDocuments(users.charlesIn(zeCollection));
		assertThat(foundRecords).containsOnly(records.folder3().getId(), records.folder3_doc1().getId());
	}

	@Test
	public void givenBobWithCollectionReadAccessOnlyHasGlobalRoleThenHasTheRolePermissionsOnTheCollectionRecords()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		Role version = getRoleWithPermissions(VERSION_HISTORY_READ);

		User bob = users.bobIn(zeCollection);
		bob.setCollectionReadAccess(true);
		bob.setUserRoles(asList(version.getCode()));

		this.saveRefreshRecord(asList(bob.getWrappedRecord()));

		assertThat(authorizationsServices.canRead(bob, records.taxo1_category1())).isTrue();
	}

	private Role getRoleWithPermissions(String operation) {

		Role role = new Role(zeCollection, "ze", "ze", Arrays.asList(operation));

		return role;
	}

	@Test
	public void givenBobOnlyHasGlobalRoleWithoutReadAccessThenHasNoPermissionsOnTheCollectionRecords()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);

		addAuthorizationWithoutDetaching(asList("zeRole"), asList(bob.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		assertThat(authorizationsServices.canRead(bob, records.taxo1_category1())).isFalse();
	}

	@Test
	public void givenBobOnlyHasGlobalRoleWithReadAccessThenHasNoPermissionsOnTheCollectionRecords()
			throws RolesManagerRuntimeException, RecordServicesException, InterruptedException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		Role version = getRoleWithPermissions(VERSION_HISTORY_READ);
		User bob = users.bobIn(zeCollection);
		bob.setUserRoles(asList(version.getCode()));

		this.saveRefreshRecord(asList(bob.getWrappedRecord()));

		assertThat(authorizationsServices.canRead(bob, records.taxo1_category1())).isFalse();
	}

	@Test
	public void givenBobHasCollectionReadAccessAndIsInAGroupWhichHasGlobalRoleWithoutReadAccessThenHasTheRolePermissionsOnTheCollectionRecords()
			throws RolesManagerRuntimeException, RecordServicesException, InterruptedException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		Group group = createGroup("HEROES");
		group.setRoles(asList("zeRole"));

		User bob = users.bobIn(zeCollection);
		bob.setCollectionReadAccess(true);
		bob.setUserGroups(asList(group.getId()));

		this.saveRefreshRecord(asList(bob.getWrappedRecord(), group.getWrappedRecord()));

		assertThat(authorizationsServices.canRead(bob, records.taxo1_category1())).isTrue();
	}

	@Test
	public void givenBobHasCollectionReadAccessAndAnAuthorizationGivingHimARoleWithoutReadAccessThenHasTheRolePermissionsOnTargetRecordsAndTheirDescendants()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		Role version = getRoleWithPermissions(VERSION_HISTORY);
		User bob = users.bobIn(zeCollection);
		bob.setCollectionReadAccess(true);

		addAuthorizationWithoutDetaching(asList("zeRole"), asList(bob.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		this.saveRefreshRecord(asList(bob.getWrappedRecord(), records.taxo1_category1(), records.folder1(), records.folder2()));

		assertThat(authorizationsServices.canRead(bob, records.taxo1_category1())).isTrue();
		assertThat(authorizationsServices.canRead(bob, records.folder1())).isTrue();
		assertThat(authorizationsServices.canRead(bob, records.folder2())).isTrue();
	}

	@Test
	public void givenBobHasCollectionReadAccessAndAnAuthorizationOnAGroupGivingHimARoleWithoutReadAccessThenHasTheRolePermissionsOnTargetRecordsAndTheirDescendants
			()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		Group group = createGroup("HEROES");

		User bob = users.bobIn(zeCollection);
		bob.setCollectionReadAccess(true);
		bob.setUserGroups(asList(group.getId()));

		addAuthorizationWithoutDetaching(asList("zeRole"), asList(group.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		this.saveRefreshRecord(asList(bob.getWrappedRecord(), group.getWrappedRecord(), records.taxo1_category1(),
				records.folder1(), records.folder2()));

		assertThat(authorizationsServices.canRead(bob, records.taxo1_category1())).isTrue();
		assertThat(authorizationsServices.canRead(bob, records.folder1())).isTrue();
		assertThat(authorizationsServices.canRead(bob, records.folder2())).isTrue();

	}

	@Test
	public void givenBobHasAnAuthorizationGivingHimARoleWithReadAccessHasTheHasTheRolePermissionsOnTargetRecordsAndTheirDescendants
			()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);

		addAuthorizationWithoutDetaching(asList("zeRole", READ), asList(bob.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		this.saveRefreshRecord(asList(bob.getWrappedRecord(), records.taxo1_category1(), records.folder1(), records.folder2()));

		assertThat(authorizationsServices.canRead(bob, records.folder1())).isTrue();
		assertThat(authorizationsServices.canRead(bob, records.folder2())).isTrue();

		assertThat(authorizationsServices.canRead(bob, records.taxo1_category1())).isTrue();
	}

	@Test
	public void givenBobHasAnAuthorizationOnAGroupGivingHimARoleWithReadAccessHasTheRolePermissionsOnTargetRecordsAndTheirDescendants
			()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		Group group = createGroup("HEROES");

		User bob = users.bobIn(zeCollection);
		bob.setCollectionReadAccess(true);
		bob.setUserGroups(asList(group.getId()));

		addAuthorizationWithoutDetaching(asList("zeRole"), asList(group.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		this.saveRefreshRecord(asList(bob.getWrappedRecord(), group.getWrappedRecord(), records.taxo1_category1(),
				records.folder1(), records.folder2()));

		assertThat(authorizationsServices.canRead(bob, records.folder1())).isTrue();
		assertThat(authorizationsServices.canRead(bob, records.folder2())).isTrue();

	}

	@Test
	public void givenBobHasAnAuthorizationGivingHimARoleWithoutReadAccessThenHasNoPermissionsOnTheCollectionRecords()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		User bob = users.bobIn(zeCollection);

		addAuthorizationWithoutDetaching(asList("zeRole"), asList(bob.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		this.saveRefreshRecord(asList(bob.getWrappedRecord()));

		assertThat(authorizationsServices.canRead(bob, records.taxo1_category1())).isFalse();
	}

	@Test
	public void givenBobHasAnAuthorizationOnAGroupGivingHimARoleWithoutReadAccessThenHasNoPermissionsOnTheCollectionRecords()
			throws RolesManagerRuntimeException, InterruptedException, RecordServicesException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		Group group = createGroup("HEROES");

		User bob = users.bobIn(zeCollection);
		// bob.setCollectionReadAccess(true);
		bob.setUserGroups(asList(group.getId()));

		addAuthorizationWithoutDetaching(asList("zeRole"), asList(group.getId()),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		this.saveRefreshRecord(asList(bob.getWrappedRecord(), group.getWrappedRecord()));

		assertThat(authorizationsServices.canRead(bob, records.taxo1_category1())).isFalse();
	}

	@Test
	public void givenUserWithDeletePermissionWhenHasDeletePermissionOnHierarchyThenReturnTrue()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		boolean hasDeletePermissionOnHierarchy = authorizationsServices.hasDeletePermissionOnHierarchy(users.bobIn(zeCollection),
				records.folder4());

		assertThat(hasDeletePermissionOnHierarchy).isTrue();
	}

	@Test
	public void givenNoDeletePermissionToUserWhenHasDeletePermissionOnHierarchyThenReturnFalse()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		boolean hasDeletePermissionOnHierarchy = authorizationsServices.hasDeletePermissionOnHierarchy(users.bobIn(zeCollection),
				records.folder4());

		assertThat(hasDeletePermissionOnHierarchy).isFalse();
	}

	@Test
	public void givenDeletePermissionToUserAndRemoveItFromSubFolderWhenHasDeletePermissionOnHierarchyThenReturnFalse()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		Authorization authorization = addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		authorizationsServices.removeAuthorizationOnRecord(authorization, records.folder4_2(),
				KEEP_ATTACHED);
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		boolean hasDeletePermissionOnHierarchy = authorizationsServices.hasDeletePermissionOnHierarchy(users.bobIn(zeCollection),
				records.folder4());

		assertThat(hasDeletePermissionOnHierarchy).isFalse();
	}

	@Test
	public void givenUserWithDeletePermissionWhenHasRestaurationPermissionOnHierarchyThenReturnTrue()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		recordServices.logicallyDelete(records.folder4(), users.bobIn(zeCollection));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		boolean hasRestaurationPermissionOnHierarchy = authorizationsServices.hasRestaurationPermissionOnHierarchy(
				users.bobIn(zeCollection), records.folder4());

		assertThat(hasRestaurationPermissionOnHierarchy).isTrue();
	}

	@Test
	public void givenNoDeletePermissionToUserWhenHasRestaurationOnHierarchyThenReturnFalse()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		recordServices.logicallyDelete(records.folder4(), users.bobIn(zeCollection));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		boolean hasDeletePermissionOnHierarchy = authorizationsServices.hasRestaurationPermissionOnHierarchy(
				users.aliceIn(zeCollection), records.folder4());

		assertThat(hasDeletePermissionOnHierarchy).isFalse();
	}

	@Test
	public void givenDeletePermissionToUserAndRemoveItFromSubFolderWhenHasRestaurationOnHierarchyThenReturnFalse()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		Authorization authorization = addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();

		recordServices.refresh(records.allRecords());
		recordServices.logicallyDelete(records.folder4(), users.bobIn(zeCollection));

		recordServices.refresh(records.allRecords());
		authorizationsServices.removeAuthorizationOnRecord(authorization, records.folder4_2(),
				KEEP_ATTACHED);
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		boolean hasDeletePermissionOnHierarchy = authorizationsServices
				.hasRestaurationPermissionOnHierarchy(users.bobIn(zeCollection),
						records.folder4());

		assertThat(hasDeletePermissionOnHierarchy).isFalse();
	}

	@Test
	public void givenUserWithDeletePermissionOnPrincipalConceptIncludingRecordsWhenHasDeletePermissionOnPrincipalConceptHierarchyThenReturnTrue
			()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		boolean hasDeletePermissionOnHierarchy = authorizationsServices.hasDeletePermissionOnPrincipalConceptHierarchy(
				users.bobIn(zeCollection), records.taxo1_category2(), true, schemasManager);

		assertThat(hasDeletePermissionOnHierarchy).isTrue();
	}

	@Test(expected = AuthorizationsServicesRuntimeException.RecordIsNotAConceptOfPrincipalTaxonomy.class)
	public void givenANotConceptRecordInAPrincipalTaxonomyIncludingRecordsWhenHasDeletePermissionOnPrincipalConceptHierarchyThenException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		authorizationsServices.hasDeletePermissionOnPrincipalConceptHierarchy(users.bobIn(zeCollection), records.folder2(), true,
				schemasManager);
	}

	@Test(expected = AuthorizationsServicesRuntimeException.RecordIsNotAConceptOfPrincipalTaxonomy.class)
	public void givenANotConceptRecordInASecondaryTaxonomyIncludingRecordsWhenHasDeletePermissionOnPrincipalConceptHierarchyThenException()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		authorizationsServices.hasDeletePermissionOnPrincipalConceptHierarchy(users.bobIn(zeCollection), records.folder1(), true,
				schemasManager);
	}

	@Test
	public void givenUserWithDeletePermissionOnPrincipalConceptAndWithoutPermissionToDeleteRecordsIncludingRecordsWhenHasDeletePermissionOnPrincipalConceptHierarchyThenReturnFalse
			()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		Authorization authorization = addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		authorizationsServices.removeAuthorizationOnRecord(authorization, records.folder3(),
				KEEP_ATTACHED);
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		boolean hasDeletePermissionOnHierarchy = authorizationsServices.hasDeletePermissionOnPrincipalConceptHierarchy(
				users.bobIn(zeCollection), records.taxo1_category2(), true, schemasManager);

		assertThat(hasDeletePermissionOnHierarchy).isFalse();
	}

	@Test
	public void givenUserWithDeletePermissionOnPrincipalConceptNotIncludingRecordsWhenHasDeletePermissionOnPrincipalConceptHierarchyThenReturnTrue
			()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		boolean hasDeletePermissionOnHierarchy = authorizationsServices.hasDeletePermissionOnPrincipalConceptHierarchy(
				users.bobIn(zeCollection), records.taxo1_category2(), false, schemasManager);

		assertThat(hasDeletePermissionOnHierarchy).isTrue();
	}

	@Test
	public void givenUserWithDeletePermissionOnPrincipalConceptAndWithoutPermissionToDeleteRecordsNotIncludingRecordsWhenHasDeletePermissionOnPrincipalConceptHierarchyThenReturnTrue
			()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(Role.WRITE, Role.DELETE);
		Authorization authorization = addAuthorizationWithoutDetaching(roles, asList(users.bobIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());
		authorizationsServices.removeAuthorizationOnRecord(authorization, records.folder4(),
				KEEP_ATTACHED);
		waitForBatchProcess();
		recordServices.refresh(records.allRecords());

		boolean hasDeletePermissionOnHierarchy = authorizationsServices.hasDeletePermissionOnPrincipalConceptHierarchy(
				users.bobIn(zeCollection), records.taxo1_category2(), false, schemasManager);

		assertThat(hasDeletePermissionOnHierarchy).isTrue();
	}

	@Test
	public void givenAddAuthorizationWhenGetAuthorizationThenReturnIt()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ);

		Authorization authorization = addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));
		waitForBatchProcess();
		String authId = authorization.getDetail().getId();

		Authorization retrievedAuthorization = authorizationsServices
				.getAuthorization(authorization.getDetail().getCollection(),
						authId);

		assertThat(authorization.getDetail()).isEqualToComparingFieldByField(retrievedAuthorization.getDetail());
		assertThat(authorization.getGrantedOnRecords()).isEqualTo(retrievedAuthorization.getGrantedOnRecords());
		assertThat(authorization.getGrantedToPrincipals()).isEqualTo(retrievedAuthorization.getGrantedToPrincipals());
	}

	@Test
	public void givenGroupHasAuthsThenAuthsInheritedToSubGroupAndItsUsers()
			throws InterruptedException {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		List<String> roles = asList(READ, Role.WRITE);

		assertThat(users.robinIn(zeCollection)).is(notAllowedToWrite(records.taxo1_category2()));

		addAuthorizationWithoutDetaching(roles, asList(users.heroesIn(zeCollection).getId()),
				asList(records.taxo1_category2().getId()));

		waitForBatchProcess();

		assertThat(users.robinIn(zeCollection)).is(allowedToWrite(records.taxo1_category2()));

	}

	@Test
	public void whenGetConceptsForWhichUserHasPermissionThenReturnTheGoodConcepts()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		String sasquatchId = users.sasquatchIn(zeCollection).getId();
		Role role1 = new Role(zeCollection, "role1", "First role", asList("operation1", "operation2"));
		Role role2 = new Role(zeCollection, "role2", "Second role", asList("operation2", "operation3"));
		Role role3 = new Role(zeCollection, "role3", "Third role", asList("operation3", "operation4"));

		roleManager.addRole(role1);
		roleManager.addRole(role2);
		roleManager.addRole(role3);

		getModelLayerFactory().newRecordServices().update(
				users.dakotaIn(zeCollection).setUserRoles(asList("role3")));

		addAuthorizationWithoutDetaching(asList("role1"), asList(sasquatchId),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(asList("role2"), asList(sasquatchId),
				asList(records.taxo1_fond1_1().getId()));
		waitForBatchProcess();

		User dakotaInZeCollection = users.dakotaIn(zeCollection);
		User sasquatchInZeCollection = users.sasquatchIn(zeCollection);
		User sasquatchInAnotherCollection = users.sasquatchIn(anotherCollection);

		String[] allConcepts =
				new String[] { records.taxo1_fond1().getId(), records.taxo1_fond1_1().getId(), records.taxo1_category1().getId(),
						records.taxo1_category1().getId(), records.taxo1_category2().getId(),
						records.taxo1_category2_1().getId() };

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation0", dakotaInZeCollection))
				.isEmpty();

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation1", dakotaInZeCollection))
				.isEmpty();

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation2", dakotaInZeCollection))
				.isEmpty();

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation3", dakotaInZeCollection))
				.containsOnly(allConcepts);

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation4", dakotaInZeCollection))
				.containsOnly(allConcepts);

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation0", sasquatchInZeCollection))
				.isEmpty();

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation1", sasquatchInZeCollection))
				.containsOnly(records.taxo1_category2().getId(), records.taxo1_category2_1().getId());

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation2", sasquatchInZeCollection))
				.containsOnly(records.taxo1_fond1_1().getId(), records.taxo1_category1().getId(),
						records.taxo1_category2().getId(), records.taxo1_category2_1().getId());

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation3", sasquatchInZeCollection))
				.containsOnly(records.taxo1_fond1_1().getId(), records.taxo1_category1().getId());

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation4", sasquatchInZeCollection))
				.isEmpty();

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation0", sasquatchInAnotherCollection))
				.isEmpty();

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation1", sasquatchInAnotherCollection))
				.isEmpty();

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation2", sasquatchInAnotherCollection))
				.isEmpty();

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation3", sasquatchInAnotherCollection))
				.isEmpty();

		assertThat(authorizationsServices.getConceptsForWhichUserHasPermission("operation4", sasquatchInAnotherCollection))
				.isEmpty();
	}

	@Test
	public void whenGetUsersWithGlobalPermissionThenReturnTheGoodUsers()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		String sasquatchId = users.sasquatchIn(zeCollection).getId();
		String robinId = users.robinIn(zeCollection).getId();
		String aliceId = users.aliceIn(zeCollection).getId();
		Role role1 = new Role(zeCollection, "role1", "First role", asList("operation1", "operation2"));
		Role role2 = new Role(zeCollection, "role2", "Second role", asList("operation2", "operation3"));
		Role role3 = new Role(zeCollection, "role3", "Third role", asList("operation3", "operation4"));

		roleManager.addRole(role1);
		roleManager.addRole(role2);
		roleManager.addRole(role3);

		getModelLayerFactory().newRecordServices().update(
				users.sasquatchIn(zeCollection).setUserRoles(asList("role1")));

		getModelLayerFactory().newRecordServices().update(
				users.robinIn(zeCollection).setUserRoles(asList("role2")));

		addAuthorizationWithoutDetaching(asList("role3"), asList(robinId),
				asList(records.taxo1_fond1().getId()));
		waitForBatchProcess();

		User alice = users.aliceIn(zeCollection);
		User sasquatch = users.sasquatchIn(zeCollection);
		User sasquatchInAnotherCollection = users.sasquatchIn(anotherCollection);
		User robin = users.robinIn(zeCollection);
		User robinInAnotherCollection = users.robinIn(anotherCollection);

		assertThat(authorizationsServices.getUsersWithGlobalPermissionInCollection("operation0", zeCollection))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithGlobalPermissionInCollection("operation1", zeCollection))
				.containsOnly(sasquatch);

		assertThat(authorizationsServices.getUsersWithGlobalPermissionInCollection("operation2", zeCollection))
				.containsOnly(sasquatch, robin);

		assertThat(authorizationsServices.getUsersWithGlobalPermissionInCollection("operation3", zeCollection))
				.containsOnly(robin);

		assertThat(authorizationsServices.getUsersWithGlobalPermissionInCollection("operation4", zeCollection))
				.isEmpty();
	}

	@Test
	public void whenGetUsersWithPermissionOnConceptExcludingInheritedAuthorizationsThenReturnTheGoodUsers()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		String sasquatchId = users.sasquatchIn(zeCollection).getId();
		String robinId = users.robinIn(zeCollection).getId();
		String aliceId = users.aliceIn(zeCollection).getId();
		Role role1 = new Role(zeCollection, "role1", "First role", asList("operation1", "operation2"));
		Role role2 = new Role(zeCollection, "role2", "Second role", asList("operation2", "operation3"));
		Role role3 = new Role(zeCollection, "role3", "Third role", asList("operation3", "operation4"));

		roleManager.addRole(role1);
		roleManager.addRole(role2);
		roleManager.addRole(role3);

		getModelLayerFactory().newRecordServices().update(
				users.aliceIn(zeCollection).setUserRoles(asList("role3")));

		addAuthorizationWithoutDetaching(asList("role1"), asList(sasquatchId),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(asList("role2"), asList(robinId),
				asList(records.taxo1_fond1().getId()));
		waitForBatchProcess();

		User alice = users.aliceIn(zeCollection);
		User sasquatch = users.sasquatchIn(zeCollection);
		User sasquatchInAnotherCollection = users.sasquatchIn(anotherCollection);
		User robin = users.robinIn(zeCollection);
		User robinInAnotherCollection = users.robinIn(anotherCollection);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation0",
				records.taxo1_fond1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation1",
				records.taxo1_fond1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation2",
				records.taxo1_fond1()))
				.containsOnly(robin);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation3",
				records.taxo1_fond1()))
				.containsOnly(robin);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation4",
				records.taxo1_fond1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation0",
				records.taxo1_fond1_1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation1",
				records.taxo1_fond1_1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation2",
				records.taxo1_fond1_1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation3",
				records.taxo1_fond1_1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation4",
				records.taxo1_fond1_1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation0",
				records.taxo1_category2()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation1",
				records.taxo1_category2()))
				.containsOnly(sasquatch);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation2",
				records.taxo1_category2()))
				.containsOnly(sasquatch);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation3",
				records.taxo1_category2()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations("operation4",
				records.taxo1_category2()))
				.isEmpty();
	}

	@Test
	public void whenGetUsersWithPermissionOnConceptThenReturnTheGoodUsers()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		String sasquatchId = users.sasquatchIn(zeCollection).getId();
		String robinId = users.robinIn(zeCollection).getId();
		String aliceId = users.aliceIn(zeCollection).getId();
		Role role1 = new Role(zeCollection, "role1", "First role", asList("operation1", "operation2"));
		Role role2 = new Role(zeCollection, "role2", "Second role", asList("operation2", "operation3"));
		Role role3 = new Role(zeCollection, "role3", "Third role", asList("operation3", "operation4"));

		roleManager.addRole(role1);
		roleManager.addRole(role2);
		roleManager.addRole(role3);

		getModelLayerFactory().newRecordServices().update(
				users.aliceIn(zeCollection).setUserRoles(asList("role3")));

		addAuthorizationWithoutDetaching(asList("role1"), asList(sasquatchId),
				asList(records.taxo1_category2().getId()));
		addAuthorizationWithoutDetaching(asList("role2"), asList(robinId),
				asList(records.taxo1_fond1().getId()));
		waitForBatchProcess();

		User alice = users.aliceIn(zeCollection);
		User sasquatch = users.sasquatchIn(zeCollection);
		User sasquatchInAnotherCollection = users.sasquatchIn(anotherCollection);
		User robin = users.robinIn(zeCollection);
		User robinInAnotherCollection = users.robinIn(anotherCollection);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation0", records.taxo1_fond1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation1", records.taxo1_fond1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation2", records.taxo1_fond1()))
				.containsOnly(robin);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation3", records.taxo1_fond1()))
				.containsOnly(robin, alice);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation4", records.taxo1_fond1()))
				.containsOnly(alice);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation0", records.taxo1_fond1_1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation1", records.taxo1_fond1_1()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation2", records.taxo1_fond1_1()))
				.containsOnly(robin);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation3", records.taxo1_fond1_1()))
				.containsOnly(robin, alice);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation4", records.taxo1_fond1_1()))
				.containsOnly(alice);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation0", records.taxo1_category2()))
				.isEmpty();

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation1", records.taxo1_category2()))
				.containsOnly(sasquatch);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation2", records.taxo1_category2()))
				.containsOnly(robin, sasquatch);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation3", records.taxo1_category2()))
				.containsOnly(robin, alice);

		assertThat(authorizationsServices.getUsersWithPermissionOnRecord("operation4", records.taxo1_category2()))
				.containsOnly(alice);
	}

	@Test
	public void givenUserHasAuthorizationsWithRolesThenHasTheRolesPermissionsOnRecords()
			throws Exception {
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		String sasquatchId = users.sasquatchIn(zeCollection).getId();
		Role role1 = new Role(zeCollection, "role1", "First role", asList("operation1", "operation2"));
		Role role2 = new Role(zeCollection, "role2", "Second role", asList("operation3", "operation4"));
		Role role3 = new Role(zeCollection, "role3", "Third role", asList("operation5", "operation6"));

		roleManager.addRole(role1);
		roleManager.addRole(role2);
		roleManager.addRole(role3);

		getModelLayerFactory().newRecordServices().update(
				users.sasquatchIn(zeCollection).setUserRoles(asList("role1")));

		addAuthorizationWithoutDetaching(asList("role2", READ), asList(sasquatchId),
				asList(records.taxo1_category1().getId()));
		waitForBatchProcess();

		User sasquatchInZeCollection = users.sasquatchIn(zeCollection);
		User sasquatchInAnotherCollection = users.sasquatchIn(anotherCollection);

		Record folder1Inside = refreshed(records.folder1());
		Record folder2Inside = refreshed(records.folder2());
		Record folder3Outside = refreshed(records.folder3());
		Record folder4Outside = refreshed(records.folder4());

		assertThat(sasquatchInZeCollection.has("operation1").onSomething()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation1").onSomething()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onSomething()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").onSomething()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation5").onSomething()).isFalse();
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

		assertThat(sasquatchInAnotherCollection.hasAny("operation1", "operation2", "operation3", "operation4",
				"operation5", "operation6", "otherPermission").globally()).isFalse();

		assertThat(userServices.has(sasquatch).globalPermissionInAnyCollection("operation1")).isTrue();
		assertThat(userServices.has(sasquatch).globalPermissionInAnyCollection("operation3")).isFalse();

	}

	// ---------------------------------------------------------------------------------------------------------

	private Condition<? super User> allowedToWrite(final Record record) {
		recordServices.refresh(record);
		return new Condition<User>() {
			@Override
			public boolean matches(User user) {
				assertThat(authorizationsServices.canWrite(user, record))
						.describedAs("can write '" + record.getId() + "'")
						.isTrue();
				return true;
			}
		};
	}

	private Condition<? super User> notAllowedToWrite(final Record record) {
		recordServices.refresh(record);
		return new Condition<User>() {
			@Override
			public boolean matches(User user) {
				assertThat(authorizationsServices.canWrite(user, record))
						.describedAs("can write '" + record.getId() + "'")
						.isFalse();
				return true;
			}
		};
	}

	private Record refreshed(Record record) {
		recordServices.refresh(record);
		return record;
	}

	private void saveRefreshRecord(List<Record> records)
			throws RecordServicesException, InterruptedException {
		Transaction transaction = new Transaction();
		for (Record oldRecord : records) {
			transaction.addUpdate(oldRecord);
		}

		recordServices.execute(transaction);
		waitForBatchProcess();

		recordServices.refresh(records);
	}

	private List<Record> findRecords(LogicalSearchCondition condition, User user) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		query.filteredWithUser(user);
		return searchServices.search(query);
	}

	private List<String> findAllFoldersAndDocumentsWithWritePermission(User user) {
		MetadataSchema folderSchema, documentSchema;
		if (user.getCollection().endsWith("zeCollection")) {
			folderSchema = setup.folderSchema.instance();
			documentSchema = setup.documentSchema.instance();
		} else {
			folderSchema = anothercollectionSetup.folderSchema.instance();
			documentSchema = anothercollectionSetup.documentSchema.instance();
		}

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(folderSchema).returnAll());
		query.filteredWithUserWrite(user);
		List<String> recordIds = searchServices.searchRecordIds(query);

		query.setCondition(from(documentSchema).returnAll());
		recordIds.addAll(searchServices.searchRecordIds(query));
		return recordIds;
	}

	private List<String> findAllFoldersAndDocumentsWithDeletePermission(User user) {
		MetadataSchema folderSchema, documentSchema;
		if (user.getCollection().endsWith("zeCollection")) {
			folderSchema = setup.folderSchema.instance();
			documentSchema = setup.documentSchema.instance();
		} else {
			folderSchema = anothercollectionSetup.folderSchema.instance();
			documentSchema = anothercollectionSetup.documentSchema.instance();
		}

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(folderSchema).returnAll());
		query.filteredWithUserDelete(user);
		List<String> recordIds = searchServices.searchRecordIds(query);

		query.setCondition(from(documentSchema).returnAll());
		recordIds.addAll(searchServices.searchRecordIds(query));
		return recordIds;
	}

	private List<String> findAllFoldersAndDocuments(User user) {
		List<String> recordIds = new ArrayList<>();

		MetadataSchema folderSchema, documentSchema;
		if (user.getCollection().endsWith("zeCollection")) {
			folderSchema = setup.folderSchema.instance();
			documentSchema = setup.documentSchema.instance();
		} else {
			folderSchema = anothercollectionSetup.folderSchema.instance();
			documentSchema = anothercollectionSetup.documentSchema.instance();
		}
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(folderSchema).returnAll());
		query.filteredWithUser(user);
		recordIds.addAll(searchServices.searchRecordIds(query));

		query = new LogicalSearchQuery();
		query.setCondition(from(documentSchema).returnAll());
		query.filteredWithUser(user);
		recordIds.addAll(searchServices.searchRecordIds(query));
		return recordIds;
	}

	private Authorization addAuthorizationWithoutDetaching(String role, List<String> grantedToPrincipals,
			List<String> grantedOnRecords) {
		return addAuthorizationWithoutDetaching(aString(), asList(role), grantedToPrincipals, grantedOnRecords);
	}

	private Authorization addAuthorizationWithoutDetaching(List<String> roles, List<String> grantedToPrincipals,
			List<String> grantedOnRecords) {
		return addAuthorizationWithoutDetaching(aString(), roles, grantedToPrincipals, grantedOnRecords);
	}

	private Authorization addAuthorizationWithoutDetaching(String id, List<String> roles, List<String> grantedToPrincipals,
			List<String> grantedOnRecords) {
		AuthorizationDetails details = AuthorizationDetails.create(id, roles, null, null, zeCollection);

		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);

		authorizationsServices.add(authorization, KEEP_ATTACHED, users.dakotaLIndienIn(zeCollection));
		return authorization;
	}

	private String addKeepingAttached(Authorization authorization) {
		authorizationsServices.add(authorization, KEEP_ATTACHED, users.dakotaLIndienIn(zeCollection));
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return authorization.getDetail().getId();
	}

	private void addDetaching(Authorization authorization) {
		authorizationsServices.add(authorization, KEEP_ATTACHED, users.dakotaLIndienIn(zeCollection));
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private Authorization addAuthorizationDetaching(List<String> roles, List<String> grantedToPrincipals,
			List<String> grantedOnRecords) {
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);
		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);

		authorizationsServices.add(authorization, CustomizedAuthorizationsBehavior.DETACH, users.dakotaLIndienIn(zeCollection));
		return authorization;
	}

	private void addAuthorizationForDates(List<String> roles, List<String> grantedToPrincipals, List<String> grantedOnRecords,
			LocalDate startDate, LocalDate endDate) {
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, startDate, endDate, zeCollection);

		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);

		authorizationsServices.add(authorization, KEEP_ATTACHED, null);
	}

	private void modifyAuthorizationWithoutDetaching(Authorization authorization) {
		authorizationsServices
				.modify(authorization, KEEP_ATTACHED, users.dakotaLIndienIn(zeCollection));
	}

	private Group createGroup(String name) {
		return userServices.createCustomGroupInCollectionWithCodeAndName(zeCollection, ZE_GROUP, name);
	}

	private void givenChuckNorrisSeesEverything()
			throws Exception {

		User chuck = users.chuckNorrisIn(zeCollection);
		chuck.setCollectionReadAccess(true);
		chuck.setCollectionWriteAccess(true);
		chuck.setCollectionDeleteAccess(true);

		recordServices.update(chuck.getWrappedRecord());
	}

	private void givenAliceCanModifyEverythingAndBobCanDeleteEverythingAndDakotaReadEverythingInAnotherCollection()
			throws RecordServicesException {

		User alice = users.aliceIn(anotherCollection);
		alice.setCollectionWriteAccess(true);
		recordServices.update(alice.getWrappedRecord());

		User bob = users.bobIn(anotherCollection);
		bob.setCollectionDeleteAccess(true);
		recordServices.update(bob.getWrappedRecord());

		User dakota = users.dakotaIn(anotherCollection);
		dakota.setCollectionReadAccess(true);
		recordServices.update(dakota.getWrappedRecord());

	}

	private Condition<? super User> authorizationsToRead(final Record... records) {
		final List<String> recordIds = new RecordUtils().toIdList(asList(records));
		return new Condition<User>() {
			@Override
			public boolean matches(User user) {

				LogicalSearchQuery query = new LogicalSearchQuery()
						.setCondition(fromAllSchemasIn(user.getCollection()).returnAll())
						.filteredWithUser(user);
				List<String> results = searchServices.searchRecordIds(query);

				assertThat(results).containsAll(recordIds);

				return true;
			}
		};
	}

	private Condition<? super User> noAuthorizationsToRead(final Record... records) {
		final List<String> recordIds = new RecordUtils().toIdList(asList(records));
		return new Condition<User>() {
			@Override
			public boolean matches(User user) {

				LogicalSearchQuery query = new LogicalSearchQuery()
						.setCondition(fromAllSchemasIn(user.getCollection()).returnAll())
						.filteredWithUser(user);
				List<String> results = searchServices.searchRecordIds(query);

				assertThat(results).doesNotContainAnyElementsOf(recordIds);

				return true;
			}
		};
	}
}
