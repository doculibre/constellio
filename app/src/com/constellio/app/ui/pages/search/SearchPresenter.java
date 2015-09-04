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
package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.builders.search.stats.StatsReportBuilderFactory;
import com.constellio.app.modules.rm.reports.factories.ExampleReportFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.FacetVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.ReportPresenter;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryFacetFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public abstract class SearchPresenter<T extends SearchView> extends BasePresenter<T> implements ReportPresenter {
	public enum SortOrder {ASCENDING, DESCENDING}

	private static Logger LOGGER = LoggerFactory.getLogger(SearchPresenter.class);

	KeySetMap<String, String> facetSelections = new KeySetMap<>();
	List<String> suggestions;
	String sortCriterion;
	SortOrder sortOrder;
	String collection;
	transient SearchPresenterService service;

	public SearchPresenter(T view) {
		super(view);
		init(view.getConstellioFactories(), view.getSessionContext());
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		init(constellioFactories, sessionContext);
	}

	private void init(ConstellioFactories constellioFactories, SessionContext sessionContext) {
		collection = sessionContext.getCurrentCollection();
		service = new SearchPresenterService(collection, constellioFactories.getAppLayerFactory());
	}

	public void resetFacetAndOrder() {
		resetFacetSelection();
		sortOrder = SortOrder.ASCENDING;
	}

	public abstract SearchPresenter<T> forRequestParameters(String params);

	public abstract boolean mustDisplayResults();

	public abstract int getPageNumber();

	public List<FacetVO> getFacets() {
		return service.getFacets(getSearchQuery());
	}

	public String getSortCriterion() {
		return sortCriterion;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public String getUserSearchExpression() {
		return null;
	}

	public boolean mustDisplaySuggestions() {
		if (searchServices().getResultsCount(getSearchQuery()) != 0) {
			return false;
		}
		SPEQueryResponse suggestionsResponse = searchServices()
				.query(getSearchQuery().setNumberOfRows(0).setSpellcheck(true));
		if (suggestionsResponse.isCorrectlySpelt()) {
			return false;
		}
		suggestions = suggestionsResponse.getSpellCheckerSuggestions();
		return !suggestions.isEmpty();
	}

	public List<String> getSuggestions() {
		return suggestions;
	}

	public SearchResultVODataProvider getSearchResults() {
		return new SearchResultVODataProvider(new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				LogicalSearchQuery query = getSearchQuery().setHighlighting(true);
				if (sortCriterion == null) {
					return query;
				}
				Metadata metadata = getMetadata(sortCriterion);
				return sortOrder == SortOrder.ASCENDING ? query.sortAsc(metadata) : query.sortDesc(metadata);
			}
		};
	}

	public void facetValueSelected(String facetId, String facetValue) {
		facetSelections.get(facetId).add(facetValue);
		view.refreshSearchResultsAndFacets();
	}

	public void facetValueDeselected(String facetId, String facetValue) {
		facetSelections.get(facetId).remove(facetValue);
		view.refreshSearchResultsAndFacets();
	}

	public KeySetMap<String, String> getFacetSelections() {
		return facetSelections;
	}

	public void sortCriterionSelected(String sortCriterion, SortOrder sortOrder) {
		this.sortCriterion = sortCriterion;
		this.sortOrder = sortOrder;
		view.refreshSearchResults();
	}

	@Override
	public List<String> getSupportedReports() {
		List<String> supportedReports = new ArrayList<>();
		if (view.computeStatistics()) {
			supportedReports.add("Reports.FolderLinearMeasureStats");
		}
		return supportedReports;
	}

	@Override
	public ReportBuilderFactory getReport(String report) {
		switch (report) {
		case "Reports.fakeReport":
			return new ExampleReportFactory(view.getSelectedRecordIds());
		case "Reports.FolderLinearMeasureStats":
			return new StatsReportBuilderFactory(view.getCollection(), modelLayerFactory, getSearchQuery());
		}
		throw new UnknownReportRuntimeException("BUG: Unknown report " + report);
	}

	public abstract void suggestionSelected(String suggestion);

	public abstract List<MetadataVO> getMetadataAllowedInSort();

	protected abstract LogicalSearchCondition getSearchCondition();

	protected LogicalSearchQuery getSearchQuery() {
		LogicalSearchQuery query = new LogicalSearchQuery(getSearchCondition())
				.setFreeTextQuery(getUserSearchExpression())
				.filteredWithUser(getCurrentUser())
				.filteredByStatus(StatusFilter.ACTIVES);
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		LogicalSearchQueryFacetFilters filters = query.getFacetFilters();
		filters.clear();
		for (Entry<String, Set<String>> selection : facetSelections.getMapEntries()) {
			try {
				Facet facet = schemas.getFacet(selection.getKey());
				if (!selection.getValue().isEmpty()) {
					if (facet.getFacetType() == FacetType.FIELD) {
						filters.selectedFieldFacetValues(facet.getFieldDataStoreCode(), selection.getValue());
					} else if (facet.getFacetType() == FacetType.QUERY) {
						filters.selectedQueryFacetValues(facet.getId(), selection.getValue());
					}
				}
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId id) {
				LOGGER.warn("Facet '" + id + "' has been deleted");
			}
		}
		return query;
	}

	protected void resetFacetSelection() {
		facetSelections.clear();
	}

	protected SavedSearch getSavedSearch(String id) {
		Record record = recordServices().getDocumentById(id, getCurrentUser());
		return new SavedSearch(record, types());
	}

	Metadata getMetadata(String code) {
		SchemaUtils utils = new SchemaUtils();
		String schemaCode = utils.getSchemaCode(code);
		return schema(schemaCode).getMetadata(utils.getLocalCode(code, schemaCode));
	}

	protected List<MetadataVO> getMetadataAllowedInAdvancedSearch(String schemaTypeCode) {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		MetadataSchemaType schemaType = schemaType(schemaTypeCode);

		List<MetadataVO> result = new ArrayList<>();
		for (Metadata metadata : schemaType.getAllMetadatas()) {
			MetadataDisplayConfig config = schemasDisplayManager().getMetadata(view.getCollection(), metadata.getCode());
			if (config.isVisibleInAdvancedSearch()) {
				result.add(builder.build(metadata, view.getSessionContext()));
			}
		}
		return result;
	}

	protected MetadataVO getMetadataVO(String metadataCode) {
		if (metadataCode == null) {
			return null;
		}
		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		MetadataSchemaTypes types = types();
		Metadata metadata = types.getMetadata(metadataCode);
		return builder.build(metadata, view.getSessionContext());
	}

	protected List<MetadataVO> getMetadataAllowedInSort(String schemaTypeCode) {
		MetadataSchemaType schemaType = schemaType(schemaTypeCode);
		return getMetadataAllowedInSort(schemaType);
	}

	protected List<MetadataVO> getMetadataAllowedInSort(MetadataSchemaType schemaType) {
		MetadataToVOBuilder builder = new MetadataToVOBuilder();

		List<MetadataVO> result = new ArrayList<>();
		for (Metadata metadata : schemaType.getAllMetadatas()) {
			if (metadata.isSortable()) {
				result.add(builder.build(metadata, view.getSessionContext()));
			}
		}
		return result;
	}

	protected List<LabelTemplate> getTemplates() {
		return appLayerFactory.getLabelTemplateManager().listTemplates(null);
	}

	protected boolean saveSearch(String title, boolean publicAccess) {
		SavedSearch search = new SavedSearch(recordServices().newRecordWithSchema(schema(SavedSearch.DEFAULT_SCHEMA)), types())
				.setTitle(title)
				.setUser(getCurrentUser().getId())
				.setPublic(publicAccess)
				.setSortField(sortCriterion)
				.setSortOrder(SavedSearch.SortOrder.valueOf(sortOrder.name()))
				.setSelectedFacets(facetSelections.getNestedMap());
		try {
			recordServices().add(prepareSavedSearch(search));
		} catch (RecordServicesException e) {
			view.showErrorMessage($("SearchView.errorSavingSearch"));
			return false;
		}
		view.showMessage($("SearchView.searchSaved"));
		return true;
	}

	protected SavedSearch prepareSavedSearch(SavedSearch search) {
		return search;
	}
}
