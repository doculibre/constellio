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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class CheckboxWebElement {
	private final ConstellioWebElement element;
	private final String SUGGEST_POPUP = "v-filterselect-suggestmenu";
	private final String DIV_XPATH = "//div[contains(@class,'" + SUGGEST_POPUP + "')]";
	private final String ROW_XPATH = DIV_XPATH + "//tr";

	public CheckboxWebElement(ConstellioWebElement element) {
		this.element = element;
	}

	public boolean isEnabled() {
		return element.findElement(By.tagName("input")).isEnabled();
	}

	public boolean isChecked() {
		return element.findElement(By.tagName("input")).isSelected();
	}

	public String getCaptionText() {
		return element.findElement(By.tagName("label")).getText();
	}

	public CheckboxWebElement toggle() {
		ConstellioWebElement input = element.findElement(By.tagName("input"));
		input.click();
		return this;
	}
}
