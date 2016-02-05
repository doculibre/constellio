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
