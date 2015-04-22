/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.conf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
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
public class FoldersLocatorGivenWrapperContextRealTest extends ConstellioTestWithGlobalContext {

	static String givenJavaRootFolderIsWebappFolder = "givenJavaRootFolderIsWebappFolder";
	static String givenJavaRootFolderIsNewWebappFolder = "givenJavaRootFolderIsNewWebappFolder";
	static File constellioProperties, constellioSetupProperties, wrapperInstallationFolder, conf, importation, bin, webapp,
			webapp2, webapp3, webinf, wrapperConf, command, deploy, temp, commandCmdTxt, uploadConstellioWar, settings, custom,
			lib, languageProfiles, dict, bpmns, anotherTemp, smtpMail;
	@Rule public TestRule benchmarkRun = new BenchmarkRule();
	String testCase;
	FoldersLocator foldersLocator;

	public FoldersLocatorGivenWrapperContextRealTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][] {
				{ givenJavaRootFolderIsWebappFolder }
		});
	}

	@Test
	public void __prepareTests__()
			throws Exception {

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
		dict = new File(webinf, "dict");
		bin = new File(wrapperInstallationFolder, "bin");
		settings = new File(conf, "settings");
		lib = new File(webinf, "lib");
		importation = new File(webinf, "to-import");
		constellioProperties = new File(conf, "constellio.properties");
		constellioSetupProperties = new File(conf, "constellio.setup.properties");
		wrapperConf = new File(conf, "wrapper.conf");
		deploy = new File(webinf, "constellio-deploy");
		command = new File(webinf, "command");
		commandCmdTxt = new File(command, "cmd");
		uploadConstellioWar = new File(wrapperInstallationFolder, "constellio.war");

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
		FileUtils.touch(commandCmdTxt);
		FileUtils.touch(constellioProperties);
		FileUtils.touch(wrapperConf);
	}

	@Before
	public void setUp()
			throws IOException {
		foldersLocator = newFoldersLocator(null, null, null);

	}

	private FoldersLocator newFoldersLocator(File customTempFolder, File customImportationFolder, File customSettingsFolder) {
		FoldersLocator locator = spy(new FoldersLocator());
		if (testCase == givenJavaRootFolderIsWebappFolder) {
			doReturn(webapp).when(locator).getJavaRootFolder();
		} else if (testCase == givenJavaRootFolderIsNewWebappFolder) {
			doReturn(webapp2).when(locator).getJavaRootFolder();
		}
		return locator;
	}

	@Test
	public void whenDetectingModeThenValidMode()
			throws Exception {
		assertThat(foldersLocator.getFoldersLocatorMode()).isEqualTo(FoldersLocatorMode.WRAPPER);
	}

	@Test
	public void whenGetConstellioWrapperInstallationFolderThenReturnCorrectFolder() {
		assertThat(foldersLocator.getWrapperInstallationFolder()).isEqualTo(wrapperInstallationFolder);
	}

	@Test
	public void whenGetConstellioWebappFolderThenReturnCorrectFolder() {
		assertThat(foldersLocator.getConstellioWebappFolder().getName()).contains(webapp.getName());
	}

	@Test
	public void whenGetDictFolderThenReturnCorrectFolder() {
		assertThat(foldersLocator.getDict()).isEqualTo(dict);
	}

	@Test
	public void whenGetConstellioLanguageSchemasFolderThenReturnCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getLanguageProfiles()).isEqualTo(languageProfiles);
	}

	@Test
	public void whenGetConstellioConfFolderThenReturnCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getConfFolder()).isEqualTo(conf);
	}

	@Test
	public void whenGetBPMNsFolderThenReturnCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getBPMNsFolder()).isEqualTo(bpmns);
	}

	@Test
	public void whenGetSmtpMailFolderThenReturnCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getSmtpMailFolder()).isEqualTo(smtpMail);
	}

	@Test
	public void whenGetConstellioPropertiesFileThenReturnCorrectFile()
			throws Exception {
		assertThat(foldersLocator.getConstellioProperties()).isEqualTo(constellioProperties);
	}

	@Test
	public void whenGetIntelliGISetupDPropertiesFileThenReturnCorrectFile()
			throws Exception {
		assertThat(foldersLocator.getConstellioSetupProperties()).isEqualTo(constellioSetupProperties);
	}

	@Test
	public void whenGetConstellioWebInfFolderThenReturnCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getConstellioWebinfFolder()).isEqualTo(webinf);
	}

	@Test
	public void whenGetBinFolderThenReturnCorrectFile()
			throws Exception {
		assertThat(foldersLocator.getBinFolder()).isEqualTo(bin);
	}

	@Test
	public void whenGetWrapperCommandFolderFolderThenReturnCorrectFile()
			throws Exception {
		assertThat(foldersLocator.getWrapperCommandFolder()).isEqualTo(command);
	}

	@Test
	public void whenGetWrapperCommandCmdTxtFileThenReturnCorrectFile()
			throws Exception {
		assertThat(foldersLocator.getWrapperCommandFile()).isEqualTo(commandCmdTxt);
	}

	@Test
	public void whenGetWrapperConfFileThenReturnCorrectFile()
			throws Exception {
		assertThat(foldersLocator.getWrapperConf()).isEqualTo(wrapperConf);
	}

	@Test
	public void whenGetWrapperUploadWarFileThenReturnCorrectFile()
			throws Exception {
		assertThat(foldersLocator.getUploadConstellioWarFile()).isEqualTo(uploadConstellioWar);
	}

	@Test
	public void whenGetWrapperDefaultTempFolderThenReturnCorrectFolderInWrapperInstallationDir()
			throws Exception {
		assertThat(foldersLocator.getDefaultTempFolder()).isEqualTo(temp);
	}

	@Test
	public void whenGetWrapperDefaultImportationFolderThenReturnCorrectFolderInWrapperInstallationDir()
			throws Exception {
		assertThat(foldersLocator.getDefaultImportationFolder()).isEqualTo(importation);
	}

	@Test
	public void whenGetSettingsThenObtainCorrectFolder() {

		assertThat(foldersLocator.getDefaultSettingsFolder()).isEqualTo(settings);
	}

	@Test
	public void whenGetLibFolderThenObtainCorrectFolder() {

		assertThat(foldersLocator.getLibFolder()).isEqualTo(lib);
	}

}
