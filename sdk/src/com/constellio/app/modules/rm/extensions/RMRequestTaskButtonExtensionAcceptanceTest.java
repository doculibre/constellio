package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerPresenter;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderPresenter;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTaskType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.RequestTask;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Locale;

import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Constellio on 2017-04-03.
 */
public class RMRequestTaskButtonExtensionAcceptanceTest extends ConstellioTest {

	@Mock RMRequestTaskButtonExtension extension;
	@Mock DisplayFolderViewImpl folderView;
	@Mock DisplayContainerViewImpl containerView;
	@Mock UIContext uiContext;
	MockedNavigation navigator;

	private DisplayFolderPresenter folderPresenter;
	private DisplayContainerPresenter containerPresenter;
	private SessionContext sessionContext;
	private RecordVO folderVO;
	private RecordVO containerVO;
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private TasksSchemasRecordsServices taskSchemas;
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	@Before
	public void setup() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);

		extension = spy(new RMRequestTaskButtonExtension(zeCollection, getAppLayerFactory()));
		doNothing().when(extension).adjustButtons(any(BaseViewImpl.class), any(Folder.class), any(ContainerRecord.class), any(User.class));

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		taskSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		users.setUp(new UserServices(getModelLayerFactory()));

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		recordServices = getModelLayerFactory().newRecordServices();

		RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
		folderVO = recordToVOBuilder.build(records.getFolder_A42().getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, sessionContext);
		containerVO = recordToVOBuilder.build(records.getContainerBac13().getWrappedRecord(), RecordVO.VIEW_MODE.DISPLAY, sessionContext);

		when(folderView.getSessionContext()).thenReturn(sessionContext);
		when(folderView.getCollection()).thenReturn(zeCollection);
		when(folderView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(folderView.navigate()).thenReturn(navigator);
		when(folderView.getUIContext()).thenReturn(uiContext);
		when(folderView.getRecord()).thenReturn(folderVO);
		when(uiContext.getAttribute(FolderDocumentContainerBreadcrumbTrail.TAXONOMY_CODE)).thenReturn("plan");

		when(containerView.getSessionContext()).thenReturn(sessionContext);
		when(containerView.getCollection()).thenReturn(zeCollection);
		when(containerView.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(containerView.navigate()).thenReturn(navigator);
		when(containerView.getUIContext()).thenReturn(uiContext);

		folderPresenter = new DisplayFolderPresenter(folderView, null, false, false);
		containerPresenter = new DisplayContainerPresenter(containerView);
		when(containerView.getPresenter()).thenReturn(containerPresenter);
	}

	@Test
	public void givenBorrowButtonClickedForFolderThenCreateValidTask() {
		folderPresenter.forParams(records.folder_A42); //Crocodile
		extension.borrowRequest(folderView, false, "7");

		List<Task> tasks = taskSchemas.wrapTasks(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.userTask.schemaType()).returnAll())));

		assertThat(tasks.size()).isEqualTo(1);
		Task task = tasks.get(0);
		assertThatRecord(task).extracting(Task.LINKED_FOLDERS, RequestTask.ACCEPTED, RequestTask.APPLICANT, Task.TYPE, Task.TITLE)
				.containsOnly(asList(records.folder_A42), null, records.getAdmin().getId(),
						taskSchemas.getTaskTypeByCode(RMTaskType.BORROW_REQUEST).getId(), "Demande d'emprunt du dossier: Crocodile");
		assertThat(task.getAssigneeUsersCandidates()).containsOnly(records.getChuckNorris().getId(), records.getAdmin().getId());
	}

	@Test
	public void givenBorrowButtonClickedForContainerThenCreateValidTask() {
		folderPresenter.forParams(records.folder_A42); //Crocodile
		extension.borrowRequest(folderView, true, "7");
		List<Task> tasks = taskSchemas.wrapTasks(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.userTask.schemaType()).returnAll())));
		assertThat(tasks.size()).isEqualTo(1);
		Task task = tasks.get(0);
		assertThatRecord(task).extracting(Task.LINKED_CONTAINERS, RequestTask.ACCEPTED,
				RequestTask.APPLICANT, Task.TYPE, Task.TITLE).containsOnly(asList(records.containerId_bac13), null, records.getAdmin().getId(),
				taskSchemas.getTaskTypeByCode(RMTaskType.BORROW_REQUEST).getId(), "Demande d'emprunt du contenant: 10_A_06");
		assertThat(task.getAssigneeUsersCandidates()).containsOnly(records.getChuckNorris().getId(), records.getAdmin().getId());

		//Delete task because only one borrowTask from same user can be created.
		recordServices.physicallyDeleteNoMatterTheStatus(task.getWrappedRecord(), User.GOD, new RecordPhysicalDeleteOptions());


		containerPresenter.forParams(records.containerId_bac13); //Crocodile
		extension.borrowRequest(containerView, true, "7");
		tasks = taskSchemas.wrapTasks(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.userTask.schemaType()).where(Schemas.IDENTIFIER).isNotEqual(task.getId()))));
		assertThat(tasks.size()).isEqualTo(1);
		task = tasks.get(0);
		assertThatRecord(task).extracting(Task.LINKED_CONTAINERS, RequestTask.ACCEPTED,
				RequestTask.APPLICANT, Task.TYPE, Task.TITLE).containsOnly(asList(records.containerId_bac13), null, records.getAdmin().getId(),
				taskSchemas.getTaskTypeByCode(RMTaskType.BORROW_REQUEST).getId(), "Demande d'emprunt du contenant: 10_A_06");
		assertThat(task.getAssigneeUsersCandidates()).containsOnly(records.getChuckNorris().getId(), records.getAdmin().getId());
	}

	@Test
	public void givenReturnButtonClickedForFolderThenCreateValidTask() {
		folderPresenter.forParams(records.folder_A42); //Crocodile
		extension.returnRequest(folderView);

		List<Task> tasks = taskSchemas.wrapTasks(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.userTask.schemaType()).returnAll())));

		assertThat(tasks.size()).isEqualTo(1);
		Task task = tasks.get(0);
		assertThatRecord(task).extracting(Task.LINKED_FOLDERS, RequestTask.ACCEPTED,
				RequestTask.APPLICANT, Task.TYPE, Task.TITLE).containsOnly(asList(records.folder_A42), null, records.getAdmin().getId(),
				taskSchemas.getTaskTypeByCode(RMTaskType.RETURN_REQUEST).getId(), "Demande de retour du dossier: Crocodile");
		assertThat(task.getAssigneeUsersCandidates()).containsOnly(records.getChuckNorris().getId(), records.getAdmin().getId());
	}

	@Test
	public void givenReturnButtonClickedForContainerThenCreateValidTask() {
		containerPresenter.forParams(records.containerId_bac13); //Crocodile
		extension.returnRequest(containerView);
		List<Task> tasks = taskSchemas.wrapTasks(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.userTask.schemaType()).returnAll())));
		assertThat(tasks.size()).isEqualTo(1);
		Task task = tasks.get(0);
		assertThatRecord(task).extracting(Task.LINKED_CONTAINERS, RequestTask.ACCEPTED,
				RequestTask.APPLICANT, Task.TYPE, Task.TITLE).containsOnly(asList(records.containerId_bac13), null, records.getAdmin().getId(),
				taskSchemas.getTaskTypeByCode(RMTaskType.RETURN_REQUEST).getId(), "Demande de retour du contenant: 10_A_06");
		assertThat(task.getAssigneeUsersCandidates()).containsOnly(records.getChuckNorris().getId(), records.getAdmin().getId());
	}

	@Test
	public void givenReactivationButtonClickedForFolderThenCreateValidTask() {
		folderPresenter.forParams(records.folder_A42); //Crocodile
		extension.reactivationRequested(folderView, new RMRequestTaskButtonExtension.Request(new LocalDate().now(), RMRequestTaskButtonExtension.RequestType.REACTIVATION));

		List<Task> tasks = taskSchemas.wrapTasks(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.userTask.schemaType()).returnAll())));

		assertThat(tasks.size()).isEqualTo(1);
		Task task = tasks.get(0);
		assertThatRecord(task).extracting(Task.LINKED_FOLDERS, RequestTask.ACCEPTED,
				RequestTask.APPLICANT, Task.TYPE, Task.TITLE).containsOnly(asList(records.folder_A42), null, records.getAdmin().getId(),
				taskSchemas.getTaskTypeByCode(RMTaskType.REACTIVATION_REQUEST).getId(), "Demande de réactivation du dossier: Crocodile");
		assertThat(task.getAssigneeUsersCandidates()).containsOnly(records.getChuckNorris().getId(), records.getAdmin().getId());
	}

	@Test
	public void givenReactivationButtonClickedForContainerThenCreateValidTask() {
		containerPresenter.forParams(records.containerId_bac13); //Crocodile
		extension.reactivationRequested(containerView, new RMRequestTaskButtonExtension.Request(new LocalDate().now(), RMRequestTaskButtonExtension.RequestType.REACTIVATION));
		List<Task> tasks = taskSchemas.wrapTasks(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.userTask.schemaType()).returnAll())));
		assertThat(tasks.size()).isEqualTo(1);
		Task task = tasks.get(0);
		assertThatRecord(task).extracting(Task.LINKED_CONTAINERS, RequestTask.ACCEPTED,
				RequestTask.APPLICANT, Task.TYPE, Task.TITLE).containsOnly(asList(records.containerId_bac13), null, records.getAdmin().getId(),
				taskSchemas.getTaskTypeByCode(RMTaskType.REACTIVATION_REQUEST).getId(), "Demande de réactivation du contenant: 10_A_06");
		assertThat(task.getAssigneeUsersCandidates()).containsOnly(records.getChuckNorris().getId(), records.getAdmin().getId());
	}

	@Test
	public void givenBorrowExtensionButtonClickedForFolderThenCreateValidTask() {
		folderPresenter.forParams(records.folder_A42); //Crocodile
		extension.borrowExtensionRequested(folderView, new RMRequestTaskButtonExtension.Request(new LocalDate().now(), RMRequestTaskButtonExtension.RequestType.EXTENSION));

		List<Task> tasks = taskSchemas.wrapTasks(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.userTask.schemaType()).returnAll())));

		assertThat(tasks.size()).isEqualTo(1);
		Task task = tasks.get(0);
		assertThatRecord(task).extracting(Task.LINKED_FOLDERS, RequestTask.ACCEPTED,
				RequestTask.APPLICANT, Task.TYPE, Task.TITLE, ExtensionRequest.EXTENSION_VALUE).containsOnly(asList(records.folder_A42), null, records.getAdmin().getId(),
				taskSchemas.getTaskTypeByCode(RMTaskType.BORROW_EXTENSION_REQUEST).getId(), "Demande de renouvellement d'emprunt du dossier: Crocodile", LocalDate.now());
		assertThat(task.getAssigneeUsersCandidates()).containsOnly(records.getChuckNorris().getId(), records.getAdmin().getId());
	}

	@Test
	public void givenBorrowExtensionButtonClickedForContainerThenCreateValidTask() {
		containerPresenter.forParams(records.containerId_bac13); //Crocodile
		extension.borrowExtensionRequested(containerView, new RMRequestTaskButtonExtension.Request(new LocalDate().now(), RMRequestTaskButtonExtension.RequestType.EXTENSION));
		List<Task> tasks = taskSchemas.wrapTasks(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(rm.userTask.schemaType()).returnAll())));
		assertThat(tasks.size()).isEqualTo(1);
		Task task = tasks.get(0);
		assertThatRecord(task).extracting(Task.LINKED_CONTAINERS, RequestTask.ACCEPTED,
				RequestTask.APPLICANT, Task.TYPE, Task.TITLE, ExtensionRequest.EXTENSION_VALUE).containsOnly(asList(records.containerId_bac13), null, records.getAdmin().getId(),
				taskSchemas.getTaskTypeByCode(RMTaskType.BORROW_EXTENSION_REQUEST).getId(), "Demande de renouvellement d'emprunt du contenant: 10_A_06", LocalDate.now());
		assertThat(task.getAssigneeUsersCandidates()).containsOnly(records.getChuckNorris().getId(), records.getAdmin().getId());
	}
}
