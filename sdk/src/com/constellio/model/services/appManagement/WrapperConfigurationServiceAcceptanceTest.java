package com.constellio.model.services.appManagement;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.sdk.tests.ConstellioTest;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
