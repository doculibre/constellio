package com.constellio.app.services.importExport.settings.utils;

import com.constellio.app.services.importExport.settings.SettingsImportServicesTestUtils;
import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException;
import com.constellio.model.entities.Language;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SettingsXMLFileReaderRealTest extends SettingsImportServicesTestUtils {

	private Document document;
	private Document document2;
	private SettingsXMLFileReader reader;
	private SettingsXMLFileReader reader2;

	@Before
	public void setup() {
		document = getDocument("settings-input.xml");
		document2 = getDocument("settings-input2.xml");
		reader = new SettingsXMLFileReader(document, getModelLayerFactory());
		reader2 = new SettingsXMLFileReader(document2, getModelLayerFactory());
	}

	@Test
	public void givenAValiddocumentWithMultiLingualTaxonomieTitle() {
		ImportedSettings importedSettings = reader2.read();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();


		java.util.Map<Language, String> titleLanguage1 = collectionSettings.get(0).getTaxonomies().get(0).getTitle();
		java.util.Map<Language, String> titleLanguage2 = collectionSettings.get(0).getTaxonomies().get(1).getTitle();


		assertThat(titleLanguage1.get(Language.French)).isEqualTo("taxo1Titre1Fr");
		assertThat(titleLanguage1.get(Language.English)).isEqualTo("taxo1Title1En");

		assertThat(titleLanguage2.get(Language.French)).isEqualTo("taxo2Titre2Fr");
		assertThat(titleLanguage2.get(Language.English)).isEqualTo("taxo2Title2En");
	}

	@Test
	public void givenAValiddocumentWithDDvMultiLingualTitle() {
		ImportedSettings importedSettings = reader2.read();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();


		java.util.Map<Language, String> titleLanguage1 = collectionSettings.get(0).getValueLists().get(0).getTitle();
		java.util.Map<Language, String> titleLanguage2 = collectionSettings.get(0).getValueLists().get(1).getTitle();
		java.util.Map<Language, String> titleLanguage3 = collectionSettings.get(0).getValueLists().get(2).getTitle();
		java.util.Map<Language, String> titleLanguage4 = collectionSettings.get(0).getValueLists().get(3).getTitle();

		assertThat(titleLanguage1.get(Language.French)).isEqualTo("domaine1fr");
		assertThat(titleLanguage1.get(Language.English)).isEqualTo("domaine1en");

		assertThat(titleLanguage2.get(Language.French)).isEqualTo("domaine2fr");
		assertThat(titleLanguage2.get(Language.English)).isEqualTo("domaine2en");

		assertThat(titleLanguage3.get(Language.French)).isEqualTo("domaine3fr");
		assertThat(titleLanguage3.get(Language.English)).isEqualTo("domaine3en");

		assertThat(titleLanguage4.get(Language.French)).isEqualTo("domaine4fr");
		assertThat(titleLanguage4.get(Language.English)).isEqualTo("domaine4en");
	}

	@Test
	public void givenAValidDocumentWhenReadingThenSequencesAreOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		// configs
		List<ImportedSequence> sequences = importedSettings.getSequences();
		assertThat(sequences.get(0).getKey()).isEqualTo("1");
		assertThat(sequences.get(0).getValue()).isEqualTo("12");

		assertThat(sequences.get(1).getKey()).isEqualTo("2");
		assertThat(sequences.get(1).getValue()).isEqualTo("10");

		assertThat(sequences.get(2).getKey()).isEqualTo("3");
		assertThat(sequences.get(2).getValue()).isEqualTo("23");

		assertThat(sequences.get(3).getKey()).isEqualTo("4");
		assertThat(sequences.get(3).getValue()).isEqualTo("51");
	}

	@Test
	public void givenAValidDocumentWhenParsingThenGlobalConfigsAreOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		// configs
		List<ImportedConfig> configs = importedSettings.getConfigs();
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
	}

	@Test
	public void givenAValidSettingsDocumentWhenReadingCollectionsTheOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();
		assertThat(collectionSettings).hasSize(2);

		// zeCollection
		ImportedCollectionSettings zeCollectionSettings = collectionSettings.get(0);
		assertThat(zeCollectionSettings).isNotNull();
		assertThat(zeCollectionSettings.getCode()).isEqualTo(zeCollection);

		// anotherCollection
		ImportedCollectionSettings anotherCollectionSettings = collectionSettings.get(1);
		assertThat(anotherCollectionSettings).isNotNull();
		assertThat(anotherCollectionSettings.getCode()).isEqualTo("anotherCollection");

	}

	@Test
	public void givenAValidSettingsDocumentWhenReadingZeCollectionsValueListsTheOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();
		assertThat(collectionSettings).hasSize(2);

		// zeCollection
		ImportedCollectionSettings zeCollectionSettings = collectionSettings.get(0);
		assertThat(zeCollectionSettings).isNotNull();
		assertThat(zeCollectionSettings.getCode()).isEqualTo(zeCollection);

		List<ImportedValueList> list1 = zeCollectionSettings.getValueLists();
		assertThat(list1.size()).isEqualTo(4);

		ImportedValueList vl1 = list1.get(0);
		assertThat(vl1.getCode()).isEqualTo("ddvUSRvl1");
		assertThat(vl1.getTitle().get(Language.French)).isEqualTo("domaine1");
		assertThat(vl1.getClassifiedTypes()).containsExactly("document", "folder");
		assertThat(vl1.getCodeMode()).isEqualTo("DISABLED");
		assertThat(vl1.getHierarchical()).isFalse();

		ImportedValueList vl2 = list1.get(1);
		assertThat(vl2.getCode()).isEqualTo("ddvUSRvl2");
		assertThat(vl2.getTitle().get(Language.French)).isEqualTo("domaine2");
		assertThat(vl2.getClassifiedTypes()).containsExactly("document");
		assertThat(vl2.getCodeMode()).isEqualTo("FACULTATIVE");
		assertThat(vl2.getHierarchical()).isFalse();

		ImportedValueList vl3 = list1.get(2);
		assertThat(vl3.getCode()).isEqualTo("ddvUSRvl3");
		assertThat(vl3.getTitle().get(Language.French)).isEqualTo("domaine3");
		assertThat(vl3.getClassifiedTypes()).isEmpty();
		assertThat(vl3.getCodeMode()).isEqualTo("REQUIRED_AND_UNIQUE");
		assertThat(vl3.getHierarchical()).isTrue();

		ImportedValueList vl4 = list1.get(3);
		assertThat(vl4.getCode()).isEqualTo("ddvUSRvl4");
		assertThat(vl4.getTitle().get(Language.French)).isEqualTo("domaine4");
		assertThat(vl4.getClassifiedTypes()).isEmpty();
		assertThat(vl4.getCodeMode()).isNull();
		assertThat(vl4.getHierarchical()).isFalse();

	}

	@Test
	public void givenAValidSettingsDocumentWhenReadingAnotherCollectionsValueListsTheOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();
		assertThat(collectionSettings).hasSize(2);

		// anotherCollection
		ImportedCollectionSettings anotherCollectionSettings = collectionSettings.get(1);
		assertThat(anotherCollectionSettings).isNotNull();
		assertThat(anotherCollectionSettings.getCode()).isEqualTo("anotherCollection");

		List<ImportedValueList> list2 = anotherCollectionSettings.getValueLists();
		assertThat(list2.size()).isEqualTo(1);

		ImportedValueList vl5 = list2.get(0);
		assertThat(vl5.getCode()).isEqualTo("ddvUSRvl4");
		assertThat(vl5.getTitle().get(Language.French)).isEqualTo("domaine4");
		assertThat(vl5.getClassifiedTypes()).isEmpty();
		assertThat(vl5.getCodeMode()).isEqualTo("DISABLED");
		assertThat(vl5.getHierarchical()).isTrue();
	}

	@Test
	public void givenAValidSettingsDocumentWhenReadingZeCollectionTaxonomiesTheOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();
		assertThat(collectionSettings).hasSize(2);

		// zeCollection
		ImportedCollectionSettings zeCollectionSettings = collectionSettings.get(0);
		assertThat(zeCollectionSettings).isNotNull();
		assertThat(zeCollectionSettings.getCode()).isEqualTo(zeCollection);

		List<ImportedTaxonomy> list1 = zeCollectionSettings.getTaxonomies();
		assertThat(list1.size()).isEqualTo(2);

		ImportedTaxonomy taxo1 = list1.get(0);
		assertThat(taxo1.getCode()).isEqualTo("taxoT1Type");
		assertThat(taxo1.getTitle().get(Language.French)).isEqualTo("taxo1Titre1");
		assertThat(taxo1.getClassifiedTypes()).containsExactly("document", "folder");
		assertThat(taxo1.getGroupIds()).containsExactly("group1");
		assertThat(taxo1.getUserIds()).containsExactly("user1", "user2");
		assertThat(taxo1.getVisibleOnHomePage()).isFalse();

		ImportedTaxonomy taxo2 = list1.get(1);
		assertThat(taxo2.getCode()).isEqualTo("taxoT2Type");
		assertThat(taxo2.getTitle().get(Language.French)).isEqualTo("taxo1Titre2");
		assertThat(taxo2.getClassifiedTypes()).isEmpty();
		assertThat(taxo2.getGroupIds()).isEmpty();
		assertThat(taxo2.getUserIds()).isEmpty();
		assertThat(taxo2.getVisibleOnHomePage()).isTrue();
	}

	@Test
	public void givenAValidSettingsDocumentWhenReadingAnotherCollectionTaxonomiesTheOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();
		assertThat(collectionSettings).hasSize(2);

		// anotherCollection
		ImportedCollectionSettings anotherCollectionSettings = collectionSettings.get(1);
		assertThat(anotherCollectionSettings).isNotNull();
		assertThat(anotherCollectionSettings.getCode()).isEqualTo("anotherCollection");

		List<ImportedTaxonomy> list2 = anotherCollectionSettings.getTaxonomies();
		assertThat(list2.size()).isEqualTo(1);

		ImportedTaxonomy taxo3 = list2.get(0);
		assertThat(taxo3.getCode()).isEqualTo("taxoT3Type");
		assertThat(taxo3.getTitle().get(Language.French)).isEqualTo("taxo1Titre3");
		assertThat(taxo3.getClassifiedTypes()).containsExactly("document", "folder");
		assertThat(taxo3.getGroupIds()).containsExactly("group1");
		assertThat(taxo3.getUserIds()).containsExactly("user1", "user2");
		assertThat(taxo3.getVisibleOnHomePage()).isNull();

	}

	@Test
	public void givenAValidDocumentWhenReadingCollectionTypeTabsThenOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();
		assertThat(collectionSettings).hasSize(2);

		// zeCollection
		ImportedCollectionSettings anotherCollectionSettings = collectionSettings.get(0);

		List<ImportedType> list1 = anotherCollectionSettings.getTypes();
		assertThat(list1.size()).isEqualTo(2);

		ImportedType folderType = list1.get(0);
		assertThat(folderType.getCode()).isEqualTo("folder");

		// tabs
		assertThat(folderType.getTabs()).hasSize(2);
		ImportedTab tab1 = folderType.getTabs().get(0);
		assertThat(tab1.getCode()).isEqualTo("zeTab");
		assertThat(tab1.getValue()).isEqualTo("Mon onglet");

		ImportedTab tab2 = folderType.getTabs().get(1);
		assertThat(tab2.getCode()).isEqualTo("default");
		assertThat(tab2.getValue()).isEqualTo("Métadonnées");
	}

	@Test
	public void givenAValidDocumentWhenReadingCollectionDefaultSchemaThenOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();
		assertThat(collectionSettings).hasSize(2);

		// zeCollection
		ImportedCollectionSettings anotherCollectionSettings = collectionSettings.get(0);

		List<ImportedType> list1 = anotherCollectionSettings.getTypes();
		assertThat(list1.size()).isEqualTo(2);

		ImportedType folderType = list1.get(0);
		assertThat(folderType.getCode()).isEqualTo("folder");

		// default-schema
		ImportedMetadataSchema folderDefaultSchema = folderType.getDefaultSchema();
		List<ImportedMetadata> folderDefaultMetadata = folderDefaultSchema.getAllMetadata();
		ImportedMetadata m1 = folderDefaultMetadata.get(0);
		assertThat(m1.getCode()).isEqualTo("m1");
		assertThat(m1.getLabel()).isEqualTo("titre m1");
		assertThat(m1.getType()).isEqualTo("STRING");
		assertThat(m1.getEnabledIn()).containsExactly("default", "custom1", "custom2");
		assertThat(m1.getVisibleInFormIn()).containsExactly("default", "custom1");

		ImportedMetadata m2 = folderDefaultMetadata.get(1);
		assertThat(m2.getCode()).isEqualTo("m2");
		assertThat(m2.getLabel()).isEqualTo("titre m2");
		assertThat(m2.getType()).isEqualTo("STRING");
		assertThat(m2.getDuplicable()).isTrue();
		assertThat(m2.getEnabled()).isTrue();
		assertThat(m2.getInputMask()).isEqualTo("9999-9999");
		assertThat(m2.getMultiLingual()).isTrue();
		assertThat(m2.getEssential()).isTrue();
		assertThat(m2.getEssentialInSummary()).isTrue();
		assertThat(m2.getRecordAutoComplete()).isTrue();
		assertThat(m2.getSearchable()).isTrue();
		assertThat(m2.getSortable()).isTrue();
		assertThat(m2.getUnique()).isTrue();
		assertThat(m2.getUnmodifiable()).isTrue();
		assertThat(m2.getAdvanceSearchable()).isTrue();
		assertThat(m2.getMultiValue()).isTrue();
		assertThat(m2.getRequired()).isTrue();
		assertThat(m2.getTab()).isEqualTo("zeTab");
	}

	@Test
	public void givenAValidDocumentWhenReadingMetadataThenDataEntryTypesOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();
		assertThat(collectionSettings).hasSize(2);

		// zeCollection
		ImportedCollectionSettings anotherCollectionSettings = collectionSettings.get(0);

		List<ImportedType> list1 = anotherCollectionSettings.getTypes();
		assertThat(list1.size()).isEqualTo(2);

		ImportedType folderType = list1.get(0);
		assertThat(folderType.getCode()).isEqualTo("folder");

		// default-schema
		ImportedMetadataSchema folderDefaultSchema = folderType.getDefaultSchema();
		List<ImportedMetadata> folderDefaultMetadata = folderDefaultSchema.getAllMetadata();
		// manual
		ImportedMetadata m1 = folderDefaultMetadata.get(0);
		assertThat(m1.getCode()).isEqualTo("m1");
		assertThat(m1.getLabel()).isEqualTo("titre m1");
		assertThat(m1.getType()).isEqualTo("STRING");
		assertThat(m1.getEnabledIn()).containsExactly("default", "custom1", "custom2");
		assertThat(m1.getVisibleInFormIn()).containsExactly("default", "custom1");

		//
		ImportedMetadata m6 = folderDefaultMetadata.get(5);
		assertThat(m6.getCode()).isEqualTo("m6");
		assertThat(m6.getLabel()).isEqualTo("titre m6");
		assertThat(m6.getType()).isEqualTo("STRING");
		assertThat(m6.getDuplicable()).isTrue();
		assertThat(m6.getEnabled()).isTrue();
		assertThat(m6.getInputMask()).isEqualTo("9999-9999");
		assertThat(m6.getMultiLingual()).isTrue();
		assertThat(m6.getEssential()).isTrue();
		assertThat(m6.getEssentialInSummary()).isTrue();
		assertThat(m6.getRecordAutoComplete()).isTrue();
		assertThat(m6.getSearchable()).isTrue();
		assertThat(m6.getSortable()).isTrue();
		assertThat(m6.getUnique()).isTrue();
		assertThat(m6.getUnmodifiable()).isTrue();
		assertThat(m6.getAdvanceSearchable()).isTrue();
		assertThat(m6.getMultiValue()).isTrue();
		assertThat(m6.getRequired()).isTrue();
		assertThat(m6.getTab()).isEqualTo("zeTab");
	}

	@Test
	public void givenAValidDocumentWhenReadingZeCollectionCustomSchemataThenOK() {
		ImportedSettings importedSettings = reader.read();
		assertThat(importedSettings).isNotNull();

		List<ImportedCollectionSettings> collectionSettings = importedSettings.getCollectionsSettings();
		assertThat(collectionSettings).hasSize(2);

		// zeCollection
		ImportedCollectionSettings anotherCollectionSettings = collectionSettings.get(0);

		List<ImportedType> list1 = anotherCollectionSettings.getTypes();
		assertThat(list1.size()).isEqualTo(2);

		ImportedType folderType = list1.get(0);
		assertThat(folderType.getCode()).isEqualTo("folder");

		// custom-schema
		List<ImportedMetadataSchema> list = folderType.getCustomSchemata();
		assertThat(list).hasSize(1);

		ImportedMetadataSchema folderCustomSchema = list.get(0);
		List<ImportedMetadata> customSchemaMetadata = folderCustomSchema.getAllMetadata();
		assertThat(customSchemaMetadata).hasSize(1);
		ImportedMetadata m3 = customSchemaMetadata.get(0);
		assertThat(m3.getCode()).isEqualTo("m7");
		assertThat(m3.getLabel()).isEqualTo("Titre m7");
		assertThat(m3.getType()).isEqualTo("STRING");
		assertThat(m3.getEnabledIn()).containsExactly("default", "custom1", "custom2");
		assertThat(m3.getRequiredIn()).containsExactly("custom1");
		assertThat(m3.getVisibleInFormIn()).isEmpty();
		assertThat(m3.getMultiValue()).isTrue();
	}

	public Document getDocument(String fileSetting) {
		String inputFilePath = getTestResourceFile(fileSetting).getPath();
		File inputFile = new File(inputFilePath);
		SAXBuilder builder = new SAXBuilder();
		try {
			return builder.build(inputFile);
		} catch (JDOMException e) {
			throw new ConfigManagerRuntimeException("JDOM2 Exception", e);
		} catch (IOException e) {
			throw new ConfigManagerRuntimeException.CannotCompleteOperation("build Document JDOM2 from file", e);
		}
	}
}
