package com.constellio.model.services.records;

import com.constellio.model.entities.schemas.Metadata;

import java.util.HashSet;
import java.util.Set;

public class TransactionExecutionRecordContext {

	TransactionExecutionContext transactionExecutionContext;

	RecordImpl record;

	Set<String> calculatedMetadatas = new HashSet<>();

	public TransactionExecutionRecordContext(RecordImpl record,
											 TransactionExecutionContext transactionExecutionContext) {
		this.record = record;
		this.transactionExecutionContext = transactionExecutionContext;
	}

	public TransactionExecutionContext getTransactionExecutionContext() {
		return transactionExecutionContext;
	}

	public void markAsCalculated(Metadata metadata) {
		calculatedMetadatas.add(metadata.getLocalCode());
	}

	public RecordImpl getRecord() {
		return record;
	}

	public Set<String> getCalculatedMetadatas() {
		return calculatedMetadatas;
	}
}
