package com.constellio.app.ui.acceptation.collection;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.pages.collection.CollectionUserRolesViewImpl;
import com.constellio.app.ui.tools.AutocompleteWebElement;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import org.openqa.selenium.By;

public class CollectionUserRolesPage extends PageHelper {
	protected CollectionUserRolesPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public CollectionUserRolesPage navigateToPage(User user) {
		driver.navigateTo().url(NavigatorConfigurationService.COLLECTION_USER_ROLES + "/" + user.getId());
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

	public CollectionUserRolesPage addRole(String role) {
		getRoleLookup().typeAndSelectFirst(role);
		getAddRoleButton().click();
		return this;
	}

	public RecordContainerWebElement getRoleTable() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(CollectionUserRolesViewImpl.ROLES));
		return new RecordContainerWebElement(element);
	}
}
