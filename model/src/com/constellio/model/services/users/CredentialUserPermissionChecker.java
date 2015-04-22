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
package com.constellio.model.services.users;

import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

public class CredentialUserPermissionChecker {

	List<User> users;

	public CredentialUserPermissionChecker(List<User> users) {
		this.users = users;
	}

	public boolean globalPermissionInAnyCollection(String permission) {
		for (User user : users) {
			if (user.has(permission).globally()) {
				return true;
			}
		}
		return false;
	}

	public boolean anyGlobalPermissionInAnyCollection(List<String> permissions) {
		return anyGlobalPermissionInAnyCollection(permissions.toArray(new String[1]));
	}

	public boolean anyGlobalPermissionInAnyCollection(String... permissions) {
		for (User user : users) {
			if (user.hasAny(permissions).globally()) {
				return true;
			}
		}
		return false;
	}

	public boolean allGlobalPermissionsInAnyCollection(List<String> permissions) {
		return allGlobalPermissionsInAnyCollection(permissions.toArray(new String[1]));
	}

	public boolean allGlobalPermissionsInAnyCollection(String... permissions) {
		for (User user : users) {
			if (user.hasAll(permissions).globally()) {
				return true;
			}
		}
		return false;
	}
}
