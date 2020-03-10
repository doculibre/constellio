package com.constellio.app.modules.rm.wrappers.triggers;

import com.constellio.app.modules.rm.wrappers.type.SchemaLinkingType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class TriggerActionType extends RecordWrapper implements SchemaLinkingType {

	public static final String SCHEMA_TYPE = "triggerActionType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String TITLE = "title";
	public static final String LINKED_SCHEMA = "linkedSchema";

	public TriggerActionType(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	@Override
	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}
}
