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

import java.util.*;
import java.util.Map.Entry;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.builders.search.stats.StatsReportBuilderFactory;
import com.constellio.app.modules.rm.reports.factories.ExampleReportFactory;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderFactory;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.ReportPresenter;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public abstract class SearchPresenter<T extends SearchView> extends BasePresenter<T> implements ReportPresenter {
	public static final int FACET_LIMIT = 5;

	public enum SortOrder {ASCENDING, DESCENDING}

	Map<String, Set<String>> facetSelections;
	List<String> suggestions;
	String sortCriterion;
	SortOrder sortOrder;
	private List<LabelTemplate> templates;

	public SearchPresenter(T view) {
		super(view);
		resetFacetSelection();
		sortOrder = SortOrder.ASCENDING;
	}

	public abstract SearchPresenter<T> forRequestParameters(String params);

	public abstract boolean mustDisplayResults();

	public abstract int getPageNumber();

	public String getUserSearchExpression() {
		return null;
	}

	public boolean mustDisplaySuggestions() {
		if (getSearchResults().size() != 0) {
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
		return new SearchResultVODataProvider(new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
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

	public Map<MetadataVO, List<FacetValue>> getFacets() {
		LogicalSearchQuery query = getSearchQuery().setNumberOfRows(0).setFieldFacetLimit(FACET_LIMIT);
		injectFacetFields(query);
		SPEQueryResponse response = searchServices().query(query);

		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		Map<MetadataVO, List<FacetValue>> result = new LinkedHashMap<>();
		for (String code : getActiveFacets()) {
			for (Entry<DataStoreField, List<FacetValue>> each : response.getFieldFacetValues().entrySet()) {
				Metadata metadata = (Metadata) each.getKey();
				if (metadata.getCode().equals(code)) {
					result.put(builder.build(metadata, view.getSessionContext()), each.getValue());
					break;
				}
			}
		}
		return result;
	}

	public void facetValueSelected(String code, String value) {
		facetSelections.get(code).add(value);
		view.refreshSearchResultsAndFacets();
	}

	public void facetValueDeselected(String code, String value) {
		facetSelections.get(code).remove(value);
		view.refreshSearchResultsAndFacets();
	}

	public Map<String, Set<String>> getFacetSelections() {
		return facetSelections;
	}

	public void sortCriterionSelected(String sortCriterion, SortOrder sortOrder) {
		this.sortCriterion = sortCriterion;
		this.sortOrder = sortOrder;
		view.refreshSearchResults();
	}

	@Override
	public List<String> getSupportedReports() {
		if(view.computeStatistics()){
			return Arrays.asList("Reports.FolderLinearMeasureStats");
		}else{
			return new ArrayList<>();
		}
	}

	@Override
	public ReportBuilderFactory getReport(String report) {
		switch (report) {
		case "Reports.fakeReport":
			return new ExampleReportFactory(view.getSelectedRecordIds());
		case "Reports.FolderLinearMeasureStats":
			return new StatsReportBuilderFactory(view.getCollection(), modelLayerFactory, getSearchQuery());
		}
		throw new RuntimeException("BUG: Unknown report " + report);
	}

	public abstract void suggestionSelected(String suggestion);

	public abstract List<MetadataVO> getMetadataAllowedInSort();

	protected abstract LogicalSearchCondition getSearchCondition();

	private LogicalSearchQuery getSearchQuery() {
		LogicalSearchQuery query = new LogicalSearchQuery(getSearchCondition())
				.setFreeTextQuery(getUserSearchExpression())
				.filteredWithUser(getCurrentUser())
				.filteredByStatus(StatusFilter.ACTIVES);
		for (Entry<String, Set<String>> selection : facetSelections.entrySet()) {
			if (!selection.getValue().isEmpty()) {
				query.filteredByFacetValues(getMetadata(selection.getKey()), selection.getValue());
			}
		}
		return query;
	}

	void injectFacetFields(LogicalSearchQuery query) {
		for (String facetCode : getActiveFacets()) {
			query.addFieldFacet(getMetadata(facetCode));
		}
	}

	protected void resetFacetSelection() {
		facetSelections = new HashMap<>();
		for (String facetCode : getActiveFacets()) {
			facetSelections.put(facetCode, new HashSet<String>());
		}
	}

	Metadata getMetadata(String code) {
		SchemaUtils utils = new SchemaUtils();
		String schemaCode = utils.getSchemaCode(code);
		return schema(schemaCode).getMetadata(utils.getLocalCode(code, schemaCode));
	}

	List<String> getActiveFacets() {
		return schemasDisplayManager().getTypes(view.getCollection()).getFacetMetadataCodes();
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

	public List<LabelTemplate> getTemplates() {
		return appLayerFactory.getLabelTemplateManager().listTemplates(null);
	}
}
