package com.constellio.app.modules.tasks.ui.pages;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Locale;

import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;
import org.mockito.Mockito;
import org.mockito.Spy;

public class TaskManagementPresenterAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	@Mock
	TaskManagementView view;
	MockedNavigation navigator;
	SessionContext sessionContext;
	private RecordServices recordServices;
	private SearchServices searchServices;
	TaskManagementPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigate()).thenReturn(navigator);
	}

	@Test
	public void givenFolderIdMetadataWhenIsRecordIdMetadataThenReturnTrue()
			throws Exception {

		//assertThat(presenter.isRecordIdMetadata(metadataValueVO)).isTrue();
	}

	@Test
	public void givenWorkflowsAreActivatedThenOnlyUsersWithNeededPermissionCanSeeTheTab()
			throws Exception {
		TaskManagementPresenter presenter = Mockito.spy(new TaskManagementPresenter(view));
		doReturn(true).when(presenter).areWorkflowsEnabled();

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		assertThat(presenter.getTabs()).contains(presenter.WORKFLOWS_STARTED);

		sessionContext = FakeSessionContext.aliceInCollection(zeCollection);
		presenter = Mockito.spy(new TaskManagementPresenter(view));
		assertThat(presenter.getTabs()).doesNotContain(presenter.WORKFLOWS_STARTED);
	}
}
