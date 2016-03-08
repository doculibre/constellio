package com.constellio.app.modules.tasks.ui.pages;

import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.DISPLAY;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.modules.tasks.ui.pages.tasks.DisplayTaskPresenter;
import com.constellio.app.modules.tasks.ui.pages.tasks.DisplayTaskView;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class DisplayTaskPresenterAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	@Mock
	DisplayTaskView view;
	@Mock
	CoreViews navigator;
	SessionContext sessionContext;
	private RecordServices recordServices;
	private SearchServices searchServices;
	private TasksSchemasRecordsServices tasksSchemas;
	DisplayTaskPresenter presenter;
	Task zeTask;
	private LocalDate testDate = LocalDate.now().minusDays(1);
	private TaskVO zeTaskAssociatedVO;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));
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
		when(view.navigateTo()).thenReturn(navigator);

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
		assertThat(searchServices.getResultsCount(allTasksQuery)).isEqualTo(0);
	}

}
