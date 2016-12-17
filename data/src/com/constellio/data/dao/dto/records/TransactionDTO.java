package com.constellio.data.dao.dto.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.solr.common.params.SolrParams;

public class TransactionDTO {
	private String transactionId;
	public List<RecordDTO> newRecords;
	public List<RecordDeltaDTO> modifiedRecords;
	private List<RecordDTO> deletedRecords;
	private List<SolrParams> deletedByQueries;
	private Map<String, RecordDTO> newRecordsById;
	private RecordsFlushing recordsFlushing;
	private Set<String> markedForReindexing;
	private boolean skippingReferenceToLogicallyDeletedValidation;
	private boolean fullRewrite = false;

	public TransactionDTO(RecordsFlushing recordsFlushing) {
		this(UUID.randomUUID().toString(), recordsFlushing, Collections.<RecordDTO>emptyList(),
				Collections.<RecordDeltaDTO>emptyList());
	}

	public TransactionDTO(String transactionId, RecordsFlushing recordsFlushing, List<RecordDTO> newRecords,
			List<RecordDeltaDTO> modifiedRecords) {
		this(transactionId, recordsFlushing, newRecords, modifiedRecords, Collections.<RecordDTO>emptyList(),
				Collections.<SolrParams>emptyList());
	}

	public TransactionDTO(String transactionId, RecordsFlushing recordsFlushing, List<RecordDTO> newRecords,
			List<RecordDeltaDTO> modifiedRecords, List<RecordDTO> deletedRecords, List<SolrParams> deletedByQueries) {
		this(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, deletedByQueries,
				Collections.<String>emptySet(), false, false);
	}

	public TransactionDTO(String transactionId, RecordsFlushing recordsFlushing, List<RecordDTO> newRecords,
			List<RecordDeltaDTO> modifiedRecords, List<RecordDTO> deletedRecords, List<SolrParams> deletedByQueries,
			Set<String> markedForReindexing, boolean skippingReferenceToLogicallyDeletedValidation, boolean fullRewrite) {
		this.transactionId = transactionId;
		this.recordsFlushing = recordsFlushing;
		this.newRecords = Collections.unmodifiableList(newRecords);
		this.modifiedRecords = Collections.unmodifiableList(modifiedRecords);
		this.deletedRecords = Collections.unmodifiableList(deletedRecords);
		this.deletedByQueries = Collections.unmodifiableList(deletedByQueries);
		this.skippingReferenceToLogicallyDeletedValidation = skippingReferenceToLogicallyDeletedValidation;
		this.fullRewrite = fullRewrite;
		this.markedForReindexing = markedForReindexing;
		newRecordsById = buildNewRecordsByIdMap();
	}

	public List<RecordDTO> getNewRecords() {
		return newRecords;
	}

	public List<RecordDeltaDTO> getModifiedRecords() {
		return modifiedRecords;

	}

	public List<RecordDTO> getDeletedRecords() {
		return deletedRecords;
	}

	public List<SolrParams> getDeletedByQueries() {
		return deletedByQueries;
	}

	public boolean isFullRewrite() {
		return fullRewrite;
	}

	public boolean isSkippingReferenceToLogicallyDeletedValidation() {
		return skippingReferenceToLogicallyDeletedValidation;
	}

	public TransactionDTO withNewRecords(List<RecordDTO> records) {
		List<RecordDTO> newRecords = new ArrayList<>();
		newRecords.addAll(this.newRecords);
		newRecords.addAll(records);

		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, deletedByQueries,
				markedForReindexing, skippingReferenceToLogicallyDeletedValidation, fullRewrite);
	}

	public TransactionDTO withModifiedRecords(List<RecordDeltaDTO> records) {
		List<RecordDeltaDTO> modifiedRecords = new ArrayList<>();
		modifiedRecords.addAll(this.modifiedRecords);
		modifiedRecords.addAll(records);

		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, deletedByQueries,
				markedForReindexing, skippingReferenceToLogicallyDeletedValidation, fullRewrite);
	}

	public TransactionDTO withDeletedRecords(List<RecordDTO> records) {
		List<RecordDTO> deletedRecords = new ArrayList<>();
		deletedRecords.addAll(this.deletedRecords);
		deletedRecords.addAll(records);

		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, deletedByQueries,
				markedForReindexing, skippingReferenceToLogicallyDeletedValidation, fullRewrite);
	}

	public TransactionDTO withDeletedByQueries(SolrParams... deletedByQueries) {
		return withDeletedByQueries(Arrays.asList(deletedByQueries));
	}

	public TransactionDTO withDeletedByQueries(List<SolrParams> deletedByQueries) {
		List<SolrParams> queries = new ArrayList<>();
		queries.addAll(this.deletedByQueries);
		queries.addAll(deletedByQueries);

		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, queries,
				markedForReindexing, skippingReferenceToLogicallyDeletedValidation, fullRewrite);
	}

	public TransactionDTO withFullRewrite(boolean fullRewrite) {
		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, deletedByQueries,
				markedForReindexing, skippingReferenceToLogicallyDeletedValidation, fullRewrite);
	}

	public TransactionDTO withSkippingReferenceToLogicallyDeletedValidation(boolean enabled) {
		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, deletedByQueries,
				markedForReindexing, enabled, fullRewrite);
	}

	public TransactionDTO withMarkedForReindexing(Set<String> markedForReindexing) {
		return new TransactionDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords, deletedByQueries,
				markedForReindexing, skippingReferenceToLogicallyDeletedValidation, fullRewrite);
	}

	public boolean hasRecord(String value) {
		return newRecordsById.containsKey(value);
	}

	public RecordsFlushing getRecordsFlushing() {
		return recordsFlushing;
	}

	public Set<String> getMarkedForReindexing() {
		return markedForReindexing;
	}

	private Map<String, RecordDTO> buildNewRecordsByIdMap() {

		Map<String, RecordDTO> allRecordsByIdMap = new HashMap<>();
		for (RecordDTO newRecord : newRecords) {
			allRecordsByIdMap.put(newRecord.getId(), newRecord);
		}

		return allRecordsByIdMap;
	}

	public String getTransactionId() {
		return transactionId;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "transactionId");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "transactionId");
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
