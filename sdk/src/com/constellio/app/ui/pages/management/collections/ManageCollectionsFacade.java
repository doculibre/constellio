package com.constellio.app.ui.pages.management.collections;

import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.app.ui.tools.RecordContainerWebElement.RecordContainerWebElementRow;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ManageCollectionsFacade {
	private final ConstellioWebDriver driver;
	private final ConstellioWebElement addButton;
	private RecordContainerWebElement table;

	public ManageCollectionsFacade(ConstellioWebDriver driver) {
		this.driver = driver;
		this.table = new RecordContainerWebElement(driver.find(CollectionManagementViewImpl.TABLE_STYLE_CODE));
		this.addButton = driver.find(CollectionManagementViewImpl.ADD_BUTTON_STYLE);
	}

	public int getCollectionSize() {
		return table.countRows();
	}

	public String getCollectionCode(int i) {
		RecordContainerWebElementRow element = table.getRow(i);
		return element.getValueInColumn(0);
	}

	public String getCollectionName(int i) {
		RecordContainerWebElementRow element = table.getRow(i);
		return element.getValueInColumn(1);
	}

	public void clickModifyCollection(int i) {
		RecordContainerWebElementRow element = table.getRow(i);
		element.clickButton(CollectionManagementViewImpl.EDIT_BUTTON_STYLE);
	}

	public void clickAddCollection() {
		int timeoutInSeconds = 100;
		String lastPageDateString = driver.getPageLoadTimeAsString(100);
		addButton.click();
		driver.waitForPageReload(timeoutInSeconds, lastPageDateString);
	}

	public void clickRemoveCollectionAndValidate(int i) {
		RecordContainerWebElementRow element = table.getRow(i);
		//element.getButton(CollectionManagementViewImpl.DELETE_BUTTON_STYLE).click();
		element.clickButtonAndConfirm(CollectionManagementViewImpl.DELETE_BUTTON_STYLE);
	}

	public void clickRemoveCollectionAndCancel(int i) {
		RecordContainerWebElementRow element = table.getRow(i);
		element.clickButtonAndCancel(CollectionManagementViewImpl.DELETE_BUTTON_STYLE);
	}

	public boolean deleteButtonEnabled(int i) {
		RecordContainerWebElementRow element = table.getRow(i);
		return element.getButton(CollectionManagementViewImpl.DELETE_BUTTON_STYLE).isEnabled();
	}
}
