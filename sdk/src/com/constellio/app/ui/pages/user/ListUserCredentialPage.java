package com.constellio.app.ui.pages.user;

import java.util.List;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BackButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListUserCredentialPage extends PageHelper {

	public ListUserCredentialPage(ConstellioWebDriver driver) {
		super(driver);
	}

	void navigateToListUserCredentialsPage() {

		driver.navigateTo()
				.url(NavigatorConfigurationService.USER_LIST);
		try {
			getAddButton();

		} catch (Exception e) {
			driver.printHierarchy();
			driver.navigateTo()
					.url(NavigatorConfigurationService.USER_LIST);
			try {
				getAddButton();

			} catch (Exception e2) {
				driver.printHierarchy();
				driver.navigateTo()
						.url(NavigatorConfigurationService.USER_LIST);
			}
		}
	}

	public TextFieldWebElement getSearchInput() {
		ConstellioWebElement element = driver.findAdaptElements(By.tagName("input")).get(1);
		element.scrollIntoView();
		return new TextFieldWebElement(element);
	}

	public ButtonWebElement getAddButton() {
		try {
			return getButtonByClassName(AddButton.BUTTON_STYLE, 0);
		} catch (RuntimeException e) {
			driver.printHierarchy();
			throw e;
		}
	}

	public ButtonWebElement getBackButton() {
		return getButtonByClassName(BackButton.BUTTON_STYLE, 0);
	}

	public ButtonWebElement getSearchButton() {
		return getButtonByClassName(SearchButton.STYLE_NAME, 1);
	}

	public ButtonWebElement getDisplayUserCredentialButtonOnIndex(int index) {
		return getButtonByClassName(DisplayButton.BUTTON_STYLE, index);
	}

	public ButtonWebElement getEditUserCredentialButtonOnIndex(int index) {
		return getButtonByClassName(EditButton.BUTTON_STYLE, index);
	}

	public List<ConstellioWebElement> getTableRows() {
		ConstellioWebElement tableElement = driver.findAdaptElements(By.tagName("table")).get(1);
		List<ConstellioWebElement> rows = tableElement.findAdaptElements(By.tagName("tr"));
		return rows;
	}
}
