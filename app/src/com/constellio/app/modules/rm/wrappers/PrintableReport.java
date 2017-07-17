package com.constellio.app.modules.rm.wrappers;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
public class PrintableReport extends Printable {
	public final static String SCHEMA_TYPE = "report";
	public final static String SCHEMA_NAME = Printable.SCHEMA_TYPE + "_" + SCHEMA_TYPE;

	public final static String RECORD_TYPE = "reportType";
	public final static String RECORD_SCHEMA = "reportSchema";

	public PrintableReport(Record record, MetadataSchemaTypes types) {
		super(record, types);
	}

	public String getReportType() {
		return get(RECORD_TYPE);
	}

	public String getSchemaType() {
		return SCHEMA_TYPE;
	}

	public String getReportSchema() {
		return get(RECORD_SCHEMA);
	}
}
