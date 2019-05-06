package com.constellio.app.api.cmis.builders.objectType;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;

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
		String metadataCode = metadata.getCode();
		String metadataLocalCode = new SchemaUtils().getLocalCodeFromMetadataCode(metadataCode);
		String id = metadataLocalCode;
		String displayName = metadataLocalCode;
		PropertyType datatype;
		if (metadata.getType() == MetadataValueType.DATE_TIME || metadata.getType() == MetadataValueType.DATE) {
			datatype = PropertyType.DATETIME;
		} else if (metadata.getType() == MetadataValueType.NUMBER) {
			datatype = PropertyType.DECIMAL;
		} else if (metadata.getType() == MetadataValueType.BOOLEAN) {
			datatype = PropertyType.BOOLEAN;
		} else {
			datatype = PropertyType.STRING;
		}

		boolean updatable = false;
		if (!metadata.isUnmodifiable() && metadata.getDataEntry().getType() == DataEntryType.MANUAL) {
			updatable = true;
		}
		if (!metadata.isUnmodifiable() && metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
			updatable = ((CalculatedDataEntry) metadata.getDataEntry()).getCalculator().hasEvaluator();
		}

		Cardinality cardinality = metadata.isMultivalue() ? Cardinality.MULTI : Cardinality.SINGLE;
		Updatability updateability = (updatable) ? Updatability.READWRITE : Updatability.READONLY;
		boolean inherited = metadata.inheritDefaultSchema();
		boolean required = metadata.isDefaultRequirement();

		PropertyDefinition<?> propertyDefinition = createPropertiesDefinition(id, displayName, datatype, cardinality,
				updateability, inherited, required);
		return propertyDefinition;
	}

	private static PropertyDefinition<?> createPropertiesDefinition(String id, String displayName,
																	PropertyType datatype,
																	Cardinality cardinality, Updatability updateability,
																	boolean inherited, boolean required) {
		AbstractPropertyDefinition<?> propertyDefinition = null;

		if (datatype == PropertyType.DATETIME) {
			propertyDefinition = new PropertyDateTimeDefinitionImpl();
		} else if (datatype == PropertyType.INTEGER) {
			propertyDefinition = new PropertyIntegerDefinitionImpl();
		} else if (datatype == PropertyType.DECIMAL) {
			propertyDefinition = new PropertyDecimalDefinitionImpl();
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
