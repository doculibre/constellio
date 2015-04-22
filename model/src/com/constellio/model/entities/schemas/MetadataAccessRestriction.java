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
package com.constellio.model.entities.schemas;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MetadataAccessRestriction {

	final List<String> requiredReadRoles;

	final List<String> requiredWriteRoles;

	final List<String> requiredModificationRoles;

	final List<String> requiredDeleteRoles;

	public MetadataAccessRestriction() {
		this.requiredReadRoles = new ArrayList<>();
		this.requiredWriteRoles = new ArrayList<>();
		this.requiredModificationRoles = new ArrayList<>();
		this.requiredDeleteRoles = new ArrayList<>();
	}

	public MetadataAccessRestriction(List<String> requiredReadRoles, List<String> requiredWriteRoles,
			List<String> requiredModificationRoles, List<String> requiredDeleteRoles) {
		this.requiredReadRoles = requiredReadRoles;
		this.requiredWriteRoles = requiredWriteRoles;
		this.requiredModificationRoles = requiredModificationRoles;
		this.requiredDeleteRoles = requiredDeleteRoles;
	}

	public List<String> getRequiredReadRoles() {
		return requiredReadRoles;
	}

	public List<String> getRequiredWriteRoles() {
		return requiredWriteRoles;
	}

	public List<String> getRequiredModificationRoles() {
		return requiredModificationRoles;
	}

	public List<String> getRequiredDeleteRoles() {
		return requiredDeleteRoles;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
