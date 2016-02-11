package com.constellio.model.services.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException.ReferenceToNonExistentIndex;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.Record;
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
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentModifications;
import com.constellio.model.services.contents.ContentModificationsBuilder;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.model.services.records.RecordServicesException.UnresolvableOptimisticLockingConflict;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NewReferenceToOtherLogicallyDeletedRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotDelayFlushingOfRecordsInCache;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_RecordsFlushingFailed;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_TransactionHasMoreThan100000Records;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution;
import com.constellio.model.services.records.RecordServicesRuntimeException.UnresolvableOptimsiticLockingCausingInfiniteLoops;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.records.populators.AutocompleteFieldPopulator;
import com.constellio.model.services.records.populators.SearchFieldsPopulator;
import com.constellio.model.services.records.populators.SortFieldsPopulator;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.ModificationImpactCalculator;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.DependencyUtils;
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

	public void execute(Transaction transaction)
			throws RecordServicesException {
		execute(transaction, 0);
	}

	public void execute(Transaction transaction, int attempt)
			throws RecordServicesException {

		validateNotTooMuchRecords(transaction);
		if (transaction.getRecords().isEmpty()) {
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
		List<ModificationImpact> impacts = getModificationImpacts(transaction, false);

		if (impacts.isEmpty()) {
			saveContentsAndRecords(transaction, null, attempt);
		} else {
			Transaction newTransaction = new Transaction(transaction);
			for (ModificationImpact impact : impacts) {
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
			List<ModificationImpact> impacts = getModificationImpacts(transaction, true);

			for (ModificationImpact modificationImpact : impacts) {
				handler.prepareToHandle(modificationImpact);
			}
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
			throw new UnresolvableOptimsiticLockingCausingInfiniteLoops(transactionDTO);
		}

		//Will wait up to 30 seconds given 35 attempt are made
		long sleepDuration = 50 * attempt;
		if (sleepDuration > 0) {
			sleep(sleepDuration);
		}

		OptimisticLockingResolution resolution = transaction.getRecordUpdateOptions().getOptimisticLockingResolution();

		if (resolution == OptimisticLockingResolution.EXCEPTION) {
			throw new RecordServicesException.OptimisticLocking(transactionDTO, e);
		} else if (resolution == OptimisticLockingResolution.TRY_MERGE) {
			mergeRecords(transaction);
			if (handler == null) {
				execute(transaction, attempt + 1);
			} else {
				executeWithImpactHandler(transaction, handler, attempt + 1);
			}
		}
	}

	void mergeRecords(Transaction transaction)
			throws RecordServicesException.UnresolvableOptimisticLockingConflict {
		List<LogicalSearchCondition> conditions = new ArrayList<>();

		for (Record record : transaction.getRecords()) {
			if (record.isSaved()) {
				conditions.add(LogicalSearchQueryOperators.where(Schemas.IDENTIFIER).isEqualTo(record.getId())
						.andWhere(Schemas.VERSION).isNotEqual(record.getVersion()));
			}
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
		return new RecordImpl(recordDTO, allFields);
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
		if (metadata.getCode().startsWith("global")) {
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
		try {
			return new RecordImpl(recordDao.get(id), true);
		} catch (NoSuchRecordWithId e) {
			throw new RecordServicesRuntimeException.NoSuchRecordWithId(id, e);
		}
	}

	public List<Record> getRecordsById(String collection, List<String> ids) {

		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(collection).where(Schemas.IDENTIFIER).isIn(ids));
		return modelFactory.newSearchServices().search(query);
	}

	void prepareRecords(Transaction transaction)
			throws RecordServicesException.ValidationException {
		prepareRecords(transaction, null);
	}

	void prepareRecords(Transaction transaction, String onlyValidateRecord)
			throws RecordServicesException.ValidationException {

		RecordPopulateServices recordPopulateServices = modelLayerFactory.newRecordPopulateServices();
		RecordProvider recordProvider = newRecordProvider(null, transaction);
		RecordValidationServices validationServices = newRecordValidationServices();
		RecordAutomaticMetadataServices automaticMetadataServices = newAutomaticMetadataServices();
		TransactionRecordsReindexation reindexation = transaction.getRecordUpdateOptions().getTransactionRecordsReindexation();
		MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(transaction.getCollection());

		if (transaction.getRecordUpdateOptions().getRecordsFlushing() != RecordsFlushing.NOW()) {
			RecordsCache cache = recordsCaches.getCache(transaction.getCollection());
			for (Record record : transaction.getRecords()) {
				if (record.isDirty() && record.isSaved() && cache.getCacheConfigOf(record.getSchemaCode()) != null) {
					throw new RecordServicesRuntimeException_CannotDelayFlushingOfRecordsInCache(record.getSchemaCode(),
							record.getId());
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
									modifiedMetadatas));
				} else {
					extensions.callRecordInCreationBeforeValidationAndAutomaticValuesCalculation(
							new RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent(record,
									transaction.getUser()));
				}
			}
		}

		if (transaction.getRecordUpdateOptions().isExtractorsEnabled()) {
			boolean validations = transaction.getRecordUpdateOptions().isValidationsEnabled();
			List<Record> records = DependencyUtils.sortRecordByDependency(types, transaction.getRecords());
			for (Record record : records) {
				recordPopulateServices.populate(record);

				if (onlyValidateRecord == null || onlyValidateRecord.equals(record.getId())) {
					if (transaction.getRecordUpdateOptions().isUpdateModificationInfos()) {
						updateCreationModificationUsersAndDates(record, transaction, types.getSchema(record.getSchemaCode()));
					}
					if (validations) {
						validationServices.validateManualMetadatas(record, recordProvider, transaction);
					}
					automaticMetadataServices
							.updateAutomaticMetadatas((RecordImpl) record, recordProvider, reindexation);
					if (validations) {
						validationServices.validateCyclicReferences(record, recordProvider, transaction);
						validationServices.validateAutomaticMetadatas(record, recordProvider, transaction);
						validationServices.validateSchemaUsingCustomSchemaValidator(record, recordProvider, transaction);
					}
				}

			}

		}

		for (Record record : transaction.getRecords()) {
			if (record.isDirty()) {
				if (record.isSaved()) {
					MetadataList modifiedMetadatas = record.getModifiedMetadatas(types);
					extensions.callRecordInModificationBeforeSave(
							new RecordInModificationBeforeSaveEvent(record, modifiedMetadatas));
				} else {
					extensions.callRecordInCreationBeforeSave(
							new RecordInCreationBeforeSaveEvent(record, transaction.getUser()));
				}
			}
		}

	}

	public void validateRecordInTransaction(Record record, Transaction transaction)
			throws ValidationException {
		prepareRecords(transaction, record.getId());
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
			if (!record.isModified(Schemas.MODIFIED_BY) && !userGroupOrCollection) {
				record.set(Schemas.MODIFIED_BY, currentUserId);
			}
			if (!record.isModified(Schemas.MODIFIED_ON)) {
				record.set(Schemas.MODIFIED_ON, now);
			}
		}

	}

	List<ModificationImpact> getModificationImpacts(Transaction transaction, boolean executedAfterTransaction) {
		SearchServices searchServices = modelFactory.newSearchServices();
		TaxonomiesManager taxonomiesManager = modelFactory.getTaxonomiesManager();
		MetadataSchemaTypes metadataSchemaTypes = modelFactory.getMetadataSchemasManager().getSchemaTypes(
				transaction.getCollection());

		return calculateImpactOfModification(transaction, taxonomiesManager, searchServices, metadataSchemaTypes,
				executedAfterTransaction);
	}

	void refreshRecordsAndCaches(String collection, List<Record> records, TransactionResponseDTO transactionResponseDTO,
			MetadataSchemaTypes types) {

		List<Record> recordsToInsert = new ArrayList<>();
		for (Record record : records) {
			RecordImpl recordImpl = (RecordImpl) record;
			long version = transactionResponseDTO.getNewDocumentVersion(record.getId());
			MetadataSchema schema = types.getSchema(record.getSchemaCode());

			recordImpl.markAsSaved(version, schema);
			recordsToInsert.add(record);
		}
		recordsCaches.insert(collection, recordsToInsert);

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
		if (!modifiedOrUnsavedRecords.isEmpty()) {
			TransactionDTO transactionDTO = createTransactionDTO(transaction, modifiedOrUnsavedRecords);
			try {
				MetadataSchemaTypes metadataSchemaTypes = modelFactory.getMetadataSchemasManager().getSchemaTypes(
						transaction.getCollection());
				List<RecordEvent> recordEvents = prepareRecordEvents(modifiedOrUnsavedRecords, metadataSchemaTypes);
				TransactionResponseDTO transactionResponseDTO = recordDao.execute(transactionDTO);

				modelFactory.newLoggingServices().logTransaction(transaction);
				refreshRecordsAndCaches(transaction.getCollection(), modifiedOrUnsavedRecords, transactionResponseDTO,
						metadataSchemaTypes);

				if (modificationImpactHandler != null) {
					modificationImpactHandler.handle();
				}

				callExtensions(transaction.getCollection(), recordEvents);

			} catch (OptimisticLocking e) {
				if (modificationImpactHandler != null) {
					modificationImpactHandler.cancel();
				}
				LOGGER.trace("Optimistic locking, handling with specified resolution {}", transaction.getRecordUpdateOptions()
						.getOptimisticLockingResolution().name(), e);
				handleOptimisticLocking(transactionDTO, transaction, modificationImpactHandler, e, attempt);
			} catch (ReferenceToNonExistentIndex e) {
				throw new NewReferenceToOtherLogicallyDeletedRecord(e.getId(), e);
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
					events.add(new RecordModificationEvent(record, modifiedMetadatas));
				}

			} else {
				events.add(new RecordCreationEvent(record));
			}
		}

		return events;
	}

	private void callExtensions(String collection, List<RecordEvent> recordEvents) {
		ModelLayerCollectionExtensions extensions = modelFactory.getExtensions().forCollection(collection);

		for (RecordEvent recordEvent : recordEvents) {
			if (recordEvent instanceof RecordCreationEvent) {
				extensions.callRecordCreated((RecordCreationEvent) recordEvent);

			} else if (recordEvent instanceof RecordModificationEvent) {
				extensions.callRecordModified((RecordModificationEvent) recordEvent);

			} else if (recordEvent instanceof RecordLogicalDeletionEvent) {
				extensions.callRecordLogicallyDeleted((RecordLogicalDeletionEvent) recordEvent);

			} else if (recordEvent instanceof RecordRestorationEvent) {
				extensions.callRecordRestored((RecordRestorationEvent) recordEvent);
			}
		}

	}

	TransactionDTO createTransactionDTO(Transaction transaction, List<Record> modifiedOrUnsavedRecords) {
		String collection = transaction.getCollection();
		List<RecordDTO> addedRecords = new ArrayList<>();
		List<RecordDeltaDTO> modifiedRecordDTOs = new ArrayList<>();
		LanguageDetectionManager languageDetectionManager = modelFactory.getLanguageDetectionManager();
		ContentManager contentManager = modelFactory.getContentManager();
		List<String> collectionLanguages = modelFactory.getCollectionsListManager().getCollectionLanguages(collection);
		List<FieldsPopulator> fieldsPopulators = new ArrayList<>();
		MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		fieldsPopulators.add(new SearchFieldsPopulator(
				types, transaction.getRecordUpdateOptions().isFullRewrite(), contentManager, collectionLanguages));
		fieldsPopulators.add(new AutocompleteFieldPopulator());
		fieldsPopulators.add(new SortFieldsPopulator(types, transaction.getRecordUpdateOptions().isFullRewrite()));

		Factory<EncryptionServices> encryptionServicesFactory = new Factory<EncryptionServices>() {
			@Override
			public EncryptionServices get() {
				return modelLayerFactory.newEncryptionServices();
			}
		};

		for (Record record : modifiedOrUnsavedRecords) {
			MetadataSchema schema = modelFactory.getMetadataSchemasManager().getSchemaTypes(collection)
					.getSchema(record.getSchemaCode());
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
		return new TransactionDTO(
				transaction.getId(), transaction.getRecordUpdateOptions().getRecordsFlushing(), addedRecords, modifiedRecordDTOs)
				.withSkippingReferenceToLogicallyDeletedValidation(transaction.isSkippingReferenceToLogicallyDeletedValidation())
				.withFullRewrite(transaction.getRecordUpdateOptions().isFullRewrite());
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
		} else if (!schema.isInTransactionLog()) {
			id = uniqueIdGenerator.next() + "ZZ";
		} else {
			id = uniqueIdGenerator.next();
		}
		return newRecordWithSchema(schema, id);
	}

	public RecordAutomaticMetadataServices newAutomaticMetadataServices() {
		return new RecordAutomaticMetadataServices(modelFactory.getMetadataSchemasManager(), modelFactory.getTaxonomiesManager(),
				modelFactory.getSystemConfigurationsManager(), modelFactory.getModelLayerLogger());
	}

	public RecordValidationServices newRecordValidationServices() {
		return new RecordValidationServices(newConfigProvider(), modelFactory.getMetadataSchemasManager(),
				modelFactory.newSearchServices(), modelFactory.newAuthorizationsServices());
	}

	ConfigProvider newConfigProvider() {
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

	RecordProvider newRecordProvider(RecordProvider nestedProvider, Transaction transaction) {
		return new RecordProvider(modelLayerFactory.newRecordServices(), nestedProvider, transaction.getRecords(), transaction);
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

	public List<ModificationImpact> calculateImpactOfModification(Transaction transaction, TaxonomiesManager taxonomiesManager,
			SearchServices searchServices, MetadataSchemaTypes metadataSchemaTypes, boolean executedAfterTransaction) {

		if (transaction.getRecords().isEmpty()) {
			return new ArrayList<>();
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
		return new ModificationImpactCalculator(metadataSchemaTypes, taxonomies, searchServices);

	}

	public boolean isRestorable(Record record, User user) {
		refresh(record);
		refresh(user);
		return newRecordDeleteServices().isRestorable(record, user);
	}

	public void restore(Record record, User user) {

		refresh(record);
		refresh(user);
		newRecordDeleteServices().restore(record, user);
	}

	public boolean isPhysicallyDeletable(Record record, User user) {
		refresh(record);
		refresh(user);
		return newRecordDeleteServices().isPhysicallyDeletable(record, user);
	}

	public void physicallyDelete(Record record, User user) {
		refresh(record);
		refresh(user);
		newRecordDeleteServices().physicallyDelete(record, user);
	}

	public boolean isLogicallyDeletable(Record record, User user) {
		refresh(record);
		refresh(user);
		return newRecordDeleteServices().isLogicallyDeletable(record, user);
	}

	public boolean isLogicallyThenPhysicallyDeletable(Record record, User user) {
		refresh(record);
		refresh(user);
		return newRecordDeleteServices().isLogicallyThenPhysicallyDeletable(record, user);
	}

	public boolean isPrincipalConceptLogicallyDeletableExcludingContent(Record record, User user) {
		refresh(record);
		refresh(user);
		return newRecordDeleteServices().isPrincipalConceptLogicallyDeletableExcludingContent(record, user);
	}

	public boolean isPrincipalConceptLogicallyDeletableIncludingContent(Record record, User user) {
		refresh(record);
		refresh(user);
		return newRecordDeleteServices().isPrincipalConceptLogicallyDeletableIncludingContent(record, user);
	}

	public void logicallyDelete(Record record, User user) {
		refresh(record);
		refresh(user);
		newRecordDeleteServices().logicallyDelete(record, user);
		refresh(record);
	}

	public void logicallyDeletePrincipalConceptIncludingRecords(Record record, User user) {
		refresh(record);
		refresh(user);
		newRecordDeleteServices().logicallyDeletePrincipalConceptIncludingRecords(record, user);
		refresh(record);
	}

	public void logicallyDeletePrincipalConceptExcludingRecords(Record record, User user) {
		refresh(record);
		refresh(user);
		newRecordDeleteServices().logicallyDeletePrincipalConceptExcludingRecords(record, user);
		refresh(record);
	}

	public List<Record> getVisibleRecordsWithReferenceTo(Record record, User user) {

		refresh(record);
		refresh(user);
		return newRecordDeleteServices().getVisibleRecordsWithReferenceToRecordInHierarchy(record, user);
	}

	public boolean isReferencedByOtherRecords(Record record) {
		return newRecordDeleteServices().isReferencedByOtherRecords(record);
	}

	private void validateNotTooMuchRecords(Transaction transaction) {
		if (transaction.getRecords().size() > 10000) {
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

	public void removeOldLocks() {
		recordDao.removeOldLocks();
	}

	public void recalculate(RecordWrapper recordWrapper) {
		recalculate(recordWrapper.getWrappedRecord());
	}

	public void recalculate(Record record) {
		newAutomaticMetadataServices().updateAutomaticMetadatas(
				(RecordImpl) record, newRecordProviderWithoutPreloadedRecords(), new TransactionRecordsReindexation());
	}

}
