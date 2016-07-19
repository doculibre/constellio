package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.SettingsImportServicesTestUtils;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        writer.addGlobalConfigs(getImportedConfigs());
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
    public void whenAddingCollectionSettingsThenElementIsAdded() throws IOException {

        ImportedCollectionSettings zeCollectionSettings = getZeCollectionSettings();
        ImportedSettings settings = new ImportedSettings().setConfigs(getImportedConfigs())
                .setCollectionsSettings(Arrays.asList(zeCollectionSettings));

        writer.writeSettings(settings);

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
        assertThat(ddv1Elem.getAttributeValue(CODE)).isEqualTo("ddvUSRcodeDuDomaineDeValeur1");
        assertThat(ddv1Elem.getAttributeValue(TITLE)).isEqualTo("Le titre du domaine de valeurs 1");
        assertThat(ddv1Elem.getAttributeValue(CLASSIFIED_TYPES)).isEqualTo("document,folder");
        assertThat(ddv1Elem.getAttributeValue("codeMode")).isEqualTo("DISABLED");

        // taxonomies
        Element taxonomiesElem = children.get(1);
        assertThat(taxonomiesElem.getChildren()).hasSize(2);

        Element taxonomy1Elem = taxonomiesElem.getChildren().get(0);
        assertThat(taxonomy1Elem.getAttributeValue(CODE)).isEqualTo(TAXO_1_CODE);
        assertThat(taxonomy1Elem.getAttributeValue(TITLE)).isEqualTo(TAXO_1_TITLE_FR);
        assertThat(taxonomy1Elem.getAttributeValue(VISIBLE_IN_HOME_PAGE)).isEqualTo("false");
        assertThat(taxonomy1Elem.getAttributeValue(USERS)).isEqualTo("gandalf,edouard");
        assertThat(taxonomy1Elem.getAttributeValue(GROUPS)).isEqualTo("heroes");

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
        assertThat(metadata1Elem.getAttributeValue(CODE)).isEqualTo("metadata1");
        assertThat(metadata1Elem.getAttributeValue(TITLE)).isEqualTo("Titre métadonnée no.1");
        assertThat(metadata1Elem.getAttributeValue(TYPE)).isEqualTo(MetadataValueType.STRING.name());
        assertThat(metadata1Elem.getAttributeValue(ENABLED)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,USRschema1,USRschema2");
        assertThat(metadata1Elem.getAttributeValue(REQUIRED)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("USRschema1");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isEqualTo("default,USRschema1");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isEqualTo("true");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(TAB)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(MULTI_VALUE)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(BEHAVIOURS)).isNull();
        assertThat(metadata1Elem.getAttributeValue(INPUT_MASK)).isNullOrEmpty();
        assertThat(metadata1Elem.getAttributeValue(SEARCHABLE)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(ADVANCE_SEARCHABLE)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(UNMODIFIABLE)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(SORTABLE)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(RECORD_AUTOCOMPLETE)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(ESSENTIAL)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(ESSENTIAL_IN_SUMMARY)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(MULTI_LINGUAL)).isEqualTo("false");
        assertThat(metadata1Elem.getAttributeValue(DUPLICABLE)).isEqualTo("false");

        Element metadata2Elem = defaultSchemaElem.getChildren().get(1);
        assertThat(metadata2Elem.getAttributeValue(CODE)).isEqualTo("metadata2");
        assertThat(metadata2Elem.getAttributeValue(TITLE)).isEqualTo("Titre métadonnée no.2");
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
        String behaviours = "searchableInSimpleSearch,searchableInAdvancedSearch,unique,unmodifiable,sortable,recordAutocomplete,essential,essentialInSummary,multiLingual,duplicable";
        assertThat(metadata2Elem.getAttributeValue(BEHAVIOURS)).isEqualTo(behaviours);
        assertThat(metadata2Elem.getAttributeValue(INPUT_MASK)).isEqualTo("9999-9999");
        assertThat(metadata2Elem.getAttributeValue(SEARCHABLE)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(ADVANCE_SEARCHABLE)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(UNMODIFIABLE)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(SORTABLE)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(RECORD_AUTOCOMPLETE)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(ESSENTIAL)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(ESSENTIAL_IN_SUMMARY)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(MULTI_LINGUAL)).isEqualTo("false");
        assertThat(metadata2Elem.getAttributeValue(DUPLICABLE)).isEqualTo("false");

        Element customSchemata = typesElement.getChildren().get(0).getChild("schemas");
        assertThat(customSchemata).isNotNull();
        assertThat(customSchemata.getChildren()).hasSize(1);

        Element schema1Element = customSchemata.getChildren().get(0);
        assertThat(schema1Element).isNotNull();
        assertThat(schema1Element.getAttributeValue(CODE)).isEqualTo("USRschema1");
        List<Element> schema1Metadata = schema1Element.getChildren();
        assertThat(schema1Metadata).hasSize(1);

        Element metadata3Elem = schema1Metadata.get(0);
        assertThat(metadata3Elem).isNotNull();
        assertThat(metadata3Elem.getAttributeValue(CODE)).isEqualTo("metadata3");
        assertThat(metadata3Elem.getAttributeValue(TITLE)).isEqualTo("Titre métadonnée no.3");
        assertThat(metadata3Elem.getAttributeValue(TYPE)).isEqualTo(MetadataValueType.STRING.name());
        assertThat(metadata3Elem.getAttributeValue(ENABLED)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,USRschema1,USRschema2");
        assertThat(metadata3Elem.getAttributeValue(REQUIRED)).isEqualTo("true");
        assertThat(metadata3Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("USRschema1");
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
        assertThat(metadata3Elem.getAttributeValue(SEARCHABLE)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(ADVANCE_SEARCHABLE)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(UNMODIFIABLE)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(SORTABLE)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(RECORD_AUTOCOMPLETE)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(ESSENTIAL)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(ESSENTIAL_IN_SUMMARY)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(MULTI_LINGUAL)).isEqualTo("false");
        assertThat(metadata3Elem.getAttributeValue(DUPLICABLE)).isEqualTo("false");

        String outputFilePath = "/home/constellio/workspaces/settings-import-tests/settings-output.xml";
        File outputFile = new File(outputFilePath);

        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        xmlOutputter.output(writer.getDocument(), fileOutputStream);

        System.out.println("File Saved!");
    }
}
