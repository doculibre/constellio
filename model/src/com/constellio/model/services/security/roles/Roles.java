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
package com.constellio.model.services.security.roles;

import java.util.List;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;

public class Roles {

	List<Role> roles;

	public Roles(List<Role> roles) {
		this.roles = roles;
	}

	public boolean has(User user, String permission) {
		for (String userRole : user.getAllRoles()) {
			Role role = getRole(userRole);
			if (role.hasOperationPermission(permission)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasAll(User user, String... permissions) {

		for (String permission : permissions) {
			boolean hasPermission = has(user, permission);
			if (!hasPermission) {
				return false;
			}
		}
		return true;
	}

	public boolean hasAny(User user, String... permissions) {

		for (String permission : permissions) {
			boolean hasPermission = has(user, permission);
			if (hasPermission) {
				return true;
			}
		}
		return false;
	}

	public Role getRole(String code) {
		for (Role role : roles) {
			if (role.getCode().equals(code)) {
				return role;
			}
		}
		return null;
	}

}
