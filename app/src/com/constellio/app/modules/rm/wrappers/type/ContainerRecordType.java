package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ContainerRecordType extends ValueListItem implements SchemaLinkingType {
	public static final String SCHEMA_TYPE = "ddvContainerRecordType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String LINKED_SCHEMA = "linkedSchema";

	public ContainerRecordType(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public ContainerRecordType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public ContainerRecordType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public ContainerRecordType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public ContainerRecordType setLinkedSchema(String folderSchema) {
		set(LINKED_SCHEMA, folderSchema);
		return this;
	}
}
