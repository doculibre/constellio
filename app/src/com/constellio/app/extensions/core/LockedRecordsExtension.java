package com.constellio.app.extensions.core;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.frameworks.validation.ExtensionValidationErrors;
import com.constellio.model.frameworks.validation.ValidationErrors;

import static com.constellio.data.utils.LangUtils.isEqual;

/**
 * Created by Charles Blanchette on 2017-03-30.
 */
public class LockedRecordsExtension extends RecordExtension {

	public static final String CODE_OR_LINKED_SCHEMA_MUST_NOT_BE_MODIFIED = "codeOrLinkedSchemaMustNotBeModified";
	public static final String CANNOT_DELETE_LOCKED_RECORD = "cannotDeleteLockedRecord";
	AppLayerFactory appLayerFactory;
	AppLayerCollectionExtensions collectionExtensions;

	public LockedRecordsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.collectionExtensions = appLayerFactory.getExtensions().forCollection(collection);

	}

	@Override
	public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {

		if (collectionExtensions.lockedRecords.contains(event.getSchemaTypeCode())) {
			String recordCode = event.getRecord().getCopyOfOriginalRecord().get(Schemas.CODE);
			String recordLinkedSchema = event.getRecord().getCopyOfOriginalRecord().get(Schemas.LINKED_SCHEMA);
			if (collectionExtensions.lockedRecords.get(event.getSchemaTypeCode()).contains(recordCode)) {
				boolean modifiedLinkedSchema = !isEqual(recordLinkedSchema, event.getRecord().get(Schemas.LINKED_SCHEMA));

				if (!recordCode.equals(event.getRecord().get(Schemas.CODE)) || modifiedLinkedSchema) {
					event.getValidationErrors().add(LockedRecordsExtension.class, CODE_OR_LINKED_SCHEMA_MUST_NOT_BE_MODIFIED);
				}
			}
		}
	}

	@Override
	public ExtensionValidationErrors isLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		ValidationErrors validationErrors = new ValidationErrors();
		if (collectionExtensions.lockedRecords.contains(event.getSchemaTypeCode())) {
			Record record = event.getRecord();
			MetadataSchema schema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(record);
			if (schema.hasMetadataWithCode(Schemas.CODE.getLocalCode()) &&
				collectionExtensions.lockedRecords.get(event.getSchemaTypeCode()).contains(record.get(Schemas.CODE))) {
				validationErrors.add(LockedRecordsExtension.class, CANNOT_DELETE_LOCKED_RECORD);
				return new ExtensionValidationErrors(validationErrors, ExtensionBooleanResult.FALSE);
			} else if (collectionExtensions.lockedRecords.get(event.getSchemaTypeCode()).contains(record.getId())) {
				validationErrors.add(LockedRecordsExtension.class, CANNOT_DELETE_LOCKED_RECORD);
				return new ExtensionValidationErrors(validationErrors, ExtensionBooleanResult.FALSE);
			}
		}
		return super.isLogicallyDeletable(event);
	}
}
