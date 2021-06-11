package com.constellio.model.services.schemas.xml;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManagerRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataAccessRestrictionBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataPopulateConfigsBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.utils.ClassProvider;
import com.constellio.model.utils.InstanciationUtils;
import com.constellio.model.utils.ParametrizedInstanceUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class MetadataSchemaXMLReader1 {

	ClassProvider classProvider;

	public MetadataSchemaXMLReader1(ClassProvider classProvider) {
		this.classProvider = classProvider;
	}

	public MetadataSchemaTypesBuilder read(CollectionInfo collection, Document document,
										   DataStoreTypesFactory typesFactory,
										   ModelLayerFactory modelLayerFactory) {
		Element rootElement = document.getRootElement();
		int version = Integer.valueOf(rootElement.getAttributeValue("version")) - 1;
		MetadataSchemaTypesBuilder typesBuilder = (new MetadataSchemaTypesBuilder(collection))
				.createWithVersion(collection, modelLayerFactory, version, classProvider, Arrays.asList(Language.French, Language.English));

		for (Element schemaTypeElement : rootElement.getChildren("schemaType")) {
			parseProfilType(typesBuilder, schemaTypeElement, typesFactory, modelLayerFactory);
		}
		return typesBuilder;
	}

	private MetadataSchemaType parseProfilType(MetadataSchemaTypesBuilder typesBuilder, Element element,
											   DataStoreTypesFactory typesFactory,
											   ModelLayerFactory modelLayerFactory) {
		MetadataSchemaTypeBuilder schemaTypeBuilder = typesBuilder.createNewSchemaTypeWithSecurity(getCodeValue(element), false).addLabel(
				Language.French, getLabelValue(element));
		schemaTypeBuilder.setSecurity(getBooleanFlagValue(element, "security"));
		schemaTypeBuilder.setInTransactionLog(getBooleanFlagValueWithTrueAsDefaultValue(element, "inTransactionLog"));
		parseDefaultSchema(element, schemaTypeBuilder);
		parseCustomSchemas(element, schemaTypeBuilder);
		return schemaTypeBuilder.build(typesFactory, typesBuilder, modelLayerFactory);
	}

	private void parseCustomSchemas(Element root, MetadataSchemaTypeBuilder schemaTypeBuilder) {
		Element customSchemasElement = root.getChild("customSchemas");
		for (Element schemaElement : customSchemasElement.getChildren("schema")) {
			parseSchema(schemaTypeBuilder, schemaElement);
		}
	}

	private void parseSchema(MetadataSchemaTypeBuilder schemaTypeBuilder, Element schemaElement) {
		MetadataSchemaBuilder schemaBuilder = schemaTypeBuilder.createCustomSchema(getCodeValue(schemaElement));
		schemaBuilder.addLabel(Language.French, getLabelValue(schemaElement));
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
		if (schemaBuilder.hasMetadata(codeValue)) {
			metadataBuilder = schemaBuilder.get(codeValue);
		} else {
			metadataBuilder = schemaBuilder.create(codeValue);
		}


		metadataBuilder.addLabel(Language.French, getLabelValue(metadataElement));
		if (!metadataBuilder.isSystemReserved()) {
			metadataBuilder.setEnabled(getBooleanFlagValue(metadataElement, "enabled"));
			metadataBuilder.setDefaultRequirement(getBooleanFlagValue(metadataElement, "defaultRequirement"));
			metadataBuilder.setDuplicable(false);
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
			metadataBuilder.setEssential(getBooleanFlagValue(metadataElement, "essential"));
			metadataBuilder.setEssentialInSummary(getBooleanFlagValue(metadataElement, "essentialInSummary"));
			metadataBuilder.setUnmodifiable(getBooleanFlagValue(metadataElement, "unmodifiable"));
			metadataBuilder.setEncrypted(getBooleanFlagValue(metadataElement, "encrypted"));
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

		ParametrizedInstanceUtils utils = new ParametrizedInstanceUtils();
		Object defaultValue = utils.toObject(metadataElement.getChild("defaultValue"));
		if (!"null".equals(defaultValue)) {
			metadataBuilder.setDefaultValue(defaultValue);
		}

		setPopulateConfigs(metadataBuilder, metadataElement);

		addReferencesToBuilder(metadataBuilder, metadataElement);
	}

	private void setPopulateConfigs(MetadataBuilder metadataBuilder, Element metadataElement) {

		List<String> styles = new ArrayList<>();
		List<String> properties = new ArrayList<>();
		List<RegexConfig> regexes = new ArrayList<>();

		Element populateConfigsElement = metadataElement.getChild("populateConfigs");
		if (populateConfigsElement != null) {
			Element stylesElement = populateConfigsElement.getChild("styles");
			Element propertiesElement = populateConfigsElement.getChild("properties");
			Element regexConfigsElement = populateConfigsElement.getChild("regexConfigs");

			addStringElementsToList("style", stylesElement, styles);
			addStringElementsToList("property", propertiesElement, properties);
			addRegexConfigElementsToList(regexConfigsElement, regexes);
		}
		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder = MetadataPopulateConfigsBuilder.create();
		metadataPopulateConfigsBuilder.setStyles(styles);
		metadataPopulateConfigsBuilder.setProperties(properties);
		metadataPopulateConfigsBuilder.setRegexes(regexes);
		metadataBuilder.definePopulateConfigsBuilder(metadataPopulateConfigsBuilder);
	}

	private void addStringElementsToList(String childName, Element element, List<String> listToAdd) {
		for (Element childElement : element.getChildren(childName)) {
			listToAdd.add(childElement.getText());
		}
	}

	private void addRegexConfigElementsToList(Element element, List<RegexConfig> listToAdd) {
		for (Element regexConfigElement : element.getChildren("regexConfig")) {
			String metadataInput = regexConfigElement.getChild("metadataInput").getText();
			String regexPattern = regexConfigElement.getChild("regex").getText();
			Pattern regex = Pattern.compile(regexPattern);
			String value = regexConfigElement.getChild("value").getText();
			RegexConfigType regexConfigType;
			try {
				regexConfigType = RegexConfigType.valueOf(regexConfigElement.getChild("regexConfigType").getText());
			} catch (IllegalArgumentException | NullPointerException e) {
				regexConfigType = RegexConfigType.SUBSTITUTION;
			}
			RegexConfig regexConfig = new RegexConfig(metadataInput, regex, value, regexConfigType);
			listToAdd.add(regexConfig);
		}
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
			return classProvider.loadClass(validatorElement.getText());
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
		defaultSchemaBuilder.addLabel(Language.French, getLabelValue(defaultSchemaElement));
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
			return classProvider.loadClass(validatorElement.getText());
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
			return new InstanciationUtils().loadClassWithoutExpectableExceptions(className);
		} else {
			return null;
		}
	}

	private boolean getBooleanFlagValue(Element element, String childTagName) {
		Element childTag = element.getChild(childTagName);
		String stringValue = childTag == null ? null : childTag.getText();
		return stringValue != null && Boolean.parseBoolean(stringValue);
	}

	private boolean getBooleanFlagValueWithTrueAsDefaultValue(Element element, String childTagName) {
		Element childTag = element.getChild(childTagName);
		String stringValue = childTag == null ? null : childTag.getText();
		return stringValue == null || Boolean.parseBoolean(stringValue);
	}

	private String getStringValue(Element element, String childTagName) {
		return element.getChild(childTagName).getText();
	}

}
