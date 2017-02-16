package com.constellio.model.services.records.cache;

import java.util.List;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.BaseRecordServices;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordLogicalDeleteOptions;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordModificationImpactHandler;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class CachedRecordServices extends BaseRecordServices implements RecordServices {

	RecordsCaches recordsCaches;

	RecordServices recordServices;

	public CachedRecordServices(ModelLayerFactory modelLayerFactory, RecordServices recordServices, RecordsCaches recordsCaches) {
		super(modelLayerFactory);
		this.recordServices = recordServices;
		this.recordsCaches = recordsCaches;
	}

	@Override
	public Record getDocumentById(String id) {
		Record record = recordsCaches.getRecord(id);
		if (record == null) {
			record = recordServices.getDocumentById(id);
			recordsCaches.insert(record);
		}
		return record;
	}

	@Override
	public Record getRecordByMetadata(Metadata metadata, String value) {
		if (!metadata.isUniqueValue()) {
			throw new IllegalArgumentException("Metadata '" + metadata + "' is not unique");
		}
		if (metadata.getCode().startsWith("global")) {
			throw new IllegalArgumentException("Metadata '" + metadata + "' is global, which has no specific schema type.");
		}

		Record foundRecord = recordsCaches.getCache(metadata.getCollection()).getByMetadata(metadata, value);

		if (foundRecord == null) {
			foundRecord = recordServices.getRecordByMetadata(metadata, value);
			if (foundRecord != null) {
				recordsCaches.insert(foundRecord);
			}
		}

		return foundRecord;
	}

	@Override
	public List<Record> getRecordsById(String collection,
			List<String> ids) {

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
	public void refresh(List<?> records) {
		recordServices.refresh(records);
	}

	@Override
	public List<String> getRecordTitles(String collection, List<String> recordIds) {
		return recordServices.getRecordTitles(collection, recordIds);
	}

	//These services are not cached

	@Override
	public List<Record> toRecords(
			List<RecordDTO> recordDTOs, boolean fullyLoaded) {
		return recordServices.toRecords(recordDTOs, fullyLoaded);
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
	public Record newRecordWithSchema(
			MetadataSchema schema, String id) {
		return recordServices.newRecordWithSchema(schema, id);
	}

	@Override
	public Record newRecordWithSchema(
			MetadataSchema schema) {
		return recordServices.newRecordWithSchema(schema);
	}

	@Override
	public List<ModificationImpact> calculateImpactOfModification(
			Transaction transaction,
			TaxonomiesManager taxonomiesManager,
			SearchServices searchServices,
			MetadataSchemaTypes metadataSchemaTypes, boolean executedAfterTransaction) {
		return recordServices.calculateImpactOfModification(transaction, taxonomiesManager, searchServices, metadataSchemaTypes,
				executedAfterTransaction);
	}

	@Override
	public RecordsCaches getRecordsCaches() {
		return recordsCaches;
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
	public boolean isPhysicallyDeletable(Record record,
			User user) {
		return recordServices.isPhysicallyDeletable(record, user);
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
	public boolean isLogicallyDeletable(Record record,
			User user) {
		return recordServices.isLogicallyDeletable(record, user);
	}

	@Override
	public boolean isLogicallyThenPhysicallyDeletable(Record record,
			User user) {
		return recordServices.isLogicallyThenPhysicallyDeletable(record, user);
	}

	@Override
	public boolean isPrincipalConceptLogicallyDeletableExcludingContent(Record record,
			User user) {
		return recordServices.isPrincipalConceptLogicallyDeletableExcludingContent(record, user);
	}

	@Override
	public boolean isPrincipalConceptLogicallyDeletableIncludingContent(Record record,
			User user) {
		return recordServices.isPrincipalConceptLogicallyDeletableIncludingContent(record, user);
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
	public void loadLazyTransientMetadatas(RecordImpl record) {
		recordServices.loadLazyTransientMetadatas(record);
	}

	@Override
	public void reloadEagerTransientMetadatas(RecordImpl record) {
		recordServices.reloadEagerTransientMetadatas(record);
	}
}
