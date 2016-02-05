package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.security.roles.Roles;

public class MetadataAccessUserPermissionsChecker extends UserPermissionsChecker {

	MetadataSchemaTypes types;

	Roles roles;

	public boolean metadataRead;
	public boolean metadataWrite;
	public boolean metadataModification;
	public boolean metadataDelete;

	MetadataAccessUserPermissionsChecker(User user, MetadataSchemaTypes types, Roles roles) {
		super(user);
		this.types = types;
		this.roles = roles;
		this.user = user;
	}

	public boolean globally() {
		return true;
	}

	public boolean on(Record record) {
		return true;
	}

	@Override
	public boolean onSomething() {
		throw new UnsupportedOperationException("onSomething() is not yet supported for this checker");
	}

}
