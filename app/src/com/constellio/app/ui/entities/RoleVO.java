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
package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleVO implements Serializable {
	private String code;
	private String title;
	private final Set<String> permissions;
	private final Set<String> originalPermissions;

	public RoleVO() {
		this(null, null, new ArrayList<String>());
	}

	public RoleVO(String code, String title, List<String> permissions) {
		this.code = code;
		this.title = title;
		this.permissions = new HashSet<>(permissions);
		this.originalPermissions = new HashSet<>(permissions);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getPermissions() {
		return new ArrayList<>(permissions);
	}

	public boolean hasPermission(String permission) {
		return permissions.contains(permission);
	}

	public void addPermission(String permission) {
		permissions.add(permission);
	}

	public void removePermission(String permission) {
		permissions.remove(permission);
	}

	public boolean isDirty() {
		return !permissions.equals(originalPermissions);
	}

	public boolean isPermissionDirty(String permission) {
		return permissions.contains(permission) != originalPermissions.contains(permission);
	}

	public void markClean() {
		originalPermissions.clear();
		originalPermissions.addAll(permissions);
	}
}
