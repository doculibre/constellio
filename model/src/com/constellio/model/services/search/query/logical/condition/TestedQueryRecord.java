package com.constellio.model.services.search.query.logical.condition;

import com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval;
import com.constellio.model.entities.records.Record;

import java.util.Locale;
import java.util.function.Supplier;

public class TestedQueryRecord implements Supplier<Record> {

	Record record;

	Locale locale;

	LocalisedRecordMetadataRetrieval metadataRetrieval;

	public TestedQueryRecord(Record record, Locale locale,
							 LocalisedRecordMetadataRetrieval metadataRetrieval) {
		this.record = record;
		this.locale = locale;
		this.metadataRetrieval = metadataRetrieval;
	}

	public Record getRecord() {
		return record;
	}

	public Locale getLocale() {
		return locale;
	}

	public LocalisedRecordMetadataRetrieval getMetadataRetrieval() {
		return metadataRetrieval;
	}

	@Override
	public Record get() {
		return record;
	}
}
