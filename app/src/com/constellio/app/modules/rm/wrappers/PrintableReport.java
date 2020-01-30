package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class PrintableReport extends Printable {
	public final static String SCHEMA_TYPE = "report";
	public final static String SCHEMA_NAME = Printable.SCHEMA_TYPE + "_" + SCHEMA_TYPE;

	public final static String RECORD_TYPE = "recordType";
	public final static String RECORD_SCHEMA = "recordSchema";

	public PrintableReport(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_NAME);
	}

	public PrintableReport setReportType(String reportType) {
		set(RECORD_TYPE, reportType);
		return this;
	}

	public String getReportType() {
		return get(RECORD_TYPE);
	}

	public PrintableReport setSchemaType(String schemaType) {
		set(SCHEMA_TYPE, schemaType);
		return this;
	}

	public String getSchemaType() {
		return SCHEMA_TYPE;
	}

	public PrintableReport setReportSchema(String schema) {
		set(RECORD_SCHEMA, schema);
		return this;
	}

	public String getReportSchema() {
		return get(RECORD_SCHEMA);
	}
}
