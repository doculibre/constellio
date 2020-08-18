package com.constellio.data.conf;

import com.constellio.sdk.tests.ConstellioTestWithGlobalContext;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
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

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FoldersLocatorGivenWrapperContextRealTest extends ConstellioTestWithGlobalContext {

	static String givenJavaRootFolderIsWebappFolder = "givenJavaRootFolderIsWebappFolder";
	static String givenJavaRootFolderIsNewWebappFolder = "givenJavaRootFolderIsNewWebappFolder";
	static File constellioProperties, keystore, constellioSetupProperties, wrapperInstallationFolder, conf, importation, bin, webapp,
			webapp2, webapp3, webinf, wrapperConf, command, deploy, temp, commandCmdTxt, uploadConstellioWar, settings, custom,
			lib, languageProfiles, dict, bpmns, anotherTemp, smtpMail, buildData, vaadin, themes, themesConstellio, classesFolder,
			themesConstellioImages, crypt, workFolder;
	String testCase;
	FoldersLocator foldersLocator;

	public FoldersLocatorGivenWrapperContextRealTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][]{
				{givenJavaRootFolderIsWebappFolder}
		});
	}

	@Test
	public void __prepareTests__()
			throws Exception {
		FoldersLocator.invalidateCaches();
		anotherTemp = null;

		wrapperInstallationFolder = modifyFileSystem().newTempFolder();
		webapp = new File(wrapperInstallationFolder, "webapp");
		webapp2 = new File(wrapperInstallationFolder, "webapp-5.1.0");
		webapp3 = new File(wrapperInstallationFolder, "webapp-5.2.1");
		webinf = new File(webapp, "WEB-INF");

		temp = new File(wrapperInstallationFolder, "temp");
		conf = new File(wrapperInstallationFolder, "conf");
		bpmns = new File(conf, "bpmns");
		smtpMail = new File(conf, "smtpMail");
		languageProfiles = new File(webinf, "languageProfiles");
		classesFolder = new File(webinf, "classes");
		dict = new File(webinf, "dict");
		bin = new File(wrapperInstallationFolder, "bin");
		settings = new File(conf, "settings");
		lib = new File(webinf, "lib");
		importation = new File(webinf, "to-import");
		constellioProperties = new File(conf, "constellio.properties");
		keystore = new File(conf, "keystore.jks");
		constellioSetupProperties = new File(conf, "constellio.setup.properties");
		wrapperConf = new File(conf, "wrapper.conf");
		deploy = new File(webinf, "constellio-deploy");
		command = new File(webinf, "command");
		commandCmdTxt = new File(command, "cmd");
		uploadConstellioWar = new File(wrapperInstallationFolder, "constellio.war");
		buildData = new File(webapp, "data.txt");
		vaadin = new File(webapp, "VAADIN");
		themes = new File(vaadin, "themes");
		themesConstellio = new File(themes, "constellio");
		themesConstellioImages = new File(themesConstellio, "images");
		crypt = new File(conf, "key.txt");
		workFolder = new File(wrapperInstallationFolder, "work");
		importation.mkdir();
		conf.mkdir();
		languageProfiles.mkdir();
		bin.mkdir();
		webapp.mkdir();
		webapp2.mkdir();
		webapp3.mkdir();
		bpmns.mkdir();
		smtpMail.mkdir();

		webinf.mkdir();
		lib.mkdir();
		command.mkdir();
		deploy.mkdir();
		temp.mkdir();
		settings.mkdir();
		dict.mkdir();
		themesConstellioImages.mkdirs();
		FileUtils.touch(commandCmdTxt);
		FileUtils.touch(constellioProperties);
		FileUtils.touch(wrapperConf);
	}

	@Before
	public void setUp()
			throws IOException {
		foldersLocator = newFoldersLocator(null, null, null);

	}

	private FoldersLocator newFoldersLocator(File customTempFolder, File customImportationFolder,
											 File customSettingsFolder) {
		FoldersLocator locator = Mockito.spy(new FoldersLocator());
		if (testCase == givenJavaRootFolderIsWebappFolder) {
			Mockito.doReturn(webapp).when(locator).getJavaRootFolder();
		} else if (testCase == givenJavaRootFolderIsNewWebappFolder) {
			Mockito.doReturn(webapp2).when(locator).getJavaRootFolder();
		}
		return locator;
	}

	@Test
	public void whenGetConstellioEncryptionFileThenReturnCorrectFolder() {
		Assertions.assertThat(foldersLocator.getConstellioEncryptionFile()).is(samePath(crypt));
	}

	@Test
	public void whenDetectingModeThenValidMode()
			throws Exception {
		Assertions.assertThat(foldersLocator.getFoldersLocatorMode()).isEqualTo(FoldersLocatorMode.WRAPPER);
	}

	@Test
	public void whenGetConstellioWrapperInstallationFolderThenReturnCorrectFolder() {
		Assertions.assertThat(foldersLocator.getWrapperInstallationFolder()).isEqualTo(wrapperInstallationFolder);
	}

	@Test
	public void whenGetConstellioWebappFolderThenReturnCorrectFolder() {
		Assertions.assertThat(foldersLocator.getConstellioWebappFolder().getName()).contains(webapp.getName());
	}

	@Test
	public void whenGetDictFolderThenReturnCorrectFolder() {
		Assertions.assertThat(foldersLocator.getDict()).isEqualTo(dict);
	}

	@Test
	public void whenGetConstellioLanguageSchemasFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getLanguageProfiles()).isEqualTo(languageProfiles);
	}

	@Test
	public void whenGetClassesPropFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getWebClasses()).isEqualTo(classesFolder);
	}

	@Test
	public void whenGetConstellioConfFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getConfFolder()).isEqualTo(conf);
	}

	@Test
	public void whenGetBPMNsFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getBPMNsFolder()).isEqualTo(bpmns);
	}

	@Test
	public void whenGetSmtpMailFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getSmtpMailFolder()).isEqualTo(smtpMail);
	}

	@Test
	public void whenGetConstellioPropertiesFileThenReturnCorrectFile()
			throws Exception {
		Assertions.assertThat(foldersLocator.getConstellioProperties()).isEqualTo(constellioProperties);
	}

	@Test
	public void whenGetKeystoreFileThenReturnCorrectFile()
			throws Exception {
		Assertions.assertThat(foldersLocator.getKeystoreFile()).isEqualTo(keystore);
	}

	@Test
	public void whenGetIntelliGISetupDPropertiesFileThenReturnCorrectFile()
			throws Exception {
		Assertions.assertThat(foldersLocator.getConstellioSetupProperties()).isEqualTo(constellioSetupProperties);
	}

	@Test
	public void whenGetConstellioWebInfFolderThenReturnCorrectFolder()
			throws Exception {
		Assertions.assertThat(foldersLocator.getConstellioWebinfFolder()).isEqualTo(webinf);
	}

	@Test
	public void whenGetBinFolderThenReturnCorrectFile()
			throws Exception {
		Assertions.assertThat(foldersLocator.getBinFolder()).isEqualTo(bin);
	}

	@Test
	public void whenGetWrapperCommandFolderFolderThenReturnCorrectFile()
			throws Exception {
		Assertions.assertThat(foldersLocator.getWrapperCommandFolder()).isEqualTo(command);
	}

	@Test
	public void whenGetWrapperCommandCmdTxtFileThenReturnCorrectFile()
			throws Exception {
		Assertions.assertThat(foldersLocator.getWrapperCommandFile()).isEqualTo(commandCmdTxt);
	}

	@Test
	public void whenGetWrapperConfFileThenReturnCorrectFile()
			throws Exception {
		Assertions.assertThat(foldersLocator.getWrapperConf()).isEqualTo(wrapperConf);
	}

	@Test
	public void whenGetWrapperUploadWarFileThenReturnCorrectFile()
			throws Exception {
		Assertions.assertThat(foldersLocator.getUploadConstellioWarFile()).isEqualTo(uploadConstellioWar);
	}

	@Test
	public void whenGetWrapperDefaultTempFolderThenReturnCorrectFolderInWrapperInstallationDir()
			throws Exception {
		Assertions.assertThat(foldersLocator.getDefaultTempFolder()).isEqualTo(temp);
	}

	@Test
	public void whenGetWrapperDefaultImportationFolderThenReturnCorrectFolderInWrapperInstallationDir()
			throws Exception {
		Assertions.assertThat(foldersLocator.getDefaultImportationFolder()).isEqualTo(importation);
	}

	@Test
	public void whenGetSettingsThenObtainCorrectFolder() {

		Assertions.assertThat(foldersLocator.getDefaultSettingsFolder()).isEqualTo(settings);
	}

	/*@Test
	public void whenGetBuildDataFileThenObtainCorrectFolder() {
		assertThat(foldersLocator.getBuildDataFile()).isEqualTo(buildData);
	}
	*/

	@Test
	public void whenGetLibFolderThenObtainCorrectFolder() {

		Assertions.assertThat(foldersLocator.getLibFolder()).isEqualTo(lib);
	}

	@Test
	public void whenConstellioThemesImagesThenObtainCorrectFolder() {
		Assertions.assertThat(foldersLocator.getConstellioThemeImages().getAbsolutePath())
				.isEqualTo(themesConstellioImages.getAbsolutePath());
	}

	@Test
	public void givenWebAppFolderWhenGetLibFolderForItThenReturnsDefaultLibFolder() {
		Assertions.assertThat(foldersLocator.getLibFolder())
				.isEqualTo(foldersLocator.getLibFolder(foldersLocator.getConstellioWebappFolder()));
	}

	@Test
	public void whenGetWorkFolderThenObtainCorrectFolderAndCreateItIfRequired() {
		Assertions.assertThat(foldersLocator.getWorkFolder()).isEqualTo(workFolder);
		Assertions.assertThat(workFolder).exists();
	}

	@Test
	public void givenWebAppFolderWhenGetPluginsFolderForItThenReturnsDefaultPluginFolder() {
		Assertions.assertThat(foldersLocator.getPluginsJarsFolder())
				.isEqualTo(foldersLocator.getPluginsJarsFolder(foldersLocator.getConstellioWebappFolder()));
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
