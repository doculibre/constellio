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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.data.utils.KeySetMap;

public class LogicalSearchQueryFacetFilters {

	private KeySetMap<String, String> fieldFacetFilters = new KeySetMap<>();

	private KeySetMap<String, String> queries = new KeySetMap<>();

	public LogicalSearchQueryFacetFilters() {
	}

	public LogicalSearchQueryFacetFilters(LogicalSearchQueryFacetFilters filters) {
		this.fieldFacetFilters = new KeySetMap<>(filters.fieldFacetFilters);
		this.queries = new KeySetMap<>(filters.queries);
	}

	public LogicalSearchQueryFacetFilters selectedFieldFacetValue(String datastoreCode, String value) {
		fieldFacetFilters.add(datastoreCode, value);
		return this;
	}

	public LogicalSearchQueryFacetFilters selectedFieldFacetValues(String datastoreCode, Collection<String> values) {
		for (String value : values) {
			fieldFacetFilters.add(datastoreCode, value);
		}
		return this;
	}

	public LogicalSearchQueryFacetFilters unselectedFieldFacetValue(String datastoreCode, String value) {
		fieldFacetFilters.remove(datastoreCode, value);
		return this;
	}

	public LogicalSearchQueryFacetFilters selectedQueryFacetValues(String queriesGroup, Collection<String> queries) {
		this.queries.get(queriesGroup).addAll(queries);
		return this;
	}

	public LogicalSearchQueryFacetFilters selectedQueryFacetValue(String queriesGroup, String query) {
		queries.add(queriesGroup, query);
		return this;
	}

	public LogicalSearchQueryFacetFilters unselectedQueryFacetValue(String queriesGroup, String query) {
		queries.remove(queriesGroup, query);
		return this;
	}

	public List<String> toSolrFilterQueries() {
		List<String> solrFilterQueries = new ArrayList<>();

		for (Map.Entry<String, Set<String>> entry : fieldFacetFilters.getMapEntries()) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("{!tag=");
			stringBuilder.append(entry.getKey());
			stringBuilder.append("}");
			stringBuilder.append(entry.getKey());
			stringBuilder.append(":(");
			stringBuilder.append(StringUtils.join(entry.getValue().toArray(), " OR "));
			stringBuilder.append(")");

			solrFilterQueries.add(stringBuilder.toString());
		}

		for (Entry<String, Set<String>> facetQueries : queries.getMapEntries()) {

			//			Set<String> excludedFields = CriteriaUtils.getFirstFieldsOfQueries(facetQueries.);
			//			for (String facetQuery : facetQueries) {
			//
			//			}

			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append("{!tag=f");
			stringBuilder.append(facetQueries.getKey());
			stringBuilder.append("}");
			stringBuilder.append(" (");
			stringBuilder.append(StringUtils.join(facetQueries.getValue().toArray(), " OR "));
			stringBuilder.append(")");
			solrFilterQueries.add(stringBuilder.toString());
		}

		return solrFilterQueries;
	}

	public void clear() {
		fieldFacetFilters.clear();
		queries.clear();
	}

	public Set<String> getSelectedFieldValues(String fieldDataStoreCode) {
		return fieldFacetFilters.get(fieldDataStoreCode);
	}

	public Set<String> getSelectedQueryFacets(String queriesGroup) {
		return queries.get(queriesGroup);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
