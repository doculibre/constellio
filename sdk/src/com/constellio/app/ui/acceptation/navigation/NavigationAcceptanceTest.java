package com.constellio.app.ui.acceptation.navigation;

import org.junit.Before;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class NavigationAcceptanceTest extends ConstellioTest {

	ConstellioWebDriver driver;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);
		driver = newWebDriver(FakeSessionContext.adminInCollection(zeCollection));

	}

}
