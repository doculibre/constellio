package com.constellio.model.extensions;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.behaviors.RecordExtension.IsRecordModifiableByParams;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.behaviors.SchemaExtension;
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
import com.constellio.model.extensions.events.schemas.PutSchemaRecordsInTrashEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.extensions.events.records.RecordSetCategoryEvent;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.extensions.events.schemas.SchemaEvent;

public class ModelLayerCollectionExtensions {

	//------------ Extension points -----------

	public VaultBehaviorsList<RecordImportExtension> recordImportExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordExtension> recordExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<SchemaExtension> schemaExtensions = new VaultBehaviorsList<>();

	//----------------- Callers ---------------

	public void callSetRecordCategory(RecordSetCategoryEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.setRecordCategory(event);
		}
	}

	public void callRecordImportBuild(String schemaType, BuildParams params) {
		for (RecordImportExtension extension : recordImportExtensions) {
			if (extension.getDecoratedSchemaType().equals(schemaType)) {
				extension.build(params);
			}
		}
	}

	public void callRecordImportValidate(String schemaType, ValidationParams params) {
		for (RecordImportExtension extension : recordImportExtensions) {
			if (extension.getDecoratedSchemaType().equals(schemaType)) {
				extension.validate(params);
			}
		}
	}

	public void callRecordImportPrevalidate(String schemaType, PrevalidationParams params) {
		for (RecordImportExtension extension : recordImportExtensions) {
			if (extension.getDecoratedSchemaType().equals(schemaType)) {
				extension.prevalidate(params);
			}
		}
	}

	public void callRecordInCreationBeforeSave(
			RecordInCreationBeforeSaveEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordInCreationBeforeSave(event);
		}
	}

	public void callRecordInModificationBeforeSave(
			RecordInModificationBeforeSaveEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordInModificationBeforeSave(event);
		}
	}

	public void callRecordInCreationBeforeValidationAndAutomaticValuesCalculation(
			RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordInCreationBeforeValidationAndAutomaticValuesCalculation(event);
		}
	}

	public void callRecordInModificationBeforeValidationAndAutomaticValuesCalculation(
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordInModificationBeforeValidationAndAutomaticValuesCalculation(event);
		}
	}

	public void callRecordCreated(RecordCreationEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordCreated(event);
		}
	}

	public void callRecordModified(RecordModificationEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordModified(event);
		}
	}

	public void callRecordLogicallyDeleted(RecordLogicalDeletionEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordLogicallyDeleted(event);
		}
	}

	public void callRecordPhysicallyDeleted(RecordPhysicalDeletionEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordPhysicallyDeleted(event);
		}
	}

	public void callRecordRestored(RecordRestorationEvent event) {
		for (RecordExtension extension : recordExtensions) {
			extension.recordRestored(event);
		}
	}

	public boolean isLogicallyDeletable(final RecordLogicalDeletionValidationEvent event) {
		return recordExtensions.getBooleanValue(true, new BooleanCaller<RecordExtension>() {
			@Override
			public ExtensionBooleanResult call(RecordExtension behavior) {
				return behavior.isLogicallyDeletable(event);
			}
		});
	}

	public boolean isPhysicallyDeletable(final RecordPhysicalDeletionValidationEvent event) {
		return recordExtensions.getBooleanValue(true, new BooleanCaller<RecordExtension>() {
			@Override
			public ExtensionBooleanResult call(RecordExtension behavior) {
				return behavior.isPhysicallyDeletable(event);
			}
		});
	}

	public boolean isPutInTrashBeforePhysicalDelete(final SchemaEvent event) {
		Boolean inTrashFirst = schemaExtensions.getBooleanValue(null, new BooleanCaller<SchemaExtension>() {
			@Override
			public ExtensionBooleanResult call(SchemaExtension behavior) {
				return behavior.isPutInTrashBeforePhysicalDelete(event);
			}
		});
		return (inTrashFirst == null) ? false : inTrashFirst;
	}

	@Deprecated
	//Use tokens instead
	public boolean isRecordModifiableBy(final Record record, final User user) {
		return recordExtensions.getBooleanValue(true, new BooleanCaller<RecordExtension>() {
			@Override
			public ExtensionBooleanResult call(RecordExtension behavior) {
				return behavior.isRecordModifiableBy(new IsRecordModifiableByParams(record, user));
			}
		});
	}
}
