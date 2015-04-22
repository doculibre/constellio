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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class GlobalGroup {

	final String parent;

	final String code;

	final String name;

	final List<String> usersAutomaticallyAddedToCollections;

	final GlobalGroupStatus status;

	public GlobalGroup(String code, String name, List<String> usersAutomaticallyAddedToCollections, String parent,
			GlobalGroupStatus status) {
		this.code = code;
		this.name = name;
		this.usersAutomaticallyAddedToCollections = Collections.unmodifiableList(usersAutomaticallyAddedToCollections);
		this.parent = parent;
		this.status = status;
	}

	public GlobalGroup(String code, String parent, GlobalGroupStatus status) {
		this.code = code;
		this.name = code;
		this.usersAutomaticallyAddedToCollections = Collections.emptyList();
		this.parent = parent;
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public List<String> getUsersAutomaticallyAddedToCollections() {
		return usersAutomaticallyAddedToCollections;
	}

	public String getParent() {
		return parent;
	}

	public GlobalGroupStatus getStatus() {
		return status;
	}

	public GlobalGroup withName(String name) {
		return new GlobalGroup(code, name, usersAutomaticallyAddedToCollections, parent, status);
	}

	public GlobalGroup withUsersAutomaticallyAddedToCollections(List<String> usersAutomaticallyAddedToCollections) {
		return new GlobalGroup(code, name, usersAutomaticallyAddedToCollections, parent, status);
	}


	public GlobalGroup withStatus(GlobalGroupStatus status) {
		return new GlobalGroup(code, name, usersAutomaticallyAddedToCollections, parent, status);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return code;
	}


}
