package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.Locale;

public class LegalReference extends RecordWrapper {
	public static final String SCHEMA_TYPE = "legalReference";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String URL = "url";

	public LegalReference(Record record, MetadataSchemaTypes types, Locale locale) {
		super(record, types, SCHEMA_TYPE, locale);
	}

	public LegalReference setTitle(Locale locale, String title) {
		super.set(TITLE, locale, title);
		return this;
	}

	public LegalReference setTitle(String title) {
		super.set(TITLE, title);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

	public LegalReference setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getUrl() {
		return get(URL);
	}

	public LegalReference setUrl(String url) {
		set(URL, url);
		return this;
	}
}
