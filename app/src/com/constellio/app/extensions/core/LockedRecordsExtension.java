package com.constellio.app.extensions.core;

import static com.constellio.data.utils.LangUtils.isEqual;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;

/**
 * Created by Charles Blanchette on 2017-03-30.
 */
public class LockedRecordsExtension extends RecordExtension {

	public static final String CODE_OR_LINKED_SCHEMA_MUST_NOT_BE_MODIFIED = "codeOrLinkedSchemaMustNotBeModified";
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
	public ExtensionBooleanResult isLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		if (collectionExtensions.lockedRecords.contains(event.getSchemaTypeCode())) {
			return ExtensionBooleanResult.FALSE;
		}
		return super.isLogicallyDeletable(event);
	}
}
