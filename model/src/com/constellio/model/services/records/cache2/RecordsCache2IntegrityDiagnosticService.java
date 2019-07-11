package com.constellio.model.services.records.cache2;

import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.impl.factory.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.constellio.model.entities.schemas.Schemas.ESTIMATED_SIZE;
import static java.util.Arrays.asList;

public class RecordsCache2IntegrityDiagnosticService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCache2IntegrityDiagnosticService.class);

	private static final String RECORDS_WITH_OLD_VERSION_IN_PERMANENT_CACHE = "recordsWithOldVersionInPermanentCache";
	private static final String RECORDS_WITH_BAD_STATE_IN_PERMANENT_CACHE = "recordsWithBadStateInPermanentCache";
	private static final String MISSING_RECORDS_IN_PERMANENT_CACHE = "missingRecordsInPermanentCache";
	private static final String RECORDS_WITH_DIFFERENT_METADATA_VALUES_IN_PERMANENT_CACHE = "recordsWithDifferentMetadataValuesInPermanentCache";
	private static final String DUPLICATE_RECORDS_IN_PERMANENT_CACHE_COLLECTION_STREAM = "duplicateRecordInPermanentCacheCollectionStream";
	private static final String DUPLICATE_RECORDS_IN_PERMANENT_CACHE_TYPE_STREAM = "duplicateRecordInPermanentCacheTypeStream";
	private static final String MISSING_RECORDS_IN_PERMANENT_CACHE_COLLECTION_STREAM = "missingRecordInPermanentCacheCollectionStream";
	private static final String MISSING_RECORDS_IN_PERMANENT_CACHE_TYPE_STREAM = "missingRecordInPermanentCacheTypeStream";
	private static final String DELETED_RECORDS_IN_PERMANENT_CACHE_COLLECTION_STREAM = "deletedRecordInPermanentCacheCollectionStream";
	private static final String DELETED_RECORDS_IN_PERMANENT_CACHE_TYPE_STREAM = "deletedRecordInPermanentCacheTypeStream";

	private static final String RECORDS_WITH_OLD_VERSION_IN_VOLATILE_CACHE = "recordsWithOldVersionInVolatileCache";
	private static final String RECORDS_WITH_BAD_STATE_IN_VOLATILE_CACHE = "recordsWithBadStateInVolatileCache";
	private static final String DELETED_RECORDS_IN_VOLATILE_CACHE = "deletedRecordsInVolatileCache";
	private static final String RECORDS_WITH_DIFFERENT_METADATA_VALUES_IN_VOLATILE_CACHE = "recordsWithDifferentMetadataValuesInVolatileCache";

	RecordsCaches recordsCaches;
	SearchServices searchServices;
	RecordServices recordServices;
	RecordServices cacheLessRecordServices;
	CollectionsListManager collectionsListManager;
	MetadataSchemasManager metadataSchemasManager;

	static List<String> includedTypesFromDiagnostic = asList(EmailToSend.SCHEMA_TYPE, TemporaryRecord.SCHEMA_TYPE);

	public RecordsCache2IntegrityDiagnosticService(ModelLayerFactory modelLayerFactory) {
		this.recordsCaches = modelLayerFactory.getRecordsCaches();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.cacheLessRecordServices = modelLayerFactory.newCachelessRecordServices();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public ValidationErrors validateIntegrity(boolean repair, boolean runTwiceToEliminateProblemsCausedByBadTiming) {
		recordServices.flushRecords();
		ValidationErrors errors = new ValidationErrors();
		for (String collection : collectionsListManager.getCollections()) {
			for (MetadataSchemaType schemaType : metadataSchemasManager.getSchemaTypes(collection).getSchemaTypes()) {


				if (!includedTypesFromDiagnostic.contains(schemaType.getCode())
					&& schemaType.getCacheType().hasPermanentCache()) {

					PermanentCacheReport report = validatePermanentCacheScanningSolr(schemaType,
							repair && !runTwiceToEliminateProblemsCausedByBadTiming);

					if (runTwiceToEliminateProblemsCausedByBadTiming && report.hasError()) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						PermanentCacheReport report2 = validatePermanentCacheScanningSolr(schemaType, repair);
						report = combineErrorsInBothReports(report, report2);
					}

					report.addErrors(errors);
				}

				if (!includedTypesFromDiagnostic.contains(schemaType.getCode())
					&& schemaType.getCacheType().hasVolatileCache()) {
					VolatileCacheReport report = validateVolatileCache(schemaType,
							repair && !runTwiceToEliminateProblemsCausedByBadTiming);


					if (runTwiceToEliminateProblemsCausedByBadTiming && report.hasError()) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						VolatileCacheReport report2 = validateVolatileCache(schemaType, repair);
						report = combineErrorsInBothReports(report, report2);
					}

					report.addErrors(errors);
				}
			}
		}

		return errors;

	}

	private static class PermanentCacheReport {

		Set<String> recordsWithOldVersion = new HashSet<>();
		Set<String> recordsWithBadState = new HashSet<>();
		Set<String> missingRecords = new HashSet<>();
		Set<String> recordsWithDifferentMetadataValues = new HashSet<>();

		Set<String> duplicateRecordInCollectionStream = new HashSet<>();
		Set<String> duplicateRecordInTypeStream = new HashSet<>();
		Set<String> missingRecordInCollectionStream = new HashSet<>();
		Set<String> missingRecordInTypeStream = new HashSet<>();
		Set<String> deletedRecordInCollectionStream = new HashSet<>();
		Set<String> deletedRecordInTypeStream = new HashSet<>();

		MetadataSchemaType schemaType;

		public PermanentCacheReport(MetadataSchemaType schemaType) {
			this.schemaType = schemaType;
		}

		public void addErrors(ValidationErrors errors) {
			addErrorIfNotEmpty(errors, schemaType, RECORDS_WITH_OLD_VERSION_IN_PERMANENT_CACHE, recordsWithOldVersion);
			addErrorIfNotEmpty(errors, schemaType, RECORDS_WITH_BAD_STATE_IN_PERMANENT_CACHE, recordsWithBadState);
			addErrorIfNotEmpty(errors, schemaType, MISSING_RECORDS_IN_PERMANENT_CACHE, missingRecords);
			addErrorIfNotEmpty(errors, schemaType, RECORDS_WITH_DIFFERENT_METADATA_VALUES_IN_PERMANENT_CACHE, recordsWithDifferentMetadataValues);

			addErrorIfNotEmpty(errors, schemaType, DUPLICATE_RECORDS_IN_PERMANENT_CACHE_COLLECTION_STREAM, duplicateRecordInCollectionStream);
			addErrorIfNotEmpty(errors, schemaType, DUPLICATE_RECORDS_IN_PERMANENT_CACHE_TYPE_STREAM, duplicateRecordInTypeStream);
			addErrorIfNotEmpty(errors, schemaType, MISSING_RECORDS_IN_PERMANENT_CACHE_COLLECTION_STREAM, missingRecordInCollectionStream);
			addErrorIfNotEmpty(errors, schemaType, MISSING_RECORDS_IN_PERMANENT_CACHE_TYPE_STREAM, missingRecordInTypeStream);
			addErrorIfNotEmpty(errors, schemaType, DELETED_RECORDS_IN_PERMANENT_CACHE_COLLECTION_STREAM, deletedRecordInCollectionStream);
			addErrorIfNotEmpty(errors, schemaType, DELETED_RECORDS_IN_PERMANENT_CACHE_TYPE_STREAM, deletedRecordInTypeStream);
		}

		public boolean hasError() {
			return !recordsWithOldVersion.isEmpty() || !recordsWithBadState.isEmpty() || !missingRecords.isEmpty()
				   || !recordsWithDifferentMetadataValues.isEmpty()
				   || !duplicateRecordInCollectionStream.isEmpty() || !duplicateRecordInTypeStream.isEmpty()
				   || !missingRecordInCollectionStream.isEmpty() || !missingRecordInTypeStream.isEmpty()
				   || !deletedRecordInCollectionStream.isEmpty() || !deletedRecordInTypeStream.isEmpty();
		}

	}

	private PermanentCacheReport combineErrorsInBothReports(PermanentCacheReport report1,
															PermanentCacheReport report2) {
		PermanentCacheReport report = new PermanentCacheReport(report1.schemaType);
		report.recordsWithOldVersion = Sets.intersect(report1.recordsWithOldVersion, report2.recordsWithOldVersion);
		report.recordsWithBadState = Sets.intersect(report1.recordsWithBadState, report2.recordsWithBadState);
		report.missingRecords = Sets.intersect(report1.missingRecords, report2.missingRecords);
		report.recordsWithDifferentMetadataValues = Sets.intersect(report1.recordsWithDifferentMetadataValues, report2.recordsWithDifferentMetadataValues);

		report.duplicateRecordInCollectionStream = Sets.intersect(report1.duplicateRecordInCollectionStream, report2.duplicateRecordInCollectionStream);
		report.duplicateRecordInTypeStream = Sets.intersect(report1.duplicateRecordInTypeStream, report2.duplicateRecordInTypeStream);
		report.missingRecordInCollectionStream = Sets.intersect(report1.missingRecordInCollectionStream, report2.missingRecordInCollectionStream);
		report.missingRecordInTypeStream = Sets.intersect(report1.missingRecordInTypeStream, report2.missingRecordInTypeStream);
		report.deletedRecordInCollectionStream = Sets.intersect(report1.deletedRecordInCollectionStream, report2.deletedRecordInCollectionStream);
		report.deletedRecordInTypeStream = Sets.intersect(report1.deletedRecordInTypeStream, report2.deletedRecordInTypeStream);

		return report;
	}

	private VolatileCacheReport combineErrorsInBothReports(VolatileCacheReport report1,
														   VolatileCacheReport report2) {

		VolatileCacheReport report = new VolatileCacheReport(report1.schemaType);
		report.recordsWithOldVersionInVolatileCache = Sets.intersect(report1.recordsWithOldVersionInVolatileCache, report2.recordsWithOldVersionInVolatileCache);
		report.recordsWithBadStateInVolatileCache = Sets.intersect(report1.recordsWithBadStateInVolatileCache, report2.recordsWithBadStateInVolatileCache);
		report.deletedRecordsInVolatileCache = Sets.intersect(report1.deletedRecordsInVolatileCache, report2.deletedRecordsInVolatileCache);
		report.recordsWithDifferentMetadataValuesInVolatileCache = Sets.intersect(report1.recordsWithDifferentMetadataValuesInVolatileCache, report2.recordsWithDifferentMetadataValuesInVolatileCache);

		return report;
	}


	private static class VolatileCacheReport {

		Set<String> recordsWithOldVersionInVolatileCache = new HashSet<>();
		Set<String> recordsWithBadStateInVolatileCache = new HashSet<>();
		Set<String> deletedRecordsInVolatileCache = new HashSet<>();
		Set<String> recordsWithDifferentMetadataValuesInVolatileCache = new HashSet<>();

		MetadataSchemaType schemaType;

		public VolatileCacheReport(MetadataSchemaType schemaType) {
			this.schemaType = schemaType;
		}

		public void addErrors(ValidationErrors errors) {
			addErrorIfNotEmpty(errors, schemaType, RECORDS_WITH_OLD_VERSION_IN_VOLATILE_CACHE, recordsWithOldVersionInVolatileCache);
			addErrorIfNotEmpty(errors, schemaType, RECORDS_WITH_BAD_STATE_IN_VOLATILE_CACHE, recordsWithBadStateInVolatileCache);
			addErrorIfNotEmpty(errors, schemaType, RECORDS_WITH_DIFFERENT_METADATA_VALUES_IN_VOLATILE_CACHE, recordsWithDifferentMetadataValuesInVolatileCache);
			addErrorIfNotEmpty(errors, schemaType, DELETED_RECORDS_IN_VOLATILE_CACHE, deletedRecordsInVolatileCache);
		}

		public boolean hasError() {
			return !recordsWithOldVersionInVolatileCache.isEmpty() || !recordsWithBadStateInVolatileCache.isEmpty()
				   || !deletedRecordsInVolatileCache.isEmpty()
				   || !recordsWithDifferentMetadataValuesInVolatileCache.isEmpty();
		}

	}


	private VolatileCacheReport validateVolatileCache(MetadataSchemaType schemaType, boolean repair) {
		VolatileCacheReport report = new VolatileCacheReport(schemaType);

		RecordsCache recordsCaches2 = recordsCaches.getCache(schemaType.getCollection());

		Stream<Record> recordStream = recordsCaches2.streamVolatile(schemaType);
		recordStream.forEach((Record recordFromVolatileCache) -> {

			Record recordFromSolr;

			try {
				recordFromSolr = cacheLessRecordServices.getDocumentById(recordFromVolatileCache.getId());
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				recordFromSolr = null;
			}

			if (recordFromSolr == null) {
				report.deletedRecordsInVolatileCache.add(recordFromVolatileCache.getId());
			} else {

				if (!wasReindexedOrMarked(schemaType, recordFromVolatileCache, recordFromSolr)) {
					if (recordFromVolatileCache.getVersion() != recordFromSolr.getVersion()) {
						report.recordsWithOldVersionInVolatileCache.add(recordFromVolatileCache.getId());
					}

					if (hasDifferentMetadatas(schemaType, recordFromVolatileCache, recordFromSolr)) {
						report.recordsWithDifferentMetadataValuesInVolatileCache.add(recordFromVolatileCache.getId());
					}
				}

				if (recordFromVolatileCache.isSummary() || recordFromVolatileCache.isDirty()) {
					report.recordsWithBadStateInVolatileCache.add(recordFromVolatileCache.getId());
				}
			}
		});


		return report;
	}


	private boolean wasReindexedOrMarked(MetadataSchemaType schemaType, Record recordFromCache,
										 Record recordFromSolr) {

		for (Metadata metadata : asList(Schemas.MARKED_FOR_REINDEXING, Schemas.MARKED_FOR_PREVIEW_CONVERSION, Schemas.MARKED_FOR_PARSING)) {
			//,
			Object valueFromSolr = recordFromSolr.getValues(metadata).stream().filter((e) -> isValidValue(e)).collect(Collectors.toList());
			Object valueFromCache = recordFromCache.getValues(metadata).stream().filter((e) -> isValidValue(e)).collect(Collectors.toList());

			if (!LangUtils.isEqual(valueFromSolr, valueFromCache)) {
				return true;
			}
		}

		return false;
	}

	private boolean hasDifferentMetadatas(MetadataSchemaType schemaType, Record recordFromCache,
										  Record recordFromSolr) {

		if (!recordFromSolr.getSchemaCode().equals(recordFromCache.getSchemaCode())) {
			LOGGER.warn("Record '" + recordFromCache.getId() + "' has a different schema code in cache");
			return true;
		}

		for (Metadata metadata : schemaType.getSchema(recordFromSolr.getSchemaCode()).getMetadatas()) {

			if (metadata.getType() == MetadataValueType.STRUCTURE || metadata.getType() == MetadataValueType.CONTENT) {
				//TODO Improve!
				continue;
			}

			//,
			if (metadata.isSameLocalCodeThanAny(ESTIMATED_SIZE)) {
				continue;
			}

			if (metadata.isMultivalue()) {

				List<Object> valueFromSolr = recordFromSolr.getValues(metadata).stream().filter((e) -> isValidValue(e)).collect(Collectors.toList());
				List<Object> valueFromCache = recordFromCache.getValues(metadata).stream().filter((e) -> isValidValue(e)).collect(Collectors.toList());

				if (metadata.getType() == MetadataValueType.INTEGER) {
					valueFromSolr = valueFromSolr.stream().map((Object o) -> (((Number) o).intValue())).collect(Collectors.toList());
				}

				if (metadata.getType() == MetadataValueType.INTEGER) {
					valueFromCache = valueFromCache.stream().map((Object o) -> (((Number) o).intValue())).collect(Collectors.toList());
				}

				if (metadata.getType() == MetadataValueType.NUMBER) {
					valueFromSolr = valueFromSolr.stream().map((Object o) -> (((Number) o).doubleValue())).collect(Collectors.toList());
				}

				if (metadata.getType() == MetadataValueType.NUMBER) {
					valueFromCache = valueFromCache.stream().map((Object o) -> (((Number) o).doubleValue())).collect(Collectors.toList());
				}

				valueFromSolr.removeIf(Objects::isNull);
				valueFromCache.removeIf(Objects::isNull);

				if (!LangUtils.isEqual(valueFromSolr, valueFromCache)) {
					LOGGER.warn("Record '" + recordFromCache.getId() + "' has a different value in cache for metadata '" + metadata.getLocalCode() + "'\nValue from cache : " + valueFromCache + "\nValue from solr : " + valueFromSolr + "\n");
					return true;
				}
			} else {
				Object valueFromSolr = recordFromSolr.get(metadata);
				Object valueFromCache = recordFromCache.get(metadata);

				if (metadata.getType() == MetadataValueType.INTEGER && valueFromSolr != null) {
					valueFromSolr = ((Number) valueFromSolr).intValue();
				}

				if (metadata.getType() == MetadataValueType.INTEGER && valueFromCache != null) {
					valueFromCache = ((Number) valueFromCache).intValue();
				}

				if (metadata.getType() == MetadataValueType.NUMBER && valueFromSolr != null) {
					valueFromSolr = ((Number) valueFromSolr).doubleValue();
				}

				if (metadata.getType() == MetadataValueType.NUMBER && valueFromCache != null) {
					valueFromCache = ((Number) valueFromCache).doubleValue();
				}

				if (!LangUtils.isEqual(valueFromSolr, valueFromCache)) {
					LOGGER.warn("Record '" + recordFromCache.getId() + "' has a different value in cache for metadata '" + metadata.getLocalCode() + "'\nValue from cache : " + valueFromCache + "\nValue from solr : " + valueFromSolr + "\n");
					return true;
				}
			}
		}

		return false;
	}

	private boolean isValidValue(Object e) {
		return e != null && (!(e instanceof String) || StringUtils.isNotBlank((String) e));
	}

	private PermanentCacheReport validatePermanentCacheScanningSolr(MetadataSchemaType schemaType, boolean repair) {
		boolean summary = schemaType.getCacheType().isSummaryCache();
		Stream<Record> recordStream = searchServices.streamFromSolr(schemaType, summary);
		PermanentCacheReport report = new PermanentCacheReport(schemaType);

		RecordsCache cache = recordsCaches.getCache(schemaType.getCollection());
		Set<String> recordsIdsFoundInSolr = new HashSet<>();

		recordStream.forEach((recordFromSolr) -> {
			recordsIdsFoundInSolr.add(recordFromSolr.getId());
			Record recordFromCache = summary ? cache.getSummary(recordFromSolr.getId()) : cache.get(recordFromSolr.getId());
			if (recordFromCache == null) {
				report.missingRecords.add(recordFromSolr.getId());
			} else {
				if (recordFromCache.isDirty() || (recordFromCache.isSummary() && !summary)
					|| (!recordFromCache.isSummary() && summary)) {
					report.recordsWithBadState.add(recordFromCache.getId());
				}

				if (!wasReindexedOrMarked(schemaType, recordFromCache, recordFromSolr)) {
					if (recordFromCache.getVersion() != recordFromSolr.getVersion()) {
						report.recordsWithOldVersion.add(recordFromSolr.getId());
					}

					if (hasDifferentMetadatas(schemaType, recordFromCache, recordFromSolr)) {
						report.recordsWithDifferentMetadataValues.add(recordFromCache.getId());
					}
				}
			}
		});


		Set<String> recordsIdsFoundInCacheCollectionStream = new HashSet<>();

		recordsCaches.stream(schemaType.getCollection())
				.filter((r -> r.getTypeCode().equals(schemaType.getCode())))
				.map(Record::getId)
				.forEach((id -> {
					if (recordsIdsFoundInCacheCollectionStream.contains(id)) {
						report.duplicateRecordInCollectionStream.add(id);
					}
					recordsIdsFoundInCacheCollectionStream.add(id);
				}));

		Set<String> recordsIdsFoundInCacheTypeStream = new HashSet<>();

		recordsCaches.stream(schemaType.getCollection())
				.filter((r -> r.getTypeCode().equals(schemaType.getCode())))
				.map(Record::getId)
				.forEach((id -> {
					if (recordsIdsFoundInCacheTypeStream.contains(id)) {
						report.duplicateRecordInTypeStream.add(id);
					}
					recordsIdsFoundInCacheTypeStream.add(id);
				}));

		ListComparisonResults<String> results = LangUtils.compareSorting(recordsIdsFoundInSolr, recordsIdsFoundInCacheCollectionStream);
		report.missingRecordInCollectionStream.addAll(results.getRemovedItems());
		report.deletedRecordInCollectionStream.addAll(results.getNewItems());

		results = LangUtils.compareSorting(recordsIdsFoundInSolr, recordsIdsFoundInCacheTypeStream);
		report.missingRecordInTypeStream.addAll(results.getRemovedItems());
		report.deletedRecordInTypeStream.addAll(results.getNewItems());

		return report;
	}


	private static void addErrorIfNotEmpty(ValidationErrors validationErrors, MetadataSchemaType schemaType,
										   String errorCode, Set<String> listOfIds) {
		if (!listOfIds.isEmpty()) {
			Map<String, Object> params = new HashMap<>();
			params.put("count", listOfIds.size());
			params.put("collection", schemaType.getCollection());
			params.put("schemaType", schemaType.getCode());
			params.put("ids", StringUtils.join(listOfIds.stream().limit(20).collect(Collectors.toList())));
			validationErrors.add(RecordsCache2IntegrityDiagnosticService.class, errorCode, params);
		}
	}

}
