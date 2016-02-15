package com.constellio.app.services.extensions.plugins;

import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.ID_MISMATCH;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_EXISTING_ID;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_ID_FORMAT;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_JAR;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_MANIFEST;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_MIGRATION_SCRIPT;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_START;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_VERSION;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.MORE_THAN_ONE_INSTALLABLE_MODULE_PER_JAR;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.NO_ID;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.NO_INSTALLABLE_MODULE_DETECTED_FROM_JAR;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.NO_VERSION;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.INVALID;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.READY_TO_INSTALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.InvalidId.InvalidId_BlankId;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.InvalidId.InvalidId_ExistingId;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.InvalidId.InvalidId_NonAlphaNumeric;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class JSPFConstellioPluginManagerAcceptanceTest extends ConstellioTest {
	private ConstellioPluginManager pluginManager;

	private File validModule, withoutConstellioVersion, validModuleThrowingExceptionInMethodsDifferentFromStartAndMigrate,
			invalidIdFormat, existingId, jarWithInvalidVersion,
			idMissMatch, invalidModuleInMigrate, invalidModuleInStart, jarWithoutCode, jarWithoutVersion, jarWithoutManifest,
			inexisting, noInstallableModule, severalInstallableModules;
	private File pluginsFolder;

	@Before
	public void setUp()
			throws Exception {
		pluginManager = getAppLayerFactory().getPluginManager();
		pluginsFolder = getAppLayerFactory().getAppLayerConfiguration().getPluginsFolder();
		initTestFiles();
	}

	private void initTestFiles()
			throws IOException {
		File jarsFolder = new SDKFoldersLocator().getPluginsJarsFolder();
		validModule = new File(jarsFolder, "ValidModule.zip");
		withoutConstellioVersion = new File(jarsFolder, "WithoutConstellioVersion.zip");
		validModuleThrowingExceptionInMethodsDifferentFromStartAndMigrate = new File(jarsFolder,
				"ValidModuleThrowingExceptionInMethodsDifferentFromStartAndMigrate.zip");

		idMissMatch = new File(jarsFolder, "IdMissMatch.zip");
		invalidModuleInMigrate = new File(jarsFolder, "InvalidModuleInMigrate.zip");
		invalidModuleInStart = new File(jarsFolder, "InvalidModuleInStart.zip");
		noInstallableModule = new File(jarsFolder, "NoInstallableModule.zip");
		severalInstallableModules = new File(jarsFolder, "SeveralInstallableModules.zip");

		invalidIdFormat = new File(jarsFolder, "InvalidIdFormat.zip");
		existingId = new File(jarsFolder, "ModuleWithRMId.zip");
		jarWithInvalidVersion = new File(jarsFolder, "jarWithInvalidVersion.zip");
		jarWithoutCode = new File(jarsFolder, "jarWithoutCode.zip");
		jarWithoutVersion = new File(jarsFolder, "jarWithoutVersion.zip");
		jarWithoutManifest = new File(jarsFolder, "jarWithoutManifest.zip");
		inexisting = new File(jarsFolder, "inexisting.zip");

	}

	//@Test
	public void givenInvalidModuleWhenDetectPluginsThenPluginWithAdequateErrorStatus() {

		PluginActivationFailureCause errorCause = pluginManager.prepareInstallablePlugin(invalidModuleInMigrate);
		assertThat(errorCause).isNull();
		errorCause = pluginManager.prepareInstallablePlugin(invalidModuleInStart);
		assertThat(errorCause).isNull();
		errorCause = pluginManager.prepareInstallablePlugin(noInstallableModule);
		assertThat(errorCause).isNull();
		errorCause = pluginManager.prepareInstallablePlugin(severalInstallableModules);
		assertThat(errorCause).isNull();
		errorCause = pluginManager.prepareInstallablePlugin(idMissMatch);
		assertThat(errorCause).isNull();

		pluginManager.detectPlugins();

		List<ConstellioPluginInfo> invalidPlugins = pluginManager.getPlugins(INVALID);
		assertThat(invalidPlugins).extracting("code", "pluginActivationFailureCause").containsOnly(
				//tuple("InvalidModuleInMigrate", "INVALID_MIGRATION_SCRIPT"),
				//tuple("InvalidModuleInStart", "INVALID_START"),
				tuple("NoInstallableModule", NO_INSTALLABLE_MODULE_DETECTED_FROM_JAR),
				tuple("SeveralInstallableModules", MORE_THAN_ONE_INSTALLABLE_MODULE_PER_JAR),
				tuple("IdMissMatch2", ID_MISMATCH)
		);

		List<ConstellioPluginInfo> validPluginsUntilMigration = pluginManager.getPlugins(ENABLED);
		assertThat(validPluginsUntilMigration).extracting("code").contains(
				"InvalidModuleInMigrate", "InvalidModuleInStart");
		assertThat(pluginManager.getRegistredModulesAndActivePlugins()).extracting("id").contains(
				"InvalidModuleInMigrate", "InvalidModuleInStart"
		);

	}

	//@Test
	public void givenValidModuleWhenDetectPluginsThenModuleDetectedCorrectly() {
		PluginActivationFailureCause errorCause = pluginManager.prepareInstallablePlugin(validModule);
		assertThat(errorCause).isNull();
		errorCause = pluginManager.prepareInstallablePlugin(validModuleThrowingExceptionInMethodsDifferentFromStartAndMigrate);
		assertThat(errorCause).isNull();
		errorCause = pluginManager.prepareInstallablePlugin(withoutConstellioVersion);
		assertThat(errorCause).isNull();

		pluginManager.detectPlugins();

		List<ConstellioPluginInfo> invalidPlugins = pluginManager.getPlugins(INVALID);
		assertThat(invalidPlugins).isEmpty();

		List<ConstellioPluginInfo> validPlugins = pluginManager.getPlugins(ENABLED);
		assertThat(validPlugins).extracting("code").contains(
				"ValidModule", "ValidModuleThrowingExceptionInMethodsDifferentFromStartAndMigrate"
				, "WithoutConstellioVersion");
		assertThat(pluginManager.getRegistredModulesAndActivePlugins()).extracting("id").contains(
				"ValidModule", "ValidModuleThrowingExceptionInMethodsDifferentFromStartAndMigrate"
				, "WithoutConstellioVersion"
		);

	}

	@Test
	public void whenRegisterThenBehavesAsExpected() {
		List<InstallableModule> modulesBefore = pluginManager.getRegistredModulesAndActivePlugins();
		try {
			pluginManager.registerModule(new TestInstallableModule("rm"));
			fail("Could not register two modules with same id");
		} catch (InvalidId_ExistingId e) {
			//ok
		}

		try {
			pluginManager.registerModule(new TestInstallableModule("<"));
			fail("Could not register module with non alpha numeric id");
		} catch (InvalidId_NonAlphaNumeric e) {
			//ok
		}

		try {
			pluginManager.registerModule(new TestInstallableModule(null));
			fail("Could not register module with blank id");
		} catch (InvalidId_BlankId e) {
			//ok
		}

		try {
			pluginManager.registerModule(new TestInstallableModule(" "));
			fail("Could not register module with blank id");
		} catch (InvalidId_BlankId e) {
			//ok
		}

		InstallableModule validModule = new TestInstallableModule("validNonregistredId");
		pluginManager.registerModule(validModule);
		List<InstallableModule> modulesAfter = pluginManager.getRegistredModulesAndActivePlugins();
		List<InstallableModule> expectedModules = new ArrayList<>(modulesBefore);
		expectedModules.add(validModule);
		assertThat(modulesAfter).containsAll(expectedModules).extracting("id");
	}

	@Test
	public void givenValidModuleWhenPrepareInstallablePluginThenModuleAddedToConfigAndSaved() {
		PluginActivationFailureCause errorCause = pluginManager.prepareInstallablePlugin(validModule);
		assertThat(errorCause).isNull();
		//saved
		List<String> pluginsFolderFilesName = listFilesHavingNameInPluginsFolder("ValidModule.jar.new");
		assertThat(pluginsFolderFilesName.size()).isEqualTo(1);
		//added to config
		List<ConstellioPluginInfo> readyToInstallPlugins = pluginManager
				.getPlugins(READY_TO_INSTALL);
		assertThat(readyToInstallPlugins).extracting("code").contains("ValidModule");
	}

	@Test
	public void givenInvalidModuleWhenPrepareInstallablePluginThenReturnExpectedError() {
		PluginActivationFailureCause errorCause = pluginManager.prepareInstallablePlugin(jarWithoutManifest);
		assertThat(errorCause).isEqualTo(INVALID_MANIFEST);

		errorCause = pluginManager.prepareInstallablePlugin(inexisting);
		assertThat(errorCause).isEqualTo(INVALID_JAR);

		errorCause = pluginManager.prepareInstallablePlugin(jarWithInvalidVersion);
		assertThat(errorCause).isEqualTo(INVALID_VERSION);

		errorCause = pluginManager.prepareInstallablePlugin(jarWithoutCode);
		assertThat(errorCause).isEqualTo(NO_ID);

		errorCause = pluginManager.prepareInstallablePlugin(jarWithoutVersion);
		assertThat(errorCause).isEqualTo(NO_VERSION);

		addValidJarInfoToConfig(new ConstellioPluginInfo().setCode("ValidModule").setVersion("2.02").setPluginStatus(ENABLED));
		errorCause = pluginManager.prepareInstallablePlugin(validModule);
		assertThat(errorCause).isEqualTo(PluginActivationFailureCause.CANNOT_INSTALL_OLDER_VERSION);

		errorCause = pluginManager.prepareInstallablePlugin(invalidIdFormat);
		assertThat(errorCause).isEqualTo(INVALID_ID_FORMAT);

		errorCause = pluginManager.prepareInstallablePlugin(existingId);
		assertThat(errorCause).isEqualTo(INVALID_EXISTING_ID);

	}

	@Test
	public void whenMarkPluginAsDisabledThenConfigManagerCalledWithAppropriateParameter() {
		ConstellioPluginConfigurationManager mockedPluginManager = mock(ConstellioPluginConfigurationManager.class);
		JSPFConstellioPluginManager newPluginManager = new JSPFConstellioPluginManager(null, null, mockedPluginManager);
		newPluginManager.markPluginAsDisabled("id");
		verify(mockedPluginManager, times(1)).markPluginAsDisabled("id");
	}

	@Test
	public void whenMarkPluginAsEnabledThenConfigManagerCalledWithAppropriateParameter() {
		ConstellioPluginConfigurationManager mockedPluginManager = mock(ConstellioPluginConfigurationManager.class);
		JSPFConstellioPluginManager newPluginManager = new JSPFConstellioPluginManager(null, null, mockedPluginManager);
		newPluginManager.markPluginAsEnabled("id");
		verify(mockedPluginManager, times(1)).markPluginAsEnabled("id");
	}

	@Test
	public void whenHandleModuleNotStartedCorrectlyThenConfigManagerCalledWithAppropriateParameter() {
		ConstellioPluginConfigurationManager mockedPluginManager = mock(ConstellioPluginConfigurationManager.class);
		JSPFConstellioPluginManager newPluginManager = new JSPFConstellioPluginManager(null, null, mockedPluginManager);
		TestInstallableModule module = new TestInstallableModule("id");
		newPluginManager.handleModuleNotStartedCorrectly(module, zeCollection, null);
		verify(mockedPluginManager, times(1)).invalidateModule("id", INVALID_START, null);
	}

	@Test
	public void whenHandleModuleNotMigratedCorrectlyThenConfigManagerCalledWithAppropriateParameter() {
		ConstellioPluginConfigurationManager mockedPluginManager = mock(ConstellioPluginConfigurationManager.class);
		JSPFConstellioPluginManager newPluginManager = new JSPFConstellioPluginManager(null, null, mockedPluginManager);
		newPluginManager.handleModuleNotMigratedCorrectly("id", zeCollection, null);
		verify(mockedPluginManager, times(1)).invalidateModule("id", INVALID_MIGRATION_SCRIPT, null);
	}


	@Test
	public void whenPrepareInstallablePluginThenBehavesAsExpected() {

	}

	private class TestInstallableModule implements InstallableModule {
		private final String id;

		public TestInstallableModule(String id) {
			this.id = id;
		}

		@Override
		public List<MigrationScript> getMigrationScripts() {
			return null;
		}

		@Override
		public void configureNavigation(NavigationConfig config) {

		}

		@Override
		public void start(String collection, AppLayerFactory appLayerFactory) {

		}

		@Override
		public void stop(String collection, AppLayerFactory appLayerFactory) {

		}

		@Override
		public void addDemoData(String collection, AppLayerFactory appLayerFactory) {

		}

		@Override
		public boolean isComplementary() {
			return false;
		}

		@Override
		public List<String> getDependencies() {
			return null;
		}

		@Override
		public List<SystemConfiguration> getConfigurations() {
			return null;
		}

		@Override
		public Map<String, List<String>> getPermissions() {
			return null;
		}

		@Override
		public List<String> getRolesForCreator() {
			return null;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getPublisher() {
			return null;
		}
	}

	private List<String> listFilesHavingNameInPluginsFolder(String... fileNameFilter) {
		List<String> returnList = new ArrayList<>();
		if (fileNameFilter.length == 0) {
			return returnList;
		}
		for (File file : pluginsFolder.listFiles()) {
			for (String validName : fileNameFilter) {
				if (file.getName().equals(validName)) {
					returnList.add(validName);
				}
			}
		}
		return returnList;
	}

	private void addValidJarInfoToConfig(ConstellioPluginInfo info) {
		ConfigManager configManager = getModelLayerFactory().getDataLayerFactory()
				.getConfigManager();
		ConstellioPluginConfigurationManager pluginConfigManger = new ConstellioPluginConfigurationManager(configManager);
		pluginConfigManger.installPlugin(info.getCode(), info.getTitle(), info.getVersion(), info.getRequiredConstellioVersion());
		switch (info.getPluginStatus()) {
		case ENABLED:
			pluginConfigManger.markPluginAsEnabled(info.getCode());
			break;
		case DISABLED:
			pluginConfigManger.markPluginAsDisabled(info.getCode());
			break;
		case INVALID:
			pluginConfigManger.invalidateModule(info.getCode(), info.getPluginActivationFailureCause(), null);
			break;
		}
	}

}
