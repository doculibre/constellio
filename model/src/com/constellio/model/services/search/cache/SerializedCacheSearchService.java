package com.constellio.model.services.search.cache;

import com.constellio.data.dao.dto.records.FacetPivotValue;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.MoreLikeThisRecord;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public class SerializedCacheSearchService {
	private static Map<String, List<FacetPivotValue>> emptyFacetPivotValues = Collections.emptyMap();
	private static List<MoreLikeThisRecord> emptyRecordsWithMoreLikeThis = Collections.emptyList();
	private static Map<String, Map<String, Object>> emptyStatisticsValues = Collections.emptyMap();
	private static List<String> emptySpellcheckerSuggestions = Collections.emptyList();
	private static Map<String, Object> noDebugMap = null;
	boolean correctlySpelt = true;
	boolean serializeRecords;

	Map<String, Map<String, List<String>>> highlights;

	SerializableSearchCache cache;

	ModelLayerFactory modelLayerFactory;

	SearchServices searchServices;

	public SerializedCacheSearchService(ModelLayerFactory modelLayerFactory,
										SerializableSearchCache cache, boolean serializeRecords) {
		this.modelLayerFactory = modelLayerFactory;
		this.searchServices = modelLayerFactory.newSearchServices();
		this.cache = cache;
		this.serializeRecords = serializeRecords;
		this.highlights = new HashMap<>();
	}

	public SPEQueryResponse query(LogicalSearchQuery query) {
		return query(query, 10);
	}

	public SPEQueryResponse query(LogicalSearchQuery query, final int batch) {
		long qtime = System.currentTimeMillis();
		LogicalSearchQuery duplicateQuery = new LogicalSearchQuery(query);
		List<Record> records = search(duplicateQuery, batch);
		Map<String, Map<String, List<String>>> highlights = unmodifiableMap(cache.getHighlightingMap());

		long numFound = records.size();

		//		Map<String, List<FacetValue>> fieldFacetValues = new LazyMap<String, List<FacetValue>>() {
		//			@Override
		//			Map<String, List<FacetValue>> getMap() {
		//				return getFieldFacetValues(batch);
		//			}
		//		};
		//		Map<String, Integer> queryFacetsValues = new LazyMap<String, Integer>() {
		//			@Override
		//			Map<String, Integer> getMap() {
		//				return getQueryFacetsValues(batch);
		//			}
		//		};

		Map<String, List<FacetValue>> fieldFacetValues = cache.getFieldFacetValues();
		Map<String, Integer> queryFacetsValues = cache.getQueryFacetsValues();

		long duration = Math.max(System.currentTimeMillis() - qtime, 1);

		return new SPEQueryResponse(fieldFacetValues, emptyFacetPivotValues, emptyStatisticsValues, queryFacetsValues, duration,
				numFound, records, highlights, noDebugMap, correctlySpelt, emptySpellcheckerSuggestions, emptyRecordsWithMoreLikeThis);
	}

	private void validateQueryNotUsingUnsupportedFeatures(LogicalSearchQuery query) {
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
		validateQueryNotUsingUnsupportedFeatures(query);
		cache.initializeFor(query);
		return new LazyRecordList(batch, cache, modelLayerFactory, query, serializeRecords);
	}

	public Map<String, List<FacetValue>> getFieldFacetValues(LogicalSearchQuery facetLoadingQuery) {
		if (!cache.areFacetsLoaded() && hasFacetsConfigured(facetLoadingQuery)) {
			SPEQueryResponse speQueryResponse = searchServices.query(new LogicalSearchQuery(facetLoadingQuery).setNumberOfRows(0));
			cache.setFieldFacetValues(speQueryResponse.getFieldFacetValues());
			cache.setQueryFacetsValues(speQueryResponse.getQueryFacetsValues());
			cache.setFacetsComputed(true);
		}

		return cache.getFieldFacetValues();
	}

	public Map<String, Integer> getQueryFacetsValues(LogicalSearchQuery facetLoadingQuery) {
		if (!cache.areFacetsLoaded() && hasFacetsConfigured(facetLoadingQuery)) {
			SPEQueryResponse speQueryResponse = searchServices.query(new LogicalSearchQuery(facetLoadingQuery).setNumberOfRows(0));
			cache.setFieldFacetValues(speQueryResponse.getFieldFacetValues());
			cache.setQueryFacetsValues(speQueryResponse.getQueryFacetsValues());
			cache.setFacetsComputed(true);
		}

		return cache.getQueryFacetsValues() == null ? Collections.emptyMap() : cache.getQueryFacetsValues();
	}

	private boolean hasFacetsConfigured(LogicalSearchQuery query) {
		return !query.getFieldFacets().isEmpty() || !query.getQueryFacets().isEmpty();
	}
}
