package com.constellio.app.api.cmis.rm;

import com.constellio.app.api.cmis.accept.CmisAcceptanceTestSetup;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordHierarchyServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.assertj.core.api.IterableAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.api.cmis.builders.object.AclBuilder.CMIS_READ;
import static com.constellio.app.modules.rm.constants.RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS;
import static com.constellio.app.modules.rm.constants.RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS;
import static com.constellio.app.modules.rm.constants.RMPermissionsTo.SHARE_DOCUMENT;
import static com.constellio.app.modules.rm.constants.RMPermissionsTo.SHARE_FOLDER;
import static com.constellio.model.entities.CorePermissions.MANAGE_SECURITY;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.apache.chemistry.opencmis.commons.enums.AclPropagation.REPOSITORYDETERMINED;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_APPLY_ACL;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_ACL;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_CHILDREN;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_PROPERTIES;
import static org.assertj.core.api.Assertions.assertThat;

@DriverTest
public class RMCmisAllowableActionsAcceptanceTest extends ConstellioTest {

	AuthorizationsServices authServices;
	RecordServices recordServices;
	Users users = new Users();
	Session session;
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		RolesManager rolesManager = getModelLayerFactory().getRolesManager();
		rolesManager.addRole(new Role(zeCollection, "r1", asList(MANAGE_SECURITY)));
		rolesManager.addRole(new Role(zeCollection, "r2", asList(MANAGE_FOLDER_AUTHORIZATIONS)));
		rolesManager.addRole(new Role(zeCollection, "r3", asList(SHARE_FOLDER)));
		rolesManager.addRole(new Role(zeCollection, "r4", asList(MANAGE_DOCUMENT_AUTHORIZATIONS)));
		rolesManager.addRole(new Role(zeCollection, "r5", asList(SHARE_DOCUMENT)));

		Role managerRole = rolesManager.getRole(zeCollection, RMRoles.MANAGER);
		List<String> managerPermissions = new ArrayList<>(managerRole.getOperationPermissions());
		managerPermissions.remove(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS);
		managerPermissions.remove(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS);
		rolesManager.updateRole(managerRole.withPermissions(managerPermissions));

		recordServices = getModelLayerFactory().newRecordServices();
		authServices = getModelLayerFactory().newAuthorizationsServices();

		users.setUp(getModelLayerFactory().newUserServices());

		recordServices.update(users.adminIn(zeCollection).setCollectionAllAccess(true));
		recordServices.update(users.aliceIn(zeCollection).setCollectionReadAccess(true));
		recordServices.update(users.sasquatchIn(zeCollection).setCollectionAllAccess(true).setUserRoles("r2"));
		recordServices.update(users.bobIn(zeCollection).setCollectionAllAccess(true).setUserRoles("r3"));
		recordServices.update(users.robinIn(zeCollection).setCollectionAllAccess(true).setUserRoles("r4"));
		recordServices.update(users.chuckNorrisIn(zeCollection).setCollectionAllAccess(true).setUserRoles("r1"));
		recordServices.update(users.gandalfIn(zeCollection).setCollectionReadAccess(true).setCollectionWriteAccess(true));

		givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, false);
		CmisAcceptanceTestSetup.giveUseCMISPermissionToUsers(getModelLayerFactory());

	}

	@Test
	public void whenGetAllowableActionsOfRootThenOK()
			throws Exception {

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf("/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThatAllowableActionsOf("/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThatAllowableActionsOf("/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

	}

	@Test
	public void whenGetAllowableActionsOfTaxonomyThenOK()
			throws Exception {

		session = newCMISSessionAsUserInZeCollection(admin);
		assertThatAllowableActionsOf("/taxo_plan/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
		assertThatAllowableActionsOf("/taxo_admUnits/").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(aliceWonderland);
		assertThatAllowableActionsOf("/taxo_plan").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
		assertThatAllowableActionsOf("/taxo_admUnits").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);

		session = newCMISSessionAsUserInZeCollection(bobGratton);
		assertThatAllowableActionsOf("/taxo_plan").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
		assertThatAllowableActionsOf("/taxo_admUnits").containsOnly(CAN_GET_PROPERTIES, CAN_GET_CHILDREN);
	}

	@Test
	public void whenUserHasRWDAndManageSecurityPermissionOnPrincipalConceptThenCanSetACL()
			throws Exception {

		givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, false);

		authServices.add(authorizationForUsers(users.dakotaIn(zeCollection)).on(records.unitId_10).givingReadWriteDeleteAccess(),
				users.adminIn(zeCollection));
		authServices.add(authorizationForUsers(users.dakotaIn(zeCollection)).on(records.unitId_10).giving("r1"),
				users.adminIn(zeCollection));

		authServices.add(authorizationForUsers(users.edouardIn(zeCollection)).on(records.unitId_10).givingReadWriteDeleteAccess(),
				users.adminIn(zeCollection));

		authServices.add(authorizationForUsers(users.charlesIn(zeCollection)).on(records.unitId_10).givingReadWriteDeleteAccess(),
				users.adminIn(zeCollection));
		authServices.add(authorizationForUsers(users.charlesIn(zeCollection)).on(records.unitId_10).giving("r2"),
				users.adminIn(zeCollection));

		authServices.add(authorizationForUsers(users.aliceIn(zeCollection)).on(records.folder_A19).givingReadWriteDeleteAccess(),
				users.adminIn(zeCollection));
		authServices.add(authorizationForUsers(users.aliceIn(zeCollection)).on(records.folder_A19).giving("r4"),
				users.adminIn(zeCollection));

		authServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(records.unitId_10).givingReadWriteDeleteAccess(),
				users.adminIn(zeCollection));
		//Roles giving share folder and share documents permissions, which are not supported in cmis
		authServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(records.unitId_10).giving("r3", "r5"),
				users.adminIn(zeCollection));

		authServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(records.unitId_10).givingReadWriteDeleteAccess(),
				users.adminIn(zeCollection));
		//Roles giving share folder and share documents permissions, which are not supported in cmis
		authServices.add(authorizationForUsers(users.bobIn(zeCollection)).on(records.unitId_10).giving("r3", "r5"),
				users.adminIn(zeCollection));

		waitForBatchProcess();

		//Folders :

		for (String user : asList(admin, dakota, chuckNorris, sasquatch, charles)) {
			ensureUserCanGetAndApplyACLOnRecords(user, records.folder_A19);
		}
		for (String user : asList(edouard, gandalf, aliceWonderland, bobGratton, robin)) {
			ensureUserCannotGetAndApplyACLOnRecords(user, records.folder_A19);
		}

		for (String user : asList(admin, chuckNorris, sasquatch)) {
			ensureUserCanGetAndApplyACLOnRecords(user, records.folder_C30);
		}
		for (String user : asList(edouard, gandalf, aliceWonderland, bobGratton, robin)) {
			ensureUserCannotGetAndApplyACLOnRecords(user, records.folder_C30);
		}

		//Documents :

		for (String user : asList(admin, aliceWonderland, chuckNorris, robin)) {
			ensureUserCanGetAndApplyACLOnRecords(user, records.document_A19);
		}
		for (String user : asList(edouard, charles, gandalf, bobGratton, sasquatch)) {
			ensureUserCannotGetAndApplyACLOnRecords(user, records.document_A19);
		}

		for (String user : asList(admin, chuckNorris, robin)) {
			ensureUserCanGetAndApplyACLOnRecords(user, records.document_B30);
		}
		for (String user : asList(edouard, gandalf, aliceWonderland, bobGratton, sasquatch)) {
			ensureUserCannotGetAndApplyACLOnRecords(user, records.document_B30);
		}

	}

	@Test
	public void whenUserHasRWDAndManagerSecurityPermissionOnFolderThenCanSetACL()
			throws Exception {

		givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, false);

		authServices.add(authorizationForUsers(users.dakotaIn(zeCollection)).on(records.folder_A03).givingReadWriteDeleteAccess(),
				users.adminIn(zeCollection));
		authServices.add(authorizationForUsers(users.dakotaIn(zeCollection)).on(records.folder_A03).giving("r1"),
				users.adminIn(zeCollection));

		authServices
				.add(authorizationForUsers(users.edouardIn(zeCollection)).on(records.folder_A03).givingReadWriteDeleteAccess(),
						users.adminIn(zeCollection));

		authServices.add(authorizationForUsers(users.charlesIn(zeCollection)).on(records.folder_A03).givingReadWriteAccess(),
				users.adminIn(zeCollection));
		authServices.add(authorizationForUsers(users.charlesIn(zeCollection)).on(records.folder_A03).giving("r1"),
				users.adminIn(zeCollection));
		waitForBatchProcess();

		for (String user : asList(admin, dakota, chuckNorris, sasquatch)) {
			ensureUserCanGetAndApplyACLOnRecords(user, records.folder_A03);
		}
		for (String user : asList(edouard, charles, gandalf, aliceWonderland, bobGratton, robin)) {
			ensureUserCannotGetAndApplyACLOnRecords(user, records.folder_A03);
		}

		for (String user : asList(admin, chuckNorris, sasquatch)) {
			ensureUserCanGetAndApplyACLOnRecords(user, records.folder_A07);
		}
		for (String user : asList(gandalf, aliceWonderland, bobGratton, robin)) {
			ensureUserCannotGetAndApplyACLOnRecords(user, records.folder_A07);
		}

	}

	private void ensureUserCanGetAndApplyACLOnRecords(String user, String id) {
		as(user).assertThatAllowableActionsOf(id).describedAs("Actions of " + user).contains(CAN_GET_ACL, CAN_APPLY_ACL);
		assertThat(session.getObject(id).getAcl().getAces()).isNotEmpty();
		assertThat(session.getAcl(session.getObject(id), false).getAces()).isNotEmpty();
		session.getObject(id).addAcl(asList(ace(bobGratton, asList(CMIS_READ))), REPOSITORYDETERMINED);
	}

	@Test
	public void givenUserAsAllCollectionAccessThenCanOnlySetACLOfAdministrativeUnitsIfItHasThePermissionTo()
			throws Exception {

		givenConfig(ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL, false);

		authServices.add(authorizationForUsers(users.dakotaIn(zeCollection)).on(records.unitId_10).givingReadWriteDeleteAccess(),
				users.adminIn(zeCollection));
		authServices.add(authorizationForUsers(users.dakotaIn(zeCollection)).on(records.unitId_10).giving(RMRoles.RGD),
				users.adminIn(zeCollection));

		authServices.add(authorizationForUsers(users.edouardIn(zeCollection)).on(records.unitId_10).givingReadWriteDeleteAccess(),
				users.adminIn(zeCollection));

		authServices.add(authorizationForUsers(users.charlesIn(zeCollection)).on(records.unitId_10).givingReadWriteAccess(),
				users.adminIn(zeCollection));
		authServices.add(authorizationForUsers(users.charlesIn(zeCollection)).on(records.unitId_10).giving(RMRoles.RGD),
				users.adminIn(zeCollection));
		waitForBatchProcess();

		for (String user : asList(admin, dakota, chuckNorris)) {
			as(user).assertThatAllowableActionsOf(records.unitId_10).contains(CAN_GET_ACL, CAN_APPLY_ACL);
			assertThat(session.getObject(records.unitId_10).getAcl().getAces()).isNotEmpty();
			assertThat(session.getAcl(session.getObject(records.unitId_10), false).getAces()).isNotEmpty();
			session.getObject(records.unitId_10).addAcl(asList(ace(bobGratton, asList(CMIS_READ))), REPOSITORYDETERMINED);
		}
		for (String user : asList(edouard, charles, sasquatch, gandalf, aliceWonderland)) {
			as(user).assertThatAllowableActionsOf(records.unitId_10).doesNotContain(CAN_GET_ACL, CAN_APPLY_ACL);
			assertThat(session.getObject(records.unitId_10).getAcl()).isNull();
			try {
				session.getAcl(session.getObject(records.unitId_10), true);
				fail("Exception expected");
			} catch (Exception e) {
				//OK
			}
			try {
				session.getAcl(session.getObject(records.unitId_10), false);
				fail("Exception expected");
			} catch (Exception e) {
				//OK
			}

			try {
				session.getObject(records.unitId_10).addAcl(asList(ace(bobGratton, asList(CMIS_READ))), REPOSITORYDETERMINED);
				fail("Exception expected");
			} catch (Exception e) {
				//OK
			}
		}

	}

	@Test
	public void testForLoggingWhenConfigIsEnable() throws Exception {
		givenConfig(RMConfigs.LOG_FOLDER_DOCUMENT_ACCESS_WITH_CMIS, true);
		session = newCMISSessionAsUserInCollection(records.getAdmin().getUsername(), zeCollection);
		session.getDefaultContext().setIncludeAcls(true);
		MetadataSchema eventMetadataSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType(Event.SCHEMA_TYPE).getDefaultSchema();
		long getNumberOfEventBeforeAccess = getModelLayerFactory().newSearchServices().getResultsCount(LogicalSearchQueryOperators.from(eventMetadataSchema).returnAll());
		Folder cmisFolder2 = cmisFolder(records.getFolder_A01().getWrappedRecord());
		waitForBatchProcess();
		long getNumberOfEventAfterAcces = getModelLayerFactory().newSearchServices().getResultsCount(LogicalSearchQueryOperators.from(eventMetadataSchema).returnAll());
		assertThat(getNumberOfEventAfterAcces).isEqualTo(getNumberOfEventBeforeAccess + 1);
	}

	@Test
	public void testForLoggingIsDisableWhenConfigIsDisable() throws Exception {
		givenConfig(RMConfigs.LOG_FOLDER_DOCUMENT_ACCESS_WITH_CMIS, false);
		session = newCMISSessionAsUserInCollection(records.getAdmin().getUsername(), zeCollection);
		session.getDefaultContext().setIncludeAcls(true);
		MetadataSchema eventMetadataSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType(Event.SCHEMA_TYPE).getDefaultSchema();
		long getNumberOfEventBeforeAccess = getModelLayerFactory().newSearchServices().getResultsCount(LogicalSearchQueryOperators.from(eventMetadataSchema).returnAll());
		Folder cmisFolder2 = cmisFolder(records.getFolder_A01().getWrappedRecord());
		waitForBatchProcess();
		long getNumberOfEventAfterAcces = getModelLayerFactory().newSearchServices().getResultsCount(LogicalSearchQueryOperators.from(eventMetadataSchema).returnAll());
		assertThat(getNumberOfEventAfterAcces).isEqualTo(getNumberOfEventBeforeAccess);
	}


	private void printTaxonomies(User user) {
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		StringBuilder stringBuilder = new StringBuilder();
		for (Taxonomy taxonomy : taxonomiesManager.getEnabledTaxonomies(zeCollection)) {
			stringBuilder.append(taxonomy.getCode() + " : \n");
			for (Record record : new RecordHierarchyServices(getModelLayerFactory())
					.getRootConcept(zeCollection, taxonomy.getCode(), new TaxonomiesSearchOptions().setRows(100))) {
				printConcept(user, taxonomy.getCode(), record, 1, stringBuilder);
			}
			stringBuilder.append("\n\n");
		}
		System.out.println(stringBuilder.toString());
	}

	private void printConcept(User user, String taxonomy, Record record, int level, StringBuilder stringBuilder) {
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();
		for (int i = 0; i < level; i++) {
			stringBuilder.append("\t");
		}
		stringBuilder.append(record.getId() + "\n");
		for (TaxonomySearchRecord child : taxonomiesSearchServices
				.getVisibleChildConcept(user, taxonomy, record, new TaxonomiesSearchOptions().setRows(100))) {

			printConcept(user, taxonomy, child.getRecord(), level + 1, stringBuilder);
		}

	}

	private IterableAssert<Action> assertThatAllowableActionsOf(String path) {
		if (path.startsWith("/")) {
			return assertThat(session.getObjectByPath(path).getAllowableActions().getAllowableActions());
		} else {
			return assertThat(session.getObject(path).getAllowableActions().getAllowableActions());
		}
	}

	private RMCmisAllowableActionsAcceptanceTest as(String user) {
		System.out.println("Logging as " + user);
		session = newCMISSessionAsUserInCollection(user, zeCollection);
		session.getDefaultContext().setIncludeAcls(true);
		return this;
	}

	private Folder cmisFolder(Record record) {
		return (Folder) session.getObject(record.getId());
	}

	private Ace ace(String principal, List<String> permissions) {
		return session.getObjectFactory().createAce(principal, permissions);
	}

	private void ensureUserCannotGetAndApplyACLOnRecords(String user, String id) {
		as(user).assertThatAllowableActionsOf(id).describedAs("Actions of " + user).doesNotContain(CAN_GET_ACL, CAN_APPLY_ACL);
		assertThat(session.getObject(id).getAcl()).isNull();
		try {
			session.getAcl(session.getObject(id), true);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}
		try {
			session.getAcl(session.getObject(id), false);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		try {
			session.getObject(id).addAcl(asList(ace(bobGratton, asList(CMIS_READ))), REPOSITORYDETERMINED);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}
	}
}
