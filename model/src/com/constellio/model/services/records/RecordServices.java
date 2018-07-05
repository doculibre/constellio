package com.constellio.model.services.records;

import java.util.List;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.ModificationImpactCalculatorResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public interface RecordServices {

	void add(Record record)
			throws RecordServicesException;

	void add(Record record, User user)
			throws RecordServicesException;

	void add(RecordWrapper wrapper)
			throws RecordServicesException;

	void add(RecordWrapper wrapper, User user)
			throws RecordServicesException;

	List<BatchProcess> executeHandlingImpactsAsync(Transaction transaction)
			throws RecordServicesException;

	void execute(Transaction transaction)
			throws RecordServicesException;

	void executeWithoutImpactHandling(Transaction transaction)
			throws RecordServicesException;

	void executeWithImpactHandler(Transaction transaction, RecordModificationImpactHandler handler)
			throws RecordServicesException;

	Record toRecord(RecordDTO recordDTO, boolean fullyLoaded);

	List<Record> toRecords(List<RecordDTO> recordDTOs, boolean fullyLoaded);

	long documentsCount();

	void update(RecordWrapper wrapper)
			throws RecordServicesException;

	void update(Record record)
			throws RecordServicesException;

	void update(Record record, RecordUpdateOptions options)
			throws RecordServicesException;

	void update(RecordWrapper wrapper, User user)
			throws RecordServicesException;

	void update(Record record, User user)
			throws RecordServicesException;

	void update(Record record, RecordUpdateOptions options, User user)
			throws RecordServicesException;

	void update(List<Record> records, User user)
			throws RecordServicesException;

	Record getRecordByMetadata(Metadata metadata, String value);

	Record realtimeGetById(MetadataSchemaType schemaType, String id);

	Record realtimeGetById(String dataStore, String id);

	Record realtimeGetRecordById(String id);

	Record getDocumentById(String id);

	Record getDocumentById(String id, User user);

	Record getById(MetadataSchemaType schemaType, String id);

	Record getById(String dataStore, String id);

	List<Record> getRecordsById(String collection, List<String> ids);

	void validateTransaction(Transaction transaction)
			throws ValidationException;

	void validateRecordInTransaction(Record record, Transaction transaction)
			throws ValidationException;

	void validateRecord(Record record)
			throws RecordServicesException.ValidationException;

	Record newRecordWithSchema(MetadataSchema schema, String id);

	Record newRecordWithSchema(MetadataSchema schema);

	void refresh(Record... records);

	void refresh(RecordWrapper... recordWrappers);

	void refresh(List<?> records);

	void refreshUsingCache(Record... records);

	void refreshUsingCache(RecordWrapper... recordWrappers);

	void refreshUsingCache(List<?> records);

	List<BatchProcess> updateAsync(Record record)
			throws RecordServicesException;

	List<BatchProcess> updateAsync(Record record, RecordUpdateOptions options)
			throws RecordServicesException;

	ModificationImpactCalculatorResponse calculateImpactOfModification(Transaction transaction,
			TaxonomiesManager taxonomiesManager,
			SearchServices searchServices, MetadataSchemaTypes metadataSchemaTypes, boolean executedAfterTransaction);

	RecordsCaches getRecordsCaches();

	boolean isRestorable(Record record, User user);

	void restore(Record record, User user);

	boolean isPhysicallyDeletable(Record record, User user);

	void physicallyDelete(Record record, User user);

	void physicallyDelete(Record record, User user, RecordPhysicalDeleteOptions options);

	void physicallyDeleteNoMatterTheStatus(Record record, User user, RecordPhysicalDeleteOptions options);

	boolean isLogicallyDeletable(Record record, User user);

	boolean isLogicallyDeletableAndIsSkipValidation(Record record, User user);

	boolean isLogicallyThenPhysicallyDeletable(Record record, User user);

	void logicallyDelete(Record record, User user);

	void logicallyDelete(Record record, User user, RecordLogicalDeleteOptions options);

	List<Record> getVisibleRecordsWithReferenceTo(Record record, User user);

	boolean isReferencedByOtherRecords(Record record);

	void flush();

	void flushRecords();

	void removeOldLocks();

	void recalculate(RecordWrapper recordWrapper);

	void recalculate(Record record);

	void loadLazyTransientMetadatas(Record record);

	void reloadEagerTransientMetadatas(Record record);
}
