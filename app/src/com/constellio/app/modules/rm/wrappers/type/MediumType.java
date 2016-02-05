package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class MediumType extends ValueListItem {

	public static final String SCHEMA_TYPE = "ddvMediumType";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String ANALOGICAL = "analogical";

	public MediumType(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public MediumType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public MediumType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public MediumType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public boolean isAnalogical() {
		return getBooleanWithDefaultValue(ANALOGICAL, false);
	}

	public MediumType setAnalogical(boolean analogical) {
		set(ANALOGICAL, analogical);
		return this;
	}
}
