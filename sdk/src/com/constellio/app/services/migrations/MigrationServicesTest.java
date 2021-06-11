package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.Migration;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MigrationServicesTest extends ConstellioTest {

	@Mock ConstellioPluginManager pluginManager;
	@Mock ModelLayerFactory modelLayerFactory;
	@Mock DataLayerFactory dataLayerFactory;
	@Mock AppLayerFactory appLayerFactory;
	@Mock ConfigManager configManager;
	@Mock IOServicesFactory ioServicesFactory;
	@Mock IOServices ioServices;
	@Mock PropertiesConfiguration propertiesConfig;

	com.constellio.app.services.migrations.MigrationServices migrationServices;

	@Mock ConstellioEIM constellioEIM;
	@Mock MigrationScript migrationTo100;
	@Mock MigrationScript migration102To103;
	@Mock MigrationScript migration103To110;

	@Mock ConstellioModulesManagerImpl moduleManager;
	@Mock InstallableModule aModule;
	@Mock MigrationScript aModuleMigrationTo100;
	@Mock MigrationScript aModuleMigration101To102;
	@Mock MigrationScript aModuleMigration103To110;

	@Before
	public void setUp() {
		when(appLayerFactory.getModelLayerFactory()).thenReturn(modelLayerFactory);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);
		when(dataLayerFactory.getIOServicesFactory()).thenReturn(ioServicesFactory);
		when(ioServicesFactory.newIOServices()).thenReturn(ioServices);

		migrationServices = spy(
				new com.constellio.app.services.migrations.MigrationServices(constellioEIM, appLayerFactory, moduleManager,
						pluginManager));

		when(migrationTo100.getVersion()).thenReturn("1.0.0");
		when(migration102To103.getVersion()).thenReturn("1.0.3");
		when(migration103To110.getVersion()).thenReturn("1.1.0");
		when(constellioEIM.getMigrationScripts()).thenReturn(
				newArrayList(migrationTo100, migration102To103, migration103To110));

		when(aModuleMigrationTo100.getVersion()).thenReturn("1.0.0");
		when(aModuleMigration101To102.getVersion()).thenReturn("1.0.2");
		when(aModuleMigration103To110.getVersion()).thenReturn("1.1.0");

		when(aModule.getId()).thenReturn("aModuleId");
		when(aModule.getMigrationScripts()).thenReturn(
				newArrayList(aModuleMigrationTo100, aModuleMigration101To102, aModuleMigration103To110));

		when(moduleManager.getInstalledModules()).thenReturn(newArrayList(aModule));
	}

	//@Test
	public void whenMigratingFromPreviousVersionThenDoEachRequiredMigration()
			throws Exception {
		doNothing().when(migrationServices).setCurrentDataVersion(eq("zeCollection"), anyString());
		doNothing().when(migrationServices).markMigrationAsCompleted(isA(Migration.class));
		doReturn("0.9.9").when(migrationServices).getCurrentVersion("zeCollection");

		migrationServices.migrate(zeCollection, false);

		InOrder inOrder = Mockito.inOrder(migrationTo100, aModuleMigrationTo100, aModuleMigration101To102);
		inOrder.verify(migrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigration101To102)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
	}

	//@Test
	public void whenMigratingToAnotherMajorVersionFromPreviousVersionThenDoEachRequiredMigration()
			throws Exception {
		doNothing().when(migrationServices).setCurrentDataVersion(eq("zeCollection"), anyString());
		doNothing().when(migrationServices).markMigrationAsCompleted(isA(Migration.class));
		doReturn("0.9.9").when(migrationServices).getCurrentVersion("zeCollection");

		migrationServices.migrate(zeCollection, false);

		InOrder inOrder = Mockito.inOrder(migrationTo100, aModuleMigrationTo100, aModuleMigration101To102,
				migration102To103, migration103To110, aModuleMigration103To110);
		inOrder.verify(migrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigration101To102)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(migration102To103)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(migration103To110)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigration103To110)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
	}

	//@Test
	public void whenMigratingToPreviousVersionFromPreviousVersionThenDoNotMigrateToCurrentVersion()
			throws Exception {
		doNothing().when(migrationServices).setCurrentDataVersion(eq("zeCollection"), anyString());
		doNothing().when(migrationServices).markMigrationAsCompleted(isA(Migration.class));
		doReturn("0.9.9").when(migrationServices).getCurrentVersion("zeCollection");

		migrationServices.migrate(zeCollection, false);

		InOrder inOrder = Mockito.inOrder(
				migrationTo100, aModuleMigrationTo100, aModuleMigration101To102, migration102To103);
		inOrder.verify(migrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigration101To102)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(migration102To103)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
	}

	//@Test
	public void whenMigratingToCurrentVersionFromNewSystemThenDoEachRequiredMigration()
			throws Exception {
		doNothing().when(migrationServices).setCurrentDataVersion(eq("zeCollection"), anyString());
		doNothing().when(migrationServices).markMigrationAsCompleted(isA(Migration.class));
		doReturn(null).when(migrationServices).getCurrentVersion("zeCollection");

		migrationServices.migrate(zeCollection, false);

		InOrder inOrder = Mockito.inOrder(migrationTo100, aModuleMigrationTo100, aModuleMigration101To102);
		inOrder.verify(migrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigration101To102)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSetCurrentDataVersionThenPropertiesFileUpdated()
			throws Exception {
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);
		when(configManager.exist(anyString())).thenReturn(true);
		when(configManager.getProperties(anyString())).thenReturn(propertiesConfig);
		when(propertiesConfig.getProperties()).thenReturn(new HashMap<String, String>());

		migrationServices.setCurrentDataVersion("zeCollection", "1.0.0");

		verify(configManager).update(anyString(), anyString(), anyMap());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSetCurrentDataVersionButPropertiesFileNotExistsThenPropertiesFileAdded()
			throws Exception {
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);
		when(configManager.exist(anyString())).thenReturn(false);

		migrationServices.setCurrentDataVersion("zeCollection", "1.0.0");

		verify(configManager).add(anyString(), anyMap());
	}

	@Test
	public void givenNoConfigurationFileWhenGetCurrentVersionThenReturnNull()
			throws Exception {
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);
		when(configManager.getProperties("version.properties")).thenReturn(null);

		assertThat(migrationServices.getCurrentVersion("zeCollection")).isNull();
	}
}
