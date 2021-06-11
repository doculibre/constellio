package com.constellio.app.ui.pages.management.configs;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.SystemConfigurationGroupVO;
import com.constellio.app.ui.entities.SystemConfigurationVO;
import com.constellio.app.ui.framework.data.SystemConfigurationGroupdataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.conf.HashingEncoding;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streams.factories.StreamsServices;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.SchemasRecordsServices;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConfigManagementPresenter extends BasePresenter<ConfigManagementView> {

	public static final String TEMP_FILE_PRIVACY_POLICY_VO = "ConfigManagementPresenter-Temp-file-privacy-policy-vo";
	public static final String TEMP_FILE_PRIVACY_POLICY = "ConfigManagementPresenter-Temp-file-privacy-policy";

	public static final String TEMP_FILE_MESSAGE_TO_USERS_VO = "ConfigManagementPresenter-Temp-file-message-to-users-vo";
	public static final String TEMP_FILE_MESSAGE_TO_USERS = "ConfigManagementPresenter-Temp-file-message-to-users";

	private SystemConfigurationGroupdataProvider dataProvider;
	private SchemasRecordsServices schemasRecordsServices;
	private HashingService hashingService;

	public ConfigManagementPresenter(ConfigManagementView view) {
		super(view);
		this.dataProvider = new SystemConfigurationGroupdataProvider();
		view.setDataProvider(dataProvider);
		schemasRecordsServices = SchemasRecordsServices.usingMainModelLayerFactory(Collection.SYSTEM_COLLECTION, modelLayerFactory);
		hashingService = modelLayerFactory.getIOServicesFactory().newHashingService(HashingEncoding.BASE64);
	}

	public void forParams(String parameters) {

		if (parameters != null && parameters.contains("dev")) {
			dataProvider.showHiddenConfigs();
		}

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
			privacyPolicyUpdateCheck();
			messageToUserUpdateCheck();

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
				ConstellioUI.getCurrent().updateContent();
			} else {
				view.showMessage($("ConfigManagementView.saved"));
			}
			view.navigate().to().adminModule();
		}
	}

	private void privacyPolicyUpdateCheck() {
		SystemConfigurationVO systemConfigurationVO = dataProvider.getSystemConfigurationGroup("others")
				.getSystemConfigurationVO("privacyPolicy");

		boolean reShowPrivacyPolicyToUser = false;

		StreamsServices.ByteArrayStreamFactory oldPrivacyPolicy = appLayerFactory.getModelLayerFactory()
				.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.PRIVACY_POLICY);
		if (systemConfigurationVO != null && systemConfigurationVO.isUpdated()) {
			if (systemConfigurationVO.getValue() != null && oldPrivacyPolicy != null) {
				StreamFactory<InputStream> newPrivacyPolicyStreamFactory = (StreamFactory<InputStream>) systemConfigurationVO
						.getValue();
				InputStream newPrivacyPolicyInputStream;
				try {
					newPrivacyPolicyInputStream = newPrivacyPolicyStreamFactory.create(TEMP_FILE_PRIVACY_POLICY_VO);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				try {
					InputStream oldPrivacyPolicyInputStream = oldPrivacyPolicy.create(TEMP_FILE_PRIVACY_POLICY);
					reShowPrivacyPolicyToUser = !hashingService.getHashFromStream(newPrivacyPolicyInputStream)
							.equals(hashingService.getHashFromStream(oldPrivacyPolicyInputStream));
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (HashingServiceException e) {
					throw new RuntimeException(e);
				}
			} else {
				if (systemConfigurationVO.getValue() != null && oldPrivacyPolicy == null) {
					reShowPrivacyPolicyToUser = true;
				}
			}
			if (reShowPrivacyPolicyToUser) {
				modelLayerFactory.newUserServices().reShowPrivacyPolicyToUser();

			}

		}
	}

	private void messageToUserUpdateCheck() {
		SystemConfigurationVO showMessageToUserAtLoginSystemConfiguration = dataProvider.getSystemConfigurationGroup("others")
				.getSystemConfigurationVO("showMessageToUserAtLogin");

		StreamsServices.ByteArrayStreamFactory oldShowMessageToUserAtLoginSystemConfiguration = appLayerFactory.getModelLayerFactory()
				.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.SHOW_MESSAGE_TO_USER_AT_LOGIN);

		boolean showMessageToUsers = false;

		if (showMessageToUserAtLoginSystemConfiguration != null && showMessageToUserAtLoginSystemConfiguration.isUpdated()) {
			if (showMessageToUserAtLoginSystemConfiguration.getValue() != null && oldShowMessageToUserAtLoginSystemConfiguration != null) {
				StreamFactory<InputStream> messageToUsersStreamFactory = (StreamFactory<InputStream>) showMessageToUserAtLoginSystemConfiguration
						.getValue();
				InputStream messageToUsersInputStream;
				try {
					messageToUsersInputStream = messageToUsersStreamFactory.create(TEMP_FILE_MESSAGE_TO_USERS_VO);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				try {
					InputStream oldMessageToUsersInputStream = oldShowMessageToUserAtLoginSystemConfiguration.create(TEMP_FILE_MESSAGE_TO_USERS);
					showMessageToUsers = !hashingService.getHashFromStream(messageToUsersInputStream)
							.equals(hashingService.getHashFromStream(oldMessageToUsersInputStream));
				} catch (IOException | HashingServiceException e) {
					throw new RuntimeException(e);
				}
			} else {
				if (showMessageToUserAtLoginSystemConfiguration.getValue() != null) {
					showMessageToUsers = true;
				}
			}
			if (showMessageToUsers) {
				modelLayerFactory.newUserServices().showMessageToUsersAtLogin();

			}
		}
	}

	void validateGroup(String groupCode, ValidationErrors errors) {
		SystemConfigurationGroupVO systemConfigurationGroup = dataProvider.getSystemConfigurationGroup(groupCode);
		if (!systemConfigurationGroup.isUpdated()) {
			return;
		}
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		List<SystemConfiguration> previousConfigs = systemConfigurationsManager
				.getNonHiddenGroupConfigurationsWithCodeOrderedByName(groupCode, dataProvider.isShowHidden());
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
		List<SystemConfiguration> previousConfigs = systemConfigurationsManager
				.getNonHiddenGroupConfigurationsWithCodeOrderedByName(groupCode, dataProvider.isShowHidden());
		for (int i = 0; i < previousConfigs.size(); i++) {
			SystemConfiguration systemConfiguration = previousConfigs.get(i);
			SystemConfigurationVO systemConfigurationVO = systemConfigurationGroup.getSystemConfigurationVO(i);
			if (systemConfigurationVO.isUpdated()) {
				reindexingRequired = systemConfigurationsManager.setValue(systemConfiguration, systemConfigurationVO.getValue())
									 || reindexingRequired;
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
