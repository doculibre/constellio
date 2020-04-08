package com.constellio.model.entities.records;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.model.entities.records.TransactionRuntimeException.RecordIdCollision;
import com.constellio.model.entities.records.TransactionRuntimeException.RecordsWithoutIds;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.preparation.AggregatedMetadataIncrementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Transaction {

	private String id;

	Map<String, Record> updatedRecordsMap = new HashMap<>();
	List<Record> records = new ArrayList<>();
	RecordUpdateOptions recordUpdateOptions = new RecordUpdateOptions();

	Map<String, Record> referencedRecords = new HashMap<>();

	Set<String> idsToReindex = new HashSet<>();

	private List<AggregatedMetadataIncrementation> aggregatedMetadataIncrementations = new ArrayList<>();

	String title;

	private User user;
	private String collection;
	private Map<String, ParsedContent> parsedContentCache = new HashMap<>();

	boolean isOnlyBeingPrepared = true;

	public Transaction() {
		this.id = UUIDV1Generator.newRandomId();
	}

	public Transaction(RecordUpdateOptions options) {
		this.id = UUIDV1Generator.newRandomId();
		this.recordUpdateOptions = new RecordUpdateOptions(options);
	}

	public Transaction(String id) {
		this.id = id;
	}

	public Transaction(RecordWrapper... records) {
		this();
		addAll(records);
	}

	public Transaction(Record... records) {
		this(Arrays.asList(records));
	}

	public Transaction(List<Record> records) {
		this.id = UUIDV1Generator.newRandomId();
		this.records = records;

		if (!records.isEmpty()) {
			collection = records.get(0).getCollection();
			validateCollections();
		}
	}

	public Transaction(Transaction transaction) {
		this.id = UUIDV1Generator.newRandomId();
		this.records = new ArrayList<>();
		for (Record record : transaction.getRecords()) {
			addUpdate(record);
		}
		this.user = transaction.user;
		this.title = transaction.title;
		this.recordUpdateOptions = transaction.recordUpdateOptions;
		this.idsToReindex.addAll(transaction.getIdsToReindex());
		this.aggregatedMetadataIncrementations.addAll(transaction.aggregatedMetadataIncrementations);
		this.recordUpdateOptions = new RecordUpdateOptions(transaction.recordUpdateOptions);
	}

	public boolean isContainingUpdatedRecord(Record record) {
		return updatedRecordsMap.containsKey(record.getId());
	}

	public boolean isOnlyBeingPrepared() {
		return isOnlyBeingPrepared;
	}

	public void setOnlyBeingPrepared(boolean onlyBeingPrepared) {
		isOnlyBeingPrepared = onlyBeingPrepared;
	}

	public Transaction addRecordToReindex(String id) {
		idsToReindex.add(id);
		return this;
	}

	public Transaction addAllRecordsToReindex(Collection<String> ids) {
		idsToReindex.addAll(ids);
		return this;
	}

	public Transaction addRecordToReindex(Record record) {
		idsToReindex.add(record.getId());
		return this;
	}

	public Transaction addRecordToReindex(RecordWrapper record) {
		idsToReindex.add(record.getId());
		return this;
	}

	public Transaction addAggregatedMetadataIncrementations(
			List<AggregatedMetadataIncrementation> aggregatedMetadataIncrementations) {
		this.aggregatedMetadataIncrementations.addAll(aggregatedMetadataIncrementations);
		return this;
	}

	public Transaction addUpdate(Record addUpdateRecord) {
		if (!addUpdateRecord.isSaved()) {
			add(addUpdateRecord);
		} else {
			update(addUpdateRecord);
		}
		return this;
	}

	public Transaction addUpdate(Record... addUpdateRecords) {
		for (Record addUpdateRecord : addUpdateRecords) {
			addUpdate(addUpdateRecord);
		}
		return this;
	}

	public Transaction addUpdate(List<Record> addUpdateRecords) {
		for (Record addUpdateRecord : addUpdateRecords) {
			addUpdate(addUpdateRecord);
		}
		return this;
	}

	public <T extends RecordWrapper> T add(T recordWrapper) {
		this.add(recordWrapper.getWrappedRecord());
		return recordWrapper;
	}

	public Transaction addAll(RecordWrapper... recordWrappers) {
		for (RecordWrapper recordWrapper : recordWrappers) {
			this.add(recordWrapper.getWrappedRecord());
		}
		return this;
	}

	public Transaction addAll(List<?> records) {
		for (Object record : records) {
			if (record != null) {
				if (record instanceof RecordWrapper) {
					this.add(((RecordWrapper) record).getWrappedRecord());

				} else if (record instanceof Record) {
					this.add((Record) record);

				} else {
					throw new IllegalArgumentException("Unsupported object of type " + record.getClass().getName());
				}
			}
		}
		return this;
	}

	public Transaction addAll(Record... records) {
		for (Record record : records) {
			this.add(record);
		}
		return this;
	}

	public Record add(Record addUpdateRecord) {

		records.add(addUpdateRecord);
		validateCollection(addUpdateRecord.getCollection());
		collection = addUpdateRecord.getCollection();
		return addUpdateRecord;
	}

	public Transaction update(Record addUpdateRecord) {

		if (updatedRecordsMap.containsKey(addUpdateRecord.getId())) {
			if (updatedRecordsMap.get(addUpdateRecord.getId()) != addUpdateRecord) {
				throw new RecordIdCollision();
			}
		} else {
			updatedRecordsMap.put(addUpdateRecord.getId(), addUpdateRecord);
			records.add(addUpdateRecord);
		}
		validateCollection(addUpdateRecord.getCollection());
		collection = addUpdateRecord.getCollection();
		return this;
	}

	public Transaction update(Record... addUpdateRecords) {
		for (Record addUpdateRecord : addUpdateRecords) {
			update(addUpdateRecord);
		}
		return this;
	}

	public Transaction update(List<Record> addUpdateRecords) {
		for (Record addUpdateRecord : addUpdateRecords) {
			update(addUpdateRecord);
		}
		return this;
	}

	public Transaction setToReindexAll() {
		recordUpdateOptions.setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		return this;
	}

	public RecordUpdateOptions onOptimisticLocking(OptimisticLockingResolution resolution) {
		return recordUpdateOptions.onOptimisticLocking(resolution);
	}

	public Transaction setRecordFlushing(RecordsFlushing recordsFlushing) {
		recordUpdateOptions.setRecordsFlushing(recordsFlushing);
		return this;
	}

	public void setOptions(RecordUpdateOptions recordUpdateOptions) {
		this.recordUpdateOptions = recordUpdateOptions;
	}

	public List<Record> getSavedRecordWithModification() {
		List<Record> recordsWithModification = new ArrayList<>();
		for (Record record : records) {
			if (record.isSaved() && record.isDirty()) {
				recordsWithModification.add(record);
			}
		}
		return recordsWithModification;
	}

	public List<Record> getRecords() {
		return Collections.unmodifiableList(records);
	}

	public RecordUpdateOptions getRecordUpdateOptions() {
		return recordUpdateOptions;
	}

	public void sortRecords(MetadataSchemaTypes schemaTypes) {
		records = new RecordUtils().sortRecordsOnDependencies(records, schemaTypes);

	}

	public Transaction setOptimisticLockingResolution(OptimisticLockingResolution resolution) {
		this.recordUpdateOptions.setOptimisticLockingResolution(resolution);
		return this;
	}

	public List<String> getRecordIds() {

		List<String> recordIds = new ArrayList<>();

		for (Record record : records) {
			if (record.getId() == null) {
				throw new RecordsWithoutIds();
			}
			recordIds.add(record.getId());
		}

		return Collections.unmodifiableList(recordIds);
	}

	public List<Record> getModifiedRecords() {

		if (recordUpdateOptions.isFullRewrite()) {
			return Collections.unmodifiableList(records);
		} else {

			List<Record> modifiedRecords = new ArrayList<>();

			for (Record record : records) {
				if (!record.isSaved() || record.isDirty()) {
					modifiedRecords.add(record);
				}
			}

			return Collections.unmodifiableList(modifiedRecords);
		}
	}

	public String getCollection() {
		return collection;
	}

	public User getUser() {
		return user;
	}

	public Transaction setUser(User user) {
		this.user = user;
		return this;
	}

	public boolean isSkippingRequiredValuesValidation() {
		return recordUpdateOptions.isSkippingRequiredValuesValidation();
	}

	public boolean isSkippingReferenceToLogicallyDeletedValidation() {
		return recordUpdateOptions.isSkippingReferenceToLogicallyDeletedValidation();
	}

	public boolean isSkippingUserAccessValidation() {
		return recordUpdateOptions.isSkipUserAccessValidation();
	}

	public Transaction setSkippingRequiredValuesValidation(boolean skippingRequiredValuesValidation) {
		recordUpdateOptions.setSkippingRequiredValuesValidation(skippingRequiredValuesValidation);
		return this;
	}

	public Transaction setSkippingReferenceToLogicallyDeletedValidation(
			boolean skippingReferenceToLogicallyDeletedValidation) {
		recordUpdateOptions.setSkippingReferenceToLogicallyDeletedValidation(skippingReferenceToLogicallyDeletedValidation);
		return this;
	}

	public String getId() {
		return id;
	}

	void validateCollection(String collection) {
		if (this.collection != null && !this.collection.equals(collection)) {
			throw new TransactionRuntimeException.DifferentCollectionsInRecords(this.collection, collection);
		}
	}

	void validateCollections() {
		for (Record record : records) {
			validateCollection(record.getCollection());
		}
	}

	public void addReferencedRecord(Record record) {
		referencedRecords.put(record.getId(), record);
	}

	public Map<String, Record> getReferencedRecords() {
		return referencedRecords;
	}

	public Record getReferencedRecord(String id) {
		return referencedRecords.get(id);
	}

	public static Transaction wrappers(List<? extends RecordWrapper> recordWrappers) {
		List<Record> records = new ArrayList<>();
		for (RecordWrapper recordWrapper : recordWrappers) {
			records.add(recordWrapper.getWrappedRecord());
		}

		return new Transaction(records);
	}

	public boolean isSkipReferenceValidation() {
		return getRecordUpdateOptions().isSkipReferenceValidation();
	}

	public void remove(Record record) {
		for (Iterator<Record> iterator = records.iterator(); iterator.hasNext(); ) {
			Record aRecord = iterator.next();

			if (aRecord.getId().equals(record.getId())) {
				iterator.remove();
			}
		}
	}

	public void remove(RecordWrapper recordWrapper) {
		for (Iterator<Record> iterator = records.iterator(); iterator.hasNext(); ) {
			Record record = iterator.next();

			if (record.getId().equals(recordWrapper.getId())) {
				iterator.remove();
			}
		}
	}

	public Record getRecord(String id) {
		for (Record record : records) {
			if (id.equals(record.getId())) {
				return record;
			}
		}
		return null;
	}

	public int getRecordCount() {
		return records.size();
	}

	public String getTitle() {
		return title;
	}

	public Transaction setTitle(String title) {
		this.title = title;
		return this;
	}

	public Set<String> getIdsToReindex() {
		return Collections.unmodifiableSet(idsToReindex);
	}

	public List<AggregatedMetadataIncrementation> getAggregatedMetadataIncrementations() {
		return Collections.unmodifiableList(aggregatedMetadataIncrementations);
	}

	public Map<String, ParsedContent> getParsedContentCache() {
		return parsedContentCache;
	}

	public void removeUnchangedRecords() {

		Iterator<Record> recordIterator = records.iterator();
		while (recordIterator.hasNext()) {
			Record record = recordIterator.next();
			if (!record.isDirty() && record.isSaved()) {
				recordIterator.remove();
				updatedRecordsMap.remove(record.getId());
			}
		}

	}
}
