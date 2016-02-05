package com.constellio.app.services.appManagement;

import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileNotFound;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.extensions.plugins.InvalidJarsTest;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;

//TODO Maxime : Vérifier pourquoi ce test est extrêmement lent
@SlowTest
public class AppManagementServicesAcceptanceTest extends ConstellioTest {

	@Mock ConstellioPluginManager pluginManager;

	File plugin1, plugin2, notAPlugin;

	File webappsFolder;
	File wrapperConf;
	File commandFile;
	File uploadWarFile;
	File pluginsFolder;

	AppManagementService appManagementService;
	FoldersLocator foldersLocator;

	@Before
	public void setup()
			throws IOException {

		webappsFolder = newTempFolder();
		File currentConstellioFolder = new File(webappsFolder, "webapp-5.0.5");
		currentConstellioFolder.mkdirs();
		commandFile = new File(newTempFolder(), "cmd");
		uploadWarFile = new File(newTempFolder(), "upload.war");
		wrapperConf = new File(newTempFolder(), "wrapper.conf");
		pluginsFolder = newTempFolder();
		FileUtils.copyFile(getTestResourceFile("initial-wrapper.conf"), wrapperConf);

		SystemGlobalConfigsManager systemGlobalConfigsManager = getAppLayerFactory().getSystemGlobalConfigsManager();
		ConstellioEIMConfigs eim = mock(ConstellioEIMConfigs.class);
		foldersLocator = getModelLayerFactory().getFoldersLocator();
		doReturn(currentConstellioFolder).when(foldersLocator).getConstellioWebappFolder();
		doReturn(commandFile).when(foldersLocator).getWrapperCommandFile();
		doReturn(wrapperConf).when(foldersLocator).getWrapperConf();
		doReturn(uploadWarFile).when(foldersLocator).getUploadConstellioWarFile();
		doReturn(pluginsFolder).when(foldersLocator).getPluginsJarsFolder();
		doReturn(true).when(eim).isCleanDuringInstall();
		appManagementService = spy(new AppManagementService(getIOLayerFactory(), foldersLocator, systemGlobalConfigsManager,
				eim, pluginManager));

		doReturn("5.0.4").when(appManagementService).getWarVersion();
	}

	@Test
	public void givenAValidWarFileHasBeenUpdatedWhenUpdateTriggeredThenUnzipInCorrectFolder()
			throws Exception {

		uploadADummyUpdateJarWithVersion("5.1.2");

		appManagementService.update(new ProgressInfo());

		String wrapperConfContent = readFileToString(wrapperConf)
				.replace(webappsFolder.getAbsolutePath() + File.separator, "/path/to/webapps/");

		assertThat(wrapperConfContent).isEqualTo(getTestResourceContent("expected-modified-wrapper.conf"));

		File newWebappVersion = new File(webappsFolder, "webapp-5.1.2");
		File newWebappVersionWEB_INF = new File(newWebappVersion, "WEB-INF");
		File newWebappVersionLib = new File(newWebappVersionWEB_INF, "lib");

		assertThat(uploadWarFile).doesNotExist();
		assertThat(readFileToString(new File(newWebappVersionLib, "core-app-5.1.2.jar"))).isEqualTo("The content of core app!");
		assertThat(readFileToString(new File(newWebappVersionLib, "core-model-5.1.2.jar")))
				.isEqualTo("The content of core model!");
		assertThat(readFileToString(new File(newWebappVersionLib, "core-data-5.1.2.jar"))).isEqualTo("The content of core data!");

	}

	@Test
	public void givenAValidWarFileWithUpdatedPluginsThenInstallThem()
			throws Exception {

		uploadADummyUpdateJarWithDummyPluginsAndVersion("5.1.2");

		appManagementService.update(new ProgressInfo());

		String wrapperConfContent = readFileToString(wrapperConf)
				.replace(webappsFolder.getAbsolutePath() + File.separator, "/path/to/webapps/");

		assertThat(wrapperConfContent).isEqualTo(getTestResourceContent("expected-modified-wrapper.conf"));

		File newWebappVersion = new File(webappsFolder, "webapp-5.1.2");
		File newWebappVersionWEB_INF = new File(newWebappVersion, "WEB-INF");
		File newWebappVersionLib = new File(newWebappVersionWEB_INF, "lib");
		File newWebappUpdatedPlugins = new File(webappsFolder, "updated-plugins");

		ArgumentCaptor<File> installedPluginsCaptor = ArgumentCaptor.forClass(File.class);
		verify(pluginManager, times(2)).prepareInstallablePlugin(installedPluginsCaptor.capture());

		List<File> installedPlugins = installedPluginsCaptor.getAllValues();
		assertThat(installedPlugins.get(0).getName()).isEqualTo("plugin1.jar");
		assertThat(installedPlugins.get(1).getName()).isEqualTo("PLUGIN2.JAR");

		assertThat(newWebappUpdatedPlugins).doesNotExist();

		assertThat(uploadWarFile).doesNotExist();
		assertThat(readFileToString(new File(newWebappVersionLib, "core-app-5.1.2.jar"))).isEqualTo("The content of core app!");
		assertThat(readFileToString(new File(newWebappVersionLib, "core-model-5.1.2.jar")))
				.isEqualTo("The content of core model!");
		assertThat(readFileToString(new File(newWebappVersionLib, "core-data-5.1.2.jar"))).isEqualTo("The content of core data!");

	}

	//FIXME
	@Test
	public void givenInstalledPluginsWhenUpdateTriggeredThenPluginsInCorrectFolder()
			throws Exception {
		InvalidJarsTest.loadJarsToPluginsFolder(pluginsFolder);

		uploadADummyUpdateJarWithVersion("5.1.2");

		appManagementService.update(new ProgressInfo());

		File newWebappVersion = new File(webappsFolder, "webapp-5.1.2");

		InvalidJarsTest.assertThatJarsLoadedCorrectly(new File(newWebappVersion, "WEB-INF/plugins"));
	}

	@Test
	public void givenWebAppFolderWithVersionsMoreRecentThanCurrentVersionWhenUpdateTriggeredThenCurrentVersionNotRemoved()
			throws Exception {

		InvalidJarsTest.loadJarsToPluginsFolder(pluginsFolder);

		addVersion("5.0");
		addVersion("5.0.7");
		addVersion("5.1.1");
		addVersion("5.1.1-1");
		addVersion("5.0.5-1");
		File versionToKeep1 = addVersion("5.1.1-3");
		File versionToKeep2 = addVersion("5.2.1");
		File versionToKeep3 = addVersion("5.2.1-4");
		File versionToKeep4 = addVersion("5.4.1");
		uploadADummyUpdateJarWithVersion("5.0.5");
		String[] expectedFilesNames = { versionToKeep1.getName(), versionToKeep2.getName(), versionToKeep3.getName(),
				versionToKeep4.getName(), "webapp-5.0.5-2" };

		appManagementService.update(new ProgressInfo());

		File[] keptFolders = webappsFolder.listFiles();
		List<String> keptFilesNames = new ArrayList<>();
		for (File keptFile : keptFolders) {
			keptFilesNames.add(keptFile.getName());
		}
		assertThat(keptFilesNames).containsOnly(expectedFilesNames);
	}

	@Test
	public void givenWebAppFolderWithInvalidVersionsWhenUpdateTriggeredThenOk()
			throws Exception {

		InvalidJarsTest.loadJarsToPluginsFolder(pluginsFolder);

		File invalidVersionToKeep1 = addVersion("5.1.1-3lol");
		File versionToKeep1 = addVersion("5.0.5");
		uploadADummyUpdateJarWithVersion("5.0.5");
		String[] expectedFilesNames = { invalidVersionToKeep1.getName(), versionToKeep1.getName(), "webapp-5.0.5-1" };

		appManagementService.update(new ProgressInfo());

		File[] keptFolders = webappsFolder.listFiles();
		List<String> keptFilesNames = new ArrayList<>();
		for (File keptFile : keptFolders) {
			keptFilesNames.add(keptFile.getName());
		}
		assertThat(keptFilesNames).containsOnly(expectedFilesNames);
	}

	@Test
	public void givenSeveralVersionsWhenUpdateTriggeredThenOnly5VersionsSaved()
			throws Exception {

		InvalidJarsTest.loadJarsToPluginsFolder(pluginsFolder);

		File fileBeforeLastWeek = getTestResourceFile("initial-wrapper.conf");

		assertThat(appManagementService.isModifiedBeforeLastWeek(fileBeforeLastWeek)).isTrue();
		addVersion("5.0");
		addVersion("5.0.7");
		addVersion("5.1.1");
		addVersion("5.1.1-1");
		File versionToKeep1 = addVersion("5.1.1-3");
		File versionToKeep2 = addVersion("5.2.1");
		File versionToKeep3 = addVersion("5.2.1-4");
		File versionToKeep4 = addVersion("5.4.1");
		uploadADummyUpdateJarWithVersion("5.4.1");
		String[] expectedFilesNames = { versionToKeep1.getName(), versionToKeep2.getName(), versionToKeep3.getName(),
				versionToKeep4.getName(), "webapp-5.4.1-1" };

		appManagementService.update(new ProgressInfo());

		File[] keptFolders = webappsFolder.listFiles();
		List<String> keptFilesNames = new ArrayList<>();
		for (File keptFile : keptFolders) {
			keptFilesNames.add(keptFile.getName());
		}
		assertThat(keptFilesNames).containsOnly(expectedFilesNames);
	}

	@Test(expected = AppManagementServiceRuntimeException.CannotConnectToServer.class)
	public void givenNoConnectionChangelogCannotBeRetrieve()
			throws Exception {
		doReturn(new LicenseInfo("", new LocalDate(), "")).when(appManagementService).getLicenseInfo();
		doReturn("").when(appManagementService).sendPost(any(String.class), any(String.class));
		doReturn(null).when(appManagementService).getInputForPost(any(String.class), any(String.class));

		appManagementService.getChangelogFromServer();
	}

	@Test
	public void givenConnectionChangelogCanBeRetrieve()
			throws Exception {
		InputStream tmp = getDummyInputStream();
		doReturn(new LicenseInfo("", new LocalDate(), "")).when(appManagementService).getLicenseInfo();
		doReturn("").when(appManagementService).sendPost(any(String.class), any(String.class));
		doReturn(tmp).when(appManagementService).getInputForPost(any(String.class), any(String.class));
		doReturn(false).when(appManagementService).isProxyPage(anyString());

		appManagementService.getChangelogFromServer();
		tmp.close();
	}

	//TODO Maxime
	//@Test(expected = AppManagementServiceRuntimeException.CannotConnectToServer.class)
	public void givenProxyConnectionWarCannotBeRetrieve()
			throws Exception {
		doReturn(new LicenseInfo("", new LocalDate(), "")).when(appManagementService).getLicenseInfo();
		doReturn("").when(appManagementService).sendPost(any(String.class), any(String.class));
		doReturn(null).when(appManagementService).getInputForPost(any(String.class), any(String.class));
		doReturn(true).when(appManagementService).isProxyPage(anyString());

		appManagementService.getWarFromServer(new ProgressInfo());
	}

	//TODO Maxime
	//@Test(expected = AppManagementServiceRuntimeException.CannotConnectToServer.class)
	public void givenNoConnectionWarCannotBeRetrieve()
			throws Exception {
		doReturn(new LicenseInfo("", new LocalDate(), "")).when(appManagementService).getLicenseInfo();
		doReturn("").when(appManagementService).sendPost(any(String.class), any(String.class));
		doReturn(null).when(appManagementService).getInputForPost(any(String.class), any(String.class));

		appManagementService.getWarFromServer(new ProgressInfo());
	}

	@Test
	public void givenConnectionWarCanBeRetrieve()
			throws Exception {
		InputStream tmp = getDummyInputStream();
		doReturn(new LicenseInfo("", new LocalDate(), "")).when(appManagementService).getLicenseInfo();
		doReturn("").when(appManagementService).sendPost(any(String.class), any(String.class));
		doReturn(tmp).when(appManagementService).getInputForPost(any(String.class), any(String.class));

		appManagementService.getWarFromServer(new ProgressInfo());
		tmp.close();
	}

	@Test
	public void givenUploadedWarIsSameVersionThenCanUpload()
			throws Exception {

		uploadADummyUpdateJarWithVersion("5.0.5");

		appManagementService.update(new ProgressInfo());

	}

	@Test(expected = AppManagementServiceRuntimeException.WarFileVersionMustBeHigher.class)
	public void givenUploadedWarIsPreviousVersionThenCannotUpload()
			throws Exception {

		uploadADummyUpdateJarWithVersion("5.0.3");

		appManagementService.update(new ProgressInfo());

	}

	@Test(expected = WarFileNotFound.class)
	public void givenWarIsNotUploadedThenCannotUpload()
			throws Exception {

		appManagementService.update(new ProgressInfo());

	}

	@Test
	public void givenVersionAndSubVersionWhenFindDeployFolderThenBehavesAsExpected()
			throws Exception {
		String version = "1.2";
		String versionWithSubVersion = version + "-3";
		File tempFolder = newTempFolder();

		addFile(tempFolder, "webapp-" + versionWithSubVersion + "lol");
		addFile(tempFolder, "webapp-" + version + "2");
		addFile(tempFolder, "webapp-" + version);
		addFile(tempFolder, "webapp-" + version + "-2");

		File folder = appManagementService.findDeployFolder(tempFolder, version);
		assertThat(folder.getName()).isEqualTo("webapp-" + versionWithSubVersion);
	}

	@Test
	public void givenSubVersionWhenFindDeployFolderThenBehavesAsExpected()
			throws Exception {
		String version = "1.2";
		String expectedVersion = version + "-3";
		File tempFolder = newTempFolder();

		addFile(tempFolder, "webapp-" + version + "lol");
		addFile(tempFolder, "webapp-" + version + "2");
		addFile(tempFolder, "webapp-" + version + "-2");

		File folder = appManagementService.findDeployFolder(tempFolder, version);
		assertThat(folder.getName()).isEqualTo("webapp-" + expectedVersion);
	}

	@Test
	public void givenVersionWhenFindDeployFolderThenBehavesAsExpected()
			throws Exception {
		String version = "1.2";
		File tempFolder = newTempFolder();

		addFile(tempFolder, "webapp-" + version + "lol");
		addFile(tempFolder, "webapp-" + version + "2");
		addFile(tempFolder, "webapp-" + version);

		File folder = appManagementService.findDeployFolder(tempFolder, version);
		assertThat(folder.getName()).isEqualTo("webapp-" + version + "-1");
	}

	@Test
	public void givenNeitherVersionNorSubVersionWhenFindDeployFolderThenBehavesAsExpected()
			throws Exception {
		String version = "1.2";
		File tempFolder = newTempFolder();

		addFile(tempFolder, "webapp-" + version + "lol");
		addFile(tempFolder, "webapp-" + version + "2");

		File folder = appManagementService.findDeployFolder(tempFolder, version);
		assertThat(folder.getName()).isEqualTo("webapp-" + version);

	}

	private InputStream getDummyInputStream() {
		try {
			File tmpContentFolder = newTempFolder();
			File warTmp = new File(tmpContentFolder, "tmpUpload.war");
			warTmp.createNewFile();
			return new FileInputStream(warTmp);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return null;
	}

	private File uploadADummyUpdateJarWithVersion(String version)
			throws Exception {
		File warContentFolder = new File(newTempFolder(), "webapp-" + version);
		File webInf = new File(warContentFolder, "WEB-INF");
		File lib = new File(webInf, "lib");
		lib.mkdirs();
		File coreApp = new File(lib, "core-app-" + version + ".jar");
		File coreModel = new File(lib, "core-model-" + version + ".jar");
		File coreData = new File(lib, "core-data-" + version + ".jar");

		FileUtils.write(coreApp, "The content of core app!");
		FileUtils.write(coreModel, "The content of core model!");
		FileUtils.write(coreData, "The content of core data!");

		getIOLayerFactory().newZipService().zip(uploadWarFile, asList(webInf));
		return warContentFolder;
	}

	private File uploadADummyUpdateJarWithDummyPluginsAndVersion(String version)
			throws Exception {
		File warContentFolder = new File(newTempFolder(), "webapp-" + version);
		File webInf = new File(warContentFolder, "WEB-INF");
		File lib = new File(webInf, "lib");
		lib.mkdirs();
		File coreApp = new File(lib, "core-app-" + version + ".jar");
		File coreModel = new File(lib, "core-model-" + version + ".jar");
		File coreData = new File(lib, "core-data-" + version + ".jar");

		FileUtils.write(coreApp, "The content of core app!");
		FileUtils.write(coreModel, "The content of core model!");
		FileUtils.write(coreData, "The content of core data!");

		File updatedPlugins = new File(warContentFolder, "updated-plugins");
		updatedPlugins.mkdirs();
		plugin1 = new File(updatedPlugins, "plugin1.jar");
		plugin2 = new File(updatedPlugins, "PLUGIN2.JAR");
		notAPlugin = new File(updatedPlugins, "plugin3.zip");
		FileUtils.write(plugin1, "A plugin");
		FileUtils.write(plugin2, "Another plugin");
		FileUtils.write(notAPlugin, "I am not a plugin - Mouhahahaha");

		getIOLayerFactory().newZipService().zip(uploadWarFile, asList(webInf, updatedPlugins));
		return warContentFolder;
	}

	private File addVersion(String version)
			throws Exception {
		File warContentFolder = new File(webappsFolder, "webapp-" + version);
		File webInf = new File(warContentFolder, "WEB-INF");
		File lib = new File(webInf, "lib");
		lib.mkdirs();
		File coreApp = new File(lib, "core-app-" + version + ".jar");
		File coreModel = new File(lib, "core-model-" + version + ".jar");
		File coreData = new File(lib, "core-data-" + version + ".jar");

		FileUtils.write(coreApp, "The content of core app!");
		FileUtils.write(coreModel, "The content of core model!");
		FileUtils.write(coreData, "The content of core data!");

		return warContentFolder;
	}

	private void addFile(File tempFolder, String fileName)
			throws IOException {
		FileUtils.writeStringToFile(new File(tempFolder, fileName), "a");
	}

}
