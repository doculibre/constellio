package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class FolderType extends ValueListItem implements SchemaLinkingType {

	public static final String SCHEMA_TYPE = "ddvFolderType";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String LINKED_SCHEMA = "linkedSchema";

	public FolderType(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public FolderType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public FolderType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public FolderType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public FolderType setLinkedSchema(String folderSchema) {
		set(LINKED_SCHEMA, folderSchema);
		return this;
	}

}
