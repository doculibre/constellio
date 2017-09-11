package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class DateType extends ValueListItem implements SchemaLinkingType {
	public static final String SCHEMA_TYPE = "ddvDateType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String YEAR_END = "yearEnd";

	public DateType(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public DateType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public DateType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public DateType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public DateType setYearEnd(String yearEnd) {
		set(YEAR_END, yearEnd);
		return this;
	}

	public String getYearEnd() {
		return get(YEAR_END);
	}

	@Override
	public String getLinkedSchema() {
		return null;
	}
}
