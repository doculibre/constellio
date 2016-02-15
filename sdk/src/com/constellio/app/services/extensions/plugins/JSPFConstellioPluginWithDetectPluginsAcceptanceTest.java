package com.constellio.app.services.extensions.plugins;

import static com.constellio.app.services.extensions.plugins.JSPFConstellioPluginManager.PREVIOUS_PLUGINS;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.ID_MISMATCH;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.MORE_THAN_ONE_INSTALLABLE_MODULE_PER_JAR;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.NO_INSTALLABLE_MODULE_DETECTED_FROM_JAR;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class JSPFConstellioPluginWithDetectPluginsAcceptanceTest extends ConstellioTest {
	private ConstellioPluginManager pluginManager;

	@Before
	public void setUp()
			throws Exception {
		File stateFile = getTestResourceFile("saveStateWithPlugins.zip");
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(stateFile).withFakeEncryptionServices()
				.withPasswordsReset()
				.withSDKPluginFolder();
		pluginManager = getAppLayerFactory().getPluginManager();
		deletePreviousPluginsFolder();
	}

	@BeforeClass
	public static void beforeClass()
			throws Exception {
		createJarsFromZip(new SDKFoldersLocator().getPluginsJarsFolder());
	}

	private static void createJarsFromZip(File pluginsJarsFolder)
			throws IOException {
		for (File zip : FileUtils.listFiles(pluginsJarsFolder, new String[] { "zip" }, false)) {
			String zipNameWithoutExtension = StringUtils.substringBeforeLast(zip.getName(), ".zip");
			FileUtils.copyFile(zip, new File(pluginsJarsFolder, zipNameWithoutExtension + ".jar"));
		}
	}

	@Test
	public void givenInvalidModuleWhenDetectPluginsThenPluginWithAdequateErrorStatus() {
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
				"InvalidModuleInMigrate", "InvalidModuleInStart",
				"ValidModule", "ValidModuleThrowingExceptionInMethodsDifferentFromStartAndMigrate"
				, "WithoutConstellioVersion");
		assertThat(pluginManager.getRegistredModulesAndActivePlugins()).extracting("id").contains(
				"InvalidModuleInMigrate", "InvalidModuleInStart",
				"ValidModule", "ValidModuleThrowingExceptionInMethodsDifferentFromStartAndMigrate"
				, "WithoutConstellioVersion"
		);

	}


	@After
	public void deletePreviousPluginsFolder() {
		File pluginsFolder = getAppLayerFactory().getAppLayerConfiguration().getPluginsFolder();
		File previousPluginsFolder = new File(pluginsFolder, PREVIOUS_PLUGINS);
		FileUtils.deleteQuietly(previousPluginsFolder);
	}

}
