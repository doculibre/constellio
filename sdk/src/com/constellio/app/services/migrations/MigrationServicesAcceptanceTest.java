package com.constellio.app.services.migrations;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MigrationServicesAcceptanceTest extends ConstellioTest {
	ConstellioPluginManager pluginManager;
	ModelLayerFactory modelLayerFactory;
	AppLayerFactory appLayerFactory;
	DataLayerFactory dataLayerFactory;
	ConfigManager configManager;

	PropertiesConfiguration propertiesConfig;
	com.constellio.app.services.migrations.MigrationServices migrationServices;

	@Mock SystemGlobalConfigsManager systemGlobalConfigsManager;
	@Mock ConstellioEIM constellioEIM;
	@Mock MigrationScript coreMigrationTo100;
	@Mock MigrationScript coreMigrationTo103;
	@Mock MigrationScript coreMigrationTo110;

	@Mock ConstellioModulesManagerImpl moduleManager;
	String aModuleId = "aModuleId";
	String aModuleWithDependencyId = "aModuleWithDependencyId";
	InstallableModule aModule, aModuleWithDependency;
	@Mock MigrationScript aModuleMigrationTo100;
	@Mock MigrationScript aModuleMigrationTo102;
	@Mock MigrationScript aModuleMigrationTo110;
	@Mock MigrationScript aModuleWithDependencyMigrationTo100;
	@Mock MigrationScript aModuleWithDependencyMigrationTo101;
	@Mock MigrationScript aModuleWithDependencyMigrationTo106;
	InOrder inOrder;

	@Mock MigrationScript moduleAMigrationTo100, moduleBMigrationTo100, moduleCMigrationTo100, moduleDMigrationTo100, moduleEMigrationTo100, moduleFMigrationTo100;

	@Before
	public void setUp() {
		pluginManager = getAppLayerFactory().getPluginManager();
		configManager = getDataLayerFactory().getConfigManager();
		dataLayerFactory = getDataLayerFactory();
		modelLayerFactory = getModelLayerFactory();
		appLayerFactory = getAppLayerFactory();

		aModule = givenModuleWithId(aModuleId);
		aModuleWithDependency = givenModuleWithId(aModuleWithDependencyId);

		migrationServices = new com.constellio.app.services.migrations.MigrationServices(constellioEIM, appLayerFactory,
				moduleManager, pluginManager);
		when(coreMigrationTo100.getVersion()).thenReturn("1.0.0");
		when(coreMigrationTo103.getVersion()).thenReturn("1.0.3");
		when(coreMigrationTo110.getVersion()).thenReturn("1.1.0");
		when(constellioEIM.getMigrationScripts()).thenReturn(
				Arrays.asList(coreMigrationTo100, coreMigrationTo103, coreMigrationTo110));

		when(aModuleMigrationTo100.getVersion()).thenReturn("1.0.0");
		when(aModuleMigrationTo102.getVersion()).thenReturn("1.0.2");
		when(aModuleMigrationTo110.getVersion()).thenReturn("1.1.0");
		when(aModule.getMigrationScripts()).thenReturn(
				Arrays.asList(aModuleMigrationTo100, aModuleMigrationTo102, aModuleMigrationTo110));

		when(aModuleWithDependencyMigrationTo100.getVersion()).thenReturn("1.0.0");
		when(aModuleWithDependencyMigrationTo101.getVersion()).thenReturn("1.0.1");
		when(aModuleWithDependencyMigrationTo106.getVersion()).thenReturn("1.0.6");
		when(aModuleWithDependency.getMigrationScripts()).thenReturn(
				Arrays.asList(aModuleWithDependencyMigrationTo100, aModuleWithDependencyMigrationTo101,
						aModuleWithDependencyMigrationTo106));

		when(aModuleWithDependency.getDependencies()).thenReturn(Arrays.asList(aModuleId));

		when(moduleAMigrationTo100.getVersion()).thenReturn("1.0.0");
		when(moduleBMigrationTo100.getVersion()).thenReturn("1.0.0");
		when(moduleCMigrationTo100.getVersion()).thenReturn("1.0.0");
		when(moduleDMigrationTo100.getVersion()).thenReturn("1.0.0");
		when(moduleEMigrationTo100.getVersion()).thenReturn("1.0.0");
		when(moduleFMigrationTo100.getVersion()).thenReturn("1.0.0");

		inOrder = inOrder(coreMigrationTo100, coreMigrationTo103, coreMigrationTo110, aModuleMigrationTo100,
				aModuleMigrationTo102, aModuleMigrationTo110, aModuleWithDependencyMigrationTo100,
				aModuleWithDependencyMigrationTo101, aModuleWithDependencyMigrationTo106, moduleAMigrationTo100,
				moduleBMigrationTo100, moduleCMigrationTo100, moduleDMigrationTo100, moduleEMigrationTo100,
				moduleFMigrationTo100);

	}

	//@Test
	// This behaviour is no longer required anywhere
	public void givenMultipleCollectionsThenMigratedIndependently()
			throws Exception {
		when(moduleManager.getEnabledModules(zeCollection)).thenReturn(Arrays.asList(aModule));

		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		CollectionsManager collectionsManager = spy(
				new CollectionsManager(getAppLayerFactory(), moduleManager, new Delayed<>(migrationServices),
						systemGlobalConfigsManager));
		collectionsManager.createCollectionConfigs("collection1");
		collectionsListManager.addCollection("collection1", Arrays.asList("fr"), (byte) 42);
		try {
			migrationServices.migrate("collection1", null, false);
		} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
			throw new RuntimeException(optimisticLockingConfiguration);
		}

		collectionsManager.createCollectionConfigs("collection2");
		collectionsListManager.addCollection("collection2", Arrays.asList("fr"), (byte) 68);
		try {
			migrationServices.migrate("collection2", null, false);
		} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
			throw new RuntimeException(optimisticLockingConfiguration);
		}

		inOrder.verify(coreMigrationTo100)
				.migrate(eq("collection1"), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo100)
				.migrate(eq("collection1"), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo102)
				.migrate(eq("collection1"), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(coreMigrationTo103)
				.migrate(eq("collection1"), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(coreMigrationTo110)
				.migrate(eq("collection1"), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo110)
				.migrate(eq("collection1"), any(MigrationResourcesProvider.class), eq(appLayerFactory));

		inOrder.verify(coreMigrationTo100)
				.migrate(eq("collection2"), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo100).migrate(eq("collection2"), any(MigrationResourcesProvider.class), eq(
				appLayerFactory));
		inOrder.verify(aModuleMigrationTo102).migrate(eq("collection2"), any(MigrationResourcesProvider.class), eq(
				appLayerFactory));
		inOrder.verify(coreMigrationTo103)
				.migrate(eq("collection2"), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(coreMigrationTo110)
				.migrate(eq("collection2"), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo110).migrate(eq("collection2"), any(MigrationResourcesProvider.class), eq(
				appLayerFactory));

		assertThat(migrationServices.getCurrentVersion("collection1")).isEqualTo("1.1.0");
		assertThat(migrationServices.getCurrentVersion("collection2")).isEqualTo("1.1.0");

	}

	@Test
	public void whenMigrateToVersionThenMigrationDone()
			throws Exception {
		givenCollection(zeCollection);
		when(moduleManager.getEnabledModules(zeCollection)).thenReturn(Arrays.asList(aModule));

		migrationServices.setCurrentDataVersion(zeCollection, "0.9.9");

		migrationServices.migrate(zeCollection, "1.1.0", false);
		inOrder.verify(coreMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo102)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(coreMigrationTo103)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(coreMigrationTo110)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo110)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));

		assertThat(migrationServices.getCurrentVersion(zeCollection)).isEqualTo("1.1.0");
	}

	@Test
	public void givenModuleWithDependencyThenDepencyAlwaysMigratedBefore()
			throws Exception {
		givenCollection(zeCollection);
		when(moduleManager.getEnabledModules(zeCollection)).thenReturn(Arrays.asList(aModuleWithDependency, aModule));

		migrationServices.setCurrentDataVersion(zeCollection, "0.9.9");

		migrationServices.migrate(zeCollection, "1.1.0", false);

		inOrder.verify(coreMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleWithDependencyMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleWithDependencyMigrationTo101)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo102)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(coreMigrationTo103)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleWithDependencyMigrationTo106)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(coreMigrationTo110)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(aModuleMigrationTo110)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));

		assertThat(migrationServices.getCurrentVersion(zeCollection)).isEqualTo("1.1.0");
	}

	@Test
	public void givenModuleWithADependencyToAnotherModuleWithADependencyThenDepencyAlwaysMigratedBefore()
			throws Exception {
		givenCollection(zeCollection);

		when(constellioEIM.getMigrationScripts()).thenReturn(Arrays.asList(coreMigrationTo100, coreMigrationTo103));
		InstallableModule moduleA = givenModuleWithIdAndMigrationScripts("a", moduleAMigrationTo100);
		InstallableModule moduleB = givenModuleWithIdAndMigrationScripts("b", moduleBMigrationTo100);
		InstallableModule moduleC = givenModuleWithIdAndMigrationScripts("c", moduleCMigrationTo100);
		InstallableModule moduleD = givenModuleWithIdAndMigrationScripts("d", moduleDMigrationTo100);
		InstallableModule moduleE = givenModuleWithIdAndMigrationScripts("e", moduleEMigrationTo100);
		InstallableModule moduleF = givenModuleWithIdAndMigrationScripts("f", moduleFMigrationTo100);
		//b -> a,c
		//a -> d,e
		//e -> f
		//f -> d
		//d -> c
		when(moduleB.getDependencies()).thenReturn(asList("a", "c"));
		when(moduleA.getDependencies()).thenReturn(asList("d", "e"));
		when(moduleE.getDependencies()).thenReturn(asList("f"));
		when(moduleF.getDependencies()).thenReturn(asList("d"));
		when(moduleD.getDependencies()).thenReturn(asList("c"));
		when(moduleManager.getEnabledModules(zeCollection))
				.thenReturn(Arrays.asList(moduleA, moduleB, moduleC, moduleD, moduleE, moduleF));

		migrationServices.setCurrentDataVersion(zeCollection, "0.9.9");
		migrationServices.migrate(zeCollection, "1.1.0", false);

		inOrder.verify(coreMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(moduleCMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(moduleDMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(moduleFMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(moduleEMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(moduleAMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(moduleBMigrationTo100)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));
		inOrder.verify(coreMigrationTo103)
				.migrate(eq(zeCollection), any(MigrationResourcesProvider.class), eq(appLayerFactory));

		assertThat(migrationServices.getCurrentVersion(zeCollection)).isEqualTo("1.0.3");
	}

	//TODO Validate module has migration scripts of different versions
	//TODO Validate no multiple modules with same id

	private InstallableModule givenModuleWithIdAndMigrationScripts(String id, MigrationScript... scripts) {
		InstallableModule constellioModule = givenModuleWithId(id);
		when(constellioModule.getMigrationScripts()).thenReturn(Arrays.asList(scripts));
		return constellioModule;
	}

	private InstallableModule givenModuleWithId(String id) {
		InstallableModule constellioModule = mock(InstallableModule.class, id);
		when(constellioModule.getId()).thenReturn(id);
		when(moduleManager.getInstalledModule(id)).thenReturn(constellioModule);
		return constellioModule;
	}
}
