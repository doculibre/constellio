package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Marco on 2017-02-13.
 */
public class RMUserFolder extends UserFolder {

    public static final String RETENTION_RULE = "retentionRule";

    public static final String CATEGORY = "category";

    public static final String ADMINISTRATIVE_UNIT = "administrativeUnit";

    public RMUserFolder(Record record, MetadataSchemaTypes types, String typeRequirement) {
        super(record, types, typeRequirement);
    }

    public RetentionRule getRetentionRule() {
        return get(RETENTION_RULE);
    }

    public UserFolder setRetentionRule(RetentionRule retentionRule) {
        set(RETENTION_RULE, retentionRule);
        return this;
    }

    public Category getCategory() {
        return get(CATEGORY);
    }

    public UserFolder setCategory(Category category) {
        set(CATEGORY, category);
        return this;
    }

    public AdministrativeUnit getAdministrativeUnit() {
        return get(ADMINISTRATIVE_UNIT);
    }

    public UserFolder setAdministrativeUnit(AdministrativeUnit administrativeUnit) {
        set(ADMINISTRATIVE_UNIT, administrativeUnit);
        return this;
    }
}
