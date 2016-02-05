package com.constellio.app.api.extensions;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class GenericRecordPageExtension {

	public ExtensionBooleanResult canManageSchema(User user, MetadataSchemaType schemaType) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult canViewSchemaRecord(User user, MetadataSchemaType schemaType, Record restrictedRecord) {
		return canManageSchema(user, schemaType);
	}

	public ExtensionBooleanResult canModifySchemaRecord(User user, MetadataSchemaType schemaType, Record restrictedRecord) {
		return canManageSchema(user, schemaType);
	}

	public ExtensionBooleanResult canLogicallyDeleteSchemaRecord(User user, MetadataSchemaType schemaType,
			Record restrictedRecord) {
		return canManageSchema(user, schemaType);
	}

	public ExtensionBooleanResult isSchemaTypeConfigurable(MetadataSchemaType schemaType) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}
}
