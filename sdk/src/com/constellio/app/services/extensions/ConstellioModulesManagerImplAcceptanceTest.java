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
package com.constellio.app.services.extensions;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.ConstellioPluginManagerRuntimeException.ConstellioPluginManagerRuntimeException_NoSuchModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.MigrationServices;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.utils.Delayed;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.sdk.tests.ConstellioTest;

public class ConstellioModulesManagerImplAcceptanceTest extends ConstellioTest {

	CollectionsManager collectionsManager;
	ConstellioPluginManager pluginManager;
	CollectionsListManager collectionsListManager;
	MigrationServices migrationServices;

	@Mock MigrationScript moduleAMigrationScript111;
	@Mock MigrationScript moduleAMigrationScript112;
	@Mock MigrationScript moduleBMigrationScript111;
	@Mock MigrationScript moduleBMigrationScript112;
	@Mock MigrationScript moduleCMigrationScript111;
	@Mock MigrationScript moduleCMigrationScript112;

	@Mock InstallableModule moduleA;
	@Mock InstallableModule moduleB;

	@Mock InstallableModule moduleC;

	ConstellioModulesManagerImpl manager;

	InOrder inOrder;

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(ConfigManager.class, ConstellioModulesManager.class, ConstellioPluginManager.class);

		pluginManager = getAppLayerFactory().getPluginManager();
		doReturn(Arrays.asList(moduleA, moduleB)).when(pluginManager).getPlugins(InstallableModule.class);

		collectionsManager = getAppLayerFactory().getCollectionsManager();
		manager = (ConstellioModulesManagerImpl) getAppLayerFactory().getModulesManager();
		migrationServices = getAppLayerFactory().newMigrationServices();
		collectionsListManager = getModelLayerFactory().getCollectionsListManager();

		when(moduleAMigrationScript111.getVersion()).thenReturn("11.1.1");
		when(moduleAMigrationScript112.getVersion()).thenReturn("11.1.2");
		when(moduleBMigrationScript111.getVersion()).thenReturn("11.1.1");
		when(moduleBMigrationScript112.getVersion()).thenReturn("11.1.2");
		when(moduleCMigrationScript111.getVersion()).thenReturn("11.1.1");
		when(moduleCMigrationScript112.getVersion()).thenReturn("11.1.2");

		when(moduleA.getId()).thenReturn("moduleA_Id");
		when(moduleA.getName()).thenReturn("moduleA");
		when(moduleA.getMigrationScripts()).thenReturn(Arrays.asList(moduleAMigrationScript111, moduleAMigrationScript112));
		when(moduleB.getId()).thenReturn("moduleB_Id");
		when(moduleB.getName()).thenReturn("moduleA");
		when(moduleB.getMigrationScripts()).thenReturn(Arrays.asList(moduleBMigrationScript111, moduleBMigrationScript112));
		when(moduleC.getId()).thenReturn("moduleC_Id");
		when(moduleC.getName()).thenReturn("moduleC");
		when(moduleC.getMigrationScripts()).thenReturn(Arrays.asList(moduleCMigrationScript111, moduleCMigrationScript112));

		inOrder = inOrder(moduleAMigrationScript111, moduleAMigrationScript112, moduleBMigrationScript111,
				moduleBMigrationScript112, moduleCMigrationScript111, moduleCMigrationScript112);

	}

	//TODO Fail on initialize if invalid modules

	@Test
	public void whenGetModulesByIdThenReturnCorrectModules() {
		assertThat(manager.getInstalledModule("moduleA_Id")).isEqualTo(moduleA);
		assertThat(manager.getInstalledModule("moduleB_Id")).isEqualTo(moduleB);
	}

	@Test(expected = ConstellioPluginManagerRuntimeException_NoSuchModule.class)
	public void whenGetModuleWithInvalidIdThenThrowException() {
		manager.getInstalledModule("unknownModuleId");
	}

	@Test
	public void whenInitializingThenLoaded()
			throws Exception {
		//Deoing some changes
		collectionsManager
				.createCollectionInCurrentVersion("collection1", Arrays.asList("fr"));
		collectionsManager
				.createCollectionInCurrentVersion("collection2", Arrays.asList("fr"));
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");
		manager.installModule(moduleA, collectionsListManager);
		manager.installModule(moduleB, collectionsListManager);

		manager.enableModule("collection1", moduleA);
		manager.enableModule("collection1", moduleB);
		manager.enableModule("collection2", moduleB);
		assertThat(manager.getEnabledModules("collection1")).containsOnly(moduleA, moduleB);
		assertThat(manager.getEnabledModules("collection2")).containsOnly(moduleB);

		ConstellioModulesManagerImpl otherManager = new ConstellioModulesManagerImpl(getAppLayerFactory(),
				pluginManager, new Delayed<>(migrationServices));
		otherManager.initialize();

		assertThat(otherManager.getEnabledModules("collection1")).containsOnly(moduleA, moduleB);
		assertThat(otherManager.getEnabledModules("collection2")).containsOnly(moduleB);

	}

	@Test
	public void givenMultipleCollectionsThenEnableStatusIsHandledSeparately()
			throws Exception {
		collectionsManager
				.createCollectionInCurrentVersion("collection1", Arrays.asList("fr"));
		collectionsManager
				.createCollectionInCurrentVersion("collection2", Arrays.asList("fr"));
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");

		manager.installModule(moduleA, collectionsListManager);
		manager.installModule(moduleB, collectionsListManager);
		assertThat(manager.getEnabledModules("collection1")).isEmpty();
		assertThat(manager.getEnabledModules("collection2")).isEmpty();

		manager.enableModule("collection1", moduleA);
		manager.enableModule("collection1", moduleB);
		manager.enableModule("collection2", moduleB);
		assertThat(manager.getEnabledModules("collection1")).containsOnly(moduleA, moduleB);
		assertThat(manager.getEnabledModules("collection2")).containsOnly(moduleB);

		manager.disableModule("collection1", moduleA);
		assertThat(manager.getEnabledModules("collection1")).containsOnly(moduleB);
		assertThat(manager.getEnabledModules("collection2")).containsOnly(moduleB);
	}

	@Test
	public void givenFreshInstallationThenNoModuleEnabledAndAllAvailables()
			throws Exception {
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");
		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleA, moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).isEmpty();
		assertThat(manager.getDisabledModules(zeCollection)).isEmpty();
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();
	}

	@Test
	public void whenInstallModuleThenMigrationScriptsRunedAndAppearAsDisabled()
			throws Exception {
		givenCollection("zeCollection");
		givenCollection("anotherCollection");
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");
		migrationServices.setCurrentDataVersion("anotherCollection", "11.1.2");

		manager.installModule(moduleA, collectionsListManager);

		verify(moduleAMigrationScript111).migrate(eq("zeCollection"), any(MigrationResourcesProvider.class),
				any(AppLayerFactory.class));
		verify(moduleAMigrationScript112).migrate(eq("zeCollection"), any(MigrationResourcesProvider.class),
				any(AppLayerFactory.class));
		verify(moduleAMigrationScript111).migrate(eq("anotherCollection"), any(MigrationResourcesProvider.class),
				any(AppLayerFactory.class));
		verify(moduleAMigrationScript112).migrate(eq("anotherCollection"), any(MigrationResourcesProvider.class),
				any(AppLayerFactory.class));
		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).isEmpty();
		assertThat(manager.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();
	}

	@Test
	public void whenInstallModuleWithDependenciesThenInstallDependencies()
			throws Exception {
		doReturn(Arrays.asList(moduleA, moduleB, moduleC)).when(pluginManager).getPlugins(InstallableModule.class);
		when(moduleC.getDependencies()).thenReturn(asList("moduleB_Id"));
		givenCollection("zeCollection");
		givenCollection("anotherCollection");
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");
		migrationServices.setCurrentDataVersion("anotherCollection", "11.1.2");

		manager.installModule(moduleC, collectionsListManager);

		inOrder.verify(moduleBMigrationScript111)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		inOrder.verify(moduleCMigrationScript111)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		inOrder.verify(moduleBMigrationScript112)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		inOrder.verify(moduleCMigrationScript112)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
	}

	@Test
	public void whenInstallDependentModuleBeforeModuleWithDependenciesThenOnlyInstalledOnce()
			throws Exception {
		doReturn(Arrays.asList(moduleA, moduleB, moduleC)).when(pluginManager).getPlugins(InstallableModule.class);
		when(moduleC.getDependencies()).thenReturn(asList("moduleB_Id"));
		givenCollection("zeCollection");
		givenCollection("anotherCollection");
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");
		migrationServices.setCurrentDataVersion("anotherCollection", "11.1.2");

		manager.installModule(moduleB, collectionsListManager);
		manager.installModule(moduleC, collectionsListManager);

		inOrder.verify(moduleBMigrationScript111)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		inOrder.verify(moduleBMigrationScript112)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		inOrder.verify(moduleCMigrationScript111)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		inOrder.verify(moduleCMigrationScript112)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
	}

	@Test
	public void whenInstallModuleThenMigrationScriptsRunedToCurrentVersionAndAppearAsDisabled()
			throws Exception {
		givenCollection("zeCollection");
		givenCollection("anotherCollection");
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.1");
		migrationServices.setCurrentDataVersion("anotherCollection", "11.1.1");

		manager.installModule(moduleA, collectionsListManager);

		inOrder.verify(moduleAMigrationScript111)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		inOrder.verify(moduleAMigrationScript111)
				.migrate(eq("anotherCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		verify(moduleAMigrationScript112)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		verify(moduleAMigrationScript112)
				.migrate(eq("anotherCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).isEmpty();
		assertThat(manager.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();
	}

	@Test
	public void whenInstallModuleOnNewSystemThenNoMigrationScriptsRunedAndAppearAsEnabled()
			throws Exception {

		manager.installModule(moduleA, collectionsListManager);

		inOrder.verify(moduleAMigrationScript111, never())
				.migrate(anyString(), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		inOrder.verify(moduleAMigrationScript112, never())
				.migrate(anyString(), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).isEmpty();
		assertThat(manager.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();
	}

	@Test
	public void whenEnablingAModuleThenAppearAsEnabled()
			throws Exception {
		migrationServices.setCurrentDataVersion("zeCollection", "1.1.2");

		manager.installModule(moduleA, collectionsListManager);
		manager.enableModule(zeCollection, moduleA);

		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.getDisabledModules(zeCollection)).isEmpty();
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isTrue();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();

	}

	@Test
	public void whenDisablingAModuleThenAppearAsDisabled()
			throws Exception {
		migrationServices.setCurrentDataVersion("zeCollection", "1.1.2");

		manager.installModule(moduleA, collectionsListManager);
		manager.enableModule(zeCollection, moduleA);
		manager.disableModule(zeCollection, moduleA);

		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).isEmpty();
		assertThat(manager.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();

	}

	@Test
	public void whenReenablingAModuleThenAppearAsEnabled()
			throws Exception {
		//givenCollectionInVersion("zeCollection", Arrays.asList(Language.French.getAuthId()), "0.0.1");
		//givenCollectionInVersion("anotherCollection", Arrays.asList(Language.French.getAuthId()), "0.0.1");
		givenCollection("zeCollection");
		givenCollection("anotherCollection");
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");
		migrationServices.setCurrentDataVersion("anotherCollection", "11.1.2");

		manager.installModule(moduleA, collectionsListManager);
		manager.enableModule(zeCollection, moduleA);
		manager.disableModule(zeCollection, moduleA);
		manager.enableModule(zeCollection, moduleA);

		verify(moduleAMigrationScript111).migrate(
				eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		verify(moduleAMigrationScript112)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		verify(moduleAMigrationScript111)
				.migrate(eq("anotherCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		verify(moduleAMigrationScript112)
				.migrate(eq("anotherCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.getDisabledModules(zeCollection)).isEmpty();
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isTrue();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();

	}

	@Test
	public void givenModuleAEnabledThenStartedWhenStartCalled()
			throws Exception {
		migrationServices.setCurrentDataVersion("zeCollection", "1.1.2");

		manager.installModule(moduleA, collectionsListManager);
		manager.enableModule(zeCollection, moduleA);

		verify(moduleA, times(1)).start(eq(zeCollection), any(AppLayerFactory.class));
		verify(moduleB, never()).start(eq(zeCollection), any(AppLayerFactory.class));

	}

	@Test
	public void givenModuleAEnabledThenStopedWhenStopCalled()
			throws Exception {
		migrationServices.setCurrentDataVersion("zeCollection", "1.1.2");

		manager.installModule(moduleA, collectionsListManager);
		manager.enableModule(zeCollection, moduleA);

		manager.stopModules(zeCollection);

		verify(moduleA, times(1)).stop(eq(zeCollection), any(AppLayerFactory.class));
		verify(moduleB, never()).stop(eq(zeCollection), any(AppLayerFactory.class));

	}

	@Test
	public void givenModulewhenGetPermissionGroupsThenReturnCoreAndModulePermissions()
			throws Exception {
		givenCollection(zeCollection);
		givenCollection("anotherCollection").withConstellioRMModule();

		assertThat(manager.getPermissionGroups(zeCollection))
				.has(permissionGroupStartingWith("core."))
				.doesNotHave(permissionGroupStartingWith("rm."));

		assertThat(manager.getPermissionGroups("anotherCollection"))
				.has(permissionGroupStartingWith("core."))
				.has(permissionGroupStartingWith("rm."));

		assertThat(manager.getPermissionsInGroup("anotherCollection", "core.management.collection"))
				.contains("core.manageTaxonomies");

		assertThat(manager.getPermissionsInGroup("anotherCollection", "rm.folders"))
				.contains("rm.shareFolders");

	}

	private Condition<? super List<String>> permissionGroupStartingWith(final String text) {
		return new Condition<List<String>>() {
			@Override
			public boolean matches(List<String> groups) {
				for (String group : groups) {
					if (group.startsWith(text)) {
						return true;
					}
				}
				return false;
			}
		};
	}
}
