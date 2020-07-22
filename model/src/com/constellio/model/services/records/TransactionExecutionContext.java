package com.constellio.model.services.records;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.security.TransactionSecurityModel;

import java.util.HashMap;
import java.util.Map;

public class TransactionExecutionContext {

	Map<String, TransactionExecutionRecordContext> recordContextMap = new HashMap<>();

	Map<String, KeyListMap<String, Record>> metadatasInvertedAggregatedValuesMap = new HashMap<>();

	Transaction transaction;

	TransactionSecurityModel transactionSecurityModel;

	public TransactionExecutionContext(Transaction transaction) {
		this.transaction = transaction;
	}


	public Transaction getTransaction() {
		return transaction;
	}

	public TransactionSecurityModel getTransactionSecurityModel() {
		return transactionSecurityModel;
	}

	public TransactionExecutionContext setTransactionSecurityModel(
			TransactionSecurityModel transactionSecurityModel) {
		this.transactionSecurityModel = transactionSecurityModel;
		return this;
	}

	TransactionExecutionRecordContext contextForRecord(Record record) {
		TransactionExecutionRecordContext recordContext = recordContextMap.get(record.getId());
		if (recordContext == null) {
			recordContext = new TransactionExecutionRecordContext((RecordImpl) record, this);
			recordContextMap.put(record.getId(), recordContext);
		}
		return recordContext;
	}
}
