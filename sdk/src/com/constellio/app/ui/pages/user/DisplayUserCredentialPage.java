package com.constellio.app.ui.pages.user;

import java.util.List;

import org.openqa.selenium.By;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BackButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DisplayUserCredentialPage extends PageHelper {

	public DisplayUserCredentialPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public TextFieldWebElement getSearchInputUsersGroups() {
		ConstellioWebElement element = driver.findAdaptElements(By.tagName("input")).get(1);
		return new TextFieldWebElement(element);
	}

	public TextFieldWebElement getSearchInputGroups() {
		ConstellioWebElement element = driver.findAdaptElements(By.tagName("input")).get(2);
		return new TextFieldWebElement(element);
	}

	public ButtonWebElement getAddButtonOnIndex(int index) {
		return getButtonByClassName(AddButton.BUTTON_STYLE, index);
	}

	public ButtonWebElement getBackButton() {
		return getButtonByClassName(BackButton.BUTTON_STYLE, 0);
	}

	public ButtonWebElement getSearchButtonUsersGroups() {
		return getButtonByClassName(SearchButton.STYLE_NAME, 1);
	}

	public ButtonWebElement getSearchButtonGroups() {
		return getButtonByClassName(SearchButton.STYLE_NAME, 2);
	}

	public ButtonWebElement getEditGlobalGroupButtonOnIndex(int index) {
		return getButtonByClassName(EditButton.BUTTON_STYLE, index);
	}

	public ButtonWebElement getEditGlobalGroupButtonMenuAction() {
		return getButtonByCssSelector("." + EditButton.BUTTON_STYLE + ".action-menu-button");
	}

	public ButtonWebElement getEditUserCredentialButtonOnIndex(int index) {
		return getButtonByClassName(EditButton.BUTTON_STYLE, index);
	}

//	public List<ConstellioWebElement> findEditButtonElements() {
	//		return driver.findAdaptElements(By.className(EditButton.BUTTON_STYLE));
	//	}
	//
	//	public List<ConstellioWebElement> findAddButtonElements() {
	//		return driver.findAdaptElements(By.className(AddButton.BUTTON_STYLE));
	//	}

	private ButtonWebElement getButtonByCssSelector(String cssSelectors) {
		ConstellioWebElement element = driver
				.findElement(By.cssSelector(cssSelectors));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getDeleteButtonOnIndex(int index) {
		return getButtonByClassName(DeleteButton.BUTTON_STYLE, index);
	}

	public List<ConstellioWebElement> getTableRowsUsersGroups() {
		ConstellioWebElement tableElement = driver.findAdaptElements(By.tagName("table")).get(1);
		List<ConstellioWebElement> rows = tableElement.findAdaptElements(By.tagName("tr"));
		return rows;
	}

	public List<ConstellioWebElement> getTableRowsGroups() {
		ConstellioWebElement tableElement = driver.findAdaptElements(By.tagName("table")).get(4);
		List<ConstellioWebElement> rows = tableElement.findAdaptElements(By.tagName("tr"));
		return rows;
	}
}
