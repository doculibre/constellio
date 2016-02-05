package com.constellio.app.modules.tasks.ui.pages;

import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class TaskManagementPresenterAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	@Mock
	TaskManagementView view;
	@Mock
	ConstellioNavigator navigator;
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
		when(view.navigateTo()).thenReturn(navigator);
	}

	@Test
	public void givenFolderIdMetadataWhenIsRecordIdMetadataThenReturnTrue()
			throws Exception {

		//assertThat(presenter.isRecordIdMetadata(metadataValueVO)).isTrue();
	}
}
