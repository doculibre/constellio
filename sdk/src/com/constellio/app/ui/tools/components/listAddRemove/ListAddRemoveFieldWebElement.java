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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebTableElement;
import com.constellio.sdk.tests.selenium.conditions.ConditionWithTimeout;

public abstract class ListAddRemoveFieldWebElement<T> {

	ConstellioWebElement nestedElement;

	public ListAddRemoveFieldWebElement(ConstellioWebElement nestedElement) {
		this.nestedElement = nestedElement;
	}

	public T getInputComponent() {
		return wrapInputElement(getInputElement());
	}
	
	public T getInputDateComponent() {
		return wrapInputElement(getInputDateElement());
	}

	protected abstract T wrapInputElement(ConstellioWebElement element);

	public ListAddRemoveFieldWebElement clickAdd() {
		final int valuesCount = getValues().size();

		getAddButtonWebElement().sendKeys("");
		getAddButtonWebElement().click();

		new ConditionWithTimeout() {
			@Override
			protected boolean evaluate() {
				return getModifyButtonWebElement(valuesCount) != null;
			}
		}.waitForTrue(1000);

		return this;
	}

	public List<String> getValues() {

		List<String> values = new ArrayList<>();

		ConstellioWebElement tableDiv = nestedElement.findElement(By.className(ListAddRemoveField.TABLE_STYLE_NAME));

		ConstellioWebTableElement table = tableDiv.find("v-table-body").findTableElement(By.tagName("table"));
		for (ConstellioWebElement element : table.getRows()) {

			ConstellioWebElement firstColumn = element.findElement(By.tagName("td"));
			ConstellioWebElement firstColumnLabel = firstColumn.findElement(By.className("v-label"));
			values.add(firstColumnLabel.getText());
		}

		return values;
	}

	public ListAddRemoveFieldWebElement remove(int index) {
		int valuesCount = getValues().size();
		ConstellioWebElement lastElement = getModifyButtonWebElement(valuesCount - 1);
		ConstellioWebElement deleteElement = getRemoveButtonWebElement(index);
		deleteElement.click();
		lastElement.waitForRemoval();

		return this;
	}

	public ListAddRemoveFieldWebElement clickModify(int index) {
		int valuesCount = getValues().size();
		ConstellioWebElement lastElement = getModifyButtonWebElement(valuesCount - 1);
		getModifyButtonWebElement(index).click();
		lastElement.waitForRemoval();

		return this;
	}

	protected ConstellioWebElement getInputElement() {
		return nestedElement.findElement(By.className(ListAddRemoveField.ADD_EDIT_FIELD_STYLE_NAME));
	}
	
	protected ConstellioWebElement getInputDateElement() {
		return nestedElement.findElement(By.className("v-datefield-textfield"));
	}

	public ConstellioWebElement getAddButtonWebElement() {
		return nestedElement.findElement(By.className(ListAddRemoveField.ADD_BUTTON_STYLE_NAME));
	}

	public ConstellioWebElement getModifyButtonWebElement(int i) {
		return nestedElement.findAdaptElements(By.className(ListAddRemoveField.EDIT_BUTTON_STYLE_NAME)).get(i);
	}

	public ConstellioWebElement getRemoveButtonWebElement(int i) {
		return nestedElement.findAdaptElements(By.className(ListAddRemoveField.REMOVE_BUTTON_STYLE_NAME)).get(i);
	}

}
