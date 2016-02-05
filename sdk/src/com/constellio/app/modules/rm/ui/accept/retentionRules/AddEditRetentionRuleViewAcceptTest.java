package com.constellio.app.modules.rm.ui.accept.retentionRules;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class AddEditRetentionRuleViewAcceptTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConstellioWebDriver driver;
	AddEditRetentionRulePage page;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		page = new AddEditRetentionRulePage(driver).navigateToPage();
	}

	@Test
	@InDevelopmentTest
	public void openThePage() {
		waitUntilICloseTheBrowsers();
	}

	@Test
	@InDevelopmentTest
	public void loadRetentionRuleForm() {
		RecordFormWebElement form = page.getForm();

		waitUntilICloseTheBrowsers();
	}
}
