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
package com.constellio.app.ui.pages.management.configs;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.constellio.app.ui.tools.AutocompleteWebElement;
import com.constellio.app.ui.tools.BaseComboBoxWebElement;
import com.constellio.app.ui.tools.CheckboxWebElement;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ConfigGroupFacade {
	private ConstellioWebDriver driver;
	private ConstellioWebElement groupCodeTab;
	private List<WebElement> elements;
	private String title;
	private int elementValue;

	public ConfigGroupFacade(ConstellioWebDriver driver, String groupCode) {
		this.driver = driver;
		this.title = $("SystemConfigurationGroup." + groupCode);
		clickTab(groupCode);
		this.groupCodeTab = driver
				.findElement(By.id(groupCode));
		loadElements();
	}

	private void clickTab(String groupCode) {
		List<WebElement> tabs = this.driver.findElements(By.className("v-tabsheet-tabitem"));
		for(WebElement tab : tabs){
			String title = getTitle(tab);
			if (title.equals(this.title)){
				tab.click();
				return;
			}
		}
	}

	private String getTitle(WebElement tab) {
		return tab.findElement(By.className("v-captiontext")).getText();
	}

	private void loadElements() {
		groupCodeTab.click();
		elements = groupCodeTab.findElements(By.className(ConfigManagementViewImpl.CONFIG_ELEMENT_VALUE));
	}

	public int size() {
		return elements.size();
	}

	public String getElementValue(int i) {
		return elements.get(i).getAttribute("value");
	}

	public boolean getChecboxElementValue(int i) {
		return new CheckboxWebElement((ConstellioWebElement)elements.get(i)).isChecked();
	}

	public String getComboBoxElementValue(int i) {
		return new AutocompleteWebElement((ConstellioWebElement)elements.get(i)).getSelectedValue();
	}

	public void setElementValue(int i, String newValue) {
		ConstellioWebElement element = (ConstellioWebElement) elements.get(i);
		new TextFieldWebElement(element).setValue(newValue);
	}

	public void setComboBoxElementValue(int i, int newValueindex) {
		ConstellioWebElement element = (ConstellioWebElement)elements.get(i);
		new BaseComboBoxWebElement(element).selectFromListValues(newValueindex);
	}

	public void toggleCheckBox(int i) {
		new CheckboxWebElement((ConstellioWebElement)elements.get(i)).toggle();
	}
}
