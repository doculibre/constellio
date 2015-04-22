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
package com.constellio.app.api.cmis.builders.objectType;

import org.apache.chemistry.opencmis.commons.definitions.MutablePropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.Updatability;

public class PropertyBuilder {

	MutablePropertyDefinition<?> propertyDefinition;

	public PropertyBuilder(MutablePropertyDefinition<?> propertyDefinition, String id) {
		this.propertyDefinition = propertyDefinition;
		propertyDefinition.setId(id);
	}

	public PropertyBuilder setName(String name) {
		this.propertyDefinition.setQueryName(name);
		this.propertyDefinition.setDisplayName(name);
		this.propertyDefinition.setLocalName(name);
		return this;
	}

	public PropertyBuilder setMultivalue(boolean multivalue) {
		this.propertyDefinition.setCardinality(multivalue ? Cardinality.MULTI : Cardinality.SINGLE);
		return this;
	}

	public PropertyBuilder setUpdatability(boolean updatability) {
		this.propertyDefinition.setUpdatability(updatability ? Updatability.READWRITE : Updatability.READONLY);
		return this;
	}

	public PropertyBuilder setRequired(boolean isRequired) {
		this.propertyDefinition.setIsRequired(isRequired);
		return this;
	}

	public MutablePropertyDefinition<?> build() {
		return propertyDefinition;
	}
}
