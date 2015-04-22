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
import org.joda.time.LocalDateTime;

import com.constellio.app.ui.tools.components.basic.DateFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListAddRemoveDateTimeFieldWebElement extends ListAddRemoveFieldWebElement<DateFieldWebElement> {

	public ListAddRemoveDateTimeFieldWebElement(ConstellioWebElement nestedElement) {
		super(nestedElement);
	}

	@Override
	protected DateFieldWebElement wrapInputElement(ConstellioWebElement element) {
		return new DateFieldWebElement(element, "yyyy-MM-dd hh:mm:ss");
	}
	
	public ListAddRemoveDateTimeFieldWebElement add(LocalDateTime value) {
		getInputComponent().setValueWithTime(value);;
		super.clickAdd();
		return this;
	}

	public ListAddRemoveDateTimeFieldWebElement modify(int index, LocalDateTime newValue) {
		super.clickModify(index);
		add(newValue);
		return this;
	}

	public ListAddRemoveDateTimeFieldWebElement remove(int index) {
		super.remove(index);
		return this;
	}	
}

