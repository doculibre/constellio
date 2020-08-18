package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.SettingsExportServices;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedDataEntry;
import com.constellio.app.services.importExport.settings.model.ImportedLabelTemplate;
import com.constellio.app.services.importExport.settings.model.ImportedMetadata;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataSchema;
import com.constellio.app.services.importExport.settings.model.ImportedRegexConfigs;
import com.constellio.app.services.importExport.settings.model.ImportedSequence;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.model.ImportedSystemVersion;
import com.constellio.app.services.importExport.settings.model.ImportedTab;
import com.constellio.app.services.importExport.settings.model.ImportedTaxonomy;
import com.constellio.app.services.importExport.settings.model.ImportedType;
import com.constellio.app.services.importExport.settings.model.ImportedValueList;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException;
import com.constellio.model.entities.Language;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.jgoodies.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SettingsXMLFileReader implements SettingsXMLFileConstants {

	public static final String VISIBLE_IN_HOME_PAGE = "visibleInHomePage";
	public static final String USERS = "users";
	public static final String GROUPS = "groups";
	public static final String CLASSIFIED_TYPES = "classifiedTypes";
	public static final String TITLE = "title";
	public static final String SEQUENCES = "sequences";

	private String currentCollection = null;
	private Document document;
	private ModelLayerFactory modelLayerFactory;

	public SettingsXMLFileReader(Document document, String currentCollection, ModelLayerFactory modelLayerFactory) {
		this.document = document;
		this.modelLayerFactory = modelLayerFactory;
		this.currentCollection = currentCollection;
	}

	public ImportedSettings read() {

		Element rootNode = document.getRootElement();

		ImportedSettings importedSettings = new ImportedSettings()
				.setImportedSystemVersion(readSystemVersion(rootNode.getChild(SYSTEM)))
				.setImportedLabelTemplates(readLabelTemplates(rootNode.getChild(LABEL_TEMPLATES)))
				.setConfigs(readConfigs(rootNode.getChild(CONFIGS)))
				.setImportedSequences(readSequences(rootNode.getChild(SEQUENCES)))
				.setCollectionsSettings(readCollectionSettings(rootNode.getChildren(COLLECTION_SETTINGS)));

		return importedSettings;
	}

	private ImportedSystemVersion readSystemVersion(Element importedSystemVersionLabel) {

		ImportedSystemVersion importedSystemVersion = new ImportedSystemVersion();
		List<String> plugins = new ArrayList<>();
		if (importedSystemVersionLabel != null) {
			Element elementVersion = importedSystemVersionLabel.getChild(VERSION);
			Element elementPlugins = importedSystemVersionLabel.getChild(PLUGINS);
			Element elementUSR = importedSystemVersionLabel.getChild(ONLY_USR);

			importedSystemVersion.setFullVersion(elementVersion.getAttributeValue(FULL));
			importedSystemVersion.setMajorVersion(Integer.parseInt(elementVersion.getAttributeValue(MAJOR)));
			importedSystemVersion.setMinorVersion(Integer.parseInt(elementVersion.getAttributeValue(MINOR)));
			importedSystemVersion.setMinorRevisionVersion(Integer.parseInt(elementVersion.getAttributeValue(REVISION)));
			importedSystemVersion.setOnlyUSR(Boolean.parseBoolean(elementUSR.getAttributeValue(VALUE)));

			for (Element plugin : elementPlugins.getChildren()) {
				plugins.add(plugin.getAttributeValue(PLUGIN_ID));
			}
			importedSystemVersion.setPlugins(plugins);
		}

		return importedSystemVersion;
	}

	private List<ImportedLabelTemplate> readLabelTemplates(Element importedLabelTemplatesElement) {
		List<ImportedLabelTemplate> templates = new ArrayList<>();
		if (importedLabelTemplatesElement != null) {
			for (Element element : importedLabelTemplatesElement.getChildren()) {
				XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
				String xml = xmlOutput.outputString(element).replace(" />", "/>");
				templates.add(new ImportedLabelTemplate(xml));
			}
		}
		return templates;
	}

	private List<ImportedSequence> readSequences(Element sequencesElement) {
		List<ImportedSequence> sequences = new ArrayList<>();
		for (Element child : sequencesElement.getChildren()) {
			ImportedSequence importedSequence = new ImportedSequence();
			importedSequence.setKey(child.getAttributeValue("key"));
			importedSequence.setValue(child.getAttributeValue("value"));
			sequences.add(importedSequence);
		}
		return sequences;
	}

	private List<ImportedCollectionSettings> readCollectionSettings(List<Element> elements) {
		List<ImportedCollectionSettings> collectionSettings = new ArrayList<>();
		for (Element collectionElement : elements) {
			collectionSettings.add(readCollectionSetting(collectionElement));
		}
		return collectionSettings;
	}

	private ImportedCollectionSettings readCollectionSetting(Element collectionElement) {
		String collectionCode = collectionElement.getAttributeValue(CODE);
		if (SettingsExportServices.CURRENT_COLLECTION_IMPORTATION_MODE.equals(collectionCode)) {
			collectionCode = currentCollection;
			if (currentCollection == null) {
				throw new RuntimeException(
						"ERROR: tried to import settings as currentCollection but currentCollection was not set");
			}
		}

		List<String> language;

		if (modelLayerFactory.getCollectionsListManager().getCollections().contains(collectionCode)) {
			language = modelLayerFactory.getCollectionsListManager().getCollectionLanguages(collectionCode);
		} else {
			language = Collections.singletonList(modelLayerFactory.getConfiguration().getMainDataLanguage());
		}
		if (language == null || language.size() == 0) {
			language = new ArrayList<>();
			language.add(modelLayerFactory.getCollectionsListManager().getMainDataLanguage());
		}

		return new ImportedCollectionSettings().setCode(collectionCode)
				.setValueLists(getCollectionValueLists(collectionElement.getChild(VALUE_LISTS), language))
				.setTaxonomies(getCollectionTaxonomies(collectionElement.getChild(TAXONOMIES), language))
				.setTypes(getCollectionTypes(collectionElement.getChild(TYPES), language));
	}

	private List<ImportedType> getCollectionTypes(Element typesElement, List<String> language) {
		List<ImportedType> types = new ArrayList<>();
		for (Element typeElement : typesElement.getChildren()) {
			types.add(readType(typeElement, language));
		}
		return types;
	}

	private ImportedType readType(Element typeElement, List<String> language) {
		ImportedType type = new ImportedType()
				.setCode(typeElement.getAttributeValue(CODE))
				.setTabs(getTabs(typeElement.getChild(TABS)))
				.setDefaultSchema(readDefaultSchema(typeElement.getChild(DEFAULT_SCHEMA), language))
				.setCustomSchemata(readCustomSchemata(typeElement.getChild("schemas"), language));
		String label = typeElement.getAttributeValue(LABEL);
		Map<Language, String> languageTitleMap = getLanguageStringMap(typeElement, language, label, LABEL);
		if (languageTitleMap.size() > 0) {
			type.setLabels(languageTitleMap);
		}
		return type;
	}

	private List<ImportedMetadataSchema> readCustomSchemata(Element schemataElement, List<String> language) {
		List<ImportedMetadataSchema> schemata = new ArrayList<>();
		for (Element schemaElement : schemataElement.getChildren()) {
			ImportedMetadataSchema importedMetadataSchema = new ImportedMetadataSchema();
			if (schemaElement.getAttribute("code") != null) {
				importedMetadataSchema.setCode(schemaElement.getAttributeValue("code"));
			}
			readSchema(schemaElement, importedMetadataSchema, language);
			schemata.add(importedMetadataSchema);
		}
		return schemata;
	}

	private ImportedMetadataSchema readDefaultSchema(Element schemaElement, List<String> language) {
		ImportedMetadataSchema importedMetadataSchema = new ImportedMetadataSchema().setCode("default");

		readSchema(schemaElement, importedMetadataSchema, language);
		return importedMetadataSchema;
	}

	private void readSchema(Element schemaElement, ImportedMetadataSchema importedMetadataSchema,
							List<String> language) {
		String label = schemaElement.getAttributeValue(LABEL);
		Map<Language, String> languageTitleMap = getLanguageStringMap(schemaElement, language, label, LABEL);
		if (languageTitleMap.size() > 0) {
			importedMetadataSchema.setLabels(languageTitleMap);
		}
		importedMetadataSchema.setFormMetadatas(toList(schemaElement.getAttributeValue("formMetadatas")));
		importedMetadataSchema.setDisplayMetadatas(toList(schemaElement.getAttributeValue("displayMetadatas")));
		importedMetadataSchema.setSearchMetadatas(toList(schemaElement.getAttributeValue("searchMetadatas")));
		importedMetadataSchema.setTableMetadatas(toList(schemaElement.getAttributeValue("tableMetadatas")));

		importedMetadataSchema.setAllMetadatas(readMetadata(schemaElement.getChildren(METADATA), language));
	}

	private List<String> toList(String listOfItems) {
		if (listOfItems == null || listOfItems.isEmpty()) {
			return new ArrayList<>();
		} else {
			return asList(listOfItems.split(","));
		}
	}

	private List<ImportedMetadata> readMetadata(List<Element> elements, List<String> languageList) {
		List<ImportedMetadata> metadata = new ArrayList<>();
		for (Element element : elements) {
			metadata.add(readMetadata(element, languageList));
		}
		return metadata;
	}

	private ImportedMetadata readMetadata(Element element, List<String> languageList) {

		ImportedMetadata importedMetadata = new ImportedMetadata();
		importedMetadata.setCode(element.getAttributeValue(CODE));
		importedMetadata.setType(element.getAttributeValue(TYPE));
		importedMetadata.setDataEntry(readDataEntry(element));
		String label = element.getAttributeValue(LABEL);

		Map<Language, String> languageTitleMap = getLanguageStringMap(element, languageList, label, LABEL);

		if (languageTitleMap.size() > 0) {
			importedMetadata.setLabels(languageTitleMap);
		}

		if (element.getAttribute(SEARCHABLE) != null) {
			importedMetadata.setSearchable(Boolean.parseBoolean(element.getAttributeValue(SEARCHABLE)));
		}

		if (element.getAttribute(ADVANCE_SEARCHABLE) != null) {
			importedMetadata.setAdvanceSearchable(Boolean.parseBoolean(element.getAttributeValue(ADVANCE_SEARCHABLE)));
		}

		if (element.getAttribute(UNMODIFIABLE) != null) {
			importedMetadata.setUnmodifiable(Boolean.parseBoolean(element.getAttributeValue(UNMODIFIABLE)));
		}

		if (element.getAttribute(UNIQUE) != null) {
			importedMetadata.setUnique(Boolean.parseBoolean(element.getAttributeValue(UNIQUE)));
		}

		if (element.getAttribute(SORTING_TYPE) != null) {
			importedMetadata.setSortingType(element.getAttributeValue(SORTING_TYPE));
		}

		if (element.getAttribute(SORTABLE) != null) {
			importedMetadata.setSortable(Boolean.parseBoolean(element.getAttributeValue(SORTABLE)));
		}

		if (element.getAttribute(RECORD_AUTOCOMPLETE) != null) {
			importedMetadata.setRecordAutoComplete(Boolean.parseBoolean(element.getAttributeValue(RECORD_AUTOCOMPLETE)));
		}

		if (element.getAttribute(ESSENTIAL) != null) {
			importedMetadata.setEssential(Boolean.parseBoolean(element.getAttributeValue(ESSENTIAL)));
		}

		if (element.getAttribute(ESSENTIAL_IN_SUMMARY) != null) {
			importedMetadata.setEssentialInSummary(Boolean.parseBoolean(element.getAttributeValue(ESSENTIAL_IN_SUMMARY)));
		}

		if (element.getAttribute(ENCRYPTED) != null) {
			importedMetadata.setEncrypted(Boolean.parseBoolean(element.getAttributeValue(ENCRYPTED)));
		}

		if (element.getAttribute(MULTI_LINGUAL) != null) {
			importedMetadata.setMultiLingual(Boolean.parseBoolean(element.getAttributeValue(MULTI_LINGUAL)));
		}

		if (element.getAttribute(DUPLICABLE) != null) {
			importedMetadata.setDuplicable(Boolean.parseBoolean(element.getAttributeValue(DUPLICABLE)));
		}

		if (element.getAttribute(RELATIONSHIP_PROVIDING_SECURITY) != null) {
			importedMetadata.setRelationshipProvidingSecurity(
					Boolean.parseBoolean(element.getAttributeValue(RELATIONSHIP_PROVIDING_SECURITY)));
		}

		if (element.getAttribute(REFERENCED_TYPE) != null) {
			importedMetadata.setReferencedType(element.getAttributeValue(REFERENCED_TYPE));
		}

		if (element.getAttribute(ENABLED) != null) {
			importedMetadata.setEnabled(Boolean.parseBoolean(element.getAttributeValue(ENABLED)));
		}

		if (element.getAttribute(ENABLED_IN) != null &&
			isNotBlank(element.getAttributeValue(ENABLED_IN))) {
			importedMetadata.setEnabledIn(toListOfString(element.getAttributeValue(ENABLED_IN)));
		}

		if (element.getAttribute(INPUT_MASK) != null &&
			isNotBlank(element.getAttributeValue(INPUT_MASK))) {
			importedMetadata.setInputMask(element.getAttributeValue(INPUT_MASK));
		}

		if (element.getAttribute(MULTI_VALUE) != null) {
			importedMetadata.setMultiValue(Boolean.parseBoolean(element.getAttributeValue(MULTI_VALUE)));
		}

		if (element.getAttribute(REQUIRED) != null) {
			importedMetadata.setRequired(Boolean.parseBoolean(element.getAttributeValue(REQUIRED)));
		}

		if (element.getAttribute(REQUIRED_IN) != null &&
			isNotBlank(element.getAttributeValue(REQUIRED_IN))) {
			importedMetadata.setRequiredIn(toListOfString(element.getAttributeValue(REQUIRED_IN)));
		}

		if (element.getAttribute(TAB) != null) {
			importedMetadata.setTab(element.getAttributeValue(TAB));
		}

		if (element.getAttributeValue(VISIBLE_IN_DISPLAY) != null) {
			importedMetadata.setVisibleInDisplay(Boolean.parseBoolean(element.getAttributeValue(VISIBLE_IN_DISPLAY)));
		}

		if (element.getAttribute(VISIBLE_IN_DISPLAY_IN) != null) {
			importedMetadata.setVisibleInDisplayIn(toListOfString(element.getAttributeValue(VISIBLE_IN_DISPLAY_IN)));
		}

		if (element.getAttribute(VISIBLE_IN_FORM) != null) {
			importedMetadata.setVisibleInForm(Boolean.parseBoolean(element.getAttributeValue(VISIBLE_IN_FORM)));
		}

		if (element.getAttribute(VISIBLE_IN_FORM_IN) != null) {
			importedMetadata.setVisibleInFormIn(toListOfString(element.getAttributeValue(VISIBLE_IN_FORM_IN)));
		}

		if (element.getAttribute(VISIBLE_IN_RESULT_IN) != null &&
			isNotBlank(element.getAttributeValue(VISIBLE_IN_RESULT_IN))) {
			importedMetadata.setVisibleInResultIn(toListOfString(element.getAttributeValue(VISIBLE_IN_RESULT_IN)));
		}

		if (element.getAttributeValue(VISIBLE_IN_SEARCH_RESULT) != null) {
			importedMetadata.setVisibleInSearchResult(Boolean.parseBoolean(element.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)));
		}

		if (element.getAttribute(VISIBLE_IN_TABLES) != null) {
			importedMetadata.setVisibleInTables(Boolean.parseBoolean(element.getAttributeValue(VISIBLE_IN_TABLES)));
		}

		if (element.getAttribute(VISIBLE_IN_TABLES_IN) != null &&
			isNotBlank(element.getAttributeValue(VISIBLE_IN_TABLES_IN))) {
			importedMetadata.setVisibleInTablesIn(toListOfString(element.getAttributeValue(VISIBLE_IN_TABLES_IN)));
		}

		if (element.getAttribute(REQUIRED_READ_ROLES) != null) {
			importedMetadata.setRequiredReadRoles(toListOfString(element.getAttributeValue(REQUIRED_READ_ROLES)));
		}

		if (element.getChild(POPULATE_CONFIGS) != null) {
			importedMetadata.newPopulateConfigs();
			for (Element regexElement : element.getChild(POPULATE_CONFIGS).getChildren(POPULATE_CONFIGS_REGEX)) {
				ImportedRegexConfigs configs = new ImportedRegexConfigs();
				configs.setInputMetadata(regexElement.getAttributeValue(POPULATE_CONFIGS_REGEX_INPUT_METADATA));
				configs.setRegex(regexElement.getAttributeValue(POPULATE_CONFIGS_REGEX_PATTERN));
				configs.setValue(regexElement.getAttributeValue(POPULATE_CONFIGS_REGEX_VALUE));
				configs.setRegexConfigType(regexElement.getAttributeValue(POPULATE_CONFIGS_REGEX_TYPE));
				importedMetadata.getPopulateConfigs().addRegexConfigs(configs);
			}
		}

		return importedMetadata;
	}

	private List<ImportedTab> getTabs(Element tabsElement) {
		List<ImportedTab> tabs = new ArrayList<>();
		for (Element child : tabsElement.getChildren()) {
			tabs.add(readTab(child));
		}
		return tabs;
	}

	private ImportedTab readTab(Element element) {
		return new ImportedTab().setCode(element.getAttributeValue(CODE))
				.setValue(element.getAttributeValue("value"));
	}

	private List<ImportedTaxonomy> getCollectionTaxonomies(Element element, List<String> languageList) {
		List<ImportedTaxonomy> taxonomies = new ArrayList<>();
		for (Element child : element.getChildren()) {
			taxonomies.add(readTaxonomy(child, languageList));
		}
		return taxonomies;
	}

	private ImportedTaxonomy readTaxonomy(Element child, List<String> languageList) {
		ImportedTaxonomy taxonomy = new ImportedTaxonomy();
		taxonomy.setCode(child.getAttributeValue(CODE));
		String title = child.getAttributeValue(TITLE);

		Map<Language, String> languageTitleMap = getLanguageStringMap(child, languageList, title, TITLE);

		if (languageTitleMap.size() > 0) {
			taxonomy.setTitle(languageTitleMap);
		}

		if (child.getAttribute(CLASSIFIED_TYPES) != null) {
			taxonomy.setClassifiedTypes(getValueListClassifiedTypes(child.getAttributeValue(CLASSIFIED_TYPES)));
		}

		if (child.getAttribute(GROUPS) != null) {
			taxonomy.setGroupIds(toListOfString(child.getAttributeValue(GROUPS)));
		}

		if (child.getAttribute(USERS) != null) {
			taxonomy.setUserIds(toListOfString(child.getAttributeValue(USERS)));
		}

		if (child.getAttribute(VISIBLE_IN_HOME_PAGE) != null) {
			taxonomy.setVisibleOnHomePage(Boolean.valueOf(child.getAttributeValue(VISIBLE_IN_HOME_PAGE)));
		}

		return taxonomy;
	}

	@NotNull
	private Map<Language, String> getLanguageStringMap(Element child, List<String> languageList, String label,
													   String labelFinal) {
		Map<Language, String> languageTitleMap = new HashMap<>();
		int numberOfLang = 0;

		List<Attribute> attributeList = child.getAttributes();

		for (Attribute currentAttribute : attributeList) {
			if (currentAttribute.getName().startsWith(labelFinal) && currentAttribute.getName().length() > labelFinal.length()) {
				String languageCode = currentAttribute.getName().replace(labelFinal, "");
				Language language = Language.withCode(languageCode);
				languageTitleMap.put(language, currentAttribute.getValue());
				numberOfLang++;
			}
		}

		String title = child.getAttributeValue("title");

		if (numberOfLang == 0 && Strings.isNotBlank(label)) {
			for (String languageCollection : languageList) {
				Language language = Language.withCode(languageCollection);
				languageTitleMap.put(language, label);
			}
		} else if (numberOfLang == 0 && Strings.isNotBlank(title)) {
			for (String languageCollection : languageList) {
				Language language = Language.withCode(languageCollection);
				languageTitleMap.put(language, title);
			}
		}
		return languageTitleMap;
	}

	private List<String> toListOfString(String stringValue) {
		if (isNotBlank(stringValue)) {
			return asList(StringUtils.split(stringValue, ","));
		}
		return new ArrayList<>();
	}

	private List<ImportedValueList> getCollectionValueLists(Element valueListsElement, List<String> languages) {
		List<ImportedValueList> importedValueLists = new ArrayList<>();
		for (Element element : valueListsElement.getChildren()) {
			importedValueLists.add(readValueList(element, languages));
		}
		return importedValueLists;
	}

	private ImportedDataEntry readDataEntry(Element metadataElement) {
		Element dataEntryElem = metadataElement.getChild("data-entry");

		ImportedDataEntry dataEntry = null;

		if (dataEntryElem != null && dataEntryElem.getAttributeValue("type") != null) {
			switch (dataEntryElem.getAttributeValue("type")) {
				case "calculated":
					dataEntry = ImportedDataEntry.asCalculated(dataEntryElem.getAttributeValue("calculator"));
					break;

				case "copied":
					dataEntry = ImportedDataEntry.asCopied(
							dataEntryElem.getAttributeValue("referenceMetadata"),
							dataEntryElem.getAttributeValue("copiedMetadata"));
					break;

				case "jexl":
					dataEntry = ImportedDataEntry.asJEXLScript(dataEntryElem.getText());
					break;

				case "sequence":
					String fixedSequenceCode = dataEntryElem.getAttributeValue("fixedSequenceCode");
					String metadataProvidingSequenceCode = dataEntryElem.getAttributeValue("metadataProvidingSequenceCode");
					if (fixedSequenceCode != null) {
						dataEntry = ImportedDataEntry.asFixedSequence(fixedSequenceCode);
					} else {
						dataEntry = ImportedDataEntry.asMetadataProvidingSequence(metadataProvidingSequenceCode);
					}

					break;

				default:
					break;
			}
		}
		return dataEntry;
	}

	private ImportedValueList readValueList(Element element, List<String> languageList) {
		ImportedValueList valueList = new ImportedValueList()
				.setCode(element.getAttributeValue(CODE));

		String title = element.getAttributeValue(TITLE);

		Map<Language, String> languageTitleMap = getLanguageStringMap(element, languageList, title, TITLE);

		if (languageTitleMap.size() > 0) {
			valueList.setTitle(languageTitleMap);
		}

		if (element.getAttribute(CLASSIFIED_TYPES) != null) {
			valueList.setClassifiedTypes(getValueListClassifiedTypes(element.getAttributeValue(CLASSIFIED_TYPES)));
		}

		if (element.getAttribute("codeMode") != null) {
			valueList.setCodeMode(element.getAttributeValue("codeMode"));
		}

		if (element.getAttribute("hierarchical") != null) {
			valueList.setHierarchical(Boolean.valueOf(element.getAttributeValue("hierarchical")));
		}

		return valueList;
	}

	private List<String> getValueListClassifiedTypes(String stringValue) {
		List<String> classifiedTypes = new ArrayList<>();
		if (isNotBlank(stringValue)) {
			classifiedTypes.addAll(asList(StringUtils.split(stringValue, ',')));
		}
		return classifiedTypes;
	}

	private List<ImportedConfig> readConfigs(Element configs) {
		List<ImportedConfig> importedConfigs = new ArrayList<>();
		for (Element childElement : configs.getChildren()) {
			importedConfigs.add(readConfig(childElement));
		}
		return importedConfigs;
	}

	private ImportedConfig readConfig(Element childElement) {
		return new ImportedConfig()
				.setKey(childElement.getAttributeValue("key"))
				.setValue(childElement.getAttributeValue("value"));
	}

	public static ImportedSettings readFromFile(File file, String currentCollection,
												ModelLayerFactory modelLayerFactory) {

		SAXBuilder builder = new SAXBuilder();
		Document document;
		try {
			document = builder.build(file);
		} catch (JDOMException e) {
			throw new ConfigManagerRuntimeException("JDOM2 Exception", e);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("build Document JDOM2 from file", e);
		}

		SettingsXMLFileReader reader = new SettingsXMLFileReader(document, currentCollection, modelLayerFactory);

		return reader.read();

	}
}
