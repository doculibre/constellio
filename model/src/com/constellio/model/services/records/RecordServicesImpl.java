package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
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
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CollectionInfo;
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
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.entities.schemas.preparationSteps.CalculateMetadatasRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.RecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.SequenceRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.UpdateCreationModificationUsersAndDateRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateCyclicReferencesRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateMetadatasRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateUsingSchemaValidatorsRecordPreparationStep;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordReindexationEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.extensions.events.records.TransactionExecutedEvent;
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
import com.constellio.model.services.records.preparation.AggregatedMetadataIncrementation;
import com.constellio.model.services.records.preparation.RecordsLinksResolver;
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
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.constellio.data.dao.dto.records.RecordDTOMode.CUSTOM;
import static com.constellio.data.dao.dto.records.RecordDTOMode.SUMMARY;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.services.records.RecordUtils.invalidateTaxonomiesCache;
import static com.constellio.model.services.records.RecordUtils.toPersistedSummaryRecordDTO;
import static com.constellio.model.services.records.cache.RecordsCachesUtils.evaluateCacheInsert;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.utils.MaskUtils.format;
import static java.util.Arrays.asList;
import static net.jcores.CoreKeeper.$;

public class RecordServicesImpl extends BaseRecordServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordServicesImpl.class);

	private final RecordDao recordDao;
	private final RecordDao eventsDao;
	private final RecordDao notificationsDao;
	private final ModelLayerFactory modelFactory;
	private final UniqueIdGenerator uniqueIdGenerator;
	private final RecordsCaches recordsCaches;

	public RecordServicesImpl(RecordDao recordDao, RecordDao eventsDao, RecordDao notificationsDao,
							  ModelLayerFactory modelFactory, DataStoreTypesFactory typesFactory,
							  UniqueIdGenerator uniqueIdGenerator,
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
		executeWithImpactHandler(transaction, handler, false, 0);
	}

	@Override
	public <T extends Supplier<Record>> void update(Stream<T> stream, Consumer<T> action)
			throws RecordServicesException {
		this.update(stream, action, RecordUpdateOptions.userModificationsSafeOptions());
	}

	@Override
	public <T extends Supplier<Record>> void update(Stream<T> stream, Consumer<T> action, RecordUpdateOptions options)
			throws RecordServicesException {
		Transaction tx = new Transaction(options);

		Iterator<T> recordsIterator = stream.iterator();

		while (recordsIterator.hasNext()) {
			T recordSupplier = recordsIterator.next();
			action.accept(recordSupplier);

			Record record = recordSupplier.get();
			if (record.isDirty()) {
				tx.add(record);

				//TODO Use modification size and memory configs
				if (tx.getRecordCount() > 100) {
					execute(tx);
					tx = new Transaction(options);
				}

			}
		}

		if (tx.getRecordCount() > 0) {
			execute(tx);
		}

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

	public void executeInBatch(Transaction transaction)
			throws RecordServicesException {
		int size = transaction.getRecords().size();
		if (size > 1000) {
			for (int i = 0; i < size; i = i + 1000) {
				Transaction embeddedTransaction = new Transaction();
				embeddedTransaction.setOptions(transaction.getRecordUpdateOptions());
				embeddedTransaction.addAll(transaction.getRecords().subList(i, Math.min(i + 1000, size)));
				execute(embeddedTransaction);
			}
		} else {
			execute(transaction);
		}
	}

	public void execute(Transaction transaction)
			throws RecordServicesException {
		execute(transaction, 0);
	}

	public void execute(Transaction transaction, int attempt)
			throws RecordServicesException {

		transaction.setOnlyBeingPrepared(false);

		validateNotTooMuchRecords(transaction);
		validateAllFullyLoadedRecords(transaction);
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

		transaction.addAllRecordsToReindex(impacts.getRecordsToReindexLater());
		if (impacts.getImpacts().isEmpty()) {
			saveContentsAndRecords(transaction, null, attempt);
		} else {
			Transaction newTransaction = new Transaction(transaction);
			for (ModificationImpact impact : impacts.getImpacts()) {

				List<Record> recordsFound;
				if (impact.getMarkForReindexingInsteadOfBatchProcess() == null) {
					LOGGER.debug("Handling modification impact. Reindexing " + impact.getMetadataToReindex() + " for records of "
								 + impact.getLogicalSearchCondition().toString());
					LogicalSearchQuery searchQuery = new LogicalSearchQuery(impact.getLogicalSearchCondition());
					recordsFound = modelFactory.newSearchServices().search(searchQuery);

				} else {
					recordsFound = getRecordsById(transaction.getCollection(), new ArrayList<>(impact.getMarkForReindexingInsteadOfBatchProcess()));
				}

				for (Record record : recordsFound) {
					if (!newTransaction.getRecordIds().contains(record.getId())) {
						newTransaction.addUpdate(record);
					}
				}

				newTransaction.getRecordUpdateOptions().getTransactionRecordsReindexation()
						.addReindexedMetadatas(impact.getMetadataToReindex());
			}


			execute(newTransaction, attempt);
		}

	}

	public List<BatchProcess> executeHandlingImpactsAsync(Transaction transaction)
			throws RecordServicesException {
		if (!transaction.getRecords().isEmpty()) {
			AddToBatchProcessImpactHandler handler = addToBatchProcessModificationImpactHandler();
			executeWithImpactHandler(transaction, handler, false, 0);
			return handler.getAllCreatedBatchProcesses();
		} else {
			return Collections.emptyList();
		}
	}

	public List<BatchProcess> executeHandlingSomeImpactsNow(Transaction transaction)
			throws RecordServicesException {
		if (!transaction.getRecords().isEmpty()) {
			AddToBatchProcessImpactHandler handler = addToBatchProcessModificationImpactHandler();
			executeWithImpactHandler(transaction, handler, true, 0);
			return handler.getAllCreatedBatchProcesses();
		} else {
			return Collections.emptyList();
		}
	}

	public void executeWithImpactHandler(Transaction transaction, RecordModificationImpactHandler handler,
										 boolean tryHandlingSomeImpactsNow, int attempt)
			throws RecordServicesException {
		transaction.setOnlyBeingPrepared(false);
		validateNotTooMuchRecords(transaction);

		prepareRecords(transaction);
		Transaction newTransaction = new Transaction(transaction);
		boolean newImpactsHandledNow = false;
		List<ModificationImpact> impactsToHandleLater = new ArrayList<>();
		if (handler != null) {
			ModificationImpactCalculatorResponse impacts = getModificationImpacts(transaction, true);

			Set<String> idsReindexedNow = new HashSet<>();


			for (ModificationImpact modificationImpact : impacts.getImpacts()) {
				if (modificationImpact.getMarkForReindexingInsteadOfBatchProcess() != null) {

					idsReindexedNow.addAll(modificationImpact.getMarkForReindexingInsteadOfBatchProcess());

					boolean tryHandlingThisImpactsNow = tryHandlingSomeImpactsNow &&
														idsReindexedNow.size() + modificationImpact.getMarkForReindexingInsteadOfBatchProcess().size() < 250;

					boolean impactIsHandledNow = false;
					boolean impactIsHandledLater = false;
					for (String id : modificationImpact.getMarkForReindexingInsteadOfBatchProcess()) {
						if (!newTransaction.getRecordIds().contains(id)) {
							if (tryHandlingThisImpactsNow) {
								Record record = getDocumentById(id);
								newTransaction.addUpdate(record);
								newImpactsHandledNow = true;
								impactIsHandledNow = true;
							} else {
								newTransaction.addRecordToReindex(id);
								impactIsHandledLater = true;
							}
						}
					}

					if (impactIsHandledNow) {
						newTransaction.getRecordUpdateOptions().getTransactionRecordsReindexation()
								.addReindexedMetadatas(modificationImpact.getMetadataToReindex());
					}

					//					if (impactIsHandledLater) {
					//						impactsToHandleLater.add(modificationImpact);
					//					}


				} else {
					impactsToHandleLater.add(modificationImpact);
				}


			}

			transaction.addAllRecordsToReindex(impacts.getRecordsToReindexLater());
		}

		if (newImpactsHandledNow) {
			executeWithImpactHandler(newTransaction, handler, false, attempt);
		} else {
			for (ModificationImpact modificationImpact : impactsToHandleLater) {
				handler.prepareToHandle(modificationImpact);
			}

			saveContentsAndRecords(newTransaction, handler, attempt);
		}

	}

	void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
	}

	void handleOptimisticLocking(TransactionDTO transactionDTO, Transaction transaction,
								 RecordModificationImpactHandler handler,
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
				executeWithImpactHandler(transaction, handler, false, attempt + 1);
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
					MetadataSchema metadataSchema = types.getSchemaOf(record);
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
		MetadataSchema schema = modelFactory.getMetadataSchemasManager().getSchemaOf(recordDTO);
		return toRecord(schema, recordDTO, allFields);
	}

	public Record toRecord(MetadataSchemaType schemaType, RecordDTO recordDTO, boolean allFields) {
		String collection = (String) recordDTO.getFields().get("collection_s");
		CollectionInfo collectionInfo = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection);
		Record record = new RecordImpl(recordDTO, collectionInfo);

		if (schemaType.hasEagerTransientMetadata()) {
			Transaction tx = new Transaction("temp");
			tx.setOptions(new RecordUpdateOptions());

			newAutomaticMetadataServices()
					.loadTransientEagerMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(), tx);
		}
		return record;
	}

	public Record toRecord(MetadataSchema schema, RecordDTO recordDTO, boolean allFields) {
		String collection = (String) recordDTO.getFields().get("collection_s");
		CollectionInfo collectionInfo = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection);
		Record record = new RecordImpl(recordDTO, collectionInfo);

		if (schema.hasEagerTransientMetadata()) {
			Transaction tx = new Transaction("temp");
			tx.setOptions(new RecordUpdateOptions());

			newAutomaticMetadataServices()
					.loadTransientEagerMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(), tx);
		}
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
		if (value == null) {
			return null;
		}

		MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(metadata.getCollection());
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(metadata);
		MetadataSchemaType schemaType = types.getSchemaType(schemaTypeCode);

		if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
			return getRecordsCaches().getCache(metadata.getCollection()).getByMetadata(metadata, value);
		} else if (schemaType.getCacheType().isSummaryCache()) {
			Record record = getRecordsCaches().getCache(metadata.getCollection()).getSummaryByMetadata(metadata, value);

			if (record != null) {
				return getDocumentById(record.getId());
			} else {
				return null;
			}
		}


		SearchServices searchServices = modelLayerFactory.newSearchServices();
		LogicalSearchCondition condition = from(schemaType).where(metadata).isEqualTo(value);

		return searchServices.searchSingleResult(condition);
	}


	@Override
	public Record getRecordSummaryByMetadata(Metadata metadata, String value) {
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

		if (!schemaType.getCacheType().hasPermanentCache()) {
			throw new IllegalArgumentException("Schema type '" + schemaTypeCode + "' has no permanent cache");
		}

		Record returnedRecord = getRecordsCaches().getCache(metadata.getCollection()).getSummaryByMetadata(metadata, value);
		if (returnedRecord == null && getRecordsCaches().isCacheInitialized(schemaType)) {
			LogicalSearchCondition condition = from(schemaType).where(metadata).isEqualTo(value);

			Record record = searchServices.searchSingleResult(condition);
			if (record != null) {
				RecordDTO recordDTO = toPersistedSummaryRecordDTO(record, schemaType.getSchema(record.getSchemaCode()));
				returnedRecord = new RecordImpl(recordDTO, schemaType.getCollectionInfo());
			}
		}

		return returnedRecord;
	}

	public Record getById(String dataStore, String id, boolean callExtensions) {
		try {
			RecordDTO recordDTO = dao(dataStore).get(id, callExtensions);
			String collection = (String) recordDTO.getFields().get("collection_s");
			CollectionInfo collectionInfo = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection);
			Record record = new RecordImpl(recordDTO, collectionInfo);
			newAutomaticMetadataServices()
					.loadTransientEagerMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(),
							new Transaction(new RecordUpdateOptions()));
			insertInCache(record, WAS_OBTAINED);
			return record;

		} catch (NoSuchRecordWithId e) {
			throw new RecordServicesRuntimeException.NoSuchRecordWithId(id, dataStore, e);
		}
	}

	public Record realtimeGetById(String dataStore, String id, Long version, boolean callExtensions) {
		try {
			RecordDTO recordDTO = dao(dataStore).realGet(id, callExtensions);
			String collection = (String) recordDTO.getFields().get("collection_s");
			CollectionInfo collectionInfo = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection);

			Record record = new RecordImpl(recordDTO, collectionInfo);
			newAutomaticMetadataServices()
					.loadTransientEagerMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(),
							new Transaction(new RecordUpdateOptions()));
			insertInCache(record, WAS_OBTAINED);
			return record;

		} catch (NoSuchRecordWithId e) {
			throw new RecordServicesRuntimeException.NoSuchRecordWithId(id, dataStore, e);
		}
	}


	public Record realtimeGetRecordSummaryById(String id, boolean callExtensions) {
		try {
			//TODO Improve!!!!
			RecordDTO recordDTO = dao(DataStore.RECORDS).realGet(id, callExtensions);
			String collection = (String) recordDTO.getFields().get("collection_s");
			CollectionInfo collectionInfo = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection);

			Record record = new RecordImpl(recordDTO, collectionInfo);
			newAutomaticMetadataServices()
					.loadTransientEagerMetadatas((RecordImpl) record, newRecordProviderWithoutPreloadedRecords(),
							new Transaction(new RecordUpdateOptions()));

			MetadataSchema schema = metadataSchemasManager.getSchemaOf(record);


			RecordDTO summaryRecordDTO = toPersistedSummaryRecordDTO(record, schema);
			record = toRecord(summaryRecordDTO, false);

			return record;

		} catch (NoSuchRecordWithId e) {
			throw new RecordServicesRuntimeException.NoSuchRecordWithId(id, DataStore.RECORDS, e);
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

	public List<Record> realtimeGetRecordById(List<String> ids, boolean callExtensions) {
		String mainDataLanguage = modelLayerFactory.getCollectionsListManager().getMainDataLanguage();
		List<Record> records = new ArrayList<>();
		for (RecordDTO recordDTO : recordDao.realGet(ids, callExtensions)) {
			String collection = (String) recordDTO.getFields().get("collection_s");
			CollectionInfo collectionInfo = modelLayerFactory.getCollectionsListManager().getCollectionInfo(collection);

			Record record = new RecordImpl(recordDTO, collectionInfo);
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
				if (evaluateCacheInsert(record) != CacheInsertionStatus.ACCEPTED) {
					insertedRecords.add(realtimeGetRecordById(record.getId()));
				} else {
					insertedRecords.add(record);
				}
			}
		}
		recordsCaches.insert(collection, insertedRecords, reason);

	}

	public List<Record> getRecordsById(String collection, List<String> ids, boolean callExtensions) {

		List<Record> records = new ArrayList<>();

		ids.forEach(id -> {
			try {
				records.add(getDocumentById(id, callExtensions));
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				LOGGER.warn("Record with id '" + id + "' does not exist");
			}
		});

		return records;
	}

	public void prepareRecords(Transaction transaction)
			throws RecordServicesException.ValidationException {
		prepareRecords(transaction, null);
	}

	void prepareRecords(final Transaction transaction, String onlyValidateRecord)
			throws RecordServicesException.ValidationException {

		TransactionExecutionContext context = new TransactionExecutionContext(transaction);
		RecordPopulateServices recordPopulateServices = modelLayerFactory.newRecordPopulateServices();
		RecordProvider recordProvider = newRecordProvider(null, transaction);
		RecordValidationServices validationServices = newRecordValidationServices(recordProvider);
		RecordAutomaticMetadataServices automaticMetadataServices = newAutomaticMetadataServices();
		TransactionRecordsReindexation reindexation = transaction.getRecordUpdateOptions().getTransactionRecordsReindexation();
		MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(transaction.getCollection());
		RecordUpdateOptions options = transaction.getRecordUpdateOptions();

		for (Record record : transaction.getRecords()) {
			MetadataSchemaType schemaType = types.getSchemaType(record.getTypeCode());
			if (schemaType.isReadOnlyLocked() && !options.isAllowSchemaTypeLockedRecordsModification()) {
				throw new RecordServicesRuntimeException.SchemaTypeOfARecordHasReadOnlyLock(record.getTypeCode(), record.getId());
			}
		}

		for (Record record : transaction.getRecords()) {
			if (record.get(Schemas.MIGRATION_DATA_VERSION) == null) {
				if (record.isSaved()) {
					record.set(Schemas.MIGRATION_DATA_VERSION, 0.0);
				} else {
					record.set(Schemas.MIGRATION_DATA_VERSION, modelLayerFactory.getRecordMigrationsManager()
							.getCurrentDataVersion(record.getCollection(), record.getTypeCode()));
				}

			}

		}

		ValidationErrors errors = new ValidationErrors();
		boolean catchValidationsErrors = transaction.getRecordUpdateOptions().isCatchExtensionsValidationsErrors();

		ModelLayerCollectionExtensions extensions = modelFactory.getExtensions().forCollection(transaction.getCollection());
		for (Record record : transaction.getRecords()) {
			ValidationErrors transactionExtensionErrors =
					catchValidationsErrors ? new ValidationErrors() : new DecoratedValidationsErrors(errors);
			if (record.isDirty()) {
				if (record.isSaved()) {
					MetadataList modifiedMetadatas = record.getModifiedMetadatas(types);
					extensions.callRecordInModificationBeforeValidationAndAutomaticValuesCalculation(
							new RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent(record,
									modifiedMetadatas, transactionExtensionErrors, transaction.getUser(), transaction.isOnlyBeingPrepared()), options);
				} else {
					extensions.callRecordInCreationBeforeValidationAndAutomaticValuesCalculation(
							new RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent(
									record, transaction.getUser(), transactionExtensionErrors), options);
				}
			}
		}

		if (!errors.isEmpty()) {

			if (catchValidationsErrors) {
				LOGGER.warn("Validating errors added by extensions : \n" + $(errors));

			} else {
				if (!errors.isEmpty()) {
					throw new RecordServicesException.ValidationException(transaction, errors);
				}
			}
		}

		boolean validations = transaction.getRecordUpdateOptions().isValidationsEnabled();
		ParsedContentProvider parsedContentProvider = new ParsedContentProvider(modelFactory.getContentManager(),
				transaction.getParsedContentCache());
		for (Record record : transaction.getRecords()) {
			if (transaction.getRecordUpdateOptions().isRepopulate()) {
				recordPopulateServices.populate(record, parsedContentProvider);
			}
			MetadataSchema schema = types.getSchemaOf(record);

			if (onlyValidateRecord == null || onlyValidateRecord.equals(record.getId())) {

				for (RecordPreparationStep step : schema.getPreparationSteps()) {

					if (step instanceof CalculateMetadatasRecordPreparationStep) {
						TransactionRecordsReindexation reindexationOptionForThisRecord = reindexation;
						RequiredRecordMigrations migrations =
								modelLayerFactory.getRecordMigrationsManager().getRecordMigrationsFor(record);

						if (options.isUpdateCalculatedMetadatas()) {

							if (!migrations.getScripts().isEmpty()) {

								for (RecordMigrationScript script : migrations.getScripts()) {
									script.migrate(record);
								}
								record.set(Schemas.MIGRATION_DATA_VERSION, migrations.getVersion());

								reindexationOptionForThisRecord = TransactionRecordsReindexation.ALL();
							}


							for (Metadata metadata : step.getMetadatas()) {
								try {
									automaticMetadataServices.updateAutomaticMetadata(context, (RecordImpl) record,
											recordProvider, metadata, reindexationOptionForThisRecord, types, transaction);
								} catch (RuntimeException e) {
									throw new RecordServicesRuntimeException_ExceptionWhileCalculating(record.getId(), metadata, e);
								}
							}

							MetadataList modifiedMetadatas = record.getModifiedMetadatas(types);
							extensions.callRecordReindexed(new RecordReindexationEvent(record, modifiedMetadatas) {
								@Override
								public void recalculateRecord(List<String> metadatas) {
									newAutomaticMetadataServices().updateAutomaticMetadatas(
											(RecordImpl) record, newRecordProvider(transaction),
											metadatas, transaction);
								}
							});

							validationServices.validateAccess(record, transaction);
						}
					} else if (step instanceof UpdateCreationModificationUsersAndDateRecordPreparationStep) {
						if (transaction.getRecordUpdateOptions().isUpdateModificationInfos()) {
							updateCreationModificationUsersAndDates(record, transaction, types.getSchemaOf(record));
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

								Metadata metadataProvidingReference;
								Metadata metadataProvidingSequenceCode;
								String sequenceCode = null;

								if (dataEntry.getMetadataProvidingSequenceCode().contains(".")) {
									String[] splittedCode = dataEntry.getMetadataProvidingSequenceCode().split("\\.");
									metadataProvidingReference = schema.getMetadata(splittedCode[0]);
									metadataProvidingSequenceCode = types
											.getDefaultSchema(metadataProvidingReference.getReferencedSchemaType())
											.getMetadata(splittedCode[1]);
									String metadataProvidingReferenceValue = record.get(metadataProvidingReference);

									if (metadataProvidingReferenceValue != null) {
										sequenceCode = getDocumentById(metadataProvidingReferenceValue)
												.get(metadataProvidingSequenceCode);
									}
								} else {
									metadataProvidingReference = schema.getMetadata(dataEntry.getMetadataProvidingSequenceCode());
									metadataProvidingSequenceCode = metadataProvidingReference;
									sequenceCode = record.get(metadataProvidingSequenceCode);
								}

								// if user did not changed seqNumber AND reference changed
								if (!record.isModified(metadata) && record.isModified(metadataProvidingReference)) {
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
							validationServices.validateMetadatas(record, recordProvider, transaction, step.getMetadatas(), ((ValidateMetadatasRecordPreparationStep) step).isAfterCalculate());
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
			if (transaction.getRecordUpdateOptions().isRepopulate()) {
				for (Metadata contentMetadata : schema.getContentMetadatasForPopulate()) {
					for (Content aContent : record.<Content>getValues(contentMetadata)) {
						allParsed &= parsedContentProvider
											 .getParsedContentIfAlreadyParsed(aContent.getCurrentVersion().getHash()) != null;
					}
				}

				if (allParsed) {
					record.set(Schemas.MARKED_FOR_PARSING, null);
				} else {
					if (!record.isModified(Schemas.MARKED_FOR_PARSING)) {
						record.set(Schemas.MARKED_FOR_PARSING, true);
					}
				}
			}
		}

		if (!transaction.getRecordUpdateOptions().isSkipFindingRecordsToReindex()) {
			new RecordsLinksResolver(types).resolveRecordsLinks(transaction);
		}

		boolean singleRecordTransaction = transaction.getRecords().size() == 1;

		ValidationErrors transactionExtensionErrors =
				catchValidationsErrors ? new ValidationErrors() : new DecoratedValidationsErrors(errors);

		extensions.callTransactionExecutionBeforeSave(
				new TransactionExecutionBeforeSaveEvent(transaction, transactionExtensionErrors), options);
		if (catchValidationsErrors && !transactionExtensionErrors.isEmptyErrorAndWarnings()) {
			LOGGER.warn("Validating errors added by extensions : \n" + $(transactionExtensionErrors));
		}

		ValidationErrors recordErrors =
				catchValidationsErrors ? new ValidationErrors() : new DecoratedValidationsErrors(errors);
		List<Record> newRecords = new ArrayList<>();
		List<Record> modifiedRecords = new ArrayList<>();
		for (final Record record : transaction.getRecords()) {
			if (record.isDirty()) {
				if (record.isSaved()) {
					modifiedRecords.add(record);
					MetadataList modifiedMetadatas = record.getModifiedMetadatas(types);
					extensions.callRecordInModificationBeforeSave(new RecordInModificationBeforeSaveEvent(record,
							modifiedMetadatas, transaction.getUser(), singleRecordTransaction, recordErrors) {
						@Override
						public void recalculateRecord(List<String> metadatas) {
							newAutomaticMetadataServices().updateAutomaticMetadatas(
									(RecordImpl) record, newRecordProvider(transaction),
									metadatas, transaction);
						}
					}, options);
				} else {
					newRecords.add(record);
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


	public void validateRecordInTransaction(Supplier<Record> record, Transaction transaction)
			throws ValidationException {
		if (transaction.getRecords().isEmpty()) {
			validateRecord(record);
		} else {
			prepareRecords(transaction, record.get().getId());
		}
	}

	public void validateRecord(Supplier<Record> record)
			throws RecordServicesException.ValidationException {

		Transaction transaction = new Transaction(record.get());
		prepareRecords(transaction);
	}

	private void updateCreationModificationUsersAndDates(Record record, Transaction transaction,
														 MetadataSchema metadataSchema) {
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

	ModificationImpactCalculatorResponse getModificationImpacts(Transaction transaction,
																boolean executedAfterTransaction) {
		SearchServices searchServices = modelFactory.newSearchServices();
		TaxonomiesManager taxonomiesManager = modelFactory.getTaxonomiesManager();
		MetadataSchemaTypes metadataSchemaTypes = modelFactory.getMetadataSchemasManager().getSchemaTypes(
				transaction.getCollection());

		return calculateImpactOfModification(transaction, taxonomiesManager, searchServices, metadataSchemaTypes,
				executedAfterTransaction);
	}

	void refreshRecordsAndCaches(String collection, List<Record> records, Set<String> idsMarkedForReindexing,
								 List<AggregatedMetadataIncrementation> aggregationMetadatasIncremented,
								 TransactionResponseDTO transactionResponseDTO,
								 MetadataSchemaTypes types, RecordProvider recordProvider) {

		Map<String, Record> updatedRecordsById = new HashMap<>();

		Map<String, Record> recordsToInsertById = new HashMap<>();
		for (AggregatedMetadataIncrementation incrementation : aggregationMetadatasIncremented) {
			if (transactionResponseDTO != null) {
				Record record = recordProvider.getRecord(incrementation.getRecordId());
				Long newVersion = transactionResponseDTO.getNewDocumentVersion(record.getId());
				if (record.isDirty() || (newVersion != null && record.getVersion() < newVersion)) {
					Number number = record.get(incrementation.getMetadata());
					if (number == null) {
						((RecordImpl) record).updateAutomaticValue(incrementation.getMetadata(), incrementation.getAmount());
					} else {
						((RecordImpl) record).updateAutomaticValue(incrementation.getMetadata(), number.doubleValue() + incrementation.getAmount());
					}

				}
				if (record != null) {
					recordsToInsertById.put(record.getId(), record);
					recordProvider.memoryList.put(record.getId(), record);
				}
			}
		}

		invalidateTaxonomiesCache(records, types, recordProvider, modelLayerFactory.getTaxonomiesSearchServicesCache());

		for (Record record : records) {
			RecordImpl recordImpl = (RecordImpl) record;
			if (transactionResponseDTO != null) {
				Long version = transactionResponseDTO.getNewDocumentVersion(record.getId());
				if (version != null) {

					MetadataSchema schema = types.getSchemaOf(record);

					recordImpl.markAsSaved(version, schema);
					recordsToInsertById.put(record.getId(), record);
				}
			}
		}

		for (String idMarkedForReindexing : idsMarkedForReindexing) {
			if (transactionResponseDTO != null && !recordsToInsertById.containsKey(idMarkedForReindexing)) {
				Long version = transactionResponseDTO.getNewDocumentVersion(idMarkedForReindexing);
				if (version != null) {

					Record record = recordsCaches.getRecord(idMarkedForReindexing);
					if (record != null) {
						record.set(Schemas.MARKED_FOR_REINDEXING, true);
						((RecordImpl) record).markAsSaved(version, metadataSchemasManager.getSchemaOf(record));
						recordsToInsertById.put(record.getId(), record);
					}
				}
			}
		}


		insertInCache(collection, new ArrayList<>(recordsToInsertById.values()), WAS_MODIFIED);

	}

	void saveContentsAndRecords(Transaction transaction, RecordModificationImpactHandler modificationImpactHandler,
								int attempt)
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

	void saveTransactionDTO(Transaction transaction, RecordModificationImpactHandler modificationImpactHandler,
							int attempt)
			throws RecordServicesException {

		List<Record> modifiedOrUnsavedRecords = transaction.getModifiedRecords();
		Set<String> idsMarkedForReindexing = transaction.getRecordUpdateOptions().isMarkIdsForReindexing()
											 ? transaction.getIdsToReindex() : Collections.<String>emptySet();
		if (!modifiedOrUnsavedRecords.isEmpty() || !idsMarkedForReindexing.isEmpty()) {
			Map<String, TransactionDTO> transactionDTOs = createTransactionDTOs(transaction, modifiedOrUnsavedRecords);
			for (Map.Entry<String, TransactionDTO> transactionDTOEntry : transactionDTOs.entrySet()) {
				try {
					MetadataSchemaTypes metadataSchemaTypes = modelFactory.getMetadataSchemasManager().getSchemaTypes(
							transaction.getCollection());

					List<RecordEvent> recordEvents = new ArrayList<>();
					TransactionExecutedEvent event = prepareRecordEvents(transaction, modifiedOrUnsavedRecords, metadataSchemaTypes, recordEvents);


					TransactionResponseDTO transactionResponseDTO;
					if (transactionDTOEntry.getKey().equals(DataStore.RECORDS)) {
						transactionResponseDTO = recordDao.execute(transactionDTOEntry.getValue());
						modelFactory.newLoggingServices().logTransaction(transaction);

					} else if (transactionDTOEntry.getKey().equals(DataStore.EVENTS)) {
						transactionResponseDTO = eventsDao.execute(transactionDTOEntry.getValue());

					} else {
						throw new ImpossibleRuntimeException("Unsupported datastore : " + transactionDTOEntry.getKey());
					}

					refreshRecordsAndCaches(transaction.getCollection(), modifiedOrUnsavedRecords, idsMarkedForReindexing,
							transaction.getAggregatedMetadataIncrementations(),
							transactionResponseDTO, metadataSchemaTypes, newRecordProvider(null, transaction));

					if (modificationImpactHandler != null) {
						modificationImpactHandler.handle();
					}

					callExtensions(transaction.getCollection(), recordEvents, event, transaction.getRecordUpdateOptions());

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

	private TransactionExecutedEvent prepareRecordEvents(Transaction transaction, List<Record> modifiedOrUnsavedRecords,
														 MetadataSchemaTypes types, List<RecordEvent> events) {

		List<Record> newRecords = new ArrayList<>();
		List<Record> modifiedRecords = new ArrayList<>();
		Map<String, MetadataList> modifiedMetadatasOfModifiedRecords = new HashMap<>();

		for (Record record : modifiedOrUnsavedRecords) {
			MetadataSchema schema = types.getSchemaOf(record);
			if (record.isSaved()) {

				if (record.isModified(Schemas.LOGICALLY_DELETED_STATUS)) {
					if (LangUtils.isFalseOrNull(record.get(Schemas.LOGICALLY_DELETED_STATUS))) {
						events.add(new RecordRestorationEvent(record));
					} else {
						events.add(new RecordLogicalDeletionEvent(record));
					}
				} else {
					modifiedRecords.add(record);
					MetadataList modifiedMetadatas = record.getModifiedMetadatas(types);
					events.add(new RecordModificationEvent(record, modifiedMetadatas, schema, transaction.getUser()));
					modifiedMetadatasOfModifiedRecords.put(record.getId(), modifiedMetadatas);
				}

			} else {
				newRecords.add(record);
				events.add(new RecordCreationEvent(record, schema, transaction.getUser()));
			}
		}

		return new TransactionExecutedEvent(transaction, newRecords, modifiedRecords, modifiedMetadatasOfModifiedRecords);
	}

	private void callExtensions(String collection, List<RecordEvent> recordEvents, TransactionExecutedEvent event,
								RecordUpdateOptions options) {
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

		extensions.callTransactionExecuted(event, options);

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
			CollectionInfo collectionInfo = modelFactory.getCollectionsListManager().getCollectionInfo(collection);
			List<FieldsPopulator> fieldsPopulators = new ArrayList<>();
			MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			ConstellioEIMConfigs systemConfigs = modelFactory.getSystemConfigs();
			ParsedContentProvider parsedContentProvider = new ParsedContentProvider(contentManager,
					transaction.getParsedContentCache());

			fieldsPopulators
					.add(new SearchFieldsPopulator(types, options.isFullRewrite(), parsedContentProvider, collectionInfo,
							systemConfigs, modelLayerFactory.getExtensions()));

			fieldsPopulators.add(new SortFieldsPopulator(types, options.isFullRewrite(), modelFactory,
					newRecordProvider(transaction)));

			Factory<EncryptionServices> encryptionServicesFactory = new Factory<EncryptionServices>() {
				@Override
				public EncryptionServices get() {
					return modelLayerFactory.newEncryptionServices();
				}
			};

			List<String> ids = transaction.getRecordIds();
			Set<String> markedForReindexing = new HashSet<>();
			if (transaction.getRecordUpdateOptions().isMarkIdsForReindexing()) {
				for (String id : transaction.getIdsToReindex()) {
					if (!ids.contains(id)) {
						markedForReindexing.add(id);
					} else {
						transaction.getRecord(id).set(Schemas.MARKED_FOR_REINDEXING, true);
					}
				}
			}

			Map<String, RecordDeltaDTO> modifiedRecordDTOById = new HashMap<>();
			for (Record record : modifiedOrUnsavedRecords) {
				MetadataSchemaType type = modelFactory.getMetadataSchemasManager().getSchemaTypeOf(record);
				MetadataSchema schema = type.getSchema(record.getSchemaCode());
				if (dataStore.equals(type.getDataStore())) {
					if (!record.isSaved()) {
						addedRecords.add(((RecordImpl) record).toNewDocumentDTO(schema, fieldsPopulators));
					} else {
						RecordImpl recordImpl = (RecordImpl) record;
						if (recordImpl.isDirty() && !transaction.getRecordUpdateOptions().isFullRewrite()) {
							RecordDeltaDTO modifiedRecordDto = recordImpl.toRecordDeltaDTO(schema, fieldsPopulators);
							modifiedRecordDTOById.put(modifiedRecordDto.getId(), modifiedRecordDto);
						} else if (transaction.getRecordUpdateOptions().isFullRewrite()) {
							addedRecords.add(((RecordImpl) record).toDocumentDTO(schema, fieldsPopulators));
						}
					}
				}
			}

			if (dataStore.equals("records") && !transaction.getAggregatedMetadataIncrementations().isEmpty()) {
				for (AggregatedMetadataIncrementation incrementation : transaction.getAggregatedMetadataIncrementations()) {
					if (!modifiedRecordDTOById.containsKey(incrementation.getRecordId())) {
						modifiedRecordDTOById.put(incrementation.getRecordId(),
								new RecordDeltaDTO(incrementation.getRecordId(), 1L));
					}
					String field = incrementation.getMetadata().getDataStoreCode();
					Map<String, Double> incrementedFields = modifiedRecordDTOById.get(incrementation.getRecordId())
							.getIncrementedFields();
					if (incrementedFields.containsKey(field)) {
						incrementedFields.put(field, incrementedFields.get(field) + incrementation.getAmount());
					} else {
						incrementedFields.put(field, incrementation.getAmount());
					}
				}
			}
			modifiedRecordDTOs = new ArrayList<>(modifiedRecordDTOById.values());

			boolean isSupportingReindexing = DataStore.RECORDS.equals(dataStore);

			boolean isChangingSomething = !addedRecords.isEmpty() || !modifiedRecordDTOs.isEmpty() ||
										  (isSupportingReindexing && !markedForReindexing.isEmpty());

			if (isChangingSomething) {
				TransactionDTO datastoreTransaction =
						new TransactionDTO(transaction.getId(), options.getRecordsFlushing(), addedRecords, modifiedRecordDTOs)
								.withSkippingReferenceToLogicallyDeletedValidation(
										transaction.isSkippingReferenceToLogicallyDeletedValidation())
								.withFullRewrite(options.isFullRewrite());

				if (isSupportingReindexing && !markedForReindexing.isEmpty()) {
					datastoreTransaction = datastoreTransaction.withMarkedForReindexing(markedForReindexing);
				}
				transactions.put(dataStore, datastoreTransaction);
			}
		}

		return transactions;
	}

	public Record newRecordWithSchema(MetadataSchema schema) {
		return newRecordWithSchema(schema, true);
	}

	public Record newRecordWithSchema(MetadataSchema schema, boolean isWithDefaultValues) {
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

		return newRecordWithSchema(schema, id, isWithDefaultValues);
	}

	public Record newRecordWithSchema(MetadataSchema schema, String id) {
		return newRecordWithSchema(schema, id, true);
	}

	public Record newRecordWithSchema(MetadataSchema schema, String id, boolean withDefaultValues) {
		Record record = new RecordImpl(schema, id);

		if (withDefaultValues) {
			for (Metadata metadata : schema.getMetadatas().onlyWithDefaultValue().onlyManuals()) {

				if (metadata.isMultivalue()) {
					List<Object> values = new ArrayList<>();
					values.addAll((List) metadata.getDefaultValue());
					record.set(metadata, values);
				} else {
					record.set(metadata, metadata.getDefaultValue());
				}
			}
		}

		return record;
	}

	public RecordAutomaticMetadataServices newAutomaticMetadataServices() {
		return new RecordAutomaticMetadataServices(modelFactory);
	}

	public RecordValidationServices newRecordValidationServices(RecordProvider recordProvider) {
		return new RecordValidationServices(newConfigProvider(), recordProvider, modelFactory.getMetadataSchemasManager(),
				modelFactory.newSearchServices(), modelFactory.newAuthorizationsServices(), newAutomaticMetadataServices());
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

	public <T extends Supplier<Record>> void refresh(List<T> records) {
		for (Object item : records) {
			Record record;

			if (item instanceof Record) {
				record = (Record) item;
			} else {
				record = ((RecordWrapper) item).getWrappedRecord();
			}

			if (record != null && record.isSaved()) {
				try {
					RecordDTO recordDTO = recordDao.get(record.getId(), true);
					((RecordImpl) record).refresh(recordDTO.getVersion(), recordDTO);
				} catch (NoSuchRecordWithId noSuchRecordWithId) {
					LOGGER.debug("Deleted record is disconnected");
					((RecordImpl) record).markAsDisconnected();
				}

			}
		}
	}

	@Override
	public <T extends Supplier<Record>> void refreshUsingCache(List<T> records) {
		for (T item : records) {
			if (item != null) {
				Record record = item.get();

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
																			  SearchServices searchServices,
																			  MetadataSchemaTypes metadataSchemaTypes,
																			  boolean executedAfterTransaction) {

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
																		MetadataSchemaTypes metadataSchemaTypes,
																		SearchServices searchServices) {
		List<Taxonomy> taxonomies = taxonomiesManager.getEnabledTaxonomies(metadataSchemaTypes.getCollection());
		return new ModificationImpactCalculator(metadataSchemaTypes, taxonomies, searchServices, this);

	}

	public boolean isRestorable(Supplier<Record> record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().validateRestorable(record.get(), user).isEmpty();
	}

	public void restore(Supplier<Record> record, User user) {

		refreshUsingCache(record);
		refreshUsingCache(user);
		newRecordDeleteServices().restore(record.get(), user);
	}

	public ValidationErrors validatePhysicallyDeletable(Supplier<Record> record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().validatePhysicallyDeletable(record.get(), user);
	}

	public ValidationErrors validatePhysicallyDeletable(Supplier<Record> record, User user,
														RecordPhysicalDeleteOptions options) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().validatePhysicallyDeletable(record.get(), user, options);
	}

	public void physicallyDelete(Supplier<Record> record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		newRecordDeleteServices().physicallyDelete(record.get(), user);
	}

	public void physicallyDeleteNoMatterTheStatus(Supplier<Record> record, User user,
												  RecordPhysicalDeleteOptions options) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		newRecordDeleteServices().physicallyDeleteNoMatterTheStatus(record.get(), user, options);
	}

	public void physicallyDelete(Supplier<Record> record, User user, RecordPhysicalDeleteOptions options) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		newRecordDeleteServices().physicallyDelete(record.get(), user, options);
	}

	public ValidationErrors validateLogicallyDeletable(Supplier<Record> record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().validateLogicallyDeletable(record.get(), user);
	}

	@Override
	public boolean isLogicallyDeletableAndIsSkipValidation(Supplier<Record> record, User user) {
		return newRecordDeleteServices().isLogicallyDeletableAndIsSkipValidation(record.get(), user, new RecordLogicalDeleteOptions());
	}

	public ValidationErrors validateLogicallyThenPhysicallyDeletable(Supplier<Record> record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().validateLogicallyThenPhysicallyDeletable(record.get(), user);
	}

	public ValidationErrors validateLogicallyThenPhysicallyDeletable(Supplier<Record> record, User user,
																	 RecordPhysicalDeleteOptions options) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().validateLogicallyThenPhysicallyDeletable(record.get(), user, options);
	}

	public boolean isPrincipalConceptLogicallyDeletableExcludingContent(Supplier<Record> record, User user) {
		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().isPrincipalConceptLogicallyDeletableExcludingContent(record.get(), user);
	}

	public void logicallyDelete(Supplier<Record> record, User user) {
		logicallyDelete(record, user, new RecordLogicalDeleteOptions());
	}

	public void logicallyDelete(Supplier<Record> record, User user, RecordLogicalDeleteOptions options) {

		if (!options.isSkipRefresh()) {
			refreshUsingCache(record);
			refreshUsingCache(user);
		}

		newRecordDeleteServices().logicallyDelete(record.get(), user, options);

		refreshUsingCache(record);
	}

	public List<Record> getVisibleRecordsWithReferenceTo(Supplier<Record> record, User user) {

		refreshUsingCache(record);
		refreshUsingCache(user);
		return newRecordDeleteServices().getVisibleRecordsWithReferenceToRecordInHierarchy(record.get(), user,
				newRecordDeleteServices().loadRecordsHierarchyOf(record.get()));
	}

	public boolean isReferencedByOtherRecords(Supplier<Record> record) {
		return newRecordDeleteServices().isReferencedByOtherRecords(record.get(),
				newRecordDeleteServices().loadRecordsHierarchyOf(record.get()));
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

	private void validateAllFullyLoadedRecords(Transaction transaction) {
		//Currently, execution of transaction containing records that are not fully loaded cause unwanted behaviors,
		// such as losing metadatas that are not kept in summary

		for (Record record : transaction.getRecords()) {
			RecordDTO recordDTO = ((RecordImpl) record).getRecordDTO();
			if (recordDTO != null && ((RecordImpl) record).getRecordDTO().getLoadingMode() == SUMMARY) {
				MetadataSchemaType schemaType = metadataSchemasManager
						.getSchemaTypes(recordDTO.getCollection()).getSchemaType(record.getTypeCode());

				//if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
				if (schemaType.getCacheType().hasPermanentCache()) {
					throw new ImpossibleRuntimeException("Cannot execute transaction using records that are not fully or summary loaded");
				}
			}

			if (recordDTO != null && ((RecordImpl) record).getRecordDTO().getLoadingMode() == CUSTOM) {
				MetadataSchemaType schemaType = metadataSchemasManager
						.getSchemaTypes(recordDTO.getCollection()).getSchemaType(record.getTypeCode());

				if (schemaType.getCacheType().hasPermanentCache()) {
					throw new ImpossibleRuntimeException("Cannot execute transaction using records that are not fully or summary loaded");
				}
			}
		}

	}

	public void flush() {
		try {
			recordDao.flush();
			if (Toggle.ADVANCED_SEARCH_CONFIGS.isEnabled()) {
				eventsDao.flush();
			}
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

	public void recalculate(Supplier<Record> record) {
		newAutomaticMetadataServices().updateAutomaticMetadatas(
				(RecordImpl) record.get(), newRecordProviderWithoutPreloadedRecords(), TransactionRecordsReindexation.ALL(),
				new Transaction(new RecordUpdateOptions()));
	}

	@Override
	public void loadLazyTransientMetadatas(Supplier<Record> record) {
		newAutomaticMetadataServices()
				.loadTransientLazyMetadatas((RecordImpl) record.get(), newRecordProviderWithoutPreloadedRecords(),
						new Transaction(new RecordUpdateOptions()));
	}

	@Override
	public void reloadEagerTransientMetadatas(Supplier<Record> record) {
		newAutomaticMetadataServices()
				.loadTransientEagerMetadatas((RecordImpl) record.get(), newRecordProviderWithoutPreloadedRecords(),
						new Transaction(new RecordUpdateOptions()));
	}

	@Override
	public SecurityModel getSecurityModel(String collection) {
		return newAutomaticMetadataServices().getSecurityModel(collection);
	}

	@Override
	public boolean isValueAutomaticallyFilled(Metadata metadata, Supplier<Record> record) {
		return newAutomaticMetadataServices().isValueAutomaticallyFilled(metadata, record.get());
	}


}
