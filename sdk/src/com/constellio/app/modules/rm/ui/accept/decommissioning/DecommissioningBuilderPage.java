package com.constellio.app.modules.rm.ui.accept.decommissioning;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import org.openqa.selenium.By;

import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.tools.AutocompleteWebElement;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.CheckboxWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DecommissioningBuilderPage extends PageHelper {
	protected DecommissioningBuilderPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public DecommissioningBuilderPage navigateToPage(SearchType type) {
		driver.navigateTo().url(RMNavigationConfiguration.DECOMMISSIONING_LIST_BUILDER + "/" + type);
		return this;
	}

	public DecommissioningBuilderPage searchAndWaitForResults() {
		getSearchButton().click();
		driver.waitUntilElementExist(By.className(DecommissioningBuilderViewImpl.CREATE_LIST));
		return this;
	}

	public AutocompleteWebElement getAdministrativeUnit() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningBuilderViewImpl.ADMIN_UNIT));
		return new AutocompleteWebElement(element);
	}

	public ButtonWebElement getSearchButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningBuilderViewImpl.SEARCH));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getCreateButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(DecommissioningBuilderViewImpl.CREATE_LIST));
		return new ButtonWebElement(element);
	}

	public List<CheckboxWebElement> getAllResultCheckBoxes() {
		List<CheckboxWebElement> result = new ArrayList<>();
		for (ConstellioWebElement element : driver.findRequiredElements(By.xpath("//input[@type='checkbox']/.."))) {
			result.add(new CheckboxWebElement(element));
		}
		return result;
	}

	public RecordFormWebElement openCreateForm() {
		getCreateButton().click();
		driver.waitUntilElementExist(By.className(BaseForm.BASE_FORM));
		ConstellioWebElement element = driver.findRequiredElement(By.className(BaseForm.BASE_FORM));
		return new RecordFormWebElement(element);
	}
}
