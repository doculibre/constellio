/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException.RecordDaoRuntimeException_RecordsFlushingFailed;
import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException.ReferenceToNonExistentIndex;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordRuntimeException;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionEventsListeners;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentModifications;
import com.constellio.model.services.contents.ContentModificationsBuilder;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.model.services.records.RecordServicesException.UnresolvableOptimisticLockingConflict;
import com.constellio.model.services.records.RecordServicesRuntimeException.NewReferenceToOtherLogicallyDeletedRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_RecordsFlushingFailed;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_TransactionHasMoreThan100000Records;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution;
import com.constellio.model.services.records.populators.AutocompleteFieldPopulator;
import com.constellio.model.services.records.populators.PathsFieldPopulator;
import com.constellio.model.services.records.populators.SearchFieldsPopulator;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.ModificationImpactCalculator;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.DependencyUtilsRuntimeException.CyclicDependency;

public class RecordServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordServices.class);

	private final RecordDao recordDao;
	private final RecordDao eventsDao;
	private final RecordDao notificationsDao;
	private final ModelLayerFactory modelFactory;
	private final UniqueIdGenerator uniqueIdGenerator;

	public RecordServices(RecordDao recordDao, RecordDao eventsDao, RecordDao notificationsDao, ModelLayerFactory modelFactory,
			DataStoreTypesFactory typesFactory, UniqueIdGenerator uniqueIdGenerator) {
		this.recordDao = recordDao;
		this.eventsDao = eventsDao;
		this.notificationsDao = notificationsDao;
		this.modelFactory = modelFactory;
		this.uniqueIdGenerator = uniqueIdGenerator;
	}

	public void add(Record record)
			throws RecordServicesException {
		add(record, null);
	}

	public void add(Record record, User user)
			throws RecordServicesException {
		Transaction transaction = new Transaction().setUser(user);
		transaction.addUpdate(record);
		execute(transaction);
	}

	public void add(RecordWrapper wrapper)
			throws RecordServicesException {
		add(wrapper.getWrappedRecord(), null);
	}

	public void add(RecordWrapper wrapper, User user)
			throws RecordServicesException {
		add(wrapper.getWrappedRecord(), user);
	}

	public List<BatchProcess> executeHandlingImpactsAsync(Transaction transaction)
			throws RecordServicesException {
		AddToBatchProcessImpactHandler handler = addToBatchProcessModificationImpactHandler();
		executeWithImpactHandler(transaction, handler);
		return handler.getAllCreatedBatchProcesses();
	}

	public void execute(Transaction transaction)
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
			saveContentsAndRecords(transaction, null);
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

	public void executeWithImpactHandler(Transaction transaction, RecordModificationImpactHandler handler)
			throws RecordServicesException {

		validateNotTooMuchRecords(transaction);

		prepareRecords(transaction);
		if (handler != null) {
			List<ModificationImpact> impacts = getModificationImpacts(transaction, true);

			for (ModificationImpact modificationImpact : impacts) {
				handler.prepareToHandle(modificationImpact);
			}
		}

		saveContentsAndRecords(transaction, handler);

	}

	void handleOptimisticLocking(Transaction transaction, RecordModificationImpactHandler handler, OptimisticLocking e)
			throws RecordServicesException {
		OptimisticLockingResolution resolution = transaction.getRecordUpdateOptions().getOptimisticLockingResolution();

		if (resolution == OptimisticLockingResolution.EXCEPTION) {
			throw new RecordServicesException.OptimisticLocking(e);
		} else if (resolution == OptimisticLockingResolution.TRY_MERGE) {
			mergeRecords(transaction);
			if (handler == null) {
				execute(transaction);
			} else {
				executeWithImpactHandler(transaction, handler);
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

	public Record toRecord(RecordDTO recordDTO) {
		return new RecordImpl(recordDTO);
	}

	public List<Record> toRecords(List<RecordDTO> recordDTOs) {
		List<Record> records = new ArrayList<Record>();
		for (RecordDTO recordDTO : recordDTOs) {
			records.add(toRecord(recordDTO));
		}

		return Collections.unmodifiableList(records);
	}

	public long documentsCount() {
		return recordDao.documentsCount();
	}

	public void update(RecordWrapper wrapper)
			throws RecordServicesException {
		update(wrapper, null);
	}

	public void update(Record record)
			throws RecordServicesException {
		update(record, new RecordUpdateOptions(), null);
	}

	public void update(Record record, RecordUpdateOptions options)
			throws RecordServicesException {
		update(record, options, null);
	}

	public void update(RecordWrapper wrapper, User user)
			throws RecordServicesException {
		update(wrapper.getWrappedRecord(), user);
	}

	public void update(Record record, User user)
			throws RecordServicesException {
		update(record, new RecordUpdateOptions(), user);
	}

	public void update(Record record, RecordUpdateOptions options, User user)
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.setUser(user);
		transaction.addUpdate(record);
		transaction.setOptions(options);
		execute(transaction);
	}

	public Record getDocumentById(String id) {
		try {
			return new RecordImpl(recordDao.get(id));
		} catch (NoSuchRecordWithId e) {
			throw new RecordServicesRuntimeException.NoSuchRecordWithId(id, e);
		}
	}

	public Record getDocumentById(String id, User user) {
		try {
			RecordImpl record = new RecordImpl(recordDao.get(id));
			if (modelFactory.newAuthorizationsServices().canRead(user, record)) {
				return record;
			} else {
				throw new RecordServicesRuntimeException.UserCannotReadDocument(id, user.getUsername());
			}
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

		RecordProvider recordProvider = newRecordProvider(null, transaction);
		RecordValidationServices validationServices = newRecordValidationServices();
		RecordAutomaticMetadataServices automaticMetadataServices = newAutomaticMetadataServices();
		TransactionRecordsReindexation reindexation = transaction.getRecordUpdateOptions().getTransactionRecordsReindexation();
		MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(transaction.getCollection());
		boolean valdations = transaction.getRecordUpdateOptions().isValidationsEnabled();
		for (Record record : transaction.getRecords()) {
			if (transaction.getRecordUpdateOptions().isUpdateModificationInfos()) {
				updateCreationModificationUsersAndDates(record, transaction, types.getSchema(record.getSchemaCode()));
			}
			if (valdations) {
				validationServices.validateManualMetadatas(record, recordProvider, transaction);
			}
			automaticMetadataServices
					.updateAutomaticMetadatas((RecordImpl) record, recordProvider, reindexation);
			if (valdations) {
				validationServices.validateCyclicReferences(record, recordProvider, transaction);
				validationServices.validateAutomaticMetadatas(record, recordProvider, transaction);
				validationServices.validateSchemaUsingCustomSchemaValidator(record, recordProvider, transaction);
			}
		}
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

	void refreshRecords(List<Record> records, TransactionResponseDTO transactionResponseDTO, MetadataSchemaTypes types) {

		for (Record record : records) {
			RecordImpl recordImpl = (RecordImpl) record;
			long version = transactionResponseDTO.getNewDocumentVersion(record.getId());
			MetadataSchema schema = types.getSchema(record.getSchemaCode());
			recordImpl.markAsSaved(version, schema);
		}

	}

	void saveContentsAndRecords(Transaction transaction, RecordModificationImpactHandler modificationImpactHandler)
			throws RecordServicesException {
		ContentManager contentManager = modelFactory.getContentManager();
		MetadataSchemaTypes types = modelFactory.getMetadataSchemasManager().getSchemaTypes(transaction.getCollection());
		ContentModifications contentModificationsBuilder = findContentsModificationsIn(types, transaction);

		try {
			for (String deletedContent : contentModificationsBuilder.getDeletedContentsVersionsHashes()) {
				System.out.println("Mark for deletion> " + deletedContent);
				contentManager.silentlyMarkForDeletionIfNotReferenced(deletedContent);
			}
			saveTransactionDTO(transaction, modificationImpactHandler);

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

	void saveTransactionDTO(Transaction transaction, RecordModificationImpactHandler modificationImpactHandler)
			throws RecordServicesException {

		List<Record> modifiedOrUnsavedRecords = transaction.getModifiedRecords();
		TransactionDTO transactionDTO = createTransactionDTO(transaction, modifiedOrUnsavedRecords);
		try {
			MetadataSchemaTypes metadataSchemaTypes = modelFactory.getMetadataSchemasManager().getSchemaTypes(
					transaction.getCollection());
			List<RecordEvent> recordEvents = prepareRecordEvents(modifiedOrUnsavedRecords, metadataSchemaTypes);
			TransactionResponseDTO transactionResponseDTO = recordDao.execute(transactionDTO);

			modelFactory.newLoggingServices().logTransaction(transaction);
			refreshRecords(modifiedOrUnsavedRecords, transactionResponseDTO, metadataSchemaTypes);

			if (modificationImpactHandler != null) {
				modificationImpactHandler.handle();
			}

			notifyListeners(transaction.getCollection(), recordEvents);

		} catch (OptimisticLocking e) {
			if (modificationImpactHandler != null) {
				modificationImpactHandler.cancel();
			}
			LOGGER.trace("Optimistic locking, handling with specified resolution {}", transaction.getRecordUpdateOptions()
					.getOptimisticLockingResolution().name(), e);
			handleOptimisticLocking(transaction, modificationImpactHandler, e);
		} catch (ReferenceToNonExistentIndex e) {
			throw new NewReferenceToOtherLogicallyDeletedRecord(e.getId(), e);
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

	private void notifyListeners(String collection, List<RecordEvent> recordEvents) {
		ModelLayerCollectionEventsListeners listeners = modelFactory.getExtensions().getCollectionListeners(collection);
		for (RecordEvent recordEvent : recordEvents) {
			if (recordEvent instanceof RecordCreationEvent) {
				listeners.recordsCreationListeners.notify((RecordCreationEvent) recordEvent);

			} else if (recordEvent instanceof RecordModificationEvent) {
				listeners.recordsModificationListeners.notify((RecordModificationEvent) recordEvent);

			} else if (recordEvent instanceof RecordLogicalDeletionEvent) {
				listeners.recordsLogicallyDeletionListeners.notify((RecordLogicalDeletionEvent) recordEvent);

			} else if (recordEvent instanceof RecordRestorationEvent) {
				listeners.recordsRestorationListeners.notify((RecordRestorationEvent) recordEvent);
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
		fieldsPopulators.add(new SearchFieldsPopulator(types, languageDetectionManager, contentManager, collectionLanguages));
		fieldsPopulators.add(new PathsFieldPopulator(types));
		fieldsPopulators.add(new AutocompleteFieldPopulator());
		//		fieldsPopulators.add(new SpellCheckFieldPopulator(types));
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
		return new TransactionDTO(transaction.getId(), transaction.getRecordUpdateOptions().getRecordsFlushing(), addedRecords,
				modifiedRecordDTOs);
	}

	public Record newRecordWithSchema(MetadataSchema schema, String id) {
		return new RecordImpl(schema.getCode(), schema.getCollection(), id);
	}

	public Record newRecordWithSchema(MetadataSchema schema) {
		String id;
		if ("collection_default".equals(schema.getCode())) {
			id = schema.getCollection();
		} else {
			id = uniqueIdGenerator.next();
		}
		return newRecordWithSchema(schema, id);
	}

	public RecordAutomaticMetadataServices newAutomaticMetadataServices() {
		return new RecordAutomaticMetadataServices(modelFactory.getMetadataSchemasManager(), modelFactory.getTaxonomiesManager(),
				modelFactory.getSystemConfigurationsManager());
	}

	public RecordValidationServices newRecordValidationServices() {
		return new RecordValidationServices(modelFactory.getMetadataSchemasManager(), modelFactory.newSearchServices(),
				modelFactory.newAuthorizationsServices());
	}

	public RecordDeleteServices newRecordDeleteServices() {
		return new RecordDeleteServices(recordDao, modelFactory);
	}

	public AddToBatchProcessImpactHandler addToBatchProcessModificationImpactHandler() {
		return new AddToBatchProcessImpactHandler(modelFactory.getBatchProcessesManager(), modelFactory.newSearchServices());
	}

	public void refresh(Record... records) {
		refresh(Arrays.asList(records));
	}

	public void refresh(RecordWrapper... recordWrappers) {
		for (RecordWrapper wrapper : recordWrappers) {
			if (wrapper != null && wrapper.getWrappedRecord() != null) {
				refresh(wrapper.getWrappedRecord());
			}
		}
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
		return new RecordProvider(this, nestedProvider, transaction.getModifiedRecords(), transaction);
	}

	RecordProvider newRecordProviderWithoutPreloadedRecords() {
		return new RecordProvider(this, null, null, null);
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

	public List<BatchProcess> updateAsync(Record record)
			throws RecordServicesException {
		return updateAsync(record, new RecordUpdateOptions());
	}

	public List<BatchProcess> updateAsync(Record record, RecordUpdateOptions options)
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.update(record);
		transaction.setOptions(options);
		return executeHandlingImpactsAsync(transaction);
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
	}

	public void logicallyDeletePrincipalConceptIncludingRecords(Record record, User user) {
		refresh(record);
		refresh(user);
		newRecordDeleteServices().logicallyDeletePrincipalConceptIncludingRecords(record, user);
	}

	public void logicallyDeletePrincipalConceptExcludingRecords(Record record, User user) {
		refresh(record);
		refresh(user);
		newRecordDeleteServices().logicallyDeletePrincipalConceptExcludingRecords(record, user);

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
