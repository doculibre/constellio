package com.constellio.app.services.extensions.plugins;

import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_InvalidJar;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_InvalidManifest;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_NoCode;
import com.constellio.app.services.extensions.plugins.InvalidPluginJarException.InvalidPluginJarException_NoVersion;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.constellio.app.services.extensions.plugins.JSPFPluginServices.NEW_JAR_EXTENSION;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.CANNOT_INSTALL_OLDER_VERSION;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_EXISTING_ID;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.INVALID_VERSION;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.DISABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class JSPFPluginServicesTestAcceptanceTest extends ConstellioTest {

	File notAJar, nonExistingJar, jarWithInvalidVersion, jarWithoutCode, jarWithoutManifest, jarWithoutVersion,
			jarWithValidManifest, jarWithoutConstellioVersionValue;
	ConstellioPluginInfo infoWithBlankVersion, infoWithNullVersion, infoWithInvalidVersion1, infoWithInvalidVersion2, infoWithInvalidVersion3,
			infoWithBlankCode, infoWithNullCode,
			validInfoWithVersionA, validInfoWithVersionBeforeA, validInfoWithVersionAfterA, validInfoWithVersionAfterAWithDisabledStatus;

	JSPFPluginServices services;

	@Before
	public void setUp()
			throws Exception {
		services = new JSPFPluginServices(new IOServices(newTempFolder()));
	}

	@Test
	public void whenExtractPluginInfoThenBehavesAsExpected()
			throws InvalidPluginJarException, IOException {
		loadJars();
		try {
			services.extractPluginInfo(notAJar);
		} catch (InvalidPluginJarException e) {
			if (e instanceof InvalidPluginJarException_InvalidManifest) {
				//ok
			} else {
				fail("expected invalid jar with invalid manifest", e);
			}
		}

		try {
			services.extractPluginInfo(nonExistingJar);
		} catch (InvalidPluginJarException e) {
			if (e instanceof InvalidPluginJarException_InvalidJar) {
				//ok
			} else {
				fail("expected invalid jar", e);
			}
		}

		try {
			services.extractPluginInfo(jarWithoutCode);
		} catch (InvalidPluginJarException e) {
			if (e instanceof InvalidPluginJarException_NoCode) {
				//ok
			} else {
				fail("expected invalid jar with no code", e);
			}
		}

		try {
			services.extractPluginInfo(jarWithoutManifest);
		} catch (InvalidPluginJarException e) {
			if (e instanceof InvalidPluginJarException_InvalidManifest) {
				//ok
			} else {
				fail("expected invalid jar with invalid manifest", e);
			}
		}

		try {
			services.extractPluginInfo(jarWithoutVersion);
		} catch (InvalidPluginJarException e) {
			if (e instanceof InvalidPluginJarException_NoVersion) {
				//ok
			} else {
				fail("expected invalid jar with no version", e);
			}
		}

		ConstellioPluginInfo info = null;
		info = services.extractPluginInfo(jarWithInvalidVersion);
		assertThat(info.getCode()).isEqualTo("InvalidComplementaryModuleInMigrateForZeCollection");
		assertThat(info.getRequiredConstellioVersion()).isEqualTo("");
		assertThat(info.getLastInstallDate()).isNull();
		assertThat(info.getVersion()).isEqualTo("1.0lol");

		info = services.extractPluginInfo(jarWithValidManifest);
		assertThat(info.getCode()).isEqualTo("InvalidComplementaryModuleInMigrateForZeCollection");
		assertThat(info.getRequiredConstellioVersion()).isEqualTo("5.1");
		assertThat(info.getLastInstallDate()).isNull();
		assertThat(info.getVersion()).isEqualTo("1.0");

		info = services.extractPluginInfo(jarWithoutConstellioVersionValue);
		assertThat(info.getCode()).isEqualTo("WithoutConstellioVersion");
		assertThat(info.getRequiredConstellioVersion()).isEqualTo("");
		assertThat(info.getLastInstallDate()).isNull();
		assertThat(info.getVersion()).isEqualTo("1.02");
	}

	@Test
	public void whenValidatePluginThenBehavesAsExpected() {
		initPluginInfos();
		assertThat(services.validatePlugin(infoWithBlankVersion, null)).isEqualTo(INVALID_VERSION);
		assertThat(services.validatePlugin(infoWithNullVersion, null)).isEqualTo(INVALID_VERSION);
		assertThat(services.validatePlugin(infoWithInvalidVersion1, null)).isEqualTo(INVALID_VERSION);
		assertThat(services.validatePlugin(infoWithInvalidVersion2, null)).isEqualTo(INVALID_VERSION);
		assertThat(services.validatePlugin(infoWithInvalidVersion3, null)).isEqualTo(INVALID_VERSION);
		assertThat(services.validatePlugin(infoWithBlankCode, null)).isEqualTo(INVALID_EXISTING_ID);
		assertThat(services.validatePlugin(infoWithNullCode, null)).isEqualTo(INVALID_EXISTING_ID);
		assertThat(services.validatePlugin(validInfoWithVersionA, null)).isNull();
		assertThat(services.validatePlugin(validInfoWithVersionA, validInfoWithVersionBeforeA)).isNull();
		assertThat(services.validatePlugin(validInfoWithVersionA, validInfoWithVersionAfterA))
				.isEqualTo(CANNOT_INSTALL_OLDER_VERSION);
		assertThat(services.validatePlugin(validInfoWithVersionA, validInfoWithVersionAfterAWithDisabledStatus)).isNull();
	}

	@Test
	public void whenSaveNewPluginThenPluginSavedWithAdequateName()
			throws IOException {
		File tempDir = newTempFolder();
		File jar = newTempFileWithContent("test", "I am not a jar");

		ConstellioPluginInfo info = newValidInfo();
		services.saveNewPlugin(tempDir, jar, info.getCode());

		File jarCopy = FileUtils.getFile(tempDir, info.getCode() + "." + NEW_JAR_EXTENSION);
		assertThat(jarCopy.exists()).isTrue();
		assertThat(FileUtils.readLines(jar)).containsExactlyElementsOf(FileUtils.readLines(jarCopy));
	}

	@Test
	public void whenSaveNewPluginASecondTimeThenPluginReplacesOldOne()
			throws IOException {
		File tempDir = newTempFolder();
		File jar1 = newTempFileWithContent("test", "I am not a jar");

		ConstellioPluginInfo info = newValidInfo();
		services.saveNewPlugin(tempDir, jar1, info.getCode());
		File jar2 = newTempFileWithContent("test", "I am not a jar 2 having a different content");
		services.saveNewPlugin(tempDir, jar2, info.getCode());

		File jarCopy = FileUtils.getFile(tempDir, info.getCode() + "." + NEW_JAR_EXTENSION);
		assertThat(jarCopy.exists()).isTrue();
		assertThat(FileUtils.readLines(jar2)).containsExactlyElementsOf(FileUtils.readLines(jarCopy));
	}

	@Test
	public void whenExtractI18nFromPluginsWithoutI18nThenExtractNothing()
			throws IOException {
		loadJars();
		File tempDir = newTempFolder();

		services.extractPluginResources(jarWithValidManifest, "zePlugin", tempDir);

		assertThat(tempDir.listFiles()).isEmpty();
	}

	@Test
	public void whenExtractI18nFromPluginsWithI18nThenExtracted()
			throws IOException {
		File tempDir = newTempFolder();

		File jarWithI18n = getTestResourceFile("zePluginWithI18n.zip");
		services.extractPluginResources(jarWithI18n, "zePlugin", tempDir);

		File zePluginsI18n = new File(tempDir,
				"zePlugin" + File.separator + "i18n" + File.separator + "zePlugin_i18n.properties");

		assertThat(zePluginsI18n).exists();
		assertThat(zePluginsI18n.length()).isGreaterThan(0);

	}

	@Test
	public void whenReplaceOldPluginVersionsByNewOnesThenOk()
			throws IOException {
		File jar1NewVersion, jar1ExistingVersion, jar1PreviousVersion, jar2PreviousVersion, jar3NewVersion, jar4ExistingVersion;
		//init
		File tempDir = newTempFolder();
		File previousVersionFolder = newTempFolder();
		jar1NewVersion = newTempFileWithContentInFolder(tempDir, "jar1." + NEW_JAR_EXTENSION, "jar1NewVersion");
		jar1ExistingVersion = newTempFileWithContentInFolder(tempDir, "jar1.jar", "jar1ExistingVersion");
		jar1PreviousVersion = newTempFileWithContentInFolder(previousVersionFolder, "jar1.jar", "jar1PreviousVersion");
		jar2PreviousVersion = newTempFileWithContentInFolder(previousVersionFolder, "jar2.jar", "jar2PreviousVersion");
		jar3NewVersion = newTempFileWithContentInFolder(tempDir, "jar3." + NEW_JAR_EXTENSION, "jar3NewVersion");
		jar4ExistingVersion = newTempFileWithContentInFolder(tempDir, "jar4.jar", "jar4ExistingVersion");

		JSPFPluginServices.pluginsWithReplacementException = null;
		services.replaceOldPluginVersionsByNewOnes(tempDir, previousVersionFolder);

		assertThat(jar1NewVersion.exists()).isFalse();
		assertThat(FileUtils.readLines(jar1ExistingVersion)).containsExactly("jar1NewVersion");
		assertThat(FileUtils.readLines(jar1PreviousVersion)).containsExactly("jar1ExistingVersion");

		assertThat(FileUtils.readLines(jar2PreviousVersion)).containsExactly("jar2PreviousVersion");

		assertThat(jar3NewVersion.exists()).isFalse();
		File file3ExistingVersion = new File(tempDir, "jar3.jar");
		assertThat(FileUtils.readLines(file3ExistingVersion)).containsExactly("jar3NewVersion");

		assertThat(FileUtils.readLines(jar4ExistingVersion)).containsExactly("jar4ExistingVersion");

	}

	private void initPluginInfos() {
		infoWithBlankVersion = newValidInfo().setVersion(" ");
		infoWithNullVersion = newValidInfo().setVersion(null);
		infoWithInvalidVersion1 = newValidInfo().setVersion("1.p");
		infoWithInvalidVersion2 = newValidInfo().setVersion("1.");
		infoWithInvalidVersion3 = newValidInfo().setVersion(".1.0");

		infoWithBlankCode = newValidInfo().setCode("\t");
		infoWithNullCode = newValidInfo().setCode(null);
		validInfoWithVersionA = newValidInfo();
		validInfoWithVersionBeforeA = newValidInfo().setVersion("5.0.9");
		validInfoWithVersionAfterA = newValidInfo().setVersion("6");
		validInfoWithVersionAfterAWithDisabledStatus = newValidInfo().setVersion("6").setPluginStatus(DISABLED);
	}

	private ConstellioPluginInfo newValidInfo() {
		return new ConstellioPluginInfo().setCode("zCode").setVersion("5.1").setPluginStatus(ENABLED);
	}

	private void loadJars()
			throws IOException {
		File jarsFolder = new SDKFoldersLocator().getPluginsJarsFolder();
		notAJar = FileUtils.getFile(jarsFolder, "notAjar.zip");
		assertThat(notAJar.exists()).isTrue();
		jarWithInvalidVersion = FileUtils.getFile(jarsFolder, "jarWithInvalidVersion.zip");
		assertThat(jarWithInvalidVersion.exists()).isTrue();
		jarWithoutCode = FileUtils.getFile(jarsFolder, "jarWithoutCode.zip");
		assertThat(jarWithoutCode.exists()).isTrue();
		jarWithoutManifest = FileUtils.getFile(jarsFolder, "jarWithoutManifest.zip");
		assertThat(jarWithoutManifest.exists()).isTrue();
		jarWithoutVersion = FileUtils.getFile(jarsFolder, "jarWithoutVersion.zip");
		assertThat(jarWithoutVersion.exists()).isTrue();
		jarWithValidManifest = FileUtils.getFile(jarsFolder, "jarWithValidManifest.zip");
		assertThat(jarWithValidManifest.exists()).isTrue();
		jarWithoutConstellioVersionValue = FileUtils.getFile(jarsFolder, "WithoutConstellioVersion.zip");
		assertThat(jarWithoutConstellioVersionValue.exists()).isTrue();
		nonExistingJar = new File("tmp");
		FileUtils.deleteQuietly(nonExistingJar);
	}

}

