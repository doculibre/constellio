package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class StorageSpaceType extends ValueListItem implements SchemaLinkingType {

	public static final String SCHEMA_TYPE = "ddvStorageSpaceType";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String LINKED_SCHEMA = "linkedSchema";

	public StorageSpaceType(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public StorageSpaceType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public StorageSpaceType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public StorageSpaceType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public StorageSpaceType setLinkedSchema(String folderSchema) {
		set(LINKED_SCHEMA, folderSchema);
		return this;
	}

}
