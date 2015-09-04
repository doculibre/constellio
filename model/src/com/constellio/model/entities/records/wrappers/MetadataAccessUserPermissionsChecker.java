/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
