package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.records.RecordUpdateOptions;

import java.util.function.Consumer;

public class RecordPhysicalDeleteOptions {

	private boolean setMostReferencesToNull;
	private boolean skipValidations;
	private RecordsFlushing recordsFlushing = RecordsFlushing.NOW();
	private Consumer<RecordUpdateOptions> transactionOptionsConsumer;


	PhysicalDeleteTaxonomyRecordsBehavior behaviorForRecordsAttachedToTaxonomy = PhysicalDeleteTaxonomyRecordsBehavior.KEEP_RECORDS;

	public Consumer<RecordUpdateOptions> getTransactionOptionsConsumer() {
		return transactionOptionsConsumer;
	}

	public RecordPhysicalDeleteOptions setTransactionOptionsConsumer(
			Consumer<RecordUpdateOptions> transactionOptionsConsumer) {
		this.transactionOptionsConsumer = transactionOptionsConsumer;
		return this;
	}

	public RecordsFlushing getRecordsFlushing() {
		return recordsFlushing;
	}

	public RecordPhysicalDeleteOptions setRecordsFlushing(
			RecordsFlushing recordsFlushing) {
		this.recordsFlushing = recordsFlushing;
		return this;
	}

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

	public boolean isSkipValidations() {
		return skipValidations;
	}

	public RecordPhysicalDeleteOptions setSkipValidations(boolean skipValidations) {
		this.skipValidations = skipValidations;
		return this;
	}

	public enum PhysicalDeleteTaxonomyRecordsBehavior {
		KEEP_RECORDS,
		PHYSICALLY_DELETE_THEM,
		PHYSICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY

	}

}
