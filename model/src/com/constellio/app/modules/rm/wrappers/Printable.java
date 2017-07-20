package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentImpl;

/**
 * Classe Wrapper pour les Rapports.
 *
 * @author Nicolas D'Amours & Charles Blanchette.
 */
public class Printable extends RecordWrapper {
    public static final String SCHEMA_TYPE = "printable";
    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
    public static final String JASPERFILE = "jasperfile";
    public static final String ISDELETABLE = "isdeletable";

    public Printable(Record record, MetadataSchemaTypes types) {
        super(record, types, SCHEMA_TYPE);
    }

    public Printable setTitle(String title) {
        super.setTitle(title);
        return this;
    }

    public Printable setJasperFile(Content file) {
        set(JASPERFILE, file);
        return this;
    }

    public ContentImpl getJasperfile() {
        return get(JASPERFILE);
    }

    public Printable setIsDeletable(Boolean isDeletable) {
        set(ISDELETABLE, isDeletable);
        return this;
    }
}
