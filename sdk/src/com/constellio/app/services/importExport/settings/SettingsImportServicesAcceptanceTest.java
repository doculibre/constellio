package com.constellio.app.services.importExport.settings;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.extensions.AppLayerExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.calculators.FolderExpectedDepositDateCalculator;
import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedDataEntry;
import com.constellio.app.services.importExport.settings.model.ImportedMetadata;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataSchema;
import com.constellio.app.services.importExport.settings.model.ImportedRegexConfigs;
import com.constellio.app.services.importExport.settings.model.ImportedSequence;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.model.ImportedTaxonomy;
import com.constellio.app.services.importExport.settings.model.ImportedType;
import com.constellio.app.services.importExport.settings.model.ImportedValueList;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileReader;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileWriter;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.dao.managers.config.ConfigManagerRuntimeException;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.JEXLMetadataValueCalculator;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.ListAssert;
import org.assertj.core.data.MapEntry;
import org.assertj.core.groups.Tuple;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.modules.rm.wrappers.Folder.DESCRIPTION;
import static com.constellio.model.entities.Language.English;
import static com.constellio.model.entities.Language.French;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.RegexConfig.RegexConfigType.SUBSTITUTION;
import static com.constellio.model.entities.schemas.RegexConfig.RegexConfigType.TRANSFORMATION;
import static com.constellio.model.services.schemas.SchemaUtils.localCodes;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class SettingsImportServicesAcceptanceTest extends SettingsImportServicesTestUtils {

	static final String FOLDER = "folder";
	static final String DOCUMENT = "document";
	static final String TITLE_FR = "Le titre du domaine de valeurs 1";
	static final String TITLE_EN = "First value list's title";
	static final String TITLE_FR_UPDATED = "Nouveau titre du domaine de valeurs 1";
	static final String TITLE_EN_UPDATED = "First value list's updated title";
	static final String TAXO_1_TITLE_FR = "Le titre de la taxonomie 1";
	static final String TAXO_1_TITLE_FR_UPDATED = "Nouveau titre de la taxonomie 1";
	static final String TAXO_1_TITLE_EN = "First taxonomy's title";
	static final String TAXO_2_TITLE_FR = "Le titre de la taxonomie 2";
	static final String TAXO_2_TITLE_EN = "Second taxonomy's title";
	static final String CODE_1_VALUE_LIST = "ddvUSRcodeDuDomaineDeValeur1";
	static final String CODE_2_VALUE_LIST = "ddvUSRcodeDuDomaineDeValeur2";
	static final String CODE_3_VALUE_LIST = "ddvUSRcodeDuDomaineDeValeur3";
	static final String CODE_4_VALUE_LIST = "ddvUSRcodeDuDomaineDeValeur4";
	static final List<String> TAXO_USERS = asList("gandalf", "edouard");
	static final List<String> TAXO_GROUPS = asList("heroes");
	static final String TAXO_1_CODE = "taxoMyFirstType";
	static final String TAXO_2_CODE = "taxoMySecondType";
	static final String CODE_METADATA_2 = "metadata2";
	static final String TITLE_METADATA_2 = "Titre métadonnée no.2";
	static final String CODE_METADATA_1 = "metadata1";
	static final String TITLE_METADATA_1 = "Titre métadonnée no.1";
	static final String CODE_SCHEMA_1 = "USRschema1";
	static final String CODE_SCHEMA_2 = "USRschema2";
	static final String CODE_DEFAULT_SCHEMA = "default";
	private static final String LABEL_FR = "Label en français";
	private static final String LABEL_EN = "Label en anglais";
	List<String> metadataCodes;
	Users users = new Users();

	//-------------------------------------------------------------------------------------
	SystemConfigurationsManager systemConfigurationsManager;
	MetadataSchemasManager metadataSchemasManager;
	SchemasDisplayManager schemasDisplayManager;
	LabelTemplateManager labelTemplateManager;
	boolean runTwice;
	SettingsImportServices services;
	ImportedSettings settings = new ImportedSettings();
	ImportedCollectionSettings zeCollectionSettings;
	ImportedCollectionSettings anotherCollectionSettings;
	@Mock
	AppLayerFactory appLayerFactory;
	@Mock
	AppLayerExtensions extensions;
	@Mock
	AppLayerSystemExtensions systemExtensions;

	@Test
	public void whenImportingLabelTemplatesThenCorrectlyImported()
			throws ValidationException {
		when(appLayerFactory.getExtensions()).thenReturn(extensions);
		when(extensions.getSystemWideExtensions()).thenReturn(systemExtensions);

		settings.addImportedLabelTemplate(getTestResourceContent("template1.xml"));
		importSettings();

		assertThat(labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE)).extracting("name").containsOnly(
				"Code de plan justifié à droite (Avery 5159)", "Code de plan justifié à droite (Avery 5161)",
				"Code de plan justifié à droite (Avery 5162)", "Code de plan justifié à droite (Avery 5163)",
				"Code de plan justifié à gauche (Avery 5159)", "Code de plan justifié à gauche (Avery 5161)",
				"Code de plan justifié à gauche (Avery 5162)", "Code de plan justifié à gauche (Avery 5163)",
				"Ze template #1");

		settings = new ImportedSettings();
		settings.addImportedLabelTemplate(getTestResourceContent("template1b.xml"));
		settings.addImportedLabelTemplate(getTestResourceContent("template2.xml"));
		importSettings();
		assertThat(labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE)).extracting("name").containsOnly(
				"Code de plan justifié à droite (Avery 5159)", "Code de plan justifié à droite (Avery 5161)",
				"Code de plan justifié à droite (Avery 5162)", "Code de plan justifié à droite (Avery 5163)",
				"Code de plan justifié à gauche (Avery 5159)", "Code de plan justifié à gauche (Avery 5161)",
				"Code de plan justifié à gauche (Avery 5162)", "Code de plan justifié à gauche (Avery 5163)",
				"Ze template #1b", "Ze template #2");

		assertThat(new LabelTemplateManager(getDataLayerFactory().getConfigManager(), appLayerFactory)
				.listTemplates(Folder.SCHEMA_TYPE)).extracting("name").containsOnly(
				"Code de plan justifié à droite (Avery 5159)", "Code de plan justifié à droite (Avery 5161)",
				"Code de plan justifié à droite (Avery 5162)", "Code de plan justifié à droite (Avery 5163)",
				"Code de plan justifié à gauche (Avery 5159)", "Code de plan justifié à gauche (Avery 5161)",
				"Code de plan justifié à gauche (Avery 5162)", "Code de plan justifié à gauche (Avery 5163)",
				"Ze template #1b", "Ze template #2");

		runTwice = false;
	}

	@Test
	public void whenImportingMetadataWithCopiedDataEntryTypeThenOK()
			throws ValidationException {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedDataEntry importedDataEntry =
				ImportedDataEntry.asCopied("category", "title");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setDataEntry(importedDataEntry);

		defaultSchema.addMetadata(m1);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata metadata1 = schemaType.getDefaultSchema().get("folder_default_m1");
		assertThat(metadata1).isNotNull();

		DataEntry dataEntry = metadata1.getDataEntry();

		assertThat(dataEntry).isNotNull();
		assertThat(dataEntry.getType()).isEqualTo(DataEntryType.COPIED);
		CopiedDataEntry copiedDataEntry = (CopiedDataEntry) dataEntry;

		assertThat(copiedDataEntry.getCopiedMetadata()).isEqualTo("category_default_title");
		assertThat(copiedDataEntry.getReferenceMetadata()).isEqualTo("folder_default_category");

		//newWebDriver();
		//waitUntilICloseTheBrowsers();

	}

	@Test

	public void whenImportingMetadataWithCalculatedDataEntryWithoutArgumentsThenOK()
			throws ValidationException {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedDataEntry importedDataEntry =
				ImportedDataEntry
						.asCalculated("com.constellio.app.modules.rm.model.calculators.FolderExpectedDepositDateCalculator");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("DATE")
				.setDataEntry(importedDataEntry);

		defaultSchema.addMetadata(m1);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata metadata1 = schemaType.getDefaultSchema().get("folder_default_m1");
		assertThat(metadata1).isNotNull();

		DataEntry dataEntry = metadata1.getDataEntry();

		assertThat(dataEntry).isNotNull();
		assertThat(dataEntry.getType()).isEqualTo(DataEntryType.CALCULATED);
		CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) dataEntry;

		MetadataValueCalculator<?> calculator = calculatedDataEntry.getCalculator();
		assertThat(calculator).isInstanceOf(FolderExpectedDepositDateCalculator.class);

		assertThat(calculator.getReturnType()).isEqualTo(MetadataValueType.DATE);

	}

	@Test

	public void whenImportingMetadataWithJEXLDataEntryCodeThenOK()
			throws ValidationException {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		String pattern = "## This is a comment on the first line\n"
						 + "'Prefixe ' + title+ ' Suffixe'\n"
						 + "## This is a comment on the last line";
		ImportedDataEntry importedDataEntry1 = ImportedDataEntry.asJEXLScript(pattern);
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setDataEntry(importedDataEntry1);

		defaultSchema.addMetadata(m1);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();
		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata metadata1 = schemaType.getDefaultSchema().get("folder_default_m1");
		assertThat(metadata1).isNotNull();
		assertThat(metadata1.getLabel(French)).isEqualTo("m1");
		assertThat(metadata1.getType()).isEqualTo(STRING);
		assertThat(metadata1.getInputMask()).isNullOrEmpty();

		DataEntry dataEntry = metadata1.getDataEntry();

		assertThat(dataEntry).isNotNull();
		assertThat(dataEntry.getType()).isEqualTo(DataEntryType.CALCULATED);
		CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) dataEntry;

		MetadataValueCalculator<?> calculator = calculatedDataEntry.getCalculator();
		assertThat(calculator).isInstanceOf(JEXLMetadataValueCalculator.class);
		assertThat(calculator.getReturnType()).isEqualTo(STRING);
		assertThat(((JEXLMetadataValueCalculator) calculator).getExpression()).isEqualTo(pattern);

	}

	@Test
	public void whenImportingMetadataWithSequenceDataEntryCodeThenOK()
			throws ValidationException {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedDataEntry importedDataEntry1 = ImportedDataEntry.asFixedSequence("zeSequence");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setDataEntry(importedDataEntry1);

		ImportedDataEntry importedDataEntry2 = ImportedDataEntry.asMetadataProvidingSequence("id");
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING")
				.setDataEntry(importedDataEntry2);

		defaultSchema.addMetadata(m1).addMetadata(m2);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata metadata1 = schemaType.getDefaultSchema().get("folder_default_m1");
		assertThat(metadata1).isNotNull();
		assertThat(metadata1.getLabel(French)).isEqualTo("m1");
		assertThat(metadata1.getType()).isEqualTo(STRING);
		assertThat(metadata1.getInputMask()).isNullOrEmpty();

		DataEntry dataEntry = metadata1.getDataEntry();

		assertThat(dataEntry).isNotNull();
		assertThat(dataEntry.getType()).isEqualTo(DataEntryType.SEQUENCE);
		assertThat(((SequenceDataEntry) dataEntry).getFixedSequenceCode()).isEqualTo("zeSequence");
		assertThat(((SequenceDataEntry) dataEntry).getMetadataProvidingSequenceCode()).isNullOrEmpty();

		Metadata metadata2 = schemaType.getDefaultSchema().get("folder_default_m2");
		assertThat(metadata2).isNotNull();
		assertThat(metadata2.getLabel(French)).isEqualTo("m2");
		assertThat(metadata2.getType()).isEqualTo(STRING);
		assertThat(metadata2.getInputMask()).isNullOrEmpty();

		DataEntry dataEntry2 = metadata2.getDataEntry();

		assertThat(dataEntry2).isNotNull();
		assertThat(dataEntry2.getType()).isEqualTo(DataEntryType.SEQUENCE);
		assertThat(((SequenceDataEntry) dataEntry2).getFixedSequenceCode()).isNullOrEmpty();
		assertThat(((SequenceDataEntry) dataEntry2).getMetadataProvidingSequenceCode()).isEqualTo("id");

	}

	@Test
	public void whenImportingUnknownConfigsThenConfigsAreNotSet()
			throws Exception {

		i18n.setLocale(ENGLISH);
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateUnknown").setValue("true"));

		assertThatErrorsWhileImportingSettingsExtracting("config").contains(
				tuple("SettingsImportServices_configurationNotFound", "calculatedCloseDateUnknown"));

		assertThatErrorsContainsLocalizedMessagesWhileImportingSettings("calculatedCloseDateUnknown")
				.doesNotContain("Aucune configuration n'existe pour le code 'calculatedCloseDateUnknown'")
				.contains("No configuration was found for code calculatedCloseDateUnknown");

	}

	@Test
	public void givenFrenchLocaleWhenImportBadBooleanConfigValueThenValidationExceptionThrown()
			throws Exception {

		i18n.setLocale(Locale.FRENCH);

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("notABoolean"));

		assertThatErrorsWhileImportingSettingsExtracting("config", "value").containsOnly(
				tuple("SettingsImportServices_invalidConfigurationValue", "calculatedCloseDate", "notABoolean"));

		assertThatErrorsContainsLocalizedMessagesWhileImportingSettings()
				.containsOnly("La valeur de la configuration est vide ou nulle")
				.doesNotContain("The configuration's value is empty or null");

	}

	@Test
	public void givenEnglishLocalewhenImportingBadIntegerConfigValueThenValidationExceptionThrown()
			throws Exception {

		i18n.setLocale(ENGLISH);

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule")
				.setValue("helloInteger"));

		assertThatErrorsWhileImportingSettingsExtracting("config", "value").containsOnly(
				tuple("SettingsImportServices_invalidConfigurationValue",
						"calculatedCloseDateNumberOfYearWhenFixedRule", "helloInteger"));

		assertThatErrorsContainsLocalizedMessagesWhileImportingSettings()
				.containsOnly("The value of the configuration is empty or null")
				.doesNotContain("La valeur de la configuration est vide ou nulle");

	}

	@Test
	public void giveEnglishLocaleWhenImportingNullValueConfigsThenNullValueExceptionIsRaised()
			throws Exception {

		i18n.setLocale(ENGLISH);
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue(null));

		assertThatErrorsWhileImportingSettingsExtracting("config").contains(
				tuple("SettingsImportServices_invalidConfigurationValue", "calculatedCloseDate"));

		assertThatErrorsContainsLocalizedMessagesWhileImportingSettings()
				.containsOnly("The value of the configuration is empty or null")
				.doesNotContain("La valeur de la configuration est vide ou nulle");
	}

	@Test
	public void whenImportConfigsThenSetted()
			throws Exception {
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("false"));

		settings.addConfig(new ImportedConfig().setKey("documentRetentionRules").setValue("true"));
		settings.addConfig(new ImportedConfig().setKey("enforceCategoryAndRuleRelationshipInFolder").setValue("false"));
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("false"));

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule").setValue("2015"));
		settings.addConfig(new ImportedConfig().setKey("closeDateRequiredDaysBeforeYearEnd").setValue("15"));

		settings.addConfig(new ImportedConfig().setKey("yearEndDate").setValue("02/28"));

		settings.addConfig(new ImportedConfig().setKey("decommissioningDateBasedOn").setValue("OPEN_DATE"));

		importSettings();

		assertThat(systemConfigurationsManager.<Boolean>getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);
		assertThat(systemConfigurationsManager.<Boolean>getValue(RMConfigs.DOCUMENT_RETENTION_RULES)).isEqualTo(true);
		assertThat(systemConfigurationsManager.<Boolean>getValue(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER))
				.isEqualTo(false);
		assertThat(systemConfigurationsManager.<Boolean>getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);

		assertThat(systemConfigurationsManager.<Integer>getValue(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE))
				.isEqualTo(2015);
		assertThat(systemConfigurationsManager.<Integer>getValue(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR))
				.isEqualTo(15);

		assertThat(systemConfigurationsManager.<String>getValue(RMConfigs.YEAR_END_DATE)).isEqualTo("02/28");

		assertThat(systemConfigurationsManager.<DecommissioningDateBasedOn>getValue(RMConfigs.DECOMMISSIONING_DATE_BASED_ON))
				.isEqualTo(DecommissioningDateBasedOn.OPEN_DATE);
	}

	@Test
	public void givenFrenchLocaleWhenImportSequencesWithEmptySequenceIdThenError()
			throws Exception {

		i18n.setLocale(Locale.FRENCH);
		settings.addSequence(new ImportedSequence().setKey("").setValue("1"));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_sequenceIdNullOrEmpty"));

		assertThatErrorsContainsLocalizedMessagesWhileImportingSettings()
				.contains("L'identifiant de la séquence est vide ou null")
				.doesNotContain("The id of the sequence is null or empty");
	}

	@Test
	public void givenFrenchLocaleWhenImportSequencesWithNonNumericalValueThenError()
			throws Exception {

		i18n.setLocale(Locale.FRENCH);
		settings.addSequence(new ImportedSequence().setKey("1").setValue("a"));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_sequenceValueNotNumerical"));

		assertThatErrorsContainsLocalizedMessagesWhileImportingSettings()
				.doesNotContain("The value of the sequence is non numerical")
				.contains("La valeur de la séquence n'est pas numérique");
	}

	@Test
	public void givenFrenchLocalizationWhenImportSequencesWithNonNumericalValueThenFrenchErrorMessage()
			throws Exception {

		i18n.setLocale(Locale.FRENCH);
		settings.addSequence(new ImportedSequence().setKey("1").setValue("a"));

		assertThatErrorsContainsLocalizedMessagesWhileImportingSettings("calculatedCloseDateUnknown")
				.containsOnly("La valeur de la séquence n'est pas numérique")
				.doesNotContain("The sequence's value is non numerical");

	}

	@Test
	public void givenEnglishLocalizationWhenImportSequencesWithNonNumericalValueThenEnglishErrorMessage()
			throws Exception {

		i18n.setLocale(ENGLISH);
		settings.addSequence(new ImportedSequence().setKey("1").setValue("a"));

		assertThatErrorsContainsLocalizedMessagesWhileImportingSettings("calculatedCloseDateUnknown")
				.contains("The value of the sequence is non numerical")
				.doesNotContain("La valeur de la séquence n'est pas numérique");

	}

	@Test

	public void whenImportSequencesThenOK()
			throws Exception {
		//TODO AFTER-TEST-VALIDATION-SEQ
		givenDisabledAfterTestValidations();

		settings.addSequence(new ImportedSequence().setKey("1").setValue("1"));

		settings.addSequence(new ImportedSequence().setKey("2").setValue("7"));

		importSettings();

		SequencesManager sequencesManager = getAppLayerFactory().getModelLayerFactory().getDataLayerFactory()
				.getSequencesManager();

		assertThat(sequencesManager.getLastSequenceValue("1")).isEqualTo(1);
		assertThat(sequencesManager.next("1")).isEqualTo(2);
		assertThat(sequencesManager.getLastSequenceValue("2")).isEqualTo(7);

		//newWebDriver();
		//waitUntilICloseTheBrowsers();

	}

	@Test
	public void givenFrenchLocaleWhenImportingConfigSettingsIfCollectionCodeIsEmptyThenExceptionIsRaised()
			throws Exception {

		i18n.setLocale(Locale.FRENCH);

		Map<Language, String> titleMap = new HashMap<>();
		titleMap.put(Language.French, "Le titre du domaine de valeurs 1");

		settings.addCollectionSettings(new ImportedCollectionSettings().setCode("")
				.addValueList(new ImportedValueList().setCode("ddvUSRcodeDuDomaineDeValeur1")
						.setTitle(titleMap)
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)));

		assertThatErrorsContainsLocalizedMessagesWhileImportingSettings("calculatedCloseDateUnknown")
				.containsOnly("Le code de la collection est vide ou nul")
				.doesNotContain("The collection's code is empty or null");

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_invalidCollectionCode"));
	}

	@Test
	public void whenImportingConfigSettingsIfCollectionCodeDoesNotExistThenExceptionIsRaised()
			throws Exception {

		Map<Language, String> titleMap = new HashMap<>();
		titleMap.put(Language.French, "Le titre du domaine de valeurs 1");

		settings.addCollectionSettings(new ImportedCollectionSettings().setCode("unknonCollection")
				.addValueList(new ImportedValueList().setCode("ddvUSRcodeDuDomaineDeValeur1")
						.setTitle(titleMap)
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_collectionCodeNotFound"));

	}

	@Test
	public void whenImportingValueListIfCodeIsInvalidThenExceptionIsRaised()
			throws Exception {

		Map<Language, String> titleMap = new HashMap<>();
		titleMap.put(Language.French, "Le titre du domaine de valeurs 1");

		settings.addCollectionSettings(new ImportedCollectionSettings().setCode(zeCollection)
				.addValueList(new ImportedValueList().setCode(null)
						.setTitle(titleMap)
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_InvalidValueListCode"));

	}

	@Test
	public void whenImportingValueListIfCodeDoesNotStartWithDDVPrefixThenExceptionIsRaised()
			throws Exception {

		Map<Language, String> titleMap = new HashMap<>();
		titleMap.put(Language.French, "Le titre du domaine de valeurs 1");

		settings.addCollectionSettings(new ImportedCollectionSettings().setCode(zeCollection)
				.addValueList(new ImportedValueList().setCode("USRcodeDuDomaineDeValeur1")
						.setTitle(titleMap)
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_InvalidValueListCode"));

	}

	@Test
	public void whenImportingValueListsThenSet()
			throws Exception {

		Map<Language, String> titleMap = new HashMap<>();
		titleMap.put(Language.French, TITLE_FR);
		titleMap.put(Language.English, TITLE_EN);

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedValueList v1 = new ImportedValueList().setCode(CODE_1_VALUE_LIST)
				.setTitle(titleMap)
				.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
				.setCodeMode("DISABLED");
		collectionSettings.addValueList(v1);

		Map<Language, String> titleMap2 = new HashMap<>();
		titleMap2.put(Language.French, "Le titre du domaine de valeurs 2");
		titleMap2.put(Language.English, "Second value list's updated title");

		ImportedValueList v2 = new ImportedValueList().setCode(CODE_2_VALUE_LIST)
				.setTitle(titleMap2)
				.setClassifiedTypes(toListOfString(DOCUMENT))
				.setCodeMode("FACULTATIVE");
		collectionSettings.addValueList(v2);

		Map<Language, String> titleMap3 = new HashMap<>();
		titleMap3.put(Language.French, "Le titre du domaine de valeurs 3");
		titleMap3.put(Language.English, "Third value list's updated title");

		ImportedValueList v3 = new ImportedValueList().setCode(CODE_3_VALUE_LIST)
				.setTitle(titleMap3)
				.setCodeMode("REQUIRED_AND_UNIQUE").setHierarchical(true);
		collectionSettings.addValueList(v3);

		Map<Language, String> titleMap4 = new HashMap<>();
		titleMap4.put(Language.French, "Le titre du domaine de valeurs 4");
		titleMap4.put(Language.English, "Forth value list's updated title");

		ImportedValueList v4 = new ImportedValueList().setCode(CODE_4_VALUE_LIST)
				.setTitle(titleMap4)
				.setHierarchical(false);
		collectionSettings.addValueList(v4);

		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(CODE_1_VALUE_LIST);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(French)).isEqualTo(TITLE_FR);
		assertThat(metadataSchemaType.getLabels().get(English)).isEqualTo(TITLE_EN);
		Metadata codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isFalse();
		assertThat(codeMetadata.isUniqueValue()).isFalse();
		assertThat(codeMetadata.isEnabled()).isFalse();
		assertThat(metadataSchemaType.getDefaultSchema().hasMetadataWithCode("parent")).isFalse();

		metadataSchemaType = schemaTypes.getSchemaType(CODE_2_VALUE_LIST);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(French)).isEqualTo("Le titre du domaine de valeurs 2");
		assertThat(metadataSchemaType.getLabels().get(English)).isEqualTo("Second value list's updated title");
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isFalse();
		assertThat(codeMetadata.isUniqueValue()).isFalse();
		assertThat(codeMetadata.isEnabled()).isTrue();
		assertThat(metadataSchemaType.getDefaultSchema().hasMetadataWithCode("parent")).isFalse();

		metadataSchemaType = schemaTypes.getSchemaType(CODE_3_VALUE_LIST);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(French)).isEqualTo("Le titre du domaine de valeurs 3");
		assertThat(metadataSchemaType.getLabels().get(English)).isEqualTo("Third value list's updated title");
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isTrue();
		assertThat(codeMetadata.isUniqueValue()).isTrue();
		assertThat(codeMetadata.isEnabled()).isTrue();
		assertThat(metadataSchemaType.getDefaultSchema().hasMetadataWithCode("parent")).isTrue();
		Metadata parentMetadata = metadataSchemaType.getDefaultSchema().get("parent");
		assertThat(parentMetadata).isNotNull();
		assertThat(parentMetadata.getType()).isEqualTo(MetadataValueType.REFERENCE);

		metadataSchemaType = schemaTypes.getSchemaType(CODE_4_VALUE_LIST);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(French)).isEqualTo("Le titre du domaine de valeurs 4");
		assertThat(metadataSchemaType.getLabels().get(English)).isEqualTo("Forth value list's updated title");
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isTrue();
		assertThat(codeMetadata.isUniqueValue()).isTrue();
		assertThat(codeMetadata.isEnabled()).isTrue();
		assertThat(metadataSchemaType.getDefaultSchema().hasMetadataWithCode("parent")).isFalse();

		//newWebDriver();
		//waitUntilICloseTheBrowsers();
	}

	@Test
	public void whenImportingMultiligualMetadatasThenSet() throws ValidationException {
		Map<Language, String> labelMap = new HashMap<>();
		labelMap.put(Language.French, LABEL_FR);
		labelMap.put(Language.English, LABEL_EN);
		ImportedMetadata metadata = new ImportedMetadata().setCode("USRm").setType(STRING).setLabels(labelMap);

		importMetadata(metadata);
		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(Folder.SCHEMA_TYPE);

		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().get(French)).isEqualTo(LABEL_FR);
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().get(English)).isEqualTo(LABEL_EN);
	}

	@Test
	public void whenModifiyingValueOfLablesMetadatasThenValueIsUpdated() throws ValidationException {
		Map<Language, String> labelMap = new HashMap<>();
		labelMap.put(Language.French, LABEL_FR);
		labelMap.put(Language.English, LABEL_EN);
		ImportedMetadata metadata = new ImportedMetadata().setCode("USRm").setType(STRING).setLabels(labelMap);

		importMetadata(metadata);
		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(Folder.SCHEMA_TYPE);

		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().get(French)).isEqualTo(LABEL_FR);
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().get(English)).isEqualTo(LABEL_EN);

		labelMap = new HashMap<>();
		labelMap.put(Language.French, "Label modifié fr");
		labelMap.put(Language.English, "Label modifié en");
		metadata.setCode("USRm").setType(STRING).setLabels(labelMap);

		importMetadata(metadata);
		schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		metadataSchemaType = schemaTypes.getSchemaType(Folder.SCHEMA_TYPE);

		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().get(French)).isEqualTo("Label modifié fr");
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().get(English)).isEqualTo("Label modifié en");

	}


	@Test
	public void whenImportingMetadataWithOnlyFrenchLabelThenSet() throws ValidationException {
		Map<Language, String> labelMap = new HashMap<>();
		labelMap.put(Language.French, LABEL_FR);
		ImportedMetadata metadata = new ImportedMetadata().setCode("USRm").setType(STRING).setLabels(labelMap);

		importMetadata(metadata);
		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(Folder.SCHEMA_TYPE);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().get(French)).isEqualTo(LABEL_FR);
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().keySet()).doesNotContain(English);
	}

	@Test
	public void whenImportingMetadataAndSettingOneValueLabelThenFrenchLabelIsSet() throws ValidationException {
		ImportedMetadata metadata = new ImportedMetadata().setCode("USRm").setType(STRING).setLabel("label");

		importMetadata(metadata);
		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(Folder.SCHEMA_TYPE);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().get(French)).isEqualTo("label");
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().keySet()).doesNotContain(English);
	}

	@Test
	public void whenImportingMetadataWithOnlyEnglishLableThenSet() throws ValidationException {
		Map<Language, String> labelMap = new HashMap<>();
		labelMap.put(Language.English, LABEL_EN);
		ImportedMetadata metadata = new ImportedMetadata().setCode("USRm").setType(STRING).setLabels(labelMap);

		importMetadata(metadata);
		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(Folder.SCHEMA_TYPE);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getMetadata("folder_default_USRm").getLabels().get(English)).isEqualTo(LABEL_EN);
	}

	private void importMetadata(ImportedMetadata metadata) throws ValidationException {
		ImportedCollectionSettings collectionSettings = settings.newCollectionSettings(zeCollection);
		ImportedType folderSchemaType = collectionSettings.newType(Folder.SCHEMA_TYPE);
		ImportedMetadataSchema defaultFoldersSchema = folderSchemaType.newDefaultSchema();

		defaultFoldersSchema.addMetadata(metadata);
		settings.add(collectionSettings);
		importSettings();
	}

	@Test
	public void whenImportingMultiligualCustomSchemasThenSet() throws ValidationException {
		Map<Language, String> labelMap = new HashMap<>();
		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		labelMap.put(Language.French, LABEL_FR);
		labelMap.put(Language.English, LABEL_EN);
		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Ze Type label");
		ImportedMetadataSchema importedMetadataSchema = folderType.newSchema("custom").setLabels(labelMap);
		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");

		assertThat(schemaType.getCustomSchema("custom").getLabels().get(French)).isEqualTo(LABEL_FR);
		assertThat(schemaType.getCustomSchema("custom").getLabels().get(English)).isEqualTo(LABEL_EN);
	}

	@Test
	public void whenImportingMultiligualSchemaTypeThenSet() throws ValidationException {
		Map<Language, String> labelMap = new HashMap<>();
		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		labelMap.put(Language.French, LABEL_FR);
		labelMap.put(Language.English, LABEL_EN);
		ImportedType folderType = new ImportedType().setCode("folder").setLabels(labelMap);
		ImportedMetadataSchema importedMetadataSchema = folderType.newSchema("custom");
		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");

		assertThat(schemaType.getLabels().get(French)).isEqualTo(LABEL_FR);
		assertThat(schemaType.getLabels().get(English)).isEqualTo(LABEL_EN);
	}

	@Test
	public void whenImportingMultiligualDefaultSchemasThenSet() throws ValidationException {
		Map<Language, String> labelMap = new HashMap<>();
		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		labelMap.put(Language.French, LABEL_FR);
		labelMap.put(Language.English, LABEL_EN);
		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Ze Type label");
		ImportedMetadataSchema defaultSchema = folderType.newDefaultSchema().setCode("default").setLabels(labelMap);
		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");

		assertThat(schemaType.getDefaultSchema().getLabels().get(French)).isEqualTo(LABEL_FR);
		assertThat(schemaType.getDefaultSchema().getLabels().get(English)).isEqualTo(LABEL_EN);
	}

	@Test
	public void whenImportingMultiligualDefaultSchemasWithOnlyEnglishThenSet() throws ValidationException {
		Map<Language, String> labelMap = new HashMap<>();
		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		labelMap.put(Language.English, LABEL_EN);
		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Ze Type label");
		ImportedMetadataSchema defaultSchema = folderType.newDefaultSchema().setCode("default").setLabels(labelMap);
		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");

		assertThat(schemaType.getDefaultSchema().getLabels().get(English)).isEqualTo(LABEL_EN);
	}

	@Test
	public void whenModifyingValueListTitleThenValueIsUpdated()
			throws Exception {

		Map<Language, String> titleMap = new HashMap<>();
		titleMap.put(Language.French, TITLE_FR);

		String codeA = "ddvUSRcodeDuDomaineDeValeurA";
		ImportedValueList valueList = new ImportedValueList().setCode(codeA)
				.setTitle(titleMap)
				.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
				.setCodeMode("DISABLED");

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		collectionSettings.addValueList(valueList);

		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(codeA);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(French)).isEqualTo(TITLE_FR);

		Map<Language, String> titleMap2 = new HashMap<>();
		titleMap2.put(Language.French, TITLE_FR_UPDATED);

		valueList.setTitle(titleMap2)
				.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
				.setCodeMode("DISABLED");

		importSettings();

		metadataSchemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType(codeA);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(French)).isEqualTo(TITLE_FR_UPDATED);

	}

	@Test

	public void whenModifyingValueListCodeModeThenValueIsUpdated()
			throws Exception {

		Map<Language, String> titleMap = new HashMap<>();
		titleMap.put(Language.French, TITLE_FR);

		String codeA = "ddvUSRcodeDuDomaineDeValeurA";
		ImportedValueList valueList = new ImportedValueList().setCode(codeA)
				.setTitle(titleMap)
				.setCodeMode("DISABLED");

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		collectionSettings.addValueList(valueList);

		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaTypes schemaTypes = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(codeA);
		Metadata codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isFalse();
		assertThat(codeMetadata.isEnabled()).isFalse();

		assertThat(metadataSchemaType).isNotNull();

		valueList.setCodeMode("REQUIRED_AND_UNIQUE");
		collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		collectionSettings.addValueList(valueList);
		importSettings();

		schemaTypes = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		metadataSchemaType = schemaTypes.getSchemaType(codeA);
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata(CODE);
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isTrue();
		assertThat(codeMetadata.isUniqueValue()).isTrue();
		assertThat(codeMetadata.isEnabled()).isTrue();

	}

	@Test
	public void whenImportingTaxonomyConfigSettingsIfTaxonomyCodeIsEmptyThenExceptionIsRaised()
			throws Exception {

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, TAXO_1_TITLE_FR);


		settings.addCollectionSettings(new ImportedCollectionSettings().setCode(zeCollection)
				.addTaxonomy(new ImportedTaxonomy().setCode(null)
						.setTitle(labelTitle1)
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
						.setVisibleOnHomePage(true)
						.setUserIds(TAXO_USERS)
						.setGroupIds(TAXO_GROUPS)
				));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_EmptyTaxonomyCode"));

	}

	@Test
	public void whenImportingTaxonomyConfigSettingsIfTaxonomyCodePrefixIsInvalidThenExceptionIsRaised()
			throws Exception {

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, TAXO_1_TITLE_FR);

		settings.addCollectionSettings(new ImportedCollectionSettings().setCode(zeCollection)
				.addTaxonomy(new ImportedTaxonomy().setCode("anotherPrefixTaxonomy")
						.setTitle(labelTitle1)
						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
						.setVisibleOnHomePage(true)
						.setUserIds(TAXO_USERS)
						.setGroupIds(TAXO_GROUPS)
				));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_InvalidTaxonomyCodePrefix"));
	}

	//TODO REMOVED VERIFICATION, SHOULD CHECK TAXONOMY SCHEMATYPE CODE INSTEAD OF TAXONOMY CODE
	//	@Test
	//	public void whenImportingTaxonomyConfigSettingsIfTaxonomyCodeSuffixIsInvalidThenExceptionIsRaised()
	//			throws Exception {
	//
	//		settings.addCollectionSettings(new ImportedCollectionSettings().setCode(zeCollection)
	//				.addTaxonomy(new ImportedTaxonomy().setCode("taxoPrefixTaxonomy")
	//						.setTitle(TAXO_1_TITLE_FR)
	//						.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
	//						.setVisibleOnHomePage(true)
	//						.setUserIds(TAXO_USERS)
	//						.setGroupIds(TAXO_GROUPS)
	//				));
	//
	//		assertThatErrorsWhileImportingSettingsExtracting()
	//				.contains(tuple("SettingsImportServices_InvalidTaxonomyCodeSuffix"));
	//
	//	}

	@Test
	public void whenImportingTaxonomyConfigSettingsThenConfigsAreSaved()
			throws Exception {

		zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, TAXO_1_TITLE_FR);

		Map<Language, String> labelTitle2 = new HashMap<>();
		labelTitle2.put(Language.French, TAXO_2_TITLE_FR);

		ImportedTaxonomy importedTaxonomy1 = new ImportedTaxonomy().setCode(TAXO_1_CODE.replace("Type", ""))
				.setTitle(labelTitle1)
				.setClassifiedTypes(toListOfString("document", "folder"))
				.setVisibleOnHomePage(false)
				.setUserIds(asList(gandalf, bobGratton))
				.setGroupIds(asList("group1"));
		zeCollectionSettings.addTaxonomy(importedTaxonomy1);

		ImportedTaxonomy importedTaxonomy2 = new ImportedTaxonomy().setCode(TAXO_2_CODE.replace("Type", ""))
				.setTitle(labelTitle2);
		zeCollectionSettings.addTaxonomy(importedTaxonomy2);

		settings.addCollectionSettings(zeCollectionSettings);

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
		assertThat(metadataSchemaType).isNotNull();

		Taxonomy taxonomy1 = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy1).isNotNull();
		assertThat(taxonomy1.getTitle().get(Language.French)).isEqualTo(TAXO_1_TITLE_FR);
		assertThat(taxonomy1.isVisibleInHomePage()).isFalse();
		assertThat(taxonomy1.getGroupIds()).hasSize(1).containsExactly("group1");
		assertThat(taxonomy1.getUserIds()).hasSize(2).containsExactly(gandalf, bobGratton);

		MetadataSchema folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
		List<Metadata> references = folderSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy1));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaTypeCode").containsOnly(TAXO_1_CODE);

		MetadataSchema documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
		references = documentSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy1));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaTypeCode").containsOnly(TAXO_1_CODE);

		Taxonomy taxonomy2 = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_2_CODE);

		assertThat(taxonomy2).isNotNull();
		assertThat(taxonomy2.getTitle().get(Language.French)).isEqualTo(TAXO_2_TITLE_FR);
		assertThat(taxonomy2.isVisibleInHomePage()).isTrue();
		assertThat(taxonomy2.getGroupIds()).isEmpty();
		assertThat(taxonomy2.getUserIds()).isEmpty();

		references = folderSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy2));
		assertThat(references).isEmpty();

		documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
		references = documentSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy2));
		assertThat(references).isEmpty();

	}

	@Test
	public void whenModifyingTaxonomyClassifiedTypesThenOK()
			throws Exception {

		zeCollectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, TAXO_1_TITLE_FR);

		ImportedTaxonomy importedTaxonomy1 = new ImportedTaxonomy().setCode(TAXO_1_CODE.replace("Type", ""))
				.setTitle(labelTitle1)
				.setClassifiedTypes(toListOfString("document", "folder"));
		zeCollectionSettings.addTaxonomy(importedTaxonomy1);

		settings.addCollectionSettings(zeCollectionSettings);

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
		assertThat(metadataSchemaType).isNotNull();

		Taxonomy taxonomy1 = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy1).isNotNull();
		assertThat(taxonomy1.getTitle().get(Language.French)).isEqualTo(TAXO_1_TITLE_FR);

		MetadataSchema folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
		List<Metadata> references = folderSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy1));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaTypeCode").containsOnly(TAXO_1_CODE);

		MetadataSchema documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
		references = documentSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy1));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaTypeCode").containsOnly(TAXO_1_CODE);

		importedTaxonomy1.setClassifiedTypes(toListOfString("document"));

		importSettings();

		taxonomy1 = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
		references = folderSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy1));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaTypeCode").containsOnly(TAXO_1_CODE);

		documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
		references = documentSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy1));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaTypeCode").containsOnly(TAXO_1_CODE);

	}

	@Test
	public void whenModifyingCollectionTaxonomyTitleThenConfigsAreUpdated()
			throws Exception {

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, TAXO_1_TITLE_FR);

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);
		ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(TAXO_1_CODE.replace("Type", ""))
				.setTitle(labelTitle1);

		settings.addCollectionSettings(collectionSettings.addTaxonomy(importedTaxonomy));

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
		assertThat(metadataSchemaType).isNotNull();

		Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.getTitle().get(Language.French)).isEqualTo(TAXO_1_TITLE_FR);

		Map<Language, String> labelTitle2 = new HashMap<>();
		labelTitle2.put(Language.French, TAXO_1_TITLE_FR_UPDATED);

		// modify title
		collectionSettings.addTaxonomy(importedTaxonomy
				.setTitle(labelTitle2));

		importSettings();

		taxonomy = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.getTitle().get(Language.French)).isEqualTo(TAXO_1_TITLE_FR_UPDATED);
	}

	@Test
	public void whenModifyingTaxonomyVisibleInHomePageThenConfigsAreUpdated()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);
		ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(TAXO_1_CODE.replace("Type", ""))
				.setVisibleOnHomePage(false);

		settings.addCollectionSettings(collectionSettings.addTaxonomy(importedTaxonomy));

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
		assertThat(metadataSchemaType).isNotNull();

		Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.isVisibleInHomePage()).isFalse();

		importedTaxonomy.setVisibleOnHomePage(true);
		collectionSettings.addTaxonomy(importedTaxonomy);

		importSettings();

		taxonomy = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.isVisibleInHomePage()).isTrue();
	}

	@Test

	public void whenModifyingCollectionTaxonomyUsersAndGroupsThenConfigsAreUpdated()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);
		ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(TAXO_1_CODE.replace("Type", ""))
				.setUserIds(asList(gandalf, robin))
				.setGroupIds(asList("group1"));

		settings.addCollectionSettings(collectionSettings.addTaxonomy(importedTaxonomy));

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
		assertThat(metadataSchemaType).isNotNull();

		Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.getGroupIds()).hasSize(1).containsExactly("group1");
		assertThat(taxonomy.getUserIds()).hasSize(2).containsExactly(gandalf, robin);

		//newWebDriver();
		//waitUntilICloseTheBrowsers();

		collectionSettings.addTaxonomy(importedTaxonomy
				.setUserIds(asList(aliceWonderland))
				.setGroupIds(asList("group2", "group3")));

		importSettings();

		taxonomy = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.getGroupIds()).hasSize(2).containsExactly("group2", "group3");
		assertThat(taxonomy.getUserIds()).hasSize(1).containsExactly(aliceWonderland);
	}

	@Test
	public void whenImportingTaxonomyThenClassifiedInIsSet()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, TAXO_1_TITLE_FR);

		ImportedTaxonomy importedTaxonomy = new ImportedTaxonomy().setCode(TAXO_1_CODE.replace("Type", ""))
				.setTitle(labelTitle1)
				.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER));

		settings.addCollectionSettings(collectionSettings.addTaxonomy(importedTaxonomy));

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(TAXO_1_CODE);
		assertThat(metadataSchemaType).isNotNull();

		Taxonomy taxonomy = getAppLayerFactory().getModelLayerFactory()
				.getTaxonomiesManager().getTaxonomyFor(zeCollection, TAXO_1_CODE);

		assertThat(taxonomy).isNotNull();
		assertThat(taxonomy.getTitle().get(Language.French)).isEqualTo(TAXO_1_TITLE_FR);

		MetadataSchema folderSchemaType = schemaTypes.getDefaultSchema(FOLDER);
		List<Metadata> references = folderSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaTypeCode").containsOnly(TAXO_1_CODE);

		MetadataSchema documentSchemaType = schemaTypes.getDefaultSchema(DOCUMENT);
		references = documentSchemaType.getTaxonomyRelationshipReferences(asList(taxonomy));
		assertThat(references).hasSize(1);
		assertThat(references).extracting("referencedSchemaTypeCode").containsOnly(TAXO_1_CODE);
	}

	@Test
	public void whenImportingTypesIfCodeIsEmptyThenExceptionIsRaised()
			throws Exception {

		settings.addCollectionSettings(new ImportedCollectionSettings().setCode(zeCollection)
				.addType(new ImportedType().setCode(null).setLabel("Dossier"))
		);

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_emptyTypeCode"));

	}

	@Test
	public void whenImportingTypeTabIfCodeIsEmptyThenExceptionIsRaised()
			throws Exception {

		Map<String, String> tabParams = new HashMap<>();
		tabParams.put("default", "Métadonnées");
		tabParams.put("", "Mon onglet");

		ImportedType importedType = new ImportedType().setCode("folder")
				.setLabel("Dossier").setTabs(toListOfTabs(tabParams));

		settings.addCollectionSettings(new ImportedCollectionSettings().setCode(zeCollection)
				.addType(importedType));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_emptyTabCode"));

	}

	@Test
	public void whenImportingMetadataThenAdvancedSearchSet()
			throws Exception {

		Map<String, String> tabParams = getTabsMap();

		ImportedMetadata m1 = new ImportedMetadata().setCode("USRm1").setType(STRING).setAdvanceSearchable(true);
		ImportedMetadata m2 = new ImportedMetadata().setCode("USRm2").setType(STRING).setAdvanceSearchable(false);

		ImportedType importedType = new ImportedType().setCode(Folder.SCHEMA_TYPE).setLabel("Dossier");
		importedType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
		settings.addCollectionSettings(new ImportedCollectionSettings().setCode(zeCollection).addType(importedType));
		importSettings();
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_USRm1").isVisibleInAdvancedSearch()).isTrue();
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_USRm2").isVisibleInAdvancedSearch()).isFalse();

		m1 = new ImportedMetadata().setCode("USRm1").setType(STRING);
		m2 = new ImportedMetadata().setCode("USRm2").setType(STRING);
		importedType = new ImportedType().setCode(Folder.SCHEMA_TYPE).setLabel("Dossier");
		importedType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
		settings = new ImportedSettings().add(new ImportedCollectionSettings().setCode(zeCollection).addType(importedType));
		importSettings();
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_USRm1").isVisibleInAdvancedSearch()).isTrue();
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_USRm2").isVisibleInAdvancedSearch()).isFalse();

		m1 = new ImportedMetadata().setCode("USRm1").setType(STRING).setAdvanceSearchable(false);
		m2 = new ImportedMetadata().setCode("USRm2").setType(STRING).setAdvanceSearchable(true);
		importedType = new ImportedType().setCode(Folder.SCHEMA_TYPE).setLabel("Dossier");
		importedType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
		settings = new ImportedSettings().add(new ImportedCollectionSettings().setCode(zeCollection).addType(importedType));
		importSettings();
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_USRm1").isVisibleInAdvancedSearch()).isFalse();
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_USRm2").isVisibleInAdvancedSearch()).isTrue();
	}

	@Test
	public void whenImportingTypeIfCustomSchemasCodeIsEmptyThenExceptionIsRaised()
			throws Exception {

		Map<String, String> tabParams = getTabsMap();

		ImportedMetadata m1 = new ImportedMetadata().setCode(CODE_METADATA_1).setLabel(TITLE_METADATA_1)
				.setType("STRING")
				.setEnabledIn(toListOfString(CODE_DEFAULT_SCHEMA, CODE_SCHEMA_1, CODE_SCHEMA_2))
				.setRequiredIn(toListOfString(CODE_SCHEMA_1))
				.setVisibleInFormIn(toListOfString(CODE_DEFAULT_SCHEMA, CODE_SCHEMA_1));

		ImportedMetadata m2 = new ImportedMetadata().setCode(CODE_METADATA_2).setLabel(TITLE_METADATA_2)
				.setType("STRING")
				.setEnabled(true)
				.setRequired(true)
				.setTab("zeTab")
				.setMultiValue(true)
				.setInputMask("9999-9999");

		ImportedMetadata m3 = new ImportedMetadata().setCode("metadata3").setLabel("Titre métadonnée no.3")
				.setType("STRING")
				.setEnabledIn(toListOfString("default", CODE_SCHEMA_1, CODE_SCHEMA_2))
				.setRequiredIn(Arrays.asList(CODE_SCHEMA_1))
				.setMultiValue(true);

		ImportedMetadataSchema importedMetadataSchema = new ImportedMetadataSchema().setCode("default")
				.addMetadata(m1)
				.addMetadata(m2);

		ImportedType importedType = new ImportedType().setCode(Folder.SCHEMA_TYPE).setLabel("Dossier")
				.setTabs(toListOfTabs(tabParams))
				.setDefaultSchema(importedMetadataSchema)
				.addSchema(new ImportedMetadataSchema().setCode(CODE_SCHEMA_1).addMetadata(m3).setCode(null));
		settings.addCollectionSettings(new ImportedCollectionSettings()
				.setCode(zeCollection).addType(importedType));

		assertThatErrorsWhileImportingSettingsExtracting()
				.contains(tuple("SettingsImportServices_invalidSchemaCode"));

	}

	@Test
	public void whenImportingTypeWithTwoCustomSchemaWithSameCodeThenErrorRaised()
			throws Exception {

		Map<String, String> tabParams = getTabsMap();

		ImportedType importedType = new ImportedType().setCode(Folder.SCHEMA_TYPE).setLabel("Dossier")
				.setTabs(toListOfTabs(tabParams))
				.addSchema(new ImportedMetadataSchema().setCode(CODE_SCHEMA_1))
				.addSchema(new ImportedMetadataSchema().setCode(CODE_SCHEMA_1));
		settings.addCollectionSettings(new ImportedCollectionSettings()
				.setCode(zeCollection).addType(importedType));

		assertThatErrorsWhileImportingSettingsExtracting("value")
				.contains(tuple("SettingsImportServices_duplicateSchemaCode", "USRschema1"));

	}

	@Test
	public void whenImportingTypesValuesAreSet()
			throws Exception {

		Map<String, String> tabParams = new HashMap<>();
		tabParams.put("default", "Métadonnées");
		tabParams.put("zeTab", "Mon onglet");
		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Ze Type label");
		ImportedMetadataSchema defaultSchema = folderType.newDefaultSchema().setCode("default").setLabel("Ze schéma label");

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING");
		defaultSchema.addMetadata(m1);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING")
				.setInputMask("9999-9999");

		ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType("REFERENCE")
				.setReferencedType(Category.SCHEMA_TYPE);

		ImportedMetadataSchema customSchema1 = folderType.newSchema("custom1").setLabel("Ze custom schema label 1")
				.addMetadata(m2).addMetadata(m3);
		ImportedMetadataSchema customSchema2 = folderType.newSchema("custom2").setLabel("Ze custom schema label 2");
		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
		assertThat(schemaType.getLabel(French)).isEqualTo("Ze Type label");

		assertThat(schemaType.getDefaultSchema().getLabel(French)).isEqualTo("Ze schéma label");
		assertThat(schemaType.getCustomSchema("custom1").getLabel(French)).isEqualTo("Ze custom schema label 1");
		assertThat(schemaType.getCustomSchema("custom2").getLabel(French)).isEqualTo("Ze custom schema label 2");

		Metadata metadata1 = schemaType.getDefaultSchema().get("folder_default_m1");
		assertThat(metadata1).isNotNull();
		assertThat(metadata1.getLabel(French)).isEqualTo("m1");
		assertThat(metadata1.getType()).isEqualTo(STRING);
		assertThat(metadata1.getInputMask()).isNullOrEmpty();

		Metadata metadata1Custom = schemaType.getSchema("folder_custom1").get("folder_custom1_m1");
		assertThat(metadata1Custom).isNotNull();

		Metadata metadata2 = schemaType.getSchema("folder_custom1").get("folder_custom1_m2");
		assertThat(metadata2).isNotNull();
		assertThat(metadata2.getInputMask()).isEqualTo("9999-9999");

		Metadata metadata3 = schemaType.getSchema("folder_custom1").get("folder_custom1_m3");
		assertThat(metadata3.getType()).isEqualTo(MetadataValueType.REFERENCE);
		assertThat(metadata3.getAllowedReferences().getAllowedSchemaType()).isEqualTo(Category.SCHEMA_TYPE);

		folderType.setLabel("Ze new type label");
		defaultSchema.setLabel("Ze new schéma label");
		customSchema1.setLabel("Ze new custom schema label 1");
		customSchema2.setLabel("Ze new custom schema label 2");
		importSettings();
		schemaType = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("folder");
		assertThat(schemaType.getLabel(French)).isEqualTo("Ze new type label");
		assertThat(schemaType.getDefaultSchema().getLabel(French)).isEqualTo("Ze new schéma label");
		assertThat(schemaType.getCustomSchema("custom1").getLabel(French)).isEqualTo("Ze new custom schema label 1");
		assertThat(schemaType.getCustomSchema("custom2").getLabel(French)).isEqualTo("Ze new custom schema label 2");

	}

	@Test
	public void whenImportingManyTypesValuesAreSet()
			throws Exception {

		Map<String, String> tabParams = new HashMap<>();
		tabParams.put("default", "Métadonnées");
		tabParams.put("zeTab", "Mon onglet");
		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING");
		defaultSchema.addMetadata(m1);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING")
				.setInputMask("9999-9999");

		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);
		folderType.addSchema(customSchema);
		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		ImportedCollectionSettings anotherCollectionSettings =
				new ImportedCollectionSettings().setCode("anotherCollection");
		anotherCollectionSettings.addType(folderType);
		settings.addCollectionSettings(anotherCollectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata metadata1 = schemaType.getDefaultSchema().get("folder_default_m1");
		assertThat(metadata1).isNotNull();
		assertThat(metadata1.getLabel(French)).isEqualTo("m1");
		assertThat(metadata1.getType()).isEqualTo(STRING);
		assertThat(metadata1.getInputMask()).isNullOrEmpty();

		Metadata metadata1Custom = schemaType.getSchema("folder_custom").get("folder_custom_m1");
		assertThat(metadata1Custom).isNotNull();

		Metadata metadata2 = schemaType.getSchema("folder_custom").get("folder_custom_m2");
		assertThat(metadata2).isNotNull();
		assertThat(metadata2.getInputMask()).isEqualTo("9999-9999");

		schemaType = metadataSchemasManager
				.getSchemaTypes("anotherCollection").getSchemaType("folder");

		metadata1 = schemaType.getDefaultSchema().get("folder_default_m1");
		assertThat(metadata1).isNotNull();
		assertThat(metadata1.getLabel(French)).isEqualTo("m1");
		assertThat(metadata1.getType()).isEqualTo(STRING);
		assertThat(metadata1.getInputMask()).isNullOrEmpty();

		metadata1Custom = schemaType.getSchema("folder_custom").get("folder_custom_m1");
		assertThat(metadata1Custom).isNotNull();

		metadata2 = schemaType.getSchema("folder_custom").get("folder_custom_m2");
		assertThat(metadata2).isNotNull();
		assertThat(metadata2.getInputMask()).isEqualTo("9999-9999");

	}

	@Test
	public void givenNewMetadataWhenModifyingMetadataLabelThenUpdated()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType()
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setLabel("m1_label");
		folderType.getDefaultSchema().addMetadata(m1);
		collectionSettings.addType(folderType);

		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		MetadataSchema defaultSchema = schemaType.getDefaultSchema();
		assertThat(defaultSchema).isNotNull();

		Metadata metadata1 = defaultSchema.get("folder_default_m1");

		assertThat(metadata1).isNotNull();
		assertThat(metadata1.getLabel(French)).isEqualTo("m1_label");

		m1.setLabel("m1_label_updated");
		importSettings();

		schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		defaultSchema = schemaType.getDefaultSchema();
		metadata1 = defaultSchema.get("folder_default_m1");
		assertThat(metadata1.getLabel(French)).isEqualTo("m1_label_updated");

	}

	@Test
	public void givenNewMetadataWhenModifyingMetadataInputMaskThenUpdated()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType()
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setInputMask("9999-9999").setType("STRING");
		folderType.getDefaultSchema().addMetadata(m1);
		collectionSettings.addType(folderType);

		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		MetadataSchema defaultSchema = schemaType.getDefaultSchema();
		assertThat(defaultSchema).isNotNull();

		Metadata metadata1 = defaultSchema.get("folder_default_m1");

		assertThat(metadata1).isNotNull();
		assertThat(metadata1.getLabel(French)).isEqualTo("m1");

		m1.setInputMask("9999-11111-2222");
		importSettings();

		schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		defaultSchema = schemaType.getDefaultSchema();
		metadata1 = defaultSchema.get("folder_default_m1");
		assertThat(metadata1.getInputMask()).isEqualTo("9999-11111-2222");

	}

	@Test
	public void givenNewMetadatasDefaultValuesThenDefaultValuesOK()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType()
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING");
		folderType.getDefaultSchema().addMetadata(m1);
		collectionSettings.addType(folderType);

		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		MetadataSchema defaultSchema = schemaType.getDefaultSchema();
		assertThat(defaultSchema).isNotNull();

		Metadata metadata1 = defaultSchema.get("folder_default_m1");

		assertThat(metadata1).isNotNull();
		assertThat(metadata1.getLabel(French)).isEqualTo("m1");
		assertThat(metadata1.getType()).isEqualTo(STRING);
		assertThat(metadata1.getInputMask()).isNullOrEmpty();
		assertThat(metadata1.isDefaultRequirement()).isFalse();
		assertThat(metadata1.isDuplicable()).isFalse();
		assertThat(metadata1.isEnabled()).isTrue();
		assertThat(metadata1.isEncrypted()).isFalse();
		assertThat(metadata1.isEssential()).isFalse();
		assertThat(metadata1.isEssentialInSummary()).isFalse();
		assertThat(metadata1.isMultiLingual()).isFalse();
		assertThat(metadata1.isMultivalue()).isFalse();
		assertThat(metadata1.isSearchable()).isFalse();
		assertThat(metadata1.isSchemaAutocomplete()).isFalse();
		assertThat(metadata1.isSortable()).isFalse();
		assertThat(metadata1.isSystemReserved()).isFalse();
		assertThat(metadata1.isUndeletable()).isFalse();
		assertThat(metadata1.isUniqueValue()).isFalse();
		assertThat(metadata1.isUnmodifiable()).isFalse();
	}

	// VisibleInForm=true, visibleInDisplay=true, visibleInTables= false, visibleInSearchResult=false
	@Test
	public void whenImportNewMetadatasThenMarkedAsVisibleInFormAndDisplayButNotInTablesAndSearch()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder")
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default"));
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
		folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1").setType("STRING"));
		customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType("STRING"));

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getFormMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getSearchResultsMetadataCodes())
				.contains("folder_default_title")
				.doesNotContain("folder_default_m1");

		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title")
				.doesNotContain("folder_default_m1");

		SchemaDisplayConfig customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title")
				.doesNotContain("folder_default_m1", "folder_custom_m2");

		assertThat(customFolder.getTableMetadataCodes())
				.contains("folder_default_title")
				.doesNotContain("folder_default_m1", "folder_custom_m2");
	}

	// default: visibleInDisplay=true, VisibleInForm=true, visibleInSearchResult=false, visibleInTables=false
	// custom : visibleInDisplay=true, VisibleInForm=true, visibleInSearchResult=false, visibleInTables=false
	@Test
	public void whenImportingMetadataSchemaWithListOfFormDisplaySearchAndListMetadatasThenApplied()
			throws Exception {
		settings = new ImportedSettings();
		ImportedCollectionSettings collectionSettings = settings.newCollectionSettings(zeCollection);
		ImportedType folderType = collectionSettings.newType("folder");
		ImportedMetadataSchema defaultSchema = folderType.getDefaultSchema();
		ImportedMetadataSchema customSchema1 = folderType.newSchema("custom1");
		ImportedMetadataSchema customSchema2 = folderType.newSchema("custom2");

		defaultSchema.newMetadata("m1").setType(STRING);
		defaultSchema.newMetadata("m2").setType(STRING);
		defaultSchema.newMetadata("m3").setType(STRING);
		defaultSchema.newMetadata("m4").setType(STRING);
		defaultSchema.newMetadata("m5").setType(STRING);
		defaultSchema.newMetadata("m6").setType(STRING);
		defaultSchema.newMetadata("m7").setType(STRING);
		defaultSchema.newMetadata("m8").setType(STRING);

		defaultSchema.setFormMetadatas(asList(
				"administrativeUnitEntered", "categoryEntered", "copyStatusEntered", "m2", "m1", "type", "title", "container", "openingDate", "actualDepositDate", "actualDestructionDate", "actualTransferDate", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "uniformSubdivisionEntered"))
				.setDisplayMetadatas(asList("m3"))
				.setSearchMetadatas(asList("m4", "m5", "m1"))
				.setTableMetadatas(asList("m3", "m5"));

		customSchema1.setFormMetadatas(asList(
				"m1", "type", "title", "container", "m2", "administrativeUnitEntered", "categoryEntered", "copyStatusEntered", "openingDate", "actualDepositDate", "actualDestructionDate", "actualTransferDate", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "uniformSubdivisionEntered"))
				.setDisplayMetadatas(asList("m3", "m2"))
				.setSearchMetadatas(asList("m3", "m4"))
				.setTableMetadatas(asList("m4", "m5"));

		customSchema2.setFormMetadatas(asList(
				"type", "title", "container", "m2", "administrativeUnitEntered", "categoryEntered", "actualDepositDate", "copyStatusEntered", "m3", "openingDate", "actualDepositDate", "actualDestructionDate", "actualTransferDate", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "uniformSubdivisionEntered"))
				.setDisplayMetadatas(asList("folder_custom2_m4", "folder_default_m5"))
				.setSearchMetadatas(asList("m1", "m3", "m2"))
				.setTableMetadatas(asList("m1", "folder_custom2_m4"));

		importSettings();
		assertThat(localCodes(folderSchemaDisplay("default").getFormMetadataCodes())).isEqualTo(asList(
				"administrativeUnitEntered", "categoryEntered", "copyStatusEntered", "m2", "m1", "type", "title", "container",
				"openingDate", "actualDepositDate", "actualDestructionDate", "actualTransferDate", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "uniformSubdivisionEntered", "mainCopyRuleIdEntered"));
		assertThat(localCodes(folderSchemaDisplay("default").getDisplayMetadataCodes())).containsExactly("m3");
		assertThat(localCodes(folderSchemaDisplay("default").getSearchResultsMetadataCodes())).containsExactly("m4", "m5", "m1");
		assertThat(localCodes(folderSchemaDisplay("default").getTableMetadataCodes())).containsExactly("m3", "m5");

		assertThat(localCodes(folderSchemaDisplay("custom1").getFormMetadataCodes())).isEqualTo(asList(
				"m1", "type", "title", "container", "m2", "administrativeUnitEntered", "categoryEntered", "copyStatusEntered",
				"openingDate", "actualDepositDate", "actualDestructionDate", "actualTransferDate", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "uniformSubdivisionEntered", "mainCopyRuleIdEntered"));
		assertThat(localCodes(folderSchemaDisplay("custom1").getDisplayMetadataCodes())).containsExactly("m3", "m2");
		assertThat(localCodes(folderSchemaDisplay("custom1").getSearchResultsMetadataCodes())).containsExactly("m3", "m4");
		assertThat(localCodes(folderSchemaDisplay("custom1").getTableMetadataCodes())).containsExactly("m4", "m5");

		assertThat(localCodes(folderSchemaDisplay("custom2").getFormMetadataCodes())).isEqualTo(asList(
				"type", "title", "container", "m2", "administrativeUnitEntered", "categoryEntered", "copyStatusEntered", "m3",
				"openingDate", "actualDepositDate", "actualDestructionDate", "actualTransferDate", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "uniformSubdivisionEntered", "mainCopyRuleIdEntered"));
		assertThat(localCodes(folderSchemaDisplay("custom2").getDisplayMetadataCodes())).containsExactly("m4", "m5");
		assertThat(localCodes(folderSchemaDisplay("custom2").getSearchResultsMetadataCodes())).containsExactly("m1", "m3", "m2");
		assertThat(localCodes(folderSchemaDisplay("custom2").getTableMetadataCodes())).containsExactly("m1", "m4");

		settings = new ImportedSettings();
		collectionSettings = settings.newCollectionSettings(zeCollection);
		folderType = collectionSettings.newType("folder");
		defaultSchema = folderType.getDefaultSchema();
		customSchema1 = folderType.newSchema("custom1");
		customSchema2 = folderType.newSchema("custom2");

		defaultSchema.newMetadata("m1").setType(STRING);
		defaultSchema.newMetadata("m2").setType(STRING);
		defaultSchema.newMetadata("m3").setType(STRING);
		defaultSchema.newMetadata("m4").setType(STRING);
		defaultSchema.newMetadata("m5").setType(STRING);
		defaultSchema.newMetadata("m6").setType(STRING);
		defaultSchema.newMetadata("m7").setType(STRING);
		defaultSchema.newMetadata("m8").setType(STRING);

		defaultSchema.setFormMetadatas(asList("m1", "m2", "openingDate", "title", "actualDepositDate", "actualDestructionDate", "actualTransferDate",
				"administrativeUnitEntered", "categoryEntered", "container", "copyStatusEntered", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "type", "uniformSubdivisionEntered"))
				.setDisplayMetadatas(asList("m5"))
				.setSearchMetadatas(asList("m1", "m2", "m5"))
				.setTableMetadatas(asList("m3", "m5"));

		customSchema1.setFormMetadatas(asList("m2", "m1", "openingDate", "title", "actualDepositDate", "actualDestructionDate", "actualTransferDate",
				"administrativeUnitEntered", "categoryEntered", "container", "copyStatusEntered", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "type", "uniformSubdivisionEntered"))
				.setDisplayMetadatas(asList("m2", "m3"))
				.setSearchMetadatas(asList("m1"))
				.setTableMetadatas(new ArrayList<String>());

		customSchema2.setFormMetadatas(new ArrayList<String>())
				.setDisplayMetadatas(new ArrayList<String>())
				.setSearchMetadatas(new ArrayList<String>())
				.setTableMetadatas(new ArrayList<String>());

		importSettings();
		assertThat(localCodes(folderSchemaDisplay("default").getFormMetadataCodes())).isEqualTo(asList(
				"m1", "m2", "openingDate", "title", "actualDepositDate", "actualDestructionDate", "actualTransferDate",
				"administrativeUnitEntered", "categoryEntered", "container", "copyStatusEntered", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "type", "uniformSubdivisionEntered", "mainCopyRuleIdEntered"));
		assertThat(localCodes(folderSchemaDisplay("default").getDisplayMetadataCodes())).containsExactly("m5");
		assertThat(localCodes(folderSchemaDisplay("default").getSearchResultsMetadataCodes())).containsExactly("m1", "m2", "m5");
		assertThat(localCodes(folderSchemaDisplay("default").getTableMetadataCodes())).containsExactly("m3", "m5");

		assertThat(localCodes(folderSchemaDisplay("custom1").getFormMetadataCodes())).isEqualTo(asList(
				"m2", "m1", "openingDate", "title", "actualDepositDate", "actualDestructionDate", "actualTransferDate",
				"administrativeUnitEntered", "categoryEntered", "container", "copyStatusEntered", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "type", "uniformSubdivisionEntered", "mainCopyRuleIdEntered"));
		assertThat(localCodes(folderSchemaDisplay("custom1").getDisplayMetadataCodes())).containsExactly("m2", "m3");
		assertThat(localCodes(folderSchemaDisplay("custom1").getSearchResultsMetadataCodes())).containsExactly("m1");
		assertThat(localCodes(folderSchemaDisplay("custom1").getTableMetadataCodes())).containsExactly("m4", "m5");

		assertThat(localCodes(folderSchemaDisplay("custom2").getFormMetadataCodes())).isEqualTo(asList(
				"type", "title", "container", "m2", "administrativeUnitEntered", "categoryEntered", "copyStatusEntered", "m3",
				"openingDate", "actualDepositDate", "actualDestructionDate", "actualTransferDate", "enteredClosingDate",
				"mediumTypes", "parentFolder", "retentionRuleEntered", "uniformSubdivisionEntered", "mainCopyRuleIdEntered", "m1", "m4",
				"m5", "m6", "m7", "m8"));
		assertThat(localCodes(folderSchemaDisplay("custom2").getDisplayMetadataCodes()))
				.isEqualTo(asList("m4", "m5", "m1", "m2", "m3", "m6", "m7", "m8"));
		assertThat(localCodes(folderSchemaDisplay("custom2").getSearchResultsMetadataCodes())).containsExactly("m1", "m3", "m2");
		assertThat(localCodes(folderSchemaDisplay("custom2").getTableMetadataCodes())).containsExactly("m1", "m4");

	}

	private SchemaDisplayConfig folderSchemaDisplay(String localCode) {
		return schemasDisplayManager.getSchema(zeCollection, Folder.SCHEMA_TYPE + "_" + localCode);
	}

	// default: visibleInDisplay=true, VisibleInForm=true, visibleInSearchResult=false, visibleInTables=false
	// custom : visibleInDisplay=true, VisibleInForm=true, visibleInSearchResult=false, visibleInTables=false
	@Test
	public void whenImportNewMetadatasWithVisibleInSearchAndTablesThenMarkedAsVisibleInFormAndDisplayAndTablesAndSearch()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder")
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default"));
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
		folderType.getDefaultSchema().addMetadata(
				new ImportedMetadata().setCode("m1").setType("STRING").setVisibleInForm(true).setVisibleInTables(true)
						.setVisibleInSearchResult(true));
		customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType("STRING"));

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getFormMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getSearchResultsMetadataCodes())
				.contains("folder_default_title", "folder_default_m1");

		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title", "folder_default_m1");

		SchemaDisplayConfig customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1")
				.doesNotContain("folder_custom_m2");

	}

	// VisibleInForm=true, visibleInDisplay=true, visibleInTables= false, visiInSearchResult=false
	@Test
	public void whenImportNewMetadatasWithVisibleInSearchAndFormTablesThenMarkedAsVisibleInFormAndDisplayAndTablesAndSearch()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder")
				.setDefaultSchema(new ImportedMetadataSchema().setCode("default"));
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
		folderType.getDefaultSchema().addMetadata(
				new ImportedMetadata().setCode("m1").setType("STRING").setVisibleInForm(true).setVisibleInTables(true)
						.setVisibleInSearchResult(true));
		customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType("STRING"));

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getFormMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getSearchResultsMetadataCodes())
				.contains("folder_default_title", "folder_default_m1");

		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title", "folder_default_m1");

		SchemaDisplayConfig customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1")
				.doesNotContain("folder_custom_m2");

	}

	// default_m1: visibleInDisplay=true, visibleInSearch=true, visibleInForm=false, visibleInTables=false
	@Test
	public void whenImportNewMetadatasThenMarkedAsVisibleInDisplayAndSearchResultsButNotInFormAndTables()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(
				new ImportedMetadataSchema().setCode("default"));
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
		folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1")
				.setVisibleInForm(false).setVisibleInDisplay(true).setVisibleInSearchResult(true).setType("STRING"));

		customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType("STRING")
				.setVisibleInDisplay(false).setVisibleInSearchResult(false).setVisibleInForm(false));
		customSchema.addMetadata(new ImportedMetadata().setCode("m1").setType("STRING")
				.setVisibleInDisplay(false));

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_default");

		assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getSearchResultsMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title")
				.doesNotContain("folder_default_m1");

		assertThat(defaultFolder.getFormMetadataCodes())
				.contains("folder_default_title")
				.doesNotContain("folder_default_m1");

		SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title")
				.doesNotContain("folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title")
				.doesNotContain("folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title")
				.doesNotContain("folder_default_m1", "folder_custom_m2");

	}

	// default_m1: visibleInDisplay=false, visibleInSearch=true, visibleInForm=true, visibleInTables=false
	// custom_m2: visibleInDisplay=true, visibleInSearch=false, visibleInForm=false, visibleInTables=false
	@Test
	public void whenImportNewMetadatasThenMarkedAsVisibleInSearchAndFormButNotInDisplayAndTables()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(
				new ImportedMetadataSchema().setCode("default"));
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
		folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1")
				.setVisibleInDisplay(false).setVisibleInSearchResult(true).setVisibleInForm(true).setType("STRING"));

		customSchema.addMetadata(
				new ImportedMetadata().setCode("m2").setType("STRING").setVisibleInSearchResult(false).setVisibleInForm(false));

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_default");

		assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title").doesNotContain("folder_default_m1");
		assertThat(defaultFolder.getSearchResultsMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title")
				.doesNotContain("folder_default_m1");
		assertThat(defaultFolder.getFormMetadataCodes())
				.contains("folder_default_title", "folder_default_m1");

		SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m2")
				.doesNotContain("folder_custom_m1");

		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1")
				.doesNotContain("folder_custom_m2");

		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title")
				.doesNotContain("folder_default_m1", "folder_custom_m2");

	}

	// default_m1: visibleInDisplay=true, visibleInSearch=true, visibleInForm=true, visibleInTables=true
	// custom_m2: visibleInDisplay=true, visibleInSearch=false, visibleInForm=true, visibleInTables=false
	@Test
	public void whenImportNewMetadatasWithAllVisibleThenAllMarkedAsVisible()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(
				new ImportedMetadataSchema().setCode("default"));
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
		folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1")
				.setVisibleInSearchResult(true).setVisibleInTables(true).setType("STRING"));

		customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType("STRING"));

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_default");

		assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getSearchResultsMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getFormMetadataCodes())
				.contains("folder_default_title", "folder_default_m1");

		SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1")
				.doesNotContain("folder_default_m1", "folder_custom_m2");

	}

	// default_m1: visibleInDisplay=false, visibleInSearch=true, visibleInForm=false, visibleInTables=true
	// custom_m2: visibleInDisplay=true, visibleInSearch=false, visibleInForm=true, visibleInTables=false
	@Test
	public void whenImportNewMetadatasWithVisibleInSearchAndTablesThenFlagsAreSet()
			throws Exception {

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setDefaultSchema(
				new ImportedMetadataSchema().setCode("default"));
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom");
		folderType.getDefaultSchema().addMetadata(new ImportedMetadata().setCode("m1")
				.setVisibleInSearchResult(true).setVisibleInTables(true).setType("STRING"));

		customSchema.addMetadata(new ImportedMetadata().setCode("m2").setType("STRING"));

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_default");

		assertThat(defaultFolder.getDisplayMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getSearchResultsMetadataCodes()).contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title", "folder_default_m1");
		assertThat(defaultFolder.getFormMetadataCodes())
				.contains("folder_default_title", "folder_default_m1");

		SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m2");

		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1")
				.doesNotContain("folder_default_m1", "folder_custom_m2");

	}

	@Test
	public void givenNewMetadataWithVisibleInDisplayFlagDefinedThenOK()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setVisibleInDisplay(true);
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING").setVisibleInDisplay(false);
		ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType("STRING").setVisibleInDisplay(true);
		ImportedMetadata m4 = new ImportedMetadata().setCode("m4").setType("STRING").setVisibleInDisplay(false);
		ImportedMetadata m5 = new ImportedMetadata().setCode("m5").setType("STRING").setVisibleInDisplay(false);
		ImportedMetadata m6 = new ImportedMetadata().setCode("m6").setType("STRING").setVisibleInDisplay(true);
		ImportedMetadata m5custom = new ImportedMetadata().setCode("m5").setType("STRING").setVisibleInDisplay(true);
		ImportedMetadata m6custom = new ImportedMetadata().setCode("m6").setType("STRING").setVisibleInDisplay(false);

		folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2).addMetadata(m5).addMetadata(m6);
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema()
				.setCode("custom").addMetadata(m3).addMetadata(m4).addMetadata(m5custom).addMetadata(m6custom);

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getDisplayMetadataCodes())
				.contains("folder_default_title", "folder_default_m1", "folder_default_m6")
				.doesNotContain("folder_default_m2", "folder_default_m5");

		SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m3", "folder_custom_m5")
				.doesNotContain("folder_custom_m2", "folder_custom_m4", "folder_custom_m6");

		//Reverse flags and re-import
		m1.setVisibleInDisplay(false);
		m2.setVisibleInDisplay(true);
		m3.setVisibleInDisplay(false);
		m4.setVisibleInDisplay(true);
		m5.setVisibleInDisplay(true);
		m6.setVisibleInDisplay(false);
		m5custom.setVisibleInDisplay(false);
		m6custom.setVisibleInDisplay(true);
		importSettings();

		defaultFolder = schemasDisplayManager.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getDisplayMetadataCodes())
				.contains("folder_default_title", "folder_default_m2", "folder_default_m5")
				.doesNotContain("folder_default_m1", "folder_default_m6");

		customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getDisplayMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m2", "folder_custom_m4", "folder_custom_m6")
				.doesNotContain("folder_custom_m1", "folder_custom_m3", "folder_custom_m5");
	}

	@Test
	public void givenNewMetadataWithVisibleInFormFlagDefinedThenOK()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setVisibleInForm(true);
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING").setVisibleInForm(false);
		ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType("STRING").setVisibleInForm(true);
		ImportedMetadata m4 = new ImportedMetadata().setCode("m4").setType("STRING").setVisibleInForm(false);
		ImportedMetadata m5 = new ImportedMetadata().setCode("m5").setType("STRING").setVisibleInForm(false);
		ImportedMetadata m6 = new ImportedMetadata().setCode("m6").setType("STRING").setVisibleInForm(true);
		ImportedMetadata m5custom = new ImportedMetadata().setCode("m5").setType("STRING").setVisibleInForm(true);
		ImportedMetadata m6custom = new ImportedMetadata().setCode("m6").setType("STRING").setVisibleInForm(false);

		folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2).addMetadata(m5).addMetadata(m6);
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema()
				.setCode("custom").addMetadata(m3).addMetadata(m4).addMetadata(m5custom).addMetadata(m6custom);

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getFormMetadataCodes())
				.contains("folder_default_title", "folder_default_m1", "folder_default_m6")
				.doesNotContain("folder_default_m2", "folder_default_m5");

		SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m3", "folder_custom_m5")
				.doesNotContain("folder_custom_m2", "folder_custom_m4", "folder_custom_m6");

		//Reverse flags and reimport
		m1.setVisibleInForm(false);
		m2.setVisibleInForm(true);
		m3.setVisibleInForm(false);
		m4.setVisibleInForm(true);
		m5.setVisibleInForm(true);
		m6.setVisibleInForm(false);
		m5custom.setVisibleInForm(false);
		m6custom.setVisibleInForm(true);
		importSettings();

		defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getFormMetadataCodes())
				.contains("folder_default_title", "folder_default_m2", "folder_default_m5")
				.doesNotContain("folder_default_m1", "folder_default_m6");

		customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getFormMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m2", "folder_custom_m4", "folder_custom_m6")
				.doesNotContain("folder_custom_m1", "folder_custom_m3", "folder_custom_m5");
	}

	@Test
	public void givenNewMetadataWithVisibleInSearchFlagDefinedThenOK()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setVisibleInSearchResult(true);
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING").setVisibleInSearchResult(false);
		ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType("STRING").setVisibleInSearchResult(true);
		ImportedMetadata m4 = new ImportedMetadata().setCode("m4").setType("STRING").setVisibleInSearchResult(false);
		ImportedMetadata m5 = new ImportedMetadata().setCode("m5").setType("STRING").setVisibleInSearchResult(false);
		ImportedMetadata m6 = new ImportedMetadata().setCode("m6").setType("STRING").setVisibleInSearchResult(true);
		ImportedMetadata m5custom = new ImportedMetadata().setCode("m5").setType("STRING").setVisibleInSearchResult(true);
		ImportedMetadata m6custom = new ImportedMetadata().setCode("m6").setType("STRING").setVisibleInSearchResult(false);

		folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2).addMetadata(m5).addMetadata(m6);
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema()
				.setCode("custom").addMetadata(m3).addMetadata(m4).addMetadata(m5custom).addMetadata(m6custom);

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder).isNotNull();

		assertThat(defaultFolder.getSearchResultsMetadataCodes())
				.contains("folder_default_title", "folder_default_m1", "folder_default_m6")
				.doesNotContain("folder_default_m2", "folder_default_m5");

		SchemaDisplayConfig customFolder = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m1", "folder_custom_m3", "folder_custom_m5")
				.doesNotContain("folder_custom_m2", "folder_custom_m4", "folder_custom_m6");

		//Reverse flags and reimport
		m1.setVisibleInSearchResult(false);
		m2.setVisibleInSearchResult(true);
		m3.setVisibleInSearchResult(false);
		m4.setVisibleInSearchResult(true);
		m5.setVisibleInSearchResult(true);
		m6.setVisibleInSearchResult(false);
		m5custom.setVisibleInSearchResult(false);
		m6custom.setVisibleInSearchResult(true);
		importSettings();

		defaultFolder = schemasDisplayManager.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getSearchResultsMetadataCodes())
				.contains("folder_default_title", "folder_default_m2", "folder_default_m5")
				.doesNotContain("folder_default_m1", "folder_default_m6");

		customFolder = schemasDisplayManager.getSchema(zeCollection, "folder_custom");
		assertThat(customFolder.getSearchResultsMetadataCodes())
				.contains("folder_custom_title", "folder_custom_m2", "folder_custom_m4", "folder_custom_m6")
				.doesNotContain("folder_custom_m1", "folder_custom_m3", "folder_custom_m5");
	}

	@Test
	public void givenNewMetadataWithVisibleInTablesFlagDefinedThenOK()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setVisibleInTables(true);
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING").setVisibleInTables(false);
		ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType("STRING").setVisibleInTables(true);
		ImportedMetadata m4 = new ImportedMetadata().setCode("m4").setType("STRING").setVisibleInTables(false);
		ImportedMetadata m5 = new ImportedMetadata().setCode("m5").setType("STRING").setVisibleInTables(false);
		ImportedMetadata m6 = new ImportedMetadata().setCode("m6").setType("STRING").setVisibleInTables(true);
		ImportedMetadata m5custom = new ImportedMetadata().setCode("m5").setType("STRING").setVisibleInTables(true);
		ImportedMetadata m6custom = new ImportedMetadata().setCode("m6").setType("STRING").setVisibleInTables(false);

		folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2).addMetadata(m5).addMetadata(m6);
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema()
				.setCode("custom").addMetadata(m3).addMetadata(m4).addMetadata(m5custom).addMetadata(m6custom);

		collectionSettings.addType(folderType.addSchema(customSchema));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		SchemaDisplayConfig defaultFolder = getAppLayerFactory()
				.getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title", "folder_default_m1", "folder_default_m6")
				.doesNotContain("folder_default_m2", "folder_default_m5");

		SchemaDisplayConfig customFolder = getAppLayerFactory()
				.getMetadataSchemasDisplayManager().getSchema(zeCollection, "folder_custom");

		//Reverse flags and re-import
		m1.setVisibleInTables(false);
		m2.setVisibleInTables(true);
		m3.setVisibleInTables(false);
		m4.setVisibleInTables(true);
		m5.setVisibleInTables(true);
		m6.setVisibleInTables(false);
		m5custom.setVisibleInTables(false);
		m6custom.setVisibleInTables(true);
		importSettings();

		defaultFolder = schemasDisplayManager.getSchema(zeCollection, "folder_default");
		assertThat(defaultFolder.getTableMetadataCodes())
				.contains("folder_default_title", "folder_default_m2", "folder_default_m5")
				.doesNotContain("folder_default_m1", "folder_default_m6");

	}

	@Test
	public void givenNewMetadataWithVisibleInDisplayOfSpecificSchemasThenOnlyVisibleOnThoseSchemas()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setVisibleInDisplayIn(asList("default", "custom1"));
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING")
				.setVisibleInDisplayIn(asList("custom2", "custom3"));

		folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
		collectionSettings.addType(folderType
				.addSchema(new ImportedMetadataSchema().setCode("custom1"))
				.addSchema(new ImportedMetadataSchema().setCode("custom2"))
				.addSchema(new ImportedMetadataSchema().setCode("custom3")));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_default").getDisplayMetadataCodes();
		assertThat(metadataCodes).contains("folder_default_title", "folder_default_m1").doesNotContain("folder_default_m2");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom1").getDisplayMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom1_title", "folder_custom1_m1").doesNotContain("folder_custom1_m2");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom2").getDisplayMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom2_title", "folder_custom2_m2").doesNotContain("folder_custom2_m1");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom3").getDisplayMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom3_title", "folder_custom3_m2").doesNotContain("folder_custom3_m1");

		//Reverse flags and re-import
		m1.setVisibleInDisplayIn(asList("custom2", "custom3"));
		m2.setVisibleInDisplayIn(asList("default", "custom1"));
		importSettings();

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_default").getDisplayMetadataCodes();
		assertThat(metadataCodes).contains("folder_default_title", "folder_default_m2").doesNotContain("folder_default_m1");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom1").getDisplayMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom1_title", "folder_custom1_m2").doesNotContain("folder_custom1_m1");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom2").getDisplayMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom2_title", "folder_custom2_m1").doesNotContain("folder_custom2_m2");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom3").getDisplayMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom3_title", "folder_custom3_m1").doesNotContain("folder_custom3_m2");
	}

	@Test
	public void givenNewMetadataWithVisibleInFormOfSpecificSchemasThenOnlyVisibleOnThoseSchemas()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setVisibleInFormIn(asList("default", "custom1"));
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING")
				.setVisibleInFormIn(asList("custom2", "custom3"));

		folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
		collectionSettings.addType(folderType
				.addSchema(new ImportedMetadataSchema().setCode("custom1"))
				.addSchema(new ImportedMetadataSchema().setCode("custom2"))
				.addSchema(new ImportedMetadataSchema().setCode("custom3")));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_default").getFormMetadataCodes();
		assertThat(metadataCodes).contains("folder_default_title", "folder_default_m1").doesNotContain("folder_default_m2");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom1").getFormMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom1_title", "folder_custom1_m1").doesNotContain("folder_custom1_m2");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom2").getFormMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom2_title", "folder_custom2_m2").doesNotContain("folder_custom2_m1");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom3").getFormMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom3_title", "folder_custom3_m2").doesNotContain("folder_custom3_m1");

		//Reverse flags and re-import
		m1.setVisibleInFormIn(asList("custom2", "custom3"));
		m2.setVisibleInFormIn(asList("default", "custom1"));
		importSettings();

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_default").getFormMetadataCodes();
		assertThat(metadataCodes).contains("folder_default_title", "folder_default_m2").doesNotContain("folder_default_m1");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom1").getFormMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom1_title", "folder_custom1_m2").doesNotContain("folder_custom1_m1");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom2").getFormMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom2_title", "folder_custom2_m1").doesNotContain("folder_custom2_m2");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom3").getFormMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom3_title", "folder_custom3_m1").doesNotContain("folder_custom3_m2");
	}

	@Test
	public void givenNewMetadataWithVisibleInTablesOfSpecificSchemasThenOnlyVisibleOnThoseSchemas()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setVisibleInTablesIn(asList("default", "custom1"));
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING")
				.setVisibleInTablesIn(asList("custom2", "custom3"));

		folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
		collectionSettings.addType(folderType
				.addSchema(new ImportedMetadataSchema().setCode("custom1"))
				.addSchema(new ImportedMetadataSchema().setCode("custom2"))
				.addSchema(new ImportedMetadataSchema().setCode("custom3")));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_default").getTableMetadataCodes();
		assertThat(metadataCodes).contains("folder_default_title", "folder_default_m1").doesNotContain("folder_default_m2");

		//Reverse flags and re-import
		m1.setVisibleInTablesIn(asList("custom2", "custom3"));
		m2.setVisibleInTablesIn(asList("default", "custom1"));
		importSettings();

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_default").getTableMetadataCodes();
		assertThat(metadataCodes).contains("folder_default_title", "folder_default_m2").doesNotContain("folder_default_m1");

	}

	@Test
	public void givenNewMetadataWithVisibleInSearchResultsOfSpecificSchemasThenOnlyVisibleOnThoseSchemas()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setVisibleInResultIn(asList("default", "custom1"));
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING")
				.setVisibleInResultIn(asList("custom2", "custom3"));

		folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2);
		collectionSettings.addType(folderType
				.addSchema(new ImportedMetadataSchema().setCode("custom1"))
				.addSchema(new ImportedMetadataSchema().setCode("custom2"))
				.addSchema(new ImportedMetadataSchema().setCode("custom3")));
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_default").getSearchResultsMetadataCodes();
		assertThat(metadataCodes).contains("folder_default_title", "folder_default_m1").doesNotContain("folder_default_m2");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom1").getSearchResultsMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom1_title", "folder_custom1_m1").doesNotContain("folder_custom1_m2");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom2").getSearchResultsMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom2_title", "folder_custom2_m2").doesNotContain("folder_custom2_m1");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom3").getSearchResultsMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom3_title", "folder_custom3_m2").doesNotContain("folder_custom3_m1");

		//Reverse flags and re-import
		m1.setVisibleInResultIn(asList("custom2", "custom3"));
		m2.setVisibleInResultIn(asList("default", "custom1"));
		importSettings();

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_default").getSearchResultsMetadataCodes();
		assertThat(metadataCodes).contains("folder_default_title", "folder_default_m2").doesNotContain("folder_default_m1");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom1").getSearchResultsMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom1_title", "folder_custom1_m2").doesNotContain("folder_custom1_m1");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom2").getSearchResultsMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom2_title", "folder_custom2_m1").doesNotContain("folder_custom2_m2");

		metadataCodes = schemasDisplayManager.getSchema(zeCollection, "folder_custom3").getSearchResultsMetadataCodes();
		assertThat(metadataCodes).contains("folder_custom3_title", "folder_custom3_m1").doesNotContain("folder_custom3_m2");
	}

	@Test
	public void givenMetadataWithCustomLabelInCustomSchemasThenOK()
			throws Exception {
		runTwice = false;
		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata defaultM1 = folderType.getDefaultSchema().newMetadata("m1").setType("STRING").setLabel("M1 label");
		ImportedMetadata defaultM2 = folderType.getDefaultSchema().newMetadata("m2").setType("STRING").setLabel("M2 label");
		ImportedMetadata defaultM3 = folderType.getDefaultSchema().newMetadata("m3").setType("STRING").setLabel("M3 label");

		ImportedMetadataSchema customSchema1 = folderType.newSchema("custom1");
		ImportedMetadata custom1M1 = customSchema1.newMetadata("m1").setLabel("Custom M1 label");
		ImportedMetadata custom1M2 = customSchema1.newMetadata("m2");
		ImportedMetadataSchema customSchema2 = folderType.newSchema("custom2");
		ImportedMetadata custom2M2 = customSchema2.newMetadata("m2").setLabel("Custom M2 label");

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(zeCollection);
		assertThat(types.getMetadata("folder_default_m1").getLabel(French)).isEqualTo("M1 label");
		assertThat(types.getMetadata("folder_default_m2").getLabel(French)).isEqualTo("M2 label");
		assertThat(types.getMetadata("folder_default_m3").getLabel(French)).isEqualTo("M3 label");

		assertThat(types.getMetadata("folder_custom1_m1").getLabel(French)).isEqualTo("Custom M1 label");
		assertThat(types.getMetadata("folder_custom1_m2").getLabel(French)).isEqualTo("M2 label");
		assertThat(types.getMetadata("folder_custom1_m3").getLabel(French)).isEqualTo("M3 label");

		assertThat(types.getMetadata("folder_custom2_m1").getLabel(French)).isEqualTo("M1 label");
		assertThat(types.getMetadata("folder_custom2_m2").getLabel(French)).isEqualTo("Custom M2 label");
		assertThat(types.getMetadata("folder_custom2_m3").getLabel(French)).isEqualTo("M3 label");

		custom1M2.setLabel("New custom label 1");
		custom2M2.setLabel("New custom label 2");
		defaultM3.setLabel("New m3 label");

		importSettings();

		types = metadataSchemasManager.getSchemaTypes(zeCollection);
		assertThat(types.getMetadata("folder_default_m1").getLabel(French)).isEqualTo("M1 label");
		assertThat(types.getMetadata("folder_default_m2").getLabel(French)).isEqualTo("M2 label");
		assertThat(types.getMetadata("folder_default_m3").getLabel(French)).isEqualTo("New m3 label");

		assertThat(types.getMetadata("folder_custom1_m1").getLabel(French)).isEqualTo("Custom M1 label");
		assertThat(types.getMetadata("folder_custom1_m2").getLabel(French)).isEqualTo("New custom label 1");
		assertThat(types.getMetadata("folder_custom1_m3").getLabel(French)).isEqualTo("New m3 label");

		assertThat(types.getMetadata("folder_custom2_m1").getLabel(French)).isEqualTo("M1 label");
		assertThat(types.getMetadata("folder_custom2_m2").getLabel(French)).isEqualTo("New custom label 2");
		assertThat(types.getMetadata("folder_custom2_m3").getLabel(French)).isEqualTo("New m3 label");

	}

	@Test
	public void givenMetadataWithCustomRequirementStatusInCustomSchemasThenOK()
			throws Exception {
		runTwice = false;
		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata defaultM1 = folderType.getDefaultSchema().newMetadata("m1").setType("STRING").setRequired(true);
		ImportedMetadata defaultM2 = folderType.getDefaultSchema().newMetadata("m2").setType("STRING").setRequired(false);
		ImportedMetadata defaultM3 = folderType.getDefaultSchema().newMetadata("m3").setType("STRING").setRequired(true);

		ImportedMetadataSchema customSchema1 = folderType.newSchema("custom1");
		ImportedMetadata custom1M1 = customSchema1.newMetadata("m1").setRequired(false);
		ImportedMetadata custom1M2 = customSchema1.newMetadata("m2").setRequired(true);
		ImportedMetadataSchema customSchema2 = folderType.newSchema("custom2");
		ImportedMetadata custom2M1 = customSchema2.newMetadata("m1");

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(zeCollection);
		assertThat(types.getMetadata("folder_default_m1").isDefaultRequirement()).isTrue();
		assertThat(types.getMetadata("folder_default_m2").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_default_m3").isDefaultRequirement()).isTrue();

		assertThat(types.getMetadata("folder_custom1_m1").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_custom1_m2").isDefaultRequirement()).isTrue();
		assertThat(types.getMetadata("folder_custom1_m3").isDefaultRequirement()).isTrue();

		assertThat(types.getMetadata("folder_custom2_m1").isDefaultRequirement()).isTrue();
		assertThat(types.getMetadata("folder_custom2_m2").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_custom2_m3").isDefaultRequirement()).isTrue();

		defaultM1.setRequired(false);
		defaultM2.setRequired(true);
		defaultM3.setRequired(false);
		custom1M1.setRequired(true);
		custom1M2.setRequired(false);

		importSettings();
		types = metadataSchemasManager.getSchemaTypes(zeCollection);
		assertThat(types.getMetadata("folder_default_m1").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_default_m2").isDefaultRequirement()).isTrue();
		assertThat(types.getMetadata("folder_default_m3").isDefaultRequirement()).isFalse();

		assertThat(types.getMetadata("folder_custom1_m1").isDefaultRequirement()).isTrue();
		assertThat(types.getMetadata("folder_custom1_m2").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_custom1_m3").isDefaultRequirement()).isFalse();

		assertThat(types.getMetadata("folder_custom2_m1").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_custom2_m2").isDefaultRequirement()).isTrue();
		assertThat(types.getMetadata("folder_custom2_m3").isDefaultRequirement()).isFalse();

	}

	@Test
	public void givenMetadataWithCustomEnabledStatusInCustomSchemasThenOK()
			throws Exception {
		runTwice = false;
		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata defaultM1 = folderType.getDefaultSchema().newMetadata("m1").setType("STRING").setEnabled(true);
		ImportedMetadata defaultM2 = folderType.getDefaultSchema().newMetadata("m2").setType("STRING").setEnabled(false);
		ImportedMetadata defaultM3 = folderType.getDefaultSchema().newMetadata("m3").setType("STRING").setEnabled(true);

		ImportedMetadataSchema customSchema1 = folderType.newSchema("custom1");
		ImportedMetadata custom1M1 = customSchema1.newMetadata("m1").setEnabled(false);
		ImportedMetadata custom1M2 = customSchema1.newMetadata("m2").setEnabled(true);
		ImportedMetadataSchema customSchema2 = folderType.newSchema("custom2");
		ImportedMetadata custom2M1 = customSchema2.newMetadata("m1");

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(zeCollection);
		assertThat(types.getMetadata("folder_default_m1").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_default_m2").isEnabled()).isFalse();
		assertThat(types.getMetadata("folder_default_m3").isEnabled()).isTrue();

		assertThat(types.getMetadata("folder_custom1_m1").isEnabled()).isFalse();
		assertThat(types.getMetadata("folder_custom1_m2").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_custom1_m3").isEnabled()).isTrue();

		assertThat(types.getMetadata("folder_custom2_m1").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_custom2_m2").isEnabled()).isFalse();
		assertThat(types.getMetadata("folder_custom2_m3").isEnabled()).isTrue();

		defaultM1.setEnabled(false);
		defaultM2.setEnabled(true);
		defaultM3.setEnabled(false);
		custom1M1.setEnabled(true);
		custom1M2.setEnabled(false);

		importSettings();
		types = metadataSchemasManager.getSchemaTypes(zeCollection);
		assertThat(types.getMetadata("folder_default_m1").isEnabled()).isFalse();
		assertThat(types.getMetadata("folder_default_m2").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_default_m3").isEnabled()).isFalse();

		assertThat(types.getMetadata("folder_custom1_m1").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_custom1_m2").isEnabled()).isFalse();
		assertThat(types.getMetadata("folder_custom1_m3").isEnabled()).isFalse();

		assertThat(types.getMetadata("folder_custom2_m1").isEnabled()).isFalse();
		assertThat(types.getMetadata("folder_custom2_m2").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_custom2_m3").isEnabled()).isFalse();

	}

	@Test
	public void givenMetadataInTabsThenOk()
			throws Exception {

		MapEntry defaultTab = entry("default:defaultGroupLabel", asMap(French, "Métadonnées", English, "Metadata"));
		MapEntry classifiedIn = entry("classifiedInGroupLabel", asMap(French, "Classé dans", English, "Classified in"));

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);
		ImportedType folderType = new ImportedType().setDefaultSchema(
				new ImportedMetadataSchema().setCode("default")).setCode("folder");
		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setTab("Ze onglet");
		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING").setTab("Ze autre onglet");
		ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType("STRING");

		folderType.getDefaultSchema().addMetadata(m1).addMetadata(m2).addMetadata(m3);
		collectionSettings.addType(folderType.addSchema(new ImportedMetadataSchema().setCode("custom")));
		settings.addCollectionSettings(collectionSettings);

		importSettings();
		assertThat(schemasDisplayManager.getType(zeCollection, "folder").getMetadataGroup()).containsOnly(
				entry("Ze onglet", asMap(French, "Ze onglet", English, "Ze onglet")),
				entry("Ze autre onglet", asMap(French, "Ze autre onglet", English, "Ze autre onglet")),
				defaultTab,
				classifiedIn
		);
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_m1").getMetadataGroupCode())
				.isEqualTo("Ze onglet");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_m2").getMetadataGroupCode())
				.isEqualTo("Ze autre onglet");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_m3").getMetadataGroupCode())
				.isEqualTo("");

		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_custom_m1").getMetadataGroupCode())
				.isEqualTo("Ze onglet");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_custom_m2").getMetadataGroupCode())
				.isEqualTo("Ze autre onglet");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_custom_m3").getMetadataGroupCode())
				.isEqualTo("");

		m1.setTab("Ze nouveau onglet");
		m2.setTab("default");
		m3.setTab("test");
		importSettings();

		assertThat(schemasDisplayManager.getType(zeCollection, "folder").getMetadataGroup()).containsOnly(
				entry("Ze nouveau onglet", asMap(French, "Ze nouveau onglet", English, "Ze nouveau onglet")),
				entry("test", asMap(French, "test", English, "test")),
				entry("Ze onglet", asMap(French, "Ze onglet", English, "Ze onglet")),
				entry("Ze autre onglet", asMap(French, "Ze autre onglet", English, "Ze autre onglet")),
				defaultTab,
				classifiedIn
		);
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_m1").getMetadataGroupCode())
				.isEqualTo("Ze nouveau onglet");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_m2").getMetadataGroupCode())
				.isEqualTo("");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_m3").getMetadataGroupCode())
				.isEqualTo("test");

		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_custom_m1").getMetadataGroupCode())
				.isEqualTo("Ze nouveau onglet");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_custom_m2").getMetadataGroupCode())
				.isEqualTo("");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_custom_m3").getMetadataGroupCode())
				.isEqualTo("test");

		m1.setTab(null);
		m2.setTab(null);
		m3.setTab(null);
		importSettings();

		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_m1").getMetadataGroupCode())
				.isEqualTo("Ze nouveau onglet");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_m2").getMetadataGroupCode())
				.isEqualTo("");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_default_m3").getMetadataGroupCode())
				.isEqualTo("test");

		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_custom_m1").getMetadataGroupCode())
				.isEqualTo("Ze nouveau onglet");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_custom_m2").getMetadataGroupCode())
				.isEqualTo("");
		assertThat(schemasDisplayManager.getMetadata(zeCollection, "folder_custom_m3").getMetadataGroupCode())
				.isEqualTo("test");
		runTwice = false;
	}

	@Test
	public void givenNewMetadataWhenDefinesListOfEnabledInSchemaThenReferenceMetadataIsEnabled()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1")
				.setType("STRING")
				.setEnabledIn(toListOfString("default", "custom"))
				.setRequiredIn(toListOfString("custom"))
				.setVisibleInFormIn(toListOfString("default", "custom"));
		defaultSchema.addMetadata(m1);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
				.setType("STRING")
				.setInputMask("9999-9999");

		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);
		folderType.addSchema(customSchema);
		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");
		Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isEnabled()).isTrue();

	}

	@Test
	public void whenUpdatingDuplicableThenFlagIsSet()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setDuplicable(false);
		defaultSchema.addMetadata(m1);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
				.setType("STRING").setDuplicable(true);

		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);
		folderType.addSchema(customSchema);
		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");
		Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isDuplicable()).isFalse();

		Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isDuplicable()).isFalse();

		Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isDuplicable()).isTrue();

		defaultSchema.addMetadata(m1.setDuplicable(true));
		customSchema.addMetadata(m2.setDuplicable(false));
		importSettings();

		schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");
		folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isDuplicable()).isTrue();

		folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isDuplicable()).isTrue();

		folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isDuplicable()).isFalse();

	}

	@Test
	public void whenUpdatingEnabledThenFlagIsSet()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setEnabled(true);
		defaultSchema.addMetadata(m1);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
				.setType("STRING").setEnabled(true);

		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);

		folderType.addSchema(customSchema);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");
		Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isEnabled()).isTrue();

		Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isEnabled()).isTrue();

		Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isEnabled()).isTrue();

		m1.setEncrypted(true);
		m2.setEncrypted(false);
		importSettings();

		schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");
		folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isEncrypted()).isTrue();

		folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isEncrypted()).isTrue();

		folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isEncrypted()).isFalse();

	}

	@Test
	public void whenUpdatingEncryptedThenFlagIsSet()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setEncrypted(true);
		defaultSchema.addMetadata(m1);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
				.setType("STRING").setEncrypted(false);

		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);

		folderType.addSchema(customSchema);

		ImportedMetadata m3 = new ImportedMetadata().setCode("m3")
				.setType("STRING").setEncrypted(true);
		ImportedMetadataSchema customSchema1 = new ImportedMetadataSchema().setCode("custom1").addMetadata(m3);
		folderType.addSchema(customSchema1);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");
		Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isEncrypted()).isTrue();

		Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isEncrypted()).isTrue();

		Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isEncrypted()).isFalse();

		Metadata folder_custom1_m1 = schemaType.getMetadata("folder_custom1_m1");
		assertThat(folder_custom1_m1.isEncrypted()).isTrue();

		Metadata folder_custom1_m3 = schemaType.getMetadata("folder_custom1_m3");
		assertThat(folder_custom1_m3.isEncrypted()).isTrue();

		m1.setEncrypted(false);
		m2.setEncrypted(true);
		m3.setEncrypted(false);
		importSettings();

		schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");
		folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isEncrypted()).isFalse();

		folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isEncrypted()).isFalse();

		folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isEncrypted()).isTrue();

		folder_custom1_m1 = schemaType.getMetadata("folder_custom1_m1");
		assertThat(folder_custom1_m1.isEncrypted()).isFalse();

		folder_custom1_m3 = schemaType.getMetadata("folder_custom1_m3");
		assertThat(folder_custom1_m3.isEncrypted()).isFalse();

	}

	@Test
	public void whenUpdatingEssentialAndEssentialInSummaryThenFlagIsSet()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setEssential(true).setEssentialInSummary(false);
		defaultSchema.addMetadata(m1);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2")
				.setType("STRING").setEssential(false).setEssentialInSummary(true);

		ImportedMetadataSchema customSchema = new ImportedMetadataSchema()
				.setCode("custom").addMetadata(m2);

		folderType.addSchema(customSchema);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isEssential()).isTrue();
		assertThat(folder_default_m1.isEssentialInSummary()).isFalse();

		Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isEssential()).isTrue();
		assertThat(folder_custom_m1.isEssentialInSummary()).isFalse();

		Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isEssential()).isFalse();
		assertThat(folder_custom_m2.isEssentialInSummary()).isTrue();

		m1.setEssential(false).setEssentialInSummary(true);
		m2.setEssential(true).setEssentialInSummary(false);
		importSettings();

		schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isEssential()).isFalse();
		assertThat(folder_default_m1.isEssentialInSummary()).isTrue();

		folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isEssential()).isFalse();
		assertThat(folder_custom_m1.isEssentialInSummary()).isTrue();

		folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isEssential()).isTrue();
		assertThat(folder_custom_m2.isEssentialInSummary()).isFalse();

	}

	@Test
	public void whenUpdatingMultivalueThenFlagIsSet()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setMultiValue(true);
		defaultSchema.addMetadata(m1);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING").setMultiValue(false);
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);

		folderType.addSchema(customSchema);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory()
				.getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isMultivalue()).isTrue();

		Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isMultivalue()).isTrue();

		Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isMultivalue()).isFalse();

		// inverser et re-importer
		m1.setMultiValue(false);
		m2.setMultiValue(true);
		importSettings();

		schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isMultivalue()).isFalse();

		folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isMultivalue()).isFalse();

		folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isMultivalue()).isTrue();

	}

	@Test

	public void whenUpdatingRecordAutoCompleteThenFlagIsSet()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setRecordAutoComplete(true);

		defaultSchema.addMetadata(m1);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING").setRecordAutoComplete(false);
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);

		folderType.addSchema(customSchema);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory()
				.getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isSchemaAutocomplete()).isTrue();

		Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isSchemaAutocomplete()).isTrue();

		Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isSchemaAutocomplete()).isFalse();

		//newWebDriver();
		//waitUntilICloseTheBrowsers();

		// inverser et re-importer
		m1.setRecordAutoComplete(false);
		m2.setRecordAutoComplete(true);
		importSettings();

		schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isSchemaAutocomplete()).isFalse();

		folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isSchemaAutocomplete()).isFalse();

		folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isSchemaAutocomplete()).isTrue();

		//newWebDriver();
		//waitUntilICloseTheBrowsers();

	}

	@Test

	public void whenUpdatingSearchableAndSortableThenOK()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING")
				.setRecordAutoComplete(true).setSearchable(true).setSortable(true);

		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default")
				.addMetadata(m1);

		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING")
				.setRecordAutoComplete(false).setSearchable(false).setSortable(false);
		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2);

		folderType.addSchema(customSchema);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory()
				.getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isSearchable()).isTrue();
		assertThat(folder_default_m1.isSortable()).isTrue();

		Metadata folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isSearchable()).isTrue();
		assertThat(folder_custom_m1.isSortable()).isTrue();

		Metadata folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isSearchable()).isFalse();
		assertThat(folder_custom_m2.isSortable()).isFalse();

		//newWebDriver();
		//waitUntilICloseTheBrowsers();

		// inverser et re-importer
		m1.setRecordAutoComplete(false).setSearchable(false).setSortable(false);
		m2.setRecordAutoComplete(true).setSearchable(true).setSortable(true);
		importSettings();

		schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isSearchable()).isFalse();
		assertThat(folder_default_m1.isSortable()).isFalse();

		folder_custom_m1 = schemaType.getMetadata("folder_custom_m1");
		assertThat(folder_custom_m1.isSearchable()).isFalse();
		assertThat(folder_custom_m1.isSortable()).isFalse();

		folder_custom_m2 = schemaType.getMetadata("folder_custom_m2");
		assertThat(folder_custom_m2.isSearchable()).isTrue();
		assertThat(folder_custom_m2.isSortable()).isTrue();

		//newWebDriver();
		//waitUntilICloseTheBrowsers();

	}

	@Test

	public void whenUpdatingRelationshipProvidingSecurityThenOK()
			throws Exception {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("REFERENCE")
				.setReferencedType(Category.SCHEMA_TYPE).setRelationshipProvidingSecurity(true);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("REFERENCE")
				.setReferencedType(Category.SCHEMA_TYPE).setRelationshipProvidingSecurity(false);

		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default").addMetadata(m1).addMetadata(m2);

		folderType.setDefaultSchema(defaultSchema);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = getAppLayerFactory().getModelLayerFactory()
				.getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isRelationshipProvidingSecurity()).isTrue();

		Metadata folder_default_m2 = schemaType.getMetadata("folder_default_m2");
		assertThat(folder_default_m2.isRelationshipProvidingSecurity()).isFalse();

		// inverser et re-importer
		m1.setRelationshipProvidingSecurity(false);
		m2.setRelationshipProvidingSecurity(true);
		importSettings();

		schemaType = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		folder_default_m1 = schemaType.getMetadata("folder_default_m1");
		assertThat(folder_default_m1.isRelationshipProvidingSecurity()).isFalse();

		folder_default_m2 = schemaType.getMetadata("folder_default_m2");
		assertThat(folder_default_m2.isRelationshipProvidingSecurity()).isTrue();

	}

	@Test
	public void testWriteAndReadImportSettings()
			throws IOException {

		Map<String, String> tabParams = new HashMap<>();
		tabParams.put("default", "Métadonnées");
		tabParams.put("zeTab", "Mon onglet");

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("false"));
		settings.addConfig(new ImportedConfig().setKey("documentRetentionRules").setValue("true"));
		settings.addConfig(new ImportedConfig().setKey("enforceCategoryAndRuleRelationshipInFolder").setValue("false"));
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("false"));
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule").setValue("2015"));
		settings.addConfig(new ImportedConfig().setKey("closeDateRequiredDaysBeforeYearEnd").setValue("15"));
		settings.addConfig(new ImportedConfig().setKey("yearEndDate").setValue("02/28"));
		settings.addConfig(new ImportedConfig().setKey("decommissioningDateBasedOn").setValue("OPEN_DATE"));

		List<ImportedSequence> sequences = new ArrayList<>();
		sequences.add(new ImportedSequence().setKey("1").setValue("1"));
		sequences.add(new ImportedSequence().setKey("1").setValue("2"));
		sequences.add(new ImportedSequence().setKey("1").setValue("3"));

		sequences.add(new ImportedSequence().setKey("2").setValue("1"));
		sequences.add(new ImportedSequence().setKey("2").setValue("2"));
		sequences.add(new ImportedSequence().setKey("2").setValue("3"));
		sequences.add(new ImportedSequence().setKey("2").setValue("4"));
		sequences.add(new ImportedSequence().setKey("2").setValue("5"));

		settings.setImportedSequences(sequences);

		ImportedCollectionSettings collectionSettings =
				new ImportedCollectionSettings().setCode(zeCollection);

		Map<Language, String> titleMap = new HashMap<>();
		titleMap.put(Language.French, TITLE_FR);

		ImportedValueList v1 = new ImportedValueList().setCode(CODE_1_VALUE_LIST)
				.setTitle(titleMap)
				.setClassifiedTypes(toListOfString(DOCUMENT, FOLDER))
				.setCodeMode("DISABLED");
		collectionSettings.addValueList(v1);

		Map<Language, String> titleMap2 = new HashMap<>();
		titleMap2.put(Language.French, "Le titre du domaine de valeurs 2");

		ImportedValueList v2 = new ImportedValueList().setCode(CODE_2_VALUE_LIST)
				.setTitle(titleMap2)
				.setClassifiedTypes(toListOfString(DOCUMENT))
				.setCodeMode("FACULTATIVE");
		collectionSettings.addValueList(v2);

		Map<Language, String> titleMap3 = new HashMap<>();
		titleMap3.put(Language.French, "Le titre du domaine de valeurs 3");

		ImportedValueList v3 = new ImportedValueList().setCode(CODE_3_VALUE_LIST)
				.setTitle(titleMap3)
				.setCodeMode("REQUIRED_AND_UNIQUE").setHierarchical(true);
		collectionSettings.addValueList(v3);

		Map<Language, String> titleMap4 = new HashMap<>();
		titleMap4.put(Language.French, "Le titre du domaine de valeurs 4");

		ImportedValueList v4 = new ImportedValueList().setCode(CODE_4_VALUE_LIST)
				.setTitle(titleMap4)
				.setHierarchical(false);
		collectionSettings.addValueList(v4);

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, TAXO_1_TITLE_FR);

		ImportedTaxonomy importedTaxonomy1 = new ImportedTaxonomy().setCode(TAXO_1_CODE)
				.setTitle(labelTitle1)
				.setClassifiedTypes(toListOfString("document", "folder"))
				.setVisibleOnHomePage(false)
				.setUserIds(asList(gandalf, bobGratton))
				.setGroupIds(asList("group1"));
		collectionSettings.addTaxonomy(importedTaxonomy1);

		Map<Language, String> labelTitle2 = new HashMap<>();
		labelTitle2.put(Language.French, TAXO_2_TITLE_FR);

		ImportedTaxonomy importedTaxonomy2 = new ImportedTaxonomy().setCode(TAXO_2_CODE)
				.setTitle(labelTitle2);
		collectionSettings.addTaxonomy(importedTaxonomy2);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setEnabledIn(asList("custom1", "custom2"))
				.setEncrypted(false).setEssential(true).setEssentialInSummary(false)
				.setMultiLingual(true).setMultiValue(false).setRecordAutoComplete(false)
				.setRequired(true).setRequiredIn(asList("custom1")).setSearchable(false)
				.setSortable(false).setUnique(true)
				.setUnmodifiable(true).setVisibleInDisplay(true).setVisibleInForm(true)
				.setVisibleInSearchResult(false).setVisibleInTables(false);
		defaultSchema.addMetadata(m1);

		ImportedMetadata m2 = new ImportedMetadata().setCode("m2").setType("STRING")
				.setInputMask("9999-9999").setAdvanceSearchable(true).setDuplicable(true).setEnabled(true)
				.setEnabledIn(asList("custom1", "custom2")).setEncrypted(false)
				.setEssential(true).setEssentialInSummary(false)
				.setMultiLingual(true).setMultiValue(false).setRecordAutoComplete(true)
				.setRequired(false).setRequiredIn(asList("custom2")).setSearchable(true)
				.setSortable(true).setUnique(true)
				.setUnmodifiable(true).setVisibleInDisplay(true).setVisibleInForm(true)
				.setVisibleInSearchResult(true).setVisibleInTables(false);

		ImportedMetadata m3 = new ImportedMetadata().setCode("m3").setType("STRING")
				.setInputMask("111-222").setAdvanceSearchable(true).setDuplicable(true).setEnabled(true)
				.setEncrypted(false).setEssential(true).setEssentialInSummary(false)
				.setMultiLingual(true).setMultiValue(false).setRecordAutoComplete(true)
				.setRequired(false).setSearchable(true).setSortable(true).setUnique(true)
				.setUnmodifiable(true).setVisibleInDisplay(true).setVisibleInForm(true)
				.setVisibleInSearchResult(true).setVisibleInTables(false);

		ImportedMetadataSchema customSchema = new ImportedMetadataSchema().setCode("custom").addMetadata(m2).addMetadata(m3);
		folderType.addSchema(customSchema);
		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		ImportedCollectionSettings anotherCollectionSettings =
				new ImportedCollectionSettings().setCode("anotherCollection");
		anotherCollectionSettings.addType(folderType);
		settings.addCollectionSettings(anotherCollectionSettings);

		// write settings settings to file ==> file1
		Document outDocument = new SettingsXMLFileWriter().writeSettings(settings);

		// read file1 to setting1
		ImportedSettings settingsRead1 = new SettingsXMLFileReader(outDocument, zeCollection, getModelLayerFactory()).read();
		//		assertThat(settingsRead1.toString()).isEqualTo(settings.toString());
		assertThat(settingsRead1).isEqualToComparingFieldByField(settings);

		// write settings1 to file ==> file2
		Document outDocument1 = new SettingsXMLFileWriter().writeSettings(settingsRead1);

		// read file2 to setting2
		ImportedSettings settingsRead2 = new SettingsXMLFileReader(outDocument1, zeCollection, getModelLayerFactory()).read();
		assertThat(settingsRead2).isEqualToComparingFieldByField(settingsRead1);

	}

	@Test
	public void whenImportingMetadataPopulateConfigsThenOK()
			throws ValidationException {
		runTwice = false;
		ImportedCollectionSettings collectionSettings = settings.newCollectionSettings(zeCollection);

		ImportedMetadataSchema folderDefaultSchema = collectionSettings.newType("folder").newDefaultSchema();

		folderDefaultSchema.newMetadata("USRtheme").setLabel("Thème").setType(STRING).newPopulateConfigs()
				.addRegexConfigs(new ImportedRegexConfigs(DESCRIPTION, "^Gandalf*", "LOTR", "SUBSTITUTION"))
				.addRegexConfigs(new ImportedRegexConfigs(DESCRIPTION, "^Dakota*", "Légendes d'indiens", "SUBSTITUTION"));

		folderDefaultSchema.newMetadata("USRisbn").setLabel("ISBN").setType(STRING).newPopulateConfigs().addRegexConfigs(
				new ImportedRegexConfigs(DESCRIPTION, "[0-9]{3}-[0-9]-[0-9]{4}-[0-9]{4}-[0-9]", "$0", "TRANSFORMATION"));

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		assertThat(schemaType.getDefaultSchema().get("folder_default_USRtheme").getPopulateConfigs().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value", "regexConfigType").containsOnly(
				tuple("description", "^Dakota*", "Légendes d'indiens", SUBSTITUTION),
				tuple("description", "^Gandalf*", "LOTR", SUBSTITUTION));

		assertThat(schemaType.getDefaultSchema().get("folder_default_USRisbn").getPopulateConfigs().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value", "regexConfigType").containsOnly(
				tuple("description", "[0-9]{3}-[0-9]-[0-9]{4}-[0-9]{4}-[0-9]", "$0", TRANSFORMATION));

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

	private void importSettings()
			throws com.constellio.model.frameworks.validation.ValidationException {
		try {
			// write settings1 to file ==> file2
			Document writtenSettings = new SettingsXMLFileWriter().writeSettings(settings);
			ImportedSettings settings2 = new SettingsXMLFileReader(writtenSettings, zeCollection, getModelLayerFactory()).read();
			assertThat(trimLines(settings2.toString())).isEqualTo(trimLines(settings.toString()));
			assertThat(settings2).isEqualToComparingFieldByField(settings);

			services.importSettings(settings);
		} catch (ValidationException e) {
			runTwice = false;
			throw e;

		} catch (RuntimeException e) {
			runTwice = false;
			throw e;
		}
	}

	private String trimLines(String lines) {
		StringBuilder stringBuilder = new StringBuilder();

		for (String line : lines.split("\n")) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("\n");
			}
			stringBuilder.append(line.trim());
		}

		return stringBuilder.toString();
	}

	private ListAssert<String> assertThatErrorsContainsLocalizedMessagesWhileImportingSettings(String... params) {
		try {
			services.importSettings(settings);
			runTwice = false;
			fail("ValidationException expected");
			return assertThat(new ArrayList<String>());
		} catch (ValidationException e) {
			ValidationErrors errors = e.getValidationErrors();

			return assertThat(i18n.asListOfMessages(errors, params));

			//		return assertThat(i18n.asListOfMessages(errors));
		}
	}

	private ListAssert<Tuple> assertThatErrorsWhileImportingSettingsExtracting(String... parameters)
			throws com.constellio.model.frameworks.validation.ValidationException {

		try {
			services.importSettings(settings);
			runTwice = false;
			fail("ValidationException expected");
			return assertThat(new ArrayList<Tuple>());
		} catch (ValidationException e) {

			return assertThat(extractingSimpleCodeAndParameters(e, parameters));
		}
	}

	@Test
	public void whenImportingMetadataWithReadRoleAccessThenOK()
			throws ValidationException {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setRequiredReadRoles(asList("RGI", "U"));

		defaultSchema.addMetadata(m1);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata metadata1 = schemaType.getDefaultSchema().get("folder_default_m1");


		assertThat(metadata1).isNotNull();

		assertThat(metadata1.getAccessRestrictions().getRequiredReadRoles()).contains("RGI", "U");
	}

	@Test
	public void whenImportingMetadataInCustomSchemaWithReadRoleAccessThenOK()
			throws ValidationException {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema usrFolder2Schema = new ImportedMetadataSchema().setCode("usrFolder2");
		folderType.setCustomSchemata(asList(usrFolder2Schema));

		ImportedMetadata m1 = new ImportedMetadata().setCode("m1").setType("STRING").setRequiredReadRoles(asList("RGI", "U"));

		usrFolder2Schema.addMetadata(m1);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata metadata1 = schemaType.getCustomSchema("usrFolder2").get("folder_usrFolder2_m1");


		assertThat(metadata1).isNotNull();

		assertThat(metadata1.getAccessRestrictions().getRequiredReadRoles()).contains("RGI", "U");
	}

	@Test
	public void whenImportingFolderTitleMetadataWithReadRoleAccessThenOK()
			throws ValidationException {

		ImportedCollectionSettings collectionSettings = new ImportedCollectionSettings().setCode(zeCollection);

		ImportedType folderType = new ImportedType().setCode("folder").setLabel("Dossier");
		ImportedMetadataSchema defaultSchema = new ImportedMetadataSchema().setCode("default");
		folderType.setDefaultSchema(defaultSchema);

		ImportedMetadata title = new ImportedMetadata().setCode("title").setType("STRING").setRequiredReadRoles(asList("M"));

		defaultSchema.addMetadata(title);

		collectionSettings.addType(folderType);
		settings.addCollectionSettings(collectionSettings);

		importSettings();

		MetadataSchemaType schemaType = metadataSchemasManager
				.getSchemaTypes(zeCollection).getSchemaType("folder");

		Metadata folderDefaultTitleMetadata = schemaType.getDefaultSchema().get("folder_default_title");

		assertThat(folderDefaultTitleMetadata).isNotNull();

		assertThat(folderDefaultTitleMetadata.getAccessRestrictions().getRequiredReadRoles()).contains("M");
	}


	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users), withCollection("anotherCollection"));
		//		givenCollection("anotherCollection");
		services = new SettingsImportServices(getAppLayerFactory());
		systemConfigurationsManager = getModelLayerFactory().getSystemConfigurationsManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		labelTemplateManager = getAppLayerFactory().getLabelTemplateManager();
		runTwice = true;
	}

	@After
	public void tearDown()
			throws Exception {

		if (runTwice) {
			runTwice = false;
			try {
				SettingsImportServicesAcceptanceTest.class.getMethod(skipTestRule.getCurrentTestName()).invoke(this);
			} catch (Exception e) {
				throw new AssertionError("An exception occured when running the test a second time", e);
			}
		}
	}
}