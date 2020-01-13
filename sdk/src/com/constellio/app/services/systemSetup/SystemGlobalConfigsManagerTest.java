package com.constellio.app.services.systemSetup;

import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SystemGlobalConfigsManagerTest extends ConstellioTest {

	@Mock DataLayerFactory dataLayerFactory;
	@Mock ConstellioPluginManager constellioPluginManager;
	@Mock ConfigManager configManager;

	@Mock PropertiesConfiguration propertiesConfiguration;

	SystemGlobalConfigsManager systemGlobalConfigsManager;
	@Mock SystemLocalConfigsManager systemLocalConfigsManager;

	@Before
	public void setUp()
			throws Exception {
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);
		systemGlobalConfigsManager = spy(new SystemGlobalConfigsManager(dataLayerFactory));

	}

	@Test
	public void whenInitializedThenCreateEmptyPropertyFilesIfInexistent()
			throws Exception {

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
		properties.put(SystemGlobalConfigsManager.MAIN_DATA_LANGUAGE, "fr");
		properties.put(SystemGlobalConfigsManager.TOKEN_DURATION,
				Integer.toString(SystemGlobalConfigsManager.TOKEN_DURATION_VALUE));
		properties.put(SystemGlobalConfigsManager.NOTIFICATION_MINUTES,
				Integer.toString(SystemGlobalConfigsManager.NOTIFICATION_MINUTES_VALUE));
		when(propertiesConfiguration.getProperties()).thenReturn(properties);

		systemGlobalConfigsManager.initialize();

		assertThat(systemGlobalConfigsManager.getMainDataLanguage()).isEqualTo("fr");
		assertThat(systemGlobalConfigsManager.getTokenDuration()).isEqualTo(SystemGlobalConfigsManager.TOKEN_DURATION_VALUE);
		assertThat(systemGlobalConfigsManager.getDelayBeforeSendingNotificationEmailsInMinutes()).isEqualTo(
				SystemGlobalConfigsManager.NOTIFICATION_MINUTES_VALUE);
	}

}
