package com.constellio.model.services.search;

import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.bigVault.LazyResultsIterator;
import com.constellio.data.dao.services.bigVault.LazyResultsKeepingOrderIterator;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.collections.CollectionsListManagerRuntimeException.CollectionsListManagerRuntimeException_NoSuchCollection;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.search.query.FilterUtils;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.UserFilter;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderParams;
import com.constellio.model.services.security.SecurityTokenManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.*;

import java.util.*;
import java.util.Map.Entry;

public class SearchServices {
	RecordDao recordDao;
	RecordServices recordServices;
	SecurityTokenManager securityTokenManager;
	CollectionsListManager collectionsListManager;
	RecordsCaches recordsCaches;
	MetadataSchemasManager metadataSchemasManager;
	String mainDataLanguage;

	public SearchServices(RecordDao recordDao, ModelLayerFactory modelLayerFactory) {
		this.recordDao = recordDao;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.securityTokenManager = modelLayerFactory.getSecurityTokenManager();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		mainDataLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();
		recordsCaches = modelLayerFactory.getRecordsCaches();
	}

	public SPEQueryResponse query(LogicalSearchQuery query) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		return buildResponse(params, query);
	}

	public List<Record> cachedSearch(LogicalSearchQuery query) {
		RecordsCache recordsCache = recordsCaches.getCache(query.getCondition().getCollection());
		List<Record> records = recordsCache.getQueryResults(query);
		if (records == null) {
			records = search(query);
			recordsCache.insertQueryResults(query, records);
		}
		return records;
	}

	public Map<Record, Map<Record, Double>> searchWithMoreLikeThis(LogicalSearchQuery query) {
		return query(query).getRecordsWithMoreLikeThis();
	}

	public List<Record> search(LogicalSearchQuery query) {
		return query(query).getRecords();
	}

	public Record searchSingleResult(LogicalSearchCondition condition) {
		SPEQueryResponse response = query(new LogicalSearchQuery(condition).setNumberOfRows(1));
		if (response.getNumFound() > 1) {
			SolrQueryBuilderParams params = new SolrQueryBuilderParams(false, "?");
			throw new SearchServicesRuntimeException.TooManyRecordsInSingleSearchResult(condition.getSolrQuery(params));
		}
		return response.getNumFound() == 1 ? response.getRecords().get(0) : null;
	}

	public Iterator<List<Record>> recordsBatchIterator(int batch, LogicalSearchQuery query) {
		Iterator<Record> recordsIterator = recordsIterator(query, batch);
		return new BatchBuilderIterator<>(recordsIterator, batch);
	}

	public Iterator<List<Record>> recordsBatchIterator(LogicalSearchQuery query) {
		return recordsBatchIterator(100, query);
	}

	public SearchResponseIterator<Record> recordsIterator(LogicalSearchQuery query) {
		return recordsIterator(query, 100);
	}

	public SearchResponseIterator<Record> recordsIterator(LogicalSearchQuery query, int batchSize) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		return new LazyResultsIterator<Record>(recordDao, params, batchSize) {

			@Override
			public Record convert(RecordDTO recordDTO) {
				return recordServices.toRecord(recordDTO, fullyLoaded);
			}
		};
	}

	public SearchResponseIterator<Record> recordsIteratorKeepingOrder(LogicalSearchQuery query, int batchSize) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		return new LazyResultsKeepingOrderIterator<Record>(recordDao, params, batchSize) {

			@Override
			public Record convert(RecordDTO recordDTO) {
				return recordServices.toRecord(recordDTO, fullyLoaded);
			}
		};
	}

	public long getResultsCount(LogicalSearchCondition condition) {
		return getResultsCount(new LogicalSearchQuery(condition));
	}

	public long getResultsCount(LogicalSearchQuery query) {
		int oldNumberOfRows = query.getNumberOfRows();
		query.setNumberOfRows(0);
		ModifiableSolrParams params = addSolrModifiableParams(query);
		long result = recordDao.query(params).getNumFound();
		query.setNumberOfRows(oldNumberOfRows);
		return result;
	}

	public List<String> searchRecordIds(LogicalSearchCondition condition) {
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		return searchRecordIds(query);
	}

	public List<String> searchRecordIds(LogicalSearchQuery query) {
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		ModifiableSolrParams params = addSolrModifiableParams(query);

		List<String> ids = new ArrayList<>();
		for (Record record : buildResponse(params, query).getRecords()) {
			ids.add(record.getId());
		}
		return ids;
	}

	public Iterator<String> recordsIdsIterator(LogicalSearchQuery query) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		return new LazyResultsIterator<String>(recordDao, params, 10000) {

			@Override
			public String convert(RecordDTO recordDTO) {
				return recordDTO.getId();
			}
		};
	}

	public boolean hasResults(LogicalSearchQuery query) {
		return getResultsCount(query) != 0;
	}

	public boolean hasResults(LogicalSearchCondition condition) {
		return getResultsCount(condition) != 0;
	}

	public String getLanguage(LogicalSearchQuery query) {
		String collection = query.getCondition().getCollection();
		String language;
		try {
			language = collectionsListManager.getCollectionLanguages(collection).get(0);
		} catch (CollectionsListManagerRuntimeException_NoSuchCollection e) {
			language = mainDataLanguage;
		}
		return language;
	}

	public ModifiableSolrParams addSolrModifiableParams(LogicalSearchQuery query) {
		ModifiableSolrParams params = new ModifiableSolrParams();

		for (String filterQuery : query.getFilterQueries()) {
			params.add(CommonParams.FQ, filterQuery);
		}
		addUserFilter(params, query.getUserFilter());

		String language = getLanguage(query);
		params.add(CommonParams.FQ, "" + query.getQuery(language));

		params.add(CommonParams.QT, "/spell");
		params.add(ShardParams.SHARDS_QT, "/spell");

		if (query.getFreeTextQuery() != null) {
			String qf = getQfFor(query.getFieldBoosts());
			params.add(DisMaxParams.QF, qf);
			params.add(DisMaxParams.PF, qf);
			params.add(DisMaxParams.MM, "2<66%");
			params.add("defType", "edismax");
			params.add(DisMaxParams.BQ, "\"" + query.getFreeTextQuery() + "\"");

			for (SearchBoost boost : query.getQueryBoosts()) {
				params.add(DisMaxParams.BQ, boost.getKey() + "^" + boost.getValue());
			}
		}

		String userCondition = "";
		if (query.getQueryCondition() != null) {
			userCondition = " AND " + query.getQueryCondition().getSolrQuery(new SolrQueryBuilderParams(false, "?"));
		}

		params.add(CommonParams.Q, String.format("%s%s", StringUtils.defaultString(query.getFreeTextQuery(), "*:*")
				, userCondition));

		params.add(CommonParams.ROWS, "" + query.getNumberOfRows());
		params.add(CommonParams.START, "" + query.getStartRow());

		if (!query.getFieldFacets().isEmpty() || !query.getQueryFacets().isEmpty()) {
			params.add(FacetParams.FACET, "true");
			params.add(FacetParams.FACET_SORT, FacetParams.FACET_SORT_COUNT);
		}
		if (!query.getFieldFacets().isEmpty()) {
			params.add(FacetParams.FACET_MINCOUNT, "1");
			for (String field : query.getFieldFacets()) {
				params.add(FacetParams.FACET_FIELD, "{!ex=" + field + "}" + field);
			}
			if (query.getFieldFacetLimit() != 0) {
				params.add(FacetParams.FACET_LIMIT, "" + query.getFieldFacetLimit());
			}
		}
		if (!query.getStatisticFields().isEmpty()) {
			params.set(StatsParams.STATS, "true");
			for (String field : query.getStatisticFields()) {
				params.add(StatsParams.STATS_FIELD, field);
			}
		}
		if (!query.getQueryFacets().isEmpty()) {
			for (Entry<String, Set<String>> facetQuery : query.getQueryFacets().getMapEntries()) {
				for (String aQuery : facetQuery.getValue()) {
					params.add(FacetParams.FACET_QUERY, "{!ex=f" + facetQuery.getKey() + "}" + aQuery);
				}
			}
		}

		String sort = query.getSort();
		if (!sort.isEmpty()) {
			params.add(CommonParams.SORT, sort);
		}

		if (query.getReturnedMetadatas() != null && query.getReturnedMetadatas().getAcceptedFields() != null) {
			List<String> fields = new ArrayList<>();
			fields.add("id");
			fields.add("schema_s");
			fields.add("_version_");
			fields.add("collection_s");
			fields.addAll(query.getReturnedMetadatas().getAcceptedFields());
			params.set(CommonParams.FL, StringUtils.join(fields.toArray(), ","));

		}

		if (query.isHighlighting()) {
			HashSet<String> highligthedMetadatas = new HashSet<>();
			MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(query.getCondition().getCollection());
			for (Metadata metadata : types.getHighlightedMetadatas()) {
				highligthedMetadatas.add(metadata.getAnalyzedField(language).getDataStoreCode());
			}

			params.add(HighlightParams.HIGHLIGHT, "true");
			params.add(HighlightParams.FIELDS, StringUtils.join(highligthedMetadatas, " "));
			params.add(HighlightParams.SNIPPETS, "1");
			params.add(HighlightParams.FRAGSIZE, "140");
			params.add(HighlightParams.MERGE_CONTIGUOUS_FRAGMENTS, "true");
		}

		if (query.isSpellcheck()) {
			params.add("spellcheck", "on");
		}

		if (query.getOverridedQueryParams() != null) {
			for (Map.Entry<String, String[]> overridedQueryParam : query.getOverridedQueryParams().entrySet()) {
				params.remove(overridedQueryParam.getKey());
				if (overridedQueryParam.getValue() != null) {
					for (String value : overridedQueryParam.getValue()) {
						params.add(overridedQueryParam.getKey(), value);
					}
				}

			}
		}

		if (query.isMoreLikeThis() /*&& query.getMoreLikeThisFields().size() > 0*/) {
			params.add(MoreLikeThisParams.MLT, "true");
			params.add(MoreLikeThisParams.MIN_DOC_FREQ, "0");
			params.add(MoreLikeThisParams.MIN_TERM_FREQ, "0");
			List<String> moreLikeThisFields = query.getMoreLikeThisFields();
			if (moreLikeThisFields.isEmpty()) {
				moreLikeThisFields.addAll(Arrays.asList("content_txt_fr", "content_txt_en", "content_txt_ar"));
			}

			StringBuilder similarityFields = new StringBuilder();
			for (String aSimilarityField : moreLikeThisFields) {
				if (similarityFields.length() != 0)
					similarityFields.append(",");
				if (!aSimilarityField.contains("_txt_") && !aSimilarityField.contains("_t_")) {
					System.err.printf("The %s does not support term vector. It may cause performance issue.\n", aSimilarityField);
				}
				similarityFields.append(aSimilarityField);
			}

			params.add(MoreLikeThisParams.SIMILARITY_FIELDS, similarityFields.toString());
		}

		return params;
	}

	private String getQfFor(List<SearchBoost> boosts) {
		StringBuilder sb = new StringBuilder();
		for (SearchBoost boost : boosts) {
			sb.append(boost.getKey());
			sb.append("^");
			sb.append(boost.getValue());
			sb.append(" ");
		}
		sb.append("search_txt_");
		sb.append(mainDataLanguage);
		return sb.toString();
	}

	private SPEQueryResponse buildResponse(ModifiableSolrParams params, LogicalSearchQuery query) {
		QueryResponseDTO queryResponseDTO = recordDao.query(params);
		List<RecordDTO> recordDTOs = queryResponseDTO.getResults();

		List<Record> records = recordServices.toRecords(recordDTOs, query.getReturnedMetadatas().isFullyLoaded());

		Map<Record, Map<Record, Double>> moreLikeThisResult = getResultWithMoreLikeThis(
				queryResponseDTO.getResultsWithMoreLikeThis());

		Map<String, List<FacetValue>> fieldFacetValues = buildFacets(query.getFieldFacets(),
				queryResponseDTO.getFieldFacetValues());
		Map<String, Integer> queryFacetValues = withRemoveExclusions(queryResponseDTO.getQueryFacetValues());

		Map<String, Map<String, Object>> statisticsValues = buildStats(query.getStatisticFields(),
				queryResponseDTO.getFieldsStatistics());
		SPEQueryResponse response = new SPEQueryResponse(fieldFacetValues, statisticsValues, queryFacetValues,
				queryResponseDTO.getQtime(),
				queryResponseDTO.getNumFound(), records, queryResponseDTO.getHighlights(),
				queryResponseDTO.isCorrectlySpelt(), queryResponseDTO.getSpellCheckerSuggestions(), moreLikeThisResult);

		if (query.getResultsProjection() != null) {
			return query.getResultsProjection().project(query, response);
		} else {
			return response;
		}
	}

	private Map<Record, Map<Record, Double>> getResultWithMoreLikeThis(
			Map<RecordDTO, Map<RecordDTO, Double>> resultsWithMoreLikeThis) {
		Map<Record, Map<Record, Double>> results = new LinkedHashMap<>();
		for (Entry<RecordDTO, Map<RecordDTO, Double>> aDocWithMoreLikeThis : resultsWithMoreLikeThis.entrySet()) {
			Map<Record, Double> similarRecords = new LinkedHashMap<>();
			for (Entry<RecordDTO, Double> similarDocWithScore : aDocWithMoreLikeThis.getValue().entrySet()) {
				similarRecords.put(recordServices.toRecord(similarDocWithScore.getKey(), true), similarDocWithScore.getValue());
			}
			results.put(recordServices.toRecord(aDocWithMoreLikeThis.getKey(), true), similarRecords);
		}
		return results;
	}

	private Map<String, Integer> withRemoveExclusions(Map<String, Integer> queryFacetValues) {
		if (queryFacetValues == null) {
			return null;
		}
		Map<String, Integer> withRemovedExclusions = new HashMap<>();
		for (Map.Entry<String, Integer> queryEntry : queryFacetValues.entrySet()) {
			String query = queryEntry.getKey();
			query = query.substring(query.indexOf("}") + 1);
			withRemovedExclusions.put(query, queryEntry.getValue());
		}
		return withRemovedExclusions;
	}

	private Map<String, List<FacetValue>> buildFacets(
			List<String> fields, Map<String, List<FacetValue>> facetValues) {
		Map<String, List<FacetValue>> result = new HashMap<>();
		for (String field : fields) {
			List<FacetValue> values = facetValues.get(field);
			if (values != null) {
				result.put(field, values);
			}
		}
		return result;
	}

	private Map<String, Map<String, Object>> buildStats(
			List<String> fields, Map<String, Map<String, Object>> fieldStatsValues) {
		Map<String, Map<String, Object>> result = new HashMap<>();
		for (String field : fields) {
			Map<String, Object> values = fieldStatsValues.get(field);
			if (values != null) {
				result.put(field, values);
			}
		}
		return result;
	}

	private void addUserFilter(ModifiableSolrParams params, UserFilter userFilter) {
		if (userFilter == null) {
			return;
		}
		String filter;
		switch (userFilter.getAccess()) {
		case Role.READ:
			filter = FilterUtils.userReadFilter(userFilter.getUser(), securityTokenManager);
			break;
		case Role.WRITE:
			filter = FilterUtils.userWriteFilter(userFilter.getUser(), securityTokenManager);
			break;
		case Role.DELETE:
			filter = FilterUtils.userDeleteFilter(userFilter.getUser(), securityTokenManager);
			break;
		default:
			throw new ImpossibleRuntimeException("Unknown access: " + userFilter.getAccess());
		}

		params.add(CommonParams.FQ, filter);
	}
}
