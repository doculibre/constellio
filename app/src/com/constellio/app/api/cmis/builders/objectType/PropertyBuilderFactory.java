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

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.DataEntryType;

public class PropertyBuilderFactory {

	public static PropertyBuilder newStringProperty(String id) {
		PropertyStringDefinitionImpl property = new PropertyStringDefinitionImpl();
		property.setPropertyType(PropertyType.STRING);
		return new PropertyBuilder(property, id);
	}

	public static PropertyBuilder newBooleanProperty(String id) {
		PropertyBooleanDefinitionImpl property = new PropertyBooleanDefinitionImpl();
		property.setPropertyType(PropertyType.BOOLEAN);
		return new PropertyBuilder(property, id);
	}

	public static PropertyBuilder newNumberProperty(String id) {
		PropertyBooleanDefinitionImpl property = new PropertyBooleanDefinitionImpl();
		property.setPropertyType(PropertyType.INTEGER);
		return new PropertyBuilder(property, id);
	}

	public static PropertyBuilder newDateProperty(String id) {
		PropertyDateTimeDefinitionImpl property = new PropertyDateTimeDefinitionImpl();
		property.setPropertyType(PropertyType.DATETIME);
		return new PropertyBuilder(property, id);
	}

	public static PropertyDefinition<?> getPropertyFor(Metadata metadata) {
		String id = metadata.getCode();
		String displayName = metadata.getCode();
		PropertyType datatype;
		if (metadata.getType() == MetadataValueType.DATE_TIME || metadata.getType() == MetadataValueType.DATE) {
			datatype = PropertyType.DATETIME;
		} else if (metadata.getType() == MetadataValueType.NUMBER) {
			datatype = PropertyType.INTEGER;
		} else if (metadata.getType() == MetadataValueType.BOOLEAN) {
			datatype = PropertyType.BOOLEAN;
		} else {
			datatype = PropertyType.STRING;
		}
		Cardinality cardinality = metadata.isMultivalue() ? Cardinality.MULTI : Cardinality.SINGLE;
		Updatability updateability = (metadata.isUnmodifiable() || metadata.getDataEntry().getType() != DataEntryType.MANUAL) ?
				Updatability.READONLY :
				Updatability.READWRITE;
		boolean inherited = metadata.inheritDefaultSchema();
		boolean required = metadata.isDefaultRequirement();

		PropertyDefinition<?> propertyDefinition = createPropertiesDefinition(id, displayName, datatype, cardinality,
				updateability, inherited, required);
		return propertyDefinition;
	}

	private static PropertyDefinition<?> createPropertiesDefinition(String id, String displayName, PropertyType datatype,
			Cardinality cardinality, Updatability updateability, boolean inherited, boolean required) {
		AbstractPropertyDefinition<?> propertyDefinition = null;

		if (datatype == PropertyType.DATETIME) {
			propertyDefinition = new PropertyDateTimeDefinitionImpl();
		} else if (datatype == PropertyType.INTEGER) {
			propertyDefinition = new PropertyIntegerDefinitionImpl();
		} else if (datatype == PropertyType.BOOLEAN) {
			propertyDefinition = new PropertyBooleanDefinitionImpl();
		} else {
			propertyDefinition = new PropertyStringDefinitionImpl();
		}

		propertyDefinition.setId(id);
		propertyDefinition.setLocalName(id);
		propertyDefinition.setDisplayName(displayName);
		propertyDefinition.setLocalName(displayName);
		propertyDefinition.setDescription(displayName);
		propertyDefinition.setPropertyType(datatype);
		propertyDefinition.setCardinality(cardinality);
		propertyDefinition.setUpdatability(updateability);
		propertyDefinition.setIsInherited(inherited);
		propertyDefinition.setIsRequired(required);
		propertyDefinition.setIsQueryable(false);
		propertyDefinition.setIsOrderable(false);
		propertyDefinition.setQueryName(id);

		return propertyDefinition;
	}
}
