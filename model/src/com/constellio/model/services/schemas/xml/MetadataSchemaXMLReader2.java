package com.constellio.model.services.schemas.xml;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.extractions.DefaultMetadataPopulatorPersistenceManager;
import com.constellio.model.services.records.extractions.MetadataPopulator;
import com.constellio.model.services.records.extractions.MetadataPopulatorPersistenceManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.constellio.model.entities.schemas.LegacyGlobalMetadatas.isLegacyGlobalMetadata;
import static com.constellio.model.entities.schemas.Schemas.isGlobalMetadata;

public class MetadataSchemaXMLReader2 {

	private static Map<String, Class<? extends RecordMetadataValidator<?>>> validatorsCache = new HashMap<>();

	public static final String FORMAT_VERSION = "2";

	ClassProvider classProvider;
	private final MetadataPopulatorPersistenceManager metadataPopulatorXMLSerializer = new DefaultMetadataPopulatorPersistenceManager();

	public MetadataSchemaXMLReader2(ClassProvider classProvider) {
		this.classProvider = classProvider;
	}

	public MetadataSchemaTypesBuilder read(CollectionInfo collection, Document document,
										   DataStoreTypesFactory typesFactory,
										   ModelLayerFactory modelLayerFactory) {

		Element rootElement = document.getRootElement();
		int version = Integer.valueOf(rootElement.getAttributeValue("version")) - 1;
		MetadataSchemaTypesBuilder typesBuilder = (new MetadataSchemaTypesBuilder(collection))
				.createWithVersion(collection, modelLayerFactory, version, classProvider, Arrays.asList(Language.French));
		for (Element schemaTypeElement : rootElement.getChildren("type")) {
			parseProfilType(typesBuilder, schemaTypeElement, typesFactory, modelLayerFactory);
		}
		return typesBuilder;
	}

	private MetadataSchemaType parseProfilType(MetadataSchemaTypesBuilder typesBuilder, Element element,
											   DataStoreTypesFactory typesFactory,
											   ModelLayerFactory modelLayerFactory) {
		String code = getCodeValue(element);
		MetadataSchemaTypeBuilder schemaTypeBuilder = typesBuilder.createNewSchemaType(code, false);

		for (Language language : typesBuilder.getLanguages()) {
			schemaTypeBuilder = schemaTypeBuilder.addLabel(language, getLabelValue(element));
		}

		MetadataSchemaBuilder collectionSchema = "collection".equals(code) ?
												 null : typesBuilder.getSchema(Collection.DEFAULT_SCHEMA);

		schemaTypeBuilder.setSecurity(getBooleanFlagValueWithFalseAsDefaultValue(element, "security"));
		schemaTypeBuilder.setInTransactionLog(getBooleanFlagValueWithTrueAsDefaultValue(element, "inTransactionLog"));
		parseDefaultSchema(element, schemaTypeBuilder, typesBuilder, collectionSchema);
		parseCustomSchemas(element, schemaTypeBuilder, collectionSchema);
		return schemaTypeBuilder.build(typesFactory, typesBuilder, modelLayerFactory);
	}

	private void parseCustomSchemas(Element root, MetadataSchemaTypeBuilder schemaTypeBuilder,
									MetadataSchemaBuilder collectionSchema) {
		Element customSchemasElement = root.getChild("customSchemas");
		for (Element schemaElement : customSchemasElement.getChildren("schema")) {
			parseSchema(schemaTypeBuilder, schemaElement, collectionSchema);
		}
	}

	private void parseSchema(MetadataSchemaTypeBuilder schemaTypeBuilder, Element schemaElement,
							 MetadataSchemaBuilder collectionSchema) {
		MetadataSchemaBuilder schemaBuilder = schemaTypeBuilder.createCustomSchema(getCodeValue(schemaElement));
		schemaBuilder.addLabel(Language.French, getLabelValue(schemaElement));
		schemaBuilder.setUndeletable(getBooleanFlagValueWithTrueAsDefaultValue(schemaElement, "undeletable"));


		for (Element metadataElement : schemaElement.getChildren("m")) {
			parseMetadata(schemaBuilder, metadataElement, collectionSchema);
		}
		List<String> validatorsClassNames = parseValidators(schemaElement, null);
		for (String validatorsClassName : validatorsClassNames) {
			schemaBuilder.defineValidators().add(getValidatorClass(validatorsClassName));
		}
	}

	private void parseMetadata(MetadataSchemaBuilder schemaBuilder, Element metadataElement,
							   MetadataSchemaBuilder collectionSchema) {
		String codeValue = getCodeValue(metadataElement);

		MetadataBuilder metadataBuilder;
		if (schemaBuilder.hasMetadata(codeValue)) {
			metadataBuilder = schemaBuilder.get(codeValue);
		} else {
			metadataBuilder = schemaBuilder.create(codeValue);
		}

		metadataBuilder.addLabel(Language.French, getLabelValue(metadataElement));

		String localCode = metadataBuilder.getLocalCode();

		if (metadataBuilder.getInheritance() != null && !metadataBuilder.getCode().contains("default")) {
			parseMetadataWithInheritance(metadataElement, metadataBuilder);
		} else {
			parseMetadataWithoutInheritance(metadataElement, metadataBuilder, collectionSchema);

		}

	}

	private void parseMetadataWithInheritance(Element metadataElement, MetadataBuilder metadataBuilder) {
		String enabledStringValue = metadataElement.getAttributeValue("enabled");
		String defaultRequirementStringValue = metadataElement.getAttributeValue("defaultRequirement");
		String inputMask = metadataElement.getAttributeValue("inputMask");

		List<String> validatorsClassNames = parseValidators(metadataElement, null);
		for (String validatorsClassName : validatorsClassNames) {
			metadataBuilder.defineValidators().add(getValidatorClass(validatorsClassName));
		}

		if (enabledStringValue != null) {
			metadataBuilder.setEnabled(readBoolean(enabledStringValue));
		}

		if (defaultRequirementStringValue != null) {
			metadataBuilder.setDefaultRequirement(readBoolean(defaultRequirementStringValue));
		}

		if (inputMask != null) {
			metadataBuilder.setInputMask(inputMask);
		}

		if (metadataElement.getChild("defaultValue") != null) {
			ParametrizedInstanceUtils utils = new ParametrizedInstanceUtils();
			Object defaultValue = utils.toObject(metadataElement.getChild("defaultValue"));
			metadataBuilder.setDefaultValue(defaultValue);
		}

		metadataBuilder.setDuplicable(false);

		setPopulateConfigs(metadataBuilder, metadataElement);
	}

	private void parseMetadataWithoutInheritance(Element metadataElement, MetadataBuilder metadataBuilder,
												 MetadataSchemaBuilder collectionSchema) {
		if (!metadataBuilder.isSystemReserved()) {
			String enabledStringValue = metadataElement.getAttributeValue("enabled");
			String defaultRequirementStringValue = metadataElement.getAttributeValue("defaultRequirement");

			if (enabledStringValue == null) {
				metadataBuilder.setEnabled(true);
			} else {
				metadataBuilder.setEnabled(readBoolean(enabledStringValue));
			}

			if (defaultRequirementStringValue == null) {
				metadataBuilder.setDefaultRequirement(false);

			} else {
				metadataBuilder.setDefaultRequirement(readBoolean(defaultRequirementStringValue));
			}

			metadataBuilder.setDuplicable(false);
		}

		MetadataBuilder globalMetadataInCollectionSchema = null;

		boolean inheriteGlobalMetadata = false;
		if ((isGlobalMetadata(metadataBuilder.getLocalCode()) || isLegacyGlobalMetadata(metadataBuilder.getLocalCode())) && collectionSchema != null
			&& collectionSchema.hasMetadata(metadataBuilder.getLocalCode())) {
			globalMetadataInCollectionSchema = collectionSchema.getMetadata(metadataBuilder.getLocalCode());
			inheriteGlobalMetadata = true;
		}

		String xmlLabel = getLabelValue(metadataElement);
		if (inheriteGlobalMetadata && xmlLabel == null) {
			metadataBuilder.setLabels(globalMetadataInCollectionSchema.getLabels());
		} else {
			metadataBuilder.addLabel(Language.French, xmlLabel);
		}

		List<String> validatorsClassNames = parseValidators(metadataElement, globalMetadataInCollectionSchema);
		for (String validatorsClassName : validatorsClassNames) {
			metadataBuilder.defineValidators().add(getValidatorClass(validatorsClassName));
		}

		boolean userDefinedMetadata = metadataBuilder.getLocalCode().startsWith("USR");
		parseDataEntryElement(metadataBuilder, metadataElement, globalMetadataInCollectionSchema);
		//if (!isInheriting(metadataElement)) {

		if (metadataBuilder.getType() == null) {
			MetadataValueType xmlTypeValue = getTypeValue(metadataElement);
			if (xmlTypeValue == null && inheriteGlobalMetadata) {
				xmlTypeValue = globalMetadataInCollectionSchema.getType();
			}
			metadataBuilder.setType(xmlTypeValue);
		}

		if (inheriteGlobalMetadata) {
			metadataBuilder.setUndeletable(true);
		} else {
			metadataBuilder.setUndeletable(getBooleanFlagValueWithTrueAsDefaultValue(metadataElement, "undeletable"));
		}

		String systemReservedStringValue = metadataElement.getAttributeValue("systemReserved");
		if (inheriteGlobalMetadata && systemReservedStringValue == null) {
			metadataBuilder.setSystemReserved(globalMetadataInCollectionSchema.isSystemReserved());
		} else {
			metadataBuilder.setSystemReserved(!userDefinedMetadata &&
											  readBooleanWithDefaultValue(systemReservedStringValue, true));
		}

		String defaultRequirementStringValue = metadataElement.getAttributeValue("defaultRequirement");
		if (inheriteGlobalMetadata && defaultRequirementStringValue == null) {
			metadataBuilder.setDefaultRequirement(globalMetadataInCollectionSchema.getDefaultRequirement());
		} else {
			metadataBuilder.setDefaultRequirement(readBooleanWithDefaultValue(defaultRequirementStringValue, false));
		}

		String essentialStringValue = metadataElement.getAttributeValue("essential");
		if (inheriteGlobalMetadata && essentialStringValue == null) {
			metadataBuilder.setEssential(globalMetadataInCollectionSchema.isEssential());
		} else {
			metadataBuilder.setEssential(readBooleanWithDefaultValue(essentialStringValue, false));
		}

		String essentialInSummaryStringValue = metadataElement.getAttributeValue("essentialInSummary");
		if (inheriteGlobalMetadata && essentialInSummaryStringValue == null) {
			metadataBuilder.setEssentialInSummary(globalMetadataInCollectionSchema.isEssentialInSummary());
		} else {
			metadataBuilder.setEssentialInSummary(readBooleanWithDefaultValue(essentialInSummaryStringValue, false));
		}

		String unmodifiableStringValue = metadataElement.getAttributeValue("unmodifiable");
		if (inheriteGlobalMetadata && unmodifiableStringValue == null) {
			metadataBuilder.setUnmodifiable(globalMetadataInCollectionSchema.isUnmodifiable());
		} else {
			metadataBuilder.setUnmodifiable(readBooleanWithDefaultValue(unmodifiableStringValue, false));
		}

		String encryptedStringValue = metadataElement.getAttributeValue("encrypted");
		if (inheriteGlobalMetadata && encryptedStringValue == null) {
			metadataBuilder.setEncrypted(globalMetadataInCollectionSchema.isEncrypted());
		} else {
			metadataBuilder.setEncrypted(readBooleanWithDefaultValue(encryptedStringValue, false));
		}

		String searchableStringValue = metadataElement.getAttributeValue("searchable");
		if (inheriteGlobalMetadata && searchableStringValue == null) {
			metadataBuilder.setSearchable(globalMetadataInCollectionSchema.isSearchable());
		} else {
			metadataBuilder.setSearchable(readBooleanWithDefaultValue(searchableStringValue, false));
		}

		String inputMaskStringValue = metadataElement.getAttributeValue("inputMask");
		if (inheriteGlobalMetadata && inputMaskStringValue == null) {
			metadataBuilder.setInputMask(globalMetadataInCollectionSchema.getInputMask());
		} else {
			metadataBuilder.setInputMask(inputMaskStringValue);
		}

		String sortableStringValue = metadataElement.getAttributeValue("sortable");
		if (inheriteGlobalMetadata && sortableStringValue == null) {
			metadataBuilder.setSortable(globalMetadataInCollectionSchema.isSortable());
		} else {
			metadataBuilder.setSortable(readBooleanWithDefaultValue(sortableStringValue, false));
		}

		String schemaAutocompleteStringValue = metadataElement.getAttributeValue("schemaAutocomplete");
		if (inheriteGlobalMetadata && schemaAutocompleteStringValue == null) {
			metadataBuilder.setSchemaAutocomplete(globalMetadataInCollectionSchema.isSchemaAutocomplete());
		} else {
			metadataBuilder.setSchemaAutocomplete(readBooleanWithDefaultValue(schemaAutocompleteStringValue, false));
		}

		String uniqueStringValue = metadataElement.getAttributeValue("uniqueValue");
		if (inheriteGlobalMetadata && uniqueStringValue == null) {
			metadataBuilder.setUniqueValue(globalMetadataInCollectionSchema.isUniqueValue());
		} else {
			metadataBuilder.setUniqueValue(readBooleanWithDefaultValue(uniqueStringValue, false));
		}

		String childOfRelationshipStringValue = metadataElement.getAttributeValue("childOfRelationship");
		if (inheriteGlobalMetadata && childOfRelationshipStringValue == null) {
			metadataBuilder.setChildOfRelationship(globalMetadataInCollectionSchema.isChildOfRelationship());
		} else {
			metadataBuilder.setChildOfRelationship(readBooleanWithDefaultValue(childOfRelationshipStringValue, false));
		}

		String taxonomyRelationshipStringValue = metadataElement.getAttributeValue("taxonomyRelationship");
		if (inheriteGlobalMetadata && taxonomyRelationshipStringValue == null) {
			metadataBuilder.setTaxonomyRelationship(globalMetadataInCollectionSchema.isTaxonomyRelationship());
		} else {
			metadataBuilder.setTaxonomyRelationship(readBooleanWithDefaultValue(taxonomyRelationshipStringValue, false));
		}

		String multivalueStringValue = metadataElement.getAttributeValue("multivalue");
		if (inheriteGlobalMetadata && multivalueStringValue == null) {
			metadataBuilder.setMultivalue(globalMetadataInCollectionSchema.isMultivalue());
		} else {
			metadataBuilder.setMultivalue(readBooleanWithDefaultValue(multivalueStringValue, false));
		}

		//}

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
		if (metadataElement.getChild("defaultValue") != null) {
			ParametrizedInstanceUtils utils = new ParametrizedInstanceUtils();
			Object defaultValue = utils.toObject(metadataElement.getChild("defaultValue"));
			metadataBuilder.setDefaultValue(defaultValue);
		}

		setPopulateConfigs(metadataBuilder, metadataElement);

		addReferencesToBuilder(metadataBuilder, metadataElement, globalMetadataInCollectionSchema);
	}

	private void setPopulateConfigs(MetadataBuilder metadataBuilder, Element metadataElement) {

		List<String> styles = new ArrayList<>();
		List<String> properties = new ArrayList<>();
		List<RegexConfig> regexes = new ArrayList<>();
		List<MetadataPopulator> metadataPopulators = new ArrayList<>();

		Element populateConfigsElement = metadataElement.getChild("populateConfigs");
		if (populateConfigsElement != null) {
			if (populateConfigsElement.getAttributeValue("styles") != null) {
				styles.addAll(Arrays.asList(populateConfigsElement.getAttributeValue("styles").split(",")));
			}
			if (populateConfigsElement.getAttributeValue("properties") != null) {
				properties.addAll(Arrays.asList(populateConfigsElement.getAttributeValue("properties").split(",")));
			}
			if (populateConfigsElement.getChild("regexConfigs") != null) {
				Element regexConfigsElement = populateConfigsElement.getChild("regexConfigs");
				addRegexConfigElementsToList(regexConfigsElement, regexes);
			}
			if (populateConfigsElement.getChild("metadataPopulators") != null) {
				Element metadataPopulatorsElement = populateConfigsElement.getChild("metadataPopulators");
				addMetadataPopulateElementsToList(metadataPopulatorsElement, metadataPopulators);
			}

		}
		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder = MetadataPopulateConfigsBuilder.create();
		metadataPopulateConfigsBuilder.setStyles(styles);
		metadataPopulateConfigsBuilder.setProperties(properties);
		metadataPopulateConfigsBuilder.setRegexes(regexes);
		metadataPopulateConfigsBuilder.setMetadataPopulators(metadataPopulators);
		metadataBuilder.definePopulateConfigsBuilder(metadataPopulateConfigsBuilder);
	}

	private void addMetadataPopulateElementsToList(Element metadataPopulatorsElement,
												   List<MetadataPopulator> metadataPopulators) {
		for (Element metadataPopulatorElement : metadataPopulatorsElement.getChildren()) {
			try {
				metadataPopulators.add(metadataPopulatorXMLSerializer.fromXML(metadataPopulatorElement));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void addRegexConfigElementsToList(Element element, List<RegexConfig> listToAdd) {
		if (element.getChildren("regexConfig") != null) {
			for (Element regexConfigElement : element.getChildren("regexConfig")) {
				String metadataInput = regexConfigElement.getAttributeValue("metadataInput");
				String regexPattern = regexConfigElement.getAttributeValue("regex");
				Pattern regex = Pattern.compile(regexPattern);
				String value = regexConfigElement.getAttributeValue("value");
				RegexConfigType regexConfigType;
				try {
					regexConfigType = RegexConfigType.valueOf(regexConfigElement.getAttributeValue("regexConfigType"));
				} catch (IllegalArgumentException | NullPointerException e) {
					regexConfigType = RegexConfigType.SUBSTITUTION;
				}
				RegexConfig regexConfig = new RegexConfig(metadataInput, regex, value, regexConfigType);
				listToAdd.add(regexConfig);
			}
		}
	}

	private void setAccessRestrictions(MetadataAccessRestrictionBuilder metadataAccessRestrictionBuilder,
									   Element metadataElement) {
		if (metadataElement.getChild("accessRestrictions") != null) {
			Element accessRestrictionsElement = metadataElement.getChild("accessRestrictions");
			if (accessRestrictionsElement.getAttributeValue("readAccessRestrictions") != null) {
				metadataAccessRestrictionBuilder.getRequiredReadRoles().addAll(Arrays.asList(
						accessRestrictionsElement.getAttributeValue("readAccessRestrictions").split(",")));
			}
			if (accessRestrictionsElement.getAttributeValue("writeAccessRestrictions") != null) {
				metadataAccessRestrictionBuilder.getRequiredWriteRoles().addAll(Arrays.asList(
						accessRestrictionsElement.getAttributeValue("writeAccessRestrictions").split(",")));
			}
			if (accessRestrictionsElement.getAttributeValue("deleteAccessRestrictions") != null) {
				metadataAccessRestrictionBuilder.getRequiredDeleteRoles().addAll(Arrays.asList(
						accessRestrictionsElement.getAttributeValue("deleteAccessRestrictions").split(",")));
			}
			if (accessRestrictionsElement.getAttributeValue("modifyAccessRestrictions") != null) {
				metadataAccessRestrictionBuilder.getRequiredModificationRoles().addAll(Arrays.asList(
						accessRestrictionsElement.getAttributeValue("modifyAccessRestrictions").split(",")));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Class<? extends RecordMetadataValidator<?>> getValidatorClass(String validatorClassName) {

		Class<? extends RecordMetadataValidator<?>> validator = validatorsCache.get(validatorClassName);

		if (validator == null) {
			try {
				validator = classProvider.loadClass(validatorClassName);
				validatorsCache.put(validatorClassName, validator);
			} catch (ClassNotFoundException e) {
				throw new MetadataSchemasManagerRuntimeException.NoSuchValidatorClass(validatorClassName, e);
			}
		}

		return validator;
	}

	private List<String> parseValidators(Element element, MetadataBuilder collectionSchemaBuilder) {
		Element validatorsElements = element.getChild("validators");
		if (validatorsElements != null) {
			String strValue = validatorsElements.getAttributeValue("classNames");

			if (StringUtils.isBlank(strValue)) {
				return new ArrayList<>();
			} else {
				return Arrays.asList(strValue.split(","));
			}
		} else if (collectionSchemaBuilder != null) {
			return new ArrayList<>(collectionSchemaBuilder.defineValidators().getClassnames());
		} else {
			return new ArrayList<>();
		}

	}

	private boolean isInheriting(Element element) {
		if (element.getAttributeValue("inheriting") == null) {
			return false;
		} else {
			String stringValue = element.getAttributeValue("inheriting");
			return "".equals(stringValue) ? null : readBoolean(stringValue);
		}
	}

	private void addReferencesToBuilder(MetadataBuilder metadataBuilder, Element metadataElement,
										MetadataBuilder collectionSchemaBuilder) {
		if (metadataElement.getChild("references") != null) {
			Element references = metadataElement.getChild("references");

			if (metadataBuilder.getInheritance() == null && !metadataBuilder.hasDefinedReferences()) {

				String schemaType = references.getAttributeValue("schemaType");
				if (StringUtils.isNotEmpty(schemaType)) {
					metadataBuilder.defineReferences().setCompleteSchemaTypeCode(schemaType);
				} else {
					List<String> schemas = Arrays.asList(references.getAttributeValue("schemas").split(","));
					for (String schema : schemas) {
						metadataBuilder.defineReferences().addCompleteSchemaCode(schema);
					}
				}
			}
		} else if (collectionSchemaBuilder != null) {
			collectionSchemaBuilder.setAllowedReferenceBuilder(collectionSchemaBuilder.getAllowedReferencesBuilder());
		}
	}

	private void parseDataEntryElement(MetadataBuilder metadataBuilder, Element metadataElement,
									   MetadataBuilder collectionSchemaBuilder) {
		Element dataEntry = metadataElement.getChild("dataEntry");
		if (dataEntry != null) {
			if (dataEntry.getAttributeValue("copied") != null) {
				String copiedMetadata = dataEntry.getAttributeValue("copied");
				String referenceMetadata = dataEntry.getAttributeValue("reference");
				metadataBuilder.defineDataEntry().as(new CopiedDataEntry(referenceMetadata, copiedMetadata));

			} else if (dataEntry.getAttributeValue("calculator") != null) {
				String calculator = dataEntry.getAttributeValue("calculator");
				metadataBuilder.defineDataEntry().asCalculated(calculator);
			}
		} else if (!isInheriting(metadataElement)) {
			if (collectionSchemaBuilder == null) {
				metadataBuilder.defineDataEntry().asManual();
			} else {
				metadataBuilder.defineDataEntry().as(collectionSchemaBuilder.getDataEntry());
			}

		}
	}

	private void parseDefaultSchema(Element root, MetadataSchemaTypeBuilder schemaTypeBuilder,
									MetadataSchemaTypesBuilder typesBuilder, MetadataSchemaBuilder collectionSchema) {
		Element defaultSchemaElement = root.getChild("defaultSchema");
		MetadataSchemaBuilder defaultSchemaBuilder = schemaTypeBuilder.getDefaultSchema();

		defaultSchemaBuilder.addLabel(Language.French, getLabelValue(defaultSchemaElement));
		//	new CommonMetadataBuilder().addCommonMetadataToExistingSchema(defaultSchemaBuilder, typesBuilder);
		for (Element metadataElement : defaultSchemaElement.getChildren("m")) {
			parseMetadata(defaultSchemaBuilder, metadataElement, collectionSchema);
		}
		List<String> validatorsClassNames = parseValidators(defaultSchemaElement, null);
		for (String validatorsClassName : validatorsClassNames) {
			defaultSchemaBuilder.defineValidators().add(getValidatorClass(validatorsClassName));
		}
	}

	private String getCodeValue(Element element) {
		return element.getAttributeValue("code");
	}

	private String getLabelValue(Element metadata) {
		String labelValue = metadata.getAttributeValue("label");
		return StringUtils.isBlank(labelValue) ? null : labelValue;
	}

	private MetadataValueType getTypeValue(Element element) {
		String stringValue = element.getAttributeValue("type");
		return StringUtils.isBlank(stringValue) ? null : MetadataValueType.valueOf(stringValue);
	}

	private <T> Class<T> getClassValue(Element element, String attributeName) {
		String className = element.getAttributeValue(attributeName);
		if (StringUtils.isNotBlank(className)) {
			return new InstanciationUtils().loadClassWithoutExpectableExceptions(className);
		} else {
			return null;
		}
	}

	private boolean getBooleanFlagValueWithTrueAsDefaultValue(Element element, String childTagName) {
		String value = element.getAttributeValue(childTagName);
		return value == null || readBoolean(value);
	}

	private boolean getBooleanFlagValueWithFalseAsDefaultValue(Element element, String childTagName) {
		String value = element.getAttributeValue(childTagName);

		if (value == null) {
			return false;
		} else if ("t".equals(value)) {
			return true;

		} else if ("f".equals(value)) {
			return false;
		}
		throw new ImpossibleRuntimeException("Unsupported value for attribute '" + childTagName + "'");

	}

	private boolean readBoolean(String value) {
		if (value == null) {
			return false;
		} else if ("t".equals(value)) {
			return true;

		} else if ("f".equals(value)) {
			return false;
		}
		throw new ImpossibleRuntimeException("Unsupported value '" + value + "'");
	}

	private boolean readBooleanWithDefaultValue(String value, boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		} else if ("t".equals(value)) {
			return true;

		} else if ("f".equals(value)) {
			return false;
		}
		throw new ImpossibleRuntimeException("Unsupported value '" + value + "'");
	}
}
