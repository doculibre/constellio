package com.constellio.app.modules.rm.ui.accept.decommissioning;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl.DecommissioningButton;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class DecommissioningBuilderViewAcceptTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConstellioWebDriver driver;
	DecommissioningBuilderPage page;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
	}

	@Test
	public void givenAdministratorTWhenAdministrativeUnitSelectedThenSearchButtonEnabled() {
		givenTransfer();

		assertThat(page.getSearchButton().isEnabled()).isFalse();

		page.getAdministrativeUnit().typeAndSelectFirst("10A");
		assertThat(page.getSearchButton().isEnabled()).isTrue();

	}

	@Test
	public void givenAdministratorThenCanCreateListToCloseWithFixedPeriod() {
		givenCloseWithFixedPeriod();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListToCloseWithCode888() {
		givenCloseWithCode888();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListToCloseWithCode999() {
		givenCloseWithCode999();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListToTransfer() {
		givenTransfer();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListOfActiveToDeposit() {
		givenActiveToDeposit();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListOfActiveToDestroy() {
		givenActiveToDestroy();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListOfSemiActiveToDeposit() {
		givenSemiActiveToDeposit();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListOfSemiActiveToDestroy() {
		givenSemiActiveToDestroy();
		createNewList();
	}

	private void givenCloseWithFixedPeriod() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.fixedPeriod);
	}

	private void givenCloseWithCode888() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.code888);
	}

	private void givenCloseWithCode999() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.code999);
	}

	private void givenTransfer() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.transfer);
	}

	private void givenActiveToDeposit() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.activeToDeposit);
	}

	private void givenActiveToDestroy() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.activeToDestroy);
	}

	private void givenSemiActiveToDeposit() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.semiActiveToDeposit);
	}

	private void givenSemiActiveToDestroy() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.semiActiveToDestroy);
	}

	private void createNewList() {
		page.getAdministrativeUnit().typeAndSelectFirst("10A");
		ButtonWebElement create = page.searchAndWaitForResults().getCreateButton();
		assertThat(create.isEnabled()).isFalse();

		page.getAllResultCheckBoxes().get(0).toggle();
		assertThat(create.isEnabled()).isTrue();

		page.openCreateForm()
				.setValue(DecommissioningButton.TITLE, "New list")
				.setValue(DecommissioningButton.DESCRIPTION, "A description")
				.clickSaveButtonAndWaitForPageReload();
	}
}
