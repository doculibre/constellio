package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

public class ImportAudit extends TemporaryRecord {
	public static final String SCHEMA = "importAudit";
	public static final String FULL_SCHEMA = SCHEMA_TYPE + "_" + SCHEMA;
	public static final String ERRORS = "errors";
	public static final String END_DATE = "endDate";

	public ImportAudit(Record record, MetadataSchemaTypes types) {
		super(record, types);
	}

	public String getErrors() {
		return get(ERRORS);
	}

	public TemporaryRecord setErrors(String stringList) {
		set(ERRORS, stringList);
		return this;
	}

	public LocalDateTime getEndDate() {
		return get(END_DATE);
	}

	public ImportAudit setEndDate(LocalDateTime localDateTime) {
		set(END_DATE, localDateTime);
		return this;
	}
}
