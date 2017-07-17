package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;


public class ContainerForm extends Printable {

    public static final String SCHEMA_CONTAINER = "containerForm";
    public static final String SCHEMA_NAME = Printable.SCHEMA_TYPE + "_" + SCHEMA_CONTAINER;

    public ContainerForm(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public String getSchemaName() {
        return SCHEMA_NAME;
    }
}
