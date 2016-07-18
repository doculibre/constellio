package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.*;

public class SettingsXMLFileReader {

    private Document document;

    public SettingsXMLFileReader(Document document) {
        this.document = document;
    }

    public ImportedSettings read() {

        Element rootNode = document.getRootElement();

        ImportedSettings importedSettings = new ImportedSettings()
                .setConfigs(readConfigs(rootNode.getChild("configs")))
                .setCollectionsSettings(readCollectionSettings(rootNode.getChildren("collection-settings")));

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
        return new ImportedCollectionSettings().setCode(collectionElement.getAttributeValue("code"))
                .setValueLists(getCollectionValueLists(collectionElement.getChild("valueLists")))
                .setTaxonomies(getCollectionTaxonomies(collectionElement.getChild("taxonomies")))
                .setTypes(getCollectionTypes(collectionElement.getChild("types")));
    }

    private List<ImportedType> getCollectionTypes(Element typesElement) {
        List<ImportedType> types = new ArrayList<>();
        for (Element typeElement : typesElement.getChildren()) {
            types.add(readType(typeElement));
        }
        return types;
    }

    private ImportedType readType(Element typeElement) {
        return new ImportedType().setCode(typeElement.getAttributeValue("code"))
                .setTabs(getTabs(typeElement.getChild("tabs")))
                .setDefaultSchema(readDefaultSchema(typeElement.getChild("default-schema")));
    }

    private ImportedMetadataSchema readDefaultSchema(Element element) {
        return new ImportedMetadataSchema().setAllMetadatas(readMetadata(element.getChildren("metadata")));
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
        if (StringUtils.isNotBlank(element.getAttributeValue("behaviours"))) {
            properties.addAll(Arrays.asList(StringUtils.split(element.getAttributeValue("behaviours"), ',')));
        }

        return new ImportedMetadata()
                .setCode(element.getAttributeValue("code"))
                .setType(EnumUtils.getEnum(MetadataValueType.class, element.getAttributeValue("type")))
                .setEnabled(Boolean.parseBoolean(element.getAttributeValue("enabled")))
                .setEnabledIn(toListOfString(element.getAttributeValue("enabledIn")))
                .setVisibleInDisplay(Boolean.parseBoolean(element.getAttributeValue("enabled")))
                .setRequired(Boolean.parseBoolean(element.getAttributeValue("required")))
                .setTab(element.getAttributeValue("tab"))
                .setMultiValue(Boolean.parseBoolean(element.getAttributeValue("multivalue")))
                .setBehaviours(properties)
                .setSearchable(properties.contains("searchable"))
                .setAdvanceSearchable(properties.contains("searchable"))
                .setRequiredIn(toListOfString(element.getAttributeValue("requiredIn")))
                .setVisibleInForm(Boolean.parseBoolean(element.getAttributeValue("visibleInForm")))
                .setVisibleInDisplay(Boolean.parseBoolean(element.getAttributeValue("visibleInDisplay")))
                .setVisibleInSearchResult(Boolean.parseBoolean(element.getAttributeValue("visibleInSearchResult")))
                .setVisibleInTables(Boolean.parseBoolean(element.getAttributeValue("visibleInTables")))
                .setVisibleInFormIn(toListOfString(element.getAttributeValue("visibleInFormIn")))
                .setVisibleInDisplayIn(toListOfString(element.getAttributeValue("visibleInDisplayIn")))
                .setVisibleInResultIn(toListOfString(element.getAttributeValue("visibleInResultIn")))
                .setVisibleInTablesIn(toListOfString(element.getAttributeValue("visibleInTablesIn")))
                .setInputMask(element.getAttributeValue("inputMask"))
                .setUnmodifiable(properties.contains("unmodifiable"))
                .setSortable(properties.contains("sortable"))
                .setRecordAutocomplete(properties.contains("recordAutocomplete"))
                .setEssential(properties.contains("essential"))
                .setEssentialInSummary(properties.contains("essentialInSummary"))
                .setMultiLingual(properties.contains("multiLingual"))
                .setDuplicable(properties.contains("duplicate"));
    }

    private List<ImportedTab> getTabs(Element tabsElement) {
        List<ImportedTab> tabs = new ArrayList<>();
        for (Element child : tabsElement.getChildren()) {
            tabs.add(readTab(child));
        }
        return tabs;
    }

    private ImportedTab readTab(Element element) {
        return new ImportedTab().setCode(element.getAttributeValue("code"))
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
                .setCode(child.getAttributeValue("code"))
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
                .setCode(element.getAttributeValue("code"))
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
