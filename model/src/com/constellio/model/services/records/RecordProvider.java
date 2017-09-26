package com.constellio.model.services.records;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;

public class RecordProvider {

	RecordServices recordServices;

	Map<String, Record> memoryList;

	Transaction transaction;

	public RecordProvider(RecordServices recordServices) {
		this(recordServices, null, Collections.<Record>emptyList(), null);
	}

	public RecordProvider(RecordServices recordServices, RecordProvider recordProvider, List<Record> records,
			Transaction transaction) {
		this.recordServices = recordServices;
		this.transaction = transaction;
		this.memoryList = new HashMap<>();

		if (transaction != null) {
			memoryList.putAll(transaction.getReferencedRecords());
		}

		if (recordProvider != null) {
			this.memoryList.putAll(recordProvider.memoryList);
		}
		if (records != null) {
			for (Record record : records) {
				this.memoryList.put(record.getId(), record);
			}
		}

	}

	public Record getRecord(String id) {
		Record record = memoryList.get(id);
		if (record == null && transaction != null) {
			record = transaction.getRecord(id);
		}
		if (record == null) {
			record = recordServices.realtimeGet(id);
			memoryList.put(id, record);
		}
		return record;
	}

	public boolean hasRecordInMemoryList(Object referenceValue) {
		if (referenceValue instanceof String) {
			return memoryList.containsKey((String) referenceValue);
		} else if (referenceValue instanceof List) {
			for (String referenceItem : (List<String>) referenceValue) {
				if (memoryList.containsKey(referenceItem)) {
					return true;
				}
			}
		}
		return false;
	}
}
