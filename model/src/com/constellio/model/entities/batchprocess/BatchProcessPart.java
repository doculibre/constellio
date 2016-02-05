package com.constellio.model.entities.batchprocess;

import java.util.List;

import com.constellio.model.entities.records.Record;

public class BatchProcessPart {

	private final int index;

	private final BatchProcess batchProcess;

	private final List<Record> records;

	public BatchProcessPart(int index, BatchProcess batchProcess, List<Record> records) {
		super();
		this.index = index;
		this.batchProcess = batchProcess;
		this.records = records;
	}

	public BatchProcess getBatchProcess() {
		return batchProcess;
	}

	public List<Record> getRecords() {
		return records;
	}

	public int getIndex() {
		return index;
	}
}
