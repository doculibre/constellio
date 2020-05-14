package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.security.roles.Roles;

/**
 * Created by Constellio on 2017-04-26.
 */
public class RMUser extends User {
	public static final String DEFAULT_ADMINISTRATIVE_UNIT = "defaultAdministrativeUnit";
	public static final String FAVORITES_DISPLAY_ORDER = "favoritesDisplayOrder";
	public static final String HIDE_NOT_ACTIVE = "hideNotActive";

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

	public boolean isHideNotActive() {
		return get(HIDE_NOT_ACTIVE);
	}

	public User setHideNotActive(boolean showSemiActive) {
		set(HIDE_NOT_ACTIVE, showSemiActive);
		return this;
	}
}
