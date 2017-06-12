package com.constellio.app.modules.tasks.model.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class BetaWorkflow extends RecordWrapper {

	public static final String SCHEMA_TYPE = "workflow";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";

	public BetaWorkflow(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getCode() {
		return get(CODE);
	}

	public BetaWorkflow setCode(String code) {
		set(CODE, code);
		return this;
	}

	public BetaWorkflow setTitle(String title) {
		super.setTitle(title);
		return this;
	}
}
