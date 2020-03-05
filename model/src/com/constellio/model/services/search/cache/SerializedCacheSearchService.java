package com.constellio.model.services.search.cache;

import com.constellio.data.dao.dto.records.FacetPivotValue;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.MoreLikeThisRecord;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.SearchQuery;
import com.constellio.model.services.search.query.list.RecordListSearchQuery;
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

	public SPEQueryResponse query(SearchQuery query) {
		return query(query, 10);
	}

	public SPEQueryResponse query(SearchQuery query, final int batch) {
		long qtime = System.currentTimeMillis();
		SearchQuery duplicateQuery = query.clone();
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
				numFound, records, highlights, correctlySpelt, emptySpellcheckerSuggestions, emptyRecordsWithMoreLikeThis);
	}

	private void validateQueryNotUsingUnsupportedFeatures(SearchQuery query) {
		validateQueryTypeIsSupported(query);
		LogicalSearchQuery logicalSearchQuery = (LogicalSearchQuery) query;
		if (!logicalSearchQuery.getStatisticFields().isEmpty()) {
			throw new IllegalArgumentException("This service doesn't support stats");
		}

		if (logicalSearchQuery.isSpellcheck()) {
			throw new IllegalArgumentException("This service doesn't support spellcheck");
		}

		if (logicalSearchQuery.getResultsProjection() != null) {
			throw new IllegalArgumentException("This service doesn't support results projection");
		}
	}

	private void validateQueryTypeIsSupported(SearchQuery query) {
		if (!(query instanceof LogicalSearchQuery || query instanceof RecordListSearchQuery)) {
			throw new IllegalArgumentException("This service doesn't support this search query implementation");
		}
	}

	public List<Record> search(SearchQuery query) {
		return search(query, 10);
	}

	public List<Record> search(SearchQuery query, int batch) {
		if (query instanceof LogicalSearchQuery) {
			return search((LogicalSearchQuery) query, batch);
		} else if (query instanceof RecordListSearchQuery) {
			return search((RecordListSearchQuery) query, batch);
		} else {
			throw (new IllegalArgumentException());
		}
	}

	private List<Record> search(LogicalSearchQuery query, int batch) {
		LogicalSearchQuery logicalSearchQuery = query;
		cache.initializeFor(logicalSearchQuery);
		return new LazyRecordList(batch, cache, modelLayerFactory, logicalSearchQuery, serializeRecords);
	}

	private List<Record> search(RecordListSearchQuery query, int batch) {
		//TODO include batch size (will need to generify SearchServices and LazyRecordList as well)
		return query.convertIdsToSummaryRecords(modelLayerFactory).getRecords();
	}

	public Map<String, List<FacetValue>> getFieldFacetValues(SearchQuery facetLoadingQuery) {
		if (!cache.areFacetsLoaded() && hasFacetsConfigured(facetLoadingQuery)) {
			validateQueryTypeIsSupported(facetLoadingQuery);
			SPEQueryResponse speQueryResponse = searchServices.query(((LogicalSearchQuery) facetLoadingQuery).clone().setNumberOfRows(0));
			cache.setFieldFacetValues(speQueryResponse.getFieldFacetValues());
			cache.setQueryFacetsValues(speQueryResponse.getQueryFacetsValues());
			cache.setFacetsComputed(true);
		}

		return cache.getFieldFacetValues();
	}

	public Map<String, Integer> getQueryFacetsValues(SearchQuery facetLoadingQuery) {
		if (!cache.areFacetsLoaded() && hasFacetsConfigured(facetLoadingQuery)) {
			validateQueryTypeIsSupported(facetLoadingQuery);
			SPEQueryResponse speQueryResponse = searchServices.query(((LogicalSearchQuery) facetLoadingQuery).clone().setNumberOfRows(0));
			cache.setFieldFacetValues(speQueryResponse.getFieldFacetValues());
			cache.setQueryFacetsValues(speQueryResponse.getQueryFacetsValues());
			cache.setFacetsComputed(true);
		}

		return cache.getQueryFacetsValues() == null ? Collections.emptyMap() : cache.getQueryFacetsValues();
	}

	private boolean hasFacetsConfigured(SearchQuery query) {
		return !query.getFieldFacets().isEmpty() || !query.getQueryFacets().isEmpty();
	}
}
