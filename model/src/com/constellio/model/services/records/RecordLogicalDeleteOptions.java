package com.constellio.model.services.records;

public class RecordLogicalDeleteOptions {

	LogicallyDeleteTaxonomyRecordsBehavior behaviorForRecordsAttachedToTaxonomy = LogicallyDeleteTaxonomyRecordsBehavior.KEEP_RECORDS;
	private boolean checkForValidationError = true;

	public LogicallyDeleteTaxonomyRecordsBehavior getBehaviorForRecordsAttachedToTaxonomy() {
		return behaviorForRecordsAttachedToTaxonomy;
	}

	public RecordLogicalDeleteOptions setBehaviorForRecordsAttachedToTaxonomy(
			LogicallyDeleteTaxonomyRecordsBehavior behaviorForRecordsAttachedToTaxonomy) {
		this.behaviorForRecordsAttachedToTaxonomy = behaviorForRecordsAttachedToTaxonomy;
		return this;
	}

	public RecordLogicalDeleteOptions disableValidatorChecking() {
		this.checkForValidationError = false;
		return this;
	}

	public RecordLogicalDeleteOptions enableValidatorChecking(){
		this.checkForValidationError  = true;
		return this;
	}

	public boolean isCheckForValidationErrorEnable() {
		return this.checkForValidationError;
	}

	public enum LogicallyDeleteTaxonomyRecordsBehavior {
		KEEP_RECORDS,
		LOGICALLY_DELETE_THEM,
		LOGICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY
	}

}
