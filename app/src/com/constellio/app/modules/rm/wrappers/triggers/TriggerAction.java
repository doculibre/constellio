package com.constellio.app.modules.rm.wrappers.triggers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;

public class TriggerAction extends RecordWrapper {

	public static final String TITLE = Schemas.TITLE_CODE;
	public static final String SCHEMA_TYPE = "triggerAction";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	//ref simplevaleur TriggerActionType
	public static final String TYPE = "type";

	public TriggerAction(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public TriggerAction(Record record, MetadataSchemaTypes types, String schema) {
		super(record, types, schema);
	}

	public String getType() {
		return get(TYPE);
	}

	public TriggerAction setType(String type) {
		set(TYPE, type);
		return this;
	}
}
