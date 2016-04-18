package com.constellio.app.ui.pages.setup;

import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;

@UiTest
@InDevelopmentTest
public class ConstellioSetupPresenterAcceptanceTest extends ConstellioTest {

	@Mock ConstellioSetupView view;

	@Test
	public void testName()
			throws Exception {
		when(view.getSessionContext()).thenReturn(FakeSessionContext.noUserNoCollection());
		ConstellioFactories constellioFactories = getConstellioFactories();
		when(view.getConstellioFactories()).thenReturn(constellioFactories);
		ConstellioSetupPresenter presenter = new ConstellioSetupPresenter(view);
		presenter.saveRequested("fr", Arrays.asList("fr"), Arrays.asList("rm"), "ZerCollection", "zeColl", "supertimor");
		newWebDriver();
		waitUntilICloseTheBrowsers();
	}
}
