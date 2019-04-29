package com.constellio.model.services.schemas.xml;

import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.MetadataNetwork;
import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.extractions.DefaultMetadataPopulatorPersistenceManager;
import com.constellio.model.services.records.extractions.MetadataPopulator;
import com.constellio.model.services.records.extractions.MetadataPopulatorPersistenceManager;
import com.constellio.model.services.schemas.builders.ClassListBuilder;
import com.constellio.model.utils.Parametrized;
import com.constellio.model.utils.ParametrizedInstanceUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MetadataSchemaXMLWriter3 {

	public static final String FORMAT_ATTRIBUTE = "format";
	public static final String FORMAT_VERSION = MetadataSchemaXMLReader3.FORMAT_VERSION;
	private static final Logger LOGGER = LoggerFactory.getLogger(DataLayerLogger.class);
	private final MetadataPopulatorPersistenceManager metadataPopulatorXMLSerializer = new DefaultMetadataPopulatorPersistenceManager();

	public void writeEmptyDocument(CollectionInfo collectionInfo, Document document) {
		writeSchemaTypes(new MetadataSchemaTypes(collectionInfo, 0, new ArrayList<MetadataSchemaType>(), new ArrayList<String>(),
				new ArrayList<String>(), Arrays.asList(Language.French, Language.English), MetadataNetwork.EMPTY()), document);
	}

	public Document write(MetadataSchemaTypes schemaTypes) {
		Document document = new Document();
		writeSchemaTypes(schemaTypes, document);
		document.getRootElement().setAttribute(FORMAT_ATTRIBUTE, FORMAT_VERSION);
		return document;
	}

	private void writeCustomSchemas(MetadataSchemaType schemaType, Element schemaTypeElement,
									MetadataSchema collectionSchema) {
		Element customSchemasElement = new Element("customSchemas");
		for (MetadataSchema schema : schemaType.getCustomSchemas()) {
			Element schemaElement = toXMLElement(schema, collectionSchema, schemaType.getCollectionInfo());
			customSchemasElement.addContent(schemaElement);
		}

		schemaTypeElement.addContent(customSchemasElement);
	}

	private void writeDefaultSchema(MetadataSchemaType schemaType, Element schemaTypeElement,
									MetadataSchema collectionSchema) {
		MetadataSchema defaultSchema = schemaType.getDefaultSchema();
		Element defaultSchemaElement = new Element("defaultSchema");
		defaultSchemaElement.setAttribute("code", "" + defaultSchema.getLocalCode());
		defaultSchemaElement.setAttribute("id", writeShort(defaultSchema.getId()));
		writeLabels(defaultSchemaElement, defaultSchema.getLabels(), schemaType.getCollectionInfo());
		//		defaultSchemaElement.setAttribute("label", "" + defaultSchema.getLabel());
		addAllMetadataToSchema(collectionSchema, defaultSchema, defaultSchemaElement, schemaType.getCollectionInfo());
		if (!defaultSchema.getValidators().isEmpty()) {
			defaultSchemaElement.addContent(writeSchemaValidators(defaultSchema));
		}
		schemaTypeElement.addContent(defaultSchemaElement);
	}

	private void addAllMetadataToSchema(MetadataSchema collectionSchema, MetadataSchema defaultSchema,
										Element defaultSchemaElement, CollectionInfo collectionInfo) {
		for (Metadata metadata : defaultSchema.getMetadatas()) {
			Metadata metadataInCollectionSchema = null;
			if (collectionSchema != null && Schemas.isGlobalMetadata(metadata.getLocalCode())
				&& collectionSchema.hasMetadataWithCode(metadata.getLocalCode())) {
				metadataInCollectionSchema = collectionSchema.getMetadata(metadata.getLocalCode());
			}


			addMetadataToSchema(defaultSchemaElement, metadata, metadataInCollectionSchema, collectionInfo);
		}
	}

	private Element writeSchemaValidators(MetadataSchema defaultSchema) {
		Element validatorsElement = new Element("validators");
		List<String> validatorsClassNames = new ArrayList<>();
		for (RecordValidator validator : defaultSchema.getValidators()) {
			validatorsClassNames.add(validator.getClass().getName());
		}
		validatorsElement.setAttribute("classNames", toCommaSeparatedString(validatorsClassNames));
		return validatorsElement;
	}

	private Element writeSchemaTypes(MetadataSchemaTypes schemaTypes, Document document) {
		Element schemaTypesElement = new Element("types");
		document.setRootElement(schemaTypesElement);
		schemaTypesElement.setAttribute("version", "" + schemaTypes.getVersion());
		schemaTypesElement.setAttribute("languages", "" + toCommaSeparatedString(toLanguagesCodes(schemaTypes)));

		MetadataSchema collectionSchema = null;
		if (schemaTypes.hasType(Collection.SCHEMA_TYPE)) {
			MetadataSchemaType collectionSchemaType = schemaTypes.getSchemaType(Collection.SCHEMA_TYPE);
			schemaTypesElement.addContent(writeSchemaType(collectionSchemaType, null));
			collectionSchema = collectionSchemaType.getDefaultSchema();
		}

		for (MetadataSchemaType schemaType : schemaTypes.getSchemaTypes()) {
			if (!schemaType.getCode().equals(Collection.SCHEMA_TYPE)) {
				schemaTypesElement.addContent(writeSchemaType(schemaType, collectionSchema));
			}
		}

		return schemaTypesElement;
	}

	private List<String> toLanguagesCodes(MetadataSchemaTypes schemaTypes) {
		List<String> languages = new ArrayList<>();
		for (Language language : schemaTypes.getLanguages()) {
			languages.add(language.getCode());
		}
		return languages;
	}

	private Element writeSchemaType(MetadataSchemaType schemaType, MetadataSchema collectionSchema) {
		Element schemaTypeElement = new Element("type");
		schemaTypeElement.setAttribute("code", schemaType.getCode());
		schemaTypeElement.setAttribute("id", writeShort(schemaType.getId()));

		writeLabels(schemaTypeElement, schemaType.getLabels(), schemaType.getCollectionInfo());
		if (schemaType.hasSecurity()) {
			schemaTypeElement.setAttribute("security", writeBoolean(schemaType.hasSecurity()));
		}
		if (schemaType.isReadOnlyLocked()) {
			schemaTypeElement.setAttribute("readOnlyLocked", writeBoolean(schemaType.isReadOnlyLocked()));
		}

		if (schemaType.getSmallCode() != null) {
			schemaTypeElement.setAttribute("smallCode", schemaType.getSmallCode());
		}

		if (schemaType.getDataStore() != null && !schemaType.getDataStore().equals("records")) {
			schemaTypeElement.setAttribute("dataStore", schemaType.getDataStore());
		}

		schemaTypeElement.setAttribute("inTransactionLog", writeBoolean(schemaType.isInTransactionLog()));
		writeDefaultSchema(schemaType, schemaTypeElement, collectionSchema);
		writeCustomSchemas(schemaType, schemaTypeElement, collectionSchema);
		return schemaTypeElement;
	}

	private Element toXMLElement(MetadataSchema schema, MetadataSchema collectionSchema,
								 CollectionInfo collectionInfo) {
		Element schemaElement = new Element("schema");
		schemaElement.setAttribute("code", schema.getLocalCode());
		schemaElement.setAttribute("id", writeShort(schema.getId()));
		writeLabels(schemaElement, schema.getLabels(), schema.getCollectionInfo());
		schemaElement.setAttribute("undeletable", writeBoolean(schema.isUndeletable()));
		schemaElement.setAttribute("active", writeBoolean(schema.isActive()));
		addAllMetadataToSchema(collectionSchema, schema, schemaElement, collectionInfo);
		if (!schema.getValidators().isEmpty()) {
			schemaElement.addContent(writeSchemaValidators(schema));
		}
		return schemaElement;
	}

	private void writeLabels(Element schemaElement, Map<Language, String> labels, CollectionInfo collectionInfo) {

		for (Language language : Language.values()) {
			if (labels.containsKey(language) && collectionInfo.getCollectionLocales().contains(language.getLocale())) {
				schemaElement.setAttribute("label_" + language.getCode(), labels.get(language));
			}
		}
	}

	private void addMetadataToSchema(Element schemaElement, Metadata metadata,
									 Metadata globalMetadataInCollectionSchema, CollectionInfo collectionInfo) {
		ParametrizedInstanceUtils utils = new ParametrizedInstanceUtils();
		Element metadataElement = new Element("m");
		metadataElement.setAttribute("code", metadata.getLocalCode());

		boolean differentFromInheritance;

		String localCode = metadata.getLocalCode();
		boolean notUserDefinedMetadata = !localCode.startsWith("USR");

		if (metadata.inheritDefaultSchema() && !metadata.getSchemaCode().contains("default")) {
			differentFromInheritance = writeMetadataWithInheritance(metadata, metadataElement, collectionInfo);
		} else {

			if (globalMetadataInCollectionSchema == null) {
				writeMetadataWithoutInheritance(metadata, utils, metadataElement, notUserDefinedMetadata, collectionInfo);
				differentFromInheritance = true;
			} else {
				writeGlobalMetadataWithoutInheritance(metadata, utils, metadataElement,
						notUserDefinedMetadata, globalMetadataInCollectionSchema, collectionInfo);
				differentFromInheritance = true;
			}
		}

		if (differentFromInheritance) {
			schemaElement.addContent(metadataElement);
		}
	}

	private void writeMetadataWithoutInheritance(Metadata metadata, ParametrizedInstanceUtils utils,
												 Element metadataElement,
												 boolean notUserDefinedMetadata, CollectionInfo collectionInfo) {
		writeLabels(metadataElement, metadata.getLabels(), collectionInfo);

		if (!metadata.isGlobal()) {
			metadataElement.setAttribute("id", writeShort(metadata.getId()));
		}
		if (!metadata.isEnabled()) {
			metadataElement.setAttribute("enabled", writeBoolean(metadata.isEnabled()));
		}
		if (!metadata.isUndeletable()) {
			metadataElement.setAttribute("undeletable", writeBoolean(metadata.isUndeletable()));
		}
		if (metadata.isMultivalue()) {
			metadataElement.setAttribute("multivalue", writeBoolean(metadata.isMultivalue()));
		}
		if (metadata.isMultiLingual()) {
			metadataElement.setAttribute("multilingual", writeBoolean(metadata.isMultiLingual()));
		}
		if (metadata.isSearchable()) {
			metadataElement.setAttribute("searchable", writeBoolean(metadata.isSearchable()));
		}
		if (metadata.isSortable()) {
			metadataElement.setAttribute("sortable", writeBoolean(metadata.isSortable()));
		}
		if (metadata.isSchemaAutocomplete()) {
			metadataElement.setAttribute("schemaAutocomplete", writeBoolean(metadata.isSchemaAutocomplete()));
		}
		if (notUserDefinedMetadata && !metadata.isSystemReserved()) {
			metadataElement.setAttribute("systemReserved", writeBoolean(metadata.isSystemReserved()));
		}
		if (metadata.isEssential()) {
			metadataElement.setAttribute("essential", writeBoolean(metadata.isEssential()));
		}
		if (metadata.isEssentialInSummary()) {
			metadataElement.setAttribute("essentialInSummary", writeBoolean(metadata.isEssentialInSummary()));
		}
		if (metadata.isIncreasedDependencyLevel()) {
			metadataElement.setAttribute("increasedDependencyLevel", writeBoolean(metadata.isIncreasedDependencyLevel()));
		}
		if (metadata.isEncrypted()) {
			metadataElement.setAttribute("encrypted", writeBoolean(metadata.isEncrypted()));
		}
		if (metadata.getTransiency() != null && metadata.getTransiency() != MetadataTransiency.PERSISTED) {
			metadataElement.setAttribute("transiency", writeEnum(metadata.getTransiency()));
		}
		if (metadata.isChildOfRelationship()) {
			metadataElement.setAttribute("childOfRelationship", writeBoolean(metadata.isChildOfRelationship()));
		}
		if (metadata.isTaxonomyRelationship()) {
			metadataElement.setAttribute("taxonomyRelationship", writeBoolean(metadata.isTaxonomyRelationship()));
		}
		if (metadata.isRelationshipProvidingSecurity()) {
			metadataElement.setAttribute("providingSecurity", writeBoolean(metadata.isRelationshipProvidingSecurity()));
		}
		if (metadata.isUniqueValue()) {
			metadataElement.setAttribute("uniqueValue", writeBoolean(metadata.isUniqueValue()));
		}
		if (metadata.isMarkedForDeletion()) {
			metadataElement.setAttribute("markedForDeletion", writeBoolean(metadata.isMarkedForDeletion()));
		}
		if (metadata.isUnmodifiable()) {
			metadataElement.setAttribute("unmodifiable", writeBoolean(metadata.isUnmodifiable()));
		}
		metadataElement.setAttribute("type", metadata.getType().name());
		if (metadata.isDefaultRequirement()) {
			metadataElement.setAttribute("defaultRequirement", writeBoolean(metadata.isDefaultRequirement()));
		}
		if (metadata.inheritDefaultSchema()) {
			metadataElement.setAttribute("inheriting", writeBoolean(metadata.inheritDefaultSchema()));
		}
		if (metadata.getStructureFactory() != null) {
			metadataElement.setAttribute("structureFactory", metadata.getStructureFactory().getClass().getName());
		}
		if (metadata.getEnumClass() != null) {
			metadataElement.setAttribute("enumClass", metadata.getEnumClass().getName());
		}
		if (metadata.getInputMask() != null) {
			metadataElement.setAttribute("inputMask", metadata.getInputMask());
		}
		if (!metadata.getAccessRestrictions().isEmpty()) {
			metadataElement.addContent(toAccessRestrictionsElement(metadata.getAccessRestrictions()));
		}
		if (metadata.getAllowedReferences() != null) {
			metadataElement.addContent(toRefencesElement(metadata.getAllowedReferences()));
		}
		if (metadata.getDefaultValue() != null) {
			utils.toElement(metadata.getDefaultValue(), metadataElement, "defaultValue");
		}
		if (!metadata.inheritDefaultSchema()) {
			Element dataEntryElement = toDataEntryElement(metadata.getDataEntry());
			if (dataEntryElement != null) {
				metadataElement.addContent(dataEntryElement);
			}
		}
		if (!metadata.getValidators().isEmpty()) {
			metadataElement.addContent(writeRecordMetadataValidators(metadata));
		}

		if (metadata.getCustomParameter() != null && !metadata.getCustomParameter().isEmpty()) {
			metadataElement.addContent(toCustomParameterElement(metadata));
		}

		if (!metadata.getPopulateConfigs().isEmpty()) {
			metadataElement.addContent(toPopulateConfigsElement(metadata.getPopulateConfigs()));
		}
		if (metadata.isDuplicable()) {
			metadataElement.setAttribute("duplicable", writeBoolean(metadata.isDuplicable()));
		}
		// TODO VALIDER SI CORRECT
		if (metadata.getCustomAttributes() != null && !metadata.getCustomAttributes().isEmpty()) {
			List<String> attributesList = new ArrayList<String>(metadata.getCustomAttributes());
			metadataElement.setAttribute("customAttributes", toCommaSeparatedString(attributesList));
		}
	}

	private boolean writeGlobalMetadataWithoutInheritance(Metadata metadata, ParametrizedInstanceUtils utils,
														  Element metadataElement, boolean notUserDefinedMetadata,
														  Metadata globalMetadataInCollection,
														  CollectionInfo collectionInfo) {

		boolean different = false;
		if (!globalMetadataInCollection.getLabels().equals(metadata.getLabels())) {
			writeLabels(metadataElement, metadata.getLabels(), collectionInfo);
			different = true;
		}

		if (globalMetadataInCollection.isEnabled() != metadata.isEnabled()) {
			metadataElement.setAttribute("enabled", writeBoolean(metadata.isEnabled()));
			different = true;
		}
		if (globalMetadataInCollection.isUndeletable() != metadata.isUndeletable()) {
			metadataElement.setAttribute("undeletable", writeBoolean(metadata.isUndeletable()));
			different = true;
		}
		if (globalMetadataInCollection.isMultivalue() != metadata.isMultivalue()) {
			metadataElement.setAttribute("multivalue", writeBoolean(metadata.isMultivalue()));
			different = true;
		}
		if (globalMetadataInCollection.isMultiLingual() != metadata.isMultiLingual()) {
			metadataElement.setAttribute("multilingual", writeBoolean(metadata.isMultiLingual()));
			different = true;
		}
		if (globalMetadataInCollection.isSearchable() != metadata.isSearchable()) {
			metadataElement.setAttribute("searchable", writeBoolean(metadata.isSearchable()));
			different = true;
		}
		if (globalMetadataInCollection.isSortable() != metadata.isSortable()) {
			metadataElement.setAttribute("sortable", writeBoolean(metadata.isSortable()));
			different = true;
		}
		if (globalMetadataInCollection.isSchemaAutocomplete() != metadata.isSchemaAutocomplete()) {
			metadataElement.setAttribute("schemaAutocomplete", writeBoolean(metadata.isSchemaAutocomplete()));
			different = true;
		}
		if (globalMetadataInCollection.isSystemReserved() != metadata.isSystemReserved()) {
			metadataElement.setAttribute("systemReserved", writeBoolean(metadata.isSystemReserved()));
			different = true;
		}
		if (globalMetadataInCollection.isEssential() != metadata.isEssential()) {
			metadataElement.setAttribute("essential", writeBoolean(metadata.isEssential()));
			different = true;
		}
		// TODO VALIDER POUR SAVOIR SI CORRECT
		if (!globalMetadataInCollection.getCustomAttributes().containsAll(metadata.getCustomAttributes())) {
			List<String> attributesList = new ArrayList<String>(metadata.getCustomAttributes());
			metadataElement.setAttribute("customAttributes", toCommaSeparatedString(attributesList));
			different = true;
		}

		if (globalMetadataInCollection.isEssentialInSummary() != metadata.isEssentialInSummary()) {
			metadataElement.setAttribute("essentialInSummary", writeBoolean(metadata.isEssentialInSummary()));
			different = true;
		}
		if (globalMetadataInCollection.isIncreasedDependencyLevel() != metadata.isIncreasedDependencyLevel()) {
			metadataElement.setAttribute("increasedDependencyLevel", writeBoolean(metadata.isIncreasedDependencyLevel()));
			different = true;
		}
		if (globalMetadataInCollection.isEncrypted() != metadata.isEncrypted()) {
			metadataElement.setAttribute("encrypted", writeBoolean(metadata.isEncrypted()));
			different = true;
		}

		if (globalMetadataInCollection.getTransiency() != metadata.getTransiency()) {
			metadataElement.setAttribute("transiency", writeEnum(metadata.getTransiency()));
			different = true;
		}
		if (globalMetadataInCollection.isChildOfRelationship() != metadata.isChildOfRelationship()) {
			metadataElement.setAttribute("childOfRelationship", writeBoolean(metadata.isChildOfRelationship()));
			different = true;
		}
		if (globalMetadataInCollection.isTaxonomyRelationship() != metadata.isTaxonomyRelationship()) {
			metadataElement.setAttribute("taxonomyRelationship", writeBoolean(metadata.isTaxonomyRelationship()));
			different = true;
		}
		if (globalMetadataInCollection.isUniqueValue() != metadata.isUniqueValue()) {
			metadataElement.setAttribute("uniqueValue", writeBoolean(metadata.isUniqueValue()));
			different = true;
		}
		if (globalMetadataInCollection.isUnmodifiable() != metadata.isUnmodifiable()) {
			metadataElement.setAttribute("unmodifiable", writeBoolean(metadata.isUnmodifiable()));
			different = true;
		}
		if (metadata.getType() != globalMetadataInCollection.getType()) {
			metadataElement.setAttribute("type", metadata.getType().name());
			different = true;
		}

		if (metadata.getInputMask() != null && !metadata.getInputMask().equals(globalMetadataInCollection.getInputMask())) {
			metadataElement.setAttribute("inputMask", metadata.getInputMask());
			different = true;
		}

		if (globalMetadataInCollection.isDefaultRequirement() != metadata.isDefaultRequirement()) {
			metadataElement.setAttribute("defaultRequirement", writeBoolean(metadata.isDefaultRequirement()));
			different = true;
		}
		if (metadata.getStructureFactory() != null && !metadata.getStructureFactory().getClass()
				.equals(globalMetadataInCollection.getStructureFactory().getClass())) {
			metadataElement.setAttribute("structureFactory", metadata.getStructureFactory().getClass().getName());
			different = true;
		}
		if (metadata.getEnumClass() != null && !metadata.getEnumClass()
				.equals(globalMetadataInCollection.getEnumClass())) {
			metadataElement.setAttribute("enumClass", metadata.getEnumClass().getName());
			different = true;
		}
		if (metadata.getAccessRestrictions() != null
			&& !metadata.getAccessRestrictions().equals(globalMetadataInCollection.getAccessRestrictions())) {
			metadataElement.addContent(toAccessRestrictionsElement(metadata.getAccessRestrictions()));
			different = true;
		}
		if (metadata.getAllowedReferences() != null
			&& !metadata.getAllowedReferences().equals(globalMetadataInCollection.getAccessRestrictions())) {
			metadataElement.addContent(toRefencesElement(metadata.getAllowedReferences()));
			different = true;
		}
		if (metadata.getDefaultValue() != null
			&& !metadata.getDefaultValue().equals(globalMetadataInCollection.getDefaultValue())) {
			utils.toElement(metadata.getDefaultValue(), metadataElement, "defaultValue");
			different = true;
		}
		if (!metadata.getDataEntry().equals(globalMetadataInCollection.getDataEntry())) {
			Element dataEntryElement = toDataEntryElement(metadata.getDataEntry());
			if (dataEntryElement != null) {
				metadataElement.addContent(dataEntryElement);
				different = true;
			}

		}
		if (metadata.getValidators() != null
			&& !ClassListBuilder.isSameValues(metadata.getValidators(), globalMetadataInCollection.getValidators())) {
			metadataElement.addContent(writeRecordMetadataValidators(metadata));
			different = true;
		}
		if (!metadata.getPopulateConfigs().isEmpty()) {
			metadataElement.addContent(toPopulateConfigsElement(metadata.getPopulateConfigs()));
			different = true;
		}
		if (globalMetadataInCollection.isDuplicable() != metadata.isDuplicable()) {
			metadataElement.setAttribute("duplicable", writeBoolean(metadata.isDuplicable()));
			different = true;
		}

		return different;
	}

	private boolean writeMetadataWithInheritance(Metadata metadata, Element metadataElement,
												 CollectionInfo collectionInfo) {
		boolean differentFromInheritance = false;
		if (metadata.getInheritance().isDefaultRequirement() != metadata.isDefaultRequirement()) {
			metadataElement.setAttribute("defaultRequirement", writeBoolean(metadata.isDefaultRequirement()));
			differentFromInheritance = true;
		}
		if (metadata.getInheritance().isEnabled() != metadata.isEnabled()) {
			metadataElement.setAttribute("enabled", writeBoolean(metadata.isEnabled()));
			differentFromInheritance = true;
		}
		if (!metadata.getValidators().equals(metadata.getInheritance().getValidators())) {
			metadataElement.addContent(writeRecordMetadataValidators(metadata));
			differentFromInheritance = true;
		}
		if (!metadata.getPopulateConfigs().equals(metadata.getInheritance().getPopulateConfigs())) {
			metadataElement.addContent(toPopulateConfigsElement(metadata.getPopulateConfigs()));
			differentFromInheritance = true;
		}
		if (metadata.getInputMask() != null && !metadata.getInputMask().equals(metadata.getInheritance().getInputMask())) {
			metadataElement.setAttribute("inputMask", metadata.getInputMask());
			differentFromInheritance = true;
		}
		if (metadata.getLabels() != null && !metadata.getLabels().isEmpty() && !metadata.getLabels()
				.equals(metadata.getInheritance().getLabels())) {
			writeLabels(metadataElement, metadata.getLabels(), collectionInfo);
			differentFromInheritance = true;
		}
		if (metadata.getDefaultValue() != null && !metadata.getDefaultValue()
				.equals(metadata.getInheritance().getDefaultValue())) {
			ParametrizedInstanceUtils utils = new ParametrizedInstanceUtils();
			utils.toElement(metadata.getDefaultValue(), metadataElement, "defaultValue");
			differentFromInheritance = true;
		}
		if (metadata.getInheritance().isDuplicable() != metadata.isDuplicable()) {
			metadataElement.setAttribute("duplicable", writeBoolean(metadata.isDuplicable()));
			differentFromInheritance = true;
		}

		if (metadata.getCustomParameter() != null && metadata.getCustomParameter().size() > 0) {
			metadataElement.addContent(toCustomParameterElement(metadata));
			differentFromInheritance = true;
		}

		return differentFromInheritance;
	}

	private String writeBoolean(boolean enabled) {
		return enabled ? "t" : "f";
	}

	private String writeShort(short id) {
		return "" + id;
	}

	private String writeEnum(EnumWithSmallCode value) {
		return value.getCode();
	}

	private Element toCustomParameterElement(Metadata metadata) {

		Element customElement = new Element("customParameter");
		for (String keyToCustomParameter : metadata.getCustomParameter().keySet()) {
			Map<String, Object> customParameter = metadata.getCustomParameter();

			Object value = customParameter.get(keyToCustomParameter);

			Element element = TypeConvertionUtil.getElement(keyToCustomParameter, value);
			if (element != null) {
				customElement.addContent(element);
			}
		}

		return customElement;
	}

	private Element toPopulateConfigsElement(MetadataPopulateConfigs populateConfigs) {
		Element populateConfigsElement = new Element("populateConfigs");

		if (Boolean.TRUE.equals(populateConfigs.isAddOnly())) {
			populateConfigsElement.setAttribute("isAddOnly", "true");
		}

		if (!populateConfigs.getStyles().isEmpty()) {
			populateConfigsElement.setAttribute("styles", toCommaSeparatedString(populateConfigs.getStyles()));
		}
		if (!populateConfigs.getProperties().isEmpty()) {
			populateConfigsElement.setAttribute("properties", toCommaSeparatedString(populateConfigs.getProperties()));
		}

		Element regexConfigsElement = new Element("regexConfigs");
		for (RegexConfig regexConfig : populateConfigs.getRegexes()) {
			regexConfigsElement.addContent(toRegexElement(regexConfig));
		}
		if (!populateConfigs.getRegexes().isEmpty()) {
			populateConfigsElement.addContent(regexConfigsElement);
		}

		Element metadataPopulatorsElement = new Element("metadataPopulators");
		for (MetadataPopulator metadataPopulator : populateConfigs.getMetadataPopulators()) {
			try {
				Element element = metadataPopulatorXMLSerializer.toXml(metadataPopulator);
				metadataPopulatorsElement.addContent(element);
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("Cannot save a metadata populator", e);
			}
		}
		if (!populateConfigs.getMetadataPopulators().isEmpty()) {
			populateConfigsElement.addContent(metadataPopulatorsElement);
		}

		return populateConfigsElement;
	}

	private Element toRegexElement(RegexConfig regexConfig) {
		Element regexConfigElement = new Element("regexConfig");

		regexConfigElement.setAttribute("metadataInput", regexConfig.getInputMetadata());
		regexConfigElement.setAttribute("regex", regexConfig.getRegex().pattern());
		regexConfigElement.setAttribute("value", regexConfig.getValue());
		regexConfigElement.setAttribute("regexConfigType", "" + regexConfig.getRegexConfigType());

		return regexConfigElement;
	}

	private Element toAccessRestrictionsElement(MetadataAccessRestriction accessRestrictions) {
		Element accessRestrictionsElement = new Element("accessRestrictions");

		if (!accessRestrictions.getRequiredReadRoles().isEmpty()) {
			accessRestrictionsElement
					.setAttribute("readAccessRestrictions", toCommaSeparatedString(accessRestrictions.getRequiredReadRoles()));
		}
		if (!accessRestrictions.getRequiredWriteRoles().isEmpty()) {
			accessRestrictionsElement
					.setAttribute("writeAccessRestrictions", toCommaSeparatedString(accessRestrictions.getRequiredWriteRoles()));
		}
		if (!accessRestrictions.getRequiredDeleteRoles().isEmpty()) {
			accessRestrictionsElement
					.setAttribute("deleteAccessRestrictions",
							toCommaSeparatedString(accessRestrictions.getRequiredDeleteRoles()));
		}
		if (!accessRestrictions.getRequiredModificationRoles().isEmpty()) {
			accessRestrictionsElement
					.setAttribute("modifyAccessRestrictions",
							toCommaSeparatedString(accessRestrictions.getRequiredModificationRoles()));
		}
		return accessRestrictionsElement;
	}

	private Element writeRecordMetadataValidators(Metadata metadata) {
		Element validatorsElement = new Element("validators");
		List<String> validatorsClassNames = new ArrayList<>();
		for (RecordMetadataValidator<?> validator : metadata.getValidators()) {
			validatorsClassNames.add(validator.getClass().getName());
		}
		validatorsElement.setAttribute("classNames", toCommaSeparatedString(validatorsClassNames));
		return validatorsElement;
	}

	private Element toRefencesElement(AllowedReferences allowedReferences) {
		Element references = new Element("references");
		if (allowedReferences.getAllowedSchemas().isEmpty()) {
			references.setAttribute("schemaType", allowedReferences.getAllowedSchemaType());
		} else {
			references.setAttribute("schemas", toCommaSeparatedString(allowedReferences.getAllowedSchemas()));
		}
		return references;
	}

	private String toCommaSeparatedString(java.util.Collection<String> values) {
		StringBuffer stringBuffer = new StringBuffer();
		for (String value : values) {
			stringBuffer.append(value).append(",");
		}
		if (!values.isEmpty()) {
			stringBuffer.deleteCharAt(stringBuffer.lastIndexOf(","));
		}
		return stringBuffer.toString();
	}

	private Element toDataEntryElement(DataEntry dataEntryValue) {
		if (dataEntryValue.getType() == DataEntryType.MANUAL) {
			return null;
		}
		Element dataEntry = new Element("dataEntry");
		if (dataEntryValue.getType() == DataEntryType.COPIED) {
			CopiedDataEntry copiedDataEntry = (CopiedDataEntry) dataEntryValue;
			dataEntry.setAttribute("copied", copiedDataEntry.getCopiedMetadata());
			dataEntry.setAttribute("reference", copiedDataEntry.getReferenceMetadata());

		} else if (dataEntryValue.getType() == DataEntryType.CALCULATED) {
			CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) dataEntryValue;
			MetadataValueCalculator calculator = calculatedDataEntry.getCalculator();
			if (calculatedDataEntry.getCalculator() instanceof Parametrized) {
				dataEntry = new ParametrizedInstanceUtils().toElement((Parametrized) calculator, "dataEntry");
				dataEntry.setAttribute("calculator", "parametrized");

			} else {
				dataEntry.setAttribute("calculator", calculatedDataEntry.getCalculator().getClass().getName());
			}

		} else if (dataEntryValue.getType() == DataEntryType.SEQUENCE) {
			SequenceDataEntry sequenceDataEntry = (SequenceDataEntry) dataEntryValue;
			if (sequenceDataEntry.getFixedSequenceCode() != null) {
				dataEntry.setAttribute("fixedSequenceCode", sequenceDataEntry.getFixedSequenceCode());
			} else {
				dataEntry.setAttribute("metadataProvidingSequenceCode", sequenceDataEntry.getMetadataProvidingSequenceCode());
			}

		} else if (dataEntryValue.getType() == DataEntryType.AGGREGATED) {
			AggregatedDataEntry agregatedDataEntry = (AggregatedDataEntry) dataEntryValue;
			dataEntry.setAttribute("agregationType", agregatedDataEntry.getAgregationType().getCode());

			Map<String, List<String>> metadatasByRefMetadata = agregatedDataEntry.getInputMetadatasByReferenceMetadata();

			String referenceMetadata = StringUtils.join(metadatasByRefMetadata.keySet(), ";");
			dataEntry.setAttribute("referenceMetadata", referenceMetadata);

			List<String> inputMetadataStr = new ArrayList<>();
			for (List<String> inputMetadatas : metadatasByRefMetadata.values()) {
				inputMetadataStr.add(StringUtils.join(inputMetadatas, ","));
			}
			if (!inputMetadataStr.isEmpty()) {
				String inputMetadatasStr = StringUtils.join(inputMetadataStr, ";");
				dataEntry.setAttribute("inputMetadata", inputMetadatasStr);
			}
			if (agregatedDataEntry.getAggregatedCalculator() != null) {
				dataEntry.setAttribute("aggregatedCalculator", agregatedDataEntry.getAggregatedCalculator().getName());
			}
		}

		return dataEntry;
	}
}
