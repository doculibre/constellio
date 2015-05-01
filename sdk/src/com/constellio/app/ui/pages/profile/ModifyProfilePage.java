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
package com.constellio.app.ui.pages.profile;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.OptionGroupWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class ModifyProfilePage extends PageHelper {

	public ModifyProfilePage(ConstellioWebDriver driver) {
		super(driver);
	}

	public void navigateToPage() {
		driver.navigateTo().home();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("locale", Locale.FRENCH);
		paramMap.put("globalGroupCode", "heroes");
		String params = ParamUtils.addParams("", paramMap);
		driver.navigateTo()
				.url(NavigatorConfigurationService.MODIFY_PROFILE + "/" + NavigatorConfigurationService.GROUP_ADD_EDIT + "/"
						+ params);
	}

	public void navigateToEditHeroesGroupPage() {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("locale", Locale.FRENCH);
		paramMap.put("globalGroupCode", "heroes");
		String params = ParamUtils.addParams("", paramMap);
		driver.navigateTo().url(NavigatorConfigurationService.GROUP_ADD_EDIT + "/" + params);
	}

	public TextFieldWebElement getFirstNameElement() {
		return getTextFieldWebElementById("firstName");
	}

	public TextFieldWebElement getLastNameElement() {
		return getTextFieldWebElementById("lastName");
	}

	public TextFieldWebElement getEmailElement() {
		return getTextFieldWebElementById("email");
	}

	public TextFieldWebElement getPhoneElement() {
		return getTextFieldWebElementById("phone");
	}

	public TextFieldWebElement getPasswordElement() {
		return getTextFieldWebElementById("password");
	}

	public TextFieldWebElement getConfirmPasswordElement() {
		return getTextFieldWebElementById("confirmPassword");
	}

	public TextFieldWebElement getOldPasswordElement() {
		return getTextFieldWebElementById("oldPassword");
	}

	public OptionGroupWebElement getStartTabElement() {
		return getOptionGroupWebElementById("startTab");
	}

	public OptionGroupWebElement getDefaultTaxonomyElement() {
		return getOptionGroupWebElementById("defaultTaxonomy");
	}

	public ButtonWebElement getCancelButton() {
		return getButtonByClassName(BaseForm.CANCEL_BUTTON, 0);
	}

	public ButtonWebElement getSaveButton() {
		return getButtonByClassName(BaseForm.SAVE_BUTTON, 0);
	}

}
