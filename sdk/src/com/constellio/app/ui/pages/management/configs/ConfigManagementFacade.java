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
package com.constellio.app.ui.pages.management.configs;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ConfigManagementFacade {
	private ConstellioWebDriver driver;
	private Map<String, ConfigGroupFacade> configGroupElementsMap;
	private ConstellioWebElement saveButton;

	public ConfigManagementFacade(ConstellioWebDriver driver) {
		this.driver = driver;
		configGroupElementsMap = new HashMap<>();
		this.saveButton = driver.find(BaseForm.SAVE_BUTTON);
	}

	public int getConfigGroupSize(String groupCode) {
		ConfigGroupFacade configGroupFacade = getConfigGroupFacade(groupCode);
		return configGroupFacade.size();
	}

	private ConfigGroupFacade getConfigGroupFacade(String groupCode) {
		ConfigGroupFacade configGroupFacade = configGroupElementsMap.get(groupCode);
		if(configGroupFacade == null){
			configGroupFacade = new ConfigGroupFacade(driver, groupCode);
			configGroupElementsMap.put(groupCode, configGroupFacade);
		}
		return configGroupFacade;
	}

	public String getConfigGroupInputValue(String groupCode, int i) {
		ConfigGroupFacade configGroupFacade = getConfigGroupFacade(groupCode);
		return configGroupFacade.getElementValue(i);
	}

	public String getConfigGroupComboboxValue(String groupCode, int i) {
		ConfigGroupFacade configGroupFacade = getConfigGroupFacade(groupCode);
		return configGroupFacade.getComboBoxElementValue(i);
	}

	public boolean getConfigGroupChecboxValue(String groupCode, int i) {
		ConfigGroupFacade configGroupFacade = getConfigGroupFacade(groupCode);
		return configGroupFacade.getChecboxElementValue(i);
	}

	public void setConfigGroupComboboxValue(String groupCode, int i, int newValueIndex) {
		ConfigGroupFacade configGroupFacade = getConfigGroupFacade(groupCode);
		configGroupFacade.setComboBoxElementValue(i, newValueIndex);
	}

	public void save() {
		saveButton.click();
	}

	public void setConfigGroupValue(String groupCode, int i, String newValue) {
		ConfigGroupFacade configGroupFacade = getConfigGroupFacade(groupCode);
		configGroupFacade.setElementValue(i, newValue);
	}

	public void toggleConfigGroupCheckBox(String groupCode, int i) {
		ConfigGroupFacade configGroupFacade = getConfigGroupFacade(groupCode);
		configGroupFacade.toggleCheckBox(i);
	}
}
