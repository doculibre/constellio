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
package com.constellio.model.entities.security;

import java.util.Collections;
import java.util.List;

public class Role {

	private final String code;

	private final String title;

	private final String collection;

	private final List<String> operationPermissions;

	public static final String READ = "READ";

	public static final Role READ_ROLE = new Role(READ);

	public static final String WRITE = "WRITE";

	public static final Role WRITE_ROLE = new Role(WRITE);

	public static final String DELETE = "DELETE";

	public static final Role DELETE_ROLE = new Role(DELETE);

	public Role(String collection, String code, String title, List<String> operationPermissions) {
		this.collection = collection;
		this.code = code;
		this.title = title;
		this.operationPermissions = Collections.unmodifiableList(operationPermissions);
	}

	private Role(String code) {
		this.collection = null;
		this.code = code;
		this.title = code;
		this.operationPermissions = Collections.emptyList();
		//this.contentPermissions = Arrays.asList(contentPermissions);
	}

	public String getCode() {
		return code;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getOperationPermissions() {
		return operationPermissions;
	}

	public String getCollection() {
		return collection;
	}

	public boolean isContentPermissionRole() {
		return operationPermissions.isEmpty();
	}

	public boolean hasOperationPermission(String wantedOperationPermission) {
		return operationPermissions.contains(wantedOperationPermission);
	}

	public Role withTitle(String title) {
		return new Role(collection, code, title, operationPermissions);
	}

	public Role withPermissions(List<String> operationPermissions) {
		return new Role(collection, code, title, operationPermissions);
	}
}
