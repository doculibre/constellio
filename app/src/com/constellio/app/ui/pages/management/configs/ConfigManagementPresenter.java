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

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.ui.entities.SystemConfigurationGroupVO;
import com.constellio.app.ui.entities.SystemConfigurationVO;
import com.constellio.app.ui.framework.data.SystemConfigurationGroupdataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.configs.SystemConfigurationsManager;

public class ConfigManagementPresenter extends BasePresenter<ConfigManagementView> {

	public ConfigManagementPresenter(ConfigManagementView view) {
		super(view);
	}

	public void saveButtonClicked(String groupCode, SystemConfigurationGroupdataProvider dataProvider) {
		SystemConfigurationGroupVO systemConfigurationGroup = dataProvider.getSystemConfigurationGroup(groupCode);
		if (!systemConfigurationGroup.isUpdated()) {
			return;
		}
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		List<SystemConfiguration> previousConfigs = systemConfigurationsManager.getGroupConfigurationsWithCode(groupCode);
		ValidationErrors errors = new ValidationErrors();
		for (int i = 0; i < previousConfigs.size(); i++) {
			SystemConfigurationVO systemConfigurationVO = systemConfigurationGroup.getSystemConfigurationVO(i);
			if (systemConfigurationVO.isUpdated()) {
				SystemConfiguration systemConfiguration = previousConfigs.get(i);
				systemConfigurationsManager.validate(systemConfiguration, systemConfigurationVO.getValue(), errors);
				if (errors.getValidationErrors().size() != 0) {
					break;
				}
			}
		}
		if (errors.getValidationErrors().size() != 0) {
			view.showErrorMessage(buildErrorMessage(errors.getValidationErrors()));
		} else {
			for (int i = 0; i < previousConfigs.size(); i++) {
				SystemConfiguration systemConfiguration = previousConfigs.get(i);
				SystemConfigurationVO systemConfigurationVO = systemConfigurationGroup.getSystemConfigurationVO(i);
				if (systemConfigurationVO.isUpdated()) {
					systemConfigurationsManager.setValue(systemConfiguration, systemConfigurationVO.getValue());
					systemConfigurationGroup.valueSave(i);
				}
			}
			view.showMessage($("ConfigManagementView.saved"));
			view.navigateTo().adminModule();
		}
	}

	private String buildErrorMessage(List<ValidationError> validationErrors) {
		StringBuilder stringBuilder = new StringBuilder();
		for (ValidationError validationError : validationErrors) {
			stringBuilder.append(validationError.toString());
		}
		return stringBuilder.toString();
	}

	public SystemConfigurationGroupdataProvider systemConfigurationGroupDataProvider() {
		return new SystemConfigurationGroupdataProvider();
	}

	public String getLabel(String groupCode, String code) {
		return $("SystemConfigurationGroup." + groupCode + "." + code);
	}

	public String getGroupLabel(String groupCode) {
		return $("SystemConfigurationGroup." + groupCode);
	}

	public void backButtonClick() {
		view.navigateTo().adminModule();
	}
}
