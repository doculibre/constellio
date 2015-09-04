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
package com.constellio.model.services.search.query.logical;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.FilterUtils;
import com.constellio.model.services.search.query.ResultsProjection;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.SearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;

//TODO Remove inheritance, rename to LogicalQuery
public class LogicalSearchQuery implements SearchQuery {
	private static final String HIGHLIGHTING_FIELDS = "search_*";

	LogicalSearchCondition condition;
	private LogicalSearchQueryFacetFilters facetFilters = new LogicalSearchQueryFacetFilters();
	private String freeTextQuery;
	String filterUser;
	String filterStatus;

	private int numberOfRows;
	private int startRow;

	private ReturnedMetadatasFilter returnedMetadatasFilter;
	private List<LogicalSearchQuerySort> sortFields = new ArrayList<>();
	private ResultsProjection resultsProjection;

	private KeySetMap<String, String> queryFacets = new KeySetMap<>();
	private List<String> fieldFacets = new ArrayList<>();
	private List<String> statisticFields = new ArrayList<>();
	private int fieldFacetLimit;

	private boolean highlighting = false;
	private boolean spellcheck = false;

	public LogicalSearchQuery() {
		numberOfRows = 10000000;
		startRow = 0;
		fieldFacetLimit = 0;
	}

	public LogicalSearchQuery(LogicalSearchCondition condition) {
		this();
		if (condition == null) {
			throw new IllegalArgumentException("Condition must not be null");
		}
		this.condition = condition;
	}

	public LogicalSearchQuery(LogicalSearchQuery query) {
		condition = query.condition;
		facetFilters = new LogicalSearchQueryFacetFilters(query.facetFilters);
		freeTextQuery = query.freeTextQuery;
		filterUser = query.filterUser;
		filterStatus = query.filterStatus;

		numberOfRows = query.numberOfRows;
		startRow = query.startRow;

		returnedMetadatasFilter = query.returnedMetadatasFilter;
		sortFields = new ArrayList<>(query.sortFields);
		resultsProjection = query.resultsProjection;

		queryFacets = new KeySetMap<>(query.queryFacets);
		fieldFacets = new ArrayList<>(query.fieldFacets);
		statisticFields = new ArrayList<>(query.statisticFields);
		fieldFacetLimit = query.fieldFacetLimit;

		highlighting = query.highlighting;
		spellcheck = query.spellcheck;
	}

	// The following methods are attribute accessors

	public LogicalSearchCondition getCondition() {
		return condition;
	}

	public LogicalSearchQuery setCondition(LogicalSearchCondition condition) {
		this.condition = condition;
		return this;
	}

	public String getFreeTextQuery() {
		return freeTextQuery;
	}

	public LogicalSearchQuery setFreeTextQuery(String freeTextQuery) {
		this.freeTextQuery = freeTextQuery;
		return this;
	}

	@Override
	public LogicalSearchQuery filteredWithUser(User user) {
		filterUser = FilterUtils.userReadFilter(user);
		return this;
	}

	@Override
	public LogicalSearchQuery filteredWithUser(User user, String access) {
		if (access.equals(Role.READ)) {
			filterUser = FilterUtils.userReadFilter(user);

		} else if (access.equals(Role.WRITE)) {
			filterUser = FilterUtils.userWriteFilter(user);

		} else if (access.equals(Role.DELETE)) {
			filterUser = FilterUtils.userDeleteFilter(user);

		}
		return this;
	}

	@Override
	public LogicalSearchQuery computeStatsOnField(String metadata) {
		this.statisticFields.add(metadata);
		return this;
	}

	public LogicalSearchQuery filteredWithUserWrite(User user) {
		filterUser = FilterUtils.userWriteFilter(user);
		return this;
	}

	public LogicalSearchQuery filteredWithUserDelete(User user) {
		filterUser = FilterUtils.userDeleteFilter(user);
		return this;
	}

	public LogicalSearchQuery filteredByStatus(StatusFilter status) {
		filterStatus = FilterUtils.statusFilter(status);
		return this;
	}

	@Override
	public int getStartRow() {
		return this.startRow;
	}

	@Override
	public LogicalSearchQuery setStartRow(int row) {
		startRow = row;
		return this;
	}

	@Override
	public int getNumberOfRows() {
		return numberOfRows;
	}

	@Override
	public LogicalSearchQuery setNumberOfRows(int number) {
		numberOfRows = number;
		return this;
	}

	public ReturnedMetadatasFilter getReturnedMetadatas() {
		return returnedMetadatasFilter == null ? ReturnedMetadatasFilter.all() : returnedMetadatasFilter;
	}

	public LogicalSearchQuery setReturnedMetadatas(ReturnedMetadatasFilter filter) {
		this.returnedMetadatasFilter = filter;
		return this;
	}

	public void clearSort() {
		sortFields.clear();
	}

	public LogicalSearchQuery sortAsc(DataStoreField field) {
		sortFields.add(new LogicalSearchQuerySort(field.getDataStoreCode(), true));
		return this;
	}

	public LogicalSearchQuery sortDesc(DataStoreField field) {
		sortFields.add(new LogicalSearchQuerySort(field.getDataStoreCode(), false));
		return this;
	}

	public ResultsProjection getResultsProjection() {
		return resultsProjection;
	}

	public LogicalSearchQuery setResultsProjection(ResultsProjection resultsProjection) {
		this.resultsProjection = resultsProjection;
		return this;
	}

	public KeySetMap<String, String> getQueryFacets() {
		return queryFacets;
	}

	public LogicalSearchQuery addQueryFacet(String facetGroup, String queryFacet) {
		queryFacets.add(facetGroup, queryFacet);
		return this;
	}

	public List<String> getFieldFacets() {
		return fieldFacets;
	}

	public List<String> getStatisticFields() {
		return statisticFields;
	}

	public LogicalSearchQuery addFieldFacet(String fieldFacet) {
		fieldFacets.add(fieldFacet);
		return this;
	}

	public LogicalSearchQueryFacetFilters getFacetFilters() {
		return facetFilters;
	}

	public int getFieldFacetLimit() {
		return fieldFacetLimit;
	}

	public LogicalSearchQuery setFieldFacetLimit(int fieldFacetLimit) {
		this.fieldFacetLimit = fieldFacetLimit;
		return this;
	}

	public LogicalSearchQuery setHighlighting(boolean highlighting) {
		this.highlighting = highlighting;
		return this;
	}

	public boolean isHighlighting() {
		return highlighting;
	}

	public boolean isSpellcheck() {
		return spellcheck;
	}

	public LogicalSearchQuery setSpellcheck(boolean spellcheck) {
		this.spellcheck = spellcheck;
		return this;
	}

	// The following methods are mainly used by the SPE itself

	@Override
	public String getQuery() {
		return condition.getSolrQuery();
	}

	@Override
	public List<String> getFilterQueries() {
		List<String> filterQueries = new ArrayList<>();

		for (String filterQuery : condition.getFilters().getFilterQueries()) {
			filterQueries.add(filterQuery);
		}

		if (filterUser != null) {
			filterQueries.add(filterUser);
		}

		if (filterStatus != null) {
			filterQueries.add(filterStatus);
		}

		filterQueries.addAll(facetFilters.toSolrFilterQueries());

		return filterQueries;
	}

	public String getSort() {
		StringBuilder stringBuilder = new StringBuilder();

		for (LogicalSearchQuerySort sort : sortFields) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}
			String sorFieldName = sortFieldName(sort);
			stringBuilder.append(sorFieldName);
			stringBuilder.append(" ");
			stringBuilder.append(sort.isAscending() ? "asc" : "desc");
		}

		return stringBuilder.toString();
	}

	private String sortFieldName(LogicalSearchQuerySort sort) {
		String fieldName = sort.getFieldName();
		if (fieldName != null && fieldName.endsWith("_s")) {
			return fieldName.substring(0, fieldName.length() - 2) + "_fs-s";
		}
		return fieldName;
	}

	@Deprecated
	public MetadataSchema getSchemaCondition() {
		return ((SchemaFilters) condition.getFilters()).getSchema();
	}

	public String getSchemaTypeCondition() {
		return ((SchemaFilters) condition.getFilters()).getSchemaType();
	}

	public String getHighlightingFields() {
		return HIGHLIGHTING_FIELDS;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public static LogicalSearchQuery returningNoResults() {
		return new LogicalSearchQuery(LogicalSearchQueryOperators.fromAllSchemasIn("inexistentCollection42").returnAll());
	}

}
