package com.constellio.app.ui.pages.management.configs;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.ui.entities.SystemConfigurationGroupVO;
import com.constellio.app.ui.entities.SystemConfigurationVO;
import com.constellio.app.ui.framework.data.SystemConfigurationGroupdataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.configs.SystemConfigurationsManager;

public class ConfigManagementPresenter extends BasePresenter<ConfigManagementView> {
	
	private SystemConfigurationGroupdataProvider dataProvider;

	public ConfigManagementPresenter(ConfigManagementView view) {
		super(view);
		this.dataProvider = new SystemConfigurationGroupdataProvider();
		view.setDataProvider(dataProvider);
	}

	void saveButtonClicked() {
		ValidationErrors errors = new ValidationErrors();
		
		List<String> groupCodes = dataProvider.getCodesList();
		for (String groupCode : groupCodes) {
			validateGroup(groupCode, errors);
		}

		if (errors.getValidationErrors().size() != 0) {
			view.showErrorMessage(buildErrorMessage(errors));
		} else {
			boolean reindexingRequired = false;
			for (String groupCode : groupCodes) {
				boolean reindexingRequiredForGroup = saveGroup(groupCode);
				if (reindexingRequiredForGroup) {
					reindexingRequired = true;
				}
			}
			if (reindexingRequired) {
				view.showMessage($("ConfigManagementView.reindexationNeeded"));
				appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
			} else {
				view.showMessage($("ConfigManagementView.saved"));
			}
			view.navigate().to().adminModule();
		}
		
	}
	
	void validateGroup(String groupCode, ValidationErrors errors) {
		SystemConfigurationGroupVO systemConfigurationGroup = dataProvider.getSystemConfigurationGroup(groupCode);
		if (!systemConfigurationGroup.isUpdated()) {
			return;
		}
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		List<SystemConfiguration> previousConfigs = systemConfigurationsManager.getNonHiddenGroupConfigurationsWithCodeOrderedByName(groupCode);
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
	}

	public boolean saveGroup(String groupCode) {
		boolean reindexingRequired = false;
		SystemConfigurationGroupVO systemConfigurationGroup = dataProvider.getSystemConfigurationGroup(groupCode);
		if (!systemConfigurationGroup.isUpdated()) {
			return false;
		}
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		List<SystemConfiguration> previousConfigs = systemConfigurationsManager.getNonHiddenGroupConfigurationsWithCodeOrderedByName(groupCode);
		for (int i = 0; i < previousConfigs.size(); i++) {
			SystemConfiguration systemConfiguration = previousConfigs.get(i);
			SystemConfigurationVO systemConfigurationVO = systemConfigurationGroup.getSystemConfigurationVO(i);
			if (systemConfigurationVO.isUpdated()) {
				reindexingRequired = reindexingRequired || systemConfigurationsManager.setValue(systemConfiguration, systemConfigurationVO.getValue());
				systemConfigurationVO.afterSetValue();
				systemConfigurationGroup.valueSave(i);
			}
		}
		return reindexingRequired;
	}

	private String buildErrorMessage(ValidationErrors validationErrors) {
		return $(validationErrors);
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
