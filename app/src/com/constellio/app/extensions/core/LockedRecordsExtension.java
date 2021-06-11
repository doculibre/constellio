package com.constellio.app.extensions.core;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionValidationEvent;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.SchemaUtils;

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

		KeyListMap<String, String> logicallyLockedRecords = collectionExtensions.lockedRecords;
		codeOrLinkedSchemaMustNotBeModified(event, logicallyLockedRecords);

		KeyListMap<String, String> physicallylockedRecords = collectionExtensions.physicallyLockedRecords;
		codeOrLinkedSchemaMustNotBeModified(event, physicallylockedRecords);
	}

	private void codeOrLinkedSchemaMustNotBeModified(RecordInModificationBeforeSaveEvent event,
													 KeyListMap<String, String> lockedRecords) {
		if (lockedRecords.contains(event.getSchemaTypeCode())) {
			String recordCode = event.getRecord().getCopyOfOriginalRecord().get(Schemas.CODE);
			String recordLinkedSchema = event.getRecord().getCopyOfOriginalRecord().get(Schemas.LINKED_SCHEMA);
			if (lockedRecords.get(event.getSchemaTypeCode()).contains(recordCode)) {
				boolean modifiedLinkedSchema = !isEqual(recordLinkedSchema, event.getRecord().get(Schemas.LINKED_SCHEMA));

				if (!recordCode.equals(event.getRecord().get(Schemas.CODE)) || modifiedLinkedSchema) {
					event.getValidationErrors().add(LockedRecordsExtension.class, CODE_OR_LINKED_SCHEMA_MUST_NOT_BE_MODIFIED);
				}
			}
		}
	}

	@Override
	public ValidationErrors validatePhysicallyDeletable(RecordPhysicalDeletionValidationEvent event) {
		KeyListMap<String, String> lockedRecords = collectionExtensions.lockedRecords;

		ValidationErrors validationErrors = validateLockedRecords(event.getRecord(), lockedRecords);

		if (validationErrors == null || validationErrors.isEmpty()) {
			return super.validatePhysicallyDeletable(event);
		} else {
			return validationErrors;
		}
	}

	@Override
	public ValidationErrors validateLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {

		KeyListMap<String, String> lockedRecords = collectionExtensions.lockedRecords;

		ValidationErrors validationErrors = validateLockedRecords(event.getRecord(), lockedRecords);

		if (validationErrors == null || validationErrors.isEmpty()) {
			return super.validateLogicallyDeletable(event);
		} else {
			return validationErrors;
		}
	}

	private ValidationErrors validateLockedRecords(Record record,
												   KeyListMap<String, String> lockedRecords) {
		ValidationErrors validationErrors = new ValidationErrors();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		if (lockedRecords.contains(schemaTypeCode)) {
			MetadataSchema schema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaOf(record);
			if (schema.hasMetadataWithCode(Schemas.CODE.getLocalCode()) &&
				lockedRecords.get(schemaTypeCode).contains(record.get(Schemas.CODE))) {
				validationErrors.add(LockedRecordsExtension.class, CANNOT_DELETE_LOCKED_RECORD);
				return validationErrors;
			} else if (lockedRecords.get(schemaTypeCode).contains(record.getId())) {
				validationErrors.add(LockedRecordsExtension.class, CANNOT_DELETE_LOCKED_RECORD);
				return validationErrors;
			}
		}

		return null;
	}
}
