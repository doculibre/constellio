package com.constellio.model.services.records;

import com.constellio.model.entities.records.RecordUpdateOptions;

public class BulkRecordTransactionHandlerOptions {

	int recordsPerBatch;
	int queueSize;
	int numberOfThreads;
	boolean showProgressionInConsole;
	RecordUpdateOptions transactionOptions = new RecordUpdateOptions();
	BulkRecordTransactionImpactHandling recordModificationImpactHandling;

	public BulkRecordTransactionHandlerOptions() {
		recordsPerBatch = 1000;
		numberOfThreads = Runtime.getRuntime().availableProcessors();
		queueSize = 1 + (numberOfThreads);
		showProgressionInConsole = true;
		recordModificationImpactHandling = BulkRecordTransactionImpactHandling.IN_SAME_TRANSACTION;
	}

	private BulkRecordTransactionHandlerOptions(int recordsPerBatch, int numberOfThreads, int queueSize,
			boolean showProgressionInConsole) {
		this.recordsPerBatch = recordsPerBatch;
		this.numberOfThreads = numberOfThreads;
		this.queueSize = queueSize;
		this.showProgressionInConsole = showProgressionInConsole;
	}

	public BulkRecordTransactionHandlerOptions withRecordsPerBatch(int recordsPerBatch) {
		this.recordsPerBatch = recordsPerBatch;
		return this;
	}

	public BulkRecordTransactionHandlerOptions withQueueSize(int queueSize) {
		this.queueSize = queueSize;
		return this;
	}

	public BulkRecordTransactionHandlerOptions withNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
		return this;
	}

	public BulkRecordTransactionHandlerOptions showProgressionInConsole(boolean showProgressionInConsole) {
		this.showProgressionInConsole = showProgressionInConsole;
		return this;
	}

	public BulkRecordTransactionHandlerOptions withBulkRecordTransactionImpactHandling(
			BulkRecordTransactionImpactHandling recordModificationImpactHandling) {
		this.recordModificationImpactHandling = recordModificationImpactHandling;
		return this;
	}

	public BulkRecordTransactionHandlerOptions setTransactionOptions(
			RecordUpdateOptions transactionOptions) {
		this.transactionOptions = transactionOptions;
		return this;
	}

	public RecordUpdateOptions getTransactionOptions() {
		return transactionOptions;
	}

	public int getRecordsPerBatch() {
		return recordsPerBatch;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	public boolean isShowProgressionInConsole() {
		return showProgressionInConsole;
	}

	public BulkRecordTransactionImpactHandling getRecordModificationImpactHandling() {
		return recordModificationImpactHandling;
	}
}
