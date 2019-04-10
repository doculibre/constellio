package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.Locale;
import java.util.Map;

public class UserFunction extends ValueListItem {

	public static final String SCHEMA_TYPE = "ddvUserFunction";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";


	public UserFunction(Record record,
						MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);

	}

	@Override
	public UserFunction setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	@Override
	public UserFunction setTitle(Locale locale, String title) {
		super.setTitle(locale, title);
		return this;
	}

	@Override
	public UserFunction setTitles(Map<Language, String> titles) {
		super.setTitles(titles);
		return this;
	}

	@Override
	public UserFunction setDescription(String description) {
		super.setDescription(description);
		return this;
	}
}
