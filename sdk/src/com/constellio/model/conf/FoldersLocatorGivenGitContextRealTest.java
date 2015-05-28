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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import com.constellio.model.conf.FoldersLocatorRuntimeException.NotAvailableInGitMode;
import com.constellio.sdk.tests.ConstellioTest;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FoldersLocatorGivenGitContextRealTest extends ConstellioTest {

	static String givenJavaRootFolderIsConstellioProject = "givenJavaRootFolderIsConstellioProject";
	static String givenJavaRootFolderIsUIProject = "givenJavaRootFolderIsUIProject";
	static String givenJavaRootFolderIsServicesProject = "givenJavaRootFolderIsServicesProject";
	static String givenJavaRootFolderIsDaoProject = "givenJavaRootFolderIsDaoProject";
	static String givenJavaRootFolderIsCustomProject = "givenJavaRootFolderIsCustomProject";
	static String givenJavaRootFolderIsSDKProject = "givenJavaRootFolderIsSDKProject";
	static File constellio, constellioUi, constellioDao, constellioServices, webinf, conf, buildLibs, constellioProperties,
			constellioSetupProperties, deploy, cmdTxt, uploadConstellioWar, temp, importation, custom, settings, sdk,
			languageProfiles, appProject, dict, appProjectWebContent, bpmns, anotherTemp, smtpMail, i18n, reportsRecource,
			buildData;
	String testCase;
	private com.constellio.model.conf.FoldersLocator foldersLocator;

	public FoldersLocatorGivenGitContextRealTest(String testCase) {
		this.testCase = testCase;
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> testCases() {
		return Arrays.asList(new Object[][] { { givenJavaRootFolderIsConstellioProject }, { givenJavaRootFolderIsUIProject },
				{ givenJavaRootFolderIsServicesProject }, { givenJavaRootFolderIsDaoProject },
				{ givenJavaRootFolderIsCustomProject }, { givenJavaRootFolderIsSDKProject } });
	}

	@Before
	public void setUp()
			throws IOException {
		foldersLocator = newFoldersLocator(null, null, null);

	}

	@Test
	public void __prepareTests__()
			throws Exception {
		File tmp = newTempFolder();
		anotherTemp = newTempFolder();
		constellio = new File(tmp, "consteLlio-dev-1-p");
		constellioUi = new File(constellio, "ui");
		constellioDao = new File(constellio, "dao");
		constellioServices = new File(constellio, "services");
		webinf = new File(constellio, "WEB-INF");
		conf = new File(constellio, "conf");
		bpmns = new File(conf, "bpmns");
		smtpMail = new File(conf, "smtpMail");
		appProject = new File(constellio, "app");
		appProjectWebContent = new File(appProject, "WebContent");
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
		reportsRecource = new File(constellio, "reportsRecource");
		buildData = new File(constellio, "data.txt");

		constellio.mkdir();
		constellioUi.mkdir();
		constellioDao.mkdir();
		constellioServices.mkdir();

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
		appProject.mkdirs();
		appProjectWebContent.mkdirs();
		FileUtils.touch(cmdTxt);
		FileUtils.touch(constellioProperties);
		reportsRecource.mkdir();
	}

	private com.constellio.model.conf.FoldersLocator newFoldersLocator(File customTempFolder, File customImportationFolder,
			File customSettingsFolder) {
		com.constellio.model.conf.FoldersLocator locator = spy(new com.constellio.model.conf.FoldersLocator());
		if (testCase == givenJavaRootFolderIsConstellioProject) {
			doReturn(constellio).when(locator).getJavaRootFolder();
		} else if (testCase == givenJavaRootFolderIsUIProject) {
			doReturn(constellioUi).when(locator).getJavaRootFolder();
		} else if (testCase == givenJavaRootFolderIsServicesProject) {
			doReturn(constellioServices).when(locator).getJavaRootFolder();
		} else if (testCase == givenJavaRootFolderIsDaoProject) {
			doReturn(constellioDao).when(locator).getJavaRootFolder();
		} else if (testCase == givenJavaRootFolderIsCustomProject) {
			doReturn(custom).when(locator).getJavaRootFolder();
		} else if (testCase == givenJavaRootFolderIsSDKProject) {
			doReturn(sdk).when(locator).getJavaRootFolder();
		}
		return locator;
	}

	@Test
	public void whenDetectingModeThenValidMode()
			throws Exception {
		assertThat(foldersLocator.getFoldersLocatorMode()).isEqualTo(FoldersLocatorMode.PROJECT);
	}

	@Test
	public void whenGetConstellioWebappFolderThenReturnCorrectFolder() {
		assertThat(foldersLocator.getConstellioWebappFolder()).isEqualTo(constellio);
	}

	@Test
	public void whenGetDictFolderThenReturnCorrectFolder() {
		assertThat(foldersLocator.getDict()).isEqualTo(dict);
	}

	@Test
	public void whenGetI18nFolderThenReturnCorrectFolder() {
		assertThat(foldersLocator.getI18nFolder()).isEqualTo(i18n);
	}

	@Test
	public void whenGetReportsFolderThenReturnCorrectFolder() {
		assertThat(foldersLocator.getReportsResourceFolder()).isEqualTo(reportsRecource);
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
	public void whenGetSmtpMailThenReturnCorrectFolder()
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
	public void whenGetCommandFileThenObtainCorrectFileInUpdateClientTempFolder()
			throws Exception {
		assertThat(foldersLocator.getWrapperCommandFile()).isEqualTo(cmdTxt);
	}

	@Test
	public void whenGetWarFileThenObtainCorrectFileInUpdateClientTempFolder()
			throws Exception {
		assertThat(foldersLocator.getUploadConstellioWarFile()).isEqualTo(uploadConstellioWar);
	}

	@Test
	public void whenGetTempFolderThenObtainCorrectFolderInProjectTempFolder()
			throws Exception {
		assertThat(foldersLocator.getDefaultTempFolder()).isEqualTo(temp);
	}

	@Test
	public void whenGetAppProjectFolderThenObtainCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getAppProject()).isEqualTo(appProject);
	}

	@Test
	public void whenGetAppProjectWebContentFolderThenObtainCorrectFolder()
			throws Exception {
		assertThat(foldersLocator.getAppProjectWebContent()).isEqualTo(appProjectWebContent);
	}

	@Test
	public void whenGetDefaultImportationFolderThenObtainCorrectFolderInConstellioProject()
			throws Exception {
		assertThat(foldersLocator.getDefaultImportationFolder()).isEqualTo(importation);
	}

	@Test
	public void whenGetCustomProjectThenObtainCorrectFolder() {
		assertThat(foldersLocator.getCustomProject()).isEqualTo(custom);
	}

	@Test
	public void whenGetSDKProjectThenObtainCorrectFolder() {
		assertThat(foldersLocator.getSDKProject()).isEqualTo(sdk);
	}

	@Test
	public void whenGetBuildLibsThenObtainCorrectFolder() {
		assertThat(foldersLocator.getBuildLibs()).isEqualTo(buildLibs);
	}

	@Test
	public void whenGetSettingsThenObtainCorrectFolder() {
		assertThat(foldersLocator.getDefaultSettingsFolder()).isEqualTo(settings);
	}

	/*
	@Test
	public void whenGetBuildDataFileThenObtainCorrectFolder() {
		assertThat(foldersLocator.getBuildDataFile()).isEqualTo(buildData);
	}
	*/

	@Test(expected = NotAvailableInGitMode.class)
	public void whenGetLibFolderThenFolderNotAvailable() {
		foldersLocator.getLibFolder();
	}
}
