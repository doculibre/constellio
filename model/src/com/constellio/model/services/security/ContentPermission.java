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
package com.constellio.model.services.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class ContentPermission implements Serializable {

	private final String code;

	private final Set<String> dependencies;

	ContentPermission(String code, ContentPermission... dependentContentPermissions) {
		this.code = code;

		Set<String> dependencies = new HashSet<>();
		for (ContentPermission contentPermission : dependentContentPermissions) {
			dependencies.add(contentPermission.getCode());
			dependencies.addAll(contentPermission.getDependencies());
		}
		this.dependencies = Collections.unmodifiableSet(dependencies);
	}

	public String getCode() {
		return code;
	}

	public Set<String> getDependencies() {
		return dependencies;
	}

	@Override
	public String toString() {
		return code + " including " + dependencies;
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
