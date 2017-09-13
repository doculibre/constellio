package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class YearType extends ValueListItem implements SchemaLinkingType {
	public static final String SCHEMA_TYPE = "ddvYearType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String YEAR_END = "yearEnd";

	public YearType(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public YearType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public YearType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public YearType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public YearType setYearEnd(String yearEnd) {
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
