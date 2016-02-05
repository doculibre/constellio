package com.constellio.app.modules.rm.ui.accept.decommissioning;

import org.openqa.selenium.By;

import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningMainViewImpl;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DecommissioningMainPage extends PageHelper {
	protected DecommissioningMainPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public DecommissioningMainPage navigateToPage() {
		driver.navigateTo().url(NavigatorConfigurationService.DECOMMISSIONING);
		return this;
	}

	public ButtonWebElement getCreationLink(SearchType type) {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningMainViewImpl.CREATE + type));
		return new ButtonWebElement(element);
	}

	public DecommissioningBuilderPage goToBuilder(SearchType type) {
		getCreationLink(type).clickAndWaitForPageReload();
		return new DecommissioningBuilderPage(driver);
	}
}
