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
package com.constellio.app.ui.tools.components.listAddRemove;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveRichTextFieldWebElement  extends ListAddRemoveFieldWebElement<TextFieldWebElement> {

	public ListAddRemoveRichTextFieldWebElement(ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected TextFieldWebElement wrapInputElement(ConstellioWebElement element) {
		return new TextFieldWebElement(element);
	}

	public ListAddRemoveRichTextFieldWebElement add(String value) {
		getFrameComponent().sendKeys(value);
		getAddButtonWebElement().sendKeys("");
		getAddButtonWebElement().click();
		return this;
	}
	
	public ListAddRemoveRichTextFieldWebElement remove(int index) {
		getRemoveButtonWebElement(index).click();
		return this;
	}

	public ListAddRemoveRichTextFieldWebElement modifyTo(int index, String value) {
		clickModify(index);
		clearRichText();
		getFrameComponent().sendKeys(value);
		clickAdd();
		return this;
	}

	private void clearRichText() {
		getFrameComponent().sendKeys(Keys.CONTROL + "a");
		getFrameComponent().sendKeys(Keys.DELETE);
	}
	
	private ConstellioWebElement getFrameComponent() {
		return nestedElement.findElement(By.className("gwt-RichTextArea"));
	}
	

	
	

	
}
