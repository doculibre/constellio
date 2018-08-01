package com.constellio.model.extensions.params;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.Locale;

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
