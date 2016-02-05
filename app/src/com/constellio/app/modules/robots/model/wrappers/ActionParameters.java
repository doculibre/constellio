package com.constellio.app.modules.robots.model.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ActionParameters extends RecordWrapper {
	public static final String SCHEMA_TYPE = "actionParameters";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public ActionParameters(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	protected ActionParameters(Record record, MetadataSchemaTypes types, String schema) {
		super(record, types, schema);
	}
}
