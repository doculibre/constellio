package com.constellio.model.entities.records;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordsFlushing;

public class RecordUpdateOptions {

	private TransactionRecordsReindexation transactionRecordsReindexation = new TransactionRecordsReindexation();

	private OptimisticLockingResolution resolution = OptimisticLockingResolution.TRY_MERGE;

	private RecordsFlushing recordsFlushing = RecordsFlushing.NOW;

	private boolean skipReferenceValidation = false;

	private boolean updateModificationInfos = true;

	private boolean fullRewrite = false;

	private boolean validationsEnabled = true;

	private boolean unicityValidationsEnabled = true;

	private boolean skipMaskedMetadataValidations = false;

	private boolean skipUSRMetadatasRequirementValidations = false;

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

	public boolean isSkipUSRMetadatasRequirementValidations() {
		return skipUSRMetadatasRequirementValidations;
	}

	public RecordUpdateOptions setSkipUSRMetadatasRequirementValidations(boolean skipUSRMetadatasRequirementValidations) {
		this.skipUSRMetadatasRequirementValidations = skipUSRMetadatasRequirementValidations;
		return this;
	}

	public boolean isSkipMaskedMetadataValidations() {
		return skipMaskedMetadataValidations;
	}

	public RecordUpdateOptions setSkipMaskedMetadataValidations(boolean skipMaskedMetadataValidations) {
		this.skipMaskedMetadataValidations = skipMaskedMetadataValidations;
		return this;
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

	public boolean isSkipReferenceValidation() {
		return skipReferenceValidation;
	}

	public RecordUpdateOptions setSkipReferenceValidation(boolean skipReferenceValidation) {
		this.skipReferenceValidation = skipReferenceValidation;
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

	public boolean isUnicityValidationsEnabled() {
		return unicityValidationsEnabled;
	}

	public RecordUpdateOptions setUnicityValidationsEnabled(boolean unicityValidationsEnabled) {
		this.unicityValidationsEnabled = unicityValidationsEnabled;
		return this;
	}
}
