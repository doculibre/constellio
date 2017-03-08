package com.constellio.app.ui.pages.management.configs;

import com.constellio.app.ui.entities.SystemConfigurationGroupVO;
import com.constellio.app.ui.entities.SystemConfigurationVO;
import com.constellio.app.ui.framework.data.SystemConfigurationGroupdataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.configs.SystemConfigurationsManager;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConfigManagementPresenter extends BasePresenter<ConfigManagementView> {

	public ConfigManagementPresenter(ConfigManagementView view) {
		super(view);
	}

	public void saveButtonClicked(String groupCode, SystemConfigurationGroupdataProvider dataProvider) {
		boolean isReindexationNeeded = false;
		SystemConfigurationGroupVO systemConfigurationGroup = dataProvider.getSystemConfigurationGroup(groupCode);
		if (!systemConfigurationGroup.isUpdated()) {
			return;
		}
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		List<SystemConfiguration> previousConfigs = systemConfigurationsManager.getNonHiddenGroupConfigurationsWithCodeOrderedByName(groupCode);
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
					isReindexationNeeded = isReindexationNeeded || systemConfigurationsManager.setValue(systemConfiguration, systemConfigurationVO.getValue());
					systemConfigurationVO.afterSetValue();
					systemConfigurationGroup.valueSave(i);
				}
			}
			if(isReindexationNeeded) {
				view.showMessage($("ConfigManagementView.reindexationNeeded"));
				appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
			} else {
				view.showMessage($("ConfigManagementView.saved"));
			}
			view.navigate().to().adminModule();
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
		view.navigate().to().adminModule();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices().has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_CONFIGURATION);
	}
}
