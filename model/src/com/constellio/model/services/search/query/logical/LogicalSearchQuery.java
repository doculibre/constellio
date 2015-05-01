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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataSchema;
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
	String filterUser;
	String filterStatus;
	private int numberOfRows;
	private int startRow;
	private ReturnedMetadatasFilter returnedMetadatasFilter;
	private List<LogicalSearchQuerySort> sortFields = new ArrayList<>();
	private List<String> queryFacets = new ArrayList<>();
	private List<DataStoreField> fieldFacets = new ArrayList<>();
	private Map<DataStoreField, Set<String>> facetFilters;
	private boolean highlighting = false;
	private boolean spellcheck = false;
	private ResultsProjection resultsProjection;

	public LogicalSearchQuery(LogicalSearchQuery query) {
		condition = query.condition;
		numberOfRows = query.numberOfRows;
		startRow = query.startRow;
		sortFields = new ArrayList<>(query.sortFields);
		returnedMetadatasFilter = query.returnedMetadatasFilter;
		filterUser = query.filterUser;
		filterStatus = query.filterStatus;
		facetFilters = query.facetFilters;
	}

	public LogicalSearchQuery() {
		numberOfRows = 10000000;
		startRow = 0;
		facetFilters = new HashMap<>();
	}

	public LogicalSearchQuery(LogicalSearchCondition condition) {
		if (condition == null) {
			throw new IllegalArgumentException("Condition must not be null");
		}
		this.condition = condition;
		numberOfRows = 10000000;
		startRow = 0;
		filterUser = null;
		filterStatus = null;
		facetFilters = new HashMap<>();
	}

	public ResultsProjection getResultsProjection() {
		return resultsProjection;
	}

	public LogicalSearchQuery setResultsProjection(ResultsProjection resultsProjection) {
		this.resultsProjection = resultsProjection;
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

	public LogicalSearchCondition getCondition() {
		return condition;
	}

	public LogicalSearchQuery setCondition(LogicalSearchCondition condition) {
		this.condition = condition;
		return this;
	}

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

		for (Entry<DataStoreField, Set<String>> filter : facetFilters.entrySet()) {
			filterQueries.add(
					"{!tag=" + filter.getKey().getDataStoreCode() + "}" + filter.getKey().getDataStoreCode() + ":(" + StringUtils
							.join(filter.getValue().toArray(), " OR ") + ")");
		}

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

	public List<DataStoreField> getFieldFacets() {
		return fieldFacets;
	}

	public LogicalSearchQuery addFieldFacet(DataStoreField fieldFacet) {
		fieldFacets.add(fieldFacet);
		return this;
	}

	public List<String> getQueryFacets() {
		return queryFacets;
	}

	public LogicalSearchQuery addQueryFacet(String queryFacet) {
		queryFacets.add(queryFacet);
		return this;
	}

	public MetadataSchema getSchemaCondition() {
		return ((SchemaFilters) condition.getFilters()).getSchema();
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
		return returnedMetadatasFilter;
	}

	public LogicalSearchQuery setReturnedMetadatas(ReturnedMetadatasFilter filter) {
		this.returnedMetadatasFilter = filter;
		return this;
	}

	@Override
	public LogicalSearchQuery filteredWithUser(User user) {
		filterUser = FilterUtils.userReadFilter(user);
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

	public LogicalSearchQuery filteredByFacetValues(DataStoreField field, Collection<String> values) {
		if (!facetFilters.containsKey(field)) {
			facetFilters.put(field, new HashSet<String>());
		}
		facetFilters.get(field).addAll(values);
		return this;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public LogicalSearchQuery setHighlighting(boolean highlighting) {
		this.highlighting = highlighting;
		return this;
	}

	public boolean isHighlighting() {
		return highlighting;
	}

	public String getHighlightingFields() {
		return HIGHLIGHTING_FIELDS;
	}

	public LogicalSearchQuery setSpellcheck(boolean spellcheck) {
		this.spellcheck = spellcheck;
		return this;
	}

	public boolean isSpellcheck() {
		return spellcheck;
	}

}
