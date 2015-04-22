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

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.security.roles.Roles;

public class RolesUserPermissionsChecker extends UserPermissionsChecker {

	MetadataSchemaTypes types;

	Roles roles;

	boolean anyRoles;

	String[] permissions;

	RolesUserPermissionsChecker(User user, MetadataSchemaTypes types, Roles roles) {
		super(user);
		this.types = types;
		this.roles = roles;
	}

	public boolean globally() {
		if (user.isSystemAdmin()) {
			return true;
		} else if (anyRoles) {
			return roles.hasAny(user, permissions);
		} else {
			return roles.hasAll(user, permissions);
		}
	}

	private Set<String> getUserPermissionsOnRecord(Record record) {
		Set<String> permissions = new HashSet<>();
		List<String> tokens = record.getList(Schemas.TOKENS);
		List<String> userTokens = user.getUserTokens();
		for (String token : tokens) {

			if (userTokens.contains(token)) {
				for (String authorizationRoleCode : token.split("_")[1].split(",")) {
					Role role = roles.getRole(authorizationRoleCode);
					if (role != null) {
						permissions.addAll(role.getOperationPermissions());
					}
				}
			}
		}
		for (String userRoleCode : user.getAllRoles()) {
			Role role = roles.getRole(userRoleCode);
			permissions.addAll(role.getOperationPermissions());
		}
		return permissions;
	}

	public boolean on(Record record) {
		if (user.isSystemAdmin()) {
			return true;
		}
		Set<String> userPermissionsOnRecord = getUserPermissionsOnRecord(record);

		if (anyRoles) {
			return LangUtils.containsAny(asList(permissions), userPermissionsOnRecord);
		} else {
			return userPermissionsOnRecord.containsAll(asList(permissions));
		}

	}

}
