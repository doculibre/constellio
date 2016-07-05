package com.constellio.model.services.records;

public class RecordPhysicalDeleteOptions {

	boolean setMostReferencesToNull;

	PhysicalDeleteTaxonomyRecordsBehavior behaviorForRecordsAttachedToTaxonomy = PhysicalDeleteTaxonomyRecordsBehavior.KEEP_RECORDS;

	public PhysicalDeleteTaxonomyRecordsBehavior getBehaviorForRecordsAttachedToTaxonomy() {
		return behaviorForRecordsAttachedToTaxonomy;
	}

	public RecordPhysicalDeleteOptions setBehaviorForRecordsAttachedToTaxonomy(
			PhysicalDeleteTaxonomyRecordsBehavior behaviorForRecordsAttachedToTaxonomy) {
		this.behaviorForRecordsAttachedToTaxonomy = behaviorForRecordsAttachedToTaxonomy;
		return this;
	}

	public boolean isSetMostReferencesToNull() {
		return setMostReferencesToNull;
	}

	public RecordPhysicalDeleteOptions setMostReferencesToNull(boolean setMostReferencesToNull) {
		this.setMostReferencesToNull = setMostReferencesToNull;
		return this;
	}

	public enum PhysicalDeleteTaxonomyRecordsBehavior {
		KEEP_RECORDS,
		PHYSICALLY_DELETE_THEM,
		PHYSICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY

	}

}
