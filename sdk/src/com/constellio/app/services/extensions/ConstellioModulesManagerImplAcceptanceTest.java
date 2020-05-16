package com.constellio.app.services.extensions;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManagerRuntimeException.ConstellioPluginManagerRuntimeException_NoSuchModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.MigrationServices;
import com.constellio.data.utils.Delayed;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static java.util.Arrays.asList;
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

// Confirm @SlowTest
public class ConstellioModulesManagerImplAcceptanceTest extends ConstellioTest {
	CollectionsManager collectionsManager;
	ConstellioPluginManager pluginManager, pluginManagerOfAnotherInstance;
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

	@Mock InstallableModule complementaryModuleAB;

	@Mock InstallableModule complementaryPluginDependentOfModuleAB;
	@Mock InstallableModule complementaryPluginWithoutDependencies;

	@Mock InstallableModule pluginDependentOfModuleAB;
	@Mock InstallableModule pluginWithoutDependencies;

	ConstellioModulesManagerImpl manager, managerOfAnotherInstance;

	InOrder inOrder;

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(ConstellioPluginManager.class);

		pluginManager = getAppLayerFactory().getPluginManager();
		pluginManagerOfAnotherInstance = getAppLayerFactory("other-instance").getPluginManager();
		linkEventBus(getModelLayerFactory(), getModelLayerFactory("other-instance"));

		doReturn(Arrays.asList(moduleA, moduleB, complementaryModuleAB, complementaryPluginDependentOfModuleAB,
				complementaryPluginWithoutDependencies,
				pluginDependentOfModuleAB, pluginWithoutDependencies)).when(pluginManager).getRegistredModulesAndActivePlugins();

		doReturn(Arrays.asList(moduleA, moduleB, complementaryModuleAB)).when(pluginManager).getRegisteredModules();

		doReturn(Arrays.asList(complementaryPluginDependentOfModuleAB,
				complementaryPluginWithoutDependencies,
				pluginDependentOfModuleAB, pluginWithoutDependencies)).when(pluginManager).getActivePluginModules();

		doReturn(Arrays.asList(moduleA, moduleB, complementaryModuleAB, complementaryPluginDependentOfModuleAB,
				complementaryPluginWithoutDependencies,
				pluginDependentOfModuleAB, pluginWithoutDependencies)).when(pluginManagerOfAnotherInstance)
				.getRegistredModulesAndActivePlugins();

		doReturn(Arrays.asList(moduleA, moduleB, complementaryModuleAB)).when(pluginManagerOfAnotherInstance)
				.getRegisteredModules();

		doReturn(Arrays.asList(complementaryPluginDependentOfModuleAB,
				complementaryPluginWithoutDependencies,
				pluginDependentOfModuleAB, pluginWithoutDependencies)).when(pluginManagerOfAnotherInstance)
				.getActivePluginModules();

		collectionsManager = getAppLayerFactory().getCollectionsManager();
		manager = (ConstellioModulesManagerImpl) getAppLayerFactory().getModulesManager();
		managerOfAnotherInstance = (ConstellioModulesManagerImpl) getAppLayerFactory("other-instance").getModulesManager();
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
		when(complementaryModuleAB.getId()).thenReturn("compModuleAB_Id");
		when(complementaryModuleAB.getName()).thenReturn("compModuleAB");
		when(complementaryModuleAB.isComplementary()).thenReturn(true);
		when(complementaryModuleAB.getDependencies()).thenReturn(Arrays.asList("moduleA_Id", "moduleB_Id"));

		when(complementaryPluginDependentOfModuleAB.getId()).thenReturn("compPluginDependentOfAB_id");
		when(complementaryPluginDependentOfModuleAB.getName()).thenReturn("compPluginDependentOfAB_name");
		when(complementaryPluginDependentOfModuleAB.isComplementary()).thenReturn(true);
		when(complementaryPluginDependentOfModuleAB.getDependencies()).thenReturn(Arrays.asList("moduleA_Id", "moduleB_Id"));

		when(complementaryPluginWithoutDependencies.getId()).thenReturn("compPluginWithoutDependencies_id");
		when(complementaryPluginWithoutDependencies.getName()).thenReturn("compPluginWithoutDependencies_name");
		when(complementaryPluginWithoutDependencies.isComplementary()).thenReturn(true);
		when(complementaryPluginWithoutDependencies.getDependencies()).thenReturn(new ArrayList<String>());

		when(pluginDependentOfModuleAB.getId()).thenReturn("pluginDependentOfAB_id");
		when(pluginDependentOfModuleAB.getName()).thenReturn("pluginDependentOfAB_name");
		when(pluginDependentOfModuleAB.isComplementary()).thenReturn(false);
		when(pluginDependentOfModuleAB.getDependencies()).thenReturn(Arrays.asList("moduleA_Id", "moduleB_Id"));

		when(pluginWithoutDependencies.getId()).thenReturn("pluginWithoutDependencies_id");
		when(pluginWithoutDependencies.getName()).thenReturn("pluginWithoutDependencies_name");
		when(pluginWithoutDependencies.isComplementary()).thenReturn(false);
		when(pluginWithoutDependencies.getDependencies()).thenReturn(new ArrayList<String>());

		inOrder = inOrder(moduleAMigrationScript111, moduleAMigrationScript112, moduleBMigrationScript111,
				moduleBMigrationScript112, moduleCMigrationScript111, moduleCMigrationScript112);

	}

	//TODO Fail on initialize if invalid modules

	@Test
	public void whenGetModulesByIdThenReturnCorrectModules() {
		assertThat(manager.getInstalledModule("moduleA_Id")).isEqualTo(moduleA);
		assertThat(manager.getInstalledModule("moduleB_Id")).isEqualTo(moduleB);

		assertThat(managerOfAnotherInstance.getInstalledModule("moduleA_Id")).isEqualTo(moduleA);
		assertThat(managerOfAnotherInstance.getInstalledModule("moduleB_Id")).isEqualTo(moduleB);
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
		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);
		manager.installValidModuleAndGetInvalidOnes(moduleB, collectionsListManager);

		manager.enableValidModuleAndGetInvalidOnes("collection1", moduleA);
		manager.enableValidModuleAndGetInvalidOnes("collection1", moduleB);
		manager.enableValidModuleAndGetInvalidOnes("collection2", moduleB);
		assertThat(manager.getEnabledModules("collection1")).containsOnly(moduleA, moduleB, complementaryModuleAB,
				complementaryPluginWithoutDependencies, complementaryPluginDependentOfModuleAB);
		assertThat(manager.getEnabledModules("collection2")).containsOnly(moduleB, complementaryPluginWithoutDependencies);

		assertThat(managerOfAnotherInstance.getEnabledModules("collection1"))
				.containsOnly(moduleA, moduleB, complementaryModuleAB,
						complementaryPluginWithoutDependencies, complementaryPluginDependentOfModuleAB);
		assertThat(managerOfAnotherInstance.getEnabledModules("collection2"))
				.containsOnly(moduleB, complementaryPluginWithoutDependencies);

		ConstellioModulesManagerImpl otherManager = new ConstellioModulesManagerImpl(getAppLayerFactory(),
				pluginManager, new Delayed<>(migrationServices));
		otherManager.initialize();

		assertThat(otherManager.getEnabledModules("collection1")).containsOnly(moduleA, moduleB, complementaryModuleAB,
				complementaryPluginWithoutDependencies, complementaryPluginDependentOfModuleAB);
		assertThat(otherManager.getEnabledModules("collection2")).containsOnly(moduleB, complementaryPluginWithoutDependencies);

	}

	@Test
	public void whenEnablingAllDependenciesThenComplementaryModuleEnabled()
			throws Exception {
		collectionsManager
				.createCollectionInCurrentVersion("collection1", Arrays.asList("fr"));
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);
		manager.installValidModuleAndGetInvalidOnes(moduleB, collectionsListManager);
		manager.installValidModuleAndGetInvalidOnes(complementaryModuleAB, collectionsListManager);

		manager.enableValidModuleAndGetInvalidOnes("collection1", moduleA);
		assertThat(manager.getEnabledModules("collection1")).containsOnly(moduleA, complementaryPluginWithoutDependencies);
		assertThat(manager.getEnabledModules("collection1")).containsOnly(moduleA, complementaryPluginWithoutDependencies);

		assertThat(managerOfAnotherInstance.getEnabledModules("collection1"))
				.containsOnly(moduleA, complementaryPluginWithoutDependencies);
		assertThat(managerOfAnotherInstance.getEnabledModules("collection1"))
				.containsOnly(moduleA, complementaryPluginWithoutDependencies);

		manager.enableValidModuleAndGetInvalidOnes("collection1", moduleB);
		assertThat(manager.getEnabledModules("collection1")).containsOnly(moduleA, moduleB, complementaryModuleAB,
				complementaryPluginDependentOfModuleAB, complementaryPluginWithoutDependencies);

		assertThat(managerOfAnotherInstance.getEnabledModules("collection1"))
				.containsOnly(moduleA, moduleB, complementaryModuleAB,
						complementaryPluginDependentOfModuleAB, complementaryPluginWithoutDependencies);
	}

	@Test
	public void givenMultipleCollectionsThenEnableStatusIsHandledSeparately()
			throws Exception {
		collectionsManager
				.createCollectionInCurrentVersion("collection1", Arrays.asList("fr"));
		collectionsManager
				.createCollectionInCurrentVersion("collection2", Arrays.asList("fr"));
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);
		manager.installValidModuleAndGetInvalidOnes(moduleB, collectionsListManager);
		assertThat(manager.getEnabledModules("collection1")).isEmpty();
		assertThat(manager.getEnabledModules("collection2")).isEmpty();
		assertThat(managerOfAnotherInstance.getEnabledModules("collection1")).isEmpty();
		assertThat(managerOfAnotherInstance.getEnabledModules("collection2")).isEmpty();

		manager.enableValidModuleAndGetInvalidOnes("collection1", moduleA);
		manager.enableValidModuleAndGetInvalidOnes("collection1", moduleB);
		manager.enableValidModuleAndGetInvalidOnes("collection2", moduleB);
		assertThat(manager.getEnabledModules("collection1")).containsOnly(moduleA, moduleB, complementaryModuleAB,
				complementaryPluginWithoutDependencies, complementaryPluginDependentOfModuleAB);
		assertThat(manager.getEnabledModules("collection2")).containsOnly(moduleB, complementaryPluginWithoutDependencies);

		assertThat(managerOfAnotherInstance.getEnabledModules("collection1"))
				.containsOnly(moduleA, moduleB, complementaryModuleAB,
						complementaryPluginWithoutDependencies, complementaryPluginDependentOfModuleAB);
		assertThat(managerOfAnotherInstance.getEnabledModules("collection2"))
				.containsOnly(moduleB, complementaryPluginWithoutDependencies);

		manager.disableModule("collection1", moduleA);
		assertThat(manager.getEnabledModules("collection1")).containsOnly(moduleB, complementaryModuleAB,
				complementaryPluginWithoutDependencies, complementaryPluginDependentOfModuleAB);
		assertThat(manager.getEnabledModules("collection2")).containsOnly(moduleB, complementaryPluginWithoutDependencies);

		assertThat(managerOfAnotherInstance.getEnabledModules("collection1")).containsOnly(moduleB, complementaryModuleAB,
				complementaryPluginWithoutDependencies, complementaryPluginDependentOfModuleAB);
		assertThat(managerOfAnotherInstance.getEnabledModules("collection2"))
				.containsOnly(moduleB, complementaryPluginWithoutDependencies);
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

		assertThat(managerOfAnotherInstance.getAllModules()).contains(moduleA, moduleB);
		assertThat(managerOfAnotherInstance.getModulesAvailableForInstallation()).contains(moduleA, moduleB);
		assertThat(managerOfAnotherInstance.getEnabledModules(zeCollection)).isEmpty();
		assertThat(managerOfAnotherInstance.getDisabledModules(zeCollection)).isEmpty();
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleB)).isFalse();
	}

	@Test
	public void whenInstallModuleThenMigrationScriptsNotRunAndAppearAsDisabled()
			throws Exception {
		givenCollection(zeCollection);
		givenCollection("anotherCollection");
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");
		migrationServices.setCurrentDataVersion("anotherCollection", "11.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);

		verify(moduleAMigrationScript111, never()).migrate(eq("zeCollection"), any(MigrationResourcesProvider.class),
				any(AppLayerFactory.class));
		verify(moduleAMigrationScript112, never()).migrate(eq("zeCollection"), any(MigrationResourcesProvider.class),
				any(AppLayerFactory.class));
		verify(moduleAMigrationScript111, never()).migrate(eq("anotherCollection"), any(MigrationResourcesProvider.class),
				any(AppLayerFactory.class));
		verify(moduleAMigrationScript112, never()).migrate(eq("anotherCollection"), any(MigrationResourcesProvider.class),
				any(AppLayerFactory.class));
		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).isEmpty();
		assertThat(manager.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();

		assertThat(managerOfAnotherInstance.getAllModules()).contains(moduleA, moduleB);
		assertThat(managerOfAnotherInstance.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(managerOfAnotherInstance.getEnabledModules(zeCollection)).isEmpty();
		assertThat(managerOfAnotherInstance.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleB)).isFalse();
	}

	@Test
	public void whenInstallModuleWithDependenciesThenInstallDependencies()
			throws Exception {
		doReturn(Arrays.asList(moduleA, moduleB, moduleC)).when(pluginManager).getRegisteredModules();
		doReturn(Arrays.asList(moduleA, moduleB, moduleC)).when(pluginManager).getRegistredModulesAndActivePlugins();
		when(moduleC.getDependencies()).thenReturn(asList("moduleB_Id"));
		givenCollection("zeCollection");
		givenCollection("anotherCollection");
		manager.markAsInstalled(moduleC, collectionsListManager);
		manager.markAsEnabled(moduleC, zeCollection);
		manager.markAsEnabled(moduleC, "anotherCollection");
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");
		migrationServices.setCurrentDataVersion("anotherCollection", "11.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleC, collectionsListManager);
		manager.enableValidModuleAndGetInvalidOnes(zeCollection, moduleC);

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
		doReturn(Arrays.asList(moduleA, moduleB, moduleC)).when(pluginManager).getRegistredModulesAndActivePlugins();
		when(moduleC.getDependencies()).thenReturn(asList("moduleB_Id"));
		givenCollection("zeCollection");
		givenCollection("anotherCollection");
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");
		migrationServices.setCurrentDataVersion("anotherCollection", "11.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleB, collectionsListManager);
		manager.installValidModuleAndGetInvalidOnes(moduleC, collectionsListManager);
		manager.enableValidModuleAndGetInvalidOnes("zeCollection", moduleB);
		manager.enableValidModuleAndGetInvalidOnes("zeCollection", moduleC);

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

		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);

		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).isEmpty();
		assertThat(manager.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();

		assertThat(managerOfAnotherInstance.getAllModules()).contains(moduleA, moduleB);
		assertThat(managerOfAnotherInstance.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(managerOfAnotherInstance.getEnabledModules(zeCollection)).isEmpty();
		assertThat(managerOfAnotherInstance.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleB)).isFalse();
	}

	@Test
	public void whenInstallModuleOnNewSystemThenNoMigrationScriptsRunedAndAppearAsEnabled()
			throws Exception {
		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);

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

		assertThat(managerOfAnotherInstance.getAllModules()).contains(moduleA, moduleB);
		assertThat(managerOfAnotherInstance.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(managerOfAnotherInstance.getEnabledModules(zeCollection)).isEmpty();
		assertThat(managerOfAnotherInstance.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleB)).isFalse();
	}

	@Test
	public void whenEnablingAModuleThenAppearAsEnabled()
			throws Exception {
		givenCollection(zeCollection);
		migrationServices.setCurrentDataVersion(zeCollection, "1.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);
		manager.enableValidModuleAndGetInvalidOnes(zeCollection, moduleA);

		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.getDisabledModules(zeCollection)).isEmpty();
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isTrue();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();

		assertThat(managerOfAnotherInstance.getAllModules()).contains(moduleA, moduleB);
		assertThat(managerOfAnotherInstance.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(managerOfAnotherInstance.getEnabledModules(zeCollection)).contains(moduleA);
		assertThat(managerOfAnotherInstance.getDisabledModules(zeCollection)).isEmpty();
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleA)).isTrue();
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleB)).isFalse();

	}

	@Test
	public void whenDisablingAModuleThenAppearAsDisabled()
			throws Exception {
		givenCollection("zeCollection");
		migrationServices.setCurrentDataVersion(zeCollection, "1.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);
		manager.enableValidModuleAndGetInvalidOnes(zeCollection, moduleA);
		assertThat(manager.getEnabledModules(zeCollection)).containsOnly(moduleA, complementaryPluginWithoutDependencies);
		manager.disableModule(zeCollection, moduleA);

		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).containsOnly(complementaryPluginWithoutDependencies);
		assertThat(manager.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();

		assertThat(managerOfAnotherInstance.getAllModules()).contains(moduleA, moduleB);
		assertThat(managerOfAnotherInstance.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(managerOfAnotherInstance.getEnabledModules(zeCollection)).containsOnly(complementaryPluginWithoutDependencies);
		assertThat(managerOfAnotherInstance.getDisabledModules(zeCollection)).contains(moduleA);
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleA)).isFalse();
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleB)).isFalse();
	}

	@Test
	public void whenReenablingAModuleThenAppearAsEnabled()
			throws Exception {
		givenCollection("zeCollection");
		givenCollection("anotherCollection");
		migrationServices.setCurrentDataVersion("zeCollection", "11.1.2");
		migrationServices.setCurrentDataVersion("anotherCollection", "11.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);
		manager.enableValidModuleAndGetInvalidOnes(zeCollection, moduleA);
		manager.disableModule(zeCollection, moduleA);
		manager.enableValidModuleAndGetInvalidOnes(zeCollection, moduleA);

		verify(moduleAMigrationScript111).migrate(
				eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		verify(moduleAMigrationScript112)
				.migrate(eq("zeCollection"), any(MigrationResourcesProvider.class), any(AppLayerFactory.class));
		assertThat(manager.getAllModules()).contains(moduleA, moduleB);
		assertThat(manager.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(manager.getEnabledModules(zeCollection)).contains(moduleA);
		assertThat(manager.getDisabledModules(zeCollection)).isEmpty();
		assertThat(manager.isModuleEnabled(zeCollection, moduleA)).isTrue();
		assertThat(manager.isModuleEnabled(zeCollection, moduleB)).isFalse();

		assertThat(managerOfAnotherInstance.getAllModules()).contains(moduleA, moduleB);
		assertThat(managerOfAnotherInstance.getModulesAvailableForInstallation()).contains(moduleB);
		assertThat(managerOfAnotherInstance.getEnabledModules(zeCollection)).contains(moduleA);
		assertThat(managerOfAnotherInstance.getDisabledModules(zeCollection)).isEmpty();
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleA)).isTrue();
		assertThat(managerOfAnotherInstance.isModuleEnabled(zeCollection, moduleB)).isFalse();
	}

	@Test
	public void givenModuleAEnabledThenStartedWhenStartCalled()
			throws Exception {
		givenCollection(zeCollection);
		migrationServices.setCurrentDataVersion("zeCollection", "1.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);
		manager.enableValidModuleAndGetInvalidOnes(zeCollection, moduleA);

		verify(moduleA, times(1)).start(eq(zeCollection), any(AppLayerFactory.class));
		verify(moduleB, never()).start(eq(zeCollection), any(AppLayerFactory.class));
	}

	@Test
	public void givenModuleAEnabledThenStoppedWhenStopCalled()
			throws Exception {
		givenCollection(zeCollection);
		migrationServices.setCurrentDataVersion(zeCollection, "1.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);
		manager.enableValidModuleAndGetInvalidOnes(zeCollection, moduleA);

		manager.stopModules(zeCollection);

		verify(moduleA, times(1)).stop(eq(zeCollection), any(AppLayerFactory.class));
		verify(moduleB, never()).stop(eq(zeCollection), any(AppLayerFactory.class));
	}

	@Test
	public void givenModuleEnabledTwiceThenOnlyStartedOnce()
			throws Exception {
		givenCollection(zeCollection);
		migrationServices.setCurrentDataVersion(zeCollection, "1.1.2");

		manager.installValidModuleAndGetInvalidOnes(moduleA, collectionsListManager);
		manager.enableValidModuleAndGetInvalidOnes(zeCollection, moduleA);
		manager.enableValidModuleAndGetInvalidOnes(zeCollection, moduleA);

		verify(moduleA, times(1)).start(eq(zeCollection), any(AppLayerFactory.class));
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

		assertThat(managerOfAnotherInstance.getPermissionGroups(zeCollection))
				.has(permissionGroupStartingWith("core."))
				.doesNotHave(permissionGroupStartingWith("rm."));

		assertThat(managerOfAnotherInstance.getPermissionGroups("anotherCollection"))
				.has(permissionGroupStartingWith("core."))
				.has(permissionGroupStartingWith("rm."));

		assertThat(managerOfAnotherInstance.getPermissionsInGroup("anotherCollection", "core.management.collection"))
				.contains("core.manageTaxonomies");

		assertThat(managerOfAnotherInstance.getPermissionsInGroup("anotherCollection", "rm.folders"))
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
