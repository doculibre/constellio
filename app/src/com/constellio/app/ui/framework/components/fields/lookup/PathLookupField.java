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
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.autocompleteFieldMatching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.TaxonomyCodeToCaptionConverter;
import com.constellio.app.ui.framework.components.tree.LazyTree;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.Resource;

public class PathLookupField extends LookupField<String> {
	private final TaxonomyCodeToCaptionConverter captionConverter;

	public PathLookupField() {
		super(new PathInputDataProvider(
						ConstellioFactories.getInstance().getModelLayerFactory(), ConstellioUI.getCurrentSessionContext()),
				getTreeDataProviders());
		setItemConverter(new RecordIdToCaptionConverter());
		captionConverter = new TaxonomyCodeToCaptionConverter();
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	@Override
	protected String getCaptionForLazyTree(LazyTreeDataProvider<String> lazyTreeDataProvider) {
		String taxonomyCode = lazyTreeDataProvider.getTaxonomyCode();
		return captionConverter.convertToPresentation(taxonomyCode, String.class, getLocale());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected LazyTree<String> newLazyTree(
			LookupTreeDataProvider<String> lookupTreeDataProvider,
			int treeBufferSize) {
		return new LazyTree<String>(lookupTreeDataProvider, treeBufferSize) {
			@Override
			public String getItemCaption(String itemId) {
				return PathLookupField.this.getCaption(itemId);
			}

			@Override
			public Resource getItemIcon(Object itemId) {
				boolean expanded = isExpanded(itemId);
				RecordIdToCaptionConverter itemsConverter = (RecordIdToCaptionConverter) PathLookupField.this.getItemConverter();
				return itemsConverter.getIcon((String) itemId, expanded);
			}
		};
	}

	private static LookupTreeDataProvider<String>[] getTreeDataProviders() {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String collection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		User currentUser = userServices.getUserInCollection(currentUserVO.getUsername(), collection);
		List<Taxonomy> taxonomies = taxonomiesManager.getAvailableTaxonomiesInHomePage(currentUser);
		List<PathLookupTreeDataProvider> dataProviders = new ArrayList<>();
		for (Taxonomy taxonomy : taxonomies) {
			String taxonomyCode = taxonomy.getCode();
			if (StringUtils.isNotBlank(taxonomyCode)) {
				dataProviders.add(new PathLookupTreeDataProvider(taxonomyCode));
			}
		}
		return !dataProviders.isEmpty() ? dataProviders.toArray(new PathLookupTreeDataProvider[dataProviders.size()]) : null;
	}

	public static class PathInputDataProvider implements TextInputDataProvider<String> {
		private transient int lastStartIndex;
		private transient String lastQuery;
		private transient SPEQueryResponse response;
		private transient ModelLayerFactory modelLayerFactory;
		private transient SessionContext sessionContext;

		public PathInputDataProvider(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
			this.modelLayerFactory = modelLayerFactory;
			this.sessionContext = sessionContext;
		}

		@Override
		public List<String> getData(String text, int startIndex, int count) {
			User user = getCurrentUser();
			if (lastQuery == null || !lastQuery.equals(text) || lastStartIndex != startIndex) {
				lastQuery = text;
				lastStartIndex = startIndex;
				response = searchAutocompleteField(user, text, startIndex, count);
			}
			return toRecordIds(response.getRecords());
		}

		@Override
		public int size(String text) {
			User user = getCurrentUser();
			if (lastQuery == null || !lastQuery.equals(text)) {
				lastQuery = text;
				lastStartIndex = -1;
				response = searchAutocompleteField(user, text, 0, 1);
			}
			return (int) response.getNumFound();
		}

		@Override
		public User getCurrentUser() {
			UserServices userServices = modelLayerFactory.newUserServices();
			return userServices.getUserInCollection(
					sessionContext.getCurrentUser().getUsername(), sessionContext.getCurrentCollection());
		}

		@Override
		public void setOnlyLinkables(boolean onlyLinkables) {
			// Ignore
		}

		private SPEQueryResponse searchAutocompleteField(User user, String text, int startIndex, int count) {
			LogicalSearchCondition condition = fromAllSchemasIn(sessionContext.getCurrentCollection())
					.where(autocompleteFieldMatching(text)).andWhere(Schemas.PATH).isNotNull();
			LogicalSearchQuery query = new LogicalSearchQuery(condition)
					.filteredWithUser(user).filteredByStatus(StatusFilter.ACTIVES)
					.setStartRow(startIndex).setNumberOfRows(count);
			return modelLayerFactory.newSearchServices().query(query);
		}

		private List<String> toRecordIds(List<Record> matches) {
			List<String> recordIds = new ArrayList<>();
			for (Record match : matches) {
				recordIds.add(match.getId());
			}
			return recordIds;
		}

		private void readObject(java.io.ObjectInputStream stream)
				throws IOException, ClassNotFoundException {
			stream.defaultReadObject();
			modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
			sessionContext = ConstellioUI.getCurrentSessionContext();
		}
	}

	public static class PathLookupTreeDataProvider extends RecordLazyTreeDataProvider implements LookupTreeDataProvider<String> {

		public PathLookupTreeDataProvider(String taxonomyCode) {
			super(taxonomyCode);
		}

		@Override
		public TextInputDataProvider<String> search() {
			ModelLayerFactory modelLayerFactory = getInstance().getModelLayerFactory();
			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
			return new PathInputDataProvider(modelLayerFactory, sessionContext);
		}

		@Override
		public boolean isSelectable(String selection) {
			return true;
		}
	}

	//	public static class PathLookupTreeDataProvider implements LookupTreeDataProvider<String> {
	//		private transient ModelLayerFactory modelLayerFactory;
	//		private transient SessionContext sessionContext;
	//		private final String taxonomyCode;
	//		private final Map<String, Integer> childrenCounts = new HashMap<>();
	//		private final Map<String, String> parentCache = new HashMap<>();
	//		private int rootObjectsCount = -1;
	//
	//		public PathLookupTreeDataProvider(ModelLayerFactory modelLayerFactory, SessionContext sessionContext,
	//				String taxonomyCode) {
	//			this.modelLayerFactory = modelLayerFactory;
	//			this.sessionContext = sessionContext;
	//			this.taxonomyCode = taxonomyCode;
	//		}
	//
	//		@Override
	//		public TextInputDataProvider<String> search() {
	//			return new PathInputDataProvider(modelLayerFactory, sessionContext);
	//		}
	//
	//		@Override
	//		public boolean isSelectable(String selection) {
	//			return true;
	//		}
	//
	//		@Override
	//		public ObjectsResponse<String> getRootObjects(int start, int maxSize) {
	//			TaxonomiesSearchServices taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
	//			TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions(maxSize, start, StatusFilter.ACTIVES);
	//			LinkableTaxonomySearchResponse response = taxonomiesSearchServices.getVisibleRootConceptResponse(
	//					getCurrentUser(), sessionContext.getCurrentCollection(), taxonomyCode, taxonomiesSearchOptions);
	//
	//			List<String> recordIds = new ArrayList<>();
	//			for (TaxonomySearchRecord match : response.getRecords()) {
	//				String recordId = match.getId();
	//				recordIds.add(recordId);
	//			}
	//			return new ObjectsResponse<String>(recordIds, response.getNumFound());
	//		}
	//
	//		@Override
	//		public String getParent(String child) {
	//			return parentCache.get(child);
	//		}
	//
	//		@Override
	//		public ObjectsResponse<String> getChildren(String parent, int start, int maxSize) {
	//			TaxonomiesSearchServices taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
	//
	//			RecordServices recordServices = modelLayerFactory.newRecordServices();
	//			Record record = recordServices.getDocumentById(parent);
	//
	//			TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions(maxSize, start, StatusFilter.ACTIVES);
	//			LinkableTaxonomySearchResponse response = taxonomiesSearchServices.getVisibleChildConceptResponse(
	//					getCurrentUser(), taxonomyCode, record, taxonomiesSearchOptions);
	//
	//			List<String> recordIds = new ArrayList<>();
	//			for (TaxonomySearchRecord match : response.getRecords()) {
	//				String recordId = match.getId();
	//				recordIds.add(recordId);
	//				parentCache.put(recordId, parent);
	//			}
	//			return new ObjectsResponse<>(recordIds, response.getNumFound());
	//		}
	//
	//		@Override
	//		public boolean hasChildren(String parent) {
	//			return getChildren(parent, 0, 1).getCount() > 0;
	//		}
	//
	//		@Override
	//		public boolean isLeaf(String object) {
	//			return !hasChildren(object);
	//		}
	//
	//		@Override
	//		public String getTaxonomyCode() {
	//			return taxonomyCode;
	//		}
	//
	//		private User getCurrentUser() {
	//			UserServices userServices = modelLayerFactory.newUserServices();
	//			return userServices.getUserInCollection(
	//					sessionContext.getCurrentUser().getUsername(), sessionContext.getCurrentCollection());
	//		}
	//
	//		private void readObject(java.io.ObjectInputStream stream)
	//				throws IOException, ClassNotFoundException {
	//			stream.defaultReadObject();
	//			modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
	//			sessionContext = ConstellioUI.getCurrentSessionContext();
	//		}
	//	}
}
