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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.sdk.tests.ConstellioTest;

public class WrapperConfigurationServiceAcceptanceTest extends ConstellioTest {

	private File configurationFile;

	@Before
	public void setup()
			throws IOException {
		File theProjectWrapperConf = getTestResourceFile("wrapper.conf.default");
		configurationFile = createTempCopy(theProjectWrapperConf);

	}

	@Test
	public void whenConfiguratingThenCorrectlyModifyingConfigs()
			throws IOException {

		WrapperConfigurationService service = new WrapperConfigurationService();
		service.configureForConstellio(configurationFile);

		String modifiedConfig = new FileService(null).readFileToString(configurationFile);

		TestCase.assertTrue(modifiedConfig.contains(WrapperConfigurationService.LOGFILE
				+ WrapperConfigurationService.ASSIGNING_VALUE + WrapperConfigurationService.LOGFILE_VALUE));
		TestCase.assertTrue(modifiedConfig.contains(WrapperConfigurationService.CONSOLE_TITLE
				+ WrapperConfigurationService.ASSIGNING_VALUE + WrapperConfigurationService.CONSOLE_TITLE_VALUE));
		TestCase.assertTrue(modifiedConfig.contains(WrapperConfigurationService.NTSERVICE_NAME
				+ WrapperConfigurationService.ASSIGNING_VALUE + WrapperConfigurationService.NTSERVICE_NAME_VALUE));
		TestCase.assertTrue(modifiedConfig.contains(WrapperConfigurationService.CONSOLE_TITLE
				+ WrapperConfigurationService.ASSIGNING_VALUE + WrapperConfigurationService.CONSOLE_TITLE_VALUE));
		TestCase.assertTrue(modifiedConfig.contains(WrapperConfigurationService.NTSERVICE_DISPLAYNAME
				+ WrapperConfigurationService.ASSIGNING_VALUE + WrapperConfigurationService.NTSERVICE_DISPLAYNAME_VALUE));
		TestCase.assertTrue(modifiedConfig.contains(WrapperConfigurationService.NTSERVICE_DESCRIPTION
				+ WrapperConfigurationService.ASSIGNING_VALUE + WrapperConfigurationService.NTSERVICE_DESCRIPTION_VALUE));
		TestCase.assertTrue(modifiedConfig.contains(WrapperConfigurationService.CONDITION_SCRIPT
				+ WrapperConfigurationService.ASSIGNING_VALUE + WrapperConfigurationService.CONDITION_SCRIPT_VALUE));
		TestCase.assertTrue(modifiedConfig.contains(WrapperConfigurationService.CONDITION_SCRIPT_ARGS
				+ WrapperConfigurationService.ASSIGNING_VALUE + WrapperConfigurationService.CONDITION_SCRIPT_ARGS_VALUE));
		TestCase.assertTrue(modifiedConfig.contains(WrapperConfigurationService.CONDITION_SCRIPT_CYCLE
				+ WrapperConfigurationService.ASSIGNING_VALUE + WrapperConfigurationService.CONDITION_SCRIPT_CYCLE_VALUE));
	}

}
