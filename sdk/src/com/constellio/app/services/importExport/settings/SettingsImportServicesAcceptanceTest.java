package com.constellio.app.services.importExport.settings;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.app.services.importExport.settings.model.*;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

public class SettingsImportServicesAcceptanceTest extends ConstellioTest {

	public static final String FOLDER = "folder";
	public static final String DOCUMENT = "document";
	public static final String TITLE_FR = "Le titre du domaine de valeurs 1";
	public static final String TITLE_EN = "First value list's title";
	SettingsImportServices services;
	ImportedSettings settings = new ImportedSettings();
	ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings();
	ImportedCollectionSettings anotherCollectionSettings = new ImportedCollectionSettings();
	SystemConfigurationsManager systemConfigurationsManager;
	MetadataSchemasManager metadataSchemasManager;
	boolean runTwice;


	@Test
	public void whenImportingUnknownConfigsThenConfigsAreNotSet() throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateUnknown").setValue("true"));

		assertThatErrorsWhileImportingSettingsExtracting("config").contains(
				tuple("SettingsImportServices_configurationNotFound", "calculatedCloseDateUnknown"));
	}

	@Test
	public void whenImportBadBooleanConfigValueThenValidationExceptionThrown() throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("notABoolean"));

		assertThatErrorsWhileImportingSettingsExtracting("config", "value").containsOnly(
				tuple("SettingsImportServices_invalidConfigurationValue", "calculatedCloseDate", "notABoolean"));
	}

	@Test
	public void whenImportingBadIntegerConfigValueThenValidationExceptionThrown() throws Exception {
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule")
				.setValue("helloInteger"));

		assertThatErrorsWhileImportingSettingsExtracting("config", "value").containsOnly(
				tuple("SettingsImportServices_invalidConfigurationValue",
						"calculatedCloseDateNumberOfYearWhenFixedRule" ,"helloInteger"));
	}

	@Test
	public void whenImportingNullValueConfigsThenNullValueExceptionIsRaised() throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue(null));

		assertThatErrorsWhileImportingSettingsExtracting("calculatedCloseDate").contains(
				tuple("SettingsImportServices_invalidConfigurationValue", null));
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

		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.DOCUMENT_RETENTION_RULES)).isEqualTo(true);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER)).isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);

		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE)).isEqualTo(2015);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR)).isEqualTo(15);

		assertThat(systemConfigurationsManager.getValue(RMConfigs.YEAR_END_DATE)).isEqualTo("02/28");

		assertThat(systemConfigurationsManager.getValue(RMConfigs.DECOMMISSIONING_DATE_BASED_ON))
				.isEqualTo(DecommissioningDateBasedOn.OPEN_DATE);
	}

	@Test
	public void whenImportingCollectionConfigSettingsIfCollectionCodeIsEmptyThenExceptionIsRaised() throws Exception {

		// TODO Valider la raison du fail
		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(null)
				.addValueList(new ImportedValueList().setCode("ddvUSRcodeDuDomaineDeValeur1")
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 1","First value list's title"))
						.setClassifiedTypes(toClassifiedTypesList(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)
				));

		assertThatErrorsWhileImportingSettingsExtracting().contains(tuple("SettingsImportServices_invalidCollectionCode"));

	}

	@Test
	public void whenImportingCollectionConfigSettingsIfCollectionCodeDoesNotExistThenExceptionIsRaised() throws Exception {

		// TODO Valider la raison du fail
		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode("unknonCollection")
				.addValueList(new ImportedValueList().setCode("ddvUSRcodeDuDomaineDeValeur1")
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 1","First value list's title"))
						.setClassifiedTypes(toClassifiedTypesList(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)
				));

		assertThatErrorsWhileImportingSettingsExtracting().contains(tuple("SettingsImportServices_collectionCodeNotFound"));

	}

	@Test
	public void whenImportingCollectionValueListIfCodeIsInvalidThenExceptionIsRaised() throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addValueList(new ImportedValueList().setCode(null)
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 1","First value list's title"))
						.setClassifiedTypes(toClassifiedTypesList(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)
				));

		assertThatErrorsWhileImportingSettingsExtracting().contains(tuple("SettingsImportServices_InvalidValueListCode"));

	}

	@Test
	public void whenImportingCollectionValueListIfCodeDoesNotStartWithDDVPrefixThenExceptionIsRaised() throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addValueList(new ImportedValueList().setCode("USRcodeDuDomaineDeValeur1")
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 1","First value list's title"))
						.setClassifiedTypes(toClassifiedTypesList(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
						.setHierarchical(false)
				));

		assertThatErrorsWhileImportingSettingsExtracting().contains(tuple("SettingsImportServices_InvalidValueListCode"));

	}

	@Test
	public void whenImportingCollectionConfigsSettingsThenSetted()
			throws Exception {

		String codeA = "ddvUSRcodeDuDomaineDeValeurA";
		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addValueList(new ImportedValueList().setCode(codeA)
						.setTitles(toTitlesMap(TITLE_FR, TITLE_EN))
						.setClassifiedTypes(toClassifiedTypesList(DOCUMENT, FOLDER)).setCodeMode("DISABLED")
		));

		String codeB = "ddvUSRcodeDuDomaineDeValeurB";
		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addValueList(new ImportedValueList().setCode(codeB)
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 2", "Second value list's title"))
						.setClassifiedTypes(toClassifiedTypesList(DOCUMENT))
						.setCodeMode("REQUIRED_AND_UNIQUE")//.setCodeMode("FACULTATIVE")
				));

		String codeC = "ddvUSRcodeDuDomaineDeValeurC" ;
		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addValueList(new ImportedValueList().setCode(codeC)
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 3", "Third value list's title"))
						.setCodeMode("REQUIRED_AND_UNIQUE")
						.setHierarchical(true)
				));

		String codeD = "ddvUSRcodeDuDomaineDeValeurD";
		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addValueList(new ImportedValueList().setCode(codeD)
						.setTitles(toTitlesMap("Le titre du domaine de valeurs 4", "Fourth value list's title"))
						.setHierarchical(false)
				));

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(codeA);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo(TITLE_FR);
		Metadata codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata("code");
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isFalse();
		assertThat(codeMetadata.isUniqueValue()).isFalse();
		assertThat(codeMetadata.isEnabled()).isFalse();
		assertThat(codeMetadata.isChildOfRelationship()).isFalse();

		metadataSchemaType = schemaTypes.getSchemaType(codeB);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo("Le titre du domaine de valeurs 2");
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata("code");
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isTrue();
		assertThat(codeMetadata.isUniqueValue()).isTrue();
		assertThat(codeMetadata.isEnabled()).isTrue();

		metadataSchemaType = schemaTypes.getSchemaType(codeC);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo("Le titre du domaine de valeurs 3");
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata("code");
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isTrue();
		assertThat(codeMetadata.isUniqueValue()).isTrue();
		assertThat(codeMetadata.isEnabled()).isTrue();

		// TODO Valider la propriété hierarchical !!!

		metadataSchemaType = schemaTypes.getSchemaType(codeD);
		assertThat(metadataSchemaType).isNotNull();
		assertThat(metadataSchemaType.getLabels().get(Language.French)).isEqualTo("Le titre du domaine de valeurs 4");
		codeMetadata = metadataSchemaType.getDefaultSchema().getMetadata("code");
		assertThat(codeMetadata).isNotNull();
		assertThat(codeMetadata.isDefaultRequirement()).isTrue();
		assertThat(codeMetadata.isUniqueValue()).isTrue();
		assertThat(codeMetadata.isEnabled()).isTrue();

		// TODO Valid3r classifiedTypes : Obtenir les schemata qu'on a défini, et vérifier q'une métadonnée de type reférenc3 pointant sur le domaine de valeur a été créée
	}

	@Test
	public void whenImportingCollectionTaxonomyConfigSettingsIfTaxonomyCodeIsEmptyThenExceptionIsRaised() throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addTaxonomy(new ImportedTaxonomy().setCode(null)
						.setTitles(toTitlesMap("Le titre de la taxo 1","First taxonomy's title"))
						.setClassifiedTypes(toClassifiedTypesList(DOCUMENT, FOLDER))
						.setVisibleOnHomePage(true)
						.setUsers(Arrays.asList(gandalf,bobGratton))
						.setUserGroups(Arrays.asList("groupCode1","groupCode2"))
				));

		assertThatErrorsWhileImportingSettingsExtracting().contains(tuple("SettingsImportServices_EmptyTaxonomyCode"));

	}

	@Test
	public void whenImportingCollectionTaxonomyConfigSettingsIfTaxonomyCodePrefixIsInvalidThenExceptionIsRaised() throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addTaxonomy(new ImportedTaxonomy().setCode("anotherPrefixTaxonomy")
						.setTitles(toTitlesMap("Le titre de la taxo 1","First taxonomy's title"))
						.setClassifiedTypes(toClassifiedTypesList(DOCUMENT, FOLDER))
						.setVisibleOnHomePage(true)
						.setUsers(Arrays.asList(gandalf,bobGratton))
						.setUserGroups(Arrays.asList("groupCode1","groupCode2"))
				));

		assertThatErrorsWhileImportingSettingsExtracting().contains(tuple("SettingsImportServices_InvalidTaxonomyCodePrefix"));

	}

	@Test
	public void whenImportingCollectionTaxonomyConfigSettingsIfTaxonomyCodeSuffixIsInvalidThenExceptionIsRaised() throws Exception {

		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addTaxonomy(new ImportedTaxonomy().setCode("taxoPrefixTaxonomy")
						.setTitles(toTitlesMap("Le titre de la taxo 1","First taxonomy's title"))
						.setClassifiedTypes(toClassifiedTypesList(DOCUMENT, FOLDER))
						.setVisibleOnHomePage(true)
						.setUsers(Arrays.asList(gandalf,bobGratton))
						.setUserGroups(Arrays.asList("groupCode1","groupCode2"))
				));

		assertThatErrorsWhileImportingSettingsExtracting().contains(tuple("SettingsImportServices_InvalidTaxonomyCodeSuffix"));

	}

	@Test
	public void whenImportingCollectionTaxonomyConfigSettingsThenConfigsAreSaved() throws Exception {

		String taxoCode = "taxoMyFirstType";
		settings.addCollectionsConfigs(new ImportedCollectionSettings().setCode(zeCollection)
				.addTaxonomy(new ImportedTaxonomy().setCode(taxoCode)
						.setTitles(toTitlesMap("Le titre de la taxo 1", "First taxonomy's title"))
						.setClassifiedTypes(toClassifiedTypesList(DOCUMENT, FOLDER))
						.setVisibleOnHomePage(true)
						.setUsers(Arrays.asList(gandalf, bobGratton))
						.setUserGroups(Arrays.asList("groupCode1", "groupCode2"))
				));

		importSettings();

		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);

		MetadataSchemaType metadataSchemaType = schemaTypes.getSchemaType(taxoCode);
		assertThat(metadataSchemaType).isNotNull();

	}

	//-------------------------------------------------------------------------------------

	private List<String> toClassifiedTypesList(String... classifiedType) {
		return Arrays.asList(classifiedType);
	}

	private Map<String, String> toTitlesMap(String title_fr, String title_en) {
		Map<String, String> titles = new HashMap<>();
		titles.put("title_fr", title_fr);
		titles.put("title_en", title_en);

		return titles;
	}

	private void importSettings()
			throws com.constellio.model.frameworks.validation.ValidationException {
		try {
			services.importSettings(settings);
		} catch (ValidationException e) {
			runTwice = false;
			throw e;

		} catch (RuntimeException e) {
			runTwice = false;
			throw e;
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

	@Before
	public void setUp() throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule(),
				withCollection("anotherCollection"));
		services = new SettingsImportServices(getAppLayerFactory());
		systemConfigurationsManager = getModelLayerFactory().getSystemConfigurationsManager();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

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
