package com.constellio.app.modules.tasks.extensions.api.params;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;

public class TaskFormRetValue {
	private Map<Record, Boolean> recordMap;
	private List<Record> recordListToPreserveOrder;

	public TaskFormRetValue() {
		recordMap = new HashMap<>();
		recordListToPreserveOrder = new ArrayList<>();
	}

	public void addRecord(Record record, boolean saveWithValidation) {
		recordListToPreserveOrder.add(record);
		recordMap.put(record, saveWithValidation);
	}

	public List<Record> getRecords() {
		return recordListToPreserveOrder;
	}

	public boolean isSaveWithValidation(Record record) {
		return recordMap.get(record);
	}

	public void addAll(TaskFormRetValue taskFormRetValue) {
		for (Record record : taskFormRetValue.getRecords()) {
			this.addRecord(record, taskFormRetValue.isSaveWithValidation(record));
		}
	}
}
