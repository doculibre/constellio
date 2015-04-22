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
package com.constellio.app.client.entities;

import java.util.List;

import com.constellio.model.entities.security.global.GlobalGroupStatus;

public class GlobalGroupResource {

	private String code;

	private String name;

	private List<String> usersAutomaticallyAddedToCollections;

	private String parent;

	private GlobalGroupStatus status = GlobalGroupStatus.ACTIVE;

	public GlobalGroupResource() {
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getUsersAutomaticallyAddedToCollections() {
		return usersAutomaticallyAddedToCollections;
	}

	public void setUsersAutomaticallyAddedToCollections(List<String> usersAutomaticallyAddedToCollections) {
		this.usersAutomaticallyAddedToCollections = usersAutomaticallyAddedToCollections;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getParent() {
		return parent;
	}

	public GlobalGroupStatus getStatus() {
		return status;
	}

	public void setStatus(GlobalGroupStatus status) {
		this.status = status;
	}
}
