package com.constellio.app.ui.pages.globalGroup;

import java.util.List;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BackButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListGlobalGroupPage extends PageHelper {

	public ListGlobalGroupPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public void navigateToListGlobalGroupsPage() {
		driver.navigateTo()
				.url(NavigatorConfigurationService.GROUP_LIST);
	}

	public TextFieldWebElement getSearchInput() {
		ConstellioWebElement element = driver.findAdaptElements(By.tagName("input")).get(1);
		element.scrollIntoView();
		return new TextFieldWebElement(element);
	}

	public ButtonWebElement getAddButton() {
		return getButtonByClassName(AddButton.BUTTON_STYLE, 0);
	}

	public ButtonWebElement getBackButton() {
		return getButtonByClassName(BackButton.BUTTON_STYLE, 0);
	}

	public ButtonWebElement getSearchButton() {
		return getButtonByClassName(SearchButton.STYLE_NAME, 1);
	}

	public ButtonWebElement getDisplayGlobalGroupButtonOnIndex(int index) {
		return getButtonByClassName(DisplayButton.BUTTON_STYLE, index);
	}

	public ButtonWebElement getEditGlobalGroupButtonOnIndex(int index) {
		return getButtonByClassName(EditButton.BUTTON_STYLE, index);
	}

	public ButtonWebElement getDeleteButtonOnIndex(int index) {
		return getButtonByClassName(DeleteButton.BUTTON_STYLE, index);
	}

	public List<ConstellioWebElement> getTableRows() {
		ConstellioWebElement tableElement = driver.findAdaptElements(By.tagName("table")).get(1);
		List<ConstellioWebElement> rows = tableElement.findAdaptElements(By.tagName("tr"));
		return rows;
	}
}
