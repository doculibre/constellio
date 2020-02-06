package com.constellio.model.services.search;

import com.constellio.data.dao.dto.records.FacetPivotValue;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.dto.records.MoreLikeThisDTO;
import com.constellio.data.dao.dto.records.QueryResponseDTO;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.dao.services.Stats.CallStatCompiler;
import com.constellio.data.dao.services.bigVault.LazyResultsIterator;
import com.constellio.data.dao.services.bigVault.LazyResultsKeepingOrderIterator;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.data.utils.BatchBuilderSearchResponseIterator;
import com.constellio.data.utils.Holder;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LazyIterator;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.collections.CollectionsListManagerRuntimeException.CollectionsListManagerRuntimeException_NoSuchCollection;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordId;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCache2IntegrityDiagnosticService;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.QueryElevation.DocElevation;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.FieldLogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.FunctionLogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.UserFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.search.query.logical.ScoreLogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderContext;
import com.constellio.model.services.security.SecurityTokenManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.StatsParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.entities.schemas.Schemas.ESTIMATED_SIZE;
import static com.constellio.model.services.records.RecordUtils.splitByCollection;
import static com.constellio.model.services.search.VisibilityStatusFilter.ALL;
import static com.constellio.model.services.search.query.ReturnedMetadatasFilter.onlyFields;
import static com.constellio.model.services.search.query.ReturnedMetadatasFilter.onlySummaryFields;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.INEXISTENT_COLLECTION_42;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.startingWithText;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.DEFAULT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class SearchServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchServices.class);

	private static String[] STOP_WORDS_FR = {"au", "aux", "avec", "ce", "ces", "dans", "de", "des", "du", "elle", "en", "et",
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
											 "ils", "les", "leurs", "quel", "quels", "quelle", "quelles", "sans", "soi"};

	//RecordDao recordDao;
	RecordServices recordServices;
	SecurityTokenManager securityTokenManager;
	CollectionsListManager collectionsListManager;
	RecordsCaches disconnectableRecordsCaches;
	MetadataSchemasManager metadataSchemasManager;
	String mainDataLanguage;
	ConstellioEIMConfigs systemConfigs;
	ModelLayerFactory modelLayerFactory;
	LogicalSearchQueryExecutorInCache logicalSearchQueryExecutorInCache;

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
		this.logicalSearchQueryExecutorInCache = new LogicalSearchQueryExecutorInCache(this, recordsCaches,
				metadataSchemasManager,
				modelLayerFactory.getSearchConfigurationsManager(),
				modelLayerFactory.getExtensions().getSystemWideExtensions(), systemConfigs, mainDataLanguage);
	}

	public LogicalSearchQueryExecutorInCache getQueryExecutorInCache() {
		return logicalSearchQueryExecutorInCache;
	}

	public RecordsCaches getConnectedRecordsCache() {
		//		if (disconnectableRecordsCaches != null && (disconnectableRecordsCaches instanceof RecordsCachesRequestMemoryImpl)) {
		//			if (((RecordsCachesRequestMemoryImpl) disconnectableRecordsCaches).isDisconnected()) {
		//				disconnectableRecordsCaches = modelLayerFactory.getModelLayerFactoryFactory().get().getRecordsCaches();
		//			}
		//		}
		return disconnectableRecordsCaches;
	}

	public SPEQueryResponse query(LogicalSearchQuery query) {
		//		if (logicalSearchQueryExecutorInCache.isQueryExecutableInCache(query)) {
		//
		//
		//			if (Toggle.VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR.isEnabled()) {
		//				List<Record> records = searchUsingCache(new LogicalSearchQuery(query));
		//
		//				if (query.getSortFields() == null || query.getSortFields().isEmpty()) {
		//					Set<String> cacheRecordIds = records.stream().limit(query.getNumberOfRows())
		//							.map(Record::getId).collect(Collectors.toSet());
		//					Set<String> solrRecordIds = searchUsingSolr(new LogicalSearchQuery(query).setName("*SDK* Validate cache"))
		//							.stream().map(Record::getId).collect(Collectors.toSet());
		//
		//					if (!cacheRecordIds.equals(solrRecordIds)) {
		//						throw new RuntimeException("Cached query execution problem\nExpected : " + solrRecordIds
		//												   + "\nWas : " + cacheRecordIds);
		//					}
		//				} else {
		//					List<String> cacheRecordIds = records.stream().limit(query.getNumberOfRows())
		//							.map(Record::getId).collect(Collectors.toList());
		//					List<String> solrRecordIds = searchUsingSolr(new LogicalSearchQuery(query).setName("*SDK* Validate cache"))
		//							.stream().map(Record::getId).collect(Collectors.toList());
		//
		//					if (!cacheRecordIds.equals(solrRecordIds)) {
		//						throw new RuntimeException("Cached query execution problem\nExpected : " + solrRecordIds
		//												   + "\nWas : " + cacheRecordIds);
		//					}
		//				}
		//
		//			}
		//
		//			List<Record> records = searchUsingCache(new LogicalSearchQuery(query).setNumberOfRows(1_000_000));
		//
		//			int to = Math.min(query.getStartRow() + query.getNumberOfRows(), records.size());
		//			return new SPEQueryResponse(records.subList(query.getStartRow(), to), records.size());
		//
		//		} else {
		return buildResponse(query);
		//		}


	}

	@Deprecated
	public List<Record> cachedSearch(LogicalSearchQuery query) {
		if (logicalSearchQueryExecutorInCache.isQueryExecutableInCache(query)) {
			List<Record> records = searchUsingCache(query);

			if (Toggle.VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR.isEnabled()) {


				if (query.getSortFields() == null || query.getSortFields().isEmpty()) {
					Set<String> cacheRecordIds = records.stream().limit(query.getNumberOfRows())
							.map(Record::getId).collect(Collectors.toSet());
					Set<String> solrRecordIds = searchUsingSolr(new LogicalSearchQuery(query).setName("*SDK* Validate cache"))
							.stream().map(Record::getId).collect(Collectors.toSet());

					if (!cacheRecordIds.equals(solrRecordIds)) {
						throw new RuntimeException("Cached query execution problem\nExpected : " + solrRecordIds
												   + "\nWas : " + cacheRecordIds);
					}
				} else {
					List<String> cacheRecordIds = records.stream().limit(query.getNumberOfRows())
							.map(Record::getId).collect(Collectors.toList());
					List<String> solrRecordIds = searchUsingSolr(new LogicalSearchQuery(query).setName("*SDK* Validate cache"))
							.stream().map(Record::getId).collect(Collectors.toList());

					if (!cacheRecordIds.equals(solrRecordIds)) {
						throw new RuntimeException("Cached query execution problem\nExpected : " + solrRecordIds
												   + "\nWas : " + cacheRecordIds);
					}
				}

			}

			return records;
		} else {
			return searchUsingSolr(query);
		}
	}

	private List<Record> searchUsingCache(LogicalSearchQuery query) {

		try {
			if (!query.getCacheableQueries().isEmpty() &&
				query.getCacheableQueries().stream().allMatch(logicalSearchQueryExecutorInCache::isQueryExecutableInCache)) {

				List<Record> records = new ArrayList<>();
				for (LogicalSearchQuery cacheableQuery : query.getCacheableQueries()) {
					records.addAll(logicalSearchQueryExecutorInCache.stream(cacheableQuery).collect(Collectors.toList()));
				}

				return records;

				//				return query.getCacheableQueries().stream()
				//						.map(query1 -> logicalSearchQueryExecutorInCache.stream(query1))
				//						.flatMap(Function.identity())
				//						//.sorted(query.getSortFields()) // FIXME sort or not?
				//						.collect(Collectors.toList());
			} else if (logicalSearchQueryExecutorInCache.isQueryExecutableInCache(query)) {
				return retrieveRecordsUsingCache(query);
			} else {
				return searchUsingSolr(query);
			}

		} catch (LogicalSearchQueryExecutionCancelledException e) {
			return searchUsingSolr(new LogicalSearchQuery(query)
					.setName("Query cancelled by cache : " + e.getMessage() + (query.getName() == null ? "" : " : " + query.getName())));
		}
	}

	private List<Record> searchUsingSolr(LogicalSearchQuery query) {
		return query(query).getRecords();
	}

	private List<Record> retrieveRecordsUsingCache(LogicalSearchQuery query)
			throws LogicalSearchQueryExecutionCancelledException {
		Stream<Record> stream = logicalSearchQueryExecutorInCache.stream(query);
		List<Record> records = stream.collect(Collectors.toList());
		stream.close();
		return records;
	}

	@Deprecated
	public List<String> cachedSearchRecordIds(LogicalSearchQuery query) {
		return searchRecordIds(query);
	}

	public List<MoreLikeThisRecord> searchWithMoreLikeThis(LogicalSearchQuery query) {
		return query(query).getMoreLikeThisRecords();
	}

	public Stream<Record> stream(MetadataSchemaType schemaType, boolean summary) {
		return streamFromSolr(schemaType, summary);
	}

	public int getMaxRecordSize(MetadataSchemaType schemaType) {
		LogicalSearchQuery maxSizeQuery = new LogicalSearchQuery(from(schemaType).returnAll());
		maxSizeQuery.sortDesc(ESTIMATED_SIZE);
		maxSizeQuery.setNumberOfRows(1);
		maxSizeQuery.filteredByVisibilityStatus(ALL);
		maxSizeQuery.filteredByStatus(StatusFilter.ALL);
		maxSizeQuery.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(ESTIMATED_SIZE));

		List<Record> records = search(maxSizeQuery);
		if (records.isEmpty()) {
			return 1_000_000;
		} else {
			Record record = records.get(0);
			Integer value = record.get(ESTIMATED_SIZE);
			return value == null ? 1_000_000 : value;
		}
	}

	public int getIdealBatchSize(MetadataSchemaType schemaType) {
		return 100_000_000 / getMaxRecordSize(schemaType);
	}

	public Stream<Record> streamFromSolr(MetadataSchemaType schemaType, boolean summary) {
		return streamFromSolr(schemaType, summary, null);
	}

	public Stream<Record> streamFromSolr(MetadataSchemaType schemaType, boolean summary, String streamName) {

		LogicalSearchQuery maxSizeQuery = new LogicalSearchQuery(from(schemaType).returnAll());
		maxSizeQuery.sortDesc(ESTIMATED_SIZE);
		maxSizeQuery.setNumberOfRows(1);
		maxSizeQuery.filteredByVisibilityStatus(ALL);
		maxSizeQuery.filteredByStatus(StatusFilter.ALL);
		maxSizeQuery.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(ESTIMATED_SIZE));
		maxSizeQuery.setName(streamName);
		//maxSizeQuery.computeStatsOnField(ESTIMATED_SIZE);

		QueryResponseDTO queryResponseDTO = queryDao(maxSizeQuery);
		if (queryResponseDTO.getResults().isEmpty()) {
			return Stream.empty();
		}

		Number maxRecordSize = (Number) queryResponseDTO.getResults().get(0).getFields().get(ESTIMATED_SIZE.getDataStoreCode());

		int batchSize;

		batchSize = (maxRecordSize == null || maxRecordSize.intValue() == 0) ? 1000 : (100_000_1000 / maxRecordSize.intValue());
		LOGGER.info("Streaming schema type '" + schemaType.getCode() + "' with batches of " + batchSize + ". Max record size is " + maxRecordSize);

		if (summary && queryResponseDTO.getNumFound() > 1_000_000) {
			batchSize = Math.max(batchSize, 20_000);

		} else if (summary && queryResponseDTO.getNumFound() > 100_000) {
			batchSize = Math.max(batchSize, 2_000);

		}

		if (summary && queryResponseDTO.getNumFound() > 1_000_000) {
			batchSize = Math.max(batchSize, 20_000);

		} else if (summary && queryResponseDTO.getNumFound() > 100_000) {
			batchSize = Math.max(batchSize, 2_000);

		}

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(schemaType).returnAll());
		query.filteredByVisibilityStatus(ALL);
		query.filteredByStatus(StatusFilter.ALL);

		if (summary) {
			query.setReturnedMetadatas(onlyFields(schemaType.getSummaryMetadatasDataStoreCodes()));
		}

		return streamFromSolr(query, batchSize);

	}

	@AllArgsConstructor
	public static class RecordIdVersion {

		@Getter
		RecordId recordId;

		@Getter
		long version;
	}


	public Stream<Record> stream(LogicalSearchQuery query) {
		return stream(query, 1000);
	}

	public Stream<Record> stream(LogicalSearchQuery query, int batchSize) {
		return cachedStream(query, batchSize);
	}

	public Stream<Record> cachedStream(LogicalSearchQuery query) {
		return cachedStream(query, 1000);
	}

	public Stream<Record> cachedStream(LogicalSearchQuery query, int batchSize) {

		if (logicalSearchQueryExecutorInCache.isQueryExecutableInCache(query)) {

			try {
				if (Toggle.VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR.isEnabled()) {

					if (query.getSortFields() == null || query.getSortFields().isEmpty()) {
						Set<String> recordsFromCacheStream = logicalSearchQueryExecutorInCache.stream(query)
								.map(Record::getId).collect(Collectors.toSet());
						Set<String> recordsFromSolrStream = streamFromSolr(new LogicalSearchQuery(query).setName("*SDK* Validate cache"))
								.map(Record::getId).collect(Collectors.toSet());

						if (!recordsFromCacheStream.equals(recordsFromSolrStream)) {
							throw new RuntimeException("Cached query execution problem\nExpected : " + recordsFromSolrStream
													   + "\nWas : " + recordsFromCacheStream);
						}
					} else {
						List<String> recordsFromCacheStream = logicalSearchQueryExecutorInCache.stream(query)
								.map(Record::getId).collect(Collectors.toList());
						List<String> recordsFromSolrStream = streamFromSolr(new LogicalSearchQuery(query).setName("*SDK* Validate cache"))
								.map(Record::getId).collect(Collectors.toList());

						if (!recordsFromCacheStream.equals(recordsFromSolrStream)) {
							throw new RuntimeException("Cached query execution problem\nExpected : " + recordsFromSolrStream
													   + "\nWas : " + recordsFromCacheStream);
						}
					}

					Stream<Record> cacheStream = logicalSearchQueryExecutorInCache.stream(query);
					Stream<Record> solrStream = streamFromSolr(new LogicalSearchQuery(query).setName("*SDK* Validate cache"));
					return new StreamValidator<>(solrStream, cacheStream, !query.getSortFields().isEmpty());

				} else {
					return logicalSearchQueryExecutorInCache.stream(query);
				}
			} catch (LogicalSearchQueryExecutionCancelledException e) {
				return streamFromSolr(new LogicalSearchQuery(query)
						.setName("Query cancelled by cache : " + e.getMessage() + (query.getName() == null ? "" : " : " + query.getName())), batchSize);
			}

		} else {
			return streamFromSolr(query, batchSize);
		}
	}

	public Stream<Record> streamFromSolr(LogicalSearchQuery query) {
		return streamFromSolr(query, 1000);
	}

	public Stream<Record> streamFromSolr(LogicalSearchQuery query, int batchSize) {

		final LogicalSearchQuery clonedQuery = new LogicalSearchQuery(query);
		//return search(query).stream();

		Stream<Record> stream = StreamSupport.stream(new Supplier<Spliterator<Record>>() {
			@Override
			public Spliterator<Record> get() {
				SearchResponseIterator<Record> iterator = getRecordSearchResponseIteratorUsingSolr(clonedQuery, batchSize, true);
				return Spliterators.spliteratorUnknownSize(iterator, 0);
			}
		}, 0, false);

		return new StreamAdaptor<Record>(stream) {

			boolean parallel = false;

			@Override
			public boolean isParallel() {
				return parallel;
			}

			@NotNull
			@Override
			public Stream<Record> parallel() {
				parallel = true;
				return this;
			}

			@Override
			public void forEach(Consumer<? super Record> action) {

				if (parallel) {
					final LinkedBlockingQueue<Holder<Record>> queue = new LinkedBlockingQueue<>(batchSize * 3);
					final CallStatCompiler statCompiler = Stats.getCurrentStatCompiler();

					Runnable runnable = () -> {
						try {

							super.forEach((r) -> {
								try {
									queue.put(new Holder(r));
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}
							});
						} finally {
							try {
								queue.put(new Holder(null));
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
					};

					new Thread(statCompiler == null ? runnable : () -> statCompiler.log(runnable)).start();

					Record nextRecord;
					try {
						while ((nextRecord = queue.take().get()) != null) {
							action.accept(nextRecord);
						}
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				} else {
					super.forEach(action);
				}
			}

			@Override
			public Stream<Record> filter(Predicate<? super Record> predicate) {
				LogicalSearchCondition condition = (LogicalSearchCondition) predicate;
				if (clonedQuery.getCondition() == null) {
					clonedQuery.setCondition(null);
				} else {
					clonedQuery.setCondition(LogicalSearchQueryOperators.allConditions(clonedQuery.getCondition(), condition));
				}
				return this;
			}

			@NotNull
			@Override
			public Optional<Record> findFirst() {
				List<Record> records = search(clonedQuery.setNumberOfRows(1));
				return Optional.ofNullable(records.isEmpty() ? null : records.get(0));
			}

			@NotNull
			@Override
			public Optional<Record> findAny() {
				return findFirst();
			}

			@Override
			public boolean anyMatch(Predicate<? super Record> predicate) {
				filter(predicate);
				return count() > 1;
			}

			@Override
			public boolean allMatch(Predicate<? super Record> predicate) {
				LogicalSearchCondition condition = (LogicalSearchCondition) predicate;
				String facet = condition.getSolrQuery(newSolrQueryBuilderContext(clonedQuery));
				clonedQuery.addQueryFacet("filterMatch", facet);
				clonedQuery.setNumberOfRows(0);
				SPEQueryResponse response = query(clonedQuery);
				return response.getNumFound() == 0 || response.getNumFound() == response.getQueryFacetCount(facet);

			}

			@Override
			public boolean noneMatch(Predicate<? super Record> predicate) {
				filter(predicate);
				return count() == 0;
			}

			@Override
			public Stream<Record> limit(long maxSize) {
				clonedQuery.setNumberOfRows((int) maxSize);
				return this;
			}

			@Override
			public Stream<Record> skip(long n) {
				clonedQuery.setStartRow(clonedQuery.getStartRow() + (int) n);
				return this;
			}

			@Override
			public long count() {
				return getResultCountUsingSolr(clonedQuery);
			}

			@Override
			public Stream<Record> distinct() {
				//Always distinct when loaded from solr, no need to iterate over the stream
				return this;
			}

			@Override
			public Record reduce(Record identity, BinaryOperator<Record> accumulator) {
				throw new UnsupportedOperationException("Unsupported operation");
			}

			@NotNull
			@Override
			public Optional<Record> reduce(BinaryOperator<Record> accumulator) {
				throw new UnsupportedOperationException("Unsupported operation");
			}

			@Override
			public <U> U reduce(U identity, BiFunction<U, ? super Record, U> accumulator,
								BinaryOperator<U> combiner) {
				throw new UnsupportedOperationException("Unsupported operation");
			}

			@NotNull
			@Override
			public Stream<Record> unordered() {
				clonedQuery.clearSort();
				return this;
			}

			@Override
			public Stream<Record> sorted() {
				clonedQuery.clearSort();
				clonedQuery.sortAsc(Schemas.IDENTIFIER);
				return this;
			}


			@Override
			public Stream<Record> sorted(Comparator<? super Record> comparator) {
				if (comparator instanceof SolrFieldsComparator) {
					clonedQuery.clearSort();
					for (int i = 0; i < ((SolrFieldsComparator<? super Record>) comparator).fields.size(); i++) {

						DataStoreField field = ((SolrFieldsComparator<? super Record>) comparator).fields.get(i);
						boolean direction = ((SolrFieldsComparator<? super Record>) comparator).directions.get(i);
						if (direction) {
							clonedQuery.sortAsc(field);
						} else {
							clonedQuery.sortDesc(field);
						}
					}

					return this;

				} else {
					throw new IllegalArgumentException("Comparator must be of type SolrFieldsComparator");
				}


			}

			@NotNull
			@Override
			public Optional<Record> min(Comparator<? super Record> comparator) {
				if (comparator instanceof SolrFieldsComparator) {
					clonedQuery.clearSort();
					for (int i = 0; i < ((SolrFieldsComparator<? super Record>) comparator).fields.size(); i++) {

						DataStoreField field = ((SolrFieldsComparator<? super Record>) comparator).fields.get(i);
						boolean direction = ((SolrFieldsComparator<? super Record>) comparator).directions.get(i);
						if (direction) {
							clonedQuery.sortAsc(field);
						} else {
							clonedQuery.sortDesc(field);
						}
					}
					clonedQuery.setNumberOfRows(1);

					List<Record> records = search(clonedQuery);
					return Optional.ofNullable(records.isEmpty() ? null : records.get(0));

				} else {
					throw new IllegalArgumentException("Comparator must be of type SolrFieldsComparator");
				}
			}

			@NotNull
			@Override
			public Optional<Record> max(Comparator<? super Record> comparator) {
				if (comparator instanceof SolrFieldsComparator) {
					clonedQuery.clearSort();
					for (int i = 0; i < ((SolrFieldsComparator<? super Record>) comparator).fields.size(); i++) {

						DataStoreField field = ((SolrFieldsComparator<? super Record>) comparator).fields.get(i);
						boolean direction = ((SolrFieldsComparator<? super Record>) comparator).directions.get(i);
						if (!direction) {
							clonedQuery.sortAsc(field);
						} else {
							clonedQuery.sortDesc(field);
						}
					}
					clonedQuery.setNumberOfRows(1);
					List<Record> records = search(clonedQuery);
					return Optional.ofNullable(records.isEmpty() ? null : records.get(0));

				} else {
					throw new IllegalArgumentException("Comparator must be of type SolrFieldsComparator");
				}
			}
		};


	}

	public List<Record> search(LogicalSearchQuery query) {
		//return searchUsingSolr(query);
		return cachedSearch(query);
	}


	public Record searchSingleResult(LogicalSearchCondition condition) {

		try {
			if (logicalSearchQueryExecutorInCache.isConditionExecutableInCache(condition, DEFAULT)) {
				Record record = searchSingleResultUsingCache(condition);

				if (Toggle.VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR.isEnabled()) {
					Record recordFromSolr = searchSingleResultUsingSolr(condition);

					String recordId = record == null ? null : record.getId();
					String recordFromSolrId = recordFromSolr == null ? null : recordFromSolr.getId();

					if (!LangUtils.isEqual(recordId, recordFromSolrId)) {
						throw new RuntimeException("Cached query execution problem");
					}


				}

				return record;

			} else if (logicalSearchQueryExecutorInCache.isConditionExecutableInCache(condition, onlySummaryFields(), DEFAULT)) {
				Record recordSummary = searchSingleResultUsingCache(condition);

				if (Toggle.VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR.isEnabled()) {
					Record recordFromSolr = searchSingleResultUsingSolr(condition);

					String recordId = recordSummary == null ? null : recordSummary.getId();
					String recordFromSolrId = recordFromSolr == null ? null : recordFromSolr.getId();

					if (!LangUtils.isEqual(recordId, recordFromSolrId)) {
						throw new RuntimeException("Cached query execution problem");
					}

				}

				return recordSummary == null ? null : recordServices.getDocumentById(recordSummary.getId());

			} else {
				return searchSingleResultUsingSolr(condition);
			}
		} catch (LogicalSearchQueryExecutionCancelledException ignored) {
			return searchSingleResultUsingSolr(condition);
		}
	}

	@Nullable
	private Record searchSingleResultUsingSolr(LogicalSearchCondition condition) {
		SPEQueryResponse response = query(new LogicalSearchQuery(condition).filteredByVisibilityStatus(ALL).setNumberOfRows(1));
		if (response.getNumFound() > 1) {
			SolrQueryBuilderContext params = new SolrQueryBuilderContext(false, new ArrayList<>(), "?", null, null, null) {
			};
			throw new SearchServicesRuntimeException.TooManyRecordsInSingleSearchResult(condition.getSolrQuery(params));
		}
		return response.getNumFound() == 1 ? response.getRecords().get(0) : null;
	}

	@Nullable
	private Record searchSingleResultUsingCache(LogicalSearchCondition condition)
			throws LogicalSearchQueryExecutionCancelledException {
		List<Record> records = logicalSearchQueryExecutorInCache.stream(condition).limit(2).collect(Collectors.toList());
		if (records.size() > 1) {
			SolrQueryBuilderContext params = new SolrQueryBuilderContext(false, new ArrayList<>(), "?", null, null, null) {
			};
			throw new SearchServicesRuntimeException.TooManyRecordsInSingleSearchResult(condition.getSolrQuery(params));
		}
		return records.size() == 1 ? records.get(0) : null;
	}

	public Iterator<List<Record>> recordsBatchIterator(int batch, LogicalSearchQuery query) {
		Iterator<Record> recordsIterator = recordsIterator(query, batch);
		return new BatchBuilderIterator<>(recordsIterator, batch);
	}

	public Iterator<List<Record>> recordsBatchIterator(LogicalSearchQuery query) {
		return recordsBatchIterator(100, query);
	}

	public SearchResponseIterator<Record> recordsIterator(LogicalSearchCondition condition) {
		return recordsIterator(new LogicalSearchQuery(condition).filteredByVisibilityStatus(ALL));
	}

	public SearchResponseIterator<Record> recordsIterator(LogicalSearchCondition condition, int batchSize) {
		return recordsIterator(new LogicalSearchQuery(condition).filteredByVisibilityStatus(ALL), batchSize);
	}

	public SearchResponseIterator<Record> recordsIterator(LogicalSearchQuery query) {
		return recordsIterator(query, 100);
	}

	private SearchResponseIterator<Record> streamToSearchResponseIterator(Stream<Record> recordStream, int batchSize) {
		List<Record> records = recordStream.collect(Collectors.toList());
		Iterator<Record> recordsIterator = records.iterator();
		return new SearchResponseIterator<Record>() {
			@Override
			public long getNumFound() {
				return records.size();
			}

			@Override
			public SearchResponseIterator<List<Record>> inBatches() {
				return new BatchBuilderSearchResponseIterator(recordsIterator, batchSize) {
					@Override
					public long getNumFound() {
						return records.size();
					}
				};
			}

			@Override
			public boolean hasNext() {
				return recordsIterator.hasNext();
			}

			@Override
			public Record next() {
				return recordsIterator.next();
			}
		};

	}

	public SearchResponseIterator<Record> recordsIterator(LogicalSearchQuery query, int batchSize) {

		if (logicalSearchQueryExecutorInCache.isQueryExecutableInCache(query)) {
			return streamToSearchResponseIterator(stream(query), batchSize);

		} else {
			ModifiableSolrParams params = addSolrModifiableParams(query);
			final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
			return new LazyResultsIterator<Record>(dataStoreDao(query.getDataStore()), params, batchSize, true, query.getName()) {

				@Override
				public Record convert(RecordDTO recordDTO) {
					return recordServices.toRecord(recordDTO, fullyLoaded);
				}
			};
		}
	}

	public Iterator<List<Record>> reverseRecordsBatchIterator(int batch, LogicalSearchQuery query) {
		Iterator<Record> recordsIterator = reverseRecordsIterator(query, batch);
		return new BatchBuilderIterator<>(recordsIterator, batch);
	}

	public Iterator<List<Record>> reverseRecordsBatchIterator(LogicalSearchQuery query) {
		return reverseRecordsBatchIterator(100, query);
	}

	public SearchResponseIterator<Record> reverseRecordsIterator(LogicalSearchCondition condition) {
		return reverseRecordsIterator(new LogicalSearchQuery(condition).filteredByVisibilityStatus(ALL));
	}

	public SearchResponseIterator<Record> reverseRecordsIterator(LogicalSearchCondition condition, int batchSize) {
		return reverseRecordsIterator(new LogicalSearchQuery(condition).filteredByVisibilityStatus(ALL), batchSize);
	}

	public SearchResponseIterator<Record> reverseRecordsIterator(LogicalSearchQuery query) {
		return reverseRecordsIterator(query, 100);
	}

	public SearchResponseIterator<Record> reverseRecordsIterator(LogicalSearchQuery query, int batchSize) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		return new LazyResultsIterator<Record>(dataStoreDao(query.getDataStore()), params, batchSize, false, query.getName()) {

			@Override
			public Record convert(RecordDTO recordDTO) {
				return recordServices.toRecord(recordDTO, fullyLoaded);
			}
		};
	}

	public SearchResponseIterator<Record> recordsIteratorKeepingOrder(LogicalSearchQuery query, int batchSize) {
		if (logicalSearchQueryExecutorInCache.isQueryExecutableInCache(query)) {
			return streamToSearchResponseIterator(stream(query), batchSize);

		} else {
			return getRecordSearchResponseIteratorUsingSolr(query, batchSize, true);
		}
	}

	@NotNull
	private SearchResponseIterator<Record> getRecordSearchResponseIteratorUsingSolr(LogicalSearchQuery query,
																					int batchSize, boolean keepOrder) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		if (keepOrder && !query.getSortFields().isEmpty()) {
			return new LazyResultsKeepingOrderIterator<Record>(dataStoreDao(query.getDataStore()), params, batchSize) {

				@Override
				public Record convert(RecordDTO recordDTO) {
					return recordServices.toRecord(recordDTO, fullyLoaded);
				}
			};
		} else {
			return new LazyResultsIterator<Record>(dataStoreDao(query.getDataStore()), params, batchSize, true, query.getName()) {
				@Override
				public Record convert(RecordDTO recordDTO) {
					return recordServices.toRecord(recordDTO, fullyLoaded);
				}

				;
			};
		}
	}

	public SearchResponseIterator<Record> recordsIteratorKeepingOrder(LogicalSearchQuery query, int batchSize,
																	  int skipping) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		final boolean fullyLoaded = query.getReturnedMetadatas().isFullyLoaded();
		return new LazyResultsKeepingOrderIterator<Record>(dataStoreDao(query.getDataStore()), params, batchSize, skipping) {

			@Override
			public Record convert(RecordDTO recordDTO) {
				return recordServices.toRecord(recordDTO, fullyLoaded);
			}
		};
	}

	public SearchResponseIterator<Record> cachedRecordsIteratorKeepingOrder(LogicalSearchQuery query,
																			final int batchSize) {
		LogicalSearchQuery querCompatibleWithCache = new LogicalSearchQuery(query);
		querCompatibleWithCache.setStartRow(0);
		querCompatibleWithCache.setNumberOfRows(100000);
		querCompatibleWithCache.setReturnedMetadatas(ReturnedMetadatasFilter.all());

		final List<Record> records = cachedSearch(querCompatibleWithCache);

		final Iterator<Record> nestedIterator = records.iterator();
		return new SearchResponseIterator<Record>() {
			@Override
			public long getNumFound() {
				return records.size();
			}

			@Override
			public SearchResponseIterator<List<Record>> inBatches() {
				final SearchResponseIterator iterator = this;
				return new BatchBuilderSearchResponseIterator<Record>(this, batchSize) {

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
	}

	public long getResultsCount(LogicalSearchCondition condition) {
		return getResultsCount(new LogicalSearchQuery(condition).filteredByVisibilityStatus(ALL));
	}

	public long getResultsCount(LogicalSearchQuery query) {
		LogicalSearchQuery clonedQuery = new LogicalSearchQuery(query);
		clonedQuery.setReturnedMetadatas(ReturnedMetadatasFilter.onlySummaryFields());
		clonedQuery.clearSort();
		try {
			if (logicalSearchQueryExecutorInCache.isQueryExecutableInCache(clonedQuery)) {
				Stream<Record> stream = logicalSearchQueryExecutorInCache.stream(clonedQuery);
				long count = stream.count();
				stream.close();

				if (Toggle.VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR.isEnabled()) {
					long countFromSolr = getResultCountUsingSolr(new LogicalSearchQuery(clonedQuery).setName("*SDK* Validate cache"));

					if (count != countFromSolr) {
						checkForCacheProblems();
						throw new RuntimeException("Cached query execution problem");
					}


				}

				return count;

			} else {
				return getResultCountUsingSolr(clonedQuery);
			}
		} catch (LogicalSearchQueryExecutionCancelledException e) {
			return getResultCountUsingSolr(new LogicalSearchQuery(clonedQuery)
					.setName("Query cancelled by cache : " + e.getMessage() + (query.getName() == null ? "" : " : " + query.getName())));
		}
	}


	private void checkForCacheProblems() {
		RecordsCache2IntegrityDiagnosticService service = new RecordsCache2IntegrityDiagnosticService(modelLayerFactory);
		ValidationErrors errors = service.validateIntegrity(false, true);
		try {
			errors.throwIfNonEmpty();
		} catch (ValidationException e) {
			throw new RuntimeException(e);
		}
	}

	public long getResultCountUsingSolr(LogicalSearchQuery query) {
		int oldNumberOfRows = query.getNumberOfRows();
		query.setNumberOfRows(0);
		ModifiableSolrParams params = addSolrModifiableParams(query);
		RecordDao recordDao = dataStoreDao(query.getDataStore());
		long result = recordDao == null ? 0 : recordDao.query(query.getName(), params).getNumFound();
		query.setNumberOfRows(oldNumberOfRows);
		return result;
	}

	public List<String> searchRecordIds(LogicalSearchCondition condition) {
		LogicalSearchQuery query = new LogicalSearchQuery(condition).filteredByVisibilityStatus(ALL);
		return searchRecordIds(query);
	}

	public List<String> searchRecordIds(LogicalSearchQuery query) {
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		if (logicalSearchQueryExecutorInCache.isQueryExecutableInCache(query)) {
			return stream(query, 10000).map(Record::getId).collect(Collectors.toList());

		} else {

			List<String> ids = new ArrayList<>();
			for (Record record : buildResponse(query).getRecords()) {
				ids.add(record.getId());
			}
			return ids;

		}
	}

	public Iterator<String> recordsIdsIterator(LogicalSearchQuery query) {
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		if (logicalSearchQueryExecutorInCache.isQueryExecutableInCache(query)) {
			return stream(query).map(Record::getId).iterator();

		} else {
			return recordsIdsIteratorUsingSolr(query);
		}

	}

	@NotNull
	private Iterator<String> recordsIdsIteratorUsingSolr(LogicalSearchQuery query) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		return new LazyResultsIterator<String>(dataStoreDao(query.getDataStore()), params, 10000, true, query.getName()) {

			@Override
			public String convert(RecordDTO recordDTO) {
				return recordDTO.getId();
			}
		};
	}


	public Iterator<RecordIdVersion> recordsIdVersionIteratorUsingSolr(MetadataSchemaType schemaType) {
		if (systemConfigs.isRunningWithSolr6() && modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration()
				.useSolrTupleStreamsIfSupported()) {
			return recordsIdVersionIteratorUsingSolrTupleStream(schemaType, null);

		} else {
			return recordsIdVersionIteratorUsingSolrIterator(schemaType, null);
		}
	}


	public Iterator<RecordIdVersion> recordsIdVersionIteratorUsingSolrIterator(MetadataSchemaType schemaType,
																			   Metadata sort) {
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemaType).returnAll());
		if (sort != null) {
			query.sortAsc(sort);
		}
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		query.filteredByVisibilityStatus(ALL);
		query.filteredByStatus(StatusFilter.ALL);
		final Iterator<Record> iterator = sort == null ? recordsIterator(query, 5000) : recordsIteratorKeepingOrder(query, 5000);

		return new LazyIterator<RecordIdVersion>() {
			@Override
			protected RecordIdVersion getNextOrNull() {
				if (iterator.hasNext()) {
					Record record = iterator.next();
					return new RecordIdVersion(record.getRecordId(), record.getVersion());

				} else {
					return null;
				}
			}
		};
	}


	public Iterator<RecordIdVersion> recordsIdVersionIteratorUsingSolrTupleStream(MetadataSchemaType schemaType,
																				  Metadata sort) {

		Map<String, String> props = new HashMap<>();
		props.put("q", "schema_s:" + schemaType.getCode() + "_*");
		props.put("fq", "collection_s:" + schemaType.getCollection());
		//props.put("qt", "/export");
		if (sort == null) {
			props.put("sort", "id asc");
			props.put("fl", "id, _version_");
		} else {
			props.put("sort", sort.getDataStoreCode() + " asc");
			props.put("fl", "id, _version_," + sort.getDataStoreCode());
		}

		props.put("rows", "100000000");

		TupleStream tupleStream = dataStoreDao(DataStore.RECORDS).tupleStream(props);

		try {
			tupleStream.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		AtomicInteger count = new AtomicInteger();
		return new LazyIterator<RecordIdVersion>() {

			@Override
			protected RecordIdVersion getNextOrNull() {

				try {

					Tuple tuple = tupleStream.read();
					if (tuple.EOF) {
						LOGGER.info("Fetching ids and versions of schema type '" + schemaType.getCollection() + ":" + schemaType.getCode() + "' using tuple stream method finished : " + count.get());
						tupleStream.close();
						return null;
					} else {
						//LOGGER.info("Fetching ids and versions of schema type '" + schemaType.getCollection() + ":" + schemaType.getCode() + "' using tuple stream method ... : " + count.get());
						count.incrementAndGet();
						RecordId id = RecordId.toId(tuple.getString("id"));
						long version = tuple.getLong("_version_");
						return new RecordIdVersion(id, version);
					}
				} catch (IOException e) {
					try {
						tupleStream.close();
					} catch (IOException e1) {
						throw new RuntimeException(e1);
					}
					throw new RuntimeException(e);
				}
			}
		};


	}

	public Iterator<String> reverseRecordsIdsIterator(LogicalSearchQuery query) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		return new LazyResultsIterator<String>(dataStoreDao(query.getDataStore()), params, 10000, false, query.getName()) {

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

	public List<String> getLanguages(LogicalSearchQuery query) {
		if (query.getLanguage() != null) {
			return getLanguageCodes(query.getCondition().getCollection());

		} else if (query.getCondition().isCollectionSearch()) {
			return getLanguageCodes(query.getCondition().getCollection());

		} else {
			return singletonList(mainDataLanguage);
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

	public List<String> getLanguageCodes(String collection) {
		List<String> languages = new ArrayList<>();
		try {
			List<String> languageCodes = INEXISTENT_COLLECTION_42.equals(collection) ? singletonList(mainDataLanguage) :
										 collectionsListManager.getCollectionLanguages(collection);
			if (languageCodes == null || languageCodes.size() == 0) {
				languages = singletonList(mainDataLanguage);
			} else {
				languages = Collections.unmodifiableList(languageCodes);
			}
		} catch (CollectionsListManagerRuntimeException_NoSuchCollection e) {
			languages = singletonList(mainDataLanguage);
		}
		return languages;
	}

	@Deprecated
	public String getLanguageCode(String collection) {
		String language;
		try {
			List<String> languageCodes = collectionsListManager.getCollectionLanguages(collection);
			if (languageCodes == null || languageCodes.size() == 0) {
				language = mainDataLanguage;
			} else {
				language = languageCodes.get(0);
			}
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

		SolrQueryBuilderContext ctx = newSolrQueryBuilderContext(query);

		params.add(CommonParams.FQ, "" + query.getCondition().getSolrQuery(ctx));

		if (DataStore.RECORDS.equals(query.getDataStore()) || query.getDataStore() == null) {
			if (query.isMoreLikeThis()) {
				params.add(CommonParams.QT, "/mlt");
			} else {
				params.add(CommonParams.QT, "/spell");
				params.add(ShardParams.SHARDS_QT, "/spell");

			}
		}
		if (query.getFreeTextQuery() != null) {
			User user = null;
			if (query.getUserFilters() != null && query.getUserFilters().size() > 0) {
				user = query.getUserFilters().get(0).getUser();
			}
			String qf = getQfFor(ctx.getLanguages(), query.getLanguage(), query.getFieldBoosts(), ctx.getSearchedSchemaTypes(), user);
			params.add(DisMaxParams.QF, qf);
			params.add(DisMaxParams.PF, qf);
			if (systemConfigs.isReplaceSpacesInSimpleSearchForAnds()) {
				int mm = calcMM(query.getFreeTextQuery());
				params.add(DisMaxParams.MM, "" + mm);
				if (systemConfigs.isRunningWithSolr6()) {
					params.add(DisMaxParams.MM, "1");
					params.add("q.op", "AND");
				}
			} else {
				params.add(DisMaxParams.MM, "1");
				if (systemConfigs.isRunningWithSolr6()) {
					params.add("q.op", "OR");
				}
			}

			if (systemConfigs.isSearchUsingEDismax()) {
				params.add("defType", "edismax");
			} else {
				params.add("defType", "dismax");
			}

			if (systemConfigs.isSearchUsingTermsInBQ()) {
				params.add(DisMaxParams.BQ, "\"" + query.getFreeTextQuery() + "\"");
			}

			for (SearchBoost boost : query.getQueryBoosts()) {
				params.add(DisMaxParams.BQ, boost.getKey() + "^" + boost.getValue());
			}
		}

		if (query.getUserFilters() != null) {
			for (UserFilter userFilter : query.getUserFilters()) {
				params.add(CommonParams.FQ, userFilter.buildFQ(securityTokenManager, query));
			}
		}

		params.add(CommonParams.ROWS, "" + query.getNumberOfRows());
		params.add(CommonParams.START, "" + query.getStartRow());

		if (!query.getFieldFacets().isEmpty() || !query.getQueryFacets().isEmpty() ||
			!query.getFieldPivotFacets().isEmpty()) {
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
		if (!query.getFieldPivotFacets().isEmpty()) {
			params.add(FacetParams.FACET_PIVOT, StringUtils.join(query.getFieldPivotFacets(), ","));
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

		//		if (query.getReturnedMetadatas() != null && query.getReturnedMetadatas().isOnlySummary()
		//			&& modelLayerFactory != null
		//			&& modelLayerFactory.getRecordsCaches() != null
		//			&& modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized()) {
		//			params.set(CommonParams.FL, "id");
		//
		//		} else
		if (query.getReturnedMetadatas() != null && query.getReturnedMetadatas().getAcceptedFields() != null) {
			List<String> fields = new ArrayList<>();
			fields.add("id");
			fields.add("schema_s");
			fields.add("_version_");
			fields.add("collection_s");

			List<String> secondaryCollectionLanguages = new ArrayList<>();
			if (ctx.getCollection() != null && !INEXISTENT_COLLECTION_42.equals(ctx.getCollection())) {
				secondaryCollectionLanguages.addAll(
						collectionsListManager.getCollectionInfo(ctx.getCollection()).getSecondaryCollectionLanguesCodes());
			}

			for (String field : query.getReturnedMetadatas().getAcceptedFields()) {
				fields.add(field);
				for (String secondaryCollectionLanguage : secondaryCollectionLanguages) {
					fields.add(Schemas.getSecondaryLanguageDataStoreCode(field, secondaryCollectionLanguage));
				}
			}


			params.set(CommonParams.FL, StringUtils.join(fields.toArray(), ","));

		}

		if (query.isHighlighting() && ctx.getTypes() != null) {
			HashSet<String> highligthedMetadatas = new HashSet<>();
			for (Metadata metadata : ctx.getTypes().getSearchableMetadatas()) {
				for (String language : ctx.getLanguages()) {
					highligthedMetadatas.add(metadata.getAnalyzedField(language).getDataStoreCode());
				}
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
				moreLikeThisFields.addAll(asList("content_txt_fr", "content_txt_en", "content_txt_ar"));
			}

			StringBuilder similarityFields = new StringBuilder();
			for (String aSimilarityField : moreLikeThisFields) {
				if (similarityFields.length() != 0) {
					similarityFields.append(",");
				}
				if (!aSimilarityField.contains("_txt_") && !aSimilarityField.contains("_t_")) {
					System.err.printf("The %s does not support term vector. It may cause performance issue.\n", aSimilarityField);
				}
				similarityFields.append(aSimilarityField);
			}

			params.add(MoreLikeThisParams.SIMILARITY_FIELDS, similarityFields.toString());
		}

		if (ctx.getCollection() != null) {
			SearchConfigurationsManager manager = modelLayerFactory.getSearchConfigurationsManager();
			List<String> excludeIds = manager.getDocExlusions(ctx.getCollection());

			List<String> elevateIds = new ArrayList<>();
			List<DocElevation> docElevation = manager.getDocElevations(ctx.getCollection(), query.getFreeTextQuery());
			for (DocElevation doc : docElevation) {
				if (doc.getId() != null && !excludeIds.contains(doc.getId())) {
					elevateIds.add(doc.getId());
				}
			}

			if (!excludeIds.isEmpty()) {
				params.add("excludeIds", StringUtils.join(excludeIds, ","));
			}

			if (!elevateIds.isEmpty()) {
				params.add("elevateIds", StringUtils.join(elevateIds, ","));
			}
		}

		if (query.isMoreLikeThis()) {
			params.add(CommonParams.Q, "id:" + query.getMoreLikeThisRecordId());
		} else if (addSynonyms && ctx.getCollection() != null && query.getFreeTextQuery() != null) {
			params.add(CommonParams.Q,
					modelLayerFactory.getSynonymsConfigurationsManager().computeSynonyms(ctx.getCollection(), query.getFreeTextQuery()));
		} else {
			params.add(CommonParams.Q, StringUtils.defaultString(query.getFreeTextQuery(), "*:*"));
		}

		if (Toggle.DEBUG_SOLR_TIMINGS.isEnabled()) {
			params.add("debug", "timing");
		}

		return params;
	}

	@NotNull
	private SolrQueryBuilderContext newSolrQueryBuilderContext(LogicalSearchQuery query) {
		String collection = getCollection(query);
		MetadataSchemaTypes types = null;
		if (collection != null && metadataSchemasManager != null && !collection.equals("inexistentCollection42")) {
			types = metadataSchemasManager.getSchemaTypes(collection);
		}

		List<MetadataSchemaType> searchedSchemaTypes = getSearchedTypes(query, types);

		List<String> languages = getLanguages(query);
		String queryLanguage = query.getLanguage() == null ? mainDataLanguage : query.getLanguage();

		return new SolrQueryBuilderContext(
				query.isPreferAnalyzedFields(), languages, queryLanguage, types, searchedSchemaTypes, collection);
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

			} else if (sort instanceof ScoreLogicalSearchQuerySort) {
				String field = ((ScoreLogicalSearchQuerySort) sort).getField();
				stringBuilder.append(field);
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
	 *
	 * @param userQuery
	 * @return
	 */
	private int calcMM(String userQuery) {
		HashSet queryTerms = new HashSet(asList(StringUtils.split(StringUtils.lowerCase(userQuery))));
		queryTerms.removeAll(asList(STOP_WORDS_FR));
		return queryTerms.size();
	}

	String getQfFor(List<String> languages, String queryLanguage, List<SearchBoost> boosts,
					List<MetadataSchemaType> searchedSchemaTypes, User user) {
		StringBuilder sb = new StringBuilder();

		Set<String> fields = new HashSet<>();

		List<String> localCodeWithNoAccess = new ArrayList<>();
		List<String> dataFieldCodeWithNoAccess = new ArrayList<>();
		if (user != null) {
			for (MetadataSchemaType schemaType : searchedSchemaTypes) {
				for (Metadata metadata : schemaType.getAllMetadatas()) {
					if (!user.hasGlobalAccessToMetadata(metadata)) {
						localCodeWithNoAccess.add(metadata.getLocalCode());
						dataFieldCodeWithNoAccess.add(metadata.getDataStoreCode());
					}
				}
			}
		}

		for (SearchBoost boost : boosts) {
			String dataStoreValue;
			int lastIndexOfSemiColumn = boost.getKey().lastIndexOf(":");

			if (lastIndexOfSemiColumn == -1) {
				dataStoreValue = boost.getKey();
			} else {
				dataStoreValue = boost.getKey().substring(0, lastIndexOfSemiColumn);
			}

			String[] dataStoreValueSplited = dataStoreValue.split("_");
			dataStoreValue = dataStoreValueSplited[0] + "_" + dataStoreValueSplited[1];

			if (!dataFieldCodeWithNoAccess.contains(dataStoreValue)) {
				sb.append(boost.getKey());
				sb.append("^");
				sb.append(boost.getValue());
				sb.append(" ");
				fields.add(boost.getKey());
			}
		}


		for (MetadataSchemaType schemaType : searchedSchemaTypes) {
			for (Metadata metadata : schemaType.getAllMetadatas()) {
				if (localCodeWithNoAccess.contains(metadata.getLocalCode())) {
					continue;
				}

				if (metadata.isSearchable()) {
					if (metadata.hasSameCode(Schemas.LEGACY_ID) && fields.add(Schemas.LEGACY_ID.getDataStoreCode())) {
						sb.append(Schemas.LEGACY_ID.getDataStoreCode());
						sb.append("^20 ");
					} else {
						if (metadata.getType() == MetadataValueType.CONTENT) {
							for (String language : languages) {
								String analyzedField = metadata.getAnalyzedField(language).getDataStoreCode();
								if (!fields.contains(analyzedField) && !analyzedField.contains("null")) {
									sb.append(analyzedField);
									sb.append(" ");
									fields.add(analyzedField);
								}
							}
						} else {
							String analyzedField = metadata.getAnalyzedField(metadata.isMultiLingual() ? queryLanguage : mainDataLanguage).getDataStoreCode();
							if (!fields.contains(analyzedField) && !analyzedField.contains("null")) {
								sb.append(analyzedField);
								sb.append(" ");
								fields.add(analyzedField);
							}

						}
					}
				}
			}
		}

		String idAnalyzedField = Schemas.IDENTIFIER.getAnalyzedField(mainDataLanguage).getDataStoreCode();
		if (!fields.contains(idAnalyzedField)) {
			sb.append(idAnalyzedField + " ");
			fields.add(idAnalyzedField);
		}
		return sb.toString();
	}

	private SPEQueryResponse buildResponse(LogicalSearchQuery query) {
		QueryResponseDTO queryResponseDTO = queryDao(query);
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
		Map<String, List<FacetPivotValue>> facetPivotValues = queryResponseDTO.getFieldFacetPivotValues();
		Map<String, Integer> queryFacetValues = withRemoveExclusions(queryResponseDTO.getQueryFacetValues());

		Map<String, Map<String, Object>> statisticsValues = buildStats(query.getStatisticFields(),
				queryResponseDTO.getFieldsStatistics());
		SPEQueryResponse response = new SPEQueryResponse(fieldFacetValues, facetPivotValues, statisticsValues,
				queryFacetValues, queryResponseDTO.getQtime(), queryResponseDTO.getNumFound(), records,
				queryResponseDTO.getHighlights(), queryResponseDTO.getDebugMap(),
				queryResponseDTO.isCorrectlySpelt(), queryResponseDTO.getSpellCheckerSuggestions(), moreLikeThisResult);


		if (query.getResultsProjection() != null) {
			return query.getResultsProjection().project(query, response);
		} else {
			return response;
		}
	}

	private QueryResponseDTO queryDao(LogicalSearchQuery query) {
		ModifiableSolrParams params = addSolrModifiableParams(query);
		QueryResponseDTO responseDTO = dataStoreDao(query.getDataStore()).query(query.getName(), params);
		if (query.getReturnedMetadatas() != null && query.getReturnedMetadatas().isOnlySummary()
			&& modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized()) {

			List<RecordDTO> loadedFromCacheRecordsDTO = new ArrayList<>();
			//todo Keep a short term history of deleted records, in the case they were returned by a query at the same moment

			for (RecordDTO recordDTOWithId : responseDTO.getResults()) {
				loadedFromCacheRecordsDTO.add(modelLayerFactory.getRecordsCaches()
						.getRecordSummary(recordDTOWithId.getId()).getRecordDTO());
			}


			return responseDTO = new QueryResponseDTO(
					loadedFromCacheRecordsDTO, (int) responseDTO.getQtime(), responseDTO.getNumFound(),
					responseDTO.getFieldFacetValues(),
					responseDTO.getFieldFacetPivotValues(),
					responseDTO.getFieldsStatistics(),
					responseDTO.getQueryFacetValues(), responseDTO.getHighlights(),
					responseDTO.getDebugMap(), responseDTO.isCorrectlySpelt(),
					responseDTO.getSpellCheckerSuggestions(), responseDTO.getMoreLikeThisResults()
			);
		} else {
			return responseDTO;
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


	public List<Record> getAllRecords(MetadataSchemaType schemaType) {

		final RecordsCache cache = getConnectedRecordsCache().getCache(schemaType.getCollection());

		if (cache.isConfigured(schemaType) && cache.getCacheConfigOf(schemaType.getCode()).isPermanent()) {
			return cache.getAllValues(schemaType.getCode());

		} else {
			LOGGER.error("getAllRecords should not be called on schema type '" + schemaType.getCode() + "'");
			return search(new LogicalSearchQuery(from(schemaType).returnAll()));
		}

	}

	public List<Record> getAllRecordsInUnmodifiableState(MetadataSchemaType schemaType) {

		final RecordsCache cache = getConnectedRecordsCache().getCache(schemaType.getCollection());

		if (cache.isConfigured(schemaType) && cache.getCacheConfigOf(schemaType.getCode()).isPermanent()) {
			return cache.getAllValuesInUnmodifiableState(schemaType.getCode());

		} else {
			LOGGER.error("getAllRecords should not be called on schema type '" + schemaType.getCode() + "'");
			return search(new LogicalSearchQuery(from(schemaType).returnAll()));
		}

	}

	RecordDao dataStoreDao(String dataStore) {
		if (dataStore == null || dataStore.equals(DataStore.RECORDS)) {
			return modelLayerFactory.getDataLayerFactory().newRecordDao();
		} else {
			return modelLayerFactory.getDataLayerFactory().newEventsDao();
		}
	}

	public Set<String> getHashesOf(LogicalSearchQuery query) {
		Set<String> hashes = new HashSet<>();
		String collection = getCollection(query);
		List<MetadataSchemaType> types = getSearchedTypes(query, metadataSchemasManager.getSchemaTypes(collection));
		if (types.size() != 1) {
			throw new IllegalArgumentException("Query must be searching in exactly one schema type");
		}
		MetadataSchemaType schemaType = types.get(0);

		List<Metadata> contentMetadatas = schemaType.getAllMetadatas().onlyWithType(MetadataValueType.CONTENT);

		if (contentMetadatas.isEmpty()) {
			return Collections.emptySet();
		}

		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(contentMetadatas));
		query.setCondition(query.getCondition().andWhereAny(contentMetadatas).isNotNull());

		Iterator<Record> recordIterator = recordsIterator(query, 1000);
		while (recordIterator.hasNext()) {
			Record record = recordIterator.next();
			for (Metadata contentMetadata : contentMetadatas) {
				for (Content content : record.<Content>getValues(contentMetadata)) {
					hashes.addAll(content.getHashOfAllVersions());
				}
			}
		}

		return hashes;
	}


	public Set<String> getHashesOf(LogicalSearchCondition condition) {
		return getHashesOf(new LogicalSearchQuery(condition));
	}


	public List<RecordId> recordsIdSortedByTheirDefaultSort() {

		//Trier par code s'il n'y a pas ddv dans le type de schéma
		//Sinon par titre

		List<RecordId> returnedIds = new ArrayList<>();

		for (String collection : collectionsListManager.getCollections()) {
			for (MetadataSchemaType schemaType : metadataSchemasManager.getSchemaTypes(collection).getSchemaTypesInDisplayOrder()) {

				if (schemaType.getMainSortMetadata() != null) {
					if (systemConfigs.isRunningWithSolr6() && modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration()
							.useSolrTupleStreamsIfSupported()) {
						returnedIds.addAll(recordsIdSortedByTitleUsingTupleStream(schemaType, schemaType.getMainSortMetadata()));


					} else {
						returnedIds.addAll(recordsIdSortedByTitleUsingIterator(schemaType, schemaType.getMainSortMetadata()));
					}
				}

			}
		}

		return returnedIds;
	}

	public List<RecordId> recordsIdSortedByTitleUsingTupleStream(MetadataSchemaType schemaType,
																 Metadata metadata) {

		LOGGER.info("Fetching ids of schema type '" + schemaType.getCode() + "' of collection '" + schemaType.getCollection() + "' using tuple stream method...");
		Map<String, String> props = new HashMap<>();
		props.put("q", "schema_s:" + schemaType.getCode() + "_*");
		props.put("fq", "collection_s:" + schemaType.getCollection());

		StringBuilder fields = new StringBuilder("id,");
		StringBuilder sort = new StringBuilder();

		if (metadata.isSortable()) {
			sort.append(metadata.getSortMetadata().getDataStoreCode());
			sort.append(" asc");
			sort.append(", ");

			fields.append(metadata.getSortMetadata().getDataStoreCode());
			fields.append(", ");
		}

		sort.append(metadata.getDataStoreCode());
		sort.append(" asc");

		fields.append(metadata.getDataStoreCode());

		props.put("sort", sort.toString());
		props.put("fl", fields.toString());
		props.put("rows", "10000");

		TupleStream tupleStream = dataStoreDao(DataStore.RECORDS).tupleStream(props);

		try {
			tupleStream.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		AtomicInteger count = new AtomicInteger();

		List<RecordId> ids = new ArrayList<>();
		try {

			Tuple tuple = tupleStream.read();
			while (!tuple.EOF) {
				count.incrementAndGet();
				if (count.get() % 10000 == 0) {
					LOGGER.info("Fetching ids using tuple stream method : " + count.get());
				}
				ids.add(RecordId.toId(tuple.getString("id")));
			}
			LOGGER.info("Fetching ids using tuple stream method finished : " + count.get());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				tupleStream.close();
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}

		return ids;
		//
		//
		//
		//

		//
		//		List<LogicalSearchCondition> conditions = typesWithSortableTitle.stream()
		//				.map((typeCode) -> where(Schemas.SCHEMA).isStartingWithText(typeCode + "_"))
		//				.collect(Collectors.toList());
		//
		//		LogicalSearchQuery query = new LogicalSearchQuery(fromEveryTypesOfEveryCollection().whereAnyCondition(conditions));
		//		query.sortAsc(Schemas.TITLE.getSortField());
		//		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		//		query.filteredByVisibilityStatus(ALL);
		//		query.filteredByStatus(StatusFilter.ALL);
		//		Iterator<Record> idIterator = recordsIteratorKeepingOrder(query, 100000);
		//
		//		long rows = getResultsCount(query);
		//		AtomicInteger progress = new AtomicInteger();
		//
		//		List<RecordId> ids = new ArrayList<>();
		//
		//		while (idIterator.hasNext()) {
		//			if (progress.incrementAndGet() % 100000 == 0) {
		//				LOGGER.info("loading ids " + progress.get() + "/" + rows);
		//			}
		//			ids.add(RecordId.id(idIterator.next().getId()));
		//		}
		//
		//		return ids;
	}

	public List<RecordId> recordsIdSortedByTitleUsingIterator(MetadataSchemaType schemaType, Metadata metadata) {

		LogicalSearchQuery query = new LogicalSearchQuery(from(schemaType).returnAll());
		if (metadata.isSortable()) {
			query.sortAsc(metadata.getSortField());
		}
		query.sortAsc(metadata);

		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		query.filteredByVisibilityStatus(ALL);
		query.filteredByStatus(StatusFilter.ALL);
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR);
		Iterator<Record> idIterator = recordsIteratorKeepingOrder(query, 100000);

		long rows = getResultsCount(query);
		AtomicInteger progress = new AtomicInteger();

		List<RecordId> ids = new ArrayList<>();

		while (idIterator.hasNext()) {
			if (progress.incrementAndGet() % 100000 == 0) {
				LOGGER.info("loading ids " + progress.get() + "/" + rows);
			}
			ids.add(RecordId.id(idIterator.next().getId()));
		}

		return ids;
	}

	public Iterator<RecordId> recordsIdIteratorExceptEvents() {
		if (systemConfigs.isRunningWithSolr6() && modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration()
				.useSolrTupleStreamsIfSupported()) {
			return recordsIdIteratorExceptEventsUsingTupleStream();

		} else {
			return recordsIdIteratorExceptEventsUsingQueryIterator();
		}
	}

	public Iterator<RecordId> recordsIdIteratorExceptEventsUsingQueryIterator() {
		LogicalSearchQuery query = new LogicalSearchQuery(fromEveryTypesOfEveryCollection()
				.where(Schemas.SCHEMA).isNot(startingWithText("event_")));
		query.sortAsc(Schemas.IDENTIFIER);
		query.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchema());
		query.filteredByVisibilityStatus(ALL);
		query.filteredByStatus(StatusFilter.ALL);
		Iterator<String> idIterator = recordsIdsIterator(query);

		long rows = getResultsCount(query);
		AtomicInteger progress = new AtomicInteger();
		return new LazyIterator<RecordId>() {
			@Override
			protected RecordId getNextOrNull() {

				if (progress.incrementAndGet() % 100000 == 0) {
					LOGGER.info("loading ids " + progress.get() + "/" + rows);
				}
				return idIterator.hasNext() ? RecordId.toId(idIterator.next()) : null;
			}
		};

	}

	public Iterator<RecordId> recordsIdIteratorExceptEventsUsingTupleStream() {

		LOGGER.info("Fetching ids using tuple stream method...");
		Map<String, String> props = new HashMap<>();
		props.put("q", "-schema_s:" + "event_*");
		//props.put("qt", "/export");
		props.put("sort", "id asc");
		props.put("fl", "id");
		props.put("rows", "1000000");

		TupleStream tupleStream = dataStoreDao(DataStore.RECORDS).tupleStream(props);

		try {
			tupleStream.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		AtomicInteger count = new AtomicInteger();

		return new LazyIterator<RecordId>() {

			@Override
			protected RecordId getNextOrNull() {

				try {

					Tuple tuple = tupleStream.read();
					if (tuple.EOF) {
						LOGGER.info("Fetching ids using tuple stream method finished : " + count.get());
						tupleStream.close();
						return null;
					} else {
						//LOGGER.info("Fetching ids and versions of schema type '" + schemaType.getCollection() + ":" + schemaType.getCode() + "' using tuple stream method ... : " + count.get());
						count.incrementAndGet();
						return RecordId.toId(tuple.getString("id"));
					}
				} catch (IOException e) {
					try {
						tupleStream.close();
					} catch (IOException e1) {
						throw new RuntimeException(e1);
					}
					throw new RuntimeException(e);
				}
			}
		};


	}

}
