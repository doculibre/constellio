/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
}
