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
package com.constellio.model.entities.security.global;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordUtils;

public class AuthorizationBuilder {

	private String id;
	private String collection;
	private List<String> principals;
	private List<String> targets;

	public AuthorizationBuilder(String collection) {
		this(collection, UUID.randomUUID().toString());
	}

	public AuthorizationBuilder(String collection, String id) {
		this.principals = principals;
		this.collection = collection;
		this.id = id;
	}

	public AuthorizationBuilder forPrincipalsIds(List<String> principals) {
		this.principals = principals;
		return this;
	}

	public AuthorizationBuilder forUsers(String... users) {
		this.principals = asList(users);
		return this;
	}

	public AuthorizationBuilder forUsers(User... users) {
		this.principals = new RecordUtils().toWrappedRecordIdsList(asList(users));
		return this;
	}

	public AuthorizationBuilder forPrincipalsIds(String... principals) {
		this.principals = asList(principals);
		return this;
	}

	public AuthorizationBuilder on(String... targetsWrappersIds) {
		this.targets = Arrays.asList(targetsWrappersIds);
		return this;
	}

	public AuthorizationBuilder on(Record... targetsRecords) {
		this.targets = new RecordUtils().toIdList(asList(targetsRecords));
		return this;
	}

	public AuthorizationBuilder on(RecordWrapper... targetsWrappers) {
		this.targets = new RecordUtils().toWrappedRecordIdsList(asList(targetsWrappers));
		return this;
	}

	private Authorization withRoles(List<String> roles) {
		AuthorizationDetails details = AuthorizationDetails.create(id, roles, null, null, collection, false);
		return new Authorization(details, principals, targets);
	}

	public Authorization givingReadAccess() {
		return withRoles(asList(Role.READ));
	}

	public Authorization givingReadWriteAccess() {
		return withRoles(asList(Role.READ, Role.WRITE));
	}

	public Authorization givingReadWriteDeleteAccess() {
		return withRoles(asList(Role.READ, Role.WRITE, Role.DELETE));
	}

	public Authorization giving(String... roles) {
		return withRoles(asList(roles));
	}

}
