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

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class RichTextFieldWebElement {
	
	ConstellioWebElement element;

	public RichTextFieldWebElement(ConstellioWebElement element) {
		this.element = element;
		this.element.scrollIntoView();
	}
	
	public RichTextFieldWebElement setValue(String text) {
		clearField();
		getFrameComponent().sendKeys(text);
		return this;
	}

	public RichTextFieldWebElement clearField() {
		getFrameComponent().sendKeys(Keys.CONTROL + "a");
		getFrameComponent().sendKeys(Keys.DELETE);
		return this;
	}
	
	private ConstellioWebElement getFrameComponent() {
		element.waitUntilElementExist(By.className("gwt-RichTextArea"));
		return element.findElement(By.className("gwt-RichTextArea"));
	}

}
