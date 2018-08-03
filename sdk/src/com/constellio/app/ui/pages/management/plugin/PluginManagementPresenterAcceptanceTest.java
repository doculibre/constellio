package com.constellio.app.ui.pages.management.plugin;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class PluginManagementPresenterAcceptanceTest extends ConstellioTest {
	MockedNavigation navigator;
	@Mock PluginManagementView view;

	PluginManagementPresenter presenter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection());
		getAppLayerFactory();
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.navigate()).thenReturn(navigator);

		presenter = new PluginManagementPresenter(view);
	}

	@Test
	public void givenARobotWithASmbDocAsCriterionWhenGetDataProviderThenOk()
			throws Exception {
		presenter.getAllPlugins();
	}

}
