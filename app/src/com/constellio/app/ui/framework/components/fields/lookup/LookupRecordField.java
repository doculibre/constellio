package com.constellio.app.ui.framework.components.fields.lookup;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.TaxonomyCodeToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.autocomplete.BaseAutocompleteField;
import com.constellio.app.ui.framework.components.fields.autocomplete.BaseAutocompleteField.AutocompleteSuggestionsProvider;
import com.constellio.app.ui.framework.components.fields.autocomplete.RecordAutocompleteField;
import com.constellio.app.ui.framework.components.tree.LazyTree;
import com.constellio.app.ui.framework.components.tree.RecordLazyTree;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordLookupTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;

public class LookupRecordField extends LookupField<String> {
	private TaxonomyCodeToCaptionConverter captionConverter = new TaxonomyCodeToCaptionConverter();

	public LookupRecordField(String schemaTypeCode) {
		this(schemaTypeCode, false);
	}

	public LookupRecordField(String schemaTypeCode, boolean writeAccess,
			RecordTextInputDataProvider recordTextInputDataProvider) {
		this(recordTextInputDataProvider, getTreeDataProvider(schemaTypeCode, writeAccess));
		setItemConverter(new RecordIdToCaptionConverter());
	}

	public LookupRecordField(String schemaTypeCode, boolean writeAccess) {
		super(new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, writeAccess),
				getTreeDataProvider(schemaTypeCode, writeAccess));
		setItemConverter(new RecordIdToCaptionConverter());
	}

	public LookupRecordField(RecordTextInputDataProvider recordTextInputDataProvider,
			LookupTreeDataProvider<String>[] lookupTreeDataProvider) {
		super(recordTextInputDataProvider, lookupTreeDataProvider);
		setItemConverter(new RecordIdToCaptionConverter());
	}

	private static LookupTreeDataProvider<String>[] getTreeDataProvider(String schemaTypeCode, boolean writeAccess) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String collection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);
		List<Taxonomy> taxonomies = taxonomiesManager
				.getAvailableTaxonomiesForSelectionOfType(schemaTypeCode, currentUser, metadataSchemasManager);
		List<RecordLookupTreeDataProvider> dataProviders = new ArrayList<>();
		for (Taxonomy taxonomy : taxonomies) {
			String taxonomyCode = taxonomy.getCode();
			if (StringUtils.isNotBlank(taxonomyCode)) {
				dataProviders.add(new RecordLookupTreeDataProvider(schemaTypeCode, taxonomyCode, writeAccess));
			}
		}
		return !dataProviders.isEmpty() ? dataProviders.toArray(new RecordLookupTreeDataProvider[0]) : null;
	}

	@Override
	protected BaseAutocompleteField<String> newAutocompleteField(AutocompleteSuggestionsProvider<String> suggestionsProvider) {
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
