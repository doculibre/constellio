package com.constellio.model.services.records;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.services.records.RecordUtils.invalidateTaxonomiesCache;
import static com.constellio.model.services.records.cache.RecordsCachesUtils.evaluateCacheInsert;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.utils.MaskUtils.format;
import static java.util.Arrays.asList;
import static net.jcores.CoreKeeper.$;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException.RecordDaoRuntimeException_RecordsFlushingFailed;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.records.RecordRuntimeException;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.entities.schemas.preparationSteps.CalculateMetadatasRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.RecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.SequenceRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.UpdateCreationModificationUsersAndDateRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateCyclicReferencesRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateMetadatasRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateUsingSchemaValidatorsRecordPreparationStep;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.extensions.events.records.TransactionExecutionBeforeSaveEvent;
import com.constellio.model.frameworks.validation.DecoratedValidationsErrors;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentModifications;
import com.constellio.model.services.contents.ContentModificationsBuilder;
import com.constellio.model.services.contents.ParsedContentProvider;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.migrations.RequiredRecordMigrations;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.model.services.records.RecordServicesException.UnresolvableOptimisticLockingConflict;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.RecordServicesRuntimeException.CannotSetIdsToReindexInEmptyTransaction;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_ExceptionWhileCalculating;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_RecordsFlushingFailed;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_TransactionHasMoreThan100000Records;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution;
import com.constellio.model.services.records.RecordServicesRuntimeException.UnresolvableOptimsiticLockingCausingInfiniteLoops;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.records.populators.SearchFieldsPopulator;
import com.constellio.model.services.records.populators.SortFieldsPopulator;
import com.constellio.model.services.records.preparation.RecordsToReindexResolver;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.ModificationImpactCalculator;
import com.constellio.model.services.schemas.ModificationImpactCalculatorResponse;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.DependencyUtilsRuntimeException.CyclicDependency;

public class RecordServicesImpl extends BaseRecordServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordServicesImpl.class);

	private final RecordDao recordDao;
	private final RecordDao eventsDao;
	private final RecordDao notificationsDao;
	private final ModelLayerFactory modelFactory;
	private final UniqueIdGenerator uniqueIdGenerator;
	private final RecordsCaches recordsCaches;

	public RecordServicesImpl(RecordDao recordDao, RecordDao eventsDao, RecordDao notificationsDao,
			ModelLayerFactory modelFactory, DataStoreTypesFactory typesFactory, UniqueIdGenerator uniqueIdGenerator,
			RecordsCaches recordsCaches) {
		super(modelFactory);
		this.recordDao = recordDao;
		this.eventsDao = eventsDao;
		this.notificationsDao = notificationsDao;
		this.modelFactory = modelFactory;
		this.uniqueIdGenerator = uniqueIdGenerator;
		this.recordsCaches = recordsCaches;
	}

	public void executeWithImpactHandler(Transaction transaction, RecordModificationImpactHandler handler)
			throws RecordServicesException {
		executeWithImpactHandler(transaction, handler, 0);
	}

	public void executeWithoutImpactHandling(Transaction transaction)
			throws RecordServicesException {
		if (!transaction.getRecords().isEmpty()) {
			executeWithImpactHandler(transaction, new RecordModificationImpactHandler() {
				@Override
				public void prepareToHandle(ModificationImpact modificationImpact) {

				}

				@Override
				public void handle() {

				}

				@Override
				public void cancel() {

				}
			});
		}
	}

	public void execute(Transaction transaction)
			throws RecordServicesException {
		execute(transaction, 0);
	}

	public void execute(Transaction transaction, int attempt)
			throws RecordServicesException {

		validateNotTooMuchRecords(transaction);
		if (transaction.getRecords().isEmpty()) {
			if (!transaction.getIdsToReindex().isEmpty()) {
				throw new CannotSetIdsToReindexInEmptyTransaction();
			}
			return;
		}
		String collection = transaction.getCollection();

		MetadataSchemaTypes schemaTypes = modelFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		try {
			transaction.sortRecords(schemaTypes);
		} catch (CyclicDependency e) {
			LOGGER.info("Cyclic dependency detected, a validation error will be thrown", e);
		}
		prepareRecords(transaction);
		ModificationImpactCalculatorResponse impacts = getModificationImpacts(transaction, false);

		transaction.addAllRecordsToReindex(impacts.getRecordsToReindex());
		if (impacts.getImpacts().isEmpty()) {
			saveContentsAndRecords(transaction, null, attempt);
		} else {
			Transaction newTransaction = new Transaction(transaction);
			for (ModificationImpact impact : impacts.getImpacts()) {
				LOGGER.debug("Handling modification impact. Reindexing " + impact.getMetadataToReindex() + " for records of "
						+ impact.getLogicalSearchCondition().toString());
				LogicalSearchQuery searchQuery = new LogicalSearchQuery(impact.getLogicalSearchCondition());
				List<Record> recordsFound = modelFactory.newSearchServices().search(searchQuery);
				for (Record record : recordsFound) {
					if (!newTransaction.isContainingUpdatedRecord(record)) {
						newTransaction.addUpdate(record);
					}
				}

				newTransaction.getRecordUpdateOptions().getTransactionRecordsReindexation()
						.addReindexedMetadatas(impact.getMetadataToReindex());
			}
			execute(newTransaction);
		}

	}

	public List<BatchProcess> executeHandlingImpactsAsync(Transaction transaction)
			throws RecordServicesException {
		if (!transaction.getRecords().isEmpty()) {
			AddToBatchProcessImpactHandler handler = addToBatchProcessModificationImpactHandler();
			executeWithImpactHandler(transaction, handler);
			return handler.getAllCreatedBatchProcesses();
		} else {
			return Collections.emptyList();
		}
	}

	public void executeWithImpactHandler(Transaction transaction, RecordModificationImpactHandler handler, int attempt)
			throws RecordServicesException {

		validateNotTooMuchRecords(transaction);

		prepareRecords(transaction);
		if (handler != null) {
			ModificationImpactCalculatorResponse impacts = getModificationImpacts(transaction, true);

			for (ModificationImpact modificationImpact : impacts.getImpacts()) {
				handler.prepareToHandle(modificationImpact);
			}
			transaction.addAllRecordsToReindex(impacts.getRecordsToReindex());
		}

		saveContentsAndRecords(transaction, handler, attempt);

	}

	void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
	}

	void handleOptimisticLocking(TransactionDTO transactionDTO, Transaction transaction, RecordModificationImpactHandler handler,
			OptimisticLocking e, int attempt)
			throws RecordServicesException {

		if (attempt > 35) {
			//e.printStackTrace();
			throw new UnresolvableOptimsiticLockingCausingInfiniteLoops(transactionDTO, e);
		}

		//Will wait up to 30 seconds given 35 attempt are made
		long sleepDuration = 50 * attempt;
		if (sleepDuration > 0) {
			sleep(sleepDuration);
		}

		OptimisticLockingResolution resolution = transaction.getRecordUpdateOptions().getOptimisticLockingResolution();

		if (resolution == OptimisticLockingResolution.EXCEPTION || transaction.getModifiedRecords().isEmpty()) {
			throw new RecordServicesException.OptimisticLocking(transactionDTO, e);
		} else if (resolution == OptimisticLockingResolution.TRY_MERGE) {

			mergeRecords(transaction, e.getId());
			if (handler == null) {
				execute(transaction, attempt + 1);
			} else {
				executeWithImpactHandler(transaction, handler, attempt + 1);
			}
		}
	}

	void mergeRecordsUsingRealtimeGet(Transaction transaction, String failedId)
			throws RecordServicesException.UnresolvableOptimisticLockingConflict {

		List<String> ids = new ArrayList<>();
		for (Record record : transaction.getRecords()) {
			ids.add(record.getId());
		}
		ids.addAll(transaction.getIdsToReindex());

		List<Record> newVersions = realtimeGetRecordById(ids);

		for (String id : transaction.getIdsToReindex()) {
			Record newRecordVersion = null;
			for (Record aNewVersion : newVersions) {
				if (aNewVersion.getId().equals(id)) {
					newRecordVersion = aNewVersion;
					break;
				}
			}

			for (Record recordWithinTransaction : transaction.getRecords()) {
				if (recordWithinTransaction.getId().equals(id)) {
					newRecordVersion = recordWithinTransaction;
					break;
				}
			}

			if (newRecordVersion == null) {
				throw new RecordServicesException.UnresolvableOptimisticLockingConflict(id);
			}
		}

		for (Record record : transaction.getRecords()) {
			if (record.isSaved()) {

				try {
					Record newRecordVersion = null;
					for (Record aNewVersion : newVersions) {
						if (aNewVersion.getId().equals(record.getId())) {
							newRecordVersion = aNewVersion;
							break;
						}
					}

					if (newRecordVersion != null && record.getVersion() != newRecordVersion.getVersion()) {
						MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager()
								.getSchemaTypes(transaction.getCollection());
						MetadataSchema metadataSchema = types.getSchema(newRecordVersion.getSchemaCode());
						try {
							((RecordImpl) record).merge((RecordImpl) newRecordVersion, metadataSchema);
						} catch (RecordRuntimeException.CannotMerge e) {
							throw new UnresolvableOptimisticLockingConflict(e);
						}
					}
					if (newRecordVersion == null) {
						throw new RecordServicesException.UnresolvableOptimisticLockingConflict(record.getId());
					}
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager()
							.getSchemaTypes(transaction.getCollection());
					MetadataSchema metadataSchema = types.getSchema(record.getSchemaCode());
				}
			} else {
				try {
					realtimeGetRecordById(record.getId());
					throw new RecordServicesRuntimeException.IdAlreadyExisting(record.getId());
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					//OK
				}
			}
		}
	}

	void mergeRecords(Transaction transaction, String failedId)
			throws RecordServicesException.UnresolvableOptimisticLockingConflict {
		//mergeRecordsUsingRealtimeGet is interesting, but it make this test fail
		//com.constellio.model.services.records.RecordServicesOptimisticLockingHandlingAcceptanceTest
		mergeRecordsUsingRealtimeGet(transaction, failedId);
		//mergeRecordsUsingQuery(transaction, failedId);
	}

	void mergeRecordsUsingQuery(Transaction transaction, String failedId)
			throws RecordServicesException.UnresolvableOptimisticLockingConflict {

		List<LogicalSearchCondition> conditions = new ArrayList<>();

		for (Record record : transaction.getRecords()) {
			if (record.isSaved()) {
				conditions.add(LogicalSearchQueryOperators.where(Schemas.IDENTIFIER).isEqualTo(record.getId())
						.andWhere(Schemas.VERSION).isNotEqual(record.getVersion()));
			}
		}

		if (conditions.isEmpty()) {

			for (Record record : transaction.getRecords()) {
				if (!record.isSaved()) {
					try {
						getDocumentById(record.getId());
						throw new RecordServicesRuntimeException.IdAlreadyExisting(record.getId());
					} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
						//OK
					}
				}
			}

		}

		if (conditions.isEmpty()) {
			throw new RecordServicesException.UnresolvableOptimisticLockingConflict(failedId);
		}

		LogicalSearchCondition condition = fromAllSchemasIn(transaction.getCollection()).whereAnyCondition(conditions);

		List<Record> modifiedRecordVersions = modelFactory.newSearchServices().search(new LogicalSearchQuery(condition));

		for (Record newRecordVersion : modifiedRecordVersions) {
			Record transactionRecord = null;
			MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(transaction.getCollection());
			MetadataSchema metadataSchema = types.getSchema(newRecordVersion.getSchemaCode());
			for (Record aTransactionRecord : transaction.getRecords()) {
				if (aTransactionRecord.getId().equals(newRecordVersion.getId())) {
					transactionRecord = aTransactionRecord;
					break;
				}
			}

			try {
				((RecordImpl) transactionRecord).merge((RecordImpl) newRecordVersion, metadataSchema);
			} catch (RecordRuntimeException.CannotMerge e) {
				throw new UnresolvableOptimisticLockingConflict(e);
			}
		}

	}

	public Record toRecord(RecordDTO recordDTO, boolean allFields) {
		Record record = new RecordImpl(recordDTO, allFields);
		newAutomaticMetadataServices()
				.loadTransientEagerMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(),
						new Transaction(new RecordUpdateOptions()));
		return record;
	}

	public List<Record> toRecords(List<RecordDTO> recordDTOs, boolean allFields) {
		List<Record> records = new ArrayList<Record>();
		for (RecordDTO recordDTO : recordDTOs) {
			records.add(toRecord(recordDTO, allFields));
		}

		return Collections.unmodifiableList(records);
	}

	public long documentsCount() {
		return recordDao.documentsCount();
	}

	@Override
	public Record getRecordByMetadata(Metadata metadata, String value) {
		if (!metadata.isUniqueValue()) {
			throw new IllegalArgumentException("Metadata '" + metadata + "' is not unique");
		}
		if (metadata.getCode().startsWith("global_")) {
			throw new IllegalArgumentException("Metadata '" + metadata + "' is global, which has no specific schema type.");
		}
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(metadata.getCollection());
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(metadata);
		MetadataSchemaType schemaType = types.getSchemaType(schemaTypeCode);
		LogicalSearchCondition condition = from(schemaType).where(metadata).isEqualTo(value);

		return searchServices.searchSingleResult(condition);
	}

	public Record getDocumentById(String id) {
		return getById(DataStore.RECORDS, id);
	}

	public Record getById(MetadataSchemaType schemaType, String id) {
		return getById(schemaType.getDataStore(), id);
	}

	public Record getById(String dataStore, String id) {
		try {
			Record record = new RecordImpl(dao(dataStore).get(id), true);
			newAutomaticMetadataServices()
					.loadTransientEagerMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(),
							new Transaction(new RecordUpdateOptions()));
			insertInCache(record, WAS_OBTAINED);
			return record;

		} catch (NoSuchRecordWithId e) {
			throw new RecordServicesRuntimeException.NoSuchRecordWithId(id, dataStore, e);
		}
	}

	public Record realtimeGetRecordById(String id) {
		return realtimeGetById(DataStore.RECORDS, id);
	}

	public Record realtimeGetById(String dataStore, String id) {
		try {
			Record record = new RecordImpl(dao(dataStore).realGet(id), true);
			newAutomaticMetadataServices()
					.loadTransientEagerMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(),
							new Transaction(new RecordUpdateOptions()));
			insertInCache(record, WAS_OBTAINED);
			return record;

		} catch (NoSuchRecordWithId e) {
			throw new RecordServicesRuntimeException.NoSuchRecordWithId(id, dataStore, e);
		}
	}

	private RecordDao dao(String dataStore) {
		switch (dataStore) {
		case DataStore.RECORDS:
			return recordDao;
		case DataStore.EVENTS:
			return eventsDao;

		default:
			throw new ImpossibleRuntimeException("Unsupported datastore : " + dataStore);
		}
	}

	public Record realtimeGetById(MetadataSchemaType schemaType, String id) {
		return realtimeGetById(schemaType.getDataStore(), id);
	}

	public List<Record> realtimeGetRecordById(List<String> ids) {

		List<Record> records = new ArrayList<>();
		for (RecordDTO recordDTO : recordDao.realGet(ids)) {
			Record record = new RecordImpl(recordDTO, true);
			newAutomaticMetadataServices()
					.loadTransientEagerMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(),
							new Transaction(new RecordUpdateOptions()));
			insertInCache(record, WAS_OBTAINED);
			records.add(record);
		}

		return records;

	}

	public void insertInCache(Record record, InsertionReason reason) {
		insertInCache(record.getCollection(), asList(record), reason);
	}

	public void insertInCache(String collection, List<Record> records, InsertionReason reason) {

		List<Record> insertedRecords = new ArrayList<>();
		for (Record record : records) {
			CacheConfig cacheConfig = recordsCaches.getCache(collection).getCacheConfigOf(record.getTypeCode());
			if (cacheConfig != null) {
				if (evaluateCacheInsert(record, cacheConfig) != CacheInsertionStatus.ACCEPTED && cacheConfig.isPermanent()) {
					insertedRecords.add(realtimeGetRecordById(record.getId()));
				} else {
					insertedRecords.add(record);
				}
			}
		}
		recordsCaches.insert(collection, insertedRecords, reason);

	}

	public List<Record> getRecordsById(String collection, List<String> ids) {

		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(collection).where(Schemas.IDENTIFIER).isIn(ids));
		return modelFactory.newSearchServices().search(query);
	}

	void prepareRecords(Transaction transaction)
			throws RecordServicesException.ValidationException {
		prepareRecords(transaction, null);
	}

	void prepareRecords(final Transaction transaction, String onlyValidateRecord)
			throws RecordServicesException.ValidationException {

		TransactionExecutionContext context = new TransactionExecutionContext();
		RecordPopulateServices recordPopulateServices = modelLayerFactory.newRecordPopulateServices();
		RecordProvider recordProvider = newRecordProvider(null, transaction);
		RecordValidationServices validationServices = newRecordValidationServices(recordProvider);
		RecordAutomaticMetadataServices automaticMetadataServices = newAutomaticMetadataServices();
		TransactionRecordsReindexation reindexation = transaction.getRecordUpdateOptions().getTransactionRecordsReindexation();
		MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(transaction.getCollection());
		RecordUpdateOptions options = transaction.getRecordUpdateOptions();
		if (transaction.getRecordUpdateOptions().getRecordsFlushing() != RecordsFlushing.NOW()) {
			//			RecordsCache cache = recordsCaches.getCache(transaction.getCollection());
			//			for (Record record : transaction.getRecords()) {
			//				if (record.isDirty() && record.isSaved() && cache.getCacheConfigOf(record.getSchemaCode()) != null) {
			//					throw new RecordServicesRuntimeException_CannotDelayFlushingOfRecordsInCache(record.getSchemaCode(),
			//							record.getId());
			//				}
			//			}
		}

		for (Record record : transaction.getRecords()) {
			MetadataSchemaType schemaType = types.getSchemaType(record.getTypeCode());
			if (schemaType.isReadOnlyLocked() && !options.isAllowSchemaTypeLockedRecordsModification()) {
				throw new RecordServicesRuntimeException.SchemaTypeOfARecordHasReadOnlyLock(record.getTypeCode(), record.getId());
			}
		}

		for (Record record : transaction.getRecords()) {
			if (record.get(Schemas.MIGRATION_DATA_VERSION) == null) {
				if (record.isSaved()) {
					record.set(Schemas.MIGRATION_DATA_VERSION, 0);
				} else {
					record.set(Schemas.MIGRATION_DATA_VERSION, modelLayerFactory.getRecordMigrationsManager()
							.getCurrentDataVersion(record.getCollection(), record.getTypeCode()));
				}

			}

		}

		ModelLayerCollectionExtensions extensions = modelFactory.getExtensions().forCollection(transaction.getCollection());
		for (Record record : transaction.getRecords()) {
			if (record.isDirty()) {
				if (record.isSaved()) {
					MetadataList modifiedMetadatas = record.getModifiedMetadatas(types);
					extensions.callRecordInModificationBeforeValidationAndAutomaticValuesCalculation(
							new RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent(record,
									modifiedMetadatas), options);
				} else {
					extensions.callRecordInCreationBeforeValidationAndAutomaticValuesCalculation(
							new RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent(
									record, transaction.getUser()), options);
				}
			}
		}

		boolean validations = transaction.getRecordUpdateOptions().isValidationsEnabled();
		//List<Record> records = RecordUtils.sortRecordByDependency(types, transaction.getRecords());
		//List<Record> records = DependencyUtils.sortRecordByDependency(types, transaction.getRecords());
		ParsedContentProvider parsedContentProvider = new ParsedContentProvider(modelFactory.getContentManager(),
				transaction.getParsedContentCache());
		for (Record record : transaction.getRecords()) {
			recordPopulateServices.populate(record, parsedContentProvider);

			MetadataSchema schema = types.getSchema(record.getSchemaCode());

			if (onlyValidateRecord == null || onlyValidateRecord.equals(record.getId())) {

				for (RecordPreparationStep step : schema.getPreparationSteps()) {

					if (step instanceof CalculateMetadatasRecordPreparationStep) {
						TransactionRecordsReindexation reindexationOptionForThisRecord = reindexation;
						RequiredRecordMigrations migrations =
								modelLayerFactory.getRecordMigrationsManager().getRecordMigrationsFor(record);

						if (!migrations.getScripts().isEmpty()) {

							for (RecordMigrationScript script : migrations.getScripts()) {
								script.migrate(record);
							}
							record.set(Schemas.MIGRATION_DATA_VERSION, migrations.getVersion());

							reindexationOptionForThisRecord = TransactionRecordsReindexation.ALL();
						}

						try {
							for (Metadata metadata : step.getMetadatas()) {
								automaticMetadataServices
										.updateAutomaticMetadata(context, (RecordImpl) record, recordProvider, metadata,
												reindexationOptionForThisRecord, types, transaction);
							}
						} catch (RuntimeException e) {
							throw new RecordServicesRuntimeException_ExceptionWhileCalculating(record.getId(), e);
						}
						validationServices.validateAccess(record, transaction);
					} else if (step instanceof UpdateCreationModificationUsersAndDateRecordPreparationStep) {
						if (transaction.getRecordUpdateOptions().isUpdateModificationInfos()) {
							updateCreationModificationUsersAndDates(record, transaction, types.getSchema(record.getSchemaCode()));
						}

					} else if (step instanceof SequenceRecordPreparationStep) {
						SequencesManager sequencesManager = modelFactory.getDataLayerFactory().getSequencesManager();
						for (Metadata metadata : step.getMetadatas()) {

							SequenceDataEntry dataEntry = (SequenceDataEntry) metadata.getDataEntry();
							if (dataEntry.getFixedSequenceCode() != null) {
								if (record.get(metadata) == null) {
									String sequenceCode = dataEntry.getFixedSequenceCode();
									String value = format(metadata.getInputMask(), "" + sequencesManager.next(sequenceCode));
									record.set(metadata, sequenceCode == null ? null : value);
								}
							} else {
								Metadata metadataProvidingSequenceCode = schema
										.getMetadata(dataEntry.getMetadataProvidingSequenceCode());

								if (record.isModified(metadataProvidingSequenceCode) && !record.isModified(metadata)) {
									String sequenceCode = record.get(metadataProvidingSequenceCode);
									String value = sequenceCode == null ?
											null :
											format(metadata.getInputMask(), "" + sequencesManager.next(sequenceCode));
									record.set(metadata, sequenceCode == null ? null : value);
								}
							}

						}

						if (validations) {
							validationServices.validateCyclicReferences(record, recordProvider, types, schema.getMetadatas());
						}

					} else if (step instanceof ValidateCyclicReferencesRecordPreparationStep) {
						if (validations) {
							validationServices.validateCyclicReferences(record, recordProvider, types, schema.getMetadatas());
						}

					} else if (step instanceof ValidateMetadatasRecordPreparationStep) {
						if (validations) {
							validationServices.validateMetadatas(record, recordProvider, transaction, step.getMetadatas());
						}

					} else if (step instanceof ValidateUsingSchemaValidatorsRecordPreparationStep) {
						if (validations) {
							validationServices.validateSchemaUsingCustomSchemaValidator(record, recordProvider, transaction);
						}
					}
				}

				if (transaction.getRecordUpdateOptions().getTransactionRecordsReindexation().isReindexAll()
						&& transaction.getRecordUpdateOptions().isUpdateAggregatedMetadatas()
						&& schema.hasMetadataWithCode(Schemas.MARKED_FOR_REINDEXING.getLocalCode())
						&& !record.isModified(Schemas.MARKED_FOR_REINDEXING)
						&& !transaction.getIdsToReindex().contains(record.getId())) {
					record.set(Schemas.MARKED_FOR_REINDEXING, null);
				}
			}

			boolean allParsed = true;
			for (Metadata contentMetadata : schema.getContentMetadatasForPopulate()) {
				for (Content aContent : record.<Content>getValues(contentMetadata)) {
					allParsed &= parsedContentProvider
							.getParsedContentIfAlreadyParsed(aContent.getCurrentVersion().getHash()) != null;
				}
			}

			if (allParsed) {
				record.set(Schemas.MARKED_FOR_PARSING, null);
			} else {
				record.set(Schemas.MARKED_FOR_PARSING, true);
			}

		}

		if (!transaction.getRecordUpdateOptions().isSkipFindingRecordsToReindex()) {
			new RecordsToReindexResolver(types).findRecordsToReindex(transaction);
		}

		ValidationErrors errors = new ValidationErrors();
		boolean singleRecordTransaction = transaction.getRecords().size() == 1;

		boolean catchValidationsErrors = transaction.getRecordUpdateOptions().isCatchExtensionsValidationsErrors();

		ValidationErrors transactionExtensionErrors =
				catchValidationsErrors ? new ValidationErrors() : new DecoratedValidationsErrors(errors);

		extensions.callTransactionExecutionBeforeSave(
				new TransactionExecutionBeforeSaveEvent(transaction, transactionExtensionErrors), options);
		if (catchValidationsErrors && !transactionExtensionErrors.isEmptyErrorAndWarnings()) {
			LOGGER.warn("Validating errors added by extensions : \n" + $(transactionExtensionErrors));
		}

		for (final Record record : transaction.getRecords()) {
			if (record.isDirty()) {
				ValidationErrors recordErrors =
						catchValidationsErrors ? new ValidationErrors() : new DecoratedValidationsErrors(errors);
				if (record.isSaved()) {
					MetadataList modifiedMetadatas = record.getModifiedMetadatas(types);
					extensions.callRecordInModificationBeforeSave(
							new RecordInModificationBeforeSaveEvent(record, modifiedMetadatas, singleRecordTransaction,
									recordErrors), options);
				} else {
					extensions.callRecordInCreationBeforeSave(new RecordInCreationBeforeSaveEvent(
							record, transaction.getUser(), singleRecordTransaction, recordErrors) {
						@Override
						public void recalculateRecord() {
							newAutomaticMetadataServices().updateAutomaticMetadatas(
									(RecordImpl) record, newRecordProvider(transaction),
									TransactionRecordsReindexation.ALL(),
									transaction);
						}
					}, options);
				}

				if (catchValidationsErrors && !recordErrors.isEmptyErrorAndWarnings()) {
					LOGGER.warn("Validating errors added by extensions : \n" + $(recordErrors));
				}
			}
		}
		if (!errors.isEmpty()) {
			throw new RecordServicesException.ValidationException(transaction, errors);
		}

	}

	public void validateRecordInTransaction(Record record, Transaction transaction)
			throws ValidationException {
		if (transaction.getRecords().isEmpty()) {
			validateRecord(record);
		} else {
			prepareRecords(transaction, record.getId());
		}
	}

	public void validateRecord(Record record)
			throws RecordServicesException.ValidationException {

		Transaction transaction = new Transaction(record);
		prepareRecords(transaction);
	}

	private void updateCreationModificationUsersAndDates(Record record, Transaction transaction, MetadataSchema metadataSchema) {
		String type = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		boolean userGroupOrCollection = User.SCHEMA_TYPE.equals(type) || Group.SCHEMA_TYPE.equals(type)
				|| Collection.SCHEMA_TYPE.equals(type);

		LocalDateTime now = TimeProvider.getLocalDateTime();
		String currentUserId = transaction.getUser() == null ? null : transaction.getUser().getId();
		if (!record.isSaved()) {
			boolean hasCreatedByMetadata = metadataSchema.hasMetadataWithCode(Schemas.CREATED_BY.getLocalCode());
			if (!record.isModified(Schemas.CREATED_BY) && !userGroupOrCollection && hasCreatedByMetadata) {
				record.set(Schemas.CREATED_BY, currentUserId);
			}
			if (!record.isModified(Schemas.CREATED_ON)) {
				record.set(Schemas.CREATED_ON, now);
			}
			boolean hasModifiedByMetadata = metadataSchema.hasMetadataWithCode(Schemas.MODIFIED_BY.getLocalCode());
			if (!record.isModified(Schemas.MODIFIED_BY) && !userGroupOrCollection && hasModifiedByMetadata) {
				record.set(Schemas.MODIFIED_BY, record.get(Schemas.CREATED_BY));
			}
			if (!record.isModified(Schemas.MODIFIED_ON)) {
				record.set(Schemas.MODIFIED_ON, record.get(Schemas.CREATED_ON));
			}
		} else {
			boolean overwriteModificationDateAndUser = transaction.getRecordUpdateOptions().isOverwriteModificationDateAndUser();
			if (!record.isModified(Schemas.MODIFIED_BY) && !userGroupOrCollection && overwriteModificationDateAndUser) {
				record.set(Schemas.MODIFIED_BY, currentUserId);
			}
			if (!record.isModified(Schemas.MODIFIED_ON) && overwriteModificationDateAndUser) {
				record.set(Schemas.MODIFIED_ON, now);
			}
		}

	}

	ModificationImpactCalculatorResponse getModificationImpacts(Transaction transaction, boolean executedAfterTransaction) {
		SearchServices searchServices = modelFactory.newSearchServices();
		TaxonomiesManager taxonomiesManager = modelFactory.getTaxonomiesManager();
		MetadataSchemaTypes metadataSchemaTypes = modelFactory.getMetadataSchemasManager().getSchemaTypes(
				transaction.getCollection());

		return calculateImpactOfModification(transaction, taxonomiesManager, searchServices, metadataSchemaTypes,
				executedAfterTransaction);
	}

	void refreshRecordsAndCaches(String collection, List<Record> records, TransactionResponseDTO transactionResponseDTO,
			MetadataSchemaTypes types, RecordProvider recordProvider) {

		invalidateTaxonomiesCache(records, types, recordProvider, modelLayerFactory.getTaxonomiesSearchServicesCache());

		List<Record> recordsToInsert = new ArrayList<>();
		for (Record record : records) {
			RecordImpl recordImpl = (RecordImpl) record;
			if (transactionResponseDTO != null) {
				Long version = transactionResponseDTO.getNewDocumentVersion(record.getId());
				if (version != null) {

					MetadataSchema schema = types.getSchema(record.getSchemaCode());

					recordImpl.markAsSaved(version, schema);
					recordsToInsert.add(record);
				}
			}
		}
		insertInCache(collection, recordsToInsert, WAS_MODIFIED);

	}

	void saveContentsAndRecords(Transaction transaction, RecordModificationImpactHandler modificationImpactHandler, int attempt)
			throws RecordServicesException {
		ContentManager contentManager = modelFactory.getContentManager();
		MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(transaction.getCollection());
		ContentModifications contentModificationsBuilder = findContentsModificationsIn(types, transaction);

		try {
			for (String deletedContent : contentModificationsBuilder.getDeletedContentsVersionsHashes()) {
				contentManager.silentlyMarkForDeletionIfNotReferenced(deletedContent);
			}
			saveTransactionDTO(transaction, modificationImpactHandler, attempt);

		} catch (RecordServicesException | RecordServicesRuntimeException e) {
			for (String newContent : contentModificationsBuilder.getContentsWithNewVersion()) {
				contentManager.silentlyMarkForDeletionIfNotReferenced(newContent);
			}
			throw e;
		}

	}

	ContentModifications findContentsModificationsIn(MetadataSchemaTypes types, Transaction transaction) {
		return new ContentModificationsBuilder(types).buildForModifiedRecords(transaction.getRecords());
	}

	void saveTransactionDTO(Transaction transaction, RecordModificationImpactHandler modificationImpactHandler, int attempt)
			throws RecordServicesException {

		List<Record> modifiedOrUnsavedRecords = transaction.getModifiedRecords();
		if (!modifiedOrUnsavedRecords.isEmpty() || !transaction.getIdsToReindex().isEmpty()) {
			Map<String, TransactionDTO> transactionDTOs = createTransactionDTOs(transaction, modifiedOrUnsavedRecords);
			for (Map.Entry<String, TransactionDTO> transactionDTOEntry : transactionDTOs.entrySet()) {
				try {
					MetadataSchemaTypes metadataSchemaTypes = modelFactory.getMetadataSchemasManager().getSchemaTypes(
							transaction.getCollection());

					List<RecordEvent> recordEvents = prepareRecordEvents(modifiedOrUnsavedRecords, metadataSchemaTypes);

					TransactionResponseDTO transactionResponseDTO;
					if (transactionDTOEntry.getKey().equals(DataStore.RECORDS)) {
						transactionResponseDTO = recordDao.execute(transactionDTOEntry.getValue());
						modelFactory.newLoggingServices().logTransaction(transaction);

					} else if (transactionDTOEntry.getKey().equals(DataStore.EVENTS)) {
						transactionResponseDTO = eventsDao.execute(transactionDTOEntry.getValue());

					} else {
						throw new ImpossibleRuntimeException("Unsupported datastore : " + transactionDTOEntry.getKey());
					}

					refreshRecordsAndCaches(transaction.getCollection(), modifiedOrUnsavedRecords, transactionResponseDTO,
							metadataSchemaTypes, newRecordProvider(null, transaction));

					if (modificationImpactHandler != null) {
						modificationImpactHandler.handle();
					}

					callExtensions(transaction.getCollection(), recordEvents, transaction.getRecordUpdateOptions());

				} catch (OptimisticLocking e) {
					if (modificationImpactHandler != null) {
						modificationImpactHandler.cancel();
					}
					LOGGER.trace("Optimistic locking, handling with specified resolution {}", transaction.getRecordUpdateOptions()
							.getOptimisticLockingResolution().name(), e);
					handleOptimisticLocking(transactionDTOEntry.getValue(), transaction, modificationImpactHandler, e, attempt);
				}
			}
		}
	}

	private List<RecordEvent> prepareRecordEvents(List<Record> modifiedOrUnsavedRecords, MetadataSchemaTypes types) {
		List<RecordEvent> events = new ArrayList<>();

		for (Record record : modifiedOrUnsavedRecords) {
			if (record.isSaved()) {

				if (record.isModified(Schemas.LOGICALLY_DELETED_STATUS)) {
					if (LangUtils.isFalseOrNull(record.get(Schemas.LOGICALLY_DELETED_STATUS))) {
						events.add(new RecordRestorationEvent(record));
					} else {
						events.add(new RecordLogicalDeletionEvent(record));
					}
				} else {

					MetadataList modifiedMetadatas = record.getModifiedMetadatas(types);
					events.add(
							new RecordModificationEvent(record, modifiedMetadatas));
				}

			} else {
				events.add(new RecordCreationEvent(record));
			}
		}

		return events;
	}

	private void callExtensions(String collection, List<RecordEvent> recordEvents, RecordUpdateOptions options) {
		ModelLayerCollectionExtensions extensions = modelFactory.getExtensions().forCollection(collection);

		for (RecordEvent recordEvent : recordEvents) {
			if (recordEvent instanceof RecordCreationEvent) {
				extensions.callRecordCreated((RecordCreationEvent) recordEvent, options);

			} else if (recordEvent instanceof RecordModificationEvent) {
				extensions.callRecordModified((RecordModificationEvent) recordEvent, options);

			} else if (recordEvent instanceof RecordLogicalDeletionEvent) {
				extensions.callRecordLogicallyDeleted((RecordLogicalDeletionEvent) recordEvent);

			} else if (recordEvent instanceof RecordRestorationEvent) {
				extensions.callRecordRestored((RecordRestorationEvent) recordEvent);
			}
		}

	}

	Map<String, TransactionDTO> createTransactionDTOs(Transaction transaction, List<Record> modifiedOrUnsavedRecords) {
		Map<String, TransactionDTO> transactions = new HashMap<>();
		RecordUpdateOptions options = transaction.getRecordUpdateOptions();
		for (String dataStore : asList("records", "events")) {
			String collection = transaction.getCollection();
			List<RecordDTO> addedRecords = new ArrayList<>();
			List<RecordDeltaDTO> modifiedRecordDTOs = new ArrayList<>();
			LanguageDetectionManager languageDetectionManager = modelFactory.getLanguageDetectionManager();
			ContentManager contentManager = modelFactory.getContentManager();
			List<String> collectionLanguages = modelFactory.getCollectionsListManager().getCollectionLanguages(collection);
			List<FieldsPopulator> fieldsPopulators = new ArrayList<>();
			MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			ConstellioEIMConfigs systemConfigs = modelFactory.getSystemConfigs();
			ParsedContentProvider parsedContentProvider = new ParsedContentProvider(contentManager,
					transaction.getParsedContentCache());

			fieldsPopulators
					.add(new SearchFieldsPopulator(types, options.isFullRewrite(), parsedContentProvider, collectionLanguages, systemConfigs));
			fieldsPopulators.add(new SortFieldsPopulator(types, options.isFullRewrite(), modelFactory));

			Factory<EncryptionServices> encryptionServicesFactory = new Factory<EncryptionServices>() {
				@Override
				public EncryptionServices get() {
					return modelLayerFactory.newEncryptionServices();
				}
			};

			List<String> ids = transaction.getRecordIds();
			Set<String> markedForReindexing = new HashSet<>();
			for (String id : transaction.getIdsToReindex()) {
				if (!ids.contains(id)) {
					markedForReindexing.add(id);
				} else {
					transaction.getRecord(id).set(Schemas.MARKED_FOR_REINDEXING, true);
				}
			}

			for (Record record : modifiedOrUnsavedRecords) {
				MetadataSchemaType type = modelFactory.getMetadataSchemasManager().getSchemaTypeOf(record);
				MetadataSchema schema = type.getSchema(record.getSchemaCode());
				if (dataStore.equals(type.getDataStore())) {
					if (!record.isSaved()) {
						addedRecords.add(((RecordImpl) record).toNewDocumentDTO(schema, fieldsPopulators));
					} else {
						RecordImpl recordImpl = (RecordImpl) record;
						if (recordImpl.isDirty() && !transaction.getRecordUpdateOptions().isFullRewrite()) {
							modifiedRecordDTOs.add(recordImpl.toRecordDeltaDTO(schema, fieldsPopulators));
						} else if (transaction.getRecordUpdateOptions().isFullRewrite()) {
							addedRecords.add(((RecordImpl) record).toDocumentDTO(schema, fieldsPopulators));
						}
					}
				}
			}

			boolean isSupportingReindexing = "records".equals(dataStore);

			boolean isChangingSomething = !addedRecords.isEmpty() || !modifiedRecordDTOs.isEmpty() ||
					(isSupportingReindexing && !markedForReindexing.isEmpty());

			if (isChangingSomething) {
				TransactionDTO datastoreTransaction = new TransactionDTO(transaction.getId(), options.getRecordsFlushing(),
						addedRecords, modifiedRecordDTOs).withSkippingReferenceToLogicallyDeletedValidation(
						transaction.isSkippingReferenceToLogicallyDeletedValidation()).withFullRewrite(options.isFullRewrite());

				if (isSupportingReindexing && !markedForReindexing.isEmpty()) {
					datastoreTransaction = datastoreTransaction.withMarkedForReindexing(markedForReindexing);
				}
				transactions.put(dataStore, datastoreTransaction);
			}
		}

		return transactions;
	}

	public Record newRecordWithSchema(MetadataSchema schema, String id) {
		Record record = new RecordImpl(schema.getCode(), schema.getCollection(), id);

		for (Metadata metadata : schema.getMetadatas().onlyWithDefaultValue().onlyManuals()) {

			if (metadata.isMultivalue()) {
				List<Object> values = new ArrayList<>();
				values.addAll((List) metadata.getDefaultValue());
				record.set(metadata, values);
			} else {
				record.set(metadata, metadata.getDefaultValue());
			}
		}

		return record;
	}

	public Record newRecordWithSchema(MetadataSchema schema) {
		String id;
		if ("collection_default".equals(schema.getCode())) {
			id = schema.getCollection();

		} else if (DataStore.EVENTS.equals(schema.getDataStore())) {
			id = UUIDV1Generator.newRandomId();

		} else if (!schema.isInTransactionLog()) {
			id = uniqueIdGenerator.next() + "ZZ";

		} else {
			id = uniqueIdGenerator.next();
		}
		return newRecordWithSchema(schema, id);
	}

	public RecordAutomaticMetadataServices newAutomaticMetadataServices() {
		return new RecordAutomaticMetadataServices(modelFactory);
	}

	public RecordValidationServices newRecordValidationServices(RecordProvider recordProvider) {
		return new RecordValidationServices(newConfigProvider(), recordProvider, modelFactory.getMetadataSchemasManager(),
				modelFactory.newSearchServices(), modelFactory.newAuthorizationsServices());
	}

	public ConfigProvider newConfigProvider() {
		return new ConfigProvider() {

			@Override
			public <T> T get(SystemConfiguration config) {
				return modelFactory.getSystemConfigurationsManager().getValue(config);
			}
		};
	}

	public RecordDeleteServices newRecordDeleteServices() {
		return new RecordDeleteServices(recordDao, modelFactory);
	}

	public AddToBatchProcessImpactHandler addToBatchProcessModificationImpactHandler() {
		return new AddToBatchProcessImpactHandler(modelFactory.getBatchProcessesManager(), modelFactory.newSearchServices());
	}

	public void refresh(List<?> records) {
		for (Object item : records) {
			Record record;

			if (item instanceof Record) {
				record = (Record) item;
			} else {
				record = ((RecordWrapper) item).getWrappedRecord();
			}

			if (record != null && record.isSaved()) {
				try {
					RecordDTO recordDTO = recordDao.get(record.getId());
					((RecordImpl) record).refresh(recordDTO.getVersion(), recordDTO);
				} catch (NoSuchRecordWithId noSuchRecordWithId) {
					LOGGER.debug("Deleted record is disconnected");
					((RecordImpl) record).markAsDisconnected();
				}

			}
		}
	}

	@Override
	public void refreshUsingCache(List<?> records) {
		for (Object item : records) {
			Record record;

			if (item instanceof Record) {
				record = (Record) item;
			} else {
				record = ((RecordWrapper) item).getWrappedRecord();
			}

			if (record != null && record.isSaved()) {

				try {
					Record recordFromCache = recordsCaches.getRecord(record.getId());

					if (recordFromCache == null) {
						recordFromCache = getDocumentById(record.getId());
					}
					RecordDTO recordDTO = ((RecordImpl) recordFromCache).getRecordDTO();
					((RecordImpl) record).refresh(recordDTO.getVersion(), recordDTO);
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					LOGGER.debug("Deleted record is disconnected");
					((RecordImpl) record).markAsDisconnected();
				}
			}
		}
		//refresh(records);
	}

	RecordProvider newRecordProvider(RecordProvider nestedProvider, Transaction transaction) {
		return new RecordProvider(modelLayerFactory.newRecordServices(), nestedProvider, transaction.getRecords(), transaction);
	}

	RecordProvider newRecordProvider(Transaction transaction) {
		return new RecordProvider(modelLayerFactory.newRecordServices(), null, transaction.getRecords(), transaction);
	}

	RecordProvider newRecordProviderWithoutPreloadedRecords() {
		return new RecordProvider(modelLayerFactory.newRecordServices(), null, null, null);
	}

	public final List<String> getRecordTitles(String collection, List<String> recordIds) {
		List<String> recordTitles = new ArrayList<>();
		if (recordIds.isEmpty()) {
			return new ArrayList<>();
		}
		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(Schemas.IDENTIFIER).isIn(recordIds);
		LogicalSearchQuery query = new LogicalSearchQuery(condition)
				.setReturnedMetadatas(ReturnedMetadatasFilter.idVersionSchemaTitle());
		List<Record> records = modelFactory.newSearchServices().search(query);
		for (Record record : records) {
			recordTitles.add((String) record.get(Schemas.TITLE));
		}

		return recordTitles;
	}

	RecordUtils newRecordUtils() {
		return new RecordUtils();
	}

	public ModificationImpactCalculatorResponse calculateImpactOfModification(Transaction transaction,
			TaxonomiesManager taxonomiesManager,
			SearchServices searchServices, MetadataSchemaTypes metadataSchemaTypes, boolean executedAfterTransaction) {

		if (transaction.getRecords().isEmpty()) {
			return new ModificationImpactCalculatorResponse(new ArrayList<ModificationImpact>(), new ArrayList<String>());
		}

		ModificationImpactCalculator calculator = newModificationImpactCalculator(taxonomiesManager, metadataSchemaTypes,
				searchServices);

		return calculator.findTransactionImpact(transaction, executedAfterTransaction);
	}

	@Override
	public RecordsCaches getRecordsCaches() {
		return recordsCaches;
	}

	public ModificationImpactCalculator newModificationImpactCalculator(TaxonomiesManager taxonomiesManager,
			MetadataSchemaTypes metadataSchemaTypes, SearchServices searchServices) {
		List<Taxonomy> taxonomies = taxonomiesManager.getEnabledTaxonomies(metadataSchemaTypes.getCollection());
		return new ModificationImpactCalculator(metadataSchemaTypes, taxonomies, searchServices, this);

	}

	public boolean isRestorable(Record record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().isRestorable(record, user);
	}

	public void restore(Record record, User user) {

		refreshUsingCache(record);
		refreshUsingCache(user);
		newRecordDeleteServices().restore(record, user);
	}

	public boolean isPhysicallyDeletable(Record record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().isPhysicallyDeletable(record, user);
	}

	public boolean isPhysicallyDeletable(Record record, User user, RecordPhysicalDeleteOptions options) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().isPhysicallyDeletable(record, user, options);
	}

	public void physicallyDelete(Record record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		newRecordDeleteServices().physicallyDelete(record, user);
	}

	public void physicallyDeleteNoMatterTheStatus(Record record, User user, RecordPhysicalDeleteOptions options) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		newRecordDeleteServices().physicallyDeleteNoMatterTheStatus(record, user, options);
	}

	public void physicallyDelete(Record record, User user, RecordPhysicalDeleteOptions options) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		newRecordDeleteServices().physicallyDelete(record, user, options);
	}

	public boolean isLogicallyDeletable(Record record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().isLogicallyDeletable(record, user);
	}

	public boolean isLogicallyThenPhysicallyDeletable(Record record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().isLogicallyThenPhysicallyDeletable(record, user);
	}

	public boolean isLogicallyThenPhysicallyDeletable(Record record, User user, RecordPhysicalDeleteOptions options) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().isLogicallyThenPhysicallyDeletable(record, user, options);
	}

	public boolean isPrincipalConceptLogicallyDeletableExcludingContent(Record record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().isPrincipalConceptLogicallyDeletableExcludingContent(record, user);
	}

	public void logicallyDelete(Record record, User user) {
		logicallyDelete(record, user, new RecordLogicalDeleteOptions());
	}

	public void logicallyDelete(Record record, User user, RecordLogicalDeleteOptions options) {

		if (!options.isSkipRefresh()) {
			refreshUsingCache(record);
			refreshUsingCache(user);
		}

		newRecordDeleteServices().logicallyDelete(record, user, options);

		refreshUsingCache(record);
	}

	public List<Record> getVisibleRecordsWithReferenceTo(Record record, User user) {

		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().getVisibleRecordsWithReferenceToRecordInHierarchy(record, user,
				newRecordDeleteServices().loadRecordsHierarchyOf(record));
	}

	public boolean isReferencedByOtherRecords(Record record) {
		return newRecordDeleteServices().isReferencedByOtherRecords(record,
				newRecordDeleteServices().loadRecordsHierarchyOf(record));
	}

	private void validateNotTooMuchRecords(Transaction transaction) {
		if (transaction.getRecords().size() > 100000) {
			throw new RecordServicesRuntimeException_TransactionHasMoreThan100000Records(transaction.getRecords().size());

		} else if (transaction.getRecords().size() > 1000) {
			RecordUpdateOptions recordUpdateOptions = transaction.getRecordUpdateOptions();
			if (recordUpdateOptions.getOptimisticLockingResolution() == OptimisticLockingResolution.TRY_MERGE) {
				throw new RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution();
			}
		}

	}

	public void flush() {
		try {
			recordDao.flush();
			eventsDao.flush();
			notificationsDao.flush();
		} catch (RecordDaoRuntimeException_RecordsFlushingFailed e) {
			throw new RecordServicesRuntimeException_RecordsFlushingFailed(e);
		}
	}

	public void flushRecords() {
		try {
			recordDao.flush();
		} catch (RecordDaoRuntimeException_RecordsFlushingFailed e) {
			throw new RecordServicesRuntimeException_RecordsFlushingFailed(e);
		}
	}

	public void removeOldLocks() {
		recordDao.removeOldLocks();
	}

	public void recalculate(RecordWrapper recordWrapper) {
		recalculate(recordWrapper.getWrappedRecord());
	}

	public void recalculate(Record record) {
		newAutomaticMetadataServices().updateAutomaticMetadatas(
				(RecordImpl) record, newRecordProviderWithoutPreloadedRecords(), TransactionRecordsReindexation.ALL(),
				new Transaction(new RecordUpdateOptions()));
	}

	@Override
	public void loadLazyTransientMetadatas(Record record) {
		newAutomaticMetadataServices()
				.loadTransientLazyMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(),
						new Transaction(new RecordUpdateOptions()));
	}

	@Override
	public void reloadEagerTransientMetadatas(Record record) {
		newAutomaticMetadataServices()
				.loadTransientEagerMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(),
						new Transaction(new RecordUpdateOptions()));
	}
}
