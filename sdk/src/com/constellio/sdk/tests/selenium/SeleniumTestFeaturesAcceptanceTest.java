package com.constellio.sdk.tests.selenium;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.openqa.selenium.By;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class SeleniumTestFeaturesAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup schemas;
	private ConstellioWebDriver webDriver;

	@Before
	public void setup()
			throws Exception {
		schemas = new TestsSchemasSetup();
		defineSchemasManager().using(schemas.withATitle().withAContent().withAParsedContent());
		webDriver = newWebDriver(FakeSessionContext.adminInCollection(zeCollection));
	}

	//TODO Vincent : Faire un petit test selenium pour valider que la mécanique de changement de page fonctionne
	//	@Test
	public void whenUserSwitchFromPageToAnOtherThenSeleniumDetectPageChanges()
			throws InterruptedException {

		// Very usefull to debug tests :
		// pleaseWaitUntilICloseTheBrowsers();

		// Test this feature with a dummy application instead
		for (int i = 0; i < 25; i++) {
			webDriver.findElement(By.id("headerMenu_addFolderButton")).clickAndWaitForPageReload();
			assertTrue(webDriver.getPageSource().contains("Add folder"));

			webDriver.findElement(By.id("headerMenu_homeButton")).clickAndWaitForPageReload();
			assertTrue(webDriver.getPageSource().contains("Home page"));
		}
	}

}
