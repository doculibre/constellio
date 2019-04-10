package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.RecordFieldsExtensionParams;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.fields.AdditionnalRecordField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.profile.ModifyProfileView;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import com.vaadin.ui.CheckBox;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class RMUserProfileFieldsExtension extends PagesComponentsExtension {
	String collection;
	AppLayerFactory appLayerFactory;

	public RMUserProfileFieldsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public List<AdditionnalRecordField> getAdditionnalFields(RecordFieldsExtensionParams params) {
		ArrayList<AdditionnalRecordField> additionnalFields = new ArrayList<>();
		if (params.getMainComponent() instanceof ModifyProfileView) {
			AdditionnalRecordField defaultTabInFolderDisplayField = buildDefaultTabInFolderDisplayField(params);
			AdditionnalRecordField defaultAdministrativeUnitField = buildDefaultAdministrativeUnitField(params);
			AdditionnalRecordField hideNotActiveField = buildHideNotActiveField(params);
			AdditionnalRecordField agentManuallyDisabledField = buildAgentManuallyDisabledField(params);

			additionnalFields.addAll(asList(defaultTabInFolderDisplayField, defaultAdministrativeUnitField, hideNotActiveField, agentManuallyDisabledField));
		}
		return additionnalFields;
	}

	private AdditionnalRecordField buildDefaultTabInFolderDisplayField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		DefaultTabInFolderAdditionalFieldImpl defaultTabInFolderField = new DefaultTabInFolderAdditionalFieldImpl();
		defaultTabInFolderField.setValue((String) user.get(RMUser.DEFAULT_TAB_IN_FOLDER_DISPLAY));
		return defaultTabInFolderField;
	}

	private AdditionnalRecordField buildDefaultAdministrativeUnitField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		DefaultAdministrativeUnitAdditionalFieldImpl defaultAdministrativeUnitField = new DefaultAdministrativeUnitAdditionalFieldImpl();
		defaultAdministrativeUnitField.setCaption($("ModifyProfileView.defaultAdministrativeUnit"));
		defaultAdministrativeUnitField.setValue((String) user.get(RMUser.DEFAULT_ADMINISTRATIVE_UNIT));
		return defaultAdministrativeUnitField;
	}

	private AdditionnalRecordField buildHideNotActiveField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		Boolean isHidingNotActive = user.get(RMUser.HIDE_NOT_ACTIVE);
		HideNotActiveAdditionalFieldImpl hideNotActiveField = new HideNotActiveAdditionalFieldImpl();
		hideNotActiveField.setImmediate(true);

		if (Boolean.TRUE.equals(isHidingNotActive)) {
			hideNotActiveField.setValue(true);
		} else {
			hideNotActiveField.setValue(false);
		}

		return hideNotActiveField;
	}

	private AdditionnalRecordField buildAgentManuallyDisabledField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		UserCredential userCredentials = appLayerFactory.getModelLayerFactory().newUserServices().getUser(user.getUsername());
		AgentStatus agentStatus = userCredentials.getAgentStatus();

		AgentManuallyDisabledFieldImpl agentManuallyDisabledField = new AgentManuallyDisabledFieldImpl(agentStatus);
		agentManuallyDisabledField.setVisible(isAgentManuallyDisabledVisible(params));
		agentManuallyDisabledField.setImmediate(true);
		agentManuallyDisabledField.setValue(agentStatus == AgentStatus.MANUALLY_DISABLED);

		return agentManuallyDisabledField;
	}

	private boolean isAgentManuallyDisabledVisible(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();

		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
		UserCredential userCredentials = userServices.getUser(user.getUsername());
		AgentStatus agentStatus = userCredentials.getAgentStatus();
		if (agentStatus == AgentStatus.DISABLED && !rmConfigs.isAgentDisabledUntilFirstConnection()) {
			agentStatus = AgentStatus.ENABLED;
		}

		return rmConfigs.isAgentEnabled() && ConstellioAgentUtils.isAgentSupported() && agentStatus != AgentStatus.DISABLED;
	}

	private class DefaultTabInFolderAdditionalFieldImpl extends EnumWithSmallCodeOptionGroup<DefaultTabInFolderDisplay> implements AdditionnalRecordField<Object> {

		public DefaultTabInFolderAdditionalFieldImpl() {
			super(DefaultTabInFolderDisplay.class);
			setCaption($("ModifyProfileView.defaultTabInFolderDisplay"));
			setId("defaultTabInFolderDisplay");
			setImmediate(true);
		}

		@Override
		public String getMetadataLocalCode() {
			return RMUser.DEFAULT_TAB_IN_FOLDER_DISPLAY;
		}

		@Override
		public String getCommittableValue() {
			DefaultTabInFolderDisplay value = getValue();
			return value == null ? DefaultTabInFolderDisplay.METADATA.getCode() : value.getCode();
		}

		@Override
		public Class<? extends DefaultTabInFolderDisplay> getType() {
			return DefaultTabInFolderDisplay.class;
		}

		@Override
		public DefaultTabInFolderDisplay getValue() {
			return (DefaultTabInFolderDisplay) super.getValue();
		}

		public void setValue(String enumCode) {
			super.setValue(EnumWithSmallCodeUtils.toEnumWithSmallCode(getType(), enumCode));
		}
	}

	private class DefaultAdministrativeUnitAdditionalFieldImpl extends LookupRecordField implements AdditionnalRecordField<Object> {

		public DefaultAdministrativeUnitAdditionalFieldImpl() {
			super(AdministrativeUnit.SCHEMA_TYPE, true, false);
			setImmediate(true);
		}

		@Override
		public String getMetadataLocalCode() {
			return RMUser.DEFAULT_ADMINISTRATIVE_UNIT;
		}

		@Override
		public Object getCommittableValue() {
			return getValue();
		}
	}

	private class HideNotActiveAdditionalFieldImpl extends CheckBox implements AdditionnalRecordField<Boolean> {

		public HideNotActiveAdditionalFieldImpl() {
			super($("ModifyProfileView.hideNotActive"));
		}

		@Override
		public String getMetadataLocalCode() {
			return RMUser.HIDE_NOT_ACTIVE;
		}

		@Override
		public Boolean getCommittableValue() {
			return getValue();
		}
	}

	private class AgentManuallyDisabledFieldImpl extends CheckBox implements AdditionnalRecordField<Boolean> {
		AgentStatus previousAgentStatus;

		public AgentManuallyDisabledFieldImpl(AgentStatus previousAgentStatus) {
			super($("ModifyProfileView.agentManuallyDisabled"));
			setId("agentManuallyDisabled");
			addStyleName("agentManuallyDisabled");
			this.previousAgentStatus = previousAgentStatus;
		}

		@Override
		public String getMetadataLocalCode() {
			return UserCredential.AGENT_STATUS;
		}

		@Override
		public AgentStatus getCommittableValue() {
			boolean agentManuallyDisabled = getValue();
			if (previousAgentStatus == AgentStatus.MANUALLY_DISABLED && !agentManuallyDisabled) {
				return AgentStatus.ENABLED;
			} else if (previousAgentStatus != AgentStatus.MANUALLY_DISABLED && agentManuallyDisabled) {
				return AgentStatus.MANUALLY_DISABLED;
			} else {
				return previousAgentStatus;
			}
		}
	}
}
