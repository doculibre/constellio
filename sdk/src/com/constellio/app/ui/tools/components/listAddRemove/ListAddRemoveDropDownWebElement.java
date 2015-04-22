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

import com.constellio.app.ui.tools.DropDownWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveDropDownWebElement extends ListAddRemoveFieldWebElement<DropDownWebElement> {

	public ListAddRemoveDropDownWebElement(
			ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected DropDownWebElement wrapInputElement(ConstellioWebElement element) {
		return new DropDownWebElement(element);
	}

	public ListAddRemoveDropDownWebElement add(String choiceCaption) {
		getInputComponent().select(choiceCaption);
		super.clickAdd();
		return this;
	}

	public ListAddRemoveDropDownWebElement modify(int index, String choiceCaption) {
		super.clickModify(index);
		add(choiceCaption);
		return this;
	}

	public ListAddRemoveDropDownWebElement clickAdd() {
		super.clickAdd();
		return this;
	}

	public ListAddRemoveDropDownWebElement clickModify(int index) {
		super.clickModify(index);
		return this;
	}

	public ListAddRemoveDropDownWebElement remove(int index) {
		super.remove(index);
		return this;
	}

}
