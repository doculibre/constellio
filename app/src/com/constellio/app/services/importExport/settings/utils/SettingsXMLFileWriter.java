package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.model.*;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

public class SettingsXMLFileWriter extends SettingsXMLFileConstants {

    public static final String DEFAULT_SCHEMA = "default-schema";
    public static final String METADATA = "metadata";
    public static final String LABEL = "label";
    public static final String SCHEMA = "schema";
    public static final String SCHEMAS = "schemas";
    public static final String DUPLICABLE = "duplicable";
    public static final String MULTI_LINGUAL = "multiLingual";
    public static final String ESSENTIAL_IN_SUMMARY = "essentialInSummary";
    public static final String ESSENTIAL = "essential";
    public static final String RECORD_AUTOCOMPLETE = "recordAutocomplete";
    public static final String SORTABLE = "sortable";
    public static final String UNMODIFIABLE = "unmodifiable";
    public static final String ADVANCE_SEARCHABLE = "advanceSearchable";
    public static final String SEARCHABLE = "searchable";
    public static final String INPUT_MASK = "inputMask";
    public static final String BEHAVIOURS = "behaviours";
    public static final String MULTI_VALUE = "multiValue";
    public static final String VISIBLE_IN_TABLES_IN = "visibleInTablesIn";
    public static final String VISIBLE_IN_TABLES = "visibleInTables";
    public static final String VISIBLE_IN_RESULT_IN = "visibleInResultIn";
    public static final String VISIBLE_IN_DISPLAY_IN = "visibleInDisplayIn";
    public static final String VISIBLE_IN_SEARCH_RESULT = "visibleInSearchResult";
    public static final String VISIBLE_IN_DISPLAY = "visibleInDisplay";
    public static final String VISIBLE_IN_FORM_IN = "visibleInFormIn";
    public static final String VISIBLE_IN_FORM = "visibleInForm";
    public static final String REQUIRED_IN = "requiredIn";
    public static final String REQUIRED = "required";
    public static final String ENABLED_IN = "enabledIn";
    public static final String ENABLED = "enabled";
    public static final String UNIQUE = "unique";
    public static final String ENCRYPTED = "encrypted";

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

        addGlobalConfigs(importedSettings.getConfigs());

        addCollectionsSettings(importedSettings.getCollectionsConfigs());

        return document;

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
        if (StringUtils.isNotBlank(importedType.getLabel())) {
            typeItem.setAttribute(LABEL, importedType.getLabel());
        }
        typesElem.addContent(typeItem);

        addTabs(importedType, typeItem);

        addDefaultSchema(importedType, typeItem);

        addCustomSchemata(importedType, typeItem);
    }

    private void addCustomSchemata(ImportedType importedType, Element typeItem) {
        Element schemasElement = new Element(SCHEMAS);
        typeItem.addContent(schemasElement);

        for (ImportedMetadataSchema customSchema : importedType.getCustomSchemas()) {
            addSchemaItem(schemasElement, customSchema);
        }
    }

    private void addSchemaItem(Element schemasElement, ImportedMetadataSchema customSchema) {
        Element schemaElement = new Element(SCHEMA);
        schemaElement.setAttribute(CODE, customSchema.getCode());
        if (StringUtils.isNotBlank(customSchema.getLabel())) {
            schemaElement.setAttribute(LABEL, customSchema.getLabel());
        }
        schemasElement.addContent(schemaElement);

        addSchemaMetadata(customSchema, schemaElement);
    }

    private void addSchemaMetadata(ImportedMetadataSchema customSchema, Element schemaElement) {
        for (ImportedMetadata importedMetadata : customSchema.getAllMetadata()) {
            addMetadatum(schemaElement, importedMetadata);
        }
    }

    private void addDefaultSchema(ImportedType importedType, Element typeItem) {
        Element defaultSchemaElem = new Element(DEFAULT_SCHEMA);
        typeItem.addContent(defaultSchemaElem);

        ImportedMetadataSchema defaultSchema = importedType.getDefaultSchema();

        addSchemaMetadata(defaultSchema, defaultSchemaElem);
    }

    private void addTabs(ImportedType importedType, Element typeItem) {
        Element tabsElement = new Element(TABS);
        typeItem.addContent(tabsElement);

        for (ImportedTab tab : importedType.getTabs()) {
            addTabItem(tabsElement, tab);
        }
    }

    private void addTabItem(Element tabsElement, ImportedTab tab) {
        Element tabElem = new Element(TAB);
        tabElem.setAttribute(CODE, tab.getCode());
        tabElem.setAttribute(VALUE, tab.getValue());
        tabsElement.addContent(tabElem);
    }

    private void addMetadatum(Element defaultSchemaElem, ImportedMetadata importedMetadata) {

        Element metadataElem = new Element(METADATA);
        metadataElem.setAttribute(CODE, importedMetadata.getCode());

        if (StringUtils.isNotBlank(importedMetadata.getLabel())) {
            metadataElem.setAttribute("title", importedMetadata.getLabel());
        }

        List<String> behaviours = new ArrayList<>();

        metadataElem.setAttribute(TYPE, importedMetadata.getType().name());

        if (importedMetadata.getEnabled() != null) {
            metadataElem.setAttribute(ENABLED, importedMetadata.getEnabled() + "");
        }

        if (!importedMetadata.getEnabledIn().isEmpty()) {
            metadataElem.setAttribute(ENABLED_IN, StringUtils.join(importedMetadata.getEnabledIn(), ","));
        }

        if (StringUtils.isNotBlank(importedMetadata.getInputMask())) {
            metadataElem.setAttribute(INPUT_MASK, importedMetadata.getInputMask());
        }

        if (importedMetadata.getMultiValue() != null && importedMetadata.getMultiValue()) {
            metadataElem.setAttribute(MULTI_VALUE, importedMetadata.getMultiValue() + "");
        }

        if (importedMetadata.getRequired() != null) {
            metadataElem.setAttribute(REQUIRED, importedMetadata.getRequired() + "");
        }

        if (!importedMetadata.getRequiredIn().isEmpty()) {
            metadataElem.setAttribute(REQUIRED_IN, StringUtils.join(importedMetadata.getRequiredIn(), ","));
        }

        if (StringUtils.isNotBlank(importedMetadata.getTab())) {
            metadataElem.setAttribute(TAB, importedMetadata.getTab());
        }

        if (importedMetadata.getVisibleInDisplay() != null) {
            metadataElem.setAttribute(VISIBLE_IN_DISPLAY, importedMetadata.getVisibleInDisplay() + "");
        }

        if (!importedMetadata.getVisibleInDisplayIn().isEmpty()) {
            metadataElem.setAttribute(VISIBLE_IN_DISPLAY_IN, StringUtils.join(importedMetadata.getVisibleInDisplayIn(), ","));
        }


        if (importedMetadata.getVisibleInForm() != null) {
            metadataElem.setAttribute(VISIBLE_IN_FORM, importedMetadata.getVisibleInForm() + "");
        }

        if (!importedMetadata.getVisibleInFormIn().isEmpty()) {
            metadataElem.setAttribute(VISIBLE_IN_FORM_IN, StringUtils.join(importedMetadata.getVisibleInFormIn(), ","));
        }

        if (importedMetadata.getVisibleInResultIn() != null) {
            metadataElem.setAttribute(VISIBLE_IN_SEARCH_RESULT, importedMetadata.getVisibleInSearchResult() + "");
        }

        if (importedMetadata.getVisibleInSearchResult() != null) {
            metadataElem.setAttribute(VISIBLE_IN_SEARCH_RESULT, importedMetadata.getVisibleInSearchResult() + "");
        }

        if (importedMetadata.getVisibleInTables() != null) {
            metadataElem.setAttribute(VISIBLE_IN_TABLES, importedMetadata.getVisibleInTables() + "");
        }

        if (!importedMetadata.getVisibleInTablesIn().isEmpty()) {
            metadataElem.setAttribute(VISIBLE_IN_TABLES_IN, StringUtils.join(importedMetadata.getVisibleInTablesIn()));
        }

        if (StringUtils.isNotBlank(importedMetadata.getBehaviours())) {
            metadataElem.setAttribute(BEHAVIOURS, StringUtils.join(importedMetadata.getBehaviours(), ','));
        }

        defaultSchemaElem.addContent(metadataElem);
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
        listElem.setAttribute(TITLE, importedTaxonomy.getTitles().get(TITLE_FR));
        listElem.setAttribute(VISIBLE_IN_HOME_PAGE, importedTaxonomy.getVisibleOnHomePage() + "");
        listElem.setAttribute(CLASSIFIED_TYPES, StringUtils.join(importedTaxonomy.getClassifiedTypes(), ','));
        listElem.setAttribute(GROUPS, StringUtils.join(importedTaxonomy.getGroupIds(), ','));
        listElem.setAttribute(USERS, StringUtils.join(importedTaxonomy.getUserIds(), ','));

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
        listElem.setAttribute(TITLE, valueList.getTitles().get(TITLE_FR));
        listElem.setAttribute(CLASSIFIED_TYPES, StringUtils.join(valueList.getClassifiedTypes(), ','));
        if (StringUtils.isNotBlank(valueList.getCodeMode())) {
            listElem.setAttribute(CODE_MODE, valueList.getCodeMode());
        }
        listElem.setAttribute(HIERARCHICAL, valueList.isHierarchical() + "");

        valueListsElem.addContent(listElem);
    }
}