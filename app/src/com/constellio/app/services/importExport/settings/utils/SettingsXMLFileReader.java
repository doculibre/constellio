package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.*;

public class SettingsXMLFileReader extends SettingsXMLFileConstants {

    private Document document;

    public SettingsXMLFileReader(Document document) {
        this.document = document;
    }

    public ImportedSettings read() {

        Element rootNode = document.getRootElement();

        ImportedSettings importedSettings = new ImportedSettings()
                .setConfigs(readConfigs(rootNode.getChild(CONFIGS)))
                .setCollectionsSettings(readCollectionSettings(rootNode.getChildren(COLLECTION_SETTINGS)));

        return importedSettings;
    }

    private List<ImportedCollectionSettings> readCollectionSettings(List<Element> elements) {
        List<ImportedCollectionSettings> collectionSettings = new ArrayList<>();
        for (Element collectionElement : elements) {
            collectionSettings.add(readCollectionSetting(collectionElement));
        }
        return collectionSettings;
    }

    private ImportedCollectionSettings readCollectionSetting(Element collectionElement) {
        return new ImportedCollectionSettings().setCode(collectionElement.getAttributeValue(CODE))
                .setValueLists(getCollectionValueLists(collectionElement.getChild(VALUE_LISTS)))
                .setTaxonomies(getCollectionTaxonomies(collectionElement.getChild(TAXONOMIES)))
                .setTypes(getCollectionTypes(collectionElement.getChild(TYPES)));
    }

    private List<ImportedType> getCollectionTypes(Element typesElement) {
        List<ImportedType> types = new ArrayList<>();
        for (Element typeElement : typesElement.getChildren()) {
            types.add(readType(typeElement));
        }
        return types;
    }

    private ImportedType readType(Element typeElement) {
        return new ImportedType().setCode(typeElement.getAttributeValue(CODE))
                .setTabs(getTabs(typeElement.getChild(TABS)))
                .setDefaultSchema(readDefaultSchema(typeElement.getChild(DEFAULT_SCHEMA)));
    }

    private ImportedMetadataSchema readDefaultSchema(Element element) {
        return new ImportedMetadataSchema().setAllMetadatas(readMetadata(element.getChildren(METADATA)));
    }

    private List<ImportedMetadata> readMetadata(List<Element> elements) {
        List<ImportedMetadata> metadata = new ArrayList<>();
        for (Element element : elements) {
            metadata.add(readMetadata(element));
        }
        return metadata;
    }

    private ImportedMetadata readMetadata(Element element) {
        List<String> properties = new ArrayList<>();
        if (element.getAttribute(BEHAVIOURS) != null &&
                StringUtils.isNotBlank(element.getAttributeValue(BEHAVIOURS))) {
            properties.addAll(Arrays.asList(StringUtils.split(element.getAttributeValue(BEHAVIOURS), ',')));
        }

        ImportedMetadata importedMetadata = new ImportedMetadata();
        importedMetadata.setCode(element.getAttributeValue(CODE));
        importedMetadata.setType(EnumUtils.getEnum(MetadataValueType.class, element.getAttributeValue(TYPE)));

        if (element.getAttribute(ENABLED) != null) {
            importedMetadata.setEnabled(Boolean.parseBoolean(element.getAttributeValue(ENABLED)));
        }

        if (element.getAttribute(ENABLED_IN) != null &&
                StringUtils.isNotBlank(element.getAttributeValue(ENABLED_IN))) {
            importedMetadata.setEnabledIn(toListOfString(element.getAttributeValue(ENABLED_IN)));
        }

        importedMetadata.setVisibleInDisplay(properties.contains(VISIBLE_IN_DISPLAY));

        if (element.getAttribute(REQUIRED) != null) {
            importedMetadata.setRequired(Boolean.parseBoolean(element.getAttributeValue(REQUIRED)));
        }

        if (element.getAttribute(TAB) != null) {
            importedMetadata.setTab(element.getAttributeValue(TAB));
        }

        importedMetadata.setMultiValue(properties.contains(MULTIVALUE));

        if (properties.size() > 0) {
            importedMetadata.setBehaviours(properties);
        }

        importedMetadata.setSearchable(properties.contains(SEARCHABLE_IN_SIMPLE_SEARCH));

        importedMetadata.setAdvanceSearchable(properties.contains(SEARCHABLE_IN_ADVANCED_SEARCH));

        if (element.getAttribute(REQUIRED_IN) != null &&
                StringUtils.isNotBlank(element.getAttributeValue(REQUIRED_IN))) {
            importedMetadata.setRequiredIn(toListOfString(element.getAttributeValue(REQUIRED_IN)));
        }

        if (element.getAttribute(VISIBLE_IN_FORM) != null) {
            importedMetadata.setVisibleInForm(Boolean.parseBoolean(element.getAttributeValue(VISIBLE_IN_FORM)));
        }

        importedMetadata.setVisibleInDisplay(Boolean.parseBoolean(element.getAttributeValue(VISIBLE_IN_DISPLAY)));

        importedMetadata.setVisibleInSearchResult(Boolean.parseBoolean(element.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)));

        importedMetadata.setVisibleInTables(Boolean.parseBoolean(element.getAttributeValue(VISIBLE_IN_TABLES)));

        importedMetadata.setVisibleInFormIn(toListOfString(element.getAttributeValue(VISIBLE_IN_FORM_IN)));

        importedMetadata.setVisibleInDisplayIn(toListOfString(element.getAttributeValue(VISIBLE_IN_DISPLAY_IN)));

        importedMetadata.setVisibleInResultIn(toListOfString(element.getAttributeValue(VISIBLE_IN_RESULT_IN)));

        importedMetadata.setVisibleInTablesIn(toListOfString(element.getAttributeValue(VISIBLE_IN_TABLES_IN)));

        if (element.getAttribute(INPUT_MASK) != null &&
                StringUtils.isNotBlank(element.getAttributeValue(INPUT_MASK))) {
            importedMetadata.setInputMask(element.getAttributeValue(INPUT_MASK));
        }

        importedMetadata.setUnmodifiable(properties.contains(UNMODIFIABLE));

        importedMetadata.setSortable(properties.contains(SORTABLE));

        importedMetadata.setRecordAutocomplete(properties.contains(RECORD_AUTOCOMPLETE));

        importedMetadata.setEssential(properties.contains(ESSENTIAL));

        importedMetadata.setEssentialInSummary(properties.contains(ESSENTIAL_IN_SUMMARY));

        importedMetadata.setMultiLingual(properties.contains(MULTI_LINGUAL));

        importedMetadata.setDuplicable(properties.contains(DUPLICATE));

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

    private List<ImportedTaxonomy> getCollectionTaxonomies(Element element) {
        List<ImportedTaxonomy> taxonomies = new ArrayList<>();
        for (Element child : element.getChildren()) {
            taxonomies.add(readTaxonomy(child));
        }
        return taxonomies;
    }

    private ImportedTaxonomy readTaxonomy(Element child) {
        return new ImportedTaxonomy()
                .setCode(child.getAttributeValue(CODE))
                .setTitles(getTitles(child.getAttributeValue("title")))
                .setClassifiedTypes(getValueListClassifiedTypes(child.getAttributeValue("classifiedTypes")))
                .setGroupIds(toListOfString(child.getAttributeValue("groups")))
                .setUserIds(toListOfString(child.getAttributeValue("users")))
                .setVisibleOnHomePage(Boolean.valueOf(child.getAttributeValue("visibleInHomePage")));
    }

    private List<String> toListOfString(String stringValue) {
        if (StringUtils.isNotBlank(stringValue)) {
            return Arrays.asList(StringUtils.split(stringValue, ","));
        }
        return new ArrayList<>();
    }

    private List<ImportedValueList> getCollectionValueLists(Element valueListsElement) {
        List<ImportedValueList> importedValueLists = new ArrayList<>();
        for (Element element : valueListsElement.getChildren()) {
            importedValueLists.add(readValueList(element));
        }
        return importedValueLists;
    }

    private ImportedValueList readValueList(Element element) {
        return new ImportedValueList()
                .setCode(element.getAttributeValue(CODE))
                .setTitles(getTitles(element.getAttributeValue("title")))
                .setClassifiedTypes(getValueListClassifiedTypes(element.getAttributeValue("classifiedTypes")))
                .setCodeMode(element.getAttributeValue("codeMode"))
                .setHierarchical(Boolean.valueOf(element.getAttributeValue("hierarchical")));
    }

    private List<String> getValueListClassifiedTypes(String stringValue) {
        List<String> classifiedTypes = new ArrayList<>();
        if (StringUtils.isNotBlank(stringValue)) {
            classifiedTypes.addAll(Arrays.asList(StringUtils.split(stringValue, ',')));
        }
        return classifiedTypes;
    }

    private Map<String, String> getTitles(String title) {
        Map<String, String> titles = new HashMap<>();
        titles.put("title_fr", title);
        return titles;
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
}
