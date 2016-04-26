package com.constellio.app.modules.rm.ui.accept.decommissioning;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import org.openqa.selenium.By;

import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningListViewImpl;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DecommissioningListPage extends PageHelper {
	private final String recordId;

	protected DecommissioningListPage(ConstellioWebDriver driver, String recordId) {
		super(driver);
		this.recordId = recordId;
	}

	public DecommissioningListPage navigateToPage() {
		driver.navigateTo().url(RMNavigationConfiguration.DECOMMISSIONING_LIST_DISPLAY + "/" + recordId);
		return this;
	}

	public ButtonWebElement getEditButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(EditButton.BUTTON_STYLE));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getDeleteButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DeleteButton.BUTTON_STYLE));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getProcessButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningListViewImpl.PROCESS));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getApprovalRequestButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningListViewImpl.APPROVAL_REQUEST_BUTTON));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getApprovalButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningListViewImpl.APPROVAL_BUTTON));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getValidationButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningListViewImpl.VALIDATION_BUTTON));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getValidationRequestButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningListViewImpl.VALIDATION_REQUEST_BUTTON));
		return new ButtonWebElement(element);
	}
}
