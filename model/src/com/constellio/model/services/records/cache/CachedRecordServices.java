package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.RecordCacheType;
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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.constellio.data.dao.services.records.DataStore.RECORDS;
import static org.apache.calcite.sql.advise.SqlAdvisor.LOGGER;

public class CachedRecordServices extends BaseRecordServices implements RecordServices {

	RecordsCaches recordsCaches;

	RecordServices recordServices;

	public CachedRecordServices(ModelLayerFactory modelLayerFactory, RecordServices recordServices,
								RecordsCaches recordsCaches) {
		super(modelLayerFactory);
		this.recordServices = recordServices;
		this.recordsCaches = recordsCaches;
	}

	public RecordsCaches getRecordsCache() {
		return recordsCaches;
	}

	@Override
	public Record getDocumentById(String id) {
		if (id == null) {
			return null;
		}
		return getById(RECORDS, id);
	}

	@Override
	public Record getById(String dataStore, String id, boolean callExtensions) {
		Record record = RECORDS.equals(dataStore) ? getRecordsCache().getRecord(id) : null;
		if (record == null) {
			record = recordServices.getById(dataStore, id, callExtensions);
		}
		return record;
	}

	public Record getById(MetadataSchemaType schemaType, String id, boolean callExtensions) {
		RecordsCache cache = getRecordsCache().getCache(schemaType.getCollection());
		Record record = cache.get(id);
		if (record == null) {
			record = recordServices.getById(schemaType, id, callExtensions);
		}
		return record;
	}

	@Override
	public Record realtimeGetRecordById(String id, Long version, boolean callExtensions) {
		return realtimeGetById(RECORDS, id, version, callExtensions);
	}

	@Override
	public Record realtimeGetById(MetadataSchemaType schemaType, String id, Long version, boolean callExtensions) {
		Record record = getRecordsCache().getRecord(id);
		if (record == null || (version != null && record.getVersion() < version)) {
			record = recordServices.realtimeGetById(schemaType, id, version, callExtensions);
		}
		return record;
	}

	@Override
	public Record realtimeGetById(String dataStore, String id, Long version, boolean callExtensions) {
		Record record = getRecordsCache().getRecord(id);
		if (record == null || (version != null && record.getVersion() < version)) {
			record = recordServices.realtimeGetById(dataStore, id, version, callExtensions);
		}
		return record;
	}

	@Override
	public Record realtimeGetRecordSummaryById(String id, boolean callExtensions) {
		Record record = getRecordsCache().getRecordSummary(id);
		if (record == null) {
			record = recordServices.realtimeGetRecordSummaryById(id, callExtensions);
		}
		return record;
	}

	@Override
	public List<Record> realtimeGetRecordById(List<String> ids, boolean callExtensions) {
		return null;
	}

	@Override
	public Record getRecordByMetadata(Metadata metadata, String value) {
		if (!metadata.isUniqueValue()) {
			throw new IllegalArgumentException("Metadata '" + metadata + "' is not unique");
		}
		if (metadata.getCode().startsWith("global_")) {
			throw new IllegalArgumentException("Metadata '" + metadata + "' is global, which has no specific schema type.");
		}

		if (StringUtils.isBlank(value)) {
			return null;
		}

		MetadataSchemaType schemaType = modelLayerFactory.getMetadataSchemasManager()
				.getSchemaTypes(metadata.getCollection()).getSchemaType(metadata.getSchemaTypeCode());

		if (schemaType.getCacheType() == RecordCacheType.FULLY_CACHED) {
			return getRecordsCache().getCache(metadata.getCollection()).getByMetadata(metadata, value);

		} else if (schemaType.getCacheType().hasPermanentCache()) {
			Record foundRecordSummary = getRecordsCache().getCache(metadata.getCollection()).getSummaryByMetadata(metadata, value);
			if (foundRecordSummary != null) {
				return getDocumentById(foundRecordSummary.getId());
			} else {
				return null;
			}

		} else {
			return recordServices.getRecordByMetadata(metadata, value);
		}


	}

	@Override
	public Record getRecordSummaryByMetadata(Metadata metadata, String value) {
		if (!metadata.isUniqueValue()) {
			throw new IllegalArgumentException("Metadata '" + metadata + "' is not unique");
		}
		if (metadata.getCode().startsWith("global_")) {
			throw new IllegalArgumentException("Metadata '" + metadata + "' is global, which has no specific schema type.");
		}

		Record foundRecord = getRecordsCache().getCache(metadata.getCollection()).getSummaryByMetadata(metadata, value);

		if (foundRecord == null) {
			foundRecord = recordServices.getRecordSummaryByMetadata(metadata, value);
		}

		return foundRecord;
	}

	@Override
	public List<Record> getRecordsById(String collection,
									   List<String> ids, boolean callExtensions) {

		List<Record> records = new ArrayList<>();

		ids.forEach(id -> {
			if (id != null) {
				try {
					records.add(getDocumentById(id, callExtensions));
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					LOGGER.warn("Record with id '" + id + "' does not exist");
				}
			}
		});

		return records;
	}

	@Override
	public void prepareRecords(Transaction transaction) throws ValidationException {
		recordServices.prepareRecords(transaction);
	}

	@Override
	public <T extends Supplier<Record>> void refresh(List<T> records) {
		recordServices.refresh(records);
	}

	@Override
	public <T extends Supplier<Record>> void refreshUsingCache(List<T> records) {
		for (Supplier<Record> item : records) {
			Record record = item.get();
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
	public <T extends Supplier<Record>> void update(Stream<T> stream, Consumer<T> action)
			throws RecordServicesException {
		recordServices.update(stream, action);
	}

	@Override
	public <T extends Supplier<Record>> void update(Stream<T> stream, Consumer<T> action, RecordUpdateOptions options)
			throws RecordServicesException {
		recordServices.update(stream, action);
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
	public Record toRecord(MetadataSchemaType schemaType, RecordDTO recordDTO, boolean fullyLoaded) {
		return recordServices.toRecord(schemaType, recordDTO, fullyLoaded);
	}

	@Override
	public Record toRecord(MetadataSchema schema, RecordDTO recordDTO, boolean fullyLoaded) {
		return recordServices.toRecord(schema, recordDTO, fullyLoaded);
	}

	@Override
	public long documentsCount() {
		return recordServices.documentsCount();
	}

	@Override
	public void validateRecordInTransaction(Supplier<Record> record,
											Transaction transaction)
			throws ValidationException {
		recordServices.validateRecordInTransaction(record, transaction);
	}

	@Override
	public void validateRecord(Supplier<Record> record)
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
		return getRecordsCache();
	}

	@Override
	public boolean isRestorable(Supplier<Record> record,
								User user) {
		return recordServices.isRestorable(record, user);
	}

	@Override
	public void restore(Supplier<Record> record,
						User user) {
		recordServices.restore(record, user);
	}

	@Override
	public ValidationErrors validatePhysicallyDeletable(Supplier<Record> record, User user) {
		return recordServices.validatePhysicallyDeletable(record, user);
	}

	@Override
	public void physicallyDelete(Supplier<Record> record,
								 User user) {
		recordServices.physicallyDelete(record, user);
	}

	@Override
	public void physicallyDelete(Supplier<Record> record,
								 User user, RecordPhysicalDeleteOptions options) {
		recordServices.physicallyDelete(record, user, options);
	}

	@Override
	public void physicallyDeleteNoMatterTheStatus(Supplier<Record> record, User user,
												  RecordPhysicalDeleteOptions options) {
		recordServices.physicallyDeleteNoMatterTheStatus(record, user, options);
	}

	@Override
	public ValidationErrors validateLogicallyDeletable(Supplier<Record> record,
													   User user) {
		return recordServices.validateLogicallyDeletable(record, user);
	}

	@Override
	public boolean isLogicallyDeletableAndIsSkipValidation(Supplier<Record> record, User user) {
		return recordServices.isLogicallyDeletableAndIsSkipValidation(record, user);
	}

	@Override
	public ValidationErrors validateLogicallyThenPhysicallyDeletable(Supplier<Record> record, User user) {
		return recordServices.validateLogicallyThenPhysicallyDeletable(record, user);
	}

	@Override
	public void logicallyDelete(Supplier<Record> record,
								User user) {
		recordServices.logicallyDelete(record, user);
	}

	@Override
	public void logicallyDelete(Supplier<Record> record,
								User user, RecordLogicalDeleteOptions options) {
		recordServices.logicallyDelete(record, user, options);
	}

	@Override
	public List<Record> getVisibleRecordsWithReferenceTo(
			Supplier<Record> record, User user) {
		return recordServices.getVisibleRecordsWithReferenceTo(record, user);
	}

	@Override
	public boolean isReferencedByOtherRecords(Supplier<Record> record) {
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
	public void recalculate(Supplier<Record> record) {
		recordServices.recalculate(record);
	}

	@Override
	public void loadLazyTransientMetadatas(Supplier<Record> record) {
		recordServices.loadLazyTransientMetadatas(record);
	}

	@Override
	public void reloadEagerTransientMetadatas(Supplier<Record> record) {
		recordServices.reloadEagerTransientMetadatas(record);
	}

	@Override
	public SecurityModel getSecurityModel(String collection) {
		return recordServices.getSecurityModel(collection);
	}

	@Override
	public boolean isValueAutomaticallyFilled(Metadata metadata, Supplier<Record> record) {
		return recordServices.isValueAutomaticallyFilled(metadata, record);
	}
}
