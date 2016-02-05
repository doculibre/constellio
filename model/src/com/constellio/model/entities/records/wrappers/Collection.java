package com.constellio.model.entities.records.wrappers;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Collection extends RecordWrapper {

	public static final String SCHEMA_TYPE = "collection";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";

	public static final String NAME = "name";

	public static final String LANGUAGES = "languages";

	public Collection(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getCode() {
		return get(CODE);
	}

	public String getName() {
		return get(NAME);
	}

	public Collection setName(String name) {
		set(NAME, name);
		return this;
	}

	public List<String> getLanguages() {
		return get(LANGUAGES);
	}
}
