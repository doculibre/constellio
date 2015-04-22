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

import com.constellio.app.ui.tools.LookupWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveLookupWebElement extends ListAddRemoveFieldWebElement<LookupWebElement> {

	public ListAddRemoveLookupWebElement(
			ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected LookupWebElement wrapInputElement(ConstellioWebElement element) {
		return new LookupWebElement(element);
	}

	public ListAddRemoveLookupWebElement addElementByChoosingFirstChoice(String text) {
		getInputComponent().typeAndSelectFirst(text);
		super.clickAdd();
		return this;
	}

	public ListAddRemoveLookupWebElement modifyElementByChoosingFirstChoice(int index, String text) {
		super.clickModify(index);
		getInputComponent().clear();
		addElementByChoosingFirstChoice(text);
		return this;
	}

	public ListAddRemoveLookupWebElement clickAdd() {
		super.clickAdd();
		return this;
	}

	public ListAddRemoveLookupWebElement clickModify(int index) {
		super.clickModify(index);
		return this;
	}

	public ListAddRemoveLookupWebElement remove(int index) {
		super.remove(index);
		return this;
	}

}
