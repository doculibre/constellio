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
package com.constellio.model.services.search.cache;

import static java.util.Collections.unmodifiableMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class SerializedCacheSearchService {

	private static Map<String, List<FacetValue>> emptyFieldFacetValues = Collections.emptyMap();
	private static Map<String, Integer> emptyQueryFacetsValues = Collections.emptyMap();
	private static Map<String, Map<String, Object>> emptyStatisticsValues = Collections.emptyMap();
	private static List<String> emptySpellcheckerSuggestions = Collections.emptyList();
	boolean correctlySpelt = true;

	Map<String, Map<String, List<String>>> highlights;

	SerializableSearchCache cache;

	ModelLayerFactory modelLayerFactory;

	SearchServices searchServices;

	public SerializedCacheSearchService(ModelLayerFactory modelLayerFactory,
			SerializableSearchCache cache) {
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.cache = cache;
		this.highlights = new HashMap<>();
	}

	public SPEQueryResponse query(LogicalSearchQuery query) {
		return query(query, 10);
	}

	public SPEQueryResponse query(LogicalSearchQuery query, int batch) {

		validateQueryNotUsingUnsupportedFeatures(query);

		long qtime = 0L;
		LogicalSearchQuery duplicateQuery = new LogicalSearchQuery(query);
		List<Record> records = search(duplicateQuery, batch);
		Map<String, Map<String, List<String>>> highlights = unmodifiableMap(cache.getHighlightingMap());

		long numFound = records.size();
		return new SPEQueryResponse(emptyFieldFacetValues, emptyStatisticsValues, emptyQueryFacetsValues, qtime, numFound,
				records, highlights, correctlySpelt, emptySpellcheckerSuggestions);
	}

	private void validateQueryNotUsingUnsupportedFeatures(LogicalSearchQuery query) {
		if (!query.getFieldFacets().isEmpty() || !query.getQueryFacets().isEmpty()) {
			throw new IllegalArgumentException("This service doesn't support facetting");
		}

		if (!query.getStatisticFields().isEmpty()) {
			throw new IllegalArgumentException("This service doesn't support stats");
		}

		if (query.isSpellcheck()) {
			throw new IllegalArgumentException("This service doesn't support spellcheck");
		}

		if (query.getResultsProjection() != null) {
			throw new IllegalArgumentException("This service doesn't support results projection");
		}

	}

	public List<Record> search(LogicalSearchQuery query) {
		return search(query, 10);
	}

	public List<Record> search(LogicalSearchQuery query, int batch) {
		cache.initializeFor(query);
		return new LazyRecordList(batch, cache, modelLayerFactory, query);
	}
}
