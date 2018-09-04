package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

public class ExportAudit extends TemporaryRecord {
	public static final String SCHEMA = "exportAudit";
	public static final String FULL_SCHEMA = SCHEMA_TYPE + "_" + SCHEMA;

	public static final String END_DATE = "endDate";

	public ExportAudit(Record record, MetadataSchemaTypes types) {
		super(record, types);
	}

	public LocalDateTime getEndDate() {
		return get(END_DATE);
	}

	public ExportAudit setEndDate(LocalDateTime localDateTime) {
		set(END_DATE, localDateTime);
		return this;
	}
}
