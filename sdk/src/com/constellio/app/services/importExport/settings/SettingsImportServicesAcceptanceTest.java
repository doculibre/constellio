package com.constellio.app.services.importExport.settings;

import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.sdk.tests.ConstellioTest;

public class SettingsImportServicesAcceptanceTest extends ConstellioTest {

	SettingsImportServices services;
	ImportedSettings settings = new ImportedSettings();
	ImportedCollectionSettings zeCollectionSettings = new ImportedCollectionSettings();
	ImportedCollectionSettings anotherCollectionSettings = new ImportedCollectionSettings();
	SystemConfigurationsManager systemConfigurationsManager;
	boolean runTwice;


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

		//TODO Tester les configurations des autres types
		settings.addConfig(new ImportedConfig().setKey("documentRetentionRules").setValue("true"));
		settings.addConfig(new ImportedConfig().setKey("enforceCategoryAndRuleRelationshipInFolder").setValue("false"));
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("false"));

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule").setValue("2015"));
		settings.addConfig(new ImportedConfig().setKey("closeDateRequiredDaysBeforeYearEnd").setValue("15"));

		settings.addConfig(new ImportedConfig().setKey("yearEndDate").setValue("02/28"));

		importSettings();

		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.DOCUMENT_RETENTION_RULES)).isEqualTo(true);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER)).isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);

		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE)).isEqualTo(2015);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_YEAR)).isEqualTo(15);

		assertThat(systemConfigurationsManager.getValue(RMConfigs.YEAR_END_DATE)).isEqualTo("02/28");
	}

	@Test
	public void whenImportingCollectionConfigsSettingsThenSetted()
			throws Exception {

		settings.addConfig(new ImportedConfig().setKey("decommissioningDateBasedOn").setValue("OPEN_DATE"));

		importSettings();

		assertThat(systemConfigurationsManager.getValue(RMConfigs.DECOMMISSIONING_DATE_BASED_ON)).isEqualTo(DecommissioningDateBasedOn.OPEN_DATE);
	}

	@Test
	public void whenImportingUnknownConfigsThenConfigsAreNotSet() throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateUnknown").setValue("true"));

		// TODO Valider l'extraction des erreur avec Francis
		assertThatErrorsWhileImportingSettingsExtracting("config").contains(
				tuple("SettingsImportServices_configurationNotFound", "calculatedCloseDateUnknown"));
	}

	@Test
	public void whenImportBadBooleanConfigValueThenValidationExceptionThrown() throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("notABoolean"));

		assertThatErrorsWhileImportingSettingsExtracting("calculatedCloseDate").containsOnly(
				tuple("SettingsImportServices_invalidConfigurationValue", "notABoolean"));
	}

	@Test
	public void whenImportingBadIntegerConfigValueThenValidationExceptionThrown() throws Exception {
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule")
				.setValue("helloInteger"));

		assertThatErrorsWhileImportingSettingsExtracting("calculatedCloseDateNumberOfYearWhenFixedRule").containsOnly(
				tuple("SettingsImportServices_invalidConfigurationValue", "helloInteger"));
	}

	@Test()
	public void whenImportBadConfigsThenValidationExceptionWithCorrectMessageIsThrown() throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("notABoolean"));
		//TODO Tester les configurations des autres types

		assertThatErrorsWhileImportingSettingsExtracting("calculatedCloseDate").containsOnly(
				tuple("SettingsImportServices_invalidConfigurationValue", "notABoolean"));
	}

	//-------------------------------------------------------------------------------------

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
