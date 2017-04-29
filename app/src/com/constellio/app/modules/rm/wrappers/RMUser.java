package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.security.roles.Roles;

/**
 * Created by Constellio on 2017-04-26.
 */
public class RMUser extends User{
    public static final String DEFAULT_ADMINISTRATIVE_UNIT = "defaultAdministrativeUnit";

    public RMUser(Record record, MetadataSchemaTypes types, Roles roles) {
        super(record, types, roles);
    }

    public String getDefaultAdministrativeUnit() {
        return get(DEFAULT_ADMINISTRATIVE_UNIT);
    }

    public RMUser setDefaultAdministrativeUnit(String administrativeUnit) {
        set(DEFAULT_ADMINISTRATIVE_UNIT, administrativeUnit);
        return this;
    }
}
