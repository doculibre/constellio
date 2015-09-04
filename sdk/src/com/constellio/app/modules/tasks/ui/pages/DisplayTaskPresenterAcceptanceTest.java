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
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
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
	ConstellioNavigator navigator;
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
		givenTimeIs(testDate);
		givenCollection(zeCollection).withTaskModule().withAllTestUsers();

		users.setUp(getModelLayerFactory().newUserServices());

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
