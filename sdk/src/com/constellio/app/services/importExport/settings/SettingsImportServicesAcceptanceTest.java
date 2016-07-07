package com.constellio.app.services.importExport.settings;

import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
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

	@Test
	public void whenImportConfigsThenSetted()
			throws Exception {
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("false"));

		//TODO Tester les configurations des autres types
		settings.addConfig(new ImportedConfig().setKey("documentRetentionRules").setValue("true"));
		settings.addConfig(new ImportedConfig().setKey("enforceCategoryAndRuleRelationshipInFolder").setValue("false"));
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("false"));

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateNumberOfYearWhenFixedRule").setValue("2015"));

		importSettings();

		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.DOCUMENT_RETENTION_RULES)).isEqualTo(true);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER))
				.isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);

		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE)).isEqualTo(2015);
	}

	@Test
	public void whenImportingUnknownConfigsThenConfigsAreNotSet() throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateUnknown").setValue("true"));

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
		services.importSettings(settings);
	}

	private ListAssert<Tuple> assertThatErrorsWhileImportingSettingsExtracting(String... parameters)
			throws com.constellio.model.frameworks.validation.ValidationException {

		try {
			importSettings();
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
	}
}
