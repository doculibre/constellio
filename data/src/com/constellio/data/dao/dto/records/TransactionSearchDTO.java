package com.constellio.data.dao.dto.records;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TransactionSearchDTO {

	private String transactionId;
	public List<RecordDTO> newRecords;
	public List<RecordDeltaDTO> modifiedRecords;
	private List<RecordDTO> deletedRecords;
	private Map<String, RecordDTO> newRecordsById;
	private RecordsFlushing recordsFlushing;

	public TransactionSearchDTO(String transactionId, RecordsFlushing recordsFlushing, List<RecordDTO> newRecords,
								List<RecordDeltaDTO> modifiedRecords) {
		this(transactionId, recordsFlushing, newRecords, modifiedRecords, Collections.<RecordDTO>emptyList()
		);
	}

	public TransactionSearchDTO(String transactionId,
								RecordsFlushing recordsFlushing,
								List<RecordDTO> newRecords,
								List<RecordDeltaDTO> modifiedRecords,
								List<RecordDTO> deletedRecords
	) {
		this.transactionId = transactionId;
		this.newRecords = newRecords;
		this.modifiedRecords = modifiedRecords;
		this.deletedRecords = deletedRecords;
		this.recordsFlushing = recordsFlushing;
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

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public List<RecordDTO> getNewRecords() {
		return newRecords;
	}

	public void setNewRecords(List<RecordDTO> newRecords) {
		this.newRecords = newRecords;
	}

	public List<RecordDeltaDTO> getModifiedRecords() {
		return modifiedRecords;
	}

	public void setModifiedRecords(List<RecordDeltaDTO> modifiedRecords) {
		this.modifiedRecords = modifiedRecords;
	}

	public List<RecordDTO> getDeletedRecords() {
		return deletedRecords;
	}

	public void setDeletedRecords(List<RecordDTO> deletedRecords) {
		this.deletedRecords = deletedRecords;
	}

	public Map<String, RecordDTO> getNewRecordsById() {
		return newRecordsById;
	}

	public void setNewRecordsById(Map<String, RecordDTO> newRecordsById) {
		this.newRecordsById = newRecordsById;
	}

	public RecordsFlushing getRecordsFlushing() {
		return recordsFlushing;
	}

	public void setRecordsFlushing(RecordsFlushing recordsFlushing) {
		this.recordsFlushing = recordsFlushing;
	}

	public TransactionSearchDTO withNewRecords(List<RecordDTO> records) {
		List<RecordDTO> newRecords = new ArrayList<>();
		newRecords.addAll(this.newRecords);
		newRecords.addAll(records);

		return new TransactionSearchDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords);
	}

	public TransactionSearchDTO withModifiedRecords(List<RecordDeltaDTO> records) {
		List<RecordDeltaDTO> modifiedRecords = new ArrayList<>();
		modifiedRecords.addAll(this.modifiedRecords);
		modifiedRecords.addAll(records);

		return new TransactionSearchDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords);

	}

	public TransactionSearchDTO withDeletedRecords(List<RecordDTO> records) {
		List<RecordDTO> deletedRecords = new ArrayList<>();
		deletedRecords.addAll(this.deletedRecords);
		deletedRecords.addAll(records);

		return new TransactionSearchDTO(transactionId, recordsFlushing, newRecords, modifiedRecords, deletedRecords);
	}

}
