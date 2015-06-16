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
package com.constellio.model.services.schemas.xml;

import static com.constellio.data.dao.services.XMLElementUtils.newElementWithContent;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.utils.ParametrizedInstanceUtils;

public class MetadataSchemaXMLWriter {

	public void writeEmptyDocument(String collection, Document document) {
		writeSchemaTypes(new MetadataSchemaTypes(collection, 0, new ArrayList<MetadataSchemaType>(), new ArrayList<String>(),
				new ArrayList<String>()), document);
	}

	public Document write(MetadataSchemaTypes schemaTypes) {
		Document document = new Document();
		writeSchemaTypes(schemaTypes, document);
		return document;
	}

	private void writeCustomSchemas(MetadataSchemaType schemaType, Element schemaTypeElement) {
		Element customSchemasElement = new Element("customSchemas");
		for (MetadataSchema schema : schemaType.getSchemas()) {
			Element schemaElement = toXMLElement(schema);
			customSchemasElement.addContent(schemaElement);
		}
		schemaTypeElement.addContent(customSchemasElement);
	}

	private void writeDefaultSchema(MetadataSchemaType schemaType, Element schemaTypeElement) {
		MetadataSchema defaultSchema = schemaType.getDefaultSchema();
		Element defaultSchemaElement = new Element("defaultSchema");
		defaultSchemaElement.addContent(newElementWithContent("code", defaultSchema.getLocalCode()));
		defaultSchemaElement.addContent(newElementWithContent("label", defaultSchema.getLabel()));
		for (Metadata metadata : defaultSchema.getMetadatas()) {
			addMetadataToSchema(defaultSchemaElement, metadata);
		}
		if (!defaultSchema.getValidators().isEmpty()) {
			defaultSchemaElement.addContent(writeSchemaValidators(defaultSchema));
		}
		schemaTypeElement.addContent(defaultSchemaElement);
	}

	private Element writeSchemaValidators(MetadataSchema defaultSchema) {
		Element validatorsElement = new Element("validators");
		for (RecordValidator validator : defaultSchema.getValidators()) {
			validatorsElement.addContent(new Element("validator").setText(validator.getClass().getName()));
		}
		return validatorsElement;
	}

	private Element writeSchemaTypes(MetadataSchemaTypes schemaTypes, Document document) {
		Element schemaTypesElement = new Element("schemaTypes");
		document.setRootElement(schemaTypesElement);
		schemaTypesElement.setAttribute("version", "" + schemaTypes.getVersion());
		for (MetadataSchemaType schemaType : schemaTypes.getSchemaTypes()) {
			schemaTypesElement.addContent(writeSchemaType(schemaType));
		}

		return schemaTypesElement;
	}

	private Element writeSchemaType(MetadataSchemaType schemaType) {
		Element schemaTypeElement = new Element("schemaType");
		schemaTypeElement.addContent(newElementWithContent("code", schemaType.getCode()));
		schemaTypeElement.addContent(newElementWithContent("label", schemaType.getLabel()));
		schemaTypeElement.addContent(newElementWithContent("security", schemaType.hasSecurity()));
		writeDefaultSchema(schemaType, schemaTypeElement);
		writeCustomSchemas(schemaType, schemaTypeElement);
		return schemaTypeElement;
	}

	private Element toXMLElement(MetadataSchema schema) {
		Element schemaElement = new Element("schema");
		schemaElement.addContent(newElementWithContent("code", schema.getLocalCode()));
		schemaElement.addContent(newElementWithContent("label", schema.getLabel()));
		schemaElement.addContent(newElementWithContent("undeletable", schema.isUndeletable()));
		for (Metadata metadata : schema.getMetadatas()) {
			addMetadataToSchema(schemaElement, metadata);
		}
		if (!schema.getValidators().isEmpty()) {
			schemaElement.addContent(writeSchemaValidators(schema));
		}
		return schemaElement;
	}

	private void addMetadataToSchema(Element schemaElement, Metadata metadata) {
		ParametrizedInstanceUtils utils = new ParametrizedInstanceUtils();
		Element metadataElement = new Element("metadata");
		metadataElement.addContent(newElementWithContent("code", metadata.getLocalCode()));
		metadataElement.addContent(newElementWithContent("label", metadata.getLabel()));
		metadataElement.addContent(newElementWithContent("enabled", metadata.isEnabled()));
		metadataElement.addContent(newElementWithContent("undeletable", metadata.isUndeletable()));
		metadataElement.addContent(newElementWithContent("multivalue", metadata.isMultivalue()));
		metadataElement.addContent(newElementWithContent("searchable", metadata.isSearchable()));
		metadataElement.addContent(newElementWithContent("sortable", metadata.isSortable()));
		metadataElement.addContent(newElementWithContent("schemaAutocomplete", metadata.isSchemaAutocomplete()));
		metadataElement.addContent(newElementWithContent("systemReserved", metadata.isSystemReserved()));
		metadataElement.addContent(newElementWithContent("essential", metadata.isEssential()));
		metadataElement.addContent(newElementWithContent("childOfRelationship", metadata.isChildOfRelationship()));
		metadataElement.addContent(newElementWithContent("taxonomyRelationship", metadata.isTaxonomyRelationship()));
		metadataElement.addContent(newElementWithContent("uniqueValue", metadata.isUniqueValue()));
		metadataElement.addContent(newElementWithContent("unmodifiable", metadata.isUnmodifiable()));
		metadataElement.addContent(newElementWithContent("type", metadata.getType().name()));
		metadataElement.addContent(newElementWithContent("defaultRequirement", metadata.isDefaultRequirement()));
		metadataElement.addContent(newElementWithContent("inheriting", metadata.inheritDefaultSchema()));
		metadataElement.addContent(toStructureFactoryElement(metadata.getStructureFactory()));
		metadataElement.addContent(toEnumClassFactoryElement(metadata.getEnumClass()));
		metadataElement.addContent(toAccessRestrictionsElement(metadata.getAccessRestrictions()));
		metadataElement.addContent(toRefencesElement(metadata.getAllowedReferences()));
		if (metadata.getDefaultValue() != null) {
			utils.toElement(metadata.getDefaultValue(), metadataElement, "defaultValue");
		} else {
			utils.toElement("null", metadataElement, "defaultValue");
		}

		if (!metadata.inheritDefaultSchema()) {
			metadataElement.addContent(toDataEntryElement(metadata.getDataEntry()));
		}
		if (!metadata.getValidators().isEmpty()) {
			metadataElement.addContent(writeRecordMetadataValidators(metadata));
		}
		schemaElement.addContent(metadataElement);
	}

	private Element toStructureFactoryElement(StructureFactory structureFactory) {
		Element structureFactoryElement = new Element("structureFactory");
		if (structureFactory != null) {
			structureFactoryElement.setText(structureFactory.getClass().getName());
		}

		return structureFactoryElement;
	}

	private Element toEnumClassFactoryElement(Class<? extends Enum<?>> enumClass) {
		Element enumClassElement = new Element("enumClass");
		if (enumClass != null) {
			enumClassElement.setText(enumClass.getName());
		}

		return enumClassElement;
	}

	private Element toAccessRestrictionsElement(MetadataAccessRestriction accessRestrictions) {
		Element accessRestrictionsElement = new Element("accessRestrictions");
		Element readAccessRestrictionsElement = new Element("readAccessRestrictions");
		Element writeAccessRestrictionsElement = new Element("writeAccessRestrictions");
		Element deleteAccessRestrictionsElement = new Element("deleteAccessRestrictions");
		Element modifyAccessRestrictionsElement = new Element("modifyAccessRestrictions");

		toAccessRestrictionsListElement(readAccessRestrictionsElement, accessRestrictions.getRequiredReadRoles());
		toAccessRestrictionsListElement(writeAccessRestrictionsElement, accessRestrictions.getRequiredWriteRoles());
		toAccessRestrictionsListElement(deleteAccessRestrictionsElement, accessRestrictions.getRequiredDeleteRoles());
		toAccessRestrictionsListElement(modifyAccessRestrictionsElement, accessRestrictions.getRequiredModificationRoles());

		accessRestrictionsElement.addContent(readAccessRestrictionsElement);
		accessRestrictionsElement.addContent(writeAccessRestrictionsElement);
		accessRestrictionsElement.addContent(deleteAccessRestrictionsElement);
		accessRestrictionsElement.addContent(modifyAccessRestrictionsElement);
		return accessRestrictionsElement;
	}

	private void toAccessRestrictionsListElement(Element element, List<String> roles) {
		for (String role : roles) {
			element.addContent(newElementWithContent("role", role));
		}
	}

	private Element writeRecordMetadataValidators(Metadata metadata) {
		Element validatorsElement = new Element("validators");
		for (RecordMetadataValidator<?> validator : metadata.getValidators()) {
			validatorsElement.addContent(new Element("validator").setText(validator.getClass().getName()));
		}
		return validatorsElement;
	}

	private Element toRefencesElement(AllowedReferences allowedReferences) {
		Element references = new Element("references");

		if (allowedReferences != null) {

			Element schemaTypes = new Element("schemaTypes");
			references.addContent(schemaTypes);

			Element schemas = new Element("schemas");
			references.addContent(schemas);

			schemaTypes.addContent(newElementWithContent("schemaType", allowedReferences.getAllowedSchemaType()));

			for (String schemaCode : allowedReferences.getAllowedSchemas()) {
				schemas.addContent(newElementWithContent("schema", schemaCode));
			}

		}

		return references;
	}

	private Element toDataEntryElement(DataEntry dataEntryValue) {
		Element dataEntry = new Element("dataEntry");

		dataEntry.addContent(newElementWithContent("dataEntryType", dataEntryValue.getType().toString()));

		if (dataEntryValue.getType() == DataEntryType.COPIED) {
			CopiedDataEntry copiedDataEntry = (CopiedDataEntry) dataEntryValue;
			dataEntry.addContent(newElementWithContent("copiedMetadata", copiedDataEntry.getCopiedMetadata()));
			dataEntry.addContent(newElementWithContent("referenceMetadata", copiedDataEntry.getReferenceMetadata()));

		} else if (dataEntryValue.getType() == DataEntryType.CALCULATED) {
			CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) dataEntryValue;
			dataEntry.addContent(newElementWithContent("calculator", calculatedDataEntry.getCalculator().getClass().getName()));
		}

		return dataEntry;
	}
}
