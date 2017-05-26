package com.constellio.app.modules.tasks.ui.pages;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.modules.tasks.ui.pages.tasks.DisplayTaskPresenter;
import com.constellio.app.modules.tasks.ui.pages.tasks.DisplayTaskView;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.SDKViewNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.DISPLAY;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DisplayTaskPresenterAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	@Mock
	DisplayTaskView view;
	SessionContext sessionContext;
	private RecordServices recordServices;
	private SearchServices searchServices;
	private TasksSchemasRecordsServices tasksSchemas;
	DisplayTaskPresenter presenter;
	Task zeTask;
	private LocalDate testDate = LocalDate.now().minusDays(1);
	private TaskVO zeTaskAssociatedVO;
	RMTestRecords rmRecords = new RMTestRecords(zeCollection);

	// TODO Francis Baril faire passer sans RMModule.
	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withTasksModule().withAllTest(users));
		givenTimeIs(testDate);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);
		sessionContext.setCurrentCollection(zeCollection);
		recordServices.add(users.chuckNorrisIn(zeCollection).setCollectionWriteAccess(true).setCollectionReadAccess(true)
				.setCollectionDeleteAccess(true));

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		new SDKViewNavigation(view);

		presenter = new DisplayTaskPresenter(view);
		zeTask = tasksSchemas.newTask().setTitle("zeTask");
		recordServices.add(zeTask);
		zeTaskAssociatedVO = new TaskVO(new RecordToVOBuilder().build(zeTask.getWrappedRecord(), DISPLAY, sessionContext));
	}

	@Test
	public void whenDisplayTaskThenTaskLoadedCorrectly()
			throws Exception {
		presenter.initTaskVO(zeTask.getId());
		TaskVO taskVO = (TaskVO) presenter.getTask();
		assertThat(taskVO.getTitle()).isEqualTo("zeTask");
	}

	@Test
	public void whenDeleteTaskThenTaskDeletedCorrectly()
			throws Exception {
		presenter.initTaskVO(zeTask.getId());
		presenter.deleteButtonClicked();
		LogicalSearchCondition allTasksQuery = from(
				tasksSchemas.userTask.schema()).returnAll();
		Task deletedTask = tasksSchemas.wrapTask(searchServices.searchSingleResult(allTasksQuery));
		assertThat(deletedTask.getLogicallyDeletedStatus()).isTrue();
	}

	@Test
	public void givenEventsThenEventsDataProviderReturnValidEvents() throws Exception {
		givenDisabledAfterTestValidations();
		prepareSystem(
				withCollection("collectionTest").withConstellioRMModule().withTasksModule().withAllTest(users)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);

		sessionContext = FakeSessionContext.adminInCollection("collectionTest");
		sessionContext.setCurrentLocale(Locale.FRENCH);
		sessionContext.setCurrentCollection("collectionTest");

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn("collectionTest");
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		new SDKViewNavigation(view);

		presenter = new DisplayTaskPresenter(view);

		Task zeTask2 = tasksSchemas.newTask().setTitle("zeTask2");
		recordServices.add(zeTask2);

		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices("collectionTest", getAppLayerFactory());
		RMEventsSearchServices rmEventsSearchServices = new RMEventsSearchServices(getModelLayerFactory(), "collectionTest");
		Transaction transaction = new Transaction();
		transaction.add(rmSchemasRecordsServices.newEvent().setRecordId(zeTask.getId())
				.setTitle(zeTask.getTitle()).setUsername(users.adminIn("collectionTest").getUsername()).setType(EventType.MODIFY_TASK)
				.setCreatedOn(LocalDateTime.now()));
		transaction.add(rmSchemasRecordsServices.newEvent().setRecordId(zeTask2.getId())
				.setTitle(zeTask2.getTitle()).setUsername(users.adminIn("collectionTest").getUsername()).setType(EventType.MODIFY_TASK)
				.setCreatedOn(LocalDateTime.now()));
		recordServices.execute(transaction);

		getDataLayerFactory().newEventsDao().flush();
		assertThat(searchServices.getResultsCount(rmEventsSearchServices.newFindEventByRecordIDQuery(users.adminIn("collectionTest"), zeTask.getId()))).isEqualTo(1);
		assertThat(searchServices.getResultsCount(rmEventsSearchServices.newFindEventByRecordIDQuery(users.adminIn("collectionTest"), zeTask2.getId()))).isEqualTo(1);

		presenter.initTaskVO(zeTask.getId());
		RecordVODataProvider provider = presenter.getEventsDataProvider();
		List<RecordVO> eventList = provider.listRecordVOs(0, 100);
		assertThat(eventList).hasSize(1);
	}

}
