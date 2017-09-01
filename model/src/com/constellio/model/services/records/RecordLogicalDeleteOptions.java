package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordsFlushing;

public class RecordLogicalDeleteOptions {

	LogicallyDeleteTaxonomyRecordsBehavior behaviorForRecordsAttachedToTaxonomy = LogicallyDeleteTaxonomyRecordsBehavior.KEEP_RECORDS;

	RecordsFlushing recordsFlushing = RecordsFlushing.NOW();

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

	public enum LogicallyDeleteTaxonomyRecordsBehavior {
		KEEP_RECORDS,
		LOGICALLY_DELETE_THEM,
		LOGICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY
	}

}
