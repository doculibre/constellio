package com.constellio.app.services.extensions.plugins;

import static com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManager.PLUGINS_CONFIG_PATH;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.DISABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.ENABLED;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.INVALID;
import static com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginStatus.READY_TO_INSTALL;
import static com.constellio.app.services.extensions.plugins.PluginActivationFailureCause.CANNOT_INSTALL_OLDER_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManagerRuntimeException.ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableInvalidPlugin;
import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManagerRuntimeException.ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableReadyToInstallPlugin;
import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManagerRuntimeException.ConstellioPluginConfigurationManagerRuntimeException_CouldNotEnableInvalidPlugin;
import com.constellio.app.services.extensions.plugins.ConstellioPluginConfigurationManagerRuntimeException.ConstellioPluginConfigurationManagerRuntimeException_InvalidPluginWithNoStatus;
import com.constellio.app.services.extensions.plugins.pluginInfo.ConstellioPluginInfo;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class ConstellioPluginConfigurationManagerAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConstellioPluginConfigurationManager pluginConfigManger;
	private ConfigManager configManager;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
		);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		configManager = getModelLayerFactory().getDataLayerFactory().getConfigManager();
		pluginConfigManger = new ConstellioPluginConfigurationManager(configManager);
	}

	@Test
	public void assertThatPluginFileExists()
			throws Exception {
		assertThat(configManager.exist(PLUGINS_CONFIG_PATH)).isTrue();
	}

	@Test
	public void givenConfigFileExistsWhenCreateConfigFileIfNotExistThenNotReplaced()
			throws Exception {
		configManager.updateXML(PLUGINS_CONFIG_PATH, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				document.setRootElement(new Element("zeRoot"));
			}
		});
		pluginConfigManger.createConfigFileIfNotExist();
		XMLConfiguration text = configManager.getXML(PLUGINS_CONFIG_PATH);
		assertThat(text.getDocument().getRootElement().getName()).isEqualTo("zeRoot");
	}

	@Test
	public void givenPluginInfoWithNullValuesWhenAddPluginAndGetItThenPluginInfoLoadedCorrectly()
			throws Exception {
		ConstellioPluginInfo expectedPluginInfo = new ConstellioPluginInfo().setCode("zeCode");
		pluginConfigManger.addOrUpdatePlugin(expectedPluginInfo);
		ConstellioPluginInfo pluginInfo = pluginConfigManger.getPluginInfo("zeCode");
		assertThat(pluginInfo).isEqualToComparingFieldByField(expectedPluginInfo);
	}

	@Test
	public void givenPluginInfoWithValidValuesWhenAddPluginAndGetItThenPluginInfoLoadedCorrectly()
			throws Exception {
		ConstellioPluginInfo expectedPluginInfo = new ConstellioPluginInfo()
				.setCode("zeCode")
				.setPluginActivationFailureCause(CANNOT_INSTALL_OLDER_VERSION)
				.setPluginStatus(INVALID)
				.setLastInstallDate(new LocalDate())
				.setRequiredConstellioVersion("5.1")
				.setVersion("9.0.1");
		pluginConfigManger.addOrUpdatePlugin(expectedPluginInfo);
		ConstellioPluginInfo pluginInfo = pluginConfigManger.getPluginInfo("zeCode");
		assertThat(pluginInfo).isEqualToComparingFieldByField(expectedPluginInfo);
	}

	@Test
	public void whenInstallPluginThenInstalledWithCurrentDateAndWithoutErrorsAndWithWriteStatus()
			throws Exception {
		LocalDate now = LocalDate.now();
		givenTimeIs(now);
		pluginConfigManger.installPlugin("zePluginId", "version", "rVersion");
		ConstellioPluginInfo pluginInfo = pluginConfigManger.getPluginInfo("zePluginId");
		assertThat(pluginInfo.getLastInstallDate()).isEqualTo(now);
		assertThat(pluginInfo.getVersion()).isEqualTo("version");
		assertThat(pluginInfo.getRequiredConstellioVersion()).isEqualTo("rVersion");
		assertThat(pluginInfo.getPluginStatus()).isEqualTo(READY_TO_INSTALL);
		assertThat(pluginInfo.getPluginActivationFailureCause()).isNull();
	}

	@Test
	public void givenInstalledPluginWhenInstallPluginThenUpdatedCorrectly()
			throws Exception {
		//first install
		LocalDate now = LocalDate.now();
		givenTimeIs(now);
		pluginConfigManger.installPlugin("zePluginId", "version", "rVersion");
		//update of status
		pluginConfigManger.markPluginAsEnabled("zePluginId");

		//second install
		LocalDate after = now.plusDays(1);
		givenTimeIs(after);
		pluginConfigManger.installPlugin("zePluginId", "version2", "rVersion2");

		ConstellioPluginInfo pluginInfo = pluginConfigManger.getPluginInfo("zePluginId");
		assertThat(pluginInfo.getLastInstallDate()).isEqualTo(after);
		assertThat(pluginInfo.getVersion()).isEqualTo("version2");
		assertThat(pluginInfo.getRequiredConstellioVersion()).isEqualTo("rVersion2");
		assertThat(pluginInfo.getPluginStatus()).isEqualTo(READY_TO_INSTALL);
		assertThat(pluginInfo.getPluginActivationFailureCause()).isNull();
	}

	@Test
	public void givenPluginInfoWhenAddPluginAndGetItThenPluginInfoLoadedCorrectly()
			throws Exception {
		ConstellioPluginInfo expectedPluginInfo = new ConstellioPluginInfo()
				.setCode("zeCode")
				.setPluginActivationFailureCause(CANNOT_INSTALL_OLDER_VERSION)
				.setPluginStatus(INVALID)
				.setLastInstallDate(new LocalDate())
				.setRequiredConstellioVersion("5.1")
				.setVersion("9.0.1");
		pluginConfigManger.addOrUpdatePlugin(expectedPluginInfo);
		ConstellioPluginInfo pluginInfo = pluginConfigManger.getPluginInfo("zeCode");
		assertThat(pluginInfo).isEqualToComparingFieldByField(expectedPluginInfo);
	}

	ConstellioPluginInfo pluginWithDisabledStatus = new ConstellioPluginInfo().setCode("pluginWithDisabledStatus")
			.setPluginStatus(DISABLED),
			pluginWithEnabledStatus = new ConstellioPluginInfo().setCode("pluginWithEnabledStatus").setPluginStatus(ENABLED),
			pluginWithReadyToInstallStatus = new ConstellioPluginInfo().setCode("pluginWithReadyToInstallStatus")
					.setPluginStatus(READY_TO_INSTALL),
			pluginWithInvalidStatus = new ConstellioPluginInfo().setCode("pluginWithInvalidStatus").setPluginStatus(INVALID),
			pluginWithNullStatus = new ConstellioPluginInfo().setCode("pluginWithNullStatus");

	@Test
	public void whenSaveSeveralPluginsThenSavedCorrectly()
			throws Exception {
		saveTestPlugins();
		ConstellioPluginInfo reLoadedPluginWithDisabledStatus = pluginConfigManger
				.getPluginInfo(pluginWithDisabledStatus.getCode());
		assertThat(reLoadedPluginWithDisabledStatus).isEqualToComparingFieldByField(pluginWithDisabledStatus);

		ConstellioPluginInfo reLoadedPluginWithEnabledStatus = pluginConfigManger
				.getPluginInfo(pluginWithEnabledStatus.getCode());
		assertThat(reLoadedPluginWithEnabledStatus).isEqualToComparingFieldByField(pluginWithEnabledStatus);

		ConstellioPluginInfo reLoadedPluginWithReadyToInstallStatus = pluginConfigManger
				.getPluginInfo(pluginWithReadyToInstallStatus.getCode());
		assertThat(reLoadedPluginWithReadyToInstallStatus).isEqualToComparingFieldByField(pluginWithReadyToInstallStatus);

		ConstellioPluginInfo reLoadedPluginWithInvalidStatus = pluginConfigManger
				.getPluginInfo(pluginWithInvalidStatus.getCode());
		assertThat(reLoadedPluginWithInvalidStatus).isEqualToComparingFieldByField(pluginWithInvalidStatus);

		ConstellioPluginInfo reLoadedPluginWithNullStatus = pluginConfigManger
				.getPluginInfo(pluginWithNullStatus.getCode());
		assertThat(reLoadedPluginWithNullStatus).isEqualToComparingFieldByField(pluginWithNullStatus);
	}

	@Test
	public void givenExistingPluginsWhenUpdatePluginThenUpdatedCorrectly()
			throws Exception {
		saveTestPlugins();
		pluginWithDisabledStatus = pluginWithDisabledStatus.setPluginStatus(ENABLED)
				.setPluginActivationFailureCause(CANNOT_INSTALL_OLDER_VERSION)
				.setLastInstallDate(LocalDate.now())
				.setRequiredConstellioVersion("dd")
				.setVersion("aa");
		pluginConfigManger.addOrUpdatePlugin(pluginWithDisabledStatus);
		ConstellioPluginInfo reLoadedPluginWithDisabledStatus = pluginConfigManger
				.getPluginInfo(pluginWithDisabledStatus.getCode());
		assertThat(reLoadedPluginWithDisabledStatus).isEqualToComparingFieldByField(pluginWithDisabledStatus);
	}

	@Test
	public void whenMarkPluginAsEnabledThenBehavesAsExpected()
			throws Exception {
		saveTestPlugins();
		try {
			pluginConfigManger.markPluginAsEnabled(pluginWithNullStatus.getCode());
		} catch (ConstellioPluginConfigurationManagerRuntimeException_InvalidPluginWithNoStatus e) {
			//OK
		}
		try {
			pluginConfigManger.markPluginAsEnabled(pluginWithInvalidStatus.getCode());
		} catch (ConstellioPluginConfigurationManagerRuntimeException_CouldNotEnableInvalidPlugin e) {
			//OK
		}
		pluginConfigManger.markPluginAsEnabled(pluginWithDisabledStatus.getCode());
		assertThat(pluginConfigManger.getPluginInfo(pluginWithDisabledStatus.getCode()).getPluginStatus()).isEqualTo(ENABLED);
		pluginConfigManger.markPluginAsEnabled(pluginWithEnabledStatus.getCode());
		assertThat(pluginConfigManger.getPluginInfo(pluginWithEnabledStatus.getCode()).getPluginStatus()).isEqualTo(ENABLED);
		pluginConfigManger.markPluginAsEnabled(pluginWithReadyToInstallStatus.getCode());
		assertThat(pluginConfigManger.getPluginInfo(pluginWithReadyToInstallStatus.getCode()).getPluginStatus())
				.isEqualTo(ENABLED);
	}

	@Test
	public void whenMarkPluginAsDisabledThenBehavesAsExpected()
			throws Exception {
		saveTestPlugins();
		try {
			pluginConfigManger.markPluginAsDisabled(pluginWithNullStatus.getCode());
		} catch (ConstellioPluginConfigurationManagerRuntimeException_InvalidPluginWithNoStatus e) {
			//OK
		}
		try {
			pluginConfigManger.markPluginAsDisabled(pluginWithInvalidStatus.getCode());
		} catch (ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableInvalidPlugin e) {
			//OK
		}
		try {
			pluginConfigManger.markPluginAsDisabled(pluginWithReadyToInstallStatus.getCode());
		} catch (ConstellioPluginConfigurationManagerRuntimeException_CouldNotDisableReadyToInstallPlugin e) {
			//OK
		}

		pluginConfigManger.markPluginAsDisabled(pluginWithDisabledStatus.getCode());
		assertThat(pluginConfigManger.getPluginInfo(pluginWithDisabledStatus.getCode()).getPluginStatus()).isEqualTo(DISABLED);
		pluginConfigManger.markPluginAsDisabled(pluginWithEnabledStatus.getCode());
		assertThat(pluginConfigManger.getPluginInfo(pluginWithEnabledStatus.getCode()).getPluginStatus()).isEqualTo(DISABLED);
	}

	private void saveTestPlugins() {
		pluginConfigManger.addOrUpdatePlugin(pluginWithDisabledStatus);
		pluginConfigManger.addOrUpdatePlugin(pluginWithEnabledStatus);
		pluginConfigManger.addOrUpdatePlugin(pluginWithReadyToInstallStatus);
		pluginConfigManger.addOrUpdatePlugin(pluginWithInvalidStatus);
		pluginConfigManger.addOrUpdatePlugin(pluginWithNullStatus);
	}
}
