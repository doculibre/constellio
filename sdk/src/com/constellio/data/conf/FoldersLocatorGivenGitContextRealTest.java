package com.constellio.data.conf;

import com.constellio.data.conf.FoldersLocatorRuntimeException.NotAvailableInGitMode;
import com.constellio.data.utils.LangUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.FileAssert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FoldersLocatorGivenGitContextRealTest extends ConstellioTest {

	static String givenJavaRootFolderIsConstellioProject = "givenJavaRootFolderIsConstellioProject";
	static String givenJavaRootFolderIsUIProject = "givenJavaRootFolderIsUIProject";
	static String givenJavaRootFolderIsServicesProject = "givenJavaRootFolderIsServicesProject";
	static String givenJavaRootFolderIsDaoProject = "givenJavaRootFolderIsDaoProject";
	static String givenJavaRootFolderIsCustomProject = "givenJavaRootFolderIsCustomProject";
	static String givenJavaRootFolderIsSDKProject = "givenJavaRootFolderIsSDKProject";
	static String givenJavaRootFolderIsPluginsSDKProject = "givenJavaRootFolderIsPluginsSDKProject";
	static String givenClassInBuildClassesMain = "givenClassInBuildClassesMain";
	static String givenClassInBin = "givenClassInBin";
	static File constellioWorkspace, constellioPlugins, constellioPluginsSdk, constellio, constellioApp, constellioData, constellioModel, webinf, conf, buildLibs, constellioProperties,
			constellioSetupProperties, deploy, cmdTxt, uploadConstellioWar, temp, importation, custom, settings, sdk,
			languageProfiles, dict, appProjectWebContent, bpmns, anotherTemp, smtpMail, i18n, resourcesReports,
			buildData, vaadin, themes, themesConstellio, themesConstellioImages, crypt,
			modelBuildClassesMainComConstellioModelConf, modelBinComConstellioModelConf, workFolder;
	String testCase;
	private FoldersLocator foldersLocator;

	public FoldersLocatorGivenGitContextRealTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][]{
				{givenJavaRootFolderIsConstellioProject}, {givenJavaRootFolderIsUIProject},
				{givenJavaRootFolderIsServicesProject}, {givenJavaRootFolderIsDaoProject},
				{givenJavaRootFolderIsCustomProject}, {givenJavaRootFolderIsSDKProject},
				{givenJavaRootFolderIsPluginsSDKProject}, {givenClassInBuildClassesMain},
				{givenClassInBin}});
	}

	@Before
	public void setUp()
			throws IOException {
		FoldersLocator.invalidateCaches();
		foldersLocator = newFoldersLocator(null, null, null);

	}

	@Test
	public void __prepareTests__()
			throws Exception {
		File tmp = newTempFolder();
		anotherTemp = newTempFolder();
		constellioWorkspace = new File(tmp, "consteLlio-dev-1-p");
		constellio = new File(constellioWorkspace, "constellio");
		modelBuildClassesMainComConstellioModelConf = new File(constellio,
				"model/build/classes/main/com/constellio/model/conf".replace("/", File.separator));
		modelBinComConstellioModelConf = new File(constellio,
				"model/bin/com/constellio/model/conf".replace("/", File.separator));
		constellioPlugins = new File(constellioWorkspace, "constellio-plugins");
		constellioPluginsSdk = new File(constellioPlugins, "sdk");
		constellioApp = new File(constellio, "app");
		constellioData = new File(constellio, "data");
		constellioModel = new File(constellio, "model");
		webinf = new File(constellio, "WEB-INF");
		conf = new File(constellio, "conf");
		bpmns = new File(conf, "bpmns");
		smtpMail = new File(conf, "smtpMail");
		//appProject = new File(constellio, "app");
		appProjectWebContent = new File(constellioApp, "WebContent");
		languageProfiles = new File(constellio, "languageProfiles");
		dict = new File(constellio, "dict");
		i18n = new File(constellio, "resources_i18n");
		settings = new File(conf, "settings");
		constellioProperties = new File(conf, "constellio.properties");
		constellioSetupProperties = new File(conf, "constellio.setup.properties");
		importation = new File(constellio, "to-import");
		temp = new File(constellio, "temp");
		File tempUpdateclient = new File(anotherTemp, "update-client");
		custom = new File(constellio, "custom");
		sdk = new File(constellio, "sdk");
		buildLibs = new File(constellio, "build" + File.separator + "libs");
		deploy = new File(tempUpdateclient, "constellio-deploy");
		cmdTxt = new File(temp, "cmd");
		uploadConstellioWar = new File(temp, "constellio.war");
		resourcesReports = new File(constellio, "resources" + File.separator + "reports");
		buildData = new File(constellio, "data.txt");
		workFolder = new File(sdk, "work");
		vaadin = new File(appProjectWebContent, "VAADIN");
		themes = new File(vaadin, "themes");
		themesConstellio = new File(themes, "constellio");
		themesConstellioImages = new File(themesConstellio, "images");
		crypt = new File(settings, "key.txt");

		constellioWorkspace.mkdir();
		constellio.mkdir();
		constellioApp.mkdir();
		constellioData.mkdir();
		constellioModel.mkdir();

		importation.mkdir();
		webinf.mkdir();
		conf.mkdir();
		bpmns.mkdir();
		smtpMail.mkdir();
		languageProfiles.mkdir();
		dict.mkdir();
		deploy.mkdirs();
		settings.mkdir();
		tempUpdateclient.mkdirs();
		custom.mkdir();
		buildLibs.mkdirs();
		//appProject.mkdirs();
		appProjectWebContent.mkdirs();
		FileUtils.touch(cmdTxt);
		FileUtils.touch(constellioProperties);
		resourcesReports.mkdirs();
		themesConstellioImages.mkdirs();
		modelBuildClassesMainComConstellioModelConf.mkdirs();
		modelBinComConstellioModelConf.mkdirs();
	}

	private FoldersLocator newFoldersLocator(File customTempFolder, File customImportationFolder,
											 File customSettingsFolder) {
		FoldersLocator locator = Mockito.spy(new FoldersLocator());
		if (testCase == givenJavaRootFolderIsConstellioProject) {
			Mockito.doReturn(constellio).when(locator).getJavaRootFolder();

		} else if (testCase == givenJavaRootFolderIsUIProject) {
			Mockito.doReturn(constellioApp).when(locator).getJavaRootFolder();

		} else if (testCase == givenJavaRootFolderIsServicesProject) {
			Mockito.doReturn(constellioModel).when(locator).getJavaRootFolder();

		} else if (testCase == givenJavaRootFolderIsDaoProject) {
			Mockito.doReturn(constellioData).when(locator).getJavaRootFolder();

		} else if (testCase == givenJavaRootFolderIsCustomProject) {
			Mockito.doReturn(custom).when(locator).getJavaRootFolder();

		} else if (testCase == givenJavaRootFolderIsSDKProject) {
			Mockito.doReturn(sdk).when(locator).getJavaRootFolder();

		} else if (testCase == givenJavaRootFolderIsPluginsSDKProject) {
			Mockito.doReturn(constellioPluginsSdk).when(locator).getJavaRootFolder();

		} else if (testCase == givenClassInBuildClassesMain) {
			String path = modelBuildClassesMainComConstellioModelConf.getAbsolutePath();
			Mockito.doReturn(path).when(locator).getCurrentClassPath();

		} else if (testCase == givenClassInBin) {
			String path = modelBinComConstellioModelConf.getAbsolutePath();
			Mockito.doReturn(path).when(locator).getCurrentClassPath();

		}
		return locator;
	}

	@Test
	public void whenDetectingModeThenValidMode()
			throws Exception {
		Assertions.assertThat(foldersLocator.getFoldersLocatorMode()).isEqualTo(FoldersLocatorMode.PROJECT);
	}

	@Test
	public void whenGetConstellioWebappFolderThenReturnCorrectFolder() {
		assertThatFile(foldersLocator.getConstellioWebappFolder()).isEqualTo(constellio);
	}

	@Test
	public void whenGetConstellioEncryptionFileThenReturnCorrectFile() {
		assertThatFile(foldersLocator.getConstellioEncryptionFile()).isEqualTo(crypt);
	}

	@Test
	public void whenGetDictFolderThenReturnCorrectFolder() {
		assertThatFile(foldersLocator.getDict()).isEqualTo(dict);
	}

	@Test
	public void whenGetI18nFolderThenReturnCorrectFolder() {
		assertThatFile(foldersLocator.getI18nFolder()).isEqualTo(i18n);
	}

	@Test
	public void whenGetReportsFolderThenReturnCorrectFolder() {
		assertThatFile(foldersLocator.getReportsResourceFolder()).isEqualTo(resourcesReports);
	}

	@Test
	public void whenGetConstellioLanguageSchemasFolderThenReturnCorrectFolder()
			throws Exception {
		assertThatFile(foldersLocator.getLanguageProfiles()).isEqualTo(languageProfiles);
	}

	@Test
	public void whenGetConstellioConfFolderThenReturnCorrectFolder()
			throws Exception {
		assertThatFile(foldersLocator.getConfFolder()).isEqualTo(conf);
	}

	@Test
	public void whenGetBPMNsFolderThenReturnCorrectFolder()
			throws Exception {
		assertThatFile(foldersLocator.getBPMNsFolder()).isEqualTo(bpmns);
	}

	@Test
	public void whenGetSmtpMailThenReturnCorrectFolder()
			throws Exception {
		assertThatFile(foldersLocator.getSmtpMailFolder()).isEqualTo(smtpMail);
	}

	@Test
	public void whenGetConstellioPropertiesFileThenReturnCorrectFile()
			throws Exception {
		assertThatFile(foldersLocator.getConstellioProperties()).isEqualTo(constellioProperties);
	}

	@Test
	public void whenGetIntelliGISetupDPropertiesFileThenReturnCorrectFile()
			throws Exception {
		assertThatFile(foldersLocator.getConstellioSetupProperties()).isEqualTo(constellioSetupProperties);
	}

	@Test
	public void whenGetConstellioWebInfFolderThenReturnCorrectFolder()
			throws Exception {
		assertThatFile(foldersLocator.getConstellioWebinfFolder()).isEqualTo(webinf);
	}

	@Test
	public void whenGetCommandFileThenObtainCorrectFileInUpdateClientTempFolder()
			throws Exception {
		assertThatFile(foldersLocator.getWrapperCommandFile()).isEqualTo(cmdTxt);
	}

	@Test
	public void whenGetWarFileThenObtainCorrectFileInUpdateClientTempFolder()
			throws Exception {
		assertThatFile(foldersLocator.getUploadConstellioWarFile()).isEqualTo(uploadConstellioWar);
	}

	@Test
	public void whenGetTempFolderThenObtainCorrectFolderInProjectTempFolder()
			throws Exception {
		assertThatFile(foldersLocator.getDefaultTempFolder()).isEqualTo(temp);
	}

	@Test
	public void whenGetAppProjectFolderThenObtainCorrectFolder()
			throws Exception {
		assertThatFile(foldersLocator.getAppProject()).isEqualTo(constellioApp);
	}

	@Test
	public void whenGetAppProjectWebContentFolderThenObtainCorrectFolder()
			throws Exception {
		assertThatFile(foldersLocator.getAppProjectWebContent()).isEqualTo(appProjectWebContent);
	}

	@Test
	public void whenGetDefaultImportationFolderThenObtainCorrectFolderInConstellioProject()
			throws Exception {
		assertThatFile(foldersLocator.getDefaultImportationFolder()).isEqualTo(importation);
	}

	@Test
	public void whenGetCustomProjectThenObtainCorrectFolder() {
		assertThatFile(foldersLocator.getCustomProject()).isEqualTo(custom);
	}

	@Test
	public void whenGetSDKProjectThenObtainCorrectFolder() {
		assertThatFile(foldersLocator.getSDKProject()).isEqualTo(sdk);
	}

	@Test
	public void whenGetBuildLibsThenObtainCorrectFolder() {
		assertThatFile(foldersLocator.getBuildLibs()).isEqualTo(buildLibs);
	}

	@Test
	public void whenGetSettingsThenObtainCorrectFolder() {
		assertThatFile(foldersLocator.getDefaultSettingsFolder()).isEqualTo(settings);
	}

	@Test
	public void whenGetWorkFolderThenObtainCorrectFolderAndCreateItIfRequired() {
		assertThatFile(foldersLocator.getWorkFolder()).isEqualTo(workFolder);
		Assertions.assertThat(workFolder).exists();
	}

	/*
	@Test
	public void whenGetBuildDataFileThenObtainCorrectFolder() {
		assertThatFile(foldersLocator.getBuildDataFile()).isEqualTo(buildData);
	}
	*/

	@Test(expected = NotAvailableInGitMode.class)
	public void whenGetLibFolderThenFolderNotAvailable() {
		foldersLocator.getLibFolder();
	}

	@Test
	public void whenConstellioThemesImagesThenObtainCorrectFolder() {
		assertThatFile(foldersLocator.getConstellioThemeImages())
				.isEqualTo(themesConstellioImages);
	}

	private Condition<? super File> samePath(final File expectedPath) {
		return new Condition<File>() {
			@Override
			public boolean matches(File value) {
				return expectedPath.getAbsolutePath().equals(value.getAbsolutePath());
			}
		};
	}

	private FileAssert assertThatFile(File file) {
		return Assertions.assertThat(file).usingComparator(new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				String p1 = o1 == null ? null : o1.getAbsolutePath();
				String p2 = o2 == null ? null : o2.getAbsolutePath();
				return LangUtils.isEqual(p1, p2) ? 0 : 1;
			}
		});
	}
}
