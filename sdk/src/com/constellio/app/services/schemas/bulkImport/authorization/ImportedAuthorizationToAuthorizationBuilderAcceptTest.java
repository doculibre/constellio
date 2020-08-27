package com.constellio.app.services.schemas.bulkImport.authorization;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationPrincipal;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationTarget;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ImportedAuthorizationToAuthorizationBuilderAcceptTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	ImportedAuthorizationToAuthorizationBuilder builder;
	ImportedAuthorization validAuthorization;
	User aliceHavingLegacyId;
	Group heroes;
	Folder folderHavingLegacyId;
	Document documentHavingLegacyId;
	AdministrativeUnit administrativeUnitHavingLegacyId;
	Task userTaskHavingLegacyId;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers().withDocumentsHavingContent()
		);
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);

		builder = new ImportedAuthorizationToAuthorizationBuilder(zeCollection, getModelLayerFactory());

		initTestRecords();

		initValidAuthorization();
	}

	private void initTestRecords()
			throws RecordServicesException {
		aliceHavingLegacyId = users.aliceIn(zeCollection);
		heroes = users.heroesIn(zeCollection);
		folderHavingLegacyId = records.getFolder_A01();
		documentHavingLegacyId = records.getDocumentWithContent_A19();
		administrativeUnitHavingLegacyId = records.getUnit10();
		TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		userTaskHavingLegacyId = tasksSchemas.newTask();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Transaction transaction = new Transaction();
		recordServices.add(userTaskHavingLegacyId.setTitle("taskTitle"));
		transaction.add(folderHavingLegacyId.setLegacyId("folderLegacyId"));
		transaction.add(documentHavingLegacyId.setLegacyId("documentLegacyId"));
		transaction.add(administrativeUnitHavingLegacyId.setLegacyId("administrativeUnitLegacyId"));
		transaction.add(userTaskHavingLegacyId.setLegacyId("userTaskLegacyId"));
		recordServices.execute(transaction);
	}

	private void initValidAuthorization() {
		List<ImportedAuthorizationPrincipal> validPrincipals = asList(
				new ImportedAuthorizationPrincipal("user", "alice"),
				new ImportedAuthorizationPrincipal("group", "heroes"));

		List<ImportedAuthorizationTarget> validTargets = asList(
				new ImportedAuthorizationTarget("folder", "folderLegacyId"));

		validAuthorization = new ImportedAuthorization().setId("id").setPrincipals(validPrincipals)
				.setTargets(validTargets).setAccess("rwd");
	}

	@Test
	public void givenValidAuthorizationWithAccessWhenBuildThenBuiltCorrectly()
			throws Exception {
		AuthorizationAddRequest authorization = builder.buildAddRequest(validAuthorization);
		assertThat(authorization.getPrincipals())
				.containsExactly(aliceHavingLegacyId.getId(), heroes.getId());
		assertThat(authorization.getTarget()).isEqualTo(folderHavingLegacyId.getId());
		assertThat(authorization.getId()).isNull();
		assertThat(authorization.getStart()).isNull();
		assertThat(authorization.getEnd()).isNull();
		assertThat(authorization.getCollection()).isEqualTo(zeCollection);
		assertThat(authorization.getRoles()).containsOnly(Role.READ, Role.WRITE, Role.DELETE);
	}

	@Test
	public void givenValidAuthorizationWithRolesWhenBuildThenBuiltCorrectly()
			throws Exception {
		validAuthorization.setAccess(null).setRoles(asList("u", "rgd"));
		AuthorizationAddRequest authorization = builder.buildAddRequest(validAuthorization);
		assertThat(authorization.getRoles()).containsExactly("u", "rgd");
	}
}
