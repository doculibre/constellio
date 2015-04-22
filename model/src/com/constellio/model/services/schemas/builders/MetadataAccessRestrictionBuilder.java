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
package com.constellio.model.services.schemas.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.schemas.MetadataAccessRestriction;

public class MetadataAccessRestrictionBuilder {

	List<String> requiredReadRoles;

	List<String> requiredWriteRoles;

	List<String> requiredModificationRoles;

	List<String> requiredDeleteRoles;

	public static MetadataAccessRestrictionBuilder modify(MetadataAccessRestriction accessRestriction) {
		MetadataAccessRestrictionBuilder builder = new MetadataAccessRestrictionBuilder();
		builder.requiredReadRoles = accessRestriction.getRequiredReadRoles();
		builder.requiredWriteRoles = accessRestriction.getRequiredWriteRoles();
		builder.requiredModificationRoles = accessRestriction.getRequiredModificationRoles();
		builder.requiredDeleteRoles = accessRestriction.getRequiredDeleteRoles();
		return builder;
	}

	public static MetadataAccessRestrictionBuilder create() {
		return modify(new MetadataAccessRestriction());
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

	public MetadataAccessRestrictionBuilder withRequiredReadRole(String role) {
		requiredReadRoles.add(role);
		return this;
	}

	public MetadataAccessRestrictionBuilder withRequiredWriteRole(String role) {
		requiredWriteRoles.add(role);
		return this;
	}

	public MetadataAccessRestrictionBuilder withRequiredModificationRole(String role) {
		requiredModificationRoles.add(role);
		return this;
	}

	public MetadataAccessRestrictionBuilder withRequiredDeleteRole(String role) {
		requiredDeleteRoles.add(role);
		return this;
	}

	public MetadataAccessRestrictionBuilder withRequiredWriteAndDeleteRole(String role) {
		requiredWriteRoles.add(role);
		requiredDeleteRoles.add(role);
		return this;
	}

	public MetadataAccessRestrictionBuilder withRequiredRole(String role) {
		requiredReadRoles.add(role);
		requiredWriteRoles.add(role);
		requiredModificationRoles.add(role);
		requiredDeleteRoles.add(role);
		return this;
	}

	public MetadataAccessRestriction build() {
		List<String> unmodifiableReadRequiredRoles = Collections.unmodifiableList(withoutDuplicates(requiredReadRoles));
		List<String> unmodifiableWriteRequiredRoles = Collections.unmodifiableList(withoutDuplicates(requiredWriteRoles));
		List<String> unmodifiableModificationRequiredRoles = Collections
				.unmodifiableList(withoutDuplicates(requiredModificationRoles));
		List<String> unmodifiableDeleteRequiredRoles = Collections.unmodifiableList(withoutDuplicates(requiredDeleteRoles));
		return new MetadataAccessRestriction(unmodifiableReadRequiredRoles, unmodifiableWriteRequiredRoles,
				unmodifiableModificationRequiredRoles, unmodifiableDeleteRequiredRoles);
	}

	private List<String> withoutDuplicates(List<String> elements) {
		List<String> withoutDuplicates = new ArrayList<>();
		for (String element : elements) {
			if (!withoutDuplicates.contains(element)) {
				withoutDuplicates.add(element);
			}
		}
		return withoutDuplicates;
	}

}
