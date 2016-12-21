package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by Constellio on 2016-12-21.
 */
public class SolrAuthorizationDetails extends RecordWrapper implements AuthorizationDetails {
    public static final String SCHEMA_TYPE = "autorizationDetail";
    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
    public static final String IDENTIFIER = "identifier";
    public static final String ROLES = "roles";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String SYNCED = "synced";

    public SolrAuthorizationDetails(Record record,
                           MetadataSchemaTypes types) {
        super(record, types, SCHEMA_TYPE);
    }

    public SolrAuthorizationDetails setTitle(String title) {
        super.setTitle(title);
        return this;
    }

    public String getIdentifier() {
        return get(IDENTIFIER);
    }

    public SolrAuthorizationDetails setIdentifier(String identifier) {
        set(IDENTIFIER, identifier);
        return this;
    }

    @Override
    public List<String> getRoles() {
        return get(ROLES);
    }

    @Override
    public LocalDate getStartDate() {
        return get(START_DATE);
    }

    @Override
    public LocalDate getEndDate() {
        return get(END_DATE);
    }

    @Override
    public boolean isSynced() {
        return Boolean.TRUE.equals(get(SYNCED));
    }
}
