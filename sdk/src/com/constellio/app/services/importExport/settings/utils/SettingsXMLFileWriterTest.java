package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.SettingsImportServicesTestUtils;
import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.apache.tools.ant.util.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SettingsXMLFileWriterTest extends SettingsImportServicesTestUtils {

    private SettingsXMLFileWriter writer;

    @Before
    public void setup() {
        writer = new SettingsXMLFileWriter();
    }

    @Test
    public void whenCreatingWriterThenRootElementIsCreated() {
        assertThat(writer.getDocument().getRootElement().getName()).isEqualTo("settings");
    }

    @Test
    public void whenCreatingWriterThenRootElementIsEmpty() {
        assertThat(writer.getDocument().getRootElement().getChildren()).isEmpty();
    }

    @Test
    public void whenAddingValueListThenElementsAreAdded() {

        List<ImportedConfig> configs = new ArrayList<>();
        configs.add(new ImportedConfig().setKey("documentRetentionRules").setValue("true"));
        configs.add((new ImportedConfig().setKey("enforceCategoryAndRuleRelationshipInFolder").setValue("false")));
        configs.add((new ImportedConfig().setKey("calculatedCloseDate").setValue("false")));

        configs.add((new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule").setValue("2015")));
        configs.add((new ImportedConfig().setKey("closeDateRequiredDaysBeforeYearEnd").setValue("15")));

        configs.add((new ImportedConfig().setKey("yearEndDate").setValue("02/28")));

        writer.addGlobalConfigs(configs);

        assertThat(writer.getDocument().getRootElement().getChildren()).isNotEmpty().hasSize(1);
        Element configsElement = writer.getDocument().getRootElement().getChildren().get(0);
        assertThat(configsElement.getName()).isEqualTo("configs");
        List<Element> children = configsElement.getChildren();

        assertThat(children.get(0).getAttribute("key")).isNotNull();
        assertThat(children.get(0).getAttributeValue("key")).isEqualTo("documentRetentionRules");
        assertThat(children.get(0).getAttributeValue("value")).isEqualTo("true");

        assertThat(children.get(1).getAttribute("key")).isNotNull();
        assertThat(children.get(1).getAttributeValue("key")).isEqualTo("enforceCategoryAndRuleRelationshipInFolder");
        assertThat(children.get(1).getAttributeValue("value")).isEqualTo("false");

        assertThat(children.get(2).getAttribute("key")).isNotNull();
        assertThat(children.get(2).getAttributeValue("key")).isEqualTo("calculatedCloseDate");
        assertThat(children.get(2).getAttributeValue("value")).isEqualTo("false");

        assertThat(children.get(3).getAttribute("key")).isNotNull();
        assertThat(children.get(3).getAttributeValue("key")).isEqualTo("calculatedCloseDateNumberOfYearWhenFixedRule");
        assertThat(children.get(3).getAttributeValue("value")).isEqualTo("2015");

        assertThat(children.get(4).getAttribute("key")).isNotNull();
        assertThat(children.get(4).getAttributeValue("key")).isEqualTo("closeDateRequiredDaysBeforeYearEnd");
        assertThat(children.get(4).getAttributeValue("value")).isEqualTo("15");

        assertThat(children.get(5).getAttribute("key")).isNotNull();
        assertThat(children.get(5).getAttributeValue("key")).isEqualTo("yearEndDate");
        assertThat(children.get(5).getAttributeValue("value")).isEqualTo("02/28");

    }

    @Test
    public void whenAddingNullCollectionSettingsThenElementIsNotAdded() {
        writer.addCollectionsSettings(null);
        assertThat(writer.getDocument().getRootElement().getChildren()).isEmpty();
    }

    @Test
    public void whenWritingValueListsThenValuesAreSaved() {
        ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl1")
                .setTitles(toTitlesMap("domaine1", "valueList1"))
                .setClassifiedTypes(toListOfString("document", "folder"))
                .setCodeMode("DISABLED"));
        zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl2")
                .setTitles(toTitlesMap("domaine2", "valueList2"))
                .setClassifiedTypes(toListOfString(DOCUMENT))
                .setCodeMode("FACULTATIVE"));
        zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl3")
                .setTitles(toTitlesMap("domaine3", "valueList3"))
                .setCodeMode("REQUIRED_AND_UNIQUE")
                .setHierarchical(true));
        zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl4")
                .setTitles(toTitlesMap("domaine4", "valueList4"))
                .setHierarchical(false));

        ImportedSettings importedSettings = new ImportedSettings().addCollectionsConfigs(zeCollectionSettings);
        writer.writeSettings(importedSettings);

        List<Element> collectionElements = writer.getDocument().getRootElement().getChildren("collection-settings");
        assertThat(collectionElements).hasSize(1);

        Element zeCollectionElem = collectionElements.get(0);
        assertThat(zeCollectionElem.getAttributeValue(CODE)).isEqualTo(zeCollection);

        List<Element> children = zeCollectionElem.getChildren();
        assertThat(children).hasSize(3);

        // valueLists
        Element valueListsElem = children.get(0);
        List<Element> valueListsItems = valueListsElem.getChildren();
        assertThat(valueListsItems).hasSize(4);

        Element ddv1Elem = valueListsItems.get(0);
        assertThat(ddv1Elem.getAttributeValue(CODE)).isEqualTo("ddvUSRvl1");
        assertThat(ddv1Elem.getAttributeValue(TITLE)).isEqualTo("domaine1");
        assertThat(ddv1Elem.getAttributeValue(CLASSIFIED_TYPES)).isEqualTo("document,folder");
        assertThat(ddv1Elem.getAttributeValue("codeMode")).isEqualTo("DISABLED");
    }

    @Test
    public void whenWritingTaxonomiesThenOK() throws IOException {

        ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        ImportedTaxonomy taxonomy1 = new ImportedTaxonomy().setCode("taxoT1Type")
                .setTitles(toTitlesMap("taxo1Titre1", "taxoTitle1"))
                .setClassifiedTypes(toListOfString("document", "folder"))
                .setVisibleOnHomePage(false)
                .setUserIds(asList("user1", "user2"))
                .setGroupIds(asList("group1"));
        zeCollectionSettings.addTaxonomy(taxonomy1);

        ImportedTaxonomy taxonomy2 = new ImportedTaxonomy().setCode("taxoT2Type")
                .setTitles(toTitlesMap("taxo1Titre2", "taxoTitle2"));
        zeCollectionSettings.addTaxonomy(taxonomy2);

        ImportedSettings importedSettings = new ImportedSettings().addCollectionsConfigs(zeCollectionSettings);
        writer.writeSettings(importedSettings);

        List<Element> collectionElements = writer.getDocument().getRootElement().getChildren("collection-settings");
        assertThat(collectionElements).hasSize(1);

        Element zeCollectionElem = collectionElements.get(0);
        assertThat(zeCollectionElem.getAttributeValue(CODE)).isEqualTo(zeCollection);

        List<Element> children = zeCollectionElem.getChildren();
        assertThat(children).hasSize(3);

        // taxonomies
        Element taxonomiesElem = children.get(1);
        assertThat(taxonomiesElem.getChildren()).hasSize(2);

        Element taxonomy1Elem = taxonomiesElem.getChildren().get(0);
        assertThat(taxonomy1Elem.getAttributeValue(CODE)).isEqualTo("taxoT1Type");
        assertThat(taxonomy1Elem.getAttributeValue(TITLE)).isEqualTo("taxo1Titre1");
        assertThat(taxonomy1Elem.getAttributeValue(VISIBLE_IN_HOME_PAGE)).isEqualTo("false");
        assertThat(taxonomy1Elem.getAttributeValue(USERS)).isEqualTo("user1,user2");
        assertThat(taxonomy1Elem.getAttributeValue(GROUPS)).isEqualTo("group1");
    }

    @Test
    public void whenWritingTypesThenElementsPresent() throws IOException {

        ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

        Map<String, String> tabParams = new HashMap<>();
        tabParams.put("default", "Métadonnées");
        tabParams.put("zeTab", "Mon onglet");

        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setLabel("titre m1")
                .setType(MetadataValueType.STRING)
                .setEnabledIn(toListOfString("default", "custom1", "custom2"))
                .setRequiredIn(toListOfString("custom1"))
                .setVisibleInFormIn(toListOfString("default", "custom1"));

        String behaviours = "searchableInSimpleSearch,searchableInAdvancedSearch,unique,unmodifiable,sortable,recordAutocomplete,essential,essentialInSummary,multiLingual,duplicable";
        ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setLabel("titre m2")
                .setType(MetadataValueType.STRING).setEnabled(true).setRequired(true)
                .setTab("zeTab").setMultiValue(true).setBehaviours(behaviours)
                .setInputMask("9999-9999");

        ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setLabel("Titre m3")
                .setType(MetadataValueType.STRING)
                .setEnabledIn(toListOfString("default", "custom1", "custom2"))
                .setRequiredIn(Arrays.asList("custom1"))
                .setMultiValue(true);
        zeCollectionSettings.addType(new ImportedType().setCode("folder").setLabel("Dossier")
                .setTabs(toListOfTabs(tabParams))
                .setDefaultSchema(new ImportedMetadataSchema().setCode("default")
                        .addMetadata(m1)
                        .addMetadata(m2))
                .addSchema(new ImportedMetadataSchema().setCode("custom1")
                        .addMetadata(m3)));

        ImportedSettings importedSettings = new ImportedSettings().addCollectionsConfigs(zeCollectionSettings);
        writer.writeSettings(importedSettings);

        List<Element> collectionElements = writer.getDocument().getRootElement().getChildren("collection-settings");
        assertThat(collectionElements).hasSize(1);

        Element zeCollectionElem = collectionElements.get(0);
        assertThat(zeCollectionElem.getAttributeValue(CODE)).isEqualTo(zeCollection);

        List<Element> children = zeCollectionElem.getChildren();
        assertThat(children).hasSize(3);

        // types
        Element typesElement = children.get(2);
        assertThat(typesElement).isNotNull();
        assertThat(typesElement.getChildren()).hasSize(1);

        // folder type
        Element folderTypeElement = typesElement.getChildren().get(0);
        assertThat(folderTypeElement).isNotNull();
        assertThat(folderTypeElement.getAttributeValue(CODE)).isEqualTo("folder");

        // tabs
        List<Element> folderChildren = folderTypeElement.getChildren();
        Element tabsElement = folderChildren.get(0);
        assertThat(tabsElement).isNotNull();
        assertThat(tabsElement.getChildren()).hasSize(2);
        List<Element> tabsChildren = tabsElement.getChildren();
        assertThat(tabsChildren.get(0).getAttributeValue(CODE)).isEqualTo("zeTab");
        assertThat(tabsChildren.get(0).getAttributeValue(VALUE)).isEqualTo("Mon onglet");
        assertThat(tabsChildren.get(1).getAttributeValue(CODE)).isEqualTo("default");
        assertThat(tabsChildren.get(1).getAttributeValue(VALUE)).isEqualTo("Métadonnées");

        // default-schema
        Element defaultSchemaElem = typesElement.getChildren().get(0).getChild("default-schema");
        assertThat(defaultSchemaElem).isNotNull();
        assertThat(defaultSchemaElem.getChildren()).hasSize(2);

        Element metadata1Elem = defaultSchemaElem.getChildren().get(0);

        assertThat(metadata1Elem.getAttributeValue(CODE)).isEqualTo("m1");
        assertThat(metadata1Elem.getAttributeValue(TITLE)).isEqualTo("titre m1");
        assertThat(metadata1Elem.getAttributeValue(TYPE)).isEqualTo(MetadataValueType.STRING.name());
        assertThat(metadata1Elem.getAttributeValue(ENABLED)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,custom1,custom2");
        assertThat(metadata1Elem.getAttributeValue(REQUIRED)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("custom1");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isEqualTo("default,custom1");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(TAB)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(MULTI_VALUE)).isEqualTo(null);
        assertThat(metadata1Elem.getAttributeValue(BEHAVIOURS)).isNull();
        assertThat(metadata1Elem.getAttributeValue(INPUT_MASK)).isNullOrEmpty();

        Element metadata2Elem = defaultSchemaElem.getChildren().get(1);
        assertThat(metadata2Elem.getAttributeValue(CODE)).isEqualTo("m2");
        assertThat(metadata2Elem.getAttributeValue(TITLE)).isEqualTo("titre m2");
        assertThat(metadata2Elem.getAttributeValue(TYPE)).isEqualTo(MetadataValueType.STRING.name());
        assertThat(metadata2Elem.getAttributeValue(ENABLED)).isEqualTo("true");
        assertThat(metadata2Elem.getAttributeValue(ENABLED_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(REQUIRED)).isEqualTo("true");
        assertThat(metadata2Elem.getAttributeValue(REQUIRED_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_FORM)).isEqualTo("true");
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isEqualTo("true");
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_TABLES)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(TAB)).isEqualTo("zeTab");
        assertThat(metadata2Elem.getAttributeValue(MULTI_VALUE)).isEqualTo("true");
        List<String> expectedBehaviours = StringUtils.split(behaviours, ',');
        List<String> actualBehaviours = StringUtils.split(metadata2Elem.getAttributeValue(BEHAVIOURS), ',');
        assertThat(actualBehaviours.containsAll(expectedBehaviours)).isTrue();
        assertThat(metadata2Elem.getAttributeValue(INPUT_MASK)).isEqualTo("9999-9999");

        Element customSchemata = typesElement.getChildren().get(0).getChild("schemas");
        assertThat(customSchemata).isNotNull();
        assertThat(customSchemata.getChildren()).hasSize(1);

        Element schema1Element = customSchemata.getChildren().get(0);
        assertThat(schema1Element).isNotNull();
        assertThat(schema1Element.getAttributeValue(CODE)).isEqualTo("custom1");
        List<Element> schema1Metadata = schema1Element.getChildren();
        assertThat(schema1Metadata).hasSize(1);

        Element metadata3Elem = schema1Metadata.get(0);
        assertThat(metadata3Elem).isNotNull();
        assertThat(metadata3Elem.getAttributeValue(CODE)).isEqualTo("m3");
        assertThat(metadata3Elem.getAttributeValue(TITLE)).isEqualTo("Titre m3");
        assertThat(metadata3Elem.getAttributeValue(TYPE)).isEqualTo(MetadataValueType.STRING.name());
        assertThat(metadata3Elem.getAttributeValue(ENABLED)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,custom1,custom2");
        assertThat(metadata3Elem.getAttributeValue(REQUIRED)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("custom1");
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_FORM)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_TABLES)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(TAB)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(MULTI_VALUE)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(BEHAVIOURS)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(INPUT_MASK)).isNullOrEmpty();

        String outputFilePath = "/home/constellio/workspaces/settings-import-tests/settings-types-output.xml";
        File outputFile = new File(outputFilePath);

        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        xmlOutputter.output(writer.getDocument(), fileOutputStream);

        System.out.println("File Saved!");
    }


    @Test
    public void whenWritingSettingsFileThenElementsPresent() throws IOException {

        List<ImportedConfig> configs = new ArrayList<>();
        configs.add(new ImportedConfig().setKey("documentRetentionRules").setValue("true"));
        configs.add((new ImportedConfig().setKey("enforceCategoryAndRuleRelationshipInFolder").setValue("false")));
        configs.add((new ImportedConfig().setKey("calculatedCloseDate").setValue("false")));

        configs.add((new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule").setValue("2015")));
        configs.add((new ImportedConfig().setKey("closeDateRequiredDaysBeforeYearEnd").setValue("15")));

        configs.add((new ImportedConfig().setKey("yearEndDate").setValue("02/28")));

        writer.addGlobalConfigs(configs);

        ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
        zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl1")
                .setTitles(toTitlesMap("domaine1", "valueList1"))
                .setClassifiedTypes(toListOfString("document", "folder"))
                .setCodeMode("DISABLED"));
        zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl2")
                .setTitles(toTitlesMap("domaine2", "valueList2"))
                .setClassifiedTypes(toListOfString(DOCUMENT))
                .setCodeMode("FACULTATIVE"));
        zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl3")
                .setTitles(toTitlesMap("domaine3", "valueList3"))
                .setCodeMode("REQUIRED_AND_UNIQUE")
                .setHierarchical(true));
        zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl4")
                .setTitles(toTitlesMap("domaine4", "valueList4"))
                .setHierarchical(false));

        zeCollectionSettings.addTaxonomy(new ImportedTaxonomy().setCode("taxoT1Type")
                .setTitles(toTitlesMap("taxo1Titre1", "taxoTitle1"))
                .setClassifiedTypes(toListOfString("document", "folder"))
                .setVisibleOnHomePage(false)
                .setUserIds(asList("user1", "user2"))
                .setGroupIds(asList("group1")));

        zeCollectionSettings.addTaxonomy(new ImportedTaxonomy().setCode("taxoT2Type")
                .setTitles(toTitlesMap("taxo1Titre2", "taxoTitle2")));

        Map<String, String> tabParams = new HashMap<>();
        tabParams.put("default", "Métadonnées");
        tabParams.put("zeTab", "Mon onglet");

        ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setLabel("titre m1")
                .setType(MetadataValueType.STRING)
                .setEnabledIn(toListOfString("default", "custom1", "custom2"))
                .setRequiredIn(toListOfString("custom1"))
                .setVisibleInFormIn(toListOfString("default", "custom1"));

        String behaviours = "searchableInSimpleSearch,searchableInAdvancedSearch,unique,unmodifiable,sortable,recordAutocomplete,essential,essentialInSummary,multiLingual,duplicable";
        ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setLabel("titre m2")
                .setType(MetadataValueType.STRING).setEnabled(true).setRequired(true)
                .setTab("zeTab").setMultiValue(true).setBehaviours(behaviours)
                .setInputMask("9999-9999");

        ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setLabel("Titre m3")
                .setType(MetadataValueType.STRING)
                .setEnabledIn(toListOfString("default", "custom1", "custom2"))
                .setRequiredIn(Arrays.asList("custom1"))
                .setMultiValue(true);
        zeCollectionSettings.addType(new ImportedType().setCode("folder").setLabel("Dossier")
                .setTabs(toListOfTabs(tabParams))
                .setDefaultSchema(new ImportedMetadataSchema().setCode("default")
                        .addMetadata(m1)
                        .addMetadata(m2))
                .addSchema(new ImportedMetadataSchema().setCode("custom1")
                        .addMetadata(m3)));

        ImportedSettings importedSettings = new ImportedSettings().addCollectionsConfigs(zeCollectionSettings);
        writer.writeSettings(importedSettings);

        List<Element> collectionElements = writer.getDocument().getRootElement().getChildren("collection-settings");
        assertThat(collectionElements).hasSize(1);

        Element zeCollectionElem = collectionElements.get(0);
        assertThat(zeCollectionElem.getAttributeValue(CODE)).isEqualTo(zeCollection);

        List<Element> children = zeCollectionElem.getChildren();
        assertThat(children).hasSize(3);

        // valueLists
        Element valueListsElem = children.get(0);
        List<Element> valueListsItems = valueListsElem.getChildren();
        assertThat(valueListsItems).hasSize(4);

        Element ddv1Elem = valueListsItems.get(0);
        assertThat(ddv1Elem.getAttributeValue(CODE)).isEqualTo("ddvUSRvl1");
        assertThat(ddv1Elem.getAttributeValue(TITLE)).isEqualTo("domaine1");
        assertThat(ddv1Elem.getAttributeValue(CLASSIFIED_TYPES)).isEqualTo("document,folder");
        assertThat(ddv1Elem.getAttributeValue("codeMode")).isEqualTo("DISABLED");

        // taxonomies
        Element taxonomiesElem = children.get(1);
        assertThat(taxonomiesElem.getChildren()).hasSize(2);

        Element taxonomy1Elem = taxonomiesElem.getChildren().get(0);
        assertThat(taxonomy1Elem.getAttributeValue(CODE)).isEqualTo("taxoT1Type");
        assertThat(taxonomy1Elem.getAttributeValue(TITLE)).isEqualTo("taxo1Titre1");
        assertThat(taxonomy1Elem.getAttributeValue(VISIBLE_IN_HOME_PAGE)).isEqualTo("false");
        assertThat(taxonomy1Elem.getAttributeValue(USERS)).isEqualTo("user1,user2");
        assertThat(taxonomy1Elem.getAttributeValue(GROUPS)).isEqualTo("group1");

        // types
        Element typesElement = children.get(2);
        assertThat(typesElement).isNotNull();
        assertThat(typesElement.getChildren()).hasSize(1);

        // folder type
        Element folderTypeElement = typesElement.getChildren().get(0);
        assertThat(folderTypeElement).isNotNull();
        assertThat(folderTypeElement.getAttributeValue(CODE)).isEqualTo("folder");

        // tabs
        List<Element> folderChildren = folderTypeElement.getChildren();
        Element tabsElement = folderChildren.get(0);
        assertThat(tabsElement).isNotNull();
        assertThat(tabsElement.getChildren()).hasSize(2);
        List<Element> tabsChildren = tabsElement.getChildren();
        assertThat(tabsChildren.get(0).getAttributeValue(CODE)).isEqualTo("zeTab");
        assertThat(tabsChildren.get(0).getAttributeValue(VALUE)).isEqualTo("Mon onglet");
        assertThat(tabsChildren.get(1).getAttributeValue(CODE)).isEqualTo("default");
        assertThat(tabsChildren.get(1).getAttributeValue(VALUE)).isEqualTo("Métadonnées");

        // default-schema
        Element defaultSchemaElem = typesElement.getChildren().get(0).getChild("default-schema");
        assertThat(defaultSchemaElem).isNotNull();
        assertThat(defaultSchemaElem.getChildren()).hasSize(2);

        Element metadata1Elem = defaultSchemaElem.getChildren().get(0);

        assertThat(metadata1Elem.getAttributeValue(CODE)).isEqualTo("m1");
        assertThat(metadata1Elem.getAttributeValue(TITLE)).isEqualTo("titre m1");
        assertThat(metadata1Elem.getAttributeValue(TYPE)).isEqualTo(MetadataValueType.STRING.name());
        assertThat(metadata1Elem.getAttributeValue(ENABLED)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,custom1,custom2");
        assertThat(metadata1Elem.getAttributeValue(REQUIRED)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("custom1");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isEqualTo("default,custom1");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(TAB)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(MULTI_VALUE)).isEqualTo(null);
        assertThat(metadata1Elem.getAttributeValue(BEHAVIOURS)).isNull();
        assertThat(metadata1Elem.getAttributeValue(INPUT_MASK)).isNullOrEmpty();

        Element metadata2Elem = defaultSchemaElem.getChildren().get(1);
        assertThat(metadata2Elem.getAttributeValue(CODE)).isEqualTo("m2");
        assertThat(metadata2Elem.getAttributeValue(TITLE)).isEqualTo("titre m2");
        assertThat(metadata2Elem.getAttributeValue(TYPE)).isEqualTo(MetadataValueType.STRING.name());
        assertThat(metadata2Elem.getAttributeValue(ENABLED)).isEqualTo("true");
        assertThat(metadata2Elem.getAttributeValue(ENABLED_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(REQUIRED)).isEqualTo("true");
        assertThat(metadata2Elem.getAttributeValue(REQUIRED_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_FORM)).isEqualTo("true");
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isEqualTo("true");
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_TABLES)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
        assertThat(metadata2Elem.getAttributeValue(TAB)).isEqualTo("zeTab");
        assertThat(metadata2Elem.getAttributeValue(MULTI_VALUE)).isEqualTo("true");
        List<String> expectedBehaviours = StringUtils.split(behaviours, ',');
        List<String> actualBehaviours = StringUtils.split(metadata2Elem.getAttributeValue(BEHAVIOURS), ',');
        assertThat(actualBehaviours.containsAll(expectedBehaviours)).isTrue();
        assertThat(metadata2Elem.getAttributeValue(INPUT_MASK)).isEqualTo("9999-9999");

        Element customSchemata = typesElement.getChildren().get(0).getChild("schemas");
        assertThat(customSchemata).isNotNull();
        assertThat(customSchemata.getChildren()).hasSize(1);

        Element schema1Element = customSchemata.getChildren().get(0);
        assertThat(schema1Element).isNotNull();
        assertThat(schema1Element.getAttributeValue(CODE)).isEqualTo("custom1");
        List<Element> schema1Metadata = schema1Element.getChildren();
        assertThat(schema1Metadata).hasSize(1);

        Element metadata3Elem = schema1Metadata.get(0);
        assertThat(metadata3Elem).isNotNull();
        assertThat(metadata3Elem.getAttributeValue(CODE)).isEqualTo("m3");
        assertThat(metadata3Elem.getAttributeValue(TITLE)).isEqualTo("Titre m3");
        assertThat(metadata3Elem.getAttributeValue(TYPE)).isEqualTo(MetadataValueType.STRING.name());
        assertThat(metadata3Elem.getAttributeValue(ENABLED)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,custom1,custom2");
        assertThat(metadata3Elem.getAttributeValue(REQUIRED)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("custom1");
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_FORM)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_TABLES)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(TAB)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(MULTI_VALUE)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(BEHAVIOURS)).isNullOrEmpty();
        assertThat(metadata3Elem.getAttributeValue(INPUT_MASK)).isNullOrEmpty();

        String outputFilePath = "/home/constellio/workspaces/settings-import-tests/settings-output.xml";
        File outputFile = new File(outputFilePath);

        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        xmlOutputter.output(writer.getDocument(), fileOutputStream);

        System.out.println("File Saved!");
    }

    @Test
    public void givenAConfigurationFileWhenReadThenLoaded() throws IOException {

        String inputFilePath = "/home/constellio/workspaces/settings-import-tests/settings-input.xml";
        File inputFile = new File(inputFilePath);
        Document originalDocument = getDocumentFromFile(inputFile);

        ImportedSettings settings = new SettingsXMLFileReader(originalDocument).read();

        assertThat(settings.getConfigs()).hasSize(6);
        // configs
        List<ImportedConfig> configs = settings.getConfigs();
        assertThat(configs).isNotEmpty().hasSize(6);
        assertThat(configs.get(0).getKey()).isEqualTo("documentRetentionRules");
        assertThat(configs.get(0).getValue()).isEqualTo("true");
        assertThat(configs.get(1).getKey()).isEqualTo("enforceCategoryAndRuleRelationshipInFolder");
        assertThat(configs.get(1).getValue()).isEqualTo("false");
        assertThat(configs.get(2).getKey()).isEqualTo("calculatedCloseDate");
        assertThat(configs.get(2).getValue()).isEqualTo("false");
        assertThat(configs.get(3).getKey()).isEqualTo("calculatedCloseDateNumberOfYearWhenFixedRule");
        assertThat(configs.get(3).getValue()).isEqualTo("2015");
        assertThat(configs.get(4).getKey()).isEqualTo("closeDateRequiredDaysBeforeYearEnd");
        assertThat(configs.get(4).getValue()).isEqualTo("15");
        assertThat(configs.get(5).getKey()).isEqualTo("yearEndDate");
        assertThat(configs.get(5).getValue()).isEqualTo("02/28");

        ImportedCollectionSettings zeCollectionSettings = settings.getCollectionsConfigs().get(0);
        assertThat(zeCollectionSettings).isNotNull();
        assertThat(zeCollectionSettings.getCode()).isEqualTo(zeCollection);

        List<ImportedValueList> valueLists = zeCollectionSettings.getValueLists();
        assertThat(valueLists).isNotEmpty().hasSize(4);

        ImportedValueList valueList1 = valueLists.get(0);
        assertThat(valueList1.getCode()).isEqualTo("ddvUSRvl1");
        assertThat(valueList1.getTitles()).containsEntry("title_fr", "domaine1");
        assertThat(valueList1.getClassifiedTypes()).containsExactly("document", "folder");
        assertThat(valueList1.getCodeMode()).isEqualTo("DISABLED");
    }

    Document getDocumentFromFile(File file) {
        SAXBuilder builder = new SAXBuilder();
        try {
            return builder.build(file);
        } catch (JDOMException e) {
            throw new ConfigManagerRuntimeException("JDOM2 Exception", e);
        } catch (IOException e) {
            throw new ConfigManagerRuntimeException.CannotCompleteOperation("build Document JDOM2 from file", e);
        }
    }
}
