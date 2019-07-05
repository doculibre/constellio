package com.constellio.app.ui.framework.components.fields.lookup;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.converters.TaxonomyCodeToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.TaxonomyRecordIdToContextCaptionConverter;
import com.constellio.app.ui.framework.components.fields.autocomplete.BaseAutocompleteField;
import com.constellio.app.ui.framework.components.fields.autocomplete.BaseAutocompleteField.AutocompleteSuggestionsProvider;
import com.constellio.app.ui.framework.components.fields.autocomplete.RecordAutocompleteField;
import com.constellio.app.ui.framework.components.tree.LazyTree;
import com.constellio.app.ui.framework.components.tree.RecordLazyTree;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.ObjectsResponse;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

public class LookupRecordField extends LookupField<String> {

	private TaxonomyCodeToCaptionConverter captionConverter = new TaxonomyCodeToCaptionConverter();

	public LookupRecordField(String schemaTypeCode) {
		this(schemaTypeCode, null);
	}

	public LookupRecordField(String schemaTypeCode, String schemaCode) {
		this(schemaTypeCode, schemaCode, false);
	}

	public LookupRecordField(String schemaTypeCode, boolean writeAccess,
							 RecordTextInputDataProvider recordTextInputDataProvider) {
		this(schemaTypeCode, null, writeAccess, recordTextInputDataProvider);
	}

	public LookupRecordField(String schemaTypeCode, String schemaCode, boolean writeAccess,
							 RecordTextInputDataProvider recordTextInputDataProvider) {
		this(recordTextInputDataProvider, getTreeDataProvider(schemaTypeCode, schemaCode, writeAccess, true));
	}

	public LookupRecordField(String schemaTypeCode, boolean writeAccess) {
		this(schemaTypeCode, null, writeAccess, true, true, false);
	}

	public LookupRecordField(String schemaTypeCode, boolean writeAccess, boolean isShowAllIfHasAccessToManageSecurity) {
		this(schemaTypeCode, null, writeAccess, true, isShowAllIfHasAccessToManageSecurity, false);
	}

	public LookupRecordField(String schemaTypeCode, String schemaCode, boolean writeAccess) {
		this(schemaTypeCode, schemaCode, writeAccess, true, true, false);
	}


	public LookupRecordField(String schemaTypeCode, String schemaCode, boolean writeAccess, boolean showDeactivated,
							 boolean isShowAllIfHasAccessToManageSecurity, boolean includeLogicallyDeleted) {
		super(new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, schemaCode, writeAccess, showDeactivated, false, includeLogicallyDeleted),
				getTreeDataProvider(schemaTypeCode, schemaCode, writeAccess, isShowAllIfHasAccessToManageSecurity));
		this.isShowDeactivated = showDeactivated;
		setItemConverter(new TaxonomyRecordIdToContextCaptionConverter());
	}

	public LookupRecordField(RecordTextInputDataProvider recordTextInputDataProvider,
							 LookupTreeDataProvider<String>[] lookupTreeDataProviders) {
		super(recordTextInputDataProvider, lookupTreeDataProviders);
		setItemConverter(new TaxonomyRecordIdToContextCaptionConverter());
	}

	public LookupRecordField(RecordTextInputDataProvider recordTextInputDataProvider,
							 LookupTreeDataProvider<String>[] lookupTreeDataProviders,
							 TaxonomyRecordIdToContextCaptionConverter itemConverter) {
		super(recordTextInputDataProvider, lookupTreeDataProviders);
		setItemConverter(itemConverter);
	}

	public static LookupTreeDataProvider<String>[] getTreeDataProvider(String schemaTypeCode, String schemaCode,
			boolean writeAccess,
			boolean isShowAllIfHasAccessToManageSecurity) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String collection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);
		List<Taxonomy> taxonomies = new ArrayList<>();
		if (schemaTypeCode != null) {
			taxonomies = taxonomiesManager
					.getAvailableTaxonomiesForSelectionOfType(schemaTypeCode, currentUser, metadataSchemasManager);
		} else if (schemaCode != null) {
			taxonomies = taxonomiesManager.getAvailableTaxonomiesForSchema(schemaCode, currentUser, metadataSchemasManager);
		}
		List<RecordLookupTreeDataProvider> dataProviders = new ArrayList<>();
		for (Taxonomy taxonomy : taxonomies) {
			String taxonomyCode = taxonomy.getCode();
			if (StringUtils.isNotBlank(taxonomyCode)) {
				dataProviders.add(new RecordLookupTreeDataProvider(schemaTypeCode, taxonomyCode, writeAccess, isShowAllIfHasAccessToManageSecurity));
			}
		}

		return !dataProviders.isEmpty() ? dataProviders.toArray(new RecordLookupTreeDataProvider[0]) : null;
	}


	public static LookupTreeDataProvider<String>[] getTreeDataProviderRootItemsOnly(String schemaTypeCode, String schemaCode,
			boolean writeAccess,
			boolean isShowAllIfHasAccessToManageSecurity, final RecordTextInputDataProviderFactory recordTextInputDataProviderFactory) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String collection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);
		List<Taxonomy> taxonomies = new ArrayList<>();
		if (schemaTypeCode != null) {
			taxonomies = taxonomiesManager
					.getAvailableTaxonomiesForSelectionOfType(schemaTypeCode, currentUser, metadataSchemasManager);
		} else if (schemaCode != null) {
			taxonomies = taxonomiesManager.getAvailableTaxonomiesForSchema(schemaCode, currentUser, metadataSchemasManager);
		}
		List<RecordLookupTreeDataProvider> dataProviders = new ArrayList<>();
		for (Taxonomy taxonomy : taxonomies) {
			String taxonomyCode = taxonomy.getCode();
			if (StringUtils.isNotBlank(taxonomyCode)) {
					dataProviders.add(new RecordLookupTreeDataProvider(schemaTypeCode, taxonomyCode, writeAccess,
							isShowAllIfHasAccessToManageSecurity) {
						@Override
						public ObjectsResponse<String> getChildren(String parent, int start, int maxSize) {
							return new ObjectsResponse<>(new ArrayList<String>(), 0L);
						}

						@Override
						public boolean isSelectable(String selection) {
							return true;
						}

						@Override
						public boolean hasChildren(String parent) {
							return false;
						}

						@Override
						public boolean isLeaf(String parent) {
							return true;
						}

						@Override
						public TextInputDataProvider<String> search() {
							return recordTextInputDataProviderFactory.getProvider();
						}

						@Override
						public TextInputDataProvider<String> searchWithoutDisabled() {
							return recordTextInputDataProviderFactory.getProvider();
						}
					});
			}
		}

		return !dataProviders.isEmpty() ? dataProviders.toArray(new RecordLookupTreeDataProvider[0]) : null;
	}

	@Override
	protected BaseAutocompleteField<String> newAutocompleteField(
			AutocompleteSuggestionsProvider<String> suggestionsProvider) {
		return new RecordAutocompleteField(suggestionsProvider);
	}

	@Override
	protected LazyTree<String> newLazyTree(LookupTreeDataProvider<String> lookupTreeDataProvider, int treeBufferSize) {
		return new RecordLazyTree(lookupTreeDataProvider, treeBufferSize);
	}

	@Override
	protected String getCaptionForLazyTree(LazyTreeDataProvider<String> lazyTreeDataProvider) {
		String taxonomyCode = lazyTreeDataProvider.getTaxonomyCode();
		return captionConverter.convertToPresentation(taxonomyCode, String.class, getLocale());
	}

	@Override
	public Class<String> getType() {
		return String.class;
	}

	SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

}
