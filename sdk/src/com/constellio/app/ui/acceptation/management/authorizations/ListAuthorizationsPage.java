package com.constellio.app.ui.acceptation.management.authorizations;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.pages.management.authorizations.ListAuthorizationsViewImpl;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAuthorizationsPage extends PageHelper {
	protected ListAuthorizationsPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public ListAuthorizationsPage navigateToPrincipalPage(RecordWrapper principal) {
		driver.navigateTo().url(NavigatorConfigurationService.LIST_PRINCIPAL_ACCESS_AUTHORIZATIONS + "/" + principal.getId());
		return this;
	}

	public ListAuthorizationsPage navigateToObjectPage(RecordWrapper object) {
		driver.navigateTo().url(NavigatorConfigurationService.LIST_OBJECT_ACCESS_AUTHORIZATIONS + "/" + object.getId());
		return this;
	}

	public RecordContainerWebElement getOwnAuthorizationsTable() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(ListAuthorizationsViewImpl.AUTHORIZATIONS));
		return new RecordContainerWebElement(element);
	}

	public RecordContainerWebElement getInheritedAuthorizationsTable() {
		ConstellioWebElement element = driver.findRequiredElement(
				By.className(ListAuthorizationsViewImpl.INHERITED_AUTHORIZATIONS));
		return new RecordContainerWebElement(element);
	}
}
