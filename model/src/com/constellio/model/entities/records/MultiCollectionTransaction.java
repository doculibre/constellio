package com.constellio.model.entities.records;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class MultiCollectionTransaction {

	@Getter
	private RecordUpdateOptions options = new RecordUpdateOptions();

	@Getter
	private Map<String, Transaction> transactionMap = new HashMap<>();

	public MultiCollectionTransaction() {
	}

	public MultiCollectionTransaction(RecordUpdateOptions options) {
		this.options = options;
	}

	public void add(Record record) {
		Transaction transaction = transactionMap.get(record.getCollection());
		if (transaction == null) {
			transaction = new Transaction(options);
		}
		transaction.add(record);
	}


}
