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

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class OptionGroupWebElement {

	ConstellioWebElement nestedElement;

	public OptionGroupWebElement(ConstellioWebElement nestedElement) {
		this.nestedElement = nestedElement;
	}

	public boolean isMultiSelect() {
		List<ConstellioWebElement> elements = nestedElement.getChildren();
		for (ConstellioWebElement element : elements) {
			ConstellioWebElement inputElement = element.findElement(By.tagName("input"));
			if (inputElement.getAttribute("type") != null) {
				String type = inputElement.getAttribute("type");
				boolean isMultiSelect = type.contains("radio") ? false : true;
				return isMultiSelect;
			}
		}
		return true;
	}

	public boolean isEnabled(String value) {
		List<ConstellioWebElement> elements = nestedElement.getChildren();
		for (ConstellioWebElement element : elements) {
			CheckboxWebElement checkboxWebElement = new CheckboxWebElement(element);
			if (checkboxWebElement.getCaptionText().equals(value)) {
				return checkboxWebElement.isEnabled();
			}
		}
		return false;
	}

	public OptionGroupWebElement toggle(String value) {
		List<ConstellioWebElement> elements = nestedElement.getChildren();
		for (ConstellioWebElement element : elements) {
			CheckboxWebElement checkboxWebElement = new CheckboxWebElement(element);
			if (checkboxWebElement.getCaptionText().contains(value)) {
				checkboxWebElement.toggle();
			}
		}
		return this;
	}
	
	public void toggleContaining(String value) {
		List<ConstellioWebElement> elements = nestedElement.getChildren();
		for (ConstellioWebElement element : elements) {
            CheckboxWebElement checkboxWebElement = new CheckboxWebElement(element);
			if (checkboxWebElement.getCaptionText().contains(value)) {
				checkboxWebElement.toggle();
			}
		}
	}

	public List<String> getAllValues() {
		List<String> values = new ArrayList<>();
		List<ConstellioWebElement> elements = nestedElement.getChildren();
		for (ConstellioWebElement element : elements) {
			CheckboxWebElement checkboxWebElement = new CheckboxWebElement(element);
			values.add(checkboxWebElement.getCaptionText());
		}
		return values;
	}

	public List<String> getEnabledValues() {
		List<String> values = new ArrayList<>();
		List<ConstellioWebElement> elements = nestedElement.getChildren();
		for (ConstellioWebElement element : elements) {
			CheckboxWebElement checkboxWebElement = new CheckboxWebElement(element);
			if (checkboxWebElement.isChecked()) {
				values.add(checkboxWebElement.getCaptionText());
			}
		}
		return values;
	}
}
