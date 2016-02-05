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
