/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.tools;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.constellio.app.ui.pages.management.configs.BaseComboBox;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class AutocompleteWebElement {
	private final String SUGGEST_POPUP = BaseComboBox.COMBO_BOX_STYLE;
	private final String DIV_XPATH = "//div[contains(@class,'" + SUGGEST_POPUP + "')]";
	private final String ROW_XPATH = DIV_XPATH + "//tr";

	private final ConstellioWebDriver driver;
	private final ConstellioWebElement element;

	public AutocompleteWebElement(ConstellioWebElement element) {
		this.element = element;
		driver = element.getWebDriver();
	}

	public AutocompleteWebElement typeAndSelectFirst(String text) {
		clear();
		getInputText().sendKeys(text);
		select(0);
		// This is a workaround for a problem on PhantomJS on OSX
		element.click();
		return this;
	}

	public List<String> getListValues() {
		List<String> texts = new ArrayList<>();
		expandOptions();

		List<ConstellioWebElement> elements = driver.waitUntilElementExist(By.xpath(DIV_XPATH)).findAdaptElements(
				By.tagName("tr"));
		for (ConstellioWebElement constellioWebElement : elements) {
			if (!constellioWebElement.getText().trim().isEmpty()) {
				texts.add(constellioWebElement.getText());
			}
		}
		getInputText().sendKeys(Keys.ENTER);
		return texts;
	}

	public AutocompleteWebElement clear() {
		new TextFieldWebElement(getInputText()).clear();
		return this;
	}

	public AutocompleteWebElement getEmptyValue() {
		this.selectItemContainingText("   ");
		return this;
	}

	public List<String> type(String text) {
		getInputText().sendKeys(text);
		return getValues();
	}

	public AutocompleteWebElement select(int index) {
		String xpathIndex = "[" + (index + 1) + "]";
		ConstellioWebElement choiceElement = driver.waitUntilElementExist(By.xpath(ROW_XPATH + xpathIndex + "//span"));
		choiceElement.click();
		return this;

	}

	public AutocompleteWebElement selectItemContainingText(String text) {
		this.expandOptions();

		List<ConstellioWebElement> listItem = element.findAdaptElements(By.xpath(ROW_XPATH + "//span"));
		int indexItemContainingText = 0;
		for (int i = 0; i < listItem.size(); i++) {

			if (listItem.get(i).getText().contains(text)) {
				indexItemContainingText = i;
			}
		}

		select(indexItemContainingText);
		return this;
	}

	public AutocompleteWebElement expandOptions() {
		for (int i = 0; i < 10; i++) {
			try {
				element.findElement(By.className("v-filterselect-button")).click();
				element.waitUntilElementExist(By.xpath(ROW_XPATH + "//span"), 300);
				return this;
			} catch (Exception e) {
				// continue trying
			}
		}
		throw new RuntimeException("Could not expand options");
	}

	public String getSelectedValue() {
		return getInputText().getAttribute("value");
	}

	protected ConstellioWebElement getInputText() {
		return element.findElement(By.tagName("input"));
	}

	protected ButtonWebElement getFilterselectButton() {
		ConstellioWebElement filterSelectedButton = element.findElement(By.className("v-filterselect-button"));
		return new ButtonWebElement(filterSelectedButton);
	}

	protected List<String> getValues() {
		driver.waitUntilElementExist(By.className(SUGGEST_POPUP));
		List<String> result = new ArrayList<>();
		for (ConstellioWebElement each : driver.findAdaptElements(By.xpath(ROW_XPATH + "//span"))) {
			result.add(each.getText());
		}
		return result;
	}

	public boolean isEnable() {
		return getInputText().isEnabled();
	}
}
