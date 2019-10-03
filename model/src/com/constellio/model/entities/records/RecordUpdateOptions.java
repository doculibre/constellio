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

	private boolean catchExtensionsExceptions = false;

	private boolean catchExtensionsValidationsErrors = false;

	private boolean catchBrokenReferenceErrors = false;

	boolean skippingRequiredValuesValidation = false;
	private boolean skippingReferenceToLogicallyDeletedValidation = false;

	private boolean skipFindingRecordsToReindex = false;

	boolean allowSchemaTypeLockedRecordsModification = false;

	private boolean overwriteModificationDateAndUser = true;

	boolean updateAggregatedMetadatas = false;

	boolean updateCalculatedMetadatas = true;

	private boolean repopulate = true;

	public RecordUpdateOptions() {

	}

	public RecordUpdateOptions(RecordUpdateOptions copy) {
		this.transactionRecordsReindexation = copy.transactionRecordsReindexation;

		this.resolution = copy.resolution;

		this.recordsFlushing = copy.recordsFlushing;

		this.skipReferenceValidation = copy.skipReferenceValidation;

		this.updateModificationInfos = copy.updateModificationInfos;

		this.fullRewrite = copy.fullRewrite;

		this.validationsEnabled = copy.validationsEnabled;

		this.unicityValidationsEnabled = copy.unicityValidationsEnabled;

		this.skipMaskedMetadataValidations = copy.skipMaskedMetadataValidations;

		this.catchExtensionsExceptions = copy.catchExtensionsExceptions;

		this.catchExtensionsValidationsErrors = copy.catchExtensionsValidationsErrors;

		this.catchBrokenReferenceErrors = copy.catchBrokenReferenceErrors;

		this.skipUSRMetadatasRequirementValidations = copy.skipUSRMetadatasRequirementValidations;

		this.skippingRequiredValuesValidation = copy.skippingRequiredValuesValidation;
		this.skippingReferenceToLogicallyDeletedValidation = copy.skippingReferenceToLogicallyDeletedValidation;
		this.skipFindingRecordsToReindex = copy.skipFindingRecordsToReindex;
		this.overwriteModificationDateAndUser = copy.overwriteModificationDateAndUser;

		this.updateAggregatedMetadatas = copy.updateAggregatedMetadatas;
		this.updateCalculatedMetadatas = copy.updateCalculatedMetadatas;

		this.repopulate = copy.repopulate;
	}

	public boolean isUpdateCalculatedMetadatas() {
		return updateCalculatedMetadatas;
	}

	public RecordUpdateOptions setUpdateCalculatedMetadatas(boolean updateCalculatedMetadatas) {
		this.updateCalculatedMetadatas = updateCalculatedMetadatas;
		return this;
	}

	public boolean isUpdateAggregatedMetadatas() {
		return updateAggregatedMetadatas;
	}

	public RecordUpdateOptions setUpdateAggregatedMetadatas(boolean updateAggregatedMetadatas) {
		this.updateAggregatedMetadatas = updateAggregatedMetadatas;
		return this;
	}

	public boolean isSkipFindingRecordsToReindex() {
		return skipFindingRecordsToReindex;
	}

	public RecordUpdateOptions setSkipFindingRecordsToReindex(boolean skipFindingRecordsToReindex) {
		this.skipFindingRecordsToReindex = skipFindingRecordsToReindex;
		return this;
	}

	public boolean isAllowSchemaTypeLockedRecordsModification() {
		return allowSchemaTypeLockedRecordsModification;
	}

	public RecordUpdateOptions setAllowSchemaTypeLockedRecordsModification(
			boolean allowSchemaTypeLockedRecordsModification) {
		this.allowSchemaTypeLockedRecordsModification = allowSchemaTypeLockedRecordsModification;
		return this;
	}

	public boolean isSkipUSRMetadatasRequirementValidations() {
		return skipUSRMetadatasRequirementValidations;
	}

	public RecordUpdateOptions setSkipUSRMetadatasRequirementValidations(
			boolean skipUSRMetadatasRequirementValidations) {
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

	public RecordUpdateOptions setForcedReindexationOfMetadatas(
			TransactionRecordsReindexation transactionRecordsReindexation) {
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

	public boolean isSkippingRequiredValuesValidation() {
		return skippingRequiredValuesValidation;
	}

	public RecordUpdateOptions setSkippingRequiredValuesValidation(boolean skippingRequiredValuesValidation) {
		this.skippingRequiredValuesValidation = skippingRequiredValuesValidation;
		return this;
	}

	public boolean isSkippingReferenceToLogicallyDeletedValidation() {
		return skippingReferenceToLogicallyDeletedValidation;
	}

	public RecordUpdateOptions setSkippingReferenceToLogicallyDeletedValidation(
			boolean skippingReferenceToLogicallyDeletedValidation) {
		this.skippingReferenceToLogicallyDeletedValidation = skippingReferenceToLogicallyDeletedValidation;
		return this;
	}

	public static RecordUpdateOptions validationExceptionSafeOptions() {
		return new RecordUpdateOptions().setSkipReferenceValidation(true).setValidationsEnabled(false)
				.setSkipMaskedMetadataValidations(true).setUnicityValidationsEnabled(false)
				.setSkippingReferenceToLogicallyDeletedValidation(true).setSkippingRequiredValuesValidation(true)
				.setCatchExtensionsExceptions(true).setCatchExtensionsValidationsErrors(true).setCatchBrokenReferenceErrors(true);
	}

	public static RecordUpdateOptions userModificationsSafeOptions() {
		return new RecordUpdateOptions().setSkipUSRMetadatasRequirementValidations(true).setSkipMaskedMetadataValidations(true);
	}

	public boolean isCatchExtensionsExceptions() {
		return catchExtensionsExceptions;
	}

	public RecordUpdateOptions setCatchExtensionsExceptions(boolean catchExtensionsExceptions) {
		this.catchExtensionsExceptions = catchExtensionsExceptions;
		return this;
	}

	public boolean isCatchExtensionsValidationsErrors() {
		return catchExtensionsValidationsErrors;
	}

	public RecordUpdateOptions setCatchExtensionsValidationsErrors(boolean catchExtensionsValidationsErrors) {
		this.catchExtensionsValidationsErrors = catchExtensionsValidationsErrors;
		return this;
	}

	public boolean isCatchBrokenReferenceErrors() {
		return catchBrokenReferenceErrors;
	}

	public RecordUpdateOptions setCatchBrokenReferenceErrors(boolean catchBrokenReferenceErrors) {
		this.catchBrokenReferenceErrors = catchBrokenReferenceErrors;
		return this;
	}

	public boolean isOverwriteModificationDateAndUser() {
		return overwriteModificationDateAndUser;
	}

	public RecordUpdateOptions setOverwriteModificationDateAndUser(boolean overwriteModificationDateAndUser) {
		this.overwriteModificationDateAndUser = overwriteModificationDateAndUser;
		return this;
	}


	public boolean isRepopulate() {
		return repopulate;
	}

	public RecordUpdateOptions setRepopulate(boolean repopulate) {
		this.repopulate = repopulate;
		return this;
	}
}
