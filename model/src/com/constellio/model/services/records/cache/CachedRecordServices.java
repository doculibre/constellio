package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.BaseRecordServices;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordLogicalDeleteOptions;
import com.constellio.model.services.records.RecordModificationImpactHandler;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.ModificationImpactCalculatorResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

import java.util.List;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.data.dao.services.records.DataStore.RECORDS;

public class CachedRecordServices extends BaseRecordServices implements RecordServices {

	RecordsCaches disconnectableRecordsCaches;

	RecordServices recordServices;

	public CachedRecordServices(ModelLayerFactory modelLayerFactory, RecordServices recordServices,
								RecordsCaches recordsCaches) {
		super(modelLayerFactory);
		this.recordServices = recordServices;
		this.disconnectableRecordsCaches = recordsCaches;
	}

	public RecordsCaches getConnectedRecordsCache() {
		if (disconnectableRecordsCaches != null && (disconnectableRecordsCaches instanceof RecordsCachesRequestMemoryImpl)) {
			if (((RecordsCachesRequestMemoryImpl) disconnectableRecordsCaches).isDisconnected()) {
				disconnectableRecordsCaches = modelLayerFactory.getModelLayerFactoryFactory().get().getRecordsCaches();
			}
		}
		return disconnectableRecordsCaches;
	}

	@Override
	public Record getDocumentById(String id) {
		return getById(RECORDS, id);
	}

	@Override
	public Record getById(String dataStore, String id) {
		Record record = RECORDS.equals(dataStore) ? getConnectedRecordsCache().getRecord(id) : null;
		if (record == null) {
			record = recordServices.getById(dataStore, id);
		}
		return record;
	}

	public Record getById(MetadataSchemaType schemaType, String id) {
		RecordsCache cache = getConnectedRecordsCache().getCache(schemaType.getCollection());
		Record record = cache.get(id);
		if (record == null) {
			record = recordServices.getById(schemaType, id);
		}
		return record;
	}

	@Override
	public Record realtimeGetRecordById(String id) {
		return realtimeGetById(RECORDS, id);
	}

	@Override
	public Record realtimeGetById(MetadataSchemaType schemaType, String id) {
		Record record = getConnectedRecordsCache().getRecord(id);
		if (record == null) {
			record = recordServices.realtimeGetById(schemaType, id);
		}
		return record;
	}

	@Override
	public Record realtimeGetById(String dataStore, String id) {
		Record record = getConnectedRecordsCache().getRecord(id);
		if (record == null) {
			record = recordServices.realtimeGetById(dataStore, id);
		}
		return record;
	}

	@Override
	public Record getRecordByMetadata(Metadata metadata, String value) {
		if (!metadata.isUniqueValue()) {
			throw new IllegalArgumentException("Metadata '" + metadata + "' is not unique");
		}
		if (metadata.getCode().startsWith("global_")) {
			throw new IllegalArgumentException("Metadata '" + metadata + "' is global, which has no specific schema type.");
		}

		Record foundRecord = getConnectedRecordsCache().getCache(metadata.getCollection()).getByMetadata(metadata, value);

		if (foundRecord == null) {
			foundRecord = recordServices.getRecordByMetadata(metadata, value);
			if (foundRecord != null) {
				getConnectedRecordsCache().insert(foundRecord, WAS_OBTAINED);
			}
		}

		return foundRecord;
	}

	@Override
	public List<Record> getRecordsById(String collection,
									   List<String> ids) {

		//TODO

		//		RecordsCache cache = recordsCaches.getCache(collection);
		//		List<Record> records = new ArrayList<>();
		//		List<String> unfoundIds = new ArrayList<>();
		//		for (int i = 0; i < ids.size(); i++) {
		//			Record cachedRecord = cache.get(ids.get(i));
		//			records.add(cachedRecord);
		//			if (cachedRecord == null) {
		//				unfoundIds.add(ids.get(i));
		//			}
		//		}
		//
		//		if (!unfoundIds.isEmpty()) {
		//			List<Record> missingRecords = recordServices.getRecordsById(collection, unfoundIds);
		//			for(int i = 0 ; i < ids.size() ; i++) {
		//				if (records.get(i) == null) {
		//					int index =
		//				}
		//			}
		//			int index = ids.indexOf(m)
		//		}

		return recordServices.getRecordsById(collection, ids);
	}

	@Override
	public void prepareRecords(Transaction transaction) throws ValidationException {
		recordServices.prepareRecords(transaction);
	}

	@Override
	public void refresh(List<?> records) {
		recordServices.refresh(records);
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
					Record recordFromCache = getDocumentById(record.getId());
					RecordDTO recordDTO = ((RecordImpl) recordFromCache).getRecordDTO();
					((RecordImpl) record).refresh(recordDTO.getVersion(), recordDTO);
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					((RecordImpl) record).markAsDisconnected();
				}
			}
		}
	}

	//These services are not cached

	@Override
	public List<Record> toRecords(
			List<RecordDTO> recordDTOs, boolean fullyLoaded) {
		return recordServices.toRecords(recordDTOs, fullyLoaded);
	}

	@Override
	public void executeInBatch(Transaction transaction)
			throws RecordServicesException {
		recordServices.executeInBatch(transaction);
	}

	@Override
	public void execute(Transaction transaction)
			throws RecordServicesException {
		recordServices.execute(transaction);
	}

	@Override
	public void executeWithoutImpactHandling(Transaction transaction)
			throws RecordServicesException {
		recordServices.executeWithoutImpactHandling(transaction);
	}

	@Override
	public void executeWithImpactHandler(Transaction transaction,
										 RecordModificationImpactHandler handler)
			throws RecordServicesException {
		recordServices.executeWithImpactHandler(transaction, handler);
	}

	@Override
	public List<BatchProcess> executeHandlingImpactsAsync(Transaction transaction)
			throws RecordServicesException {
		return recordServices.executeHandlingImpactsAsync(transaction);
	}

	@Override
	public Record toRecord(RecordDTO recordDTO, boolean fullyLoaded) {
		return recordServices.toRecord(recordDTO, fullyLoaded);
	}

	@Override
	public long documentsCount() {
		return recordServices.documentsCount();
	}

	@Override
	public void validateRecordInTransaction(Record record,
											Transaction transaction)
			throws ValidationException {
		recordServices.validateRecordInTransaction(record, transaction);
	}

	@Override
	public void validateRecord(Record record)
			throws ValidationException {
		recordServices.validateRecord(record);
	}

	@Override
	public Record newRecordWithSchema(MetadataSchema schema, String id, boolean withDefaultValues) {
		return recordServices.newRecordWithSchema(schema, id, withDefaultValues);
	}

	@Override
	public Record newRecordWithSchema(
			MetadataSchema schema, String id) {
		return recordServices.newRecordWithSchema(schema, id);
	}

	@Override
	public Record newRecordWithSchema(MetadataSchema schema, boolean isWithDefaultValues) {
		return recordServices.newRecordWithSchema(schema, isWithDefaultValues);
	}

	@Override
	public Record newRecordWithSchema(
			MetadataSchema schema) {
		return recordServices.newRecordWithSchema(schema);
	}

	@Override
	public ModificationImpactCalculatorResponse calculateImpactOfModification(
			Transaction transaction,
			TaxonomiesManager taxonomiesManager,
			SearchServices searchServices,
			MetadataSchemaTypes metadataSchemaTypes, boolean executedAfterTransaction) {
		return recordServices.calculateImpactOfModification(transaction, taxonomiesManager, searchServices, metadataSchemaTypes,
				executedAfterTransaction);
	}

	@Override
	public RecordsCaches getRecordsCaches() {
		return getConnectedRecordsCache();
	}

	@Override
	public boolean isRestorable(Record record,
								User user) {
		return recordServices.isRestorable(record, user);
	}

	@Override
	public void restore(Record record,
						User user) {
		recordServices.restore(record, user);
	}

	@Override
	public ValidationErrors validatePhysicallyDeletable(Record record, User user) {
		return recordServices.validatePhysicallyDeletable(record, user);
	}

	@Override
	public void physicallyDelete(Record record,
								 User user) {
		recordServices.physicallyDelete(record, user);
	}

	@Override
	public void physicallyDelete(Record record,
								 User user, RecordPhysicalDeleteOptions options) {
		recordServices.physicallyDelete(record, user, options);
	}

	@Override
	public void physicallyDeleteNoMatterTheStatus(Record record, User user, RecordPhysicalDeleteOptions options) {
		recordServices.physicallyDeleteNoMatterTheStatus(record, user, options);
	}

	@Override
	public ValidationErrors validateLogicallyDeletable(Record record,
													   User user) {
		return recordServices.validateLogicallyDeletable(record, user);
	}

	@Override
	public boolean isLogicallyDeletableAndIsSkipValidation(Record record, User user) {
		return recordServices.isLogicallyDeletableAndIsSkipValidation(record, user);
	}

	@Override
	public ValidationErrors validateLogicallyThenPhysicallyDeletable(Record record, User user) {
		return recordServices.validateLogicallyThenPhysicallyDeletable(record, user);
	}

	@Override
	public void logicallyDelete(Record record,
								User user) {
		recordServices.logicallyDelete(record, user);
	}

	@Override
	public void logicallyDelete(Record record,
								User user, RecordLogicalDeleteOptions options) {
		recordServices.logicallyDelete(record, user, options);
	}

	@Override
	public List<Record> getVisibleRecordsWithReferenceTo(
			Record record, User user) {
		return recordServices.getVisibleRecordsWithReferenceTo(record, user);
	}

	@Override
	public boolean isReferencedByOtherRecords(Record record) {
		return recordServices.isReferencedByOtherRecords(record);
	}

	@Override
	public void flush() {
		recordServices.flush();
	}

	@Override
	public void flushRecords() {
		recordServices.flushRecords();
	}

	@Override
	public void removeOldLocks() {
		recordServices.removeOldLocks();
	}

	@Override
	public void recalculate(RecordWrapper recordWrapper) {
		recordServices.recalculate(recordWrapper);
	}

	@Override
	public void recalculate(Record record) {
		recordServices.recalculate(record);
	}

	@Override
	public void loadLazyTransientMetadatas(Record record) {
		recordServices.loadLazyTransientMetadatas(record);
	}

	@Override
	public void reloadEagerTransientMetadatas(Record record) {
		recordServices.reloadEagerTransientMetadatas(record);
	}

	@Override
	public SecurityModel getSecurityModel(String collection) {
		return recordServices.getSecurityModel(collection);
	}
}
