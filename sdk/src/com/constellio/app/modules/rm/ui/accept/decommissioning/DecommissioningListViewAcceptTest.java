package com.constellio.app.modules.rm.ui.accept.decommissioning;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class DecommissioningListViewAcceptTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConstellioWebDriver driver;
	DecommissioningListPage page;

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
	@InDevelopmentTest
	public void openPage() {
		giveListNotApprovedAndNotValidated();
		waitUntilICloseTheBrowsers();
	}

	@Test
	@Ignore
	public void givenListsOfManyStatesThenActionsAreAvailableAccordingToTheListState() {
		givenHybridListToClose();
		assertThat(page.getEditButton().isEnabled()).isTrue();
		assertThat(page.getDeleteButton().isEnabled()).isTrue();
		assertThat(page.getProcessButton().isEnabled()).isTrue();

		givenAnalogicListToTransfer();
		assertThat(page.getEditButton().isEnabled()).isTrue();
		assertThat(page.getDeleteButton().isEnabled()).isTrue();
		assertThat(page.getProcessButton().isEnabled()).isFalse();

		givenAlreadyProcessedList();
		assertThat(page.getEditButton().isEnabled()).isFalse();
		assertThat(page.getDeleteButton().isEnabled()).isFalse();
		assertThat(page.getProcessButton().isEnabled()).isFalse();

		givenListRequestedAndNotApproved();
		assertThat(page.getApprovalButton().isEnabled()).isTrue();
		assertThat(page.getApprovalRequestButton().isEnabled()).isFalse();
		assertThat(page.getProcessButton().isEnabled()).isFalse();

		givenListRequestedAndApproved();
		assertThat(page.getApprovalButton().isEnabled()).isFalse();
		assertThat(page.getApprovalRequestButton().isEnabled()).isFalse();
		assertThat(page.getProcessButton().isEnabled()).isTrue();

		givenListApprovedAndValidated();
		assertThat(page.getApprovalRequestButton().isEnabled()).isFalse();
		assertThat(page.getApprovalButton().isEnabled()).isFalse();
		assertThat(page.getProcessButton().isEnabled()).isTrue();
		assertThat(page.getValidationRequestButton().isEnabled()).isFalse();
		assertThat(page.getValidationButton().isEnabled()).isFalse();

		giveListNotApprovedAndNotValidated();
		assertThat(page.getApprovalRequestButton().isEnabled()).isFalse();
		assertThat(page.getApprovalButton().isEnabled()).isFalse();
		assertThat(page.getProcessButton().isEnabled()).isFalse();
		assertThat(page.getValidationRequestButton().isEnabled()).isFalse();
		assertThat(page.getValidationButton().isEnabled()).isTrue();

	}

	private void givenHybridListToClose() {
		page = new DecommissioningListPage(driver, records.list_03).navigateToPage();
	}

	private void givenAnalogicListToTransfer() {
		page = new DecommissioningListPage(driver, records.list_04).navigateToPage();
	}

	public void givenAlreadyProcessedList() {
		page = new DecommissioningListPage(driver, records.list_11).navigateToPage();
	}

	public void givenListRequestedAndApproved() {
		page = new DecommissioningListPage(driver, records.list_03).navigateToPage();
	}

	public void givenListRequestedAndNotApproved() {
		page = new DecommissioningListPage(driver, records.list_23).navigateToPage();
	}

	public void givenListApprovedAndValidated() {
		page = new DecommissioningListPage(driver, records.list_24).navigateToPage();
	}

	public void giveListNotApprovedAndNotValidated() {
		page = new DecommissioningListPage(driver, records.list_25).navigateToPage();
	}
}
