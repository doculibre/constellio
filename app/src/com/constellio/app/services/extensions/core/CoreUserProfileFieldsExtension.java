package com.constellio.app.services.extensions.core;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.RecordFieldsExtensionParams;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.app.ui.framework.components.fields.AdditionnalRecordField;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeOptionGroup;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.data.TaxonomyVODataProvider;
import com.constellio.app.ui.pages.home.HomeView;
import com.constellio.app.ui.pages.profile.ModifyProfileView;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.enums.SearchPageLength;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.OptionGroup;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class CoreUserProfileFieldsExtension extends PagesComponentsExtension {
	String collection;
	AppLayerFactory appLayerFactory;

	public CoreUserProfileFieldsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public List<AdditionnalRecordField> getAdditionnalFields(RecordFieldsExtensionParams params) {
		ArrayList<AdditionnalRecordField> additionnalFields = new ArrayList<>();
		if(params.getMainComponent() instanceof ModifyProfileView) {
			AdditionnalRecordField defaultPageLengthField = buildDefaultPageLengthField(params);
			AdditionnalRecordField startTabField = buildStartTabField(params);
			AdditionnalRecordField defaultTaxonomyField = buildDefaultTaxonomyField(params);

			additionnalFields.addAll(asList(defaultPageLengthField, startTabField, defaultTaxonomyField));
		}
		return additionnalFields;
	}

	private AdditionnalRecordField buildDefaultPageLengthField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		DefaultSearchPageLengthFieldImpl defaultPageLength = new DefaultSearchPageLengthFieldImpl();
		defaultPageLength.setValue(user.get(User.DEFAULT_PAGE_LENGTH));
		return defaultPageLength;
	}

	private AdditionnalRecordField buildStartTabField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		ConstellioModulesManagerImpl manager = (ConstellioModulesManagerImpl) appLayerFactory.getModulesManager();
		NavigationConfig navigationConfig = manager.getNavigationConfig(collection);
		List<String> tabs = new ArrayList<>();
		for (PageItem tab : navigationConfig.getFragments(HomeView.TABS)) {
			tabs.add(tab.getCode());
		}

		StartTabFieldImpl defaultPageLength = new StartTabFieldImpl(tabs);
		defaultPageLength.setValue(user.get(User.START_TAB));
		return defaultPageLength;
	}

	private AdditionnalRecordField buildDefaultTaxonomyField(RecordFieldsExtensionParams params) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());

		TaxonomyVODataProvider dataProvider = new TaxonomyVODataProvider(new TaxonomyToVOBuilder(), appLayerFactory.getModelLayerFactory(),
				collection, user.getUsername());

		DefaultTaxonomyFieldImpl defaultTaxonomyField = new DefaultTaxonomyFieldImpl(dataProvider.getTaxonomyVOs());
		defaultTaxonomyField.setValue(user.get(User.DEFAULT_TAXONOMY));
		return defaultTaxonomyField;
	}

	private class DefaultSearchPageLengthFieldImpl extends EnumWithSmallCodeComboBox<SearchPageLength> implements AdditionnalRecordField<Object> {

		public DefaultSearchPageLengthFieldImpl() {
			super(SearchPageLength.class);
			setCaption($("ModifyProfileView.searchPageLength"));
			setId("defaultPageLength");
			setNullSelectionAllowed(false);
			setImmediate(true);
		}

		@Override
        public String getMetadataLocalCode() {
            return User.DEFAULT_PAGE_LENGTH;
        }

		@Override
		public SearchPageLength getCommittableValue() {
			return (SearchPageLength) getValue();
		}

		@Override
		public Class<? extends SearchPageLength> getType() {
			return SearchPageLength.class;
		}

		@Override
		protected int compare(EnumWithSmallCode o1, EnumWithSmallCode o2) {
			return Integer.compare(Integer.parseInt(o1.getCode()), Integer.parseInt(o2.getCode()));
		}
	}

	private class StartTabFieldImpl extends OptionGroup implements AdditionnalRecordField<Object> {
		public StartTabFieldImpl(List<String> tabs) {
			super($("ModifyProfileView.startTab"));
			setId("startTab");
			for (String tab : tabs) {
				addItem(tab);
				setItemCaption(tab, $("HomeView.tab." + tab));
			}
		}

		@Override
		public String getMetadataLocalCode() {
			return User.START_TAB;
		}

		@Override
		public Object getCommittableValue() {
			return getValue();
		}

		@Override
		public Class<? extends String> getType() {
			return String.class;
		}

		@Override
		public String getValue() {
			return (String) super.getValue();
		}
	}

	private class DefaultTaxonomyFieldImpl extends ListOptionGroup implements AdditionnalRecordField<Object> {
		public DefaultTaxonomyFieldImpl(List<TaxonomyVO> taxonomies) {
			super($("ModifyProfileView.defaultTaxonomy"));
			addStyleName("defaultTaxonomy");
			setId("defaultTaxonomy");
			setMultiSelect(false);
			setRequired(false);
			for (TaxonomyVO taxonomy : taxonomies) {
				addItem(taxonomy.getCode());
				setItemCaption(taxonomy.getCode(), taxonomy.getTitle());
			}
		}

		@Override
		public String getMetadataLocalCode() {
			return User.DEFAULT_TAXONOMY;
		}

		@Override
		public Object getCommittableValue() {
			return getValue();
		}

		@Override
		public Class<? extends String> getType() {
			return String.class;
		}

		@Override
		public String getValue() {
			return (String) super.getValue();
		}
	}
}
