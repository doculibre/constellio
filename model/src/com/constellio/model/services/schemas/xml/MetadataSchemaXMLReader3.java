package com.constellio.model.services.schemas.xml;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.AggregatedCalculator;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.AggregationType;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.extractions.DefaultMetadataPopulatorPersistenceManager;
import com.constellio.model.services.records.extractions.MetadataPopulator;
import com.constellio.model.services.records.extractions.MetadataPopulatorPersistenceManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataAccessRestrictionBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataPopulateConfigsBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.utils.ClassProvider;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import com.constellio.model.utils.InstanciationUtils;
import com.constellio.model.utils.Parametrized;
import com.constellio.model.utils.ParametrizedInstanceUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.constellio.model.entities.schemas.LegacyGlobalMetadatas.isLegacyGlobalMetadata;
import static com.constellio.model.entities.schemas.Schemas.isGlobalMetadata;
import static com.constellio.model.utils.EnumWithSmallCodeUtils.toEnum;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MetadataSchemaXMLReader3 {

	private static Map<String, Class<? extends RecordMetadataValidator<?>>> validatorsCache = new HashMap<>();

	public static final String FORMAT_VERSION = "3";

	ClassProvider classProvider;

	private final MetadataPopulatorPersistenceManager metadataPopulatorXMLSerializer = new DefaultMetadataPopulatorPersistenceManager();

	public MetadataSchemaXMLReader3(ClassProvider classProvider) {
		this.classProvider = classProvider;
	}

	public MetadataSchemaTypesBuilder read(CollectionInfo collectionInfo, Document document,
										   DataStoreTypesFactory typesFactory,
										   ModelLayerFactory modelLayerFactory) {

		Element rootElement = document.getRootElement();
		int version = Integer.valueOf(rootElement.getAttributeValue("version")) - 1;
		List<Language> languages = getLanguages(rootElement);
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder
				.createWithVersion(collectionInfo, version, classProvider, languages);
		for (Element schemaTypeElement : rootElement.getChildren("type")) {
			parseSchemaType(typesBuilder, schemaTypeElement, typesFactory, modelLayerFactory);
		}
		return typesBuilder;
	}

	private List<Language> getLanguages(Element rootElement) {
		List<Language> languages = new ArrayList<>();
		List<String> languagesCodes = asList(rootElement.getAttributeValue("languages").split(","));
		for (String code : languagesCodes) {
			languages.add(Language.withCode(code));
		}
		return languages;
	}

	private MetadataSchemaType parseSchemaType(MetadataSchemaTypesBuilder typesBuilder, Element element,
											   DataStoreTypesFactory typesFactory,
											   ModelLayerFactory modelLayerFactory) {
		String code = getCodeValue(element);
		String id = element.getAttributeValue("id");

		Map<Language, String> labels = readLabels(element);
		MetadataSchemaTypeBuilder schemaTypeBuilder = typesBuilder.createNewSchemaType(code, false)
				.setLabels(labels);

		if (id != null) {
			schemaTypeBuilder.setId(Short.valueOf(id));
		}

		MetadataSchemaBuilder collectionSchema = "collection".equals(code) ?
												 null : typesBuilder.getSchema(Collection.DEFAULT_SCHEMA);

		schemaTypeBuilder.setReadOnlyLocked(getBooleanFlagValueWithFalseAsDefaultValue(element, "readOnlyLocked"));
		schemaTypeBuilder.setSecurity(getBooleanFlagValueWithFalseAsDefaultValue(element, "security"));
		schemaTypeBuilder.setInTransactionLog(getBooleanFlagValueWithTrueAsDefaultValue(element, "inTransactionLog"));
		schemaTypeBuilder.setSmallCode(element.getAttributeValue("smallCode"));
		schemaTypeBuilder.setDataStore(element.getAttributeValue("dataStore"));

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
		schemaBuilder.setLabels(readLabels(schemaElement));
		schemaBuilder.setUndeletable(getBooleanFlagValueWithTrueAsDefaultValue(schemaElement, "undeletable"));
		schemaBuilder.setActive(getBooleanFlagValueWithTrueAsDefaultValue(schemaElement, "active"));

		String id = schemaElement.getAttributeValue("id");
		if (id != null) {
			schemaBuilder.setId(Short.valueOf(id));
		}

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

		metadataBuilder.setLabels(readLabels(metadataElement));

		if (metadataBuilder.getInheritance() != null && !metadataBuilder.getCode().contains("_default_")) {
			parseMetadataWithInheritance(metadataElement, metadataBuilder);
		} else {
			parseMetadataWithoutInheritance(metadataElement, metadataBuilder, collectionSchema);
		}
	}

	private void parseMetadataWithInheritance(Element metadataElement, MetadataBuilder metadataBuilder) {
		String enabledStringValue = metadataElement.getAttributeValue("enabled");
		String defaultRequirementStringValue = metadataElement.getAttributeValue("defaultRequirement");
		String inputMask = metadataElement.getAttributeValue("inputMask");
		String duplicableStringValue = metadataElement.getAttributeValue("duplicable");

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

		if (duplicableStringValue != null) {
			metadataBuilder.setDuplicable(readBoolean(duplicableStringValue));
		}

		setPopulateConfigs(metadataBuilder, metadataElement);

		Map<String, Object> customParameter = TypeConvertionUtil.getCustomParameterMap(metadataElement);
		if (customParameter != null) {
			metadataBuilder.setCustomParameter(customParameter);
		}
	}

	private void parseMetadataWithoutInheritance(Element metadataElement, MetadataBuilder metadataBuilder,
												 MetadataSchemaBuilder collectionSchema) {
		if (!metadataBuilder.isSystemReserved()) {
			String enabledStringValue = metadataElement.getAttributeValue("enabled");
			String defaultRequirementStringValue = metadataElement.getAttributeValue("defaultRequirement");
			String duplicableStringValue = metadataElement.getAttributeValue("duplicable");

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

			if (duplicableStringValue == null) {
				metadataBuilder.setDuplicable(false);
			} else {
				metadataBuilder.setDuplicable(readBoolean(duplicableStringValue));
			}
		}

		MetadataBuilder globalMetadataInCollectionSchema = null;

		boolean inheriteGlobalMetadata = false;
		if ((isGlobalMetadata(metadataBuilder.getLocalCode()) || isLegacyGlobalMetadata(metadataBuilder.getLocalCode()))
			&& collectionSchema != null
			&& collectionSchema.hasMetadata(metadataBuilder.getLocalCode())) {
			globalMetadataInCollectionSchema = collectionSchema.getMetadata(metadataBuilder.getLocalCode());
			inheriteGlobalMetadata = true;
		}

		String idValue = metadataElement.getAttributeValue("id");
		if (idValue != null) {
			metadataBuilder.setId(Short.valueOf(idValue));

		} else if (globalMetadataInCollectionSchema != null) {
			metadataBuilder.setId(globalMetadataInCollectionSchema.getId());
		}

		Map<Language, String> xmlLabels = readLabels(metadataElement);
		if (inheriteGlobalMetadata && (xmlLabels == null || xmlLabels.isEmpty())) {
			metadataBuilder.setLabels(globalMetadataInCollectionSchema.getLabels());
		} else {
			metadataBuilder.setLabels(xmlLabels);
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

		String customAttributes = metadataElement.getAttributeValue("customAttributes");
		if (inheriteGlobalMetadata && customAttributes == null) {
			metadataBuilder.setCustomAttributes(globalMetadataInCollectionSchema.getCustomAttributes());
		} else {
			metadataBuilder.setCustomAttributes(parseCustomAttributes(customAttributes));
		}

		String essentialInSummaryStringValue = metadataElement.getAttributeValue("essentialInSummary");
		if (inheriteGlobalMetadata && essentialInSummaryStringValue == null) {
			metadataBuilder.setEssentialInSummary(globalMetadataInCollectionSchema.isEssentialInSummary());
		} else {
			metadataBuilder.setEssentialInSummary(readBooleanWithDefaultValue(essentialInSummaryStringValue, false));
		}

		String increasedDependencyLevelStringValue = metadataElement.getAttributeValue("increasedDependencyLevel");
		if (inheriteGlobalMetadata && increasedDependencyLevelStringValue == null) {
			metadataBuilder.setIncreasedDependencyLevel(globalMetadataInCollectionSchema.isIncreasedDependencyLevel());
		} else {
			metadataBuilder.setIncreasedDependencyLevel(readBooleanWithDefaultValue(increasedDependencyLevelStringValue, false));
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

		String transientStringValue = metadataElement.getAttributeValue("transiency");
		if (inheriteGlobalMetadata && transientStringValue == null) {
			metadataBuilder.setTransiency(globalMetadataInCollectionSchema.getTransiency());
		} else {
			metadataBuilder.setTransiency(readEnum(transientStringValue, MetadataTransiency.class));
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

		String markedForDeletion = metadataElement.getAttributeValue("markedForDeletion");
		if (inheriteGlobalMetadata && markedForDeletion == null) {
			metadataBuilder.setMarkedForDeletion(globalMetadataInCollectionSchema.isMarkedForDeletion());
		} else {
			metadataBuilder.setMarkedForDeletion(readBooleanWithDefaultValue(markedForDeletion, false));
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

		String providingSecurityStringValue = metadataElement.getAttributeValue("providingSecurity");
		if (inheriteGlobalMetadata && providingSecurityStringValue == null) {
			metadataBuilder.setRelationshipProvidingSecurity(globalMetadataInCollectionSchema.isRelationshipProvidingSecurity());
		} else {
			metadataBuilder.setRelationshipProvidingSecurity(readBooleanWithDefaultValue(providingSecurityStringValue, false));
		}

		String multivalueStringValue = metadataElement.getAttributeValue("multivalue");
		if (inheriteGlobalMetadata && multivalueStringValue == null) {
			metadataBuilder.setMultivalue(globalMetadataInCollectionSchema.isMultivalue());
		} else {
			metadataBuilder.setMultivalue(readBooleanWithDefaultValue(multivalueStringValue, false));
		}

		String multilingualStringValue = metadataElement.getAttributeValue("multilingual");
		if (inheriteGlobalMetadata && multilingualStringValue == null) {
			metadataBuilder.setMultiLingual(globalMetadataInCollectionSchema.isMultiLingual());
		} else {
			metadataBuilder.setMultiLingual(readBooleanWithDefaultValue(multilingualStringValue, false));
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

		Map<String, Object> customParameters = TypeConvertionUtil.getCustomParameterMap(metadataElement);
		if (inheriteGlobalMetadata && taxonomyRelationshipStringValue == null) {
			metadataBuilder.setCustomParameter(globalMetadataInCollectionSchema.getCustomParameter());
		} else {
			metadataBuilder.setCustomParameter(customParameters);
		}

		addReferencesToBuilder(metadataBuilder, metadataElement, globalMetadataInCollectionSchema);
	}

	private <T> T readEnum(String code, Class<T> clazz) {
		return (T) EnumWithSmallCodeUtils.toEnum((Class) clazz, code);
	}

	private void setPopulateConfigs(MetadataBuilder metadataBuilder, Element metadataElement) {

		Boolean isAddOnly = null;
		List<String> styles = new ArrayList<>();
		List<String> properties = new ArrayList<>();
		List<RegexConfig> regexes = new ArrayList<>();
		List<MetadataPopulator> metadataPopulators = new ArrayList<>();

		Element populateConfigsElement = metadataElement.getChild("populateConfigs");
		if (populateConfigsElement != null) {
			if (populateConfigsElement.getAttributeValue("isAddOnly") != null) {
				isAddOnly = true;
			}
			if (populateConfigsElement.getAttributeValue("styles") != null) {
				styles.addAll(asList(populateConfigsElement.getAttributeValue("styles").split(",")));
			}
			if (populateConfigsElement.getAttributeValue("properties") != null) {
				properties.addAll(asList(populateConfigsElement.getAttributeValue("properties").split(",")));
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
		metadataPopulateConfigsBuilder.setAddOnly(isAddOnly);
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
				metadataAccessRestrictionBuilder.getRequiredReadRoles().addAll(asList(
						accessRestrictionsElement.getAttributeValue("readAccessRestrictions").split(",")));
			}
			if (accessRestrictionsElement.getAttributeValue("writeAccessRestrictions") != null) {
				metadataAccessRestrictionBuilder.getRequiredWriteRoles().addAll(asList(
						accessRestrictionsElement.getAttributeValue("writeAccessRestrictions").split(",")));
			}
			if (accessRestrictionsElement.getAttributeValue("deleteAccessRestrictions") != null) {
				metadataAccessRestrictionBuilder.getRequiredDeleteRoles().addAll(asList(
						accessRestrictionsElement.getAttributeValue("deleteAccessRestrictions").split(",")));
			}
			if (accessRestrictionsElement.getAttributeValue("modifyAccessRestrictions") != null) {
				metadataAccessRestrictionBuilder.getRequiredModificationRoles().addAll(asList(
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
				return asList(strValue.split(","));
			}
		} else if (collectionSchemaBuilder != null) {
			return new ArrayList<>(collectionSchemaBuilder.defineValidators().getClassnames());
		} else {
			return new ArrayList<>();
		}

	}

	private Set<String> parseCustomAttributes(String customAttributes) {
		if (customAttributes != null && !customAttributes.isEmpty()) {
			String[] elements = customAttributes.split(",");

			Set<String> customAttributesSet = new HashSet<>();
			for (String element : elements) {
				customAttributesSet.add(element);
			}

			return customAttributesSet;
		}
		return new HashSet<>();
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
					List<String> schemas = asList(references.getAttributeValue("schemas").split(","));
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
				ParametrizedInstanceUtils utils = new ParametrizedInstanceUtils();
				if ("parametrized".equals(calculator)) {
					metadataBuilder.defineDataEntry().asCalculated(
							(MetadataValueCalculator<?>) utils.toObject(dataEntry, Parametrized.class));
				} else {
					metadataBuilder.defineDataEntry().asCalculated(calculator);
				}

			} else if (dataEntry.getAttributeValue("fixedSequenceCode") != null) {
				String fixedSequenceCode = dataEntry.getAttributeValue("fixedSequenceCode");
				metadataBuilder.defineDataEntry().asFixedSequence(fixedSequenceCode);

			} else if (dataEntry.getAttributeValue("metadataProvidingSequenceCode") != null) {
				String metadataProvidingSequenceCode = dataEntry.getAttributeValue("metadataProvidingSequenceCode");
				metadataBuilder.defineDataEntry().asSequenceDefinedByMetadata(metadataProvidingSequenceCode);

			} else if (dataEntry.getAttributeValue("agregationType") != null) {
				AggregationType aggregationType = (AggregationType)
						toEnum(AggregationType.class, dataEntry.getAttributeValue("agregationType"));

				String referenceMetadata = dataEntry.getAttributeValue("referenceMetadata");
				String inputMetadataStr = dataEntry.getAttributeValue("inputMetadata");

				Map<String, List<String>> inputMetadatasByRefMetadata = new HashMap<>();
				if (!isBlank(referenceMetadata)) {
					List<String> referenceMetadatas = asList(referenceMetadata.split(";"));
					List<String> inputMetadatas = !isBlank(inputMetadataStr) ?
												  asList(inputMetadataStr.split(";")) :
												  new ArrayList<String>();

					for (int i = 0; i < referenceMetadatas.size(); i++) {
						inputMetadatasByRefMetadata.put(referenceMetadatas.get(i),
								!inputMetadatas.isEmpty() ? asList(inputMetadatas.get(i).split(",")) :
								new ArrayList<String>());
					}
				}

				String calculatorClassName = dataEntry.getAttributeValue("aggregatedCalculator");
				if (calculatorClassName != null) {
					Class<? extends AggregatedCalculator<?>> calculatorClass;
					try {
						calculatorClass = classProvider.loadClass(calculatorClassName);
					} catch (ClassNotFoundException e) {
						throw new MetadataBuilderRuntimeException.CannotInstanciateClass(calculatorClassName, e);
					}
					metadataBuilder.defineDataEntry()
							.as(new AggregatedDataEntry(inputMetadatasByRefMetadata, aggregationType, calculatorClass));
				} else {
					metadataBuilder.defineDataEntry()
							.as(new AggregatedDataEntry(inputMetadatasByRefMetadata, aggregationType));
				}
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

		String id = defaultSchemaElement.getAttributeValue("id");
		if (id != null) {
			defaultSchemaBuilder.setId(Short.valueOf(id));
		}

		defaultSchemaBuilder.setLabels(readLabels(defaultSchemaElement));
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

	private Map<Language, String> readLabels(Element schemaElement) {
		Map<Language, String> labels = new HashMap<>();
		for (Attribute attribute : schemaElement.getAttributes()) {
			if (attribute.getName().startsWith("label_")) {
				labels.put(Language.withCode(attribute.getName().substring(6)), attribute.getValue());
			}
		}
		return labels;
	}


}
