package com.constellio.model.services.security;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.REMOVED_AUTHORIZATIONS;
import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollectionWithId;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.entities.security.global.AuthorizationModificationRequest;
import com.constellio.model.entities.security.global.AuthorizationModificationResponse;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidTargetRecordId;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup.Records;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class BaseAuthorizationsServicesAcceptanceTest extends ConstellioTest {
	protected String anotherCollection = "anotherCollection";
	protected SecurityAcceptanceTestSetup anothercollectionSetup = new SecurityAcceptanceTestSetup(anotherCollection);
	protected String ZE_ROLE = "zeRoleCode";
	protected String ZE_GROUP = "zeGroupCode";
	protected String zeUnusedRoleCode = "zeNotUsed";
	protected SecurityAcceptanceTestSetup setup = new SecurityAcceptanceTestSetup(zeCollection);
	protected MetadataSchemasManager schemasManager;
	protected SearchServices searchServices;
	protected RecordServices recordServices;
	protected TaxonomiesManager taxonomiesManager;
	protected CollectionsListManager collectionsListManager;
	protected AuthorizationsServices services;
	protected UserServices userServices;
	protected SchemasRecordsServices schemas;

	protected Records records;
	protected Records otherCollectionRecords;
	protected Users users = new Users();
	protected RolesManager roleManager;

	protected String ROLE1 = "role1";
	protected String ROLE2 = "role2";
	protected String ROLE3 = "role3";

	protected String PERMISSION_OF_NO_ROLE = "permissionOfNoRole";
	protected String PERMISSION_OF_ROLE1 = "permissionOfRole1";
	protected String PERMISSION_OF_ROLE2 = "permissionOfRole2";
	protected String PERMISSION_OF_ROLE3 = "permissionOfRole3";
	protected String PERMISSION_OF_ROLE1_AND_ROLE2 = "permissionOfRole1AndRole2";

	protected final String VERSION_HISTORY_READ = "VERSION_HISTORY_READ";
	protected final String VERSION_HISTORY = "VERSION_HISTORY";

	protected AuthorizationModificationResponse request1, request2, request3;

	protected List<String> initialFinishedBatchProcesses;

	protected String auth1, auth2, auth3, auth4, auth5, auth6, auth7;

	String[] allUsers = new String[] { alice, bob, charles, dakota, edouard, gandalf, chuck, sasquatch, robin };

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
				rolesManager.addRole(new Role(zeCollection, ROLE3, "Ze role 3", asList(PERMISSION_OF_ROLE3)));

				try {
					givenChuckNorrisSeesEverything();
					givenAliceCanModifyEverythingAndBobCanDeleteEverythingAndDakotaReadEverythingInAnotherCollection();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			protected void setServices() {
				recordServices = getModelLayerFactory().newRecordServices();
				taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
				searchServices = getModelLayerFactory().newSearchServices();
				services = getModelLayerFactory().newAuthorizationsServices();
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
		givenTaxonomy1IsThePrincipalAndSomeRecords();
		recordServices.getRecordsCaches().getCache(zeCollection)
				.configureCache(CacheConfig.permanentCache(schemas.authorizationDetails.schemaType()));
		recordServices.getRecordsCaches().getCache(anotherCollection)
				.configureCache(CacheConfig.permanentCache(schemas.authorizationDetails.schemaType()));
	}

	protected ListAssert<String> assertThatBatchProcessDuringTest() {

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

	protected void givenTaxonomy1IsThePrincipalAndSomeRecords() {
		Taxonomy taxonomy = taxonomiesManager.getEnabledTaxonomyWithCode(zeCollection, "taxo1");
		taxonomiesManager.setPrincipalTaxonomy(taxonomy, schemasManager);
		records = setup.givenRecords(recordServices);
		otherCollectionRecords = anothercollectionSetup.givenRecords(recordServices);
	}

	static int totalBatchProcessCount = 0;

	@After
	public void checkIfNoBatchProcessRequired() {
		List<String> finishedBatchProcesses = new ArrayList<>();
		for (BatchProcess batchProcess : getModelLayerFactory().getBatchProcessesManager().getFinishedBatchProcesses()) {
			finishedBatchProcesses.add(batchProcess.getId());
		}

		int batchProcessCount = finishedBatchProcesses.size() - initialFinishedBatchProcesses.size();
		totalBatchProcessCount += batchProcessCount;
	}

	@AfterClass
	public static void tearDown()
			throws Exception {

		System.out.println("Total batch process count : " + totalBatchProcessCount);

	}

	protected UserVerifier forUserInAnotherCollection(String username) {
		return new UserVerifier(username, anotherCollection);
	}

	protected UserVerifier forUser(String username) {
		return new UserVerifier(username, zeCollection);
	}

	protected class UserVerifier {
		String username;
		String collection;

		public UserVerifier(String username, String collection) {
			this.username = username;
			this.collection = collection;
		}

		public User getUser() {
			return userServices.getUserInCollection(username, collection);
		}

		public ListAssert<String> assertThatConceptsForWhichUserHas(String permission) {
			return assertThat(services.getConceptsForWhichUserHasPermission(permission, getUser()))
					.describedAs("Concepts for which user '" + username + "' has permission '" + permission + "'");
		}

		public BooleanAssert assertHasDeletePermissionOnPrincipalConceptExcludingRecords(
				String id) {
			Record record = recordServices.getDocumentById(id);
			return assertThat(services.hasDeletePermissionOnPrincipalConceptHierarchy(getUser(), record, false, schemasManager));
		}

		public BooleanAssert assertHasDeletePermissionOnPrincipalConceptIncludingRecords(
				String id) {
			Record record = recordServices.getDocumentById(id);
			return assertThat(services.hasDeletePermissionOnPrincipalConceptHierarchy(getUser(), record, true, schemasManager));
		}

		public BooleanAssert assertHasRestorePermissionOnHierarchyOf(
				String id) {
			Record record = recordServices.getDocumentById(id);
			return assertThat(services.hasRestaurationPermissionOnHierarchy(getUser(), record));
		}

		public BooleanAssert assertHasDeletePermissionOnHierarchyOf(
				String id) {
			Record record = recordServices.getDocumentById(id);
			return assertThat(services.hasDeletePermissionOnHierarchy(getUser(), record));
		}

		public ListAssert<String> assertThatRecordsWithReadAccess() {
			List<MetadataSchemaType> types = asList(setup.folderSchema.type(), setup.documentFond.type(),
					setup.administrativeUnit.type(), setup.category.type(), setup.classificationStation.type(),
					setup.documentSchema.type());

			return assertThat(searchServices.searchRecordIds(query(from(types).returnAll()).filteredWithUser(getUser())));
		}

		public ListAssert<String> assertThatAllFoldersAndDocuments() {
			return assertThat(findAllFoldersAndDocuments(getUser()));
		}
	}

	protected RecordVerifier forRecord(String id) {
		return new RecordVerifier(id);
	}

	protected class RecordVerifier {
		String recordId;

		public RecordVerifier(String recordId) {
			this.recordId = recordId;
		}

		public ListAssert<Object> usersWithRole(String role) {
			return assertThat(services.getUsersWithRoleForRecord(role, get(recordId)))
					.describedAs("users with role '" + role + "' on record '" + recordId + "'").extracting("username");
		}

		public ListAssert<Object> assertThatUsersWithPermission(String permission) {
			return assertThat(services.getUsersWithPermissionOnRecord(permission, get(recordId)))
					.describedAs("users with permission '" + permission + "' on record '" + recordId + "'")
					.extracting("username");
		}

		public ListAssert<Object> getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(String permission) {
			return assertThat(
					services.getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(permission, get(recordId)))
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

		public ListAssert<String> usersWithPermission(String permission) {

			Record record = get(recordId);
			List<User> allUsers = userServices.getAllUsersInCollection(zeCollection);

			List<String> usersWithDeleteAccess = new ArrayList<>();
			for (User user : allUsers) {
				if (hasPermissionOn(user, record, permission)) {
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

	protected boolean hasReadAccess(User user, Record record) {
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

	protected boolean hasWriteAccess(User user, Record record) {
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

	protected boolean hasDeleteAccess(User user, Record record) {
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

	protected boolean hasPermissionOn(User user, Record record, String permission) {
		boolean hasAccessUsingWrapperMethod = user.has(permission).on(record);
		return hasAccessUsingWrapperMethod;
	}

	protected AuthorizationAddRequest authorization() {
		return authorizationInCollection(zeCollection);
	}

	protected AuthorizationAddRequest authorization(String existingAuthorizationId) {

		return authorizationInCollection(zeCollection);
	}

	protected AuthorizationAddRequest authorizationForUsers(String... usernames) {

		User[] usersArray = new User[usernames.length];

		for (int i = 0; i < usernames.length; i++) {
			usersArray[i] = userServices.getUserInCollection(usernames[i], zeCollection);
		}

		return authorizationInCollection(zeCollection).forUsers(usersArray);
	}

	protected AuthorizationAddRequest authorizationForGroups(String... groups) {

		Group[] groupsArray = new Group[groups.length];

		for (int i = 0; i < groups.length; i++) {
			groupsArray[i] = userServices.getGroupInCollection(groups[i], zeCollection);
		}

		return authorizationInCollection(zeCollection).forGroups(groupsArray);
	}

	protected AuthorizationAddRequest authorizationForUser(String username) {

		User[] usersArray = new User[] { userServices.getUserInCollection(username, zeCollection) };
		return authorizationInCollection(zeCollection).forUsers(usersArray);
	}

	protected AuthorizationAddRequest authorizationForPrincipals(String... principals) {

		List<String> principalIds = new ArrayList<>();

		for (String principal : principals) {
			principalIds.add(toPrincipalId(principal));
		}

		return authorizationInCollection(zeCollection).forPrincipalsIds(principalIds);
	}

	protected AuthorizationAddRequest authorizationForGroup(String group) {

		Group[] groupsArray = new Group[] { userServices.getGroupInCollection(group, zeCollection) };
		return authorizationInCollection(zeCollection).forGroups(groupsArray);
	}

	protected long fetchEventCount() {
		LogicalSearchCondition condition = from(schemasManager.getSchemaTypes(zeCollection).getSchemaType(Event.SCHEMA_TYPE))
				.returnAll();
		return searchServices.getResultsCount(condition);
	}

	protected RecordVerifier verifyRecord(String id) {
		return new RecordVerifier(id);
	}

	protected List<RecordVerifier> $(String... ids) {
		List<RecordVerifier> verifiers = new ArrayList<>();

		for (String id : ids) {
			verifiers.add(new RecordVerifier(id));
		}

		return verifiers;
	}

	protected VerifiedAuthorization authOnRecord(String recordId) {
		return new VerifiedAuthorization(recordId);
	}

	protected class VerifiedAuthorization {

		String recordId;

		Set<String> principals;

		Set<String> removedOnRecords = new HashSet<>();

		List<String> roles;

		LocalDate start;

		LocalDate end;

		protected VerifiedAuthorization(String recordId) {
			this.recordId = recordId;
		}

		protected VerifiedAuthorization forPrincipals(String... principals) {
			this.principals = new HashSet<>(asList(principals));
			return this;
		}

		protected VerifiedAuthorization forPrincipalIds(List<String> principals) {
			this.principals = new HashSet<>(toPrincipalCodes(principals.toArray(new String[] {})));
			return this;
		}

		protected VerifiedAuthorization removedOnRecords(String... removedOnRecords) {
			this.removedOnRecords = new HashSet<>(asList(removedOnRecords));
			return this;
		}

		protected VerifiedAuthorization givingRoles(String... roles) {
			this.roles = asList(roles);
			return this;
		}

		protected VerifiedAuthorization givingRead() {
			this.roles = asList(READ);
			return this;
		}

		protected VerifiedAuthorization givingReadWrite() {
			this.roles = asList(READ, WRITE);
			return this;
		}

		protected VerifiedAuthorization givingReadWriteDelete() {
			this.roles = asList(READ, WRITE, DELETE);
			return this;
		}

		protected VerifiedAuthorization startingOn(LocalDate start) {
			this.start = start;
			return this;
		}

		protected VerifiedAuthorization endingOn(LocalDate end) {
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
			return "{recordId='" + recordId + '\'' +
					",  principals=" + principals +
					",  removedOnRecords=" + removedOnRecords +
					",  roles=" + roles +
					",  start=" + start +
					",  end=" + end +
					'}';
		}
	}

	protected ListAssert<String> assertThatAllAuthorizationIds() {

		List<String> authorizations = new ArrayList<>();
		for (AuthorizationDetails details : getModelLayerFactory().getAuthorizationDetailsManager()
				.getAuthorizationsDetails(zeCollection).values()) {
			authorizations.add(details.getId());
		}

		return assertThat(authorizations);

	}

	protected ListAssert<String> ensureNoRecordsHaveAnInvalidAuthorization() {
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		List<String> authorizations = new ArrayList<>();

		for (SolrAuthorizationDetails details : schemas.searchSolrAuthorizationDetailss(ALL)) {
			authorizations.add(details.getId());
			try {
				recordServices.getDocumentById(details.getTarget());
			} catch (NoSuchRecordWithId e) {
				throw new RuntimeException(
						"Auth '" + details.getId() + "' is targetting an inexistent record : " + details.getTarget());
			}
		}

		Iterator<Record> recordIterator = searchServices.recordsIterator(query(fromAllSchemasIn(zeCollection).returnAll()));

		while (recordIterator.hasNext()) {
			Record record = recordIterator.next();

			for (String auth : record.<String>getList(Schemas.REMOVED_AUTHORIZATIONS)) {
				if (!authorizations.contains(auth)) {
					throw new RuntimeException("Record '" + record.getIdTitle() + "' has an invalid authorization : " + auth);
				}
			}

		}

		return assertThat(authorizations);

	}

	protected ListAssert<String> assertThatAllAuthorizationsIds() {
		List<String> authorizations = new ArrayList<>();
		for (AuthorizationDetails details : schemas.searchSolrAuthorizationDetailss(ALL)) {
			authorizations.add(details.getId());
		}
		return assertThat(authorizations);
	}

	protected ListAssert<VerifiedAuthorization> assertThatAllAuthorizations() {

		List<VerifiedAuthorization> authorizations = new ArrayList<>();
		for (AuthorizationDetails details : schemas.searchSolrAuthorizationDetailss(ALL)) {
			Authorization authorization = services.getAuthorization(zeCollection, details.getId());

			List<String> removedOnRecords = searchServices.searchRecordIds(fromAllSchemasIn(zeCollection).where(
					REMOVED_AUTHORIZATIONS).isEqualTo(authorization.getDetail().getId()));

			authorizations.add(authOnRecord(authorization.getGrantedOnRecord())
					.forPrincipalIds(authorization.getGrantedToPrincipals())
					.givingRoles(details.getRoles().toArray(new String[0]))
					.startingOn(details.getStartDate()).endingOn(details.getEndDate())
					.removedOnRecords(removedOnRecords.toArray(new String[0])));
		}
		return assertThat(authorizations).usingFieldByFieldElementComparator();

	}

	protected String toPrincipalId(String principal) {
		try {
			return userServices.getUserInCollection(principal, zeCollection).getId();
		} catch (Exception e) {
			return userServices.getGroupInCollection(principal, zeCollection).getId();
		}
	}

	protected ListAssert<VerifiedAuthorization> assertThatAuthorizationsFor(String principal) {
		return assertThatAuthorizationsOn(toPrincipalId(principal));
	}

	protected ListAssert<VerifiedAuthorization> assertThatAuthorizationsOn(String recordId) {
		Record record = recordServices.getDocumentById(recordId);

		List<VerifiedAuthorization> authorizations = new ArrayList<>();
		for (Authorization authorization : services.getRecordAuthorizations(record)) {

			List<String> removedOnRecords = searchServices.searchRecordIds(fromAllSchemasIn(zeCollection).where(
					REMOVED_AUTHORIZATIONS).isEqualTo(authorization.getDetail().getId()));

			authorizations.add(authOnRecord(authorization.getGrantedOnRecord())
					.forPrincipalIds(authorization.getGrantedToPrincipals())
					.givingRoles(authorization.getDetail().getRoles().toArray(new String[0]))
					.removedOnRecords(removedOnRecords.toArray(new String[0])));
		}
		return assertThat(authorizations).usingFieldByFieldElementComparator();

	}

	protected class AuthorizationVerifier {

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
			Authorization authorization = services
					.getAuthorization(zeCollection, authId);
			assertThat(authorization.getGrantedToPrincipals()).describedAs("principals")
					.containsOnly(expectedPrincipals.toArray(new String[0]));
			return this;
		}

	}

	protected List<String> toPrincipalCodes(String... principalIds) {
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

	protected List<String> toPrincipalIds(String... principals) {
		List<String> ids = new ArrayList<>();
		for (String principal : principals) {
			try {
				ids.add(userServices.getUserInCollection(principal, zeCollection).getId());
			} catch (Exception e) {
				ids.add(userServices.getGroupInCollection(principal, zeCollection).getId());
			}
		}
		return ids;
	}

	protected AuthorizationVerifier assertThatAuth(String id) {
		return new AuthorizationVerifier(id);
	}

	protected UserAction givenUser(String username) {
		return new UserAction(username);
	}

	protected class UserAction {

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

	protected AuthorizationModificationRequest authorizationOnRecord(String authorizationId, String recordId) {
		return new AuthorizationModificationRequest(authorizationId, zeCollection, recordId);
	}

	protected AuthorizationModificationResponse modify(AuthorizationModificationRequest request) {
		AuthorizationModificationResponse response = services.execute(request);
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return response;
	}

	protected Condition<? super AuthorizationModificationResponse> deleted() {
		return new Condition<AuthorizationModificationResponse>() {
			@Override
			public boolean matches(AuthorizationModificationResponse value) {
				return value.isAuthorizationDeleted();
			}
		};
	}

	protected Condition<? super AuthorizationModificationResponse> creatingACopy() {
		return new Condition<AuthorizationModificationResponse>() {
			@Override
			public boolean matches(AuthorizationModificationResponse value) {
				return value.getIdOfAuthorizationCopy() != null;
			}
		};
	}

	protected Record get(String recordId) {
		return recordServices.getDocumentById(recordId);
	}

	// ---------------------------------------------------------------------------------------------------------

	protected Condition<? super User> allowedToWrite(final Record record) {
		recordServices.refresh(record);
		return new Condition<User>() {
			@Override
			public boolean matches(User user) {
				assertThat(services.canWrite(user, record))
						.describedAs("can write '" + record.getId() + "'")
						.isTrue();
				return true;
			}
		};
	}

	protected Condition<? super User> notAllowedToWrite(final Record record) {
		recordServices.refresh(record);
		return new Condition<User>() {
			@Override
			public boolean matches(User user) {
				assertThat(services.canWrite(user, record))
						.describedAs("can write '" + record.getId() + "'")
						.isFalse();
				return true;
			}
		};
	}

	protected List<String> findAllFoldersAndDocumentsWithWritePermission(User user) {
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

	protected List<String> findAllFoldersAndDocumentsWithDeletePermission(User user) {
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

	protected List<String> findAllFoldersAndDocuments(User user) {
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

	@Deprecated
	protected Authorization addAuthorizationWithoutDetaching(List<String> roles, List<String> grantedToPrincipals,
			String grantedOnRecord) {
		return addAuthorizationWithoutDetaching(aString(), roles, grantedToPrincipals, grantedOnRecord);
	}

	@Deprecated
	protected Authorization addAuthorizationWithoutDetaching(String id, List<String> roles, List<String> grantedToPrincipals,
			String grantedOnRecord) {

		id = services.add(authorizationInCollectionWithId(zeCollection, id).forPrincipalsIds(grantedToPrincipals)
				.on(grantedOnRecord)
				.giving(roles).setExecutedBy(users.dakotaLIndienIn(zeCollection)));

		return services.getAuthorization(zeCollection, id);
	}

	protected String add(AuthorizationAddRequest request) {
		String id = services.add(request, users.dakotaLIndienIn(zeCollection));
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return id;
	}

	protected String addWithoutUser(AuthorizationAddRequest authorization) {
		String id = services.add(authorization, null);
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return id;
	}

	protected Map<String, String> detach(String recordId) {
		Map<String, String> copies = services.detach(get(recordId));
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return copies;
	}

	protected Group createGroup(String name) {
		return userServices.createCustomGroupInCollectionWithCodeAndName(zeCollection, ZE_GROUP, name);
	}

	protected void givenChuckNorrisSeesEverything()
			throws Exception {

		User chuck = users.chuckNorrisIn(zeCollection);
		chuck.setCollectionReadAccess(true);
		chuck.setCollectionWriteAccess(true);
		chuck.setCollectionDeleteAccess(true);

		recordServices.update(chuck.getWrappedRecord());
	}

	protected void givenAliceCanModifyEverythingAndBobCanDeleteEverythingAndDakotaReadEverythingInAnotherCollection()
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

	protected Condition<? super User> authorizationsToRead(final Record... records) {
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

	protected Condition<? super User> noAuthorizationsToRead(final Record... records) {
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

	protected ListAssert<Object> assertThatUsersWithGlobalPermissionInCollection(String permission, String collection) {
		return assertThat(services.getUsersWithGlobalPermissionInCollection(permission, collection))
				.extracting("username");
	}

	protected ListAssert<Object> assertThatUsersWithGlobalPermissionInZeCollection(String permission) {
		return assertThatUsersWithGlobalPermissionInCollection(permission, zeCollection);
	}
}
