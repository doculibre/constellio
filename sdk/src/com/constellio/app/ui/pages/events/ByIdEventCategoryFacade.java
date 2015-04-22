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
package com.constellio.app.ui.pages.events;

import com.constellio.app.ui.framework.components.EventByIdSearchPanel;
import com.constellio.app.ui.tools.BaseComboBoxWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class ByIdEventCategoryFacade extends BaseEventCategoryFacade {
	private BaseComboBoxWebElement byIdElement;

	public ByIdEventCategoryFacade(ConstellioWebDriver driver) {
		super(driver);
		byIdElement = new BaseComboBoxWebElement(driver.find(EventByIdSearchPanel.LOOKUP_STYLE_CODE));
	}

	public void selectElement(String byIdFieldValue) {
		byIdElement.typeAndSelectFirst(byIdFieldValue);
		//TODO remove when problem with lookup fixed
		byIdElement.selectFromListValues(1);
	}
}
