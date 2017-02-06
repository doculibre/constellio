package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.reports.wrapper.ReportConfig;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Classe RM pour les Rapports.
 *
 * @author Nicolas D'Amours & Charles Blanchette.
 */
public class RMReport extends ReportConfig {

    public RMReport(Record record, MetadataSchemaTypes type) {
        super(record, type);
    }

    public static final String SCHEMA_LABEL = "label";
    public static final String TYPE_LABEL = "typelabel";
    public static final String COLONNE = "colonne";
    public static final String LIGNE = "ligne";
    public static final String SCHEMA_NAME = ReportConfig.SCHEMA_TYPE + "_" + SCHEMA_LABEL;

    public static String getSchemaLabel() {
        return SCHEMA_LABEL;
    }

    public static String getTypeLabel() {
        return TYPE_LABEL;
    }

    public static String getCOLONNE() {
        return COLONNE;
    }

    public static String getLigne() {
        return LIGNE;
    }
}
