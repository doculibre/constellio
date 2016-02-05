package com.constellio.app.modules.tasks.model.wrappers.types;

import com.constellio.app.modules.rm.wrappers.type.SchemaLinkingType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class TaskType extends ValueListItem implements SchemaLinkingType {
	public static final String SCHEMA_TYPE = "ddvTaskType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String LINKED_SCHEMA = "linkedSchema";

	public TaskType(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public TaskType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public TaskType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public TaskType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	@Override
	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public TaskType setLinkedSchema(String linkedSchema) {
		set(LINKED_SCHEMA, linkedSchema);
		return this;
	}
}
