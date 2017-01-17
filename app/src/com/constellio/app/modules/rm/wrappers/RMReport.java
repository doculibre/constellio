package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.reports.wrapper.ReportConfig;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Classe RM pour les Rapports.
 *
 * @author Nicolas D'Amours & Charles Blanchette.
 */
public class RMReport extends ReportConfig {

    public static final String SCHEMA_LABEL = "reportslabel";
    public static final String HEIGHT = "height";
    public static final String WIDTH = "width";

    public RMReport(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public RMReport setHeight(String height) {
        set(HEIGHT, height);
        return this;
    }

    public String getHeight() {
        return get(HEIGHT);
    }

    public RMReport setWidth(String width) {
        set(WIDTH, width);
        return this;
    }

    public String getWidth() {
        return get(WIDTH);
    }


}
