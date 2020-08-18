package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.RecordFieldsExtensionParams;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.fields.ExtraTabAdditionalRecordField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.profile.ModifyProfileView;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

public class RMUserProfileFieldsExtension extends PagesComponentsExtension {
	String collection;
	AppLayerFactory appLayerFactory;

	public RMUserProfileFieldsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public List<ExtraTabAdditionalRecordField> getExtraTabAdditionalRecordFields(RecordFieldsExtensionParams params) {
		ArrayList<ExtraTabAdditionalRecordField> additionnalFields = new ArrayList<>();
		if (params.getMainComponent() instanceof ModifyProfileView) {
			ExtraTabAdditionalRecordField defaultTabInFolderDisplayField = buildDefaultTabInFolderDisplayField(params);
			ExtraTabAdditionalRecordField defaultAdministrativeUnitField = buildDefaultAdministrativeUnitField(params);
			ExtraTabAdditionalRecordField hideNotActiveField = buildHideNotActiveField(params);
			ExtraTabAdditionalRecordField agentManuallyDisabledField = buildAgentManuallyDisabledField(params);
			ExtraTabAdditionalRecordField favoritesOrderField = buildFavoritesDisplayOrderField(params);

			additionnalFields.addAll(asList(defaultTabInFolderDisplayField, defaultAdministrativeUnitField, hideNotActiveField, agentManuallyDisabledField, favoritesOrderField));
		}
		return additionnalFields;
	}

	private ExtraTabAdditionalRecordField buildDefaultTabInFolderDisplayField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		DefaultTabInFolderAdditionalFieldImpl defaultTabInFolderField = new DefaultTabInFolderAdditionalFieldImpl();
		defaultTabInFolderField.setValue((String) user.get(RMUser.DEFAULT_TAB_IN_FOLDER_DISPLAY));
		return defaultTabInFolderField;
	}

	private ExtraTabAdditionalRecordField buildDefaultAdministrativeUnitField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		DefaultAdministrativeUnitAdditionalFieldImpl defaultAdministrativeUnitField = new DefaultAdministrativeUnitAdditionalFieldImpl();
		defaultAdministrativeUnitField.setCaption($("ModifyProfileView.defaultAdministrativeUnit"));
		defaultAdministrativeUnitField.setValue((String) user.get(RMUser.DEFAULT_ADMINISTRATIVE_UNIT));
		return defaultAdministrativeUnitField;
	}

	private ExtraTabAdditionalRecordField buildHideNotActiveField(RecordFieldsExtensionParams params) {
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

	private ExtraTabAdditionalRecordField buildAgentManuallyDisabledField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		UserCredential userCredentials = appLayerFactory.getModelLayerFactory().newUserServices().getUser(user.getUsername());
		AgentStatus agentStatus = userCredentials.getAgentStatus();

		AgentManuallyDisabledFieldImpl agentManuallyDisabledField = new AgentManuallyDisabledFieldImpl(agentStatus);
		agentManuallyDisabledField.setVisible(isAgentManuallyDisabledVisible(params));
		agentManuallyDisabledField.setImmediate(true);
		agentManuallyDisabledField.setValue(agentStatus == AgentStatus.MANUALLY_DISABLED);

		return agentManuallyDisabledField;
	}

	private ExtraTabAdditionalRecordField buildFavoritesDisplayOrderField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		RecordVODataProvider sharedCartsDataProvider = getFavoritesDataProvider(user, params.getMainComponent().getSessionContext());

		FavoritesDisplayOrderFieldImpl favoritesDisplayOrderField = new FavoritesDisplayOrderFieldImpl(sharedCartsDataProvider.getIterator(), user);
		favoritesDisplayOrderField.setValue((List<String>) user.get(RMUser.FAVORITES_DISPLAY_ORDER));
		return favoritesDisplayOrderField;
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

	private class DefaultTabInFolderAdditionalFieldImpl extends EnumWithSmallCodeOptionGroup<DefaultTabInFolderDisplay> implements ExtraTabAdditionalRecordField<Object> {

		public DefaultTabInFolderAdditionalFieldImpl() {
			super(DefaultTabInFolderDisplay.class);
			setCaption($("ModifyProfileView.defaultTabInFolderDisplay"));
			setId("defaultTabInFolderDisplay");
			setImmediate(true);
		}

		@Override
		public String getTab() {
			return $("ModifyProfileView.configsTab");
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

	private class DefaultAdministrativeUnitAdditionalFieldImpl extends LookupRecordField implements ExtraTabAdditionalRecordField<Object> {

		public DefaultAdministrativeUnitAdditionalFieldImpl() {
			super(AdministrativeUnit.SCHEMA_TYPE, true, false);
			setImmediate(true);
		}

		@Override
		public String getTab() {
			return $("ModifyProfileView.configsTab");
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

	private class HideNotActiveAdditionalFieldImpl extends CheckBox implements ExtraTabAdditionalRecordField<Boolean> {

		public HideNotActiveAdditionalFieldImpl() {
			super($("ModifyProfileView.hideNotActive"));
		}

		@Override
		public String getTab() {
			return $("ModifyProfileView.configsTab");
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

	private class AgentManuallyDisabledFieldImpl extends CheckBox implements ExtraTabAdditionalRecordField<Boolean> {
		AgentStatus previousAgentStatus;

		public AgentManuallyDisabledFieldImpl(AgentStatus previousAgentStatus) {
			super($("ModifyProfileView.agentManuallyDisabled"));
			setId("agentManuallyDisabled");
			addStyleName("agentManuallyDisabled");
			this.previousAgentStatus = previousAgentStatus;
		}

		@Override
		public String getTab() {
			return $("ModifyProfileView.configsTab");
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

	private class FavoritesDisplayOrderFieldImpl extends ListAddRemoveField<String, AbstractField<String>>
			implements ExtraTabAdditionalRecordField<List<String>> {
		private List<Record> favorites;
		private Map<String, String> favoritesTitle;
		private User currentUser;
		private boolean showDefaultFavorite;

		public FavoritesDisplayOrderFieldImpl(SearchResponseIterator<Record> favorites, User currentUser) {
			super();
			setCaption($("ModifyProfileView.favoritesDisplayOrder"));
			addStyleName("favoritesDisplayOrder");
			setId("favoritesDisplayOrder");
			setRequired(false);
			this.currentUser = currentUser;
			this.showDefaultFavorite = currentUser.has(RMPermissionsTo.USE_MY_CART).globally();
			this.favorites = favorites.stream().collect(Collectors.toList());
			this.favoritesTitle = new HashMap<>();
			this.favorites.stream().forEach(record -> favoritesTitle.put(record.getId(), record.getTitle()));

			if (showDefaultFavorite) {
				this.favoritesTitle.put(currentUser.getId(), $("CartView.defaultFavorites"));
			}
		}

		@Override
		public String getTab() {
			return $("ModifyProfileView.configsTab");
		}

		@Override
		public String getMetadataLocalCode() {
			return RMUser.FAVORITES_DISPLAY_ORDER;
		}

		@Override
		public Object getCommittableValue() {
			return getValue();
		}

		@Override
		public Class getType() {
			return List.class;
		}

		@Override
		public List<String> getValue() {
			return super.getValue();
		}

		@Override
		protected AbstractField newAddEditField() {
			ComboBox comboBox = new ComboBox();
			if (showDefaultFavorite) {
				comboBox.addItem(currentUser.getId());
				comboBox.setItemCaption(currentUser.getId(), $("CartView.defaultFavorites"));
			}
			for (Record favorite : favorites) {
				comboBox.addItem(favorite.getId());
				comboBox.setItemCaption(favorite.getId(), favorite.getTitle());
			}
			return comboBox;
		}

		@Override
		protected String getItemCaption(Object itemId) {
			if (favoritesTitle.containsKey(itemId)) {
				return favoritesTitle.get(itemId);
			}
			return super.getItemCaption(itemId);
		}

		@Override
		public void setValue(List<String> favorites) throws ReadOnlyException, ConversionException {
			if (favorites != null) {
				List<String> filteredList = favorites.stream().filter(favoriteId -> favoritesTitle.containsKey(favoriteId)).collect(Collectors.toList());
				super.setValue(filteredList);
			} else {
				super.setValue(favorites);
			}
		}
	}

	private RecordVODataProvider getFavoritesDataProvider(User user, SessionContext sessionContext) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder.build(rm.cartSchema(), VIEW_MODE.TABLE, sessionContext);
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(rm.cartSchema()).whereAnyCondition(
						where(rm.cartSharedWithUsers()).isEqualTo(user),
						where(rm.cart.owner()).isEqualTo(user))
				).sortAsc(Schemas.TITLE);
			}
		};
	}
}
