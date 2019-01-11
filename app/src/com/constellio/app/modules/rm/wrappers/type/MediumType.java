package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.Map;

public class MediumType extends ValueListItem {

	public static final String SCHEMA_TYPE = "ddvMediumType";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String ANALOGICAL = "analogical";

	public static final String ACTIVATED_ON_CONTENT = "activatedOnContent";

	public MediumType(Record record,
					  MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public MediumType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	@Override
	public MediumType setTitles(Map<Language, String> titles) {
		return (MediumType) super.setTitles(titles);
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

	public boolean isActivatedOnContent() {
		return getBooleanWithDefaultValue(ACTIVATED_ON_CONTENT, false);
	}

	public MediumType setActivatedOnContent(Boolean activatedOnContent) {
		set(ACTIVATED_ON_CONTENT, activatedOnContent);
		return this;
	}
}
