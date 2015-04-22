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

import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveTextFieldWebElement extends ListAddRemoveFieldWebElement<TextFieldWebElement> {

	public ListAddRemoveTextFieldWebElement(
			ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected TextFieldWebElement wrapInputElement(ConstellioWebElement element) {
		return new TextFieldWebElement(element);
	}

	public ListAddRemoveTextFieldWebElement add(String value) {
		getInputComponent().setValue(value);
		clickAdd();
		return this;
	}
	
	public ListAddRemoveTextFieldWebElement addDate(String date) {
		getInputDateComponent().setValue(date);
		clickAdd();
		return this;
	}

	public ListAddRemoveTextFieldWebElement modifyTo(int index, String value) {
		clickModify(index);
		getInputComponent().setValue(value);
		clickAdd();
		return this;
	}

	public ListAddRemoveTextFieldWebElement setValue(String value) {
		getInputComponent().setValue(value);
		return this;

	}
	
	
}
