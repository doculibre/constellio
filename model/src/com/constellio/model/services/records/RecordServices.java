package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.ModificationImpactCalculatorResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface RecordServices {

	void add(Supplier<Record> record)
			throws RecordServicesException;

	void add(Supplier<Record> record, User user)
			throws RecordServicesException;

	List<BatchProcess> executeHandlingImpactsAsync(Transaction transaction)
			throws RecordServicesException;

	void executeInBatch(Transaction transaction)
			throws RecordServicesException;

	void execute(Transaction transaction)
			throws RecordServicesException;

	void executeWithoutImpactHandling(Transaction transaction)
			throws RecordServicesException;

	void executeWithImpactHandler(Transaction transaction, RecordModificationImpactHandler handler)
			throws RecordServicesException;

	<T extends Supplier<Record>> void update(Stream<T> stream, Consumer<T> action)
			throws RecordServicesException;

	<T extends Supplier<Record>> void update(Stream<T> stream, Consumer<T> action, RecordUpdateOptions options)
			throws RecordServicesException;

	Record toRecord(RecordDTO recordDTO, boolean fullyLoaded);

	Record toRecord(MetadataSchema schema, RecordDTO recordDTO, boolean fullyLoaded);

	Record toRecord(MetadataSchemaType schema, RecordDTO recordDTO, boolean fullyLoaded);

	List<Record> toRecords(List<RecordDTO> recordDTOs, boolean fullyLoaded);

	long documentsCount();

	void update(Supplier<Record> record)
			throws RecordServicesException;

	void update(Supplier<Record> record, RecordUpdateOptions options)
			throws RecordServicesException;

	void update(Supplier<Record> record, User user)
			throws RecordServicesException;

	void update(Supplier<Record> record, RecordUpdateOptions options, User user)
			throws RecordServicesException;

	<T extends Supplier<Record>> void update(List<T> records, User user)
			throws RecordServicesException;

	<T extends Supplier<Record>> void update(List<T> records, RecordUpdateOptions options, User user)
			throws RecordServicesException;

	Record getRecordByMetadata(Metadata metadata, String value);

	Record getRecordSummaryByMetadata(Metadata metadata, String value);

	default Record getRecordSummaryById(String collection, String id) {
		return getRecordSummaryById(collection, id, true);
	}

	Record getRecordSummaryById(String collection, String id, boolean callExtensions);

	default Record realtimeGetById(MetadataSchemaType schemaType, String id, Long version, boolean callExtensions) {
		return realtimeGetById(schemaType.getDataStore(), id, version, callExtensions);
	}

	Record realtimeGetById(String dataStore, String id, Long version, boolean callExtensions);

	Record realtimeGetRecordSummaryById(String id, boolean callExtensions);

	List<Record> realtimeGetRecordById(List<String> ids, boolean callExtensions);

	default Record realtimeGetRecordById(String id, boolean callExtensions) {
		return realtimeGetRecordById(id, null, callExtensions);
	}

	default Record realtimeGetRecordById(String id, Long version, boolean callExtensions) {
		return realtimeGetById(DataStore.RECORDS, id, version, callExtensions);
	}

	default Record getDocumentById(String id, boolean callExtensions) {
		return getById(DataStore.RECORDS, id, callExtensions);
	}

	Record getDocumentById(String id, User user, boolean callExtensions);

	default Record getById(MetadataSchemaType schemaType, String id, boolean callExtensions) {
		return getById(schemaType.getDataStore(), id, callExtensions);
	}

	Record getById(String dataStore, String id, boolean callExtensions);

	List<Record> getRecordsById(String collection, List<String> ids, boolean callExtensions);

	// --

	default Record realtimeGetRecordSummaryById(String id) {
		return realtimeGetRecordSummaryById(id, true);
	}

	default Record realtimeGetRecordById(String id) {
		return realtimeGetRecordById(id, true);
	}

	default Record realtimeGetRecordById(RecordId id) {
		return realtimeGetRecordById(id.stringValue(), true);
	}


	default Record realtimeGetRecordById(String id, Long version) {
		return realtimeGetRecordById(id, version, true);
	}

	default Record getDocumentById(String id) {
		return getDocumentById(id, true);
	}

	default Record getDocumentById(String id, User user) {
		return getDocumentById(id, user, true);
	}

	default Record getById(MetadataSchemaType schemaType, String id) {
		return getById(schemaType, id, true);
	}

	default Record getById(String dataStore, String id) {
		return getById(dataStore, id, true);
	}

	default List<Record> getRecordsById(String collection, List<String> ids) {
		return getRecordsById(collection, ids, true);
	}

	default List<Record> realtimeGetRecordById(List<String> ids) {
		return realtimeGetRecordById(ids, true);
	}

	void prepareRecords(Transaction transaction)
			throws RecordServicesException.ValidationException;

	void validateTransaction(Transaction transaction)
			throws ValidationException;

	void validateRecordInTransaction(Supplier<Record> record, Transaction transaction)
			throws ValidationException;

	void validateRecord(Supplier<Record> record)
			throws RecordServicesException.ValidationException;

	Record newRecordWithSchema(MetadataSchema schema, String id, boolean withDefaultValues);

	Record newRecordWithSchema(MetadataSchema schema, String id);

	Record newRecordWithSchema(MetadataSchema schema, boolean isWithDefaultValues);

	Record newRecordWithSchema(MetadataSchema schema);

	void refresh(Supplier<Record>... records);

	<T extends Supplier<Record>> void refresh(List<T> records);

	void refreshUsingCache(Supplier<Record>... records);

	<T extends Supplier<Record>> void refreshUsingCache(List<T> records);

	List<BatchProcess> updateAsync(Supplier<Record> record)
			throws RecordServicesException;

	List<BatchProcess> updateAsync(Supplier<Record> record, RecordUpdateOptions options)
			throws RecordServicesException;

	ModificationImpactCalculatorResponse calculateImpactOfModification(Transaction transaction,
																	   TaxonomiesManager taxonomiesManager,
																	   SearchServices searchServices,
																	   MetadataSchemaTypes metadataSchemaTypes,
																	   boolean executedAfterTransaction);

	RecordsCaches getRecordsCaches();

	boolean isRestorable(Supplier<Record> record, User user);

	void restore(Supplier<Record> record, User user);

	ValidationErrors validatePhysicallyDeletable(Supplier<Record> record, User user);

	void physicallyDelete(Supplier<Record> record, User user);

	void physicallyDelete(Supplier<Record> record, User user, RecordPhysicalDeleteOptions options);

	void physicallyDeleteNoMatterTheStatus(Supplier<Record> record, User user, RecordPhysicalDeleteOptions options);

	ValidationErrors validateLogicallyDeletable(Supplier<Record> record, User user);

	boolean isLogicallyDeletableAndIsSkipValidation(Supplier<Record> record, User user);

	ValidationErrors validateLogicallyThenPhysicallyDeletable(Supplier<Record> record, User user);

	void logicallyDelete(Supplier<Record> record, User user);

	void logicallyDelete(Supplier<Record> record, User user, RecordLogicalDeleteOptions options);

	List<Record> getVisibleRecordsWithReferenceTo(Supplier<Record> record, User user);

	boolean isReferencedByOtherRecords(Supplier<Record> record);

	void flush();

	void flushRecords();

	void removeOldLocks();

	void recalculate(Supplier<Record> record);

	void loadLazyTransientMetadatas(Supplier<Record> record);

	void reloadEagerTransientMetadatas(Supplier<Record> record);

	SecurityModel getSecurityModel(String collection);

	boolean isValueAutomaticallyFilled(Metadata metadata, Supplier<Record> record);

}
