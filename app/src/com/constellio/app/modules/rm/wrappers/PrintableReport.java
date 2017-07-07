package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Marco on 2017-07-07.
 */
public class PrintableReport extends Printable {
    public final static String SCHEMA_TYPE = "report";
    public final static String SCHEMA_NAME = Printable.SCHEMA_TYPE +  "_" + SCHEMA_TYPE;

    public final static String REPORT_TYPE = "reporttype";


    public PrintableReport(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public String getReportType() {
        return get(REPORT_TYPE);
    }

    public String getSchemaType() {
       return SCHEMA_TYPE;
    }
}
