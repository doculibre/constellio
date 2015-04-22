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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.schemas.MetadataSchemasManagerRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataAccessRestrictionBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.InstanciationUtils;

public class MetadataSchemaXMLReader {

	public MetadataSchemaTypesBuilder read(String collection, Document document, DataStoreTypesFactory typesFactory,
			TaxonomiesManager taxonomiesManager) {
		Element rootElement = document.getRootElement();
		int version = Integer.valueOf(rootElement.getAttributeValue("version")) - 1;
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.createWithVersion(collection, version);

		for (Element schemaTypeElement : rootElement.getChildren("schemaType")) {
			parseProfilType(typesBuilder, schemaTypeElement, typesFactory, taxonomiesManager);
		}
		return typesBuilder;
	}

	private MetadataSchemaType parseProfilType(MetadataSchemaTypesBuilder typesBuilder, Element element,
			DataStoreTypesFactory typesFactory, TaxonomiesManager taxonomiesManager) {
		MetadataSchemaTypeBuilder schemaTypeBuilder = typesBuilder.createNewSchemaType(getCodeValue(element), false).setLabel(
				getLabelValue(element));
		schemaTypeBuilder.setSecurity(getBooleanFlagValue(element, "security"));
		parseDefaultSchema(element, schemaTypeBuilder);
		parseCustomSchemas(element, schemaTypeBuilder);
		return schemaTypeBuilder.build(typesFactory, taxonomiesManager);
	}

	private void parseCustomSchemas(Element root, MetadataSchemaTypeBuilder schemaTypeBuilder) {
		Element customSchemasElement = root.getChild("customSchemas");
		for (Element schemaElement : customSchemasElement.getChildren("schema")) {
			parseSchema(schemaTypeBuilder, schemaElement);
		}
	}

	private void parseSchema(MetadataSchemaTypeBuilder schemaTypeBuilder, Element schemaElement) {
		MetadataSchemaBuilder schemaBuilder = schemaTypeBuilder.createCustomSchema(getCodeValue(schemaElement));
		schemaBuilder.setLabel(getLabelValue(schemaElement));
		schemaBuilder.setUndeletable(getBooleanFlagValue(schemaElement, "undeletable"));
		for (Element metadataElement : schemaElement.getChildren("metadata")) {
			parseMetadata(schemaBuilder, metadataElement);
		}
		List<Element> validatorElements = parseValidators(schemaElement);
		if (!validatorElements.isEmpty()) {
			for (Element validatorElement : validatorElements) {
				schemaBuilder.defineValidators().add(parseSchemaValidator(validatorElement));
			}
		}
	}

	private void parseMetadata(MetadataSchemaBuilder schemaBuilder, Element metadataElement) {
		String codeValue = getCodeValue(metadataElement);

		MetadataBuilder metadataBuilder;
		try {
			metadataBuilder = schemaBuilder.get(codeValue);
		} catch (MetadataSchemaBuilderRuntimeException.NoSuchMetadata e) {
			metadataBuilder = schemaBuilder.create(codeValue);
		}

		metadataBuilder.setLabel(getLabelValue(metadataElement));
		if (!metadataBuilder.isSystemReserved()) {
			metadataBuilder.setEnabled(getBooleanFlagValue(metadataElement, "enabled"));
			metadataBuilder.setDefaultRequirement(getBooleanFlagValue(metadataElement, "defaultRequirement"));
		}

		List<Element> validatorElements = parseValidators(metadataElement);
		if (!validatorElements.isEmpty()) {
			for (Element validatorElement : validatorElements) {
				metadataBuilder.defineValidators().add(parseRecordMetadataValidator(validatorElement));
			}
		}

		parseDataEntryElement(metadataBuilder, metadataElement);
		if (!isInheriting(metadataElement)) {
			if (metadataBuilder.getType() == null) {
				metadataBuilder.setType(getTypeValue(metadataElement));
			}
			metadataBuilder.setUndeletable(getBooleanFlagValue(metadataElement, "undeletable"));
			metadataBuilder.setSystemReserved(getBooleanFlagValue(metadataElement, "systemReserved"));
			metadataBuilder.setUnmodifiable(getBooleanFlagValue(metadataElement, "unmodifiable"));
			metadataBuilder.setSearchable(getBooleanFlagValue(metadataElement, "searchable"));
			metadataBuilder.setSortable(getBooleanFlagValue(metadataElement, "sortable"));
			metadataBuilder.setSchemaAutocomplete(getBooleanFlagValue(metadataElement, "schemaAutocomplete"));
			metadataBuilder.setUniqueValue(getBooleanFlagValue(metadataElement, "uniqueValue"));
			metadataBuilder.setChildOfRelationship(getBooleanFlagValue(metadataElement, "childOfRelationship"));
			metadataBuilder.setTaxonomyRelationship(getBooleanFlagValue(metadataElement, "taxonomyRelationship"));
			metadataBuilder.setMultivalue(getBooleanFlagValue(metadataElement, "multivalue"));
		}

		Class<StructureFactory> structureFactoryClass = getClassValue(metadataElement, "structureFactory");
		if (structureFactoryClass != null) {
			metadataBuilder.defineStructureFactory(structureFactoryClass);
		}

		Class<? extends Enum<?>> enumClass = getClassValue(metadataElement, "enumClass");
		if (enumClass != null) {
			metadataBuilder.defineAsEnum(enumClass);
		}

		if (!isInheriting(metadataElement)) {
			setAccessRestrictions(metadataBuilder.defineAccessRestrictions(), metadataElement);
		}

		addReferencesToBuilder(metadataBuilder, metadataElement);
	}

	private void setAccessRestrictions(MetadataAccessRestrictionBuilder metadataAccessRestrictionBuilder,
			Element metadataElement) {
		Element accessRestrictionsElement = metadataElement.getChild("accessRestrictions");
		Element readAccessRestrictionsElement = accessRestrictionsElement.getChild("readAccessRestrictions");
		Element writeAccessRestrictionsElement = accessRestrictionsElement.getChild("writeAccessRestrictions");
		Element deleteAccessRestrictionsElement = accessRestrictionsElement.getChild("deleteAccessRestrictions");
		Element modifyAccessRestrictionsElement = accessRestrictionsElement.getChild("modifyAccessRestrictions");

		addAccessRestrictionsRoles(readAccessRestrictionsElement, metadataAccessRestrictionBuilder.getRequiredReadRoles());
		addAccessRestrictionsRoles(writeAccessRestrictionsElement, metadataAccessRestrictionBuilder.getRequiredWriteRoles());
		addAccessRestrictionsRoles(deleteAccessRestrictionsElement, metadataAccessRestrictionBuilder.getRequiredDeleteRoles());
		addAccessRestrictionsRoles(modifyAccessRestrictionsElement,
				metadataAccessRestrictionBuilder.getRequiredModificationRoles());

	}

	private void addAccessRestrictionsRoles(Element rolesListElement, List<String> rolesList) {
		for (Element roleElement : rolesListElement.getChildren("role")) {
			rolesList.add(roleElement.getText());
		}
	}

	@SuppressWarnings("unchecked")
	private Class<? extends RecordMetadataValidator<?>> parseRecordMetadataValidator(Element validatorElement) {
		try {
			return (Class<? extends RecordMetadataValidator<?>>) Class.forName(validatorElement.getText());
		} catch (ClassNotFoundException e) {
			throw new MetadataSchemasManagerRuntimeException.NoSuchValidatorClass(validatorElement.getText(), e);
		}
	}

	private List<Element> parseValidators(Element element) {
		Element validatorsElements = element.getChild("validators");
		return validatorsElements != null ? validatorsElements.getChildren() : new ArrayList<Element>();
	}

	private boolean isInheriting(Element element) {
		String stringValue = element.getChild("inheriting").getText();
		return "".equals(stringValue) ? null : Boolean.parseBoolean(stringValue);
	}

	private void addReferencesToBuilder(MetadataBuilder metadataBuilder, Element metadataElement) {
		Element references = metadataElement.getChild("references");

		if (metadataBuilder.getInheritance() == null && !references.getChildren().isEmpty() && !metadataBuilder
				.hasDefinedReferences()) {

			String schemaType = references.getChild("schemaTypes").getChildText("schemaType");
			if (!"null".equals(schemaType) && StringUtils.isNotEmpty(schemaType)) {
				metadataBuilder.defineReferences().setCompleteSchemaTypeCode(schemaType);
			}

			Element schemas = references.getChild("schemas");
			for (Element schema : schemas.getChildren("schema")) {
				metadataBuilder.defineReferences().addCompleteSchemaCode(schema.getText());
			}

		}
	}

	private void parseDataEntryElement(MetadataBuilder metadataBuilder, Element metadataElement) {
		Element dataEntry = metadataElement.getChild("dataEntry");
		if (dataEntry != null) {

			String typeString = dataEntry.getChildText("dataEntryType");
			DataEntryType type = DataEntryType.valueOf(typeString);

			if (type == DataEntryType.MANUAL) {
				metadataBuilder.defineDataEntry().asManual();

			} else if (type == DataEntryType.COPIED) {
				String copiedMetadata = dataEntry.getChildText("copiedMetadata");
				String referenceMetadata = dataEntry.getChildText("referenceMetadata");
				metadataBuilder.defineDataEntry().as(new CopiedDataEntry(referenceMetadata, copiedMetadata));

			} else if (type == DataEntryType.CALCULATED) {
				String calculator = dataEntry.getChildText("calculator");
				metadataBuilder.defineDataEntry().asCalculated(calculator);
			}
		}
	}

	private void parseDefaultSchema(Element root, MetadataSchemaTypeBuilder schemaTypeBuilder) {
		Element defaultSchemaElement = root.getChild("defaultSchema");
		MetadataSchemaBuilder defaultSchemaBuilder = schemaTypeBuilder.getDefaultSchema();
		defaultSchemaBuilder.setLabel(getLabelValue(defaultSchemaElement));
		for (Element metadataElement : defaultSchemaElement.getChildren("metadata")) {
			parseMetadata(defaultSchemaBuilder, metadataElement);
		}
		List<Element> validatorElements = parseValidators(defaultSchemaElement);
		if (!validatorElements.isEmpty()) {
			for (Element validatorElement : validatorElements) {
				defaultSchemaBuilder.defineValidators().add(parseSchemaValidator(validatorElement));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Class<? extends RecordValidator> parseSchemaValidator(Element validatorElement) {
		try {
			return (Class<? extends RecordValidator>) Class.forName(validatorElement.getText());
		} catch (ClassNotFoundException e) {
			throw new MetadataSchemasManagerRuntimeException.NoSuchValidatorClass(validatorElement.getText(), e);
		}
	}

	private String getCodeValue(Element element) {
		return element.getChild("code").getText();
	}

	private String getLabelValue(Element metadata) {
		String labelValue = metadata.getChild("label").getText();
		return "".equals(labelValue) ? null : labelValue;
	}

	private MetadataValueType getTypeValue(Element element) {
		String stringValue = element.getChild("type").getText();
		return "".equals(stringValue) ? null : MetadataValueType.valueOf(stringValue);
	}

	private <T> Class<T> getClassValue(Element element, String childTagName) {
		String className = element.getChild(childTagName).getText();
		if (StringUtils.isNotBlank(className)) {
			Class classValue = new InstanciationUtils().loadClassWithoutExpectableExceptions(className);
			return classValue;
		} else {
			return null;
		}
	}

	private boolean getBooleanFlagValue(Element element, String childTagName) {
		String stringValue = element.getChild(childTagName).getText();
		return "".equals(stringValue) ? null : Boolean.parseBoolean(stringValue);
	}

	private String getStringValue(Element element, String childTagName) {
		return element.getChild(childTagName).getText();
	}

}
