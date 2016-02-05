package com.constellio.app.ui.acceptation.collection;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.pages.collection.CollectionUserRolesViewImpl;
import com.constellio.app.ui.tools.AutocompleteWebElement;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class CollectionGroupRolesPage extends PageHelper {
	protected CollectionGroupRolesPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public CollectionGroupRolesPage navigateToPage(Group group) {
		driver.navigateTo().url(NavigatorConfigurationService.COLLECTION_GROUP_ROLES + "/" + group.getId());
		return this;
	}

	public AutocompleteWebElement getRoleLookup() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(CollectionUserRolesViewImpl.ROLE_SELECTOR));
		return new AutocompleteWebElement(element);
	}

	public ButtonWebElement getAddRoleButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(CollectionUserRolesViewImpl.ADD_ROLE));
		return new ButtonWebElement(element);
	}

	public CollectionGroupRolesPage addRole(String role) {
		getRoleLookup().typeAndSelectFirst(role);
		getAddRoleButton().click();
		return this;
	}

	public RecordContainerWebElement getRoleTable() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(CollectionUserRolesViewImpl.ROLES));
		return new RecordContainerWebElement(element);
	}
}
