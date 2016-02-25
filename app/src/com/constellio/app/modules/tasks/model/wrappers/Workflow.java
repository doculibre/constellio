package com.constellio.app.modules.tasks.model.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Workflow extends RecordWrapper {

	public static final String SCHEMA_TYPE = "workflow";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";

	public Workflow(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getCode() {
		return get(CODE);
	}

	public Workflow setCode(String code) {
		set(CODE, code);
		return this;
	}

	public Workflow setTitle(String title) {
		super.setTitle(title);
		return this;
	}
}
