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
package com.constellio.model.services.appManagement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.constellio.sdk.tests.ConstellioTest;

public class InstallationServiceTest extends ConstellioTest {

	private File constellioInstallationDir = aFile();

	private File configFile;

	private PlatformService platformService;

	private WrapperConfigurationService wrapperConfigurationService;

	private InstallationService service;

	@Test
	public void givenConfigurationFileGeneratedWhenWaitingUntilGeneratedThenStopWaiting()
			throws Exception {
		when(service.getConfigFile()).thenReturn(configFile);

		doReturn(0L).doReturn(1L).when(configFile).length();

		try {
			service.waitUntilConfigurationFileGenerated();
		} finally {
			verify(configFile, times(2)).length();
			verify(platformService, times(1)).sleepWithoutExpectableException(
					InstallationService.CONFIG_FILE_COMPLETION_WAIT_TIME);
		}
	}

	@Test(expected = RuntimeException.class)
	public void givenConfigurationFileNeverGeneratedWhenWaitingUntilGeneratedThenThrowExceptionAfterMaxWaitTime()
			throws Exception {
		when(service.getConfigFile()).thenReturn(configFile);

		int numberOfWhileLoop = (int) Math.ceil((double) InstallationService.CONFIG_FILE_COMPLETION_MAX_WAIT_TIME
				/ (double) InstallationService.CONFIG_FILE_COMPLETION_WAIT_TIME);

		when(configFile.length()).thenReturn(0L);

		try {
			service.waitUntilConfigurationFileGenerated();

		} finally {
			verify(configFile, times(numberOfWhileLoop)).length();
			verify(platformService, times(numberOfWhileLoop)).sleepWithoutExpectableException(
					InstallationService.CONFIG_FILE_COMPLETION_WAIT_TIME);
		}

	}

	@Test
	public void givenFileExistwhenWaitingUntilConfigurationFileGeneratedThenDoNotWait()
			throws Exception {
		when(service.getConfigFile()).thenReturn(configFile);

		when(configFile.length()).thenReturn(1L);

		service.waitUntilConfigurationFileGenerated();

		verify(configFile, times(1)).length();
		verify(platformService, times(0)).sleepWithoutExpectableException(anyInt());

	}

	@Before
	public void setup() {
		platformService = mock(PlatformService.class);
		wrapperConfigurationService = mock(WrapperConfigurationService.class);
		service = spy(new InstallationService(constellioInstallationDir, platformService, wrapperConfigurationService));
		configFile = mock(File.class);
	}

	@Test
	public void whenAskingConfigFileThenGettingCorrectFile() {
		File testConfigFile = new File(constellioInstallationDir, "conf" + File.separator + "wrapper.conf");
		File realConfigFile = service.getConfigFile();

		assertEquals(testConfigFile.getPath(), realConfigFile.getPath());
	}

	@Test
	public void whenAskingConfigGeneratorBatScriptThenGettingCorrectFile() {
		File testGenConfig = new File(constellioInstallationDir, "bat" + File.separator + "genConfig.bat");
		File genConfig = service.getConfigGeneratorBatScript();

		assertEquals(testGenConfig.getPath(), genConfig.getPath());
	}

	@Test
	public void whenAskingConfigGeneratorSHScriptThenGettingCorrectFile() {
		File testGenConfig = new File(constellioInstallationDir, "bin" + File.separator + "genConfig.sh");
		File genConfig = service.getConfigGeneratorSHScript();

		assertEquals(testGenConfig.getPath(), genConfig.getPath());
	}

	@Test
	public void whenCreatingFoldersThenCreateLogFolderAndCommandFolder() {
		File logFolder = mock(File.class);
		File commandFolder = mock(File.class);

		doReturn(logFolder).when(service).getLogFolder();
		doReturn(commandFolder).when(service).getCommandFolder();

		service.createDefaultFolders();

		verify(logFolder).mkdirs();
		verify(commandFolder).mkdirs();
	}

	@Test
	public void whenExecutingBatConfGeneratorScriptThenRunTheScriptAndWaitUntilConfFileGenerated()
			throws IOException, InterruptedException {
		String theProcessID = aString();
		File theConfigGeneratorScript = aFile();

		doReturn(theConfigGeneratorScript).when(service).getConfigGeneratorBatScript();
		doNothing().when(service).waitUntilConfigurationFileGenerated();
		service.executeWindowsInstallScript(theProcessID);

		verify(platformService, times(1)).runBatchScriptWithParameters(Matchers.eq(theConfigGeneratorScript),
				Matchers.eq(theProcessID));
		verify(service, times(1)).waitUntilConfigurationFileGenerated();
	}

	@Test
	public void whenExecutingInstallScriptOnLinuxThenExecuteSHScriptWithProcessID()
			throws Exception {

		String theProcessID = aString();

		doNothing().when(service).executeLinuxInstallScript(theProcessID);
		when(platformService.getProcessID()).thenReturn(theProcessID);
		when(platformService.isWindows()).thenReturn(false);

		service.executeInstallScript();

		verify(service, times(1)).executeLinuxInstallScript(theProcessID);
	}

	@Test
	public void whenExecutingInstallScriptOnWindowsThenExecuteBatchScriptWithProcessID()
			throws Exception {

		String theProcessID = aString();

		doNothing().when(service).executeWindowsInstallScript(theProcessID);
		when(platformService.getProcessID()).thenReturn(theProcessID);
		when(platformService.isWindows()).thenReturn(true);

		service.executeInstallScript();

		verify(service, times(1)).executeWindowsInstallScript(theProcessID);
	}

	@Test
	public void whenExecutingSHConfGeneratorScriptThenRunTheScriptAndWaitUntilConfFileGenerated()
			throws IOException, InterruptedException {

		String theProcessID = aString();
		File theConfigGeneratorScript = aFile();

		doReturn(theConfigGeneratorScript).when(service).getConfigGeneratorSHScript();
		doNothing().when(service).waitUntilConfigurationFileGenerated();
		service.executeLinuxInstallScript(theProcessID);

		verify(platformService, times(1)).runSHScriptWithParameters(Matchers.eq(theConfigGeneratorScript),
				Matchers.eq(theProcessID));
		verify(service, times(1)).waitUntilConfigurationFileGenerated();
	}

	@Test
	public void whenGeneratingConfigurationFileThenExecuteInstallScriptAndConfigureForConstellio()
			throws Exception {

		doNothing().when(service).executeInstallScript();

		service.generateConfigurationFile();

		verify(service, times(1)).executeInstallScript();
		verify(wrapperConfigurationService, times(1)).configureForConstellio(service.getConfigFile());
	}

	@Test
	public void whenGettingLogFolderThenReturnLogFolderInInstallationDir() {
		assertEquals(new File(constellioInstallationDir, "log"), service.getLogFolder());
	}

	@Test
	public void whenLaunchingInstallationThenGenerateConfigurationFileAndCreateFolders()
			throws Exception {

		doNothing().when(service).generateConfigurationFile();
		doNothing().when(service).createDefaultFolders();

		service.launchInstallation();

		verify(service, times(1)).generateConfigurationFile();
		verify(service, times(1)).createDefaultFolders();
	}

}
