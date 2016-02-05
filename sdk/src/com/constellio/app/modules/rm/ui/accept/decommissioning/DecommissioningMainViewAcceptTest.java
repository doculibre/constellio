package com.constellio.app.modules.rm.ui.accept.decommissioning;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMConfigs.DecommissioningPhase;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class DecommissioningMainViewAcceptTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConstellioWebDriver driver;
	DecommissioningMainPage page;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		driver = newWebDriver(loggedAsUserInCollection(chuckNorris, zeCollection));
		page = new DecommissioningMainPage(driver).navigateToPage();
	}

	@Test
	@InDevelopmentTest
	public void openPage() {
		records.withDocumentsHavingContent();
		getConfigurationManager().setValue(RMConfigs.MINOR_VERSIONS_PURGED_ON, DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT);
		getConfigurationManager().setValue(RMConfigs.PDFA_CREATED_ON, DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT);

		waitUntilICloseTheBrowsers();
	}

	@Test
	public void givenAdministratorThenCanClickOnAllCreationLinks() {
		DecommissioningBuilderPage builderPage = page.goToBuilder(SearchType.fixedPeriod);
		assertThat(builderPage.getTitle()).contains("durée fixe");

		builderPage.getBackButton().clickAndWaitForPageReload();
		assertThat(page.goToBuilder(SearchType.code888).getTitle()).contains("code 888");

		builderPage.getBackButton().clickAndWaitForPageReload();
		assertThat(page.goToBuilder(SearchType.code999).getTitle()).contains("code 999");

		builderPage.getBackButton().clickAndWaitForPageReload();
		assertThat(page.goToBuilder(SearchType.transfer).getTitle()).contains("transférer au semi-actif");

		builderPage.getBackButton().clickAndWaitForPageReload();
		assertThat(page.goToBuilder(SearchType.activeToDeposit).getTitle()).contains(" actifs à verser");

		builderPage.getBackButton().clickAndWaitForPageReload();
		assertThat(page.goToBuilder(SearchType.activeToDestroy).getTitle()).contains(" actifs à détruire");

		builderPage.getBackButton().clickAndWaitForPageReload();
		assertThat(page.goToBuilder(SearchType.semiActiveToDeposit).getTitle()).contains("semi-actifs à verser");

		builderPage.getBackButton().clickAndWaitForPageReload();
		assertThat(page.goToBuilder(SearchType.semiActiveToDestroy).getTitle()).contains("semi-actifs à détruire");
	}

	private SystemConfigurationsManager getConfigurationManager() {
		return getModelLayerFactory().getSystemConfigurationsManager();
	}
}
