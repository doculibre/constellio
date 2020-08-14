package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedDataEntry;
import com.constellio.app.services.importExport.settings.model.ImportedLabelTemplate;
import com.constellio.app.services.importExport.settings.model.ImportedMetadata;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataPopulateConfigs;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataSchema;
import com.constellio.app.services.importExport.settings.model.ImportedRegexConfigs;
import com.constellio.app.services.importExport.settings.model.ImportedSequence;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.model.ImportedSystemVersion;
import com.constellio.app.services.importExport.settings.model.ImportedTab;
import com.constellio.app.services.importExport.settings.model.ImportedTaxonomy;
import com.constellio.app.services.importExport.settings.model.ImportedType;
import com.constellio.app.services.importExport.settings.model.ImportedValueList;
import com.constellio.model.entities.Language;
import com.jgoodies.common.base.Strings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.join;

public class SettingsXMLFileWriter implements SettingsXMLFileConstants {

	private Document document;
	private Element settingsElement;

	public SettingsXMLFileWriter() {

		this.document = new Document();
		settingsElement = new Element(SETTINGS);
		document.setRootElement(settingsElement);
	}

	public Document getDocument() {
		return document;
	}

	public Document writeSettings(ImportedSettings importedSettings) {

		addSystemVersion(importedSettings.getImportedSystemVersion());

		addLabelTemplates(importedSettings.getImportedLabelTemplates());

		addGlobalConfigs(importedSettings.getConfigs());

		addSequences(importedSettings.getSequences());

		addCollectionsSettings(importedSettings.getCollectionsSettings());

		return document;

	}

	private void addSystemVersion(ImportedSystemVersion importedSystemVersion) {
		Element systemElem = new Element(SYSTEM);
		settingsElement.addContent(systemElem);
		addVersion(systemElem, importedSystemVersion);
		addPlugins(systemElem, importedSystemVersion.getPlugins());
		addOnlyUSRMode(systemElem, importedSystemVersion.isOnlyUSR());
	}

	private void addOnlyUSRMode(Element systemElem, boolean onlyUSR) {
		Element usrElement = new Element(ONLY_USR);
		usrElement.setAttribute(VALUE, onlyUSR + "");
		systemElem.addContent(usrElement);
	}

	private void addPlugins(Element systemElem, List<String> plugins) {
		Element pluginsElement = new Element(PLUGINS);
		systemElem.addContent(pluginsElement);
		for (String id : plugins) {
			Element pluginElement = new Element(PLUGIN);
			pluginElement.setAttribute(PLUGIN_ID, id);
			pluginsElement.addContent(pluginElement);
		}
	}

	private void addVersion(Element systemElem, ImportedSystemVersion importedSystemVersion) {
		Element versionElement = new Element(VERSION);
		versionElement.setAttribute(FULL, importedSystemVersion.getFullVersion());
		versionElement.setAttribute(MAJOR, importedSystemVersion.getMajorVersion() + "");
		versionElement.setAttribute(MINOR, importedSystemVersion.getMinorVersion() + "");
		versionElement.setAttribute(REVISION, importedSystemVersion.getMinorRevisionVersion() + "");
		systemElem.addContent(versionElement);
	}

	private void addLabelTemplates(List<ImportedLabelTemplate> importedLabelTemplates) {
		if (!importedLabelTemplates.isEmpty()) {
			Element labelTemplatesElement = new Element(LABEL_TEMPLATES);
			settingsElement.addContent(labelTemplatesElement);
			SAXBuilder builder = new SAXBuilder();

			for (ImportedLabelTemplate labelTemplate : importedLabelTemplates) {
				try {
					Document document = builder.build(new StringReader(labelTemplate.getXml()));
					Element element = document.getRootElement();
					element.detach();
					labelTemplatesElement.addContent(element);

				} catch (JDOMException | IOException e) {
					throw new RuntimeException(e);
				}

			}

		}
	}

	public void addSequences(List<ImportedSequence> sequences) {
		Element sequencesElem = new Element(SEQUENCES);
		settingsElement.addContent(sequencesElem);
		for (ImportedSequence importedSequence : sequences) {
			sequencesElem.addContent(buildSequenceElement(importedSequence));
		}
	}

	private Element buildSequenceElement(ImportedSequence importedSequence) {
		Element element = new Element(SEQUENCE);
		element.setAttribute(KEY, importedSequence.getKey());
		element.setAttribute(VALUE, importedSequence.getValue());
		return element;
	}

	public void addGlobalConfigs(List<ImportedConfig> configs) {
		Element configsElem = new Element(CONFIGS);
		settingsElement.addContent(configsElem);
		for (ImportedConfig importedConfig : configs) {
			addConfiguration(configsElem, importedConfig);
		}
	}

	private void addConfiguration(Element configsElem, ImportedConfig importedConfig) {
		Element configElem = new Element(CONFIG);
		configElem.setAttribute(KEY, importedConfig.getKey());
		configElem.setAttribute(VALUE, importedConfig.getValue());
		configsElem.addContent(configElem);
	}

	public void addCollectionsSettings(List<ImportedCollectionSettings> importedCollectionSettings) {
		if (importedCollectionSettings != null) {
			for (ImportedCollectionSettings collectionSettings : importedCollectionSettings) {
				addSettingsFor(collectionSettings);
			}
		}
	}

	private void addSettingsFor(ImportedCollectionSettings collectionSettings) {
		Element collectionSettingsElem = new Element(COLLECTION_SETTINGS);
		collectionSettingsElem.setAttribute(CODE, collectionSettings.getCode());
		settingsElement.addContent(collectionSettingsElem);

		addValueLists(collectionSettings, collectionSettingsElem);

		addTaxonomies(collectionSettings, collectionSettingsElem);

		addTypes(collectionSettings, collectionSettingsElem);
	}

	private void addTypes(ImportedCollectionSettings importedCollectionSettings, Element collectionSettingsElem) {
		Element typesElem = new Element(TYPES);
		collectionSettingsElem.addContent(typesElem);
		for (ImportedType importedType : importedCollectionSettings.getTypes()) {
			addImportedType(typesElem, importedType);
		}
	}

	private void addImportedType(Element typesElem, ImportedType importedType) {
		Element typeItem = new Element(TYPE);
		typeItem.setAttribute(CODE, importedType.getCode());
		setLabelsAttribute(typeItem, importedType.getLabels());
		typesElem.addContent(typeItem);

		addTabs(importedType, typeItem);

		addDefaultSchema(importedType, typeItem);

		addCustomSchemata(importedType, typeItem);
	}

	private void addCustomSchemata(ImportedType importedType, Element typeItem) {
		Element schemasElement = new Element(SCHEMAS);
		typeItem.addContent(schemasElement);

		for (ImportedMetadataSchema customSchema : importedType.getCustomSchemata()) {
			addSchemaItem(schemasElement, customSchema);
		}
	}

	private void addSchemaItem(Element schemasElement, ImportedMetadataSchema customSchema) {
		Element schemaElement = new Element(SCHEMA);
		schemaElement.setAttribute(CODE, customSchema.getCode());
		setLabelsAttribute(schemaElement, customSchema.getLabels());
		schemasElement.addContent(schemaElement);

		writeSchema(customSchema, schemaElement);
	}

	private void setLabelsAttribute(Element element, Map<Language, String> labels) {
		if (labels != null && labels.keySet() != null) {
			for (Language language : labels.keySet()) {
				if (Strings.isNotBlank(labels.get(language))) {
					element.setAttribute(LABEL + language.getCode(), labels.get(language));
				}
			}
		}
	}

	private void writeSchema(ImportedMetadataSchema metadataSchema, Element schemaElement) {
		setLabelsAttribute(schemaElement, metadataSchema.getLabels());
		if (metadataSchema.getTableMetadatas() != null && !metadataSchema.getFormMetadatas().isEmpty()) {
			schemaElement.setAttribute("formMetadatas", join(metadataSchema.getFormMetadatas(), ","));
		}
		if (metadataSchema.getDisplayMetadatas() != null && !metadataSchema.getDisplayMetadatas().isEmpty()) {
			schemaElement.setAttribute("displayMetadatas", join(metadataSchema.getDisplayMetadatas(), ","));
		}
		if (metadataSchema.getSearchMetadatas() != null && !metadataSchema.getSearchMetadatas().isEmpty()) {
			schemaElement.setAttribute("searchMetadatas", join(metadataSchema.getSearchMetadatas(), ","));
		}
		if (metadataSchema.getTableMetadatas() != null && !metadataSchema.getTableMetadatas().isEmpty()) {
			schemaElement.setAttribute("tableMetadatas", join(metadataSchema.getTableMetadatas(), ","));
		}
		for (ImportedMetadata importedMetadata : metadataSchema.getAllMetadata()) {
			addMetadatum(schemaElement, importedMetadata);
		}
	}

	private void addDefaultSchema(ImportedType importedType, Element typeItem) {
		if (importedType.getDefaultSchema() != null) {
			Element defaultSchemaElem = new Element(DEFAULT_SCHEMA);
			typeItem.addContent(defaultSchemaElem);

			ImportedMetadataSchema defaultSchema = importedType.getDefaultSchema();

			writeSchema(defaultSchema, defaultSchemaElem);
		}
	}

	private void addTabs(ImportedType importedType, Element typeItem) {
		Element tabsElement = new Element(TABS);
		typeItem.addContent(tabsElement);

		for (ImportedTab tab : importedType.getTabs()) {
			addTabItem(tabsElement, tab);
		}
	}

	private void addTabItem(Element tabsElement, ImportedTab tab) {
		if (StringUtils.isNotBlank(tab.getCode()) && StringUtils.isNotBlank(tab.getValue())) {
			Element tabElem = new Element(TAB);
			tabElem.setAttribute(CODE, tab.getCode());
			tabElem.setAttribute(VALUE, tab.getValue());
			tabsElement.addContent(tabElem);
		}
	}

	private void addMetadatum(Element defaultSchemaElem, ImportedMetadata importedMetadata) {
		Element metadataElem = new Element(METADATA);
		metadataElem.setAttribute(CODE, importedMetadata.getCode());
		setLabelsAttribute(metadataElem, importedMetadata.getLabels());
		if (StringUtils.isNotBlank(importedMetadata.getLabel())) {
			metadataElem.setAttribute(TITLE, importedMetadata.getLabel());
		}

		if (importedMetadata.getType() != null) {
			metadataElem.setAttribute(TYPE, importedMetadata.getType());
		}

		if (importedMetadata.getDuplicable() != null) {
			metadataElem.setAttribute(DUPLICABLE, importedMetadata.getDuplicable() + "");
		}

		if (importedMetadata.getRelationshipProvidingSecurity() != null) {
			metadataElem.setAttribute(RELATIONSHIP_PROVIDING_SECURITY, importedMetadata.getRelationshipProvidingSecurity() + "");
		}

		if (importedMetadata.getReferencedType() != null) {
			metadataElem.setAttribute(REFERENCED_TYPE, importedMetadata.getReferencedType() + "");
		}

		if (importedMetadata.getEnabled() != null) {
			metadataElem.setAttribute(ENABLED, importedMetadata.getEnabled() + "");
		}

		if (!importedMetadata.getEnabledIn().isEmpty()) {
			metadataElem.setAttribute(ENABLED_IN, join(importedMetadata.getEnabledIn(), ","));
		}

		if (StringUtils.isNotBlank(importedMetadata.getInputMask())) {
			metadataElem.setAttribute(INPUT_MASK, importedMetadata.getInputMask());
		}

		if (importedMetadata.getMultiLingual() != null) {
			metadataElem.setAttribute(MULTI_LINGUAL, importedMetadata.getMultiLingual() + "");
		}

		if (importedMetadata.getEncrypted() != null) {
			metadataElem.setAttribute(ENCRYPTED, importedMetadata.getEncrypted() + "");
		}

		if (importedMetadata.getEssential() != null) {
			metadataElem.setAttribute(ESSENTIAL, importedMetadata.getEssential() + "");
		}

		if (importedMetadata.getEssentialInSummary() != null) {
			metadataElem.setAttribute(ESSENTIAL_IN_SUMMARY, importedMetadata.getEssentialInSummary() + "");
		}

		if (importedMetadata.getRecordAutoComplete() != null) {
			metadataElem.setAttribute(RECORD_AUTOCOMPLETE, importedMetadata.getRecordAutoComplete() + "");
		}

		if (importedMetadata.getSearchable() != null) {
			metadataElem.setAttribute(SEARCHABLE, importedMetadata.getSearchable() + "");
		}

		if (importedMetadata.getSortable() != null) {
			metadataElem.setAttribute(SORTABLE, importedMetadata.getSortable() + "");
		}

		if (importedMetadata.getUnique() != null) {
			metadataElem.setAttribute(UNIQUE, importedMetadata.getUnique() + "");
		}

		if (importedMetadata.getSortingType() != null) {
			metadataElem.setAttribute(SORTING_TYPE, importedMetadata.getSortingType() + "");
		}

		if (importedMetadata.getUnmodifiable() != null) {
			metadataElem.setAttribute(UNMODIFIABLE, importedMetadata.getUnmodifiable() + "");
		}

		if (importedMetadata.getAdvanceSearchable() != null) {
			metadataElem.setAttribute(ADVANCE_SEARCHABLE, importedMetadata.getAdvanceSearchable() + "");
		}

		if (importedMetadata.getMultiValue() != null) {
			metadataElem.setAttribute(MULTI_VALUE, importedMetadata.getMultiValue() + "");
		}

		if (importedMetadata.getRequired() != null) {
			metadataElem.setAttribute(REQUIRED, importedMetadata.getRequired() + "");
		}

		if (!importedMetadata.getRequiredIn().isEmpty()) {
			metadataElem.setAttribute(REQUIRED_IN, join(importedMetadata.getRequiredIn(), ","));
		}

		if (StringUtils.isNotBlank(importedMetadata.getTab())) {
			metadataElem.setAttribute(TAB, importedMetadata.getTab());
		}

		if (importedMetadata.getVisibleInDisplay() != null) {
			metadataElem.setAttribute(VISIBLE_IN_DISPLAY, importedMetadata.getVisibleInDisplay() + "");
		}

		if (!importedMetadata.getVisibleInDisplayIn().isEmpty()) {
			metadataElem.setAttribute(VISIBLE_IN_DISPLAY_IN, join(importedMetadata.getVisibleInDisplayIn(), ","));
		}

		if (importedMetadata.getVisibleInForm() != null) {
			metadataElem.setAttribute(VISIBLE_IN_FORM, importedMetadata.getVisibleInForm() + "");
		}

		if (!importedMetadata.getVisibleInFormIn().isEmpty()) {
			metadataElem.setAttribute(VISIBLE_IN_FORM_IN, join(importedMetadata.getVisibleInFormIn(), ","));
		}

		if (!importedMetadata.getVisibleInResultIn().isEmpty()) {
			metadataElem.setAttribute(VISIBLE_IN_RESULT_IN, join(importedMetadata.getVisibleInResultIn(), ","));
		}

		if (importedMetadata.getVisibleInSearchResult() != null) {
			metadataElem.setAttribute(VISIBLE_IN_SEARCH_RESULT, importedMetadata.getVisibleInSearchResult() + "");
		}

		if (importedMetadata.getVisibleInTables() != null) {
			metadataElem.setAttribute(VISIBLE_IN_TABLES, importedMetadata.getVisibleInTables() + "");
		}

		if (!importedMetadata.getVisibleInTablesIn().isEmpty()) {
			metadataElem.setAttribute(VISIBLE_IN_TABLES_IN, join(importedMetadata.getVisibleInTablesIn(), ","));
		}

		if (importedMetadata.getPopulateConfigs() != null) {
			metadataElem.addContent(writePopulateConfigElement(importedMetadata.getPopulateConfigs()));
		}

		if (importedMetadata.getRequiredReadRoles() != null && importedMetadata.getRequiredReadRoles().size() > 0) {
			metadataElem.setAttribute(REQUIRED_READ_ROLES, join(importedMetadata.getRequiredReadRoles(), ","));
		}

		if (importedMetadata.getDataEntry() != null) {

			ImportedDataEntry dataEntry = importedMetadata.getDataEntry();

			Element dataEntryElem = new Element("data-entry");
			dataEntryElem.setAttribute("type", dataEntry.getType());
			metadataElem.addContent(dataEntryElem);

			switch (dataEntry.getType()) {
				case "calculated":
					if (StringUtils.isNotBlank(dataEntry.getCalculator())) {
						dataEntryElem.setAttribute("calculator", dataEntry.getCalculator());
					}
					break;

				case "copied":
					if (StringUtils.isNotBlank(dataEntry.getReferencedMetadata())) {
						dataEntryElem.setAttribute("referenceMetadata", dataEntry.getReferencedMetadata());
					}

					if (StringUtils.isNotBlank(dataEntry.getReferencedMetadata())) {
						dataEntryElem.setAttribute("copiedMetadata", dataEntry.getCopiedMetadata());
					}
					break;

				case "jexl":
					if (StringUtils.isNotBlank(dataEntry.getPattern())) {
						dataEntryElem.setText(dataEntry.getPattern());
					}
					break;

				case "sequence":
					if (StringUtils.isNotBlank(dataEntry.getFixedSequenceCode())) {
						dataEntryElem.setAttribute("fixedSequenceCode", dataEntry.getFixedSequenceCode());
					} else {
						dataEntryElem.setAttribute("metadataProvidingSequenceCode", dataEntry.getMetadataProvidingSequenceCode());
					}
					break;

				default:
					break;
			}

		}

		defaultSchemaElem.addContent(metadataElem);
	}

	private Element writePopulateConfigElement(ImportedMetadataPopulateConfigs populateConfigs) {

		Element element = new Element(POPULATE_CONFIGS);
		for (ImportedRegexConfigs regexPopulateConfigs : populateConfigs.getRegexes()) {
			Element elementConfigsRegex = new Element(POPULATE_CONFIGS_REGEX);
			element.addContent(elementConfigsRegex);

			elementConfigsRegex.setAttribute(POPULATE_CONFIGS_REGEX_INPUT_METADATA, regexPopulateConfigs.getInputMetadata());
			elementConfigsRegex.setAttribute(POPULATE_CONFIGS_REGEX_PATTERN, regexPopulateConfigs.getRegex());
			elementConfigsRegex.setAttribute(POPULATE_CONFIGS_REGEX_VALUE, regexPopulateConfigs.getValue());
			elementConfigsRegex.setAttribute(POPULATE_CONFIGS_REGEX_TYPE, regexPopulateConfigs.getRegexConfigType());
		}

		return element;
	}

	private void addTaxonomies(ImportedCollectionSettings importedCollectionSettings, Element collectionSettingsElem) {
		Element taxonomiesElem = new Element(TAXONOMIES);
		collectionSettingsElem.addContent(taxonomiesElem);
		for (ImportedTaxonomy importedTaxonomy : importedCollectionSettings.getTaxonomies()) {
			addTaxonomy(taxonomiesElem, importedTaxonomy);
		}
	}

	private void addTaxonomy(Element taxonomiesElem, ImportedTaxonomy importedTaxonomy) {
		Element listElem = new Element(TAXONOMY);
		listElem.setAttribute(CODE, importedTaxonomy.getCode());
		if (importedTaxonomy.getTitle() != null && importedTaxonomy.getTitleLanguage() != null) {
			for (Language language : importedTaxonomy.getTitleLanguage()) {
				listElem.setAttribute(TITLE + language.getCode(), importedTaxonomy.getTitle(language));
			}
		}
		if (importedTaxonomy.getVisibleOnHomePage() != null) {
			listElem.setAttribute(VISIBLE_IN_HOME_PAGE, importedTaxonomy.getVisibleOnHomePage() + "");
		}
		listElem.setAttribute(CLASSIFIED_TYPES, join(importedTaxonomy.getClassifiedTypes(), ','));
		listElem.setAttribute(GROUPS, join(importedTaxonomy.getGroupIds(), ','));
		listElem.setAttribute(USERS, join(importedTaxonomy.getUserIds(), ','));

		taxonomiesElem.addContent(listElem);
	}

	private void addValueLists(ImportedCollectionSettings importedCollectionSettings, Element collectionSettingsElem) {
		Element valueListsElem = new Element(VALUE_LISTS);
		collectionSettingsElem.addContent(valueListsElem);
		for (ImportedValueList valueList : importedCollectionSettings.getValueLists()) {
			addValueListItem(valueListsElem, valueList);
		}
	}

	private void addValueListItem(Element valueListsElem, ImportedValueList valueList) {
		Element listElem = new Element(VALUE_LIST);
		listElem.setAttribute(CODE, valueList.getCode());


		if (valueList.getTitle() != null && valueList.getTitle().keySet() != null) {
			for (Language language : valueList.getTitle().keySet()) {
				listElem.setAttribute(TITLE + language.getCode(), valueList.getTitle().get(language));
			}
		}

		if (!valueList.getClassifiedTypes().isEmpty()) {
			listElem.setAttribute(CLASSIFIED_TYPES, join(valueList.getClassifiedTypes(), ','));
		}

		if (StringUtils.isNotBlank(valueList.getCodeMode())) {
			listElem.setAttribute(CODE_MODE, valueList.getCodeMode());
		}

		if (valueList.getHierarchical() != null) {
			listElem.setAttribute(HIERARCHICAL, valueList.getHierarchical() + "");
		}

		valueListsElem.addContent(listElem);
	}

	public static void writeToFile(ImportedSettings settings, File file)
			throws IOException {

		Document document = new SettingsXMLFileWriter().writeSettings(settings);
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());

		FileOutputStream fos = new FileOutputStream(file);
		try {
			xmlOutput.output(document, fos);

		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

}