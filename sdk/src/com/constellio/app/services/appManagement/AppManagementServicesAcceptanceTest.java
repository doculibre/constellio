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
package com.constellio.app.services.appManagement;

import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.services.appManagement.AppManagementServiceRuntimeException.WarFileNotFound;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.sdk.tests.ConstellioTest;

public class AppManagementServicesAcceptanceTest extends ConstellioTest {

	File webappsFolder;
	File wrapperConf;
	File commandFile;
	File uploadWarFile;

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
		FileUtils.copyFile(getTestResourceFile("initial-wrapper.conf"), wrapperConf);

		foldersLocator = getModelLayerFactory().getFoldersLocator();
		doReturn(currentConstellioFolder).when(foldersLocator).getConstellioWebappFolder();
		doReturn(commandFile).when(foldersLocator).getWrapperCommandFile();
		doReturn(wrapperConf).when(foldersLocator).getWrapperConf();
		doReturn(uploadWarFile).when(foldersLocator).getUploadConstellioWarFile();
		appManagementService = spy(new AppManagementService(getIOLayerFactory(), foldersLocator));

		doReturn("5.0.5").when(appManagementService).getWarVersion();
	}

	@Test
	public void givenAValidWarFileHasBeenUpdatedWhenUpdateTriggeredThenUnzipInCorrectFolder()
			throws Exception {

		uploadADummyUpdateJarWithVersion("5.1.2");

		appManagementService.update(new ProgressInfo());

		String wrapperConfContent = readFileToString(wrapperConf).replace(webappsFolder.getAbsolutePath(), "/path/to/webapps");

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

	@Test(expected = AppManagementServiceRuntimeException.CannotConnectToServer.class)
	public void givenNoConnectionChangelogCannotBeRetrieve()
			throws Exception {
		doReturn(null).when(appManagementService).getStreamForURL(AppManagementService.URL_CHANGELOG);

		appManagementService.getChangelogFromServer();
	}

	@Test
	public void givenConnectionChangelogCanBeRetrieve()
			throws Exception {
		InputStream tmp = getDummyInputStream();
		doReturn(tmp).when(appManagementService).getStreamForURL(AppManagementService.URL_CHANGELOG);
		doReturn(false).when(appManagementService).isProxyPage(anyString());

		appManagementService.getChangelogFromServer();
		tmp.close();
	}

	@Test(expected = AppManagementServiceRuntimeException.CannotConnectToServer.class)
	public void givenProxyConnectionWarCannotBeRetrieve()
			throws Exception {
		doReturn(null).when(appManagementService).getStreamForURL(AppManagementService.URL_CHANGELOG);
		doReturn(true).when(appManagementService).isProxyPage(anyString());

		appManagementService.getWarFromServer(new ProgressInfo());
	}

	@Test(expected = AppManagementServiceRuntimeException.CannotConnectToServer.class)
	public void givenNoConnectionWarCannotBeRetrieve()
			throws Exception {
		doReturn(null).when(appManagementService).getStreamForURL(AppManagementService.URL_CHANGELOG);

		appManagementService.getWarFromServer(new ProgressInfo());
	}

	@Test
	public void givenConnectionWarCanBeRetrieve()
			throws Exception {
		InputStream tmp = getDummyInputStream();
		doReturn(tmp).when(appManagementService).getStreamForURL(AppManagementService.URL_WAR);

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

		uploadADummyUpdateJarWithVersion("5.0.4");

		appManagementService.update(new ProgressInfo());

	}

	@Test(expected = WarFileNotFound.class)
	public void givenWarIsNotUploadedThenCannotUpload()
			throws Exception {

		appManagementService.update(new ProgressInfo());

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

	private void uploadADummyUpdateJarWithVersion(String version)
			throws Exception {
		File warContentFolder = newTempFolder();
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
	}
}
