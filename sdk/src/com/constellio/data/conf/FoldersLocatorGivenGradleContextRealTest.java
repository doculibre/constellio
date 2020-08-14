package com.constellio.data.conf;

import com.constellio.sdk.tests.ConstellioTestWithGlobalContext;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FoldersLocatorGivenGradleContextRealTest extends ConstellioTestWithGlobalContext {

	static File constellio, constellioApp, constellioData, constellioModel, webinf, conf, buildLibs, constellioProperties,
			constellioSetupProperties, deploy, cmdTxt, uploadConstellioWar, temp, importation, custom, settings, sdk,
			languageProfiles, dict, appProjectWebContent, bpmns, anotherTemp, smtpMail, i18n, resourcesReports, classesFolder,
			buildData, vaadin, themes, themesConstellio, themesConstellioImages, crypt, workFolder;
	FoldersLocator foldersLocator;
	private static String classpath;

	@Test
	public void __prepareTests__()
			throws Exception {
		FoldersLocator.invalidateCaches();
		anotherTemp = null;

		File tmp = newTempFolder();
		anotherTemp = newTempFolder();
		constellio = new File(tmp, "consteLlio-dev-1-p");
		constellioApp = new File(constellio, "app");
		constellioData = new File(constellio, "data");
		constellioModel = new File(constellio, "model");
		webinf = new File(constellio, "WEB-INF");
		conf = new File(constellio, "conf");
		bpmns = new File(conf, "bpmns");
		smtpMail = new File(conf, "smtpMail");
		appProjectWebContent = new File(constellioApp, "WebContent");
		languageProfiles = new File(constellio, "languageProfiles");
		classesFolder = new File(webinf, "classes");
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
		vaadin = new File(appProjectWebContent, "VAADIN");
		themes = new File(vaadin, "themes");
		themesConstellio = new File(themes, "constellio");
		themesConstellioImages = new File(themesConstellio, "images");
		workFolder = new File(sdk, "work");
		crypt = new File(settings, "key.txt");

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

		classpath = constellio.getAbsolutePath() + "/file:" + constellio.getAbsolutePath()
					+ "/model/build/libs/core-model-5.0.4.jar!/com/constellio/model/conf";
	}

	@Before
	public void setUp()
			throws IOException {
		foldersLocator = Mockito.spy(new FoldersLocator());
		Mockito.doReturn(classpath).when(foldersLocator).getCurrentClassPath();
	}

	@Test
	public void whenDetectingModeThenValidMode()
			throws Exception {
		Assertions.assertThat(foldersLocator.getFoldersLocatorMode()).isEqualTo(FoldersLocatorMode.PROJECT);
	}

	@Test
	public void whenGradleClasspathIsJarThenRightJavaRootFolder() {
		Assertions.assertThat(foldersLocator.getJavaRootFolder().getAbsolutePath()).isEqualTo(constellio.getAbsolutePath());
	}

	@Test
	public void whenGetConstellioWebappFolderThenReturnCorrectFolder() {
		//	assertThat(foldersLocator.getConstellioWebappFolder()).isEqualTo(constellio);
	}

	@Test
	public void whenGetDictFolderThenReturnCorrectFolder() {
		Assertions.assertThat(foldersLocator.getDict().getAbsolutePath()).isEqualTo(dict.getAbsolutePath());
	}

	@Test
	public void whenGetI18nFolderThenReturnCorrectFolder() {
		//	assertThat(foldersLocator.getI18nFolder()).isEqualTo(i18n);
	}

	@Test
	public void whenGetConstellioEncryptionFileThenReturnCorrectFolder() {
		Assertions.assertThat(foldersLocator.getConstellioEncryptionFile()).is(samePath(crypt));
	}

	@Test
	public void whenGetReportsFolderThenReturnCorrectFolder() {
		//	assertThat(foldersLocator.getReportsResourceFolder()).isEqualTo(resourcesReports);
	}

	@Test
	public void whenGetConstellioLanguageSchemasFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getLanguageProfiles().getAbsolutePath()).isEqualTo(languageProfiles.getAbsolutePath());
	}

	@Test
	public void whenGetClassesPropFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getWebClasses().getAbsolutePath()).isEqualTo(classesFolder.getAbsolutePath());
	}

	@Test
	public void whenGetConstellioConfFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getConfFolder().getAbsolutePath()).isEqualTo(conf.getAbsolutePath());
	}

	@Test
	public void whenGetBPMNsFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getBPMNsFolder().getAbsolutePath()).isEqualTo(bpmns.getAbsolutePath());
	}

	@Test
	public void whenGetSmtpMailThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getSmtpMailFolder().getAbsolutePath()).isEqualTo(smtpMail.getAbsolutePath());
	}

	@Test
	public void whenGetConstellioPropertiesFileThenReturnCorrectFile()
			throws Exception {
		Assertions.assertThat(foldersLocator.getConstellioProperties().getAbsolutePath()).isEqualTo(constellioProperties.getAbsolutePath());
	}

	@Test
	public void whenGetIntelliGISetupDPropertiesFileThenReturnCorrectFile()
			throws Exception {
		Assertions.assertThat(foldersLocator.getConstellioSetupProperties().getAbsolutePath())
				.isEqualTo(constellioSetupProperties.getAbsolutePath());
	}

	@Test
	public void whenGetConstellioWebInfFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getConstellioWebinfFolder().getAbsolutePath()).isEqualTo(webinf.getAbsolutePath());
	}

	@Test
	public void whenGetCommandFileThenObtainCorrectFileInUpdateClientTempFolder()
			throws Exception {
		//	assertThat(foldersLocator.getWrapperCommandFile()).isEqualTo(cmdTxt);
	}

	@Test
	public void whenGetWarFileThenObtainCorrectFileInUpdateClientTempFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getUploadConstellioWarFile().getAbsolutePath())
				.isEqualTo(uploadConstellioWar.getAbsolutePath());
	}

	@Test
	public void whenGetTempFolderThenObtainCorrectFolderInProjectTempFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getDefaultTempFolder().getAbsolutePath()).isEqualTo(temp.getAbsolutePath());
	}

	@Test
	public void whenGetAppProjectFolderThenObtainCorrectFolder()
			throws Exception {
		//	assertThat(foldersLocator.getAppProject()).isEqualTo(constellioApp);
	}

	@Test
	public void whenGetAppProjectWebContentFolderThenObtainCorrectFolder()
			throws Exception {
		//	assertThat(foldersLocator.getAppProjectWebContent()).isEqualTo(appProjectWebContent);
	}

	@Test
	public void whenGetDefaultImportationFolderThenObtainCorrectFolderInConstellioProject()
			throws Exception {
		Assertions.assertThat(foldersLocator.getDefaultImportationFolder().getAbsolutePath()).isEqualTo(importation.getAbsolutePath());
	}

	@Test
	public void whenGetCustomProjectThenObtainCorrectFolder() {
		Assertions.assertThat(foldersLocator.getCustomProject().getAbsolutePath()).isEqualTo(custom.getAbsolutePath());
	}

	@Test
	public void whenGetSDKProjectThenObtainCorrectFolder() {
		//	assertThat(foldersLocator.getSDKProject()).isEqualTo(sdk);
	}

	@Test
	public void whenGetBuildLibsThenObtainCorrectFolder() {
		//	assertThat(foldersLocator.getBuildLibs()).isEqualTo(buildLibs);
	}

	@Test
	public void whenGetSettingsThenObtainCorrectFolder() {
		Assertions.assertThat(foldersLocator.getDefaultSettingsFolder().getAbsolutePath()).isEqualTo(settings.getAbsolutePath());
	}

	@Test
	public void whenConstellioThemesImagesThenObtainCorrectFolder() {
		Assertions.assertThat(foldersLocator.getConstellioThemeImages().getAbsolutePath())
				.isEqualTo(themesConstellioImages.getAbsolutePath());
	}

	@Test
	public void whenGetWorkFolderThenObtainCorrectFolderAndCreateItIfRequired() {
		Assertions.assertThat(foldersLocator.getWorkFolder().getAbsolutePath()).isEqualTo(workFolder.getAbsolutePath());
		Assertions.assertThat(workFolder).exists();
	}

	private Condition<? super File> samePath(final File expectedPath) {
		return new Condition<File>() {
			@Override
			public boolean matches(File value) {
				return expectedPath.getAbsolutePath().equals(value.getAbsolutePath());
			}
		};
	}

}