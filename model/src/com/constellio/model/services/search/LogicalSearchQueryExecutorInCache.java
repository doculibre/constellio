package com.constellio.model.services.search;

import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerSystemExtensions;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.FieldLogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery.UserFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.search.query.logical.condition.CompositeLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.search.query.logical.criteria.IsEqualCriterion;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.PREFERRING;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.search.VisibilityStatusFilter.ALL;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_CACHE;
import static java.util.Arrays.asList;

public class LogicalSearchQueryExecutorInCache {

	private static Logger LOGGER = LoggerFactory.getLogger(LogicalSearchQueryExecutorInCache.class);

	SearchServices searchServices;
	RecordsCaches recordsCaches;
	MetadataSchemasManager schemasManager;
	ModelLayerSystemExtensions modelLayerExtensions;
	String mainDataLanguage;

	public LogicalSearchQueryExecutorInCache(SearchServices searchServices, RecordsCaches recordsCaches,
											 MetadataSchemasManager schemasManager,
											 ModelLayerSystemExtensions modelLayerExtensions,
											 String mainDataLanguage) {
		this.searchServices = searchServices;
		this.recordsCaches = recordsCaches;
		this.schemasManager = schemasManager;
		this.modelLayerExtensions = modelLayerExtensions;
		this.mainDataLanguage = mainDataLanguage;
	}

	public Stream<Record> stream(LogicalSearchQuery query) {
		MetadataSchemaType schemaType = getQueriedSchemaType(query.getCondition());

		Predicate<Record> filter = toStreamFilter(query);

		Stream<Record> stream = newBaseRecordStream(query, schemaType, filter);

		if (!query.getSortFields().isEmpty()) {
			return stream.sorted(newQuerySortFieldsComparator(query, schemaType));
		} else {
			return stream;
		}
	}

	private Predicate<Record> toStreamFilter(LogicalSearchQuery query) {
		Predicate<Record> filter = new Predicate<Record>() {
			@Override
			public boolean test(Record record) {

				boolean result = true;

				switch (query.getVisibilityStatusFilter()) {

					case HIDDENS:
						result = Boolean.TRUE.equals(record.get(Schemas.HIDDEN));
						break;
					case VISIBLES:
						result = !Boolean.TRUE.equals(record.get(Schemas.HIDDEN));
						break;
				}

				if (!result) {
					return false;
				}

				switch (query.getStatusFilter()) {

					case DELETED:
						result = Boolean.TRUE.equals(record.get(Schemas.LOGICALLY_DELETED_STATUS));
						break;
					case ACTIVES:
						result = !Boolean.TRUE.equals(record.get(Schemas.LOGICALLY_DELETED_STATUS));
						break;
				}

				return result;
			}
		}.and(query.getCondition());


		if (query.getCondition().getFilters() instanceof SchemaFilters) {
			SchemaFilters schemaFilters = (SchemaFilters) query.getCondition().getFilters();

			if (schemaFilters.getSchemaFilter() != null && schemaFilters.getSchemaTypeFilter() == null) {
				filter = new Predicate<Record>() {
					@Override
					public boolean test(Record record) {
						return schemaFilters.getSchemaFilter().getCode().equals(record.getSchemaCode());
					}
				}.and(filter);
			}
		}


		//		if (Toggle.VALIDATE_CACHE_EXECUTION_SERVICE_USING_SOLR.isEnabled()) {
		//			filter = filter.and(new Predicate<Record>() {
		//				@Override
		//				public boolean test(Record record) {
		//					LOGGER.info("Record returned by stream : " + record.getIdTitle());
		//
		//					return true;
		//				}
		//			});
		//		}

		if (query.getUserFilters() != null && !query.getUserFilters().isEmpty()) {
			filter = filter.and(record -> isRecordAccessibleForUser(query.getUserFilters(), record));
		}
		return filter;
	}

	private DataStoreFieldLogicalSearchCondition findRequiredFieldEqualCondition(LogicalSearchCondition condition,
																				 MetadataSchemaType schemaType) {
		if (condition instanceof DataStoreFieldLogicalSearchCondition) {
			DataStoreFieldLogicalSearchCondition fieldCondition = (DataStoreFieldLogicalSearchCondition) condition;
			LogicalSearchValueCondition logicalSearchValueCondition = fieldCondition.getValueCondition();
			if (logicalSearchValueCondition instanceof IsEqualCriterion) {
				IsEqualCriterion isEqualCriterion = (IsEqualCriterion) logicalSearchValueCondition;
				List<DataStoreField> dataStoreFields = fieldCondition.getDataStoreFields();

				if (dataStoreFields != null && dataStoreFields.size() == 1) {
					Object value = isEqualCriterion.getMemoryQueryValue();
					DataStoreField dataStoreField = dataStoreFields.get(0);
					if (((Metadata) dataStoreField).getCode().startsWith("global")) {
						dataStoreField = schemaType.getDefaultSchema().getMetadata(dataStoreField.getLocalCode());
					}
					if (canDataGetByMetadata(dataStoreField, value)) {
						return (DataStoreFieldLogicalSearchCondition) condition;
					}
				}
			}
		} else if (condition instanceof CompositeLogicalSearchCondition) {
			CompositeLogicalSearchCondition compositeCondition = (CompositeLogicalSearchCondition) condition;
			LogicalOperator logicalOperator = compositeCondition.getLogicalOperator();
			if (logicalOperator == LogicalOperator.AND) {
				for (LogicalSearchCondition childCondition : compositeCondition.getNestedSearchConditions()) {
					DataStoreFieldLogicalSearchCondition requiredFieldEqualCondition = findRequiredFieldEqualCondition(childCondition, schemaType);
					if (requiredFieldEqualCondition != null) {
						return requiredFieldEqualCondition;
					}
				}
			}
		}

		return null;
	}

	@NotNull
	private Stream<Record> newBaseRecordStream(LogicalSearchQuery query, MetadataSchemaType schemaType,
											   Predicate<Record> filter) {
		final long startOfStreaming = new Date().getTime();

		DataStoreFieldLogicalSearchCondition requiredFieldEqualCondition
				= findRequiredFieldEqualCondition(query.getCondition(), schemaType);

		Stream<Record> stream;
		if (requiredFieldEqualCondition == null) {
			stream = recordsCaches.stream(schemaType);
		} else {
			Metadata metadata = (Metadata) requiredFieldEqualCondition.getDataStoreFields().get(0);

			if ((metadata).getCode().startsWith("global")) {
				metadata = schemaType.getDefaultSchema().getMetadata(metadata.getLocalCode());
			}

			Object value = ((IsEqualCriterion) requiredFieldEqualCondition.getValueCondition()).getMemoryQueryValue();
			if (query.getReturnedMetadatas() != null && query.getReturnedMetadatas().isOnlySummary()) {
				stream = recordsCaches.getRecordsSummaryByIndexedMetadata(schemaType, metadata, (String) value);
			} else {
				stream = recordsCaches.getRecordsByIndexedMetadata(schemaType, metadata, (String) value);
			}
		}

		return stream.filter(filter).

				onClose(() ->

				{
					long duration = new Date().getTime() - startOfStreaming;
					modelLayerExtensions.onQueryExecution(query, duration);
				});
	}

	private static boolean isRecordAccessibleForUser(List<UserFilter> userFilterList, Record record) {
		for (UserFilter currentUserFilter : userFilterList) {
			if (!currentUserFilter.hasUserAccessToRecord(record)) {
				return false;
			}
		}

		return true;
	}

	private boolean canDataGetByMetadata(DataStoreField dataStoreField, Object value) {
		return value instanceof String
			   && !dataStoreField.isEncrypted() && (dataStoreField.isUniqueValue() || dataStoreField.isCacheIndex());
	}

	@NotNull
	private Comparator<Record> newQuerySortFieldsComparator(LogicalSearchQuery query, MetadataSchemaType schemaType) {
		return (o1, o2) -> {

			String queryLanguage = query.getLanguage() == null ? mainDataLanguage : query.getLanguage();
			Locale locale = Language.withCode(queryLanguage).getLocale();
			for (LogicalSearchQuerySort sort : query.getSortFields()) {
				FieldLogicalSearchQuerySort fieldSort = (FieldLogicalSearchQuerySort) sort;
				Metadata metadata =
						schemaType.getDefaultSchema().getMetadataByDatastoreCode(fieldSort.getField().getDataStoreCode());
				if (metadata != null) {
					int sortValue;
					sortValue = compareMetadatasValues(o1, o2, metadata, locale, sort.isAscending());

					if (sortValue != 0) {
						return sortValue;
					}
				}
			}

			return 0;
		};
	}


	@NotNull
	private Comparator<Record> newIdComparator() {
		return (o1, o2) -> {
			return o1.getId().compareTo(o2.getId());
		};
	}

	private int compareMetadatasValues(Record record1, Record record2, Metadata metadata, Locale preferedLanguage,
									   boolean ascending) {
		Object value1 = record1.get(metadata, preferedLanguage, PREFERRING);
		Object value2 = record2.get(metadata, preferedLanguage, PREFERRING);

		if (metadata.getType() == INTEGER) {
			if (value1 == null) {
				value1 = 0;
			}
			if (value2 == null) {
				value2 = 0;
			}
		} else if (metadata.getType() == NUMBER) {
			if (value1 == null) {
				value1 = 0.0;
			}
			if (value2 == null) {
				value2 = 0.0;
			}
		}

		if (metadata.getLocalCode().equals(Schemas.IDENTIFIER.getLocalCode())) {
			//Nothing!

		} else if (metadata.hasNormalizedSortField()) {
			if (value1 instanceof String && metadata.getSortFieldNormalizer() != null) {
				value1 = metadata.getSortFieldNormalizer().normalize((String) value1);
			}

			if (value2 instanceof String && metadata.getSortFieldNormalizer() != null) {
				value2 = metadata.getSortFieldNormalizer().normalize((String) value2);
			}
		} else {
			if (value1 instanceof String) {
				value1 = AccentApostropheCleaner.removeAccents(((String) value1).toLowerCase());
			}

			if (value2 instanceof String) {
				value2 = AccentApostropheCleaner.removeAccents(((String) value2).toLowerCase());
			}
		}

		int sort = LangUtils.nullableNaturalCompare((Comparable) value1, (Comparable) value2, ascending);
		return ascending ? sort : (-1 * sort);
	}

	public Stream<Record> stream(LogicalSearchCondition condition) {
		return stream(new LogicalSearchQuery(condition).filteredByVisibilityStatus(ALL));
	}

	public boolean isQueryExecutableInCache(LogicalSearchQuery query) {
		if (recordsCaches == null) {
			return false;
		}

		return hasNoUnsupportedFeatureOrFilter(query)
			   && isConditionExecutableInCache(query.getCondition(), query.getReturnedMetadatas(), query.getQueryExecutionMethod());

	}


	private static List<Predicate<LogicalSearchQuery>> requirements = asList(
			(query) -> query.getQueryExecutionMethod() != QueryExecutionMethod.USE_SOLR,
			(query) -> query.getFacetFilters().toSolrFilterQueries().isEmpty(),
			(query) -> hasNoUnsupportedSort(query),
			(query) -> query.getFreeTextQuery() == null,
			(query) -> query.getFieldPivotFacets().isEmpty(),
			(query) -> query.getQueryBoosts().isEmpty(),
			(query) -> query.getStatisticFields().isEmpty(),
			(query) -> !query.isPreferAnalyzedFields(),
			(query) -> query.getResultsProjection() == null,
			(query) -> query.getFieldFacets().isEmpty(),
			(query) -> query.getFieldPivotFacets().isEmpty(),
			(query) -> query.getQueryFacets().isEmpty(),
			(query) -> areAllFiltersExecutableInCache(query.getUserFilters()),
			(query) -> !query.isHighlighting()


	);

	/**
	 * Will throw IllegalArgumentException if the query is requiring execution in cache and use unsupported features
	 *
	 * @param query
	 * @return
	 */
	public static boolean hasNoUnsupportedFeatureOrFilter(LogicalSearchQuery query) {
		for (int i = 0; i < requirements.size(); i++) {
			if (!requirements.get(i).test(query)) {
				if (query.getQueryExecutionMethod().requiringCacheExecution()) {
					throw new IllegalArgumentException("Query is using a feature which is not supported with execution in cache. Requirement at index '" + i + "' failed.");
				} else {
					return false;
				}

			}
		}
		return true;

	}

	private static boolean areAllFiltersExecutableInCache(List<UserFilter> userFilters) {
		if (userFilters == null || userFilters.isEmpty()) {
			return true;
		}

		for (UserFilter currentUserFilter : userFilters) {
			if (!currentUserFilter.isExecutableInCache()) {
				return false;
			}
		}

		return true;
	}

	private static boolean hasNoSort(LogicalSearchQuery query) {
		return query.getSortFields().isEmpty();
	}


	private static boolean hasNoUnsupportedSort(LogicalSearchQuery query) {
		for (LogicalSearchQuerySort sort : query.getSortFields()) {
			if (!(sort instanceof FieldLogicalSearchQuerySort)) {
				return false;
			} else {
				FieldLogicalSearchQuerySort fieldSort = (FieldLogicalSearchQuerySort) sort;
				return fieldSort.getField().getType() == STRING
					   || fieldSort.getField().getType() == NUMBER
					   || fieldSort.getField().getType() == INTEGER;
			}
		}

		return true;
	}


	public boolean isConditionExecutableInCache(LogicalSearchCondition condition,
												QueryExecutionMethod queryExecutionMethod) {
		return isConditionExecutableInCache(condition, ReturnedMetadatasFilter.all(), queryExecutionMethod);
	}

	public boolean isConditionExecutableInCache(LogicalSearchCondition condition,
												ReturnedMetadatasFilter returnedMetadatasFilter,
												QueryExecutionMethod queryExecutionMethod) {
		MetadataSchemaType schemaType = getQueriedSchemaType(condition);

		if (recordsCaches == null || schemaType == null || !recordsCaches.isCacheInitialized(schemaType)) {
			return false;
		}

		if (schemaType == null || !Toggle.USE_CACHE_FOR_QUERY_EXECUTION.isEnabled()) {
			return false;

		} else if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
			return condition.isSupportingMemoryExecution(false, queryExecutionMethod == USE_CACHE)
				   && (!queryExecutionMethod.requiringCacheIndexBaseStream(schemaType.getCacheType()) || findRequiredFieldEqualCondition(condition, schemaType) != null);

		} else if (schemaType.getCacheType().hasPermanentCache()) {
			//Verify that schemaType is loaded
			return (returnedMetadatasFilter.isOnlySummary() || returnedMetadatasFilter.isOnlyId()) &&
				   condition.isSupportingMemoryExecution(true, queryExecutionMethod == USE_CACHE)
				   && (!queryExecutionMethod.requiringCacheIndexBaseStream(schemaType.getCacheType()) || findRequiredFieldEqualCondition(condition, schemaType) != null);

		} else {
			return false;
		}


	}

	private MetadataSchemaType getQueriedSchemaType(LogicalSearchCondition condition) {
		List<String> schemaTypes = condition.getFilterSchemaTypesCodes();

		if (schemaTypes != null && schemaTypes.size() == 1 && condition.getCollection() != null) {

			MetadataSchemaType schemaType = schemasManager.getSchemaTypes(condition.getCollection())
					.getSchemaType(schemaTypes.get(0));

			return schemaType;

		} else {
			return null;
		}
	}

	public int estimateMaxResultSize(LogicalSearchQuery query) {
		if (isQueryExecutableInCache(query)) {

			MetadataSchemaType schemaType = getQueriedSchemaType(query.getCondition());

			DataStoreFieldLogicalSearchCondition requiredFieldEqualCondition =
					findRequiredFieldEqualCondition(query.getCondition(), schemaType);

			Metadata metadata = (Metadata) requiredFieldEqualCondition.getDataStoreFields().get(0);

			if ((metadata).getCode().startsWith("global")) {
				metadata = schemaType.getDefaultSchema().getMetadata(metadata.getLocalCode());
			}

			Object value = ((IsEqualCriterion) requiredFieldEqualCondition.getValueCondition()).getMemoryQueryValue();
			return recordsCaches.estimateMaxResultSizeUsingIndexedMetadata(schemaType, metadata, (String) value);
		} else {
			return -1;
		}

	}
}
