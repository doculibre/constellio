package com.constellio.app.ui.pages.management.plugin;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class PluginManagementPresenterAcceptanceTest extends ConstellioTest {
	@Mock ConstellioNavigator navigator;
	@Mock PluginManagementView view;

	PluginManagementPresenter presenter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection());
		getAppLayerFactory();
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.navigateTo()).thenReturn(navigator);

		presenter = new PluginManagementPresenter(view);
	}

	@Test
	public void givenARobotWithASmbDocAsCriterionWhenGetDataProviderThenOk()
			throws Exception {
		presenter.getAllPlugins();
	}

}
