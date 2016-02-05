package com.constellio.model.extensions.behaviors;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
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
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.extensions.events.records.RecordSetCategoryEvent;

public class RecordExtension {

	public ExtensionBooleanResult isLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isPhysicallyDeletable(RecordPhysicalDeletionValidationEvent event) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
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

	public void setRecordCategory(RecordSetCategoryEvent event) {
	}

}
