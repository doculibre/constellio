package com.constellio.model.extensions.behaviors;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionValidationEvent;
import com.constellio.model.extensions.events.records.RecordReindexationEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.extensions.events.records.RecordSetCategoryEvent;
import com.constellio.model.extensions.events.records.TransactionExecutedEvent;
import com.constellio.model.extensions.events.records.TransactionExecutionBeforeSaveEvent;
import com.constellio.model.extensions.params.GetCaptionForRecordParams;
import com.constellio.model.frameworks.validation.ExtensionValidationErrors;

public class RecordExtension {

	public ExtensionValidationErrors isLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		return new ExtensionValidationErrors(ExtensionBooleanResult.NOT_APPLICABLE);
	}

	public ExtensionValidationErrors isPhysicallyDeletable(RecordPhysicalDeletionValidationEvent event) {
		return new ExtensionValidationErrors(ExtensionBooleanResult.NOT_APPLICABLE);
	}

	public void recordInCreationBeforeValidationAndAutomaticValuesCalculation(
			RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event) {
	}

	public void recordInModificationBeforeValidationAndAutomaticValuesCalculation(
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
	}

	public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
	}

	public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
	}

	public void recordCreated(RecordCreationEvent event) {
	}

	public void recordModified(RecordModificationEvent event) {
	}

	public void recordLogicallyDeleted(RecordLogicalDeletionEvent event) {
	}

	public void recordPhysicallyDeleted(RecordPhysicalDeletionEvent event) {
	}

	public void recordRestored(RecordRestorationEvent event) {
	}

	public void recordReindexed(RecordReindexationEvent event) {
	}

	public void transactionExecuted(TransactionExecutedEvent event) {
	}

	public void setRecordCategory(RecordSetCategoryEvent event) {
	}

	public void transactionExecutionBeforeSave(TransactionExecutionBeforeSaveEvent event) {

	}

	public ExtensionBooleanResult isRecordModifiableBy(IsRecordModifiableByParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public static class IsRecordModifiableByParams {

		Record record;
		User user;

		public IsRecordModifiableByParams(Record record, User user) {
			this.record = record;
			this.user = user;
		}

		public Record getRecord() {
			return record;
		}

		public User getUser() {
			return user;
		}

		public boolean isSchemaType(String schemaType) {
			return record.getSchemaCode().startsWith(schemaType + "_");
		}
	}

	public ExtensionValidationErrors validateDeleteAuthorized(Record record, User user) {
		return new ExtensionValidationErrors(ExtensionBooleanResult.NOT_APPLICABLE);
	}

	public boolean isModifyBlocked(Record record, User user) {
		return false;
	}

	public String getCaptionForRecord(GetCaptionForRecordParams params) {
		return null;
	}
}
