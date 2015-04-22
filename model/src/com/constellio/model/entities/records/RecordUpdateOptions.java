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
package com.constellio.model.entities.records;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordsFlushing;

public class RecordUpdateOptions {

	private TransactionRecordsReindexation transactionRecordsReindexation = new TransactionRecordsReindexation();

	private OptimisticLockingResolution resolution = OptimisticLockingResolution.TRY_MERGE;

	private RecordsFlushing recordsFlushing = RecordsFlushing.NOW;

	private boolean updateModificationInfos = true;

	private boolean fullRewrite = false;

	private boolean validationsEnabled = true;

	public RecordUpdateOptions() {

	}

	public RecordUpdateOptions(RecordUpdateOptions copy) {
		this.transactionRecordsReindexation = copy.transactionRecordsReindexation;
		this.resolution = copy.resolution;
		this.recordsFlushing = copy.recordsFlushing;
		this.updateModificationInfos = copy.updateModificationInfos;
		this.fullRewrite = copy.fullRewrite;
		this.validationsEnabled = copy.validationsEnabled;
	}

	public RecordUpdateOptions forceReindexationOfMetadatas(TransactionRecordsReindexation transactionRecordsReindexation) {
		this.transactionRecordsReindexation = transactionRecordsReindexation;
		return this;
	}

	public RecordUpdateOptions onOptimisticLocking(OptimisticLockingResolution resolution) {
		this.resolution = resolution;
		return this;
	}

	public TransactionRecordsReindexation getTransactionRecordsReindexation() {
		return transactionRecordsReindexation;
	}

	public OptimisticLockingResolution getOptimisticLockingResolution() {
		return resolution;
	}

	public RecordUpdateOptions setOptimisticLockingResolution(OptimisticLockingResolution resolution) {
		this.resolution = resolution;
		return this;
	}

	public RecordsFlushing getRecordsFlushing() {
		return recordsFlushing;
	}

	public RecordUpdateOptions setRecordsFlushing(RecordsFlushing recordsFlushing) {
		this.recordsFlushing = recordsFlushing;
		return this;
	}

	public boolean isUpdateModificationInfos() {
		return updateModificationInfos;
	}

	public RecordUpdateOptions setUpdateModificationInfos(boolean updateModificationInfos) {
		this.updateModificationInfos = updateModificationInfos;
		return this;
	}

	public boolean isFullRewrite() {
		return fullRewrite;
	}

	public RecordUpdateOptions setFullRewrite(boolean fullRewrite) {
		this.fullRewrite = fullRewrite;
		return this;
	}

	public boolean isValidationsEnabled() {
		return validationsEnabled;
	}

	public RecordUpdateOptions setValidationsEnabled(boolean validationsEnabled) {
		this.validationsEnabled = validationsEnabled;
		return this;
	}
}
