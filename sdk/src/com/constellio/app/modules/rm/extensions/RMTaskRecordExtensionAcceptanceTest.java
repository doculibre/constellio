package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMGeneratedSchemaRecordsServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RMTaskRecordExtensionAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	RMSchemasRecordsServices rm;
	RMGeneratedSchemaRecordsServices generatedSchemaRecordsServices;
	AuthorizationsServices authorizationsServices;
	TasksSearchServices tasksSearchServices;
	Users users = new Users();

	private User sasquatch;
	private User robin;

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule().withTasksModule().withAllTestUsers()
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		givenConfig(RMConfigs.CREATE_MISSING_AUTHORIZATIONS_FOR_TASK, true);

		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		generatedSchemaRecordsServices = new RMGeneratedSchemaRecordsServices(zeCollection, getModelLayerFactory());
		authorizationsServices = new AuthorizationsServices(getModelLayerFactory());
		TasksSchemasRecordsServices tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		tasksSearchServices = new TasksSearchServices(tasksSchemas);
		users.setUp(getModelLayerFactory().newUserServices());

		sasquatch = users.sasquatchIn(zeCollection);
		robin = users.robinIn(zeCollection);
	}

	@Test
	public void givenTaskCreatedForUserWithoutAccessAndConfigDisabledThenNoAuthorizationCreated() throws Exception {
		givenConfig(RMConfigs.CREATE_MISSING_AUTHORIZATIONS_FOR_TASK, false);

		String id = "task1";
		createRMTask(id, sasquatch.getId(), Collections.singletonList(records.folder_A01));

		RMTask task = rm.wrapRMTask(recordServices.getDocumentById(id));
		assertThat(task.getCreatedAuthorizations()).isEmpty();

		assertThat(sasquatch.hasReadAccess().on(records.getFolder_A01())).isFalse();
	}

	@Test
	public void givenTaskCreatedForUserWithoutAccessThenAuthorizationCreated()
			throws Exception {
		String id = "task1";
		createRMTask(id, sasquatch.getId(), Collections.singletonList(records.folder_A01));

		RMTask task = rm.wrapRMTask(recordServices.getDocumentById(id));
		assertThat(task.getCreatedAuthorizations()).hasSize(1);

		Authorization authorization =
				authorizationsServices.getAuthorization(zeCollection, task.getCreatedAuthorizations().get(0));
		assertThat(authorization.getTarget()).isEqualTo(records.folder_A01);
		assertThat(authorization.getRoles()).containsOnly(Role.READ);
		assertThat(authorization.getPrincipals()).containsOnly(sasquatch.getId());

		assertThat(sasquatch.hasReadAccess().on(records.getFolder_A01())).isTrue();
	}

	@Test
	public void givenTaskCreatedForUserWithoutAccessThenAuthorizationsDeletedWhenTaskIsClosed() throws Exception {
		String id = "task1";
		createRMTask(id, sasquatch.getId(), Collections.singletonList(records.folder_A01));

		RMTask task = rm.wrapRMTask(recordServices.getDocumentById(id));
		assertThat(task.getCreatedAuthorizations()).hasSize(1);

		Authorization authorization =
				authorizationsServices.getAuthorization(zeCollection, task.getCreatedAuthorizations().get(0));
		String authorizationId = authorization.getId();

		recordServices.update(task.setStatus(tasksSearchServices.getClosedStatus().getId()));

		task = rm.wrapRMTask(recordServices.getDocumentById(id));
		assertThat(task.getCreatedAuthorizations()).hasSize(0);

		try {
			authorizationsServices.getAuthorization(zeCollection, authorizationId);
			fail("id exists");
		} catch (AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId e) {
			//
		}
		assertThat(sasquatch.hasReadAccess().on(records.getFolder_A01())).isFalse();
	}

	@Test
	public void givenTaskModifiedForUserWithoutAccessThenAuthorizationCreated() throws Exception {
		String id = "task1";
		createRMTask(id, sasquatch.getId(), null);

		RMTask task = rm.wrapRMTask(recordServices.getDocumentById(id));
		assertThat(task.getCreatedAuthorizations()).hasSize(0);

		recordServices.update(task.setLinkedDocuments(asList(records.document_A19, records.document_A49)));

		task = rm.wrapRMTask(recordServices.getDocumentById(id));
		assertThat(task.getCreatedAuthorizations()).hasSize(2);

		Authorization authorization =
				authorizationsServices.getAuthorization(zeCollection, task.getCreatedAuthorizations().get(0));
		assertThat(authorization.getTarget()).isIn(asList(records.document_A19, records.document_A49));
		assertThat(authorization.getRoles()).containsOnly(Role.READ);
		assertThat(authorization.getPrincipals()).containsOnly(sasquatch.getId());

		Authorization authorization2 =
				authorizationsServices.getAuthorization(zeCollection, task.getCreatedAuthorizations().get(1));
		assertThat(authorization2.getTarget()).isIn(asList(records.document_A19, records.document_A49));
		assertThat(authorization2.getRoles()).containsOnly(Role.READ);
		assertThat(authorization2.getPrincipals()).containsOnly(sasquatch.getId());

		assertThat(sasquatch.hasReadAccess().on(records.getDocumentWithContent_A19())).isTrue();
		assertThat(sasquatch.hasReadAccess().on(records.getDocumentWithContent_A49())).isTrue();
	}

	@Test
	public void givenTaskWithInvalidAuthorizationIdThenNoExceptionDuringCleanup() throws Exception {
		String id = "task1";
		createRMTask(id, sasquatch.getId(), null);

		RMTask task = rm.wrapRMTask(recordServices.getDocumentById(id));
		task.addCreatedAuthorizations(Collections.singletonList("fakeId"));
		task.setStatus(tasksSearchServices.getClosedStatus().getId());
		recordServices.update(task);
	}

	@Test
	public void givenTaskWithUserChangeThenAuthorizationsUpdatedProperly() throws Exception {
		String id = "task1";
		createRMTask(id, sasquatch.getId(), Collections.singletonList(records.folder_A01));

		assertThat(users.sasquatchIn(zeCollection).hasReadAccess().on(records.getFolder_A01())).isTrue();

		recordServices.update(rm.getRMTask(id).addCreatedAuthorizations(Collections.singletonList("fakeId"))
				.setAssignee(robin.getId()));

		assertThat(sasquatch.hasReadAccess().on(records.getFolder_A01())).isFalse();
		assertThat(robin.hasReadAccess().on(records.getFolder_A01())).isTrue();
	}

	private void createRMTask(String id, String assignee, List<String> folders)
			throws Exception {
		RMTask task = generatedSchemaRecordsServices.newRMTaskWithId(id);
		task.setTitle("Task1")
				.setLinkedFolders(folders)
				.setAssignee(assignee).setAssigner(users.adminIn(zeCollection).getId())
				.setAssignationDate(new LocalDate());
		recordServices.update(task);
	}

}
