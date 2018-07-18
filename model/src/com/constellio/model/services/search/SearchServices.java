package com.constellio.model.services.search;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.services.records.RecordUtils.splitByCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.StatsParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.dto.records.MoreLikeThisDTO;
import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.bigVault.LazyResultsIterator;
import com.constellio.data.dao.services.bigVault.LazyResultsKeepingOrderIterator;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.BatchBuilderSearchResponseIterator;
import com.constellio.data.utils.ThreadList;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.collections.CollectionsListManagerRuntimeException.CollectionsListManagerRuntimeException_NoSuchCollection;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.records.cache.RecordsCachesRequestMemoryImpl;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.FieldLogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.FunctionLogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.UserFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderParams;
import com.constellio.model.services.security.SecurityTokenManager;

public class SearchServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchServices.class);

	private static String[] STOP_WORDS_FR = { "au", "aux", "avec", "ce", "ces", "dans", "de", "des", "du", "elle", "en", "et",
			"eux", "il", "je", "la", "le", "leur", "lui", "ma", "mais", "me", "même", "mes", "moi", "mon", "ne", "nos", "notre",
			"nous", "on", "ou", "par", "pas", "pour", "qu", "que", "qui", "sa", "se", "ses", "son", "sur", "ta", "te", "tes",
			"toi", "ton", "tu", "un", "une", "vos", "votre", "vous", "c", "d", "j", "l", "à", "m", "n", "s", "t", "y", "été",
			"étée", "étées", "étés", "étant", "suis", "es", "est", "sommes", "êtes", "sont", "serai", "seras", "sera", "serons",
			"serez", "seront", "serais", "serait", "serions", "seriez", "seraient", "étais", "était", "étions", "étiez",
			"étaient", "fus", "fut", "fûmes", "fûtes", "furent", "sois", "soit", "soyons", "soyez", "soient", "fusse", "fusses",
			"fût", "fussions", "fussiez", "fussent", "ayant", "eu", "eue", "eues", "eus", "ai", "as", "avons", "avez", "ont",
			"aurai", "auras", "aura", "aurons", "aurez", "auront", "aurais", "aurait", "aurions", "auriez", "auraient", "avais",
			"avait", "avions", "aviez", "avaient", "eut", "eûmes", "eûtes", "eurent", "aie", "aies", "ait", "ayons", "ayez",
			"aient", "eusse", "eusses", "eût", "eussions", "eussiez", "eussent", "ceci", "cela", "celà", "cet", "cette", "ici",
			"ils", "les", "leurs", "quel", "quels", "quelle", "quelles", "sans", "soi" };

	//RecordDao recordDao;
	RecordServices recordServices;
	SecurityTokenManager securityTokenManager;
	CollectionsListManager collectionsListManager;
	RecordsCaches disconnectableRecordsCaches;
	MetadataSchemasManager metadataSchemasManager;
	String mainDataLanguage;
	ConstellioEIMConfigs systemConfigs;
	ModelLayerFactory modelLayerFactory;

	public SearchServices(RecordDao recordDao, ModelLayerFactory modelLayerFactory) {
		this(recordDao, modelLayerFactory, modelLayerFactory.getRecordsCaches());
	}

	public SearchServices(ModelLayerFactory modelLayerFactory, RecordsCaches recordsCaches) {
		this(modelLayerFactory.getDataLayerFactory().newRecordDao(), modelLayerFactory, recordsCaches);
	}

	private SearchServices(RecordDao recordDao, ModelLayerFactory modelLayerFactory, RecordsCaches recordsCaches) {
		this.recordServices = modelLayerFactory.newRecordServices();
		this.securityTokenManager = modelLayerFactory.getSecurityTokenManager();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		mainDataLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();
		this.systemConfigs = modelLayerFactory.getSystemConfigs();
		this.disconnectableRecordsCaches = recordsCaches;
		this.modelLayerFactory = modelLayerFactory;
	}

	public RecordsCaches getConnectedRecordsCache() {
		if (disconnectableRecordsCaches != null && (disconnectableRecordsCaches instanceof RecordsCachesRequestMemoryImpl)) {
			if (((RecordsCachesRequestMemoryImpl) disconnectableRecordsCaches).isDisconnected()) {
				disconnectableRecordsCaches = modelLayerFactory.getModelLayerFactoryFactory().get().getRecordsCaches();
			}
		}
		return disconnectableRecordsCaches;
	}

	public SPEQueryResponse query(LogicalSearchQuery query) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		return buildResponse(params, query);
	}

	public List<Record> cachedSearch(LogicalSearchQuery query) {
		RecordsCache recordsCache = getConnectedRecordsCache().getCache(query.getCondition().getCollection());
		List<Record> records = recordsCache.getQueryResults(query);
		if (records == null) {
			records = search(query);
			recordsCache.insertQueryResults(query, records);
		}
		return records;
	}

	public List<String> cachedSearchRecordIds(LogicalSearchQuery query) {
		RecordsCache recordsCache = getConnectedRecordsCache().getCache(query.getCondition().getCollection());
		List<String> records = recordsCache.getQueryResultIds(query);
		if (records == null) {
			records = searchRecordIds(query);
			recordsCache.insertQueryResultIds(query, records);
		}
		return records;
	}

	public List<MoreLikeThisRecord> searchWithMoreLikeThis(LogicalSearchQuery query) {
		return query(query).getMoreLikeThisRecords();
	}

	public List<Record> search(LogicalSearchQuery query) {
		return query(query).getRecords();
	}

	public Record searchSingleResult(LogicalSearchCondition condition) {
		SPEQueryResponse response = query(new LogicalSearchQuery(condition).setNumberOfRows(1));
		if (response.getNumFound() > 1) {
			SolrQueryBuilderParams params = new SolrQueryBuilderParams(false, "?", null) {
			};
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

	public SearchResponseIterator<Record> recordsIterator(LogicalSearchCondition condition) {
		return recordsIterator(new LogicalSearchQuery(condition));
	}

	public SearchResponseIterator<Record> recordsIterator(LogicalSearchCondition condition, int batchSize) {
		return recordsIterator(new LogicalSearchQuery(condition), batchSize);
	}

	public SearchResponseIterator<Record> recordsIterator(LogicalSearchQuery query) {
		return recordsIterator(query, 100);
	}

	public SearchResponseIterator<Record> recordsIterator(LogicalSearchQuery query, int batchSize) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		return new LazyResultsIterator<Record>(dataStoreDao(query.getDataStore()), params, batchSize, true) {

			@Override
			public Record convert(RecordDTO recordDTO) {
				return recordServices.toRecord(recordDTO, fullyLoaded);
			}
		};
	}

	public Iterator<List<Record>> reverseRecordsBatchIterator(int batch, LogicalSearchQuery query) {
		Iterator<Record> recordsIterator = reverseRecordsIterator(query, batch);
		return new BatchBuilderIterator<>(recordsIterator, batch);
	}

	public Iterator<List<Record>> reverseRecordsBatchIterator(LogicalSearchQuery query) {
		return reverseRecordsBatchIterator(100, query);
	}

	public SearchResponseIterator<Record> reverseRecordsIterator(LogicalSearchCondition condition) {
		return reverseRecordsIterator(new LogicalSearchQuery(condition));
	}

	public SearchResponseIterator<Record> reverseRecordsIterator(LogicalSearchCondition condition, int batchSize) {
		return reverseRecordsIterator(new LogicalSearchQuery(condition), batchSize);
	}

	public SearchResponseIterator<Record> reverseRecordsIterator(LogicalSearchQuery query) {
		return reverseRecordsIterator(query, 100);
	}

	public SearchResponseIterator<Record> reverseRecordsIterator(LogicalSearchQuery query, int batchSize) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		return new LazyResultsIterator<Record>(dataStoreDao(query.getDataStore()), params, batchSize, false) {

			@Override
			public Record convert(RecordDTO recordDTO) {
				return recordServices.toRecord(recordDTO, fullyLoaded);
			}
		};
	}

	public SearchResponseIterator<Record> recordsIteratorKeepingOrder(LogicalSearchQuery query, int batchSize) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		return new LazyResultsKeepingOrderIterator<Record>(dataStoreDao(query.getDataStore()), params, batchSize) {

			@Override
			public Record convert(RecordDTO recordDTO) {
				return recordServices.toRecord(recordDTO, fullyLoaded);
			}
		};
	}

	public SearchResponseIterator<Record> recordsIteratorKeepingOrder(LogicalSearchQuery query, int batchSize, int skipping) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		return new LazyResultsKeepingOrderIterator<Record>(dataStoreDao(query.getDataStore()), params, batchSize, skipping) {

			@Override
			public Record convert(RecordDTO recordDTO) {
				return recordServices.toRecord(recordDTO, fullyLoaded);
			}
		};
	}

	public SearchResponseIterator<Record> cachedRecordsIteratorKeepingOrder(LogicalSearchQuery query, final int batchSize) {
		LogicalSearchQuery querCompatibleWithCache = new LogicalSearchQuery(query);
		querCompatibleWithCache.setStartRow(0);
		querCompatibleWithCache.setNumberOfRows(100000);
		querCompatibleWithCache.setReturnedMetadatas(ReturnedMetadatasFilter.all());

		//final List<Record> original = search(query);
		final List<Record> records = cachedSearch(querCompatibleWithCache);

		//		if (original.size() != records.size()) {
		//			System.out.println("different");
		//		}

		final Iterator<Record> nestedIterator = records.iterator();
		return new SearchResponseIterator<Record>() {
			@Override
			public long getNumFound() {
				return records.size();
			}

			@Override
			public SearchResponseIterator<List<Record>> inBatches() {
				final SearchResponseIterator iterator = this;
				return new BatchBuilderSearchResponseIterator<Record>(iterator, batchSize) {

					@Override
					public long getNumFound() {
						return iterator.getNumFound();
					}
				};
			}

			@Override
			public boolean hasNext() {
				return nestedIterator.hasNext();
			}

			@Override
			public Record next() {
				return nestedIterator.next();
			}

			@Override
			public void remove() {
				nestedIterator.remove();
			}
		};
	}

	public SearchResponseIterator<Record> cachedRecordsIteratorKeepingOrder(LogicalSearchQuery query, int batchSize,
			int skipping) {

		SearchResponseIterator<Record> iterator = cachedRecordsIteratorKeepingOrder(query, batchSize);

		for (int i = 0; i < skipping && iterator.hasNext(); i++) {
			iterator.next();
		}
		return iterator;
		//		ModifiableSolrParams params = addSolrModifiableParams(query);
		//		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		//		return new LazyResultsKeepingOrderIterator<Record>(recordDao, params, batchSize, skipping) {
		//
		//			@Override
		//			public Record convert(RecordDTO recordDTO) {
		//				return recordServices.toRecord(recordDTO, fullyLoaded);
		//			}
		//		};
	}

	public long getResultsCount(LogicalSearchCondition condition) {
		return getResultsCount(new LogicalSearchQuery(condition));
	}

	public long getResultsCount(LogicalSearchQuery query) {
		int oldNumberOfRows = query.getNumberOfRows();
		query.setNumberOfRows(0);
		ModifiableSolrParams params = addSolrModifiableParams(query);
		long result = dataStoreDao(query.getDataStore()).query(query.getName(), params).getNumFound();
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
		return new LazyResultsIterator<String>(dataStoreDao(query.getDataStore()), params, 10000, true) {

			@Override
			public String convert(RecordDTO recordDTO) {
				return recordDTO.getId();
			}
		};
	}

	public Iterator<String> reverseRecordsIdsIterator(LogicalSearchQuery query) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		return new LazyResultsIterator<String>(dataStoreDao(query.getDataStore()), params, 10000, false) {

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
		if (query.getLanguage() != null) {
			return query.getLanguage();

		} else if (query.getCondition().isCollectionSearch()) {
			return getLanguageCode(query.getCondition().getCollection());

		} else {
			return mainDataLanguage;
		}
	}

	public String getCollection(LogicalSearchQuery query) {
		if (query.getCondition().isCollectionSearch()) {
			return query.getCondition().getCollection();

		} else {
			return null;
		}
	}

	public List<MetadataSchemaType> getSearchedTypes(LogicalSearchQuery query, MetadataSchemaTypes types) {
		if (types == null) {
			return Collections.emptyList();
		}

		List<MetadataSchemaType> metadataSchemaTypes = new ArrayList<>();
		if (query.getCondition() != null) {
			List<String> schemaTypesCodes = query.getCondition().getFilterSchemaTypesCodes();
			if (schemaTypesCodes != null) {
				for (String schemaTypeCode : schemaTypesCodes) {
					metadataSchemaTypes.add(types.getSchemaType(schemaTypeCode));
				}
			}
		}

		String collection = getCollection(query);
		if (metadataSchemaTypes.isEmpty() && collection != null) {
			metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(collection).getSchemaTypes();
		}

		return metadataSchemaTypes;
	}

	public String getLanguageCode(String collection) {
		String language;
		try {
			language = collectionsListManager.getCollectionLanguages(collection).get(0);
		} catch (CollectionsListManagerRuntimeException_NoSuchCollection e) {
			language = mainDataLanguage;
		}
		return language;
	}

	public ModifiableSolrParams addSolrModifiableParams(LogicalSearchQuery query) {
		return addSolrModifiableParams(query, true);
	}

	public ModifiableSolrParams addSolrModifiableParams(LogicalSearchQuery query, boolean addSynonyms) {
		ModifiableSolrParams params = new ModifiableSolrParams();

		for (String filterQuery : query.getFilterQueries()) {
			params.add(CommonParams.FQ, filterQuery);
		}

		String collection = getCollection(query);
		MetadataSchemaTypes types = null;
		if (collection != null && metadataSchemasManager != null && !collection.equals("inexistentCollection42")) {
			types = metadataSchemasManager.getSchemaTypes(collection);
		}

		List<MetadataSchemaType> searchedSchemaTypes = getSearchedTypes(query, types);

		String language = getLanguage(query);
		params.add(CommonParams.FQ, "" + query.getQuery(language, types));

		if (DataStore.RECORDS.equals(query.getDataStore()) || query.getDataStore() == null) {
			if (query.isMoreLikeThis()) {
				params.add(CommonParams.QT, "/mlt");
			} else {
				params.add(CommonParams.QT, "/spell");
				params.add(ShardParams.SHARDS_QT, "/spell");

			}
		}
		if (query.getFreeTextQuery() != null) {
			String qf = getQfFor(language, query.getFieldBoosts(), searchedSchemaTypes);
			params.add(DisMaxParams.QF, qf);
			params.add(DisMaxParams.PF, qf);
			if (systemConfigs.isReplaceSpacesInSimpleSearchForAnds()) {
				int mm = calcMM(query.getFreeTextQuery());
				params.add(DisMaxParams.MM, "" + mm);
			} else {
				params.add(DisMaxParams.MM, "1");
			}
			params.add("defType", "edismax");
			params.add(DisMaxParams.BQ, "\"" + query.getFreeTextQuery() + "\"");

			for (SearchBoost boost : query.getQueryBoosts()) {
				params.add(DisMaxParams.BQ, boost.getKey() + "^" + boost.getValue());
			}
		}

		//		String userCondition = "";
		//		if (query.getQueryCondition() != null) {
		//			userCondition = " AND " + query.getQueryCondition().getSolrQuery(new SolrQueryBuilderParams(false, "?")) + " AND (";
		//			if (query.getUserFilters() != null) {
		//				if (!userCondition.endsWith("(")) {
		//					userCondition += " OR ";
		//				}
		//				for (UserFilter userFilter : query.getUserFilters()) {
		//					userCondition += userFilter.buildFQ(securityTokenManager);
		//				}
		//
		//			}
		//			if (userCondition.endsWith("(")) {
		//				userCondition += "*:*";
		//			}
		//			userCondition += ")";
		//		}

		if (query.getUserFilters() != null) {
			for (UserFilter userFilter : query.getUserFilters()) {
				params.add(CommonParams.FQ, userFilter.buildFQ(securityTokenManager));
			}
		}

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

		String sort = getSortQuery(query);
		if (!sort.isEmpty()) {
			params.add(CommonParams.SORT, sort);
		}

		if (query.getReturnedMetadatas() != null && query.getReturnedMetadatas().getAcceptedFields() != null) {
			List<String> fields = new ArrayList<>();
			fields.add("id");
			fields.add("schema_s");
			fields.add("_version_");
			fields.add("collection_s");

			List<String> secondaryCollectionLanguages = new ArrayList<>();
			if (collection != null) {
				secondaryCollectionLanguages.addAll(
						collectionsListManager.getCollectionInfo(collection).getSecondaryCollectionLanguesCodes());
			}

			for (String field : query.getReturnedMetadatas().getAcceptedFields()) {
				fields.add(field);
				for (String secondaryCollectionLanguage : secondaryCollectionLanguages) {
					fields.add(Schemas.getSecondaryLanguageDataStoreCode(field, secondaryCollectionLanguage));
				}
			}

			params.set(CommonParams.FL, StringUtils.join(fields.toArray(), ","));

		}

		if (query.isHighlighting() && types != null) {
			HashSet<String> highligthedMetadatas = new HashSet<>();
			for (Metadata metadata : types.getSearchableMetadatas()) {
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

			if (params.get("rows") == null || Integer.parseInt(params.get("rows")) > 5) {
				params.set("rows", 5);
			}

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

		String collection = query.getCondition().getCollection();
		if (collection != null) {
			SearchConfigurationsManager manager = modelLayerFactory.getSearchConfigurationsManager();
			List<String> excludeIds = manager.getDocExlusions(collection);

			List<String> elevateIds = new ArrayList<>();
			List<DocElevation> docElevation = manager.getDocElevations(collection, query.getFreeTextQuery());
			for (DocElevation doc:docElevation) {
				if (doc.getId() != null && !excludeIds.contains(doc.getId())) {
					elevateIds.add(doc.getId());
				}
			}

			if(!excludeIds.isEmpty()) {
				params.add("excludeIds", StringUtils.join(excludeIds, ","));
			}

			if(!elevateIds.isEmpty()) {
				params.add("elevateIds", StringUtils.join(elevateIds, ","));
			}
		}

		if (query.isMoreLikeThis()) {
			params.add(CommonParams.Q, "id:" + query.getMoreLikeThisRecordId());
		} else if(addSynonyms && collection != null && query.getFreeTextQuery() != null) {
			params.add(CommonParams.Q, modelLayerFactory.getSynonymsConfigurationsManager().computeSynonyms(collection, query.getFreeTextQuery()));
		} else {
			params.add(CommonParams.Q, StringUtils.defaultString(query.getFreeTextQuery(), "*:*"));
		}

		return params;
	}

	public String getSortQuery(LogicalSearchQuery query) {
		StringBuilder stringBuilder = new StringBuilder();

		for (LogicalSearchQuerySort sort : query.getSortFields()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(", ");
			}

			if (sort instanceof FieldLogicalSearchQuerySort) {
				if (query.getLanguage() != null && !query.getLanguage()
						.equals(modelLayerFactory.getCollectionsListManager().getMainDataLanguage())) {
					DataStoreField dataStoreField = ((FieldLogicalSearchQuerySort) sort).getField();
					//Metadata may not be multilingual, fields of main data language.

					if (dataStoreField.getSortField() != null) {
						stringBuilder.append(sortFieldName(
								dataStoreField.getSortField().getSecondaryLanguageDataStoreCode(query.getLanguage())));
						stringBuilder.append(" ");
						stringBuilder.append(sort.isAscending() ? "asc" : "desc");
						stringBuilder.append(", ");
					}
					stringBuilder.append(sortFieldName(dataStoreField.getSecondaryLanguageDataStoreCode(query.getLanguage())));
					stringBuilder.append(" ");
					stringBuilder.append(sort.isAscending() ? "asc" : "desc");
					stringBuilder.append(", ");

					if (dataStoreField.getSortField() != null) {
						stringBuilder.append(sortFieldName(dataStoreField.getSortField().getDataStoreCode()));
						stringBuilder.append(" ");
						stringBuilder.append(sort.isAscending() ? "asc" : "desc");
						stringBuilder.append(", ");
					}
					stringBuilder.append(sortFieldName(dataStoreField.getDataStoreCode()));
					stringBuilder.append(" ");
					stringBuilder.append(sort.isAscending() ? "asc" : "desc");

				} else {
					DataStoreField dataStoreField = ((FieldLogicalSearchQuerySort) sort).getField();
					if (dataStoreField.getSortField() != null) {
						stringBuilder.append(sortFieldName(dataStoreField.getSortField().getDataStoreCode()));
						stringBuilder.append(" ");
						stringBuilder.append(sort.isAscending() ? "asc" : "desc");
						stringBuilder.append(", ");
					}
					stringBuilder.append(sortFieldName(dataStoreField.getDataStoreCode()));
					stringBuilder.append(" ");
					stringBuilder.append(sort.isAscending() ? "asc" : "desc");
				}

			} else if (sort instanceof FunctionLogicalSearchQuerySort) {
				String function = ((FunctionLogicalSearchQuerySort) sort).getFunction();
				stringBuilder.append(function);
				stringBuilder.append(" ");
				stringBuilder.append(sort.isAscending() ? "asc" : "desc");

			} else {
				throw new IllegalArgumentException("Unsupported sort : " + sort.getClass());
			}

		}

		return stringBuilder.toString();
	}

	private String sortFieldName(String fieldName) {
		if (fieldName != null && fieldName.endsWith("_s")) {
			return fieldName.substring(0, fieldName.length() - 2) + "_fs-s";
		}
		return fieldName;
	}

	/**
	 * FIXME With solr 6+, use mm autorelax instead
	 * @param userQuery
	 * @return
	 */
	private int calcMM(String userQuery) {
		HashSet queryTerms = new HashSet(Arrays.asList(StringUtils.split(StringUtils.lowerCase(userQuery))));
		queryTerms.removeAll(Arrays.asList(STOP_WORDS_FR));
		return queryTerms.size();
	}

	private String getQfFor(String language, List<SearchBoost> boosts,
			List<MetadataSchemaType> searchedSchemaTypes) {
		StringBuilder sb = new StringBuilder();

		Set<String> fields = new HashSet<>();

		for (SearchBoost boost : boosts) {
			sb.append(boost.getKey());
			sb.append("^");
			sb.append(boost.getValue());
			sb.append(" ");
			fields.add(boost.getKey());
		}

		for (MetadataSchemaType schemaType : searchedSchemaTypes) {
			for (Metadata metadata : schemaType.getAllMetadatas()) {
				if (metadata.isSearchable()) {
					if (metadata.hasSameCode(Schemas.LEGACY_ID)) {
						sb.append(Schemas.LEGACY_ID.getDataStoreCode());
						sb.append("^20 ");
					} else {
						String analyzedField = metadata.getAnalyzedField(metadata.isMultiLingual() ? language : mainDataLanguage)
								.getDataStoreCode();
						if (!fields.contains(analyzedField)) {
							sb.append(analyzedField + " ");
						}
					}
				}
			}
		}

		String idAnalyzedField = Schemas.IDENTIFIER.getAnalyzedField(mainDataLanguage).getDataStoreCode();
		if (!fields.contains(idAnalyzedField)) {
			sb.append(idAnalyzedField + " ");
		}
		return sb.toString();
	}

	private SPEQueryResponse buildResponse(ModifiableSolrParams params, LogicalSearchQuery query) {
		QueryResponseDTO queryResponseDTO = dataStoreDao(query.getDataStore()).query(query.getName(), params);
		List<RecordDTO> recordDTOs = queryResponseDTO.getResults();

		List<Record> records = recordServices.toRecords(recordDTOs, query.getReturnedMetadatas().isFullyLoaded());
		if (!records.isEmpty() && Toggle.PUTS_AFTER_SOLR_QUERY.isEnabled() && query.getReturnedMetadatas().isFullyLoaded()) {
			for (Map.Entry<String, List<Record>> entry : splitByCollection(records).entrySet()) {
				getConnectedRecordsCache().insert(entry.getKey(), entry.getValue(), WAS_OBTAINED);
			}

		}
		List<MoreLikeThisRecord> moreLikeThisResult = getResultWithMoreLikeThis(queryResponseDTO.getMoreLikeThisResults());

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

	private List<MoreLikeThisRecord> getResultWithMoreLikeThis(List<MoreLikeThisDTO> moreLikeThisResults) {
		List<MoreLikeThisRecord> moreLikeThisRecords = new ArrayList<>();

		for (MoreLikeThisDTO dto : moreLikeThisResults) {
			moreLikeThisRecords.add(new MoreLikeThisRecord(recordServices.toRecord(dto.getRecord(), true), dto.getScore()));
		}
		return moreLikeThisRecords;
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

	private void addUserFilter(ModifiableSolrParams params, List<UserFilter> userFilters) {
		if (userFilters == null) {
			return;
		}

		for (UserFilter userFilter : userFilters) {
			params.add(CommonParams.FQ, userFilter.buildFQ(securityTokenManager));
		}
	}

	//public Record getRecordWithId(MetadataSchemaType schemaType) {

	public List<Record> getAllRecords(MetadataSchemaType schemaType) {

		final RecordsCache cache = getConnectedRecordsCache().getCache(schemaType.getCollection());
		if (Toggle.GET_ALL_VALUES_USING_NEW_CACHE_METHOD.isEnabled()) {

			if (cache.isConfigured(schemaType)) {
				if (cache.isFullyLoaded(schemaType.getCode())) {
					return cache.getAllValues(schemaType.getCode());

				} else {

					List<Record> records = cachedSearch(new LogicalSearchQuery(from(schemaType).returnAll()));
					if (!Toggle.PUTS_AFTER_SOLR_QUERY.isEnabled()) {

						if (records.size() > 1000) {
							loadUsingMultithreading(cache, records);

						} else {
							cache.insert(records, WAS_OBTAINED);
						}
					}
					cache.markAsFullyLoaded(schemaType.getCode());

					return records;
				}
			} else {
				LOGGER.warn("getAllRecords should not be called on schema type '" + schemaType.getCode() + "'");
				return search(new LogicalSearchQuery(from(schemaType).returnAll()));
			}

		} else {
			List<Record> records = cachedSearch(new LogicalSearchQuery(from(schemaType).returnAll()));
			if (!Toggle.PUTS_AFTER_SOLR_QUERY.isEnabled()) {
				cache.insert(records, WAS_OBTAINED);
			}
			return records;
		}
	}

	public List<Record> getAllRecordsInUnmodifiableState(MetadataSchemaType schemaType) {

		final RecordsCache cache = getConnectedRecordsCache().getCache(schemaType.getCollection());
		if (Toggle.GET_ALL_VALUES_USING_NEW_CACHE_METHOD.isEnabled()) {

			if (cache.isConfigured(schemaType)) {
				if (cache.isFullyLoaded(schemaType.getCode())) {
					return cache.getAllValuesInUnmodifiableState(schemaType.getCode());

				} else {

					List<Record> records = cachedSearch(new LogicalSearchQuery(from(schemaType).returnAll()));
					if (!Toggle.PUTS_AFTER_SOLR_QUERY.isEnabled()) {

						if (records.size() > 1000) {
							loadUsingMultithreading(cache, records);

						} else {
							cache.insert(records, WAS_OBTAINED);
						}
					}
					cache.markAsFullyLoaded(schemaType.getCode());

					List<Record> unmodifiableRecords = new ArrayList<>();
					for (Record record : records) {
						unmodifiableRecords.add(record.getUnmodifiableCopyOfOriginalRecord());
					}

					return unmodifiableRecords;
				}
			} else {
				LOGGER.warn("getAllRecords should not be called on schema type '" + schemaType.getCode() + "'");
				return search(new LogicalSearchQuery(from(schemaType).returnAll()));
			}

		} else {
			List<Record> records = cachedSearch(new LogicalSearchQuery(from(schemaType).returnAll()));
			if (!Toggle.PUTS_AFTER_SOLR_QUERY.isEnabled()) {
				cache.insert(records, WAS_OBTAINED);
			}
			return records;
		}
	}

	private void loadUsingMultithreading(final RecordsCache cache, List<Record> records) {
		final Iterator<List<Record>> recordIterator = new BatchBuilderIterator<>(records.iterator(), 500);

		ThreadList threadList = new ThreadList<>();
		for (int i = 0; i < 5; i++) {
			threadList.addAndStart(new Thread() {
				@Override
				public void run() {
					boolean hasMoreRecords = true;

					while (hasMoreRecords) {

						List<Record> records;
						synchronized (recordIterator) {
							records = recordIterator.hasNext() ? recordIterator.next() : null;
						}
						if (records != null) {
							cache.insert(records, WAS_OBTAINED);
						} else {
							hasMoreRecords = false;
						}

					}
				}
			});
		}
		try {
			threadList.joinAll();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	RecordDao dataStoreDao(String dataStore) {
		if (dataStore == null || dataStore.equals(DataStore.RECORDS)) {
			return modelLayerFactory.getDataLayerFactory().newRecordDao();
		} else {
			return modelLayerFactory.getDataLayerFactory().newEventsDao();
		}
	}
}
