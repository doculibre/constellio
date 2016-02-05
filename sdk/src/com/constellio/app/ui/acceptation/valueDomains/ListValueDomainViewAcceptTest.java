package com.constellio.app.ui.acceptation.valueDomains;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class ListValueDomainViewAcceptTest extends ConstellioTest {
	ConstellioWebDriver driver;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);

		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
	}

	@Test
	public void given()
			throws Exception {
		driver.navigateTo().url(NavigatorConfigurationService.LIST_VALUE_DOMAINS);
		waitUntilICloseTheBrowsers();
	}
}
