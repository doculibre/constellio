package com.constellio.app.services.importExport.settings;

import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.constellio.model.frameworks.validation.ValidationException;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedConfig;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
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

		// Allow to enter retention rules for documents
		settings.addConfig(new ImportedConfig().setKey("documentRetentionRules").setValue("true"));

		importSettings();

		assertThat(systemConfigurationsManager.getValue(RMConfigs.CALCULATED_CLOSING_DATE)).isEqualTo(false);
		assertThat(systemConfigurationsManager.getValue(RMConfigs.DOCUMENT_RETENTION_RULES)).isEqualTo(true);
	}

	@Test
	public void whenImportingUnknownConfigsThenConfigsAreNotSet() throws Exception {

		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDateUnknown").setValue("true"));
		//TODO Tester les configurations des autres types

		try {
			importSettings();
		} catch (ValidationException e) {

			assertThat(extractingSimpleCodeAndParameters(e, "config", "key")).contains(
					tuple( "SettingsImportServices_calculatedCloseDateUnknown", null, null)
			);
		}

	}

	@Test(expected = ValidationException.class)
	public void whenImportBadBooleanConfigValueThenValidationExceptionThrown()
			throws Exception {
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("notABoolean"));
		//TODO Tester les configurations des autres types

		importSettings();
	}

	@Test()
	public void whenImportBadConfigsThenValidationExceptionWithCorrectMessageIsThrown()
			throws Exception {
		settings.addConfig(new ImportedConfig().setKey("calculatedCloseDate").setValue("notABoolean"));
		//TODO Tester les configurations des autres types

		try {
			importSettings();
		} catch (ValidationException e) {

			assertThat(extractingSimpleCodeAndParameters(e,"calculatedCloseDate")).containsOnly(
					tuple("invalidConfiguration", "notABoolean"));
		}

	}

	//-------------------------------------------------------------------------------------

	private void importSettings() throws com.constellio.model.frameworks.validation.ValidationException {
		services.importSettings(settings);
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
