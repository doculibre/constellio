package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordProvider {

	RecordServices recordServices;

	Map<String, Record> memoryList;

	Map<String, Record> summaryMemoryList;

	RecordProvider nestedRecordProvider;

	Transaction transaction;

	public RecordProvider(RecordServices recordServices) {
		this(recordServices, null, Collections.<Record>emptyList(), null);
	}

	public RecordProvider(RecordServices recordServices, RecordProvider recordProvider, List<Record> records,
						  Transaction transaction) {
		this.recordServices = recordServices;
		this.transaction = transaction;
		this.memoryList = new HashMap<>();
		this.summaryMemoryList = new HashMap<>();
		this.nestedRecordProvider = recordProvider;

		if (transaction != null) {
			for (Record transactionRecord : transaction.getRecords()) {
				memoryList.put(transactionRecord.getId(), transactionRecord);
			}
			for (Map.Entry<String, Record> entry : transaction.getReferencedRecords().entrySet()) {
				if (entry.getValue().getRecordDTOMode() == RecordDTOMode.SUMMARY) {
					summaryMemoryList.put(entry.getKey(), entry.getValue());
				} else {
					memoryList.put(entry.getKey(), entry.getValue());
				}
			}

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
		if (record == null && nestedRecordProvider != null) {
			record = nestedRecordProvider.getRecord(id);
			if (record != null) {
				memoryList.put(id, record);
			}
		}
		if (record == null && recordServices != null) {
			record = recordServices.getDocumentById(id);
			if (record != null) {
				memoryList.put(id, record);
			}

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


	public Record getRecordSummary(String id) {
		Record record = summaryMemoryList.get(id);
		if (record == null && transaction != null) {
			record = transaction.getRecord(id);
		}
		if (record == null && nestedRecordProvider != null) {
			record = nestedRecordProvider.getRecordSummary(id);
			if (record != null) {
				if (record.getRecordDTOMode() == RecordDTOMode.SUMMARY) {
					summaryMemoryList.put(id, record);
				} else {
					memoryList.put(id, record);
				}
			}
		}
		if (record == null && recordServices != null) {
			record = recordServices.realtimeGetRecordSummaryById(id);
			if (record != null) {
				summaryMemoryList.put(id, record);
			}

		}
		return record;
	}

	public boolean hasRecordSummaryInMemoryList(Object referenceValue) {
		if (referenceValue instanceof String) {
			return summaryMemoryList.containsKey((String) referenceValue);
		} else if (referenceValue instanceof List) {
			for (String referenceItem : (List<String>) referenceValue) {
				if (summaryMemoryList.containsKey(referenceItem)) {
					return true;
				}
			}
		}
		return hasRecordInMemoryList(referenceValue);
	}
}
