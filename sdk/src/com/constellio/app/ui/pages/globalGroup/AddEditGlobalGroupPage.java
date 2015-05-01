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
package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.ui.tools.OptionGroupWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class AddEditGlobalGroupPage extends PageHelper {

	public AddEditGlobalGroupPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public TextFieldWebElement getCodeElement() {
		return getTextFieldWebElementById("code");
	}

	public TextFieldWebElement getNameElement() {
		return getTextFieldWebElementById("name");
	}

	public OptionGroupWebElement getCollectionsElement() {
		return getOptionGroupWebElementById("collections");
	}

	public OptionGroupWebElement getStatusElement() {
		return getOptionGroupWebElementById("status");
	}
}
