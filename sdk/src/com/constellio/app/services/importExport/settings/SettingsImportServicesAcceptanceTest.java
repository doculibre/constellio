package com.constellio.app.services.importExport.settings;

import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

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

		importSettings();

		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.DOCUMENT_RETENTION_RULES)).isEqualTo(true);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER))
				.isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);
	}

	@Test
	public void whenImportingUnknownConfigsThenConfigsAreNotSet()
			throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateUnknown").setValue("true"));
		//TODO Tester les configurations des autres types

		try {
			importSettings();
		} catch (ValidationException e) {

			assertThatErrorsWhileImportingSettingsExtracting("config", "key").contains(
					tuple("SettingsImportServices_calculatedCloseDateUnknown", null, null)
			);
		}

	}

	@Test
	public void whenImportBadBooleanConfigValueThenValidationExceptionThrown()
			throws Exception {
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("notABoolean"));
		//TODO Tester les configurations des autres types

		assertThatErrorsWhileImportingSettingsExtracting("config", "key").containsOnly(
				tuple("invalidConfiguration", "notABoolean")
		);
	}

	@Test()
	public void whenImportBadConfigsThenValidationExceptionWithCorrectMessageIsThrown()
			throws Exception {
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("notABoolean"));
		//TODO Tester les configurations des autres types

		try {
			importSettings();
		} catch (ValidationException e) {

			assertThat(extractingSimpleCodeAndParameters(e, "calculatedCloseDate")).containsOnly(
					tuple("invalidConfiguration", "notABoolean"));
		}

	}

	//-------------------------------------------------------------------------------------

	private void importSettings()
			throws com.constellio.model.frameworks.validation.ValidationException {
		services.importSettings(settings);
	}

	private ListAssert<Tuple> assertThatErrorsWhileImportingSettingsExtracting(String... parameters)
			throws com.constellio.model.frameworks.validation.ValidationException {

		try {
			services.importSettings(settings);
			fail("ValidationException expected");
			return null;
		} catch (ValidationException e) {

			return assertThat(extractingSimpleCodeAndParameters(e, parameters));
		}
	}

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule(),
				withCollection("anotherCollection"));
		services = new SettingsImportServices(getAppLayerFactory());
		systemConfigurationsManager = getModelLayerFactory().getSystemConfigurationsManager();
	}
}
