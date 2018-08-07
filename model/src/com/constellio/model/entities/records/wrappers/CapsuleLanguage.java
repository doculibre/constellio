package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.Map;

public class CapsuleLanguage extends ValueListItem {

	public static final String SCHEMA_TYPE = "ddvCapsuleLanguage";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public CapsuleLanguage(Record record,
						   MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public CapsuleLanguage setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	@Override
	public CapsuleLanguage setTitles(Map<Language, String> titles) {
		return (CapsuleLanguage) super.setTitles(titles);
	}

	public CapsuleLanguage setCode(String code) {
		super.setCode(code);
		return this;
	}

	public CapsuleLanguage setDescription(String description) {
		super.setDescription(description);
		return this;
	}


}
