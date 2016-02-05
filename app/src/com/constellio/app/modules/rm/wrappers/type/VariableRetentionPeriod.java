package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class VariableRetentionPeriod extends ValueListItem {

	public static final String SCHEMA_TYPE = "ddvVariablePeriod";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public VariableRetentionPeriod(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public VariableRetentionPeriod setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public VariableRetentionPeriod setCode(String code) {
		super.setCode(code);
		return this;
	}

	public VariableRetentionPeriod setDescription(String description) {
		super.setDescription(description);
		return this;
	}

}