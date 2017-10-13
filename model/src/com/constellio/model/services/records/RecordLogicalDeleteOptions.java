package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordsFlushing;

public class RecordLogicalDeleteOptions {

	LogicallyDeleteTaxonomyRecordsBehavior behaviorForRecordsAttachedToTaxonomy = LogicallyDeleteTaxonomyRecordsBehavior.KEEP_RECORDS;
	private boolean checkForValidationError = true;

	RecordsFlushing recordsFlushing = RecordsFlushing.NOW();

	boolean skipValidations;

	boolean skipRefresh;

	public LogicallyDeleteTaxonomyRecordsBehavior getBehaviorForRecordsAttachedToTaxonomy() {
		return behaviorForRecordsAttachedToTaxonomy;
	}

	public RecordLogicalDeleteOptions setBehaviorForRecordsAttachedToTaxonomy(
			LogicallyDeleteTaxonomyRecordsBehavior behaviorForRecordsAttachedToTaxonomy) {
		this.behaviorForRecordsAttachedToTaxonomy = behaviorForRecordsAttachedToTaxonomy;
		return this;
	}

	public RecordsFlushing getRecordsFlushing() {
		return recordsFlushing;
	}

	public RecordLogicalDeleteOptions setRecordsFlushing(RecordsFlushing recordsFlushing) {
		this.recordsFlushing = recordsFlushing;
		return this;
	}

	public boolean isSkipValidations() {
		return skipValidations;
	}

	public RecordLogicalDeleteOptions setSkipValidations(boolean skipValidations) {
		this.skipValidations = skipValidations;
		return this;
	}

	public boolean isSkipRefresh() {
		return skipRefresh;
	}

	public RecordLogicalDeleteOptions setSkipRefresh(boolean skipRefresh) {
		this.skipRefresh = skipRefresh;
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
