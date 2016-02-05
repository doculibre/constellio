package com.constellio.model.conf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.constellio.sdk.tests.ConstellioTestWithGlobalContext;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FoldersLocatorGivenTomcatContextRealTest extends ConstellioTestWithGlobalContext {

	static String givenJavaRootFolderIsTomcatInstallationBinFolder = "givenJavaRootFolderIsTomcatInstallationBinFolder";
	static String givenJavaRootFolderIsTomcatInstallationFolder = "givenJavaRootFolderIsTomcatInstallationFolder";
	static String givenJavaRootFolderIsTomcatWebappFolder = "givenJavaRootFolderIsTomcatWebappFolder";
	static File constellioProperties, constellioSetupProperties, tomcatInstallationFolder, conf, importation, bin, webapp,
			webapps, webinf, deploy, temp, uploadConstellioWar, settings, buildData,
			i18n_resources, lib, languageProfiles, dict, bpmns, anotherTemp, smtpMail, crypt;
	@Rule public TestRule benchmarkRun = new BenchmarkRule();
	String testCase;
	FoldersLocator foldersLocator;

	public FoldersLocatorGivenTomcatContextRealTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][] { { givenJavaRootFolderIsTomcatInstallationBinFolder },
				{ givenJavaRootFolderIsTomcatInstallationFolder }, { givenJavaRootFolderIsTomcatWebappFolder } });
	}

	@Test
	public void __prepareTests__()
			throws Exception {
		FoldersLocator.invalidateCaches();
		anotherTemp = null;

		File tomcatInstallationParentFolder = modifyFileSystem().newTempFolder();
		tomcatInstallationFolder = new File(tomcatInstallationParentFolder, "apache-tomcat-7.0.57");
		temp = new File(tomcatInstallationFolder, "temp");
		conf = new File(tomcatInstallationFolder, "conf");
		bpmns = new File(conf, "bpmns");
		smtpMail = new File(conf, "smtpMail");

		bin = new File(tomcatInstallationFolder, "bin");
		webapps = new File(tomcatInstallationFolder, "webapps");
		webapp = new File(webapps, "constellio");
		settings = new File(tomcatInstallationFolder, "settings");
		webinf = new File(webapp, "WEB-INF");
		lib = new File(webinf, "lib");
		importation = new File(tomcatInstallationFolder, "to-import");
		constellioProperties = new File(conf, "constellio.properties");
		constellioSetupProperties = new File(conf, "constellio.setup.properties");
		deploy = new File(tomcatInstallationFolder, "constellio-deploy");
		uploadConstellioWar = new File(tomcatInstallationFolder, "constellio.war");
		buildData = new File(webapp, "data.txt");
		languageProfiles = new File(webinf, "languageProfiles");
		i18n_resources = new File(webinf, "i18n_resources");
		dict = new File(webinf, "dict");
		crypt = new File(conf, "key.txt");

		tomcatInstallationFolder.mkdir();
		importation.mkdir();
		conf.mkdir();
		languageProfiles.mkdir();
		bin.mkdir();
		webapps.mkdir();
		webapp.mkdir();
		bpmns.mkdir();
		smtpMail.mkdir();

		webinf.mkdir();
		lib.mkdir();
		deploy.mkdir();
		temp.mkdir();
		settings.mkdir();
		dict.mkdir();
		FileUtils.touch(constellioProperties);
	}

	@Before
	public void setUp()
			throws IOException {
		foldersLocator = newFoldersLocator(null, null, null);
	}

	private FoldersLocator newFoldersLocator(File customTempFolder, File customImportationFolder, File customSettingsFolder) {
		FoldersLocator locator = spy(new FoldersLocator());

		if (testCase == givenJavaRootFolderIsTomcatInstallationBinFolder) {
			doReturn(bin).when(locator).getJavaRootFolder();

		} else if (testCase == givenJavaRootFolderIsTomcatWebappFolder) {
			doReturn(webapp).when(locator).getJavaRootFolder();

		} else if (testCase == givenJavaRootFolderIsTomcatInstallationFolder) {
			doReturn(tomcatInstallationFolder).when(locator).getJavaRootFolder();

		}
		return locator;
	}

	@Test
	public void whenDetectingModeThenValidMode()
			throws Exception {
		assertThat(foldersLocator.getFoldersLocatorMode()).isEqualTo(FoldersLocatorMode.TOMCAT);
	}

	@Test
	public void whenGetConstellioTomcatInstallationFolderThenReturnCorrectFolder() {
		assertThat(foldersLocator.getTomcatInstallationFolder()).is(samePath(tomcatInstallationFolder));
	}

	@Test
	public void whenGetConstellioWebappFolderThenReturnCorrectFolder() {
		assertThat(foldersLocator.getConstellioWebappFolder().getName()).contains(webapp.getName());
	}

	@Test
	public void whenGetConstellioEncryptionFileThenReturnCorrectFile() {
		assertThat(foldersLocator.getConstellioEncryptionFile()).is(samePath(crypt));
	}

	@Test
	public void whenGetDictFolderThenReturnCorrectFolder() {
		assertThat(foldersLocator.getDict()).is(samePath(dict));
	}

	@Test
	public void whenGetConstellioLanguageSchemasFolderThenReturnCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getLanguageProfiles()).is(samePath(languageProfiles));
	}

	@Test
	public void whenGetConstellioConfFolderThenReturnCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getConfFolder()).is(samePath(conf));
	}

	@Test
	public void whenGetBPMNsFolderThenReturnCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getBPMNsFolder()).is(samePath(bpmns));
	}

	@Test
	public void whenGetSmtpMailFolderThenReturnCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getSmtpMailFolder()).is(samePath(smtpMail));
	}

	@Test
	public void whenGetConstellioPropertiesFileThenReturnCorrectFile()
			throws Exception {
		assertThat(foldersLocator.getConstellioProperties()).is(samePath(constellioProperties));
	}

	@Test
	public void whenGetIntelliGISetupDPropertiesFileThenReturnCorrectFile()
			throws Exception {
		assertThat(foldersLocator.getConstellioSetupProperties()).is(samePath(constellioSetupProperties));
	}

	@Test
	public void whenGetConstellioWebInfFolderThenReturnCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getConstellioWebinfFolder()).is(samePath(webinf));
	}

	@Test
	public void whenGetWrapperDefaultTempFolderThenReturnCorrectFolderInWrapperInstallationDir()
			throws Exception {
		assertThat(foldersLocator.getDefaultTempFolder()).is(samePath(temp));
	}

	@Test
	public void whenGetWrapperDefaultImportationFolderThenReturnCorrectFolderInWrapperInstallationDir()
			throws Exception {
		assertThat(foldersLocator.getDefaultImportationFolder()).is(samePath(importation));
	}

	@Test
	public void whenGetSettingsThenObtainCorrectFolder() {

		assertThat(foldersLocator.getDefaultSettingsFolder()).is(samePath(settings));
	}

	@Test
	public void whenGetLibFolderThenObtainCorrectFolder() {

		assertThat(foldersLocator.getLibFolder()).is(samePath(lib));
	}

	/*
	@Test
	public void whenGetBuildDataFileThenObtainCorrectFolder() {
		assertThat(foldersLocator.getBuildDataFile()).isEqualTo(buildData);
	}
*/
	private Condition<? super File> samePath(final File expectedPath) {
		return new Condition<File>() {
			@Override
			public boolean matches(File value) {
				return expectedPath.getAbsolutePath().equals(value.getAbsolutePath());
			}
		};
	}

}
