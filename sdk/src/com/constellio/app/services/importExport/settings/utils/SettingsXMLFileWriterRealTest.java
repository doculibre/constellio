package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.SettingsImportServicesTestUtils;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedDataEntry;
import com.constellio.app.services.importExport.settings.model.ImportedMetadata;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataSchema;
import com.constellio.app.services.importExport.settings.model.ImportedSequence;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.model.ImportedTaxonomy;
import com.constellio.app.services.importExport.settings.model.ImportedType;
import com.constellio.app.services.importExport.settings.model.ImportedValueList;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException;
import com.constellio.model.entities.Language;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SettingsXMLFileWriterRealTest extends SettingsImportServicesTestUtils implements SettingsXMLFileConstants {

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
	public void whenAddingSequencesThenElementsAreAdded() {

		List<ImportedSequence> sequences = new ArrayList<>();
		sequences.add(new ImportedSequence().setKey("1").setValue("1"));
		sequences.add(new ImportedSequence().setKey("1").setValue("2"));
		sequences.add(new ImportedSequence().setKey("1").setValue("3"));

		sequences.add(new ImportedSequence().setKey("2").setValue("1"));
		sequences.add(new ImportedSequence().setKey("2").setValue("2"));
		sequences.add(new ImportedSequence().setKey("2").setValue("3"));
		sequences.add(new ImportedSequence().setKey("2").setValue("4"));
		sequences.add(new ImportedSequence().setKey("2").setValue("5"));

		writer.addSequences(sequences);

		assertThat(writer.getDocument().getRootElement().getChildren()).isNotEmpty().hasSize(1);
		Element sequencesElement = writer.getDocument().getRootElement().getChild("sequences");
		assertThat(sequencesElement).isNotNull();
		List<Element> children = sequencesElement.getChildren();
		assertThat(children).hasSize(8);

		assertThat(children.get(0).getAttributeValue("key")).isEqualTo("1");
		assertThat(children.get(0).getAttributeValue("value")).isEqualTo("1");

		assertThat(children.get(1).getAttributeValue("key")).isEqualTo("1");
		assertThat(children.get(1).getAttributeValue("value")).isEqualTo("2");

		assertThat(children.get(2).getAttributeValue("key")).isEqualTo("1");
		assertThat(children.get(2).getAttributeValue("value")).isEqualTo("3");

		assertThat(children.get(3).getAttributeValue("key")).isEqualTo("2");
		assertThat(children.get(3).getAttributeValue("value")).isEqualTo("1");

		assertThat(children.get(5).getAttributeValue("key")).isEqualTo("2");
		assertThat(children.get(5).getAttributeValue("value")).isEqualTo("3");

		assertThat(children.get(7).getAttributeValue("key")).isEqualTo("2");
		assertThat(children.get(7).getAttributeValue("value")).isEqualTo("5");

	}

	@Test
	public void whenAddingNullCollectionSettingsThenElementIsNotAdded() {
		writer.addCollectionsSettings(null);
		assertThat(writer.getDocument().getRootElement().getChildren()).isEmpty();
	}

	@Test
	public void whenWritingValueListsThenValuesAreSaved() {
		ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		Map<Language, String> titleMap = new HashMap<>();
		titleMap.put(Language.French, "domaine1");


		zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl1")
				.setTitle(titleMap)
				.setClassifiedTypes(toListOfString("document", "folder"))
				.setCodeMode("DISABLED"));

		Map<Language, String> titleMap2 = new HashMap<>();
		titleMap2.put(Language.French, "domaine2");

		zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl2")
				.setTitle(titleMap2)
				.setClassifiedTypes(toListOfString("document"))
				.setCodeMode("FACULTATIVE"));

		Map<Language, String> titleMap3 = new HashMap<>();
		titleMap3.put(Language.French, "domaine3");

		zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl3")
				.setTitle(titleMap3)
				.setCodeMode("REQUIRED_AND_UNIQUE")
				.setHierarchical(true));

		Map<Language, String> titleMap4 = new HashMap<>();
		titleMap4.put(Language.French, "domaine4");

		zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl4")
				.setTitle(titleMap4)
				.setHierarchical(false));

		ImportedSettings importedSettings = new ImportedSettings().addCollectionSettings(zeCollectionSettings);
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
		assertThat(ddv1Elem.getAttributeValue(TITLE + Language.French.getCode())).isEqualTo("domaine1");
		assertThat(ddv1Elem.getAttributeValue(CLASSIFIED_TYPES)).isEqualTo("document,folder");
		assertThat(ddv1Elem.getAttributeValue("codeMode")).isEqualTo("DISABLED");
	}

	@Test
	public void whenWritingTaxonomiesThenOK()
			throws IOException {

		ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "taxo1Titre1");

		ImportedTaxonomy taxonomy1 = new ImportedTaxonomy().setCode("taxoT1Type")
				.setTitle(labelTitle1)
				.setClassifiedTypes(toListOfString("document", "folder"))
				.setVisibleOnHomePage(false)
				.setUserIds(asList("user1", "user2"))
				.setGroupIds(asList("group1"));
		zeCollectionSettings.addTaxonomy(taxonomy1);

		Map<Language, String> labelTitle2 = new HashMap<>();
		labelTitle2.put(Language.French, "taxo1Titre2");

		ImportedTaxonomy taxonomy2 = new ImportedTaxonomy().setCode("taxoT2Type")
				.setTitle(labelTitle2);
		zeCollectionSettings.addTaxonomy(taxonomy2);

		ImportedSettings importedSettings = new ImportedSettings().addCollectionSettings(zeCollectionSettings);
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
		assertThat(taxonomy1Elem.getAttributeValue(TITLE + Language.French.getCode())).isEqualTo("taxo1Titre1");
		assertThat(taxonomy1Elem.getAttributeValue(VISIBLE_IN_HOME_PAGE)).isEqualTo("false");
		assertThat(taxonomy1Elem.getAttributeValue(USERS)).isEqualTo("user1,user2");
		assertThat(taxonomy1Elem.getAttributeValue(GROUPS)).isEqualTo("group1");
	}

	@Test
	public void whenWritingTypesWithCalculatedDataEntryTypeMetadataThenOK()
			throws IOException {

		ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		String qualifiedName = "com.constellio.app.modules.rm.model.calculators.FolderExpectedDepositDateCalculator";
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setLabel("titre m1")
				.setType("STRING")
				.setEnabledIn(toListOfString("default", "custom1", "custom2"))
				.setRequiredIn(toListOfString("custom1"))
				.setVisibleInFormIn(toListOfString("default", "custom1"))
				.setDataEntry(ImportedDataEntry.asCalculated(qualifiedName));

		zeCollectionSettings.addType(new ImportedType().setCode("folder").setLabel("Dossier")
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default")
						.addMetadata(m1)));

		ImportedSettings importedSettings = new ImportedSettings().addCollectionSettings(zeCollectionSettings);
		writer.writeSettings(importedSettings);

		List<Element> collectionElements = writer.getDocument().getRootElement().getChildren("collection-settings");
		assertThat(collectionElements).hasSize(1);

		Element zeCollectionElem = collectionElements.get(0);
		assertThat(zeCollectionElem.getAttributeValue(CODE)).isEqualTo(zeCollection);

		List<Element> children = zeCollectionElem.getChildren();

		// types
		Element typesElement = children.get(2);
		// folder type
		Element folderTypeElement = typesElement.getChildren().get(0);
		assertThat(folderTypeElement.getAttributeValue(CODE)).isEqualTo("folder");

		// default-schema
		Element defaultSchemaElem = typesElement.getChildren().get(0).getChild("default-schema");
		assertThat(defaultSchemaElem).isNotNull();

		Element metadata1Elem = defaultSchemaElem.getChildren().get(0);
		assertThat(metadata1Elem.getAttributeValue(CODE)).isEqualTo("m1");
		assertThat(metadata1Elem.getAttributeValue(TITLE)).isEqualTo("titre m1");
		assertThat(metadata1Elem.getAttributeValue(TYPE)).isEqualTo("STRING");

		Element dataEntry = metadata1Elem.getChildren("data-entry").get(0);
		assertThat(dataEntry.getAttributeValue("type")).isEqualTo("calculated");
		assertThat(dataEntry.getAttributeValue("calculator")).isEqualTo(qualifiedName);

		String outputFilePath = "settings-types-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(writer.getDocument(), fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	@Test
	public void whenWritingTypesWithCopiedDataEntryTypeMetadataThenOK()
			throws IOException {

		ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setLabel("titre m1")
				.setType("STRING")
				.setEnabledIn(toListOfString("default", "custom1", "custom2"))
				.setRequiredIn(toListOfString("custom1"))
				.setVisibleInFormIn(toListOfString("default", "custom1"))
				.setDataEntry(ImportedDataEntry.asCopied("category", "title"));

		zeCollectionSettings.addType(new ImportedType().setCode("folder").setLabel("Dossier")
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default")
						.addMetadata(m1)));

		ImportedSettings importedSettings = new ImportedSettings().addCollectionSettings(zeCollectionSettings);
		writer.writeSettings(importedSettings);

		List<Element> collectionElements = writer.getDocument().getRootElement().getChildren("collection-settings");
		assertThat(collectionElements).hasSize(1);

		Element zeCollectionElem = collectionElements.get(0);
		assertThat(zeCollectionElem.getAttributeValue(CODE)).isEqualTo(zeCollection);

		List<Element> children = zeCollectionElem.getChildren();

		// types
		Element typesElement = children.get(2);
		// folder type
		Element folderTypeElement = typesElement.getChildren().get(0);
		assertThat(folderTypeElement.getAttributeValue(CODE)).isEqualTo("folder");

		// default-schema
		Element defaultSchemaElem = typesElement.getChildren().get(0).getChild("default-schema");
		assertThat(defaultSchemaElem).isNotNull();

		Element metadata1Elem = defaultSchemaElem.getChildren().get(0);
		assertThat(metadata1Elem.getAttributeValue(CODE)).isEqualTo("m1");
		assertThat(metadata1Elem.getAttributeValue(TITLE)).isEqualTo("titre m1");
		assertThat(metadata1Elem.getAttributeValue(TYPE)).isEqualTo("STRING");

		Element dataEntry = metadata1Elem.getChildren("data-entry").get(0);
		assertThat(dataEntry.getAttributeValue("type")).isEqualTo("copied");
		assertThat(dataEntry.getAttributeValue("referenceMetadata")).isEqualTo("category");
		assertThat(dataEntry.getAttributeValue("copiedMetadata")).isEqualTo("title");

		String outputFilePath = "settings-types-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(writer.getDocument(), fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	@Test
	public void whenWritingTypesWithJEXLDataEntryTypeMetadataThenOK()
			throws IOException {

		ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		String pattern = "## This is a comment on the first line\n"
						 + "'Prefixe ' + title+ ' Suffixe'\n"
						 + "## This is a comment on the last line";

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setLabel("titre m1")
				.setType("STRING")
				.setEnabledIn(toListOfString("default", "custom1", "custom2"))
				.setRequiredIn(toListOfString("custom1"))
				.setVisibleInFormIn(toListOfString("default", "custom1"))
				.setDataEntry(ImportedDataEntry.asJEXLScript("title").withPattern(pattern));

		zeCollectionSettings.addType(new ImportedType().setCode("folder").setLabel("Dossier")
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default")
						.addMetadata(m1)));

		ImportedSettings importedSettings = new ImportedSettings().addCollectionSettings(zeCollectionSettings);
		writer.writeSettings(importedSettings);

		List<Element> collectionElements = writer.getDocument().getRootElement().getChildren("collection-settings");
		assertThat(collectionElements).hasSize(1);

		Element zeCollectionElem = collectionElements.get(0);
		assertThat(zeCollectionElem.getAttributeValue(CODE)).isEqualTo(zeCollection);

		List<Element> children = zeCollectionElem.getChildren();

		// types
		Element typesElement = children.get(2);
		// folder type
		Element folderTypeElement = typesElement.getChildren().get(0);
		assertThat(folderTypeElement.getAttributeValue(CODE)).isEqualTo("folder");

		// default-schema
		Element defaultSchemaElem = typesElement.getChildren().get(0).getChild("default-schema");
		assertThat(defaultSchemaElem).isNotNull();

		Element metadata1Elem = defaultSchemaElem.getChildren().get(0);
		assertThat(metadata1Elem.getAttributeValue(CODE)).isEqualTo("m1");
		assertThat(metadata1Elem.getAttributeValue(TITLE)).isEqualTo("titre m1");
		assertThat(metadata1Elem.getAttributeValue(TYPE)).isEqualTo("STRING");

		Element dataEntry = metadata1Elem.getChildren("data-entry").get(0);
		assertThat(dataEntry.getAttributeValue("type")).isEqualTo("jexl");
		assertThat(dataEntry.getText()).isEqualTo(pattern);

		String outputFilePath = "settings-types-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(writer.getDocument(), fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	@Test
	public void whenWritingTypesWithSequenceDataEntryTypeMetadataThenOK()
			throws IOException {

		ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setLabel("titre m1")
				.setType("STRING")
				.setEnabledIn(toListOfString("default", "custom1", "custom2"))
				.setRequiredIn(toListOfString("custom1"))
				.setVisibleInFormIn(toListOfString("default", "custom1"))
				.setDataEntry(ImportedDataEntry.asFixedSequence("zeSequence"));

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setLabel("titre m2")
				.setType("STRING").setEnabled(true).setRequired(true).setMultiValue(true)
				.setInputMask("9999-9999")
				.setDataEntry(ImportedDataEntry.asMetadataProvidingSequence("id"));

		zeCollectionSettings.addType(new ImportedType().setCode("folder").setLabel("Dossier")
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default")
						.addMetadata(m1))
				.addSchema(new ImportedMetadataSchema().setCode("custom1")
						.addMetadata(m2)));

		ImportedSettings importedSettings = new ImportedSettings().addCollectionSettings(zeCollectionSettings);
		writer.writeSettings(importedSettings);

		List<Element> collectionElements = writer.getDocument().getRootElement().getChildren("collection-settings");
		assertThat(collectionElements).hasSize(1);

		Element zeCollectionElem = collectionElements.get(0);
		assertThat(zeCollectionElem.getAttributeValue(CODE)).isEqualTo(zeCollection);

		List<Element> children = zeCollectionElem.getChildren();

		// types
		Element typesElement = children.get(2);
		assertThat(typesElement).isNotNull();
		assertThat(typesElement.getChildren()).hasSize(1);

		// folder type
		Element folderTypeElement = typesElement.getChildren().get(0);
		assertThat(folderTypeElement).isNotNull();
		assertThat(folderTypeElement.getAttributeValue(CODE)).isEqualTo("folder");

		// default-schema
		Element defaultSchemaElem = typesElement.getChildren().get(0).getChild("default-schema");
		assertThat(defaultSchemaElem).isNotNull();

		Element metadata1Elem = defaultSchemaElem.getChildren().get(0);
		assertThat(metadata1Elem.getAttributeValue(CODE)).isEqualTo("m1");
		assertThat(metadata1Elem.getAttributeValue(TITLE)).isEqualTo("titre m1");
		assertThat(metadata1Elem.getAttributeValue(TYPE)).isEqualTo("STRING");
		assertThat(metadata1Elem.getAttributeValue(ENABLED)).isNull();
		assertThat(metadata1Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,custom1,custom2");
		assertThat(metadata1Elem.getAttributeValue(REQUIRED)).isNull();
		assertThat(metadata1Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("custom1");
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isEqualTo("default,custom1");
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(TAB)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(MULTI_VALUE)).isNull();
		assertThat(metadata1Elem.getAttributeValue(INPUT_MASK)).isNullOrEmpty();

		Element dataEntry = metadata1Elem.getChildren("data-entry").get(0);
		assertThat(dataEntry.getAttributeValue("type")).isEqualTo("sequence");
		assertThat(dataEntry.getAttributeValue("fixedSequenceCode")).isEqualTo("zeSequence");
		assertThat(dataEntry.getAttributeValue("metadataProvidingSequenceCode")).isNullOrEmpty();

		Element customSchemata = typesElement.getChildren().get(0).getChild("schemas");
		Element schema1Element = customSchemata.getChildren().get(0);
		Element metadata2Elem = schema1Element.getChildren().get(0);
		assertThat(metadata2Elem.getAttributeValue(CODE)).isEqualTo("m2");
		assertThat(metadata2Elem.getAttributeValue(TITLE)).isEqualTo("titre m2");
		assertThat(metadata2Elem.getAttributeValue(TYPE)).isEqualTo("STRING");
		assertThat(metadata2Elem.getAttributeValue(ENABLED)).isEqualTo("true");
		assertThat(metadata2Elem.getAttributeValue(ENABLED_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(REQUIRED)).isEqualTo("true");
		assertThat(metadata2Elem.getAttributeValue(REQUIRED_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_FORM)).isNull();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isNull();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isNull();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_TABLES)).isNull();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(MULTI_VALUE)).isEqualTo("true");
		assertThat(metadata2Elem.getAttributeValue(INPUT_MASK)).isEqualTo("9999-9999");

		String outputFilePath = "settings-types-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(writer.getDocument(), fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	@Test
	public void whenWritingTypesWithAdvancedSequenceDataEntryTypeMetadataThenOK()
			throws IOException {

		ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setLabel("titre m1")
				.setType("STRING")
				.setEnabledIn(toListOfString("default", "custom1", "custom2"))
				.setRequiredIn(toListOfString("custom1"))
				.setVisibleInFormIn(toListOfString("default", "custom1"))
				.setDataEntry(ImportedDataEntry.asAdvancedSequence("zeAdvancedSequenceCalculatorClass"));


		zeCollectionSettings.addType(new ImportedType().setCode("folder").setLabel("Dossier")
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default").addMetadata(m1)));

		ImportedSettings importedSettings = new ImportedSettings().addCollectionSettings(zeCollectionSettings);
		writer.writeSettings(importedSettings);

		List<Element> collectionElements = writer.getDocument().getRootElement().getChildren("collection-settings");
		assertThat(collectionElements).hasSize(1);

		Element zeCollectionElem = collectionElements.get(0);
		assertThat(zeCollectionElem.getAttributeValue(CODE)).isEqualTo(zeCollection);

		List<Element> children = zeCollectionElem.getChildren();

		// types
		Element typesElement = children.get(2);
		assertThat(typesElement).isNotNull();
		assertThat(typesElement.getChildren()).hasSize(1);

		// folder type
		Element folderTypeElement = typesElement.getChildren().get(0);
		assertThat(folderTypeElement).isNotNull();
		assertThat(folderTypeElement.getAttributeValue(CODE)).isEqualTo("folder");

		// default-schema
		Element defaultSchemaElem = typesElement.getChildren().get(0).getChild("default-schema");
		assertThat(defaultSchemaElem).isNotNull();

		Element metadata1Elem = defaultSchemaElem.getChildren().get(0);
		assertThat(metadata1Elem.getAttributeValue(CODE)).isEqualTo("m1");
		assertThat(metadata1Elem.getAttributeValue(TITLE)).isEqualTo("titre m1");
		assertThat(metadata1Elem.getAttributeValue(TYPE)).isEqualTo("STRING");
		assertThat(metadata1Elem.getAttributeValue(ENABLED)).isNull();
		assertThat(metadata1Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,custom1,custom2");
		assertThat(metadata1Elem.getAttributeValue(REQUIRED)).isNull();
		assertThat(metadata1Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("custom1");
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isEqualTo("default,custom1");
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(TAB)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(MULTI_VALUE)).isNull();
		assertThat(metadata1Elem.getAttributeValue(INPUT_MASK)).isNullOrEmpty();

		Element dataEntry = metadata1Elem.getChildren("data-entry").get(0);
		assertThat(dataEntry.getAttributeValue("type")).isEqualTo("advancedSequence");
		assertThat(dataEntry.getAttributeValue("advancedSequenceCalculator")).isEqualTo("zeAdvancedSequenceCalculatorClass");

		String outputFilePath = "settings-types-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(writer.getDocument(), fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	@Test
	public void whenWritingTypesThenElementsPresent()
			throws IOException {

		ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		Map<String, String> tabParams = new TreeMap<>();
		tabParams.put("default", "Métadonnées");
		tabParams.put("zeTab", "Mon onglet");

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setLabel("titre m1")
				.setType("STRING")
				.setEnabledIn(toListOfString("default", "custom1", "custom2"))
				.setRequiredIn(toListOfString("custom1"))
				.setVisibleInFormIn(toListOfString("default", "custom1"));

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setLabel("titre m2")
				.setType("STRING").setEnabled(true).setRequired(true).setMultiValue(true)
				.setTab("zeTab")
				.setInputMask("9999-9999");

		ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setLabel("Titre m3")
				.setType("STRING")
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

		ImportedSettings importedSettings = new ImportedSettings().addCollectionSettings(zeCollectionSettings);
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
		assertThat(tabsChildren.get(0).getAttributeValue(CODE)).isEqualTo("default");
		assertThat(tabsChildren.get(0).getAttributeValue(VALUE)).isEqualTo("Métadonnées");
		assertThat(tabsChildren.get(1).getAttributeValue(CODE)).isEqualTo("zeTab");
		assertThat(tabsChildren.get(1).getAttributeValue(VALUE)).isEqualTo("Mon onglet");

		// default-schema
		Element defaultSchemaElem = typesElement.getChildren().get(0).getChild("default-schema");
		assertThat(defaultSchemaElem).isNotNull();
		assertThat(defaultSchemaElem.getChildren()).hasSize(2);

		Element metadata1Elem = defaultSchemaElem.getChildren().get(0);
		assertThat(metadata1Elem.getAttributeValue(CODE)).isEqualTo("m1");
		assertThat(metadata1Elem.getAttributeValue(TITLE)).isEqualTo("titre m1");
		assertThat(metadata1Elem.getAttributeValue(TYPE)).isEqualTo("STRING");
		assertThat(metadata1Elem.getAttributeValue(ENABLED)).isNull();
		assertThat(metadata1Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,custom1,custom2");
		assertThat(metadata1Elem.getAttributeValue(REQUIRED)).isNull();
		assertThat(metadata1Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("custom1");
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isEqualTo("default,custom1");
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES)).isNull();
		assertThat(metadata1Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(TAB)).isNullOrEmpty();
		assertThat(metadata1Elem.getAttributeValue(MULTI_VALUE)).isNull();
		assertThat(metadata1Elem.getAttributeValue(INPUT_MASK)).isNullOrEmpty();

		Element metadata2Elem = defaultSchemaElem.getChildren().get(1);
		assertThat(metadata2Elem.getAttributeValue(CODE)).isEqualTo("m2");
		assertThat(metadata2Elem.getAttributeValue(TITLE)).isEqualTo("titre m2");
		assertThat(metadata2Elem.getAttributeValue(TYPE)).isEqualTo("STRING");
		assertThat(metadata2Elem.getAttributeValue(ENABLED)).isEqualTo("true");
		assertThat(metadata2Elem.getAttributeValue(ENABLED_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(REQUIRED)).isEqualTo("true");
		assertThat(metadata2Elem.getAttributeValue(REQUIRED_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_FORM)).isNull();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isNull();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isNull();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_TABLES)).isNull();
		assertThat(metadata2Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
		assertThat(metadata2Elem.getAttributeValue(TAB)).isEqualTo("zeTab");
		assertThat(metadata2Elem.getAttributeValue(MULTI_VALUE)).isEqualTo("true");
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
		assertThat(metadata3Elem.getAttributeValue(TYPE)).isEqualTo("STRING");
		assertThat(metadata3Elem.getAttributeValue(ENABLED)).isNull();
		assertThat(metadata3Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,custom1,custom2");
		assertThat(metadata3Elem.getAttributeValue(REQUIRED)).isNull();
		assertThat(metadata3Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("custom1");
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_FORM)).isNull();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isNullOrEmpty();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isNull();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isNull();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_TABLES)).isNull();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
		assertThat(metadata3Elem.getAttributeValue(TAB)).isNullOrEmpty();
		assertThat(metadata3Elem.getAttributeValue(MULTI_VALUE)).isEqualTo("true");
		assertThat(metadata3Elem.getAttributeValue(INPUT_MASK)).isNullOrEmpty();

		String outputFilePath = "settings-types-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(writer.getDocument(), fileOutputStream);
		}

		System.out.println("File Saved!");
	}

	@Test
	public void whenWritingSettingsFileThenElementsPresent()
			throws IOException {

		List<ImportedConfig> configs = new ArrayList<>();
		configs.add(new ImportedConfig().setKey("documentRetentionRules").setValue("true"));
		configs.add((new ImportedConfig().setKey("enforceCategoryAndRuleRelationshipInFolder").setValue("false")));
		configs.add((new ImportedConfig().setKey("calculatedCloseDate").setValue("false")));

		configs.add((new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule").setValue("2015")));
		configs.add((new ImportedConfig().setKey("closeDateRequiredDaysBeforeYearEnd").setValue("15")));

		configs.add((new ImportedConfig().setKey("yearEndDate").setValue("02/28")));

		writer.addGlobalConfigs(configs);

		List<ImportedSequence> sequences = new ArrayList<>();
		sequences.add(new ImportedSequence().setKey("1").setValue("10"));
		sequences.add(new ImportedSequence().setKey("2").setValue("23"));
		sequences.add(new ImportedSequence().setKey("3").setValue("43"));

		writer.addSequences(sequences);

		Map<Language, String> titleMap = new HashMap<>();
		titleMap.put(Language.French, "domaine1");

		ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl1")
				.setTitle(titleMap)
				.setClassifiedTypes(toListOfString("document", "folder"))
				.setCodeMode("DISABLED"));

		Map<Language, String> titleMap2 = new HashMap<>();
		titleMap2.put(Language.French, "domaine2");

		zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl2")
				.setTitle(titleMap2)
				.setClassifiedTypes(toListOfString("document"))
				.setCodeMode("FACULTATIVE"));

		Map<Language, String> titleMap3 = new HashMap<>();
		titleMap3.put(Language.French, "domaine3");

		zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl3")
				.setTitle(titleMap3)
				.setCodeMode("REQUIRED_AND_UNIQUE")
				.setHierarchical(true));

		Map<Language, String> titleMap4 = new HashMap<>();
		titleMap4.put(Language.French, "domaine4");

		zeCollectionSettings.addValueList(new ImportedValueList().setCode("ddvUSRvl4")
				.setTitle(titleMap4)
				.setHierarchical(false));

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "taxo1Titre1");

		ImportedTaxonomy taxonomy1 = new ImportedTaxonomy().setCode("taxoT1Type")
				.setTitle(labelTitle1)
				.setClassifiedTypes(toListOfString("document", "folder"))
				.setVisibleOnHomePage(false)
				.setUserIds(asList("user1", "user2"))
				.setGroupIds(asList("group1"));
		zeCollectionSettings.addTaxonomy(taxonomy1);

		Map<Language, String> labelTitle2 = new HashMap<>();
		labelTitle2.put(Language.French, "taxo1Titre2");

		ImportedTaxonomy taxonomy2 = new ImportedTaxonomy().setCode("taxoT2Type")
				.setTitle(labelTitle2);
		zeCollectionSettings.addTaxonomy(taxonomy2);

		Map<String, String> tabParams = new TreeMap<>();
		tabParams.put("default", "Métadonnées");
		tabParams.put("zeTab", "Mon onglet");

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setLabel("titre m1")
				.setType("STRING")
				.setEnabledIn(toListOfString("default", "custom1", "custom2"))
				.setRequiredIn(toListOfString("custom1"))
				.setVisibleInFormIn(toListOfString("default", "custom1"));

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setLabel("titre m2")
				.setType("STRING").setEnabled(true).setRequired(true)
				.setTab("zeTab").setMultiValue(true)
				.setSearchable(true).setAdvanceSearchable(true).setUnique(true).setUnmodifiable(true)
				.setSortable(true).setRecordAutoComplete(true).setEssential(true).setEssentialInSummary(true)
				.setMultiLingual(true).setDuplicable(true)
				.setInputMask("9999-9999");

		ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setLabel("Titre m3")
				.setType("STRING").setEnabledIn(toListOfString("default", "custom1", "custom2"))
				.setRequiredIn(Arrays.asList("custom1")).setMultiValue(true);
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default")
				.addMetadata(m1)
				.addMetadata(m2);
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom1")
				.addMetadata(m3);
		ImportedType importedType = new ImportedType().setCode("folder").setLabel("Dossier")
				.setTabs(toListOfTabs(tabParams))
				.setDefaultSchema(defaultSchema)
				.addSchema(customSchema);
		zeCollectionSettings.addType(importedType);

		ImportedSettings importedSettings = new ImportedSettings().addCollectionSettings(zeCollectionSettings);
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
		assertThat(ddv1Elem.getAttributeValue(TITLE + Language.French.getCode())).isEqualTo("domaine1");
		assertThat(ddv1Elem.getAttributeValue(CLASSIFIED_TYPES)).isEqualTo("document,folder");
		assertThat(ddv1Elem.getAttributeValue("codeMode")).isEqualTo("DISABLED");

		// taxonomies
		Element taxonomiesElem = children.get(1);
		assertThat(taxonomiesElem.getChildren()).hasSize(2);

		Element taxonomy1Elem = taxonomiesElem.getChildren().get(0);
		assertThat(taxonomy1Elem.getAttributeValue(CODE)).isEqualTo("taxoT1Type");
		assertThat(taxonomy1Elem.getAttributeValue(TITLE + Language.French.getCode())).isEqualTo("taxo1Titre1");
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
		assertThat(tabsChildren.get(0).getAttributeValue(CODE)).isEqualTo("default");
		assertThat(tabsChildren.get(0).getAttributeValue(VALUE)).isEqualTo("Métadonnées");
		assertThat(tabsChildren.get(1).getAttributeValue(CODE)).isEqualTo("zeTab");
		assertThat(tabsChildren.get(1).getAttributeValue(VALUE)).isEqualTo("Mon onglet");

		// default-schema
		Element defaultSchemaElem = typesElement.getChildren().get(0).getChild("default-schema");
		assertThat(defaultSchemaElem).isNotNull();
		assertThat(defaultSchemaElem.getChildren()).hasSize(2);

		Element m1Element = defaultSchemaElem.getChildren().get(0);
		assertThat(m1Element.getAttributeValue(CODE)).isEqualTo("m1");
		assertThat(m1Element.getAttributeValue(TITLE)).isEqualTo("titre m1");
		assertThat(m1Element.getAttributeValue(TYPE)).isEqualTo("STRING");
		assertThat(m1Element.getAttributeValue(ENABLED)).isNull();
		assertThat(m1Element.getAttributeValue(ENABLED_IN)).isEqualTo("default,custom1,custom2");
		assertThat(m1Element.getAttributeValue(REQUIRED)).isNull();
		assertThat(m1Element.getAttributeValue(REQUIRED_IN)).isEqualTo("custom1");
		assertThat(m1Element.getAttributeValue(VISIBLE_IN_FORM)).isNull();
		assertThat(m1Element.getAttributeValue(VISIBLE_IN_FORM_IN)).isEqualTo("default,custom1");
		assertThat(m1Element.getAttributeValue(VISIBLE_IN_DISPLAY)).isNull();
		assertThat(m1Element.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
		assertThat(m1Element.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isNull();
		assertThat(m1Element.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
		assertThat(m1Element.getAttributeValue(VISIBLE_IN_TABLES)).isNull();
		assertThat(m1Element.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
		assertThat(m1Element.getAttributeValue(TAB)).isNullOrEmpty();
		assertThat(m1Element.getAttributeValue(MULTI_VALUE)).isNull();
		assertThat(m1Element.getAttributeValue(INPUT_MASK)).isNullOrEmpty();

		Element m2Element = defaultSchemaElem.getChildren().get(1);
		assertThat(m2Element.getAttributeValue(CODE)).isEqualTo("m2");
		assertThat(m2Element.getAttributeValue(TITLE)).isEqualTo("titre m2");
		assertThat(m2Element.getAttributeValue(TYPE)).isEqualTo("STRING");
		assertThat(m2Element.getAttributeValue(ENABLED)).isEqualTo("true");
		assertThat(m2Element.getAttributeValue(ENABLED_IN)).isNullOrEmpty();
		assertThat(m2Element.getAttributeValue(REQUIRED)).isEqualTo("true");
		assertThat(m2Element.getAttributeValue(REQUIRED_IN)).isNullOrEmpty();
		assertThat(m2Element.getAttributeValue(VISIBLE_IN_FORM)).isNull();
		assertThat(m2Element.getAttributeValue(VISIBLE_IN_FORM_IN)).isNullOrEmpty();
		assertThat(m2Element.getAttributeValue(VISIBLE_IN_DISPLAY)).isNull();
		assertThat(m2Element.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
		assertThat(m2Element.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isNull();
		assertThat(m2Element.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
		assertThat(m2Element.getAttributeValue(VISIBLE_IN_TABLES)).isNull();
		assertThat(m2Element.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
		assertThat(m2Element.getAttributeValue(TAB)).isEqualTo("zeTab");
		assertThat(m2Element.getAttributeValue(MULTI_VALUE)).isEqualTo("true");
		assertThat(m2Element.getAttributeValue(INPUT_MASK)).isEqualTo("9999-9999");

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
		assertThat(metadata3Elem.getAttributeValue(TYPE)).isEqualTo("STRING");
		assertThat(metadata3Elem.getAttributeValue(ENABLED)).isNull();
		assertThat(metadata3Elem.getAttributeValue(ENABLED_IN)).isEqualTo("default,custom1,custom2");
		assertThat(metadata3Elem.getAttributeValue(REQUIRED)).isNull();
		assertThat(metadata3Elem.getAttributeValue(REQUIRED_IN)).isEqualTo("custom1");
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_FORM)).isNull();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_FORM_IN)).isNullOrEmpty();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_DISPLAY)).isNull();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_DISPLAY_IN)).isNullOrEmpty();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_SEARCH_RESULT)).isNull();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_RESULT_IN)).isNullOrEmpty();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_TABLES)).isNull();
		assertThat(metadata3Elem.getAttributeValue(VISIBLE_IN_TABLES_IN)).isNullOrEmpty();
		assertThat(metadata3Elem.getAttributeValue(TAB)).isNullOrEmpty();
		assertThat(metadata3Elem.getAttributeValue(MULTI_VALUE)).isEqualTo("true");
		assertThat(metadata3Elem.getAttributeValue(INPUT_MASK)).isNullOrEmpty();

		String outputFilePath = "settings-output.xml";
		File outputFile = new File(newTempFolder(), outputFilePath);

		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
		try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
			xmlOutputter.output(writer.getDocument(), fileOutputStream);
		}

		System.out.println("File Saved!");
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
