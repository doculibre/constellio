package com.constellio.model.extensions.params;

import java.util.Locale;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class GetCaptionForRecordParams {

	Record record;

	MetadataSchemaTypes types;

	Locale locale;

	public GetCaptionForRecordParams(Record record, MetadataSchemaTypes types, Locale locale) {
		this.record = record;
		this.types = types;
		this.locale = locale;
	}

	public Record getRecord() {
		return record;
	}

	public MetadataSchemaTypes getTypes() {
		return types;
	}

	public Locale getLocale() {
		return locale;
	}
}
