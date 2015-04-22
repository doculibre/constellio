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
package com.constellio.app.services.systemSetup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.sdk.tests.ConstellioTest;

public class SystemGlobalConfigsManagerTest extends ConstellioTest {

	@Mock ConstellioPluginManager constellioPluginManager;
	@Mock SystemSetupService systemSetupService;
	@Mock ConfigManager configManager;

	@Mock PropertiesConfiguration propertiesConfiguration;

	SystemGlobalConfigsManager systemGlobalConfigsManager;

	@Before
	public void setUp()
			throws Exception {
		systemGlobalConfigsManager = spy(new SystemGlobalConfigsManager(configManager, systemSetupService));

	}

	@Test
	public void whenInitializedThenCreateEmptyPropertyFilesIfInexistent()
			throws Exception {

		doReturn(true).when(systemGlobalConfigsManager).isSystemSettedUp();

		systemGlobalConfigsManager.initialize();

		verify(configManager).createPropertiesDocumentIfInexistent(SystemGlobalConfigsManager.SYSTEM_GLOBAL_PROPERTIES,
				ConfigManager.EMPTY_PROPERTY_ALTERATION);

	}

	@Test
	public void givenSystemAlreadySettedUpThenDoNotSetupItAgainAndSettingsAvailable()
			throws Exception {
		when(configManager.getProperties(SystemGlobalConfigsManager.SYSTEM_GLOBAL_PROPERTIES))
				.thenReturn(propertiesConfiguration);
		Map<String, String> properties = new HashMap<>();
		properties.put(SystemGlobalConfigsManager.IS_SYSTEM_SETTED_UP, "true");
		properties.put(SystemGlobalConfigsManager.MAIN_DATA_LANGUAGE, "fr");
		properties.put(SystemGlobalConfigsManager.TOKEN_DURATION,
				Integer.toString(SystemGlobalConfigsManager.TOKEN_DURATION_VALUE));
		properties.put(SystemGlobalConfigsManager.NOTIFICATION_MINUTES,
				Integer.toString(SystemGlobalConfigsManager.NOTIFICATION_MINUTES_VALUE));
		when(propertiesConfiguration.getProperties()).thenReturn(properties);

		systemGlobalConfigsManager.initialize();

		verify(systemSetupService, never()).setup();
		assertThat(systemGlobalConfigsManager.getMainDataLanguage()).isEqualTo("fr");
		assertThat(systemGlobalConfigsManager.getTokenDuration()).isEqualTo(SystemGlobalConfigsManager.TOKEN_DURATION_VALUE);
		assertThat(systemGlobalConfigsManager.getDelayBeforeSendingNotificationEmailsInMinutes()).isEqualTo(
				SystemGlobalConfigsManager.NOTIFICATION_MINUTES_VALUE);
	}

	@Test
	public void givenSystemNotSettedUpThenSetupAndMarkAsSettedUp()
			throws Exception {

		when(configManager.getProperties(SystemGlobalConfigsManager.SYSTEM_GLOBAL_PROPERTIES))
				.thenReturn(propertiesConfiguration);
		when(propertiesConfiguration.getProperties()).thenReturn(new HashMap<String, String>());

		systemGlobalConfigsManager.initialize();

		verify(systemSetupService).setup();
		verify(configManager)
				.updateProperties(eq(SystemGlobalConfigsManager.SYSTEM_GLOBAL_PROPERTIES), any(PropertiesAlteration.class));
	}

}
