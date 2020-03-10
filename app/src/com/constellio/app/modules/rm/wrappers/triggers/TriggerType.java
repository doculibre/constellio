package com.constellio.app.modules.rm.wrappers.triggers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class TriggerType extends RecordWrapper {

	public static final String SCHEMA_TYPE = "triggerType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String TITLE = "title";

	public TriggerType(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}


}
