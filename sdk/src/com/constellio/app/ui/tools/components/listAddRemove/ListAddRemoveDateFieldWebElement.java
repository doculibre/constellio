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

import org.joda.time.LocalDate;

import com.constellio.app.ui.tools.components.basic.DateFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveDateFieldWebElement extends ListAddRemoveFieldWebElement<DateFieldWebElement> {

	public ListAddRemoveDateFieldWebElement(
			ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected DateFieldWebElement wrapInputElement(ConstellioWebElement element) {
		return new DateFieldWebElement(element);
	}
	
	public ListAddRemoveDateFieldWebElement add(LocalDate value) {
		getInputComponent().setValue(value);;
		super.clickAdd();
		return this;
	}

	public ListAddRemoveDateFieldWebElement modify(int index, LocalDate newValue) {
		super.clickModify(index);
		add(newValue);
		return this;
	}

	public ListAddRemoveDateFieldWebElement remove(int index) {
		super.remove(index);
		return this;
	}	
}
