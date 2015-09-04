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

	public LookupRecordField(String schemaTypeCode, boolean writeAccess) {
		super(new RecordTextInputDataProvider(getInstance(), getCurrentSessionContext(), schemaTypeCode, writeAccess),
				getTreeDataProvider(schemaTypeCode, writeAccess));
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
